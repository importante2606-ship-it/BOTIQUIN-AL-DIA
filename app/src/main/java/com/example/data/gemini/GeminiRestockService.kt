package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Fuente del resultado del análisis de IA.
 */
enum class ResultSource {
    GEMINI_API,
    LOCAL_FALLBACK,
    MOCK_TEST
}

/**
 * Representa un producto priorizado por la IA para su reposición.
 */
data class PrioritizedRestockItem(
    val name: String,
    val priority: String, // URGENTE, ALTA, MEDIA
    val category: String,
    val medicalReason: String
)

/**
 * Representa una advertencia de incompatibilidad o almacenamiento seguro entre productos.
 */
data class StorageWarning(
    val productA: String,
    val productB: String,
    val dangerLevel: String, // ALTO, MEDIO, PRECAUCION
    val recommendation: String
)

/**
 * Estructura fija retornada tras el análisis.
 */
data class GeminiRestockResult(
    val prioritizedItems: List<PrioritizedRestockItem>,
    val storageWarnings: List<StorageWarning>,
    val source: ResultSource,
    val statusMessage: String
)

/**
 * Servicio para integrar el Sello de IA con la API de Gemini (gemini-3.5-flash)
 * con salida estructurada en formato JSON mediante responseSchema.
 */
object GeminiRestockService {

    private const val TAG = "GeminiRestockService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    // OkHttpClient configurado con 60 segundos de timeout según especificación
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Requisito 1: Esquema JSON fijo (responseSchema) para Gemini.
     */
    val RESPONSE_SCHEMA_JSON = """
    {
      "type": "OBJECT",
      "properties": {
        "prioritizedItems": {
          "type": "ARRAY",
          "items": {
            "type": "OBJECT",
            "properties": {
              "name": { "type": "STRING", "description": "Nombre del producto a reponer" },
              "priority": { "type": "STRING", "description": "Nivel de urgencia médica: URGENTE, ALTA, o MEDIA" },
              "category": { "type": "STRING", "description": "Categoría farmacológica o uso" },
              "medicalReason": { "type": "STRING", "description": "Motivo conciso de por qué debe reponerse con esa prioridad" }
            },
            "required": ["name", "priority", "category", "medicalReason"]
          }
        },
        "storageWarnings": {
          "type": "ARRAY",
          "items": {
            "type": "OBJECT",
            "properties": {
              "productA": { "type": "STRING", "description": "Primer producto o categoría incompatible" },
              "productB": { "type": "STRING", "description": "Segundo producto o factor de riesgo" },
              "dangerLevel": { "type": "STRING", "description": "Nivel de riesgo: ALTO, MEDIO, o PRECAUCION" },
              "recommendation": { "type": "STRING", "description": "Indicación precisa de separación física o almacenamiento correcto" }
            },
            "required": ["productA", "productB", "dangerLevel", "recommendation"]
          }
        }
      },
      "required": ["prioritizedItems", "storageWarnings"]
    }
    """.trimIndent()

    /**
     * Requisito 5: Ejemplo de respuesta de prueba estática (Mock) para desarrollar
     * y probar la interfaz sin consumir llamadas ni requerir una API Key activa.
     */
    val MOCK_RESPONSE_JSON = """
    {
      "prioritizedItems": [
        {
          "name": "Ibuprofeno 400mg",
          "priority": "URGENTE",
          "category": "Analgésico / Antinflamatorio",
          "medicalReason": "Básico para alivio rápido de dolor agudo, fiebre o inflamación en el hogar."
        },
        {
          "name": "Gasas estériles",
          "priority": "ALTA",
          "category": "Curación básica",
          "medicalReason": "Imprescindibles para contener heridas sangrantes y prevenir infecciones inmediatas."
        },
        {
          "name": "Alcohol 70%",
          "priority": "MEDIA",
          "category": "Antiséptico",
          "medicalReason": "Útil para desinfección de material e instrumental; no colocar directo en heridas abiertas."
        }
      ],
      "storageWarnings": [
        {
          "productA": "Alcohol / Antisépticos líquidos",
          "productB": "Gasas estériles sin sellar",
          "dangerLevel": "ALTO",
          "recommendation": "Guardar líquidos inflamables en compartimentos inferiores y apósitos estériles en bolsa hermética para evitar contaminación por vapores o derrames."
        },
        {
          "productA": "Medicamentos fotosensibles (Ibuprofeno)",
          "productB": "Luz solar directa o humedad",
          "dangerLevel": "MEDIO",
          "recommendation": "Mantener siempre dentro de su caja original de cartón y lejos de fuentes de calor o vapor del baño."
        }
      ]
    }
    """.trimIndent()

