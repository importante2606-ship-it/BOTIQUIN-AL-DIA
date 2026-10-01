package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ProductEntity
import com.example.data.gemini.GeminiRestockResult
import com.example.data.gemini.PrioritizedRestockItem
import com.example.data.gemini.ResultSource
import com.example.data.gemini.StorageWarning
import com.example.ui.GeminiUiState
import com.example.ui.components.ProductCard

/**
 * Pantalla 3: Lista de Reposición (Función 3) + Sello de IA con Gemini 3.5 Flash.
 *
 * Requisitos implementados:
 * 1. Respuesta en JSON estructurado (responseSchema).
 * 2. Visualización de datos estructurados (tarjetas, badges de urgencia, incompatibilidades).
 * 3. Configuración de API Key desde variable de entorno / Secrets panel.
 * 4. Manejo de fallos con fallback automático y explicación visual.
 * 5. Botón para cargar datos de prueba (Mock) estáticos sin costo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestockScreen(
    restockProducts: List<ProductEntity>,
    geminiState: GeminiUiState,
    onAnalyzeGemini: () -> Unit,
    onLoadMockData: () -> Unit,
    onClearGemini: () -> Unit,
    onIncrement: (ProductEntity) -> Unit,
    onDecrement: (ProductEntity) -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit,
    onRestock: (ProductEntity, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var productToRestock by remember { mutableStateOf<ProductEntity?>(null) }
    var restockAmountText by remember { mutableStateOf("10") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Banner Superior Faltantes Farmacia
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Faltantes para la Farmacia",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Productos agotados, con stock bajo (≤2) o vencidos que necesitan recambio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // 2. SELLO DE IA (Gemini 3.5 Flash)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_gemini_ai_panel"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sello de IA: Priorización y Guardado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (geminiState is GeminiUiState.Success || geminiState is GeminiUiState.Error) {
                            IconButton(
                                onClick = onClearGemini,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar análisis",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "La IA clasifica la reposición por urgencia médica sanitaria e identifica qué productos no deben almacenarse juntos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botones de acción del Sello de IA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAnalyzeGemini,
                            enabled = geminiState !is GeminiUiState.Loading,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("btn_analyze_gemini")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (geminiState is GeminiUiState.Loading) "Analizando..." else "Analizar con Gemini",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        OutlinedButton(
                            onClick = onLoadMockData,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_load_mock_data")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prueba Mock", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 3. Estado de Carga (Spinner)
        if (geminiState is GeminiUiState.Loading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Consultando a Gemini 3.5 Flash con esquema JSON...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // 4. Resultados del Análisis de IA (Datos consumidos desde el JSON)
        when (geminiState) {
            is GeminiUiState.Success -> {
                item {
                    GeminiResultContent(result = geminiState.result)
                }
            }
            is GeminiUiState.Error -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3CD)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Aviso de conexión",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404)
                                    )
                                    Text(
                                        text = geminiState.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF856404)
                                    )
                                }
                            }
                        }

                        // Mostrar fallback local
                        GeminiResultContent(result = geminiState.fallbackResult)
                    }
                }
            }
            else -> {}
        }

        // 5. Lista de productos faltantes física en inventario
        item {
            Text(
                text = "Lista de productos para reponer (${restockProducts.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (restockProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD1FAE5).copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "¡Botiquín completo y abastecido!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                            Text(
                                text = "No hay productos agotados, con stock bajo ni vencidos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }
        } else {
            items(restockProducts, key = { it.id }) { product ->
                Column {
                    ProductCard(
                        product = product,
                        onIncrement = { onIncrement(product) },
                        onDecrement = { onDecrement(product) },
                        onEdit = { onEdit(product) },
                        onDelete = { onDelete(product) }
                    )

                    // Botón directo para reponer unidades
                    OutlinedButton(
                        onClick = {
                            productToRestock = product
                            restockAmountText = "10"
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("btn_quick_restock_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (product.quantity == 0) "Reponer stock agotado" else "Agregar más unidades",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal para ingresar cantidad al reponer
    productToRestock?.let { product ->
        AlertDialog(
            onDismissRequest = { productToRestock = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Reabastecer ${product.name}")
            },
            text = {
                Column {
                    Text(
                        text = "Stock actual: ${product.quantity} ${product.unit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restockAmountText,
                        onValueChange = { text ->
                            if (text.all { it.isDigit() }) restockAmountText = text
                        },
                        label = { Text("Cantidad a sumar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = restockAmountText.toIntOrNull() ?: 1
                        onRestock(product, amount)
                        productToRestock = null
                    }
                ) {
                    Text("Reabastecer")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToRestock = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Renderiza los datos estructurados del JSON generado por Gemini (Requisito 2).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeminiResultContent(result: GeminiRestockResult) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Indicador de fuente
        val badgeColor = when (result.source) {
            ResultSource.GEMINI_API -> Color(0xFFD1E7DD)
            ResultSource.MOCK_TEST -> Color(0xFFE2D9F3)
            ResultSource.LOCAL_FALLBACK -> Color(0xFFFFF3CD)
        }
        val textColor = when (result.source) {
            ResultSource.GEMINI_API -> Color(0xFF0F5132)
            ResultSource.MOCK_TEST -> Color(0xFF432874)
            ResultSource.LOCAL_FALLBACK -> Color(0xFF856404)
        }

        Surface(
            color = badgeColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = result.statusMessage,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        // 1. Datos de Prioridad de Reposición (Consumo del array prioritizedItems)
        Text(
            text = "Prioridad Sanitaria de Reposición (IA)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        result.prioritizedItems.forEach { item ->
            PrioritizedItemCard(item)
        }

        // 2. Datos de Advertencias de Almacenamiento (Consumo del array storageWarnings)
        Text(
            text = "Incompatibilidades de Guardado en Botiquín (IA)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        result.storageWarnings.forEach { warning ->
            StorageWarningCard(warning)
        }
    }
}

@Composable
private fun PrioritizedItemCard(item: PrioritizedRestockItem) {
    val (priorityBg, priorityFg) = when (item.priority.uppercase()) {
        "URGENTE" -> Color(0xFFFFDAD6) to Color(0xFFBA1A1A)
        "ALTA" -> Color(0xFFFFE0B2) to Color(0xFFE65100)
        else -> Color(0xFFE1F5FE) to Color(0xFF0277BD)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = priorityBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.priority.equals("URGENTE", true)) Icons.Default.PriorityHigh else Icons.Default.Info,
                            contentDescription = null,
                            tint = priorityFg,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.priority.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityFg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Motivo sanitario: ${item.medicalReason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StorageWarningCard(warning: StorageWarning) {
    val (badgeBg, badgeFg) = when (warning.dangerLevel.uppercase()) {
        "ALTO" -> Color(0xFFFFDAD6) to Color(0xFFBA1A1A)
        "MEDIO" -> Color(0xFFFFE0B2) to Color(0xFFE65100)
        else -> Color(0xFFFFF9C4) to Color(0xFFF57F17)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = warning.productA,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " ❌ ",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = warning.productB,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "RIESGO ${warning.dangerLevel.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeFg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Recomendación: ${warning.recommendation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