    /**
     * Analiza los productos para reponer y advierte sobre incompatibilidades de guardado.
     * Si la clave de API no está configurada, falla la red o el esquema es inválido,
     * se activa automáticamente el mecanismo de recuperación (Requisito 4).
     */
    suspend fun analyzeRestock(
        productsToRestock: List<ProductEntity>,
        allCabinetProducts: List<ProductEntity>
    ): GeminiRestockResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()

        // Verificar si la clave es la por defecto o está vacía
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Clave GEMINI_API_KEY no configurada en .env o Secrets panel. Usando fallback.")
            return@withContext getLocalFallbackResult(
                productsToRestock = productsToRestock,
                statusMessage = "Modo sin conexión: Clave GEMINI_API_KEY no configurada. Mostrando reglas sanitarias locales predefinidas."
            )
        }

        try {
            val promptText = buildPrompt(productsToRestock, allCabinetProducts)

            // Construir el cuerpo de la petición con responseSchema
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("responseSchema", JSONObject(RESPONSE_SCHEMA_JSON))
                    put("temperature", 0.2)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorCode = response.code
                val errorBody = response.body?.string() ?: "Sin detalle"
                Log.e(TAG, "Error HTTP $errorCode de Gemini API: $errorBody")
                return@withContext getLocalFallbackResult(
                    productsToRestock = productsToRestock,
                    statusMessage = "Fallo de conexión HTTP $errorCode con Gemini API. Se activó el respaldo local."
                )
            }

            val responseString = response.body?.string() ?: ""
            val parsedResult = parseGeminiResponse(responseString)

            return@withContext GeminiRestockResult(
                prioritizedItems = parsedResult.first,
                storageWarnings = parsedResult.second,
                source = ResultSource.GEMINI_API,
                statusMessage = "✨ Análisis generado en tiempo real con Gemini 3.5 Flash."
            )

        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante la llamada a Gemini API: ${e.message}", e)
            return@withContext getLocalFallbackResult(
                productsToRestock = productsToRestock,
                statusMessage = "No se pudo comunicar con Gemini (${e.localizedMessage ?: "Timeout / Red"}). Respaldo local activado."
            )
        }
    }

    /**
     * Devuelve el resultado del ejemplo de prueba Mock (Requisito 5).
     */
    fun getMockTestResult(): GeminiRestockResult {
        val (items, warnings) = parseStructuredJson(MOCK_RESPONSE_JSON)
        return GeminiRestockResult(
            prioritizedItems = items,
            storageWarnings = warnings,
            source = ResultSource.MOCK_TEST,
            statusMessage = "🧪 Modo de prueba activo: Datos simulados sin costo ni consumo de API."
        )
    }

    /**
     * Construye el prompt con los productos del botiquín.
     */
    private fun buildPrompt(
        restockList: List<ProductEntity>,
        allCabinetList: List<ProductEntity>
    ): String {
        val restockNames = if (restockList.isNotEmpty()) {
            restockList.joinToString(", ") { "${it.name} (Categoría: ${it.category}, Stock actual: ${it.quantity} ${it.unit})" }
        } else {
            "Ninguno específico agotado (generar reposición preventiva para botiquín básico familiar)"
        }

        val allNames = allCabinetList.joinToString(", ") { "${it.name} (${it.category})" }

        return """
        Actuá como un farmacéutico y especialista en seguridad de botiquines domiciliarios.
        
        PRODUCTOS QUE FALTAN O ESTÁN VENCIDOS PARA REPONER:
        $restockNames
        
        TODOS LOS PRODUCTOS PRESENTES EN EL BOTIQUÍN:
        $allNames
        
        TAREA:
        1. Ordená los productos a reponer por prioridad de necesidad sanitaria (URGENTE, ALTA, MEDIA) con una justificación médica concisa.
        2. Analizá los productos guardados y genera advertencias concretas de incompatibilidad o almacenamiento (por ejemplo: líquidos corrosivos o inflamables cerca de apósitos, o medicamentos fotosensibles expuestos a luz o humedad).
        
        Respondé respetando rigurosamente el esquema JSON provisto.
        """.trimIndent()
    }

    /**
     * Parsea la respuesta del contenedor candidates de Gemini.
     */
    private fun parseGeminiResponse(jsonText: String): Pair<List<PrioritizedRestockItem>, List<StorageWarning>> {
        val root = JSONObject(jsonText)
        val candidates = root.optJSONArray("candidates") ?: return Pair(emptyList(), emptyList())
        val firstCandidate = candidates.optJSONObject(0) ?: return Pair(emptyList(), emptyList())
        val content = firstCandidate.optJSONObject("content") ?: return Pair(emptyList(), emptyList())
        val parts = content.optJSONArray("parts") ?: return Pair(emptyList(), emptyList())
        val firstPart = parts.optJSONObject(0) ?: return Pair(emptyList(), emptyList())
        val text = firstPart.optString("text", "{}")

        return parseStructuredJson(text)
    }

    /**
     * Parsea el JSON estructurado según el responseSchema.
     */
    fun parseStructuredJson(jsonText: String): Pair<List<PrioritizedRestockItem>, List<StorageWarning>> {
        val cleanText = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val root = JSONObject(cleanText)

        val items = mutableListOf<PrioritizedRestockItem>()
        val itemsArray = root.optJSONArray("prioritizedItems")
        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val obj = itemsArray.getJSONObject(i)
                items.add(
                    PrioritizedRestockItem(
                        name = obj.optString("name", "Producto"),
                        priority = obj.optString("priority", "MEDIA"),
                        category = obj.optString("category", "General"),
                        medicalReason = obj.optString("medicalReason", "Reponer por seguridad del botiquín.")
                    )
                )
            }
        }

        val warnings = mutableListOf<StorageWarning>()
        val warningsArray = root.optJSONArray("storageWarnings")
        if (warningsArray != null) {
            for (i in 0 until warningsArray.length()) {
                val obj = warningsArray.getJSONObject(i)
                warnings.add(
                    StorageWarning(
                        productA = obj.optString("productA", "Producto"),
                        productB = obj.optString("productB", "Ambiente"),
                        dangerLevel = obj.optString("dangerLevel", "PRECAUCION"),
                        recommendation = obj.optString("recommendation", "Mantener en lugar seco y fresco.")
                    )
                )
            }
        }

        return Pair(items, warnings)
    }

    /**
     * Fallback local en caso de error de conexión, timeout o esquema inválido.
     */
    private fun getLocalFallbackResult(
        productsToRestock: List<ProductEntity>,
        statusMessage: String
    ): GeminiRestockResult {
        val items = productsToRestock.map { product ->
            val priority = when {
                product.isExpired() -> "URGENTE"
                product.quantity <= 2 && product.category.contains("Analgésico", ignoreCase = true) -> "URGENTE"
                product.category.contains("Curación", ignoreCase = true) -> "ALTA"
                else -> "MEDIA"
            }
            val reason = when (priority) {
                "URGENTE" -> if (product.isExpired()) "Producto vencido en botiquín: reemplazar inmediatamente para evitar riesgos toxicológicos." else "Insumo analgésico de primera línea agotado."
                "ALTA" -> "Material de primera curación esencial para accidentes domésticos."
                else -> "Suministro general para mantenimiento del inventario del hogar."
            }
            PrioritizedRestockItem(
                name = product.name,
                priority = priority,
                category = product.category,
                medicalReason = reason
            )
        }

        val warnings = listOf(
            StorageWarning(
                productA = "Antisépticos (Alcohol / Yodo)",
                productB = "Gasas y apósitos estériles",
                dangerLevel = "ALTO",
                recommendation = "Almacenar los líquidos en un nivel inferior para evitar que una pérdida contamine el material estéril de curación."
            ),
            StorageWarning(
                productA = "Comprimidos y cápsulas",
                productB = "Humedad del baño / Cocina",
                dangerLevel = "MEDIO",
                recommendation = "El botiquín debe estar en un ambiente seco (habitación o pasillo), nunca en el baño donde el vapor degrada los principios activos."
            )
        )

        return GeminiRestockResult(
            prioritizedItems = items,
            storageWarnings = warnings,
            source = ResultSource.LOCAL_FALLBACK,
            statusMessage = statusMessage
        )
    }
}
