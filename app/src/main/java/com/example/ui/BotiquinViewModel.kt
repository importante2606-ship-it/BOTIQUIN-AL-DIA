package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BotiquinDatabase
import com.example.data.ProductEntity
import com.example.data.ProductRepository
import com.example.data.gemini.GeminiRestockResult
import com.example.data.gemini.GeminiRestockService
import com.example.data.gemini.ResultSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Pestañas principales de la aplicación correspondientes a las tres funciones del brief.
 */
enum class BotiquinTab(val title: String) {
    INVENTARIO("Botiquín"),       // Función 1: Registrar y gestionar productos
    ALERTAS("Alertas (30d)"),    // Función 2: Lo que vence en 30 días o ya venció
    REPOSICION("Reposición")     // Función 3: Lista de reposición y faltantes
}

/**
 * Estado del análisis de IA con Gemini 3.5 Flash para la lista de reposición y guardado.
 */
sealed interface GeminiUiState {
    object Idle : GeminiUiState
    object Loading : GeminiUiState
    data class Success(val result: GeminiRestockResult) : GeminiUiState
    data class Error(val message: String, val fallbackResult: GeminiRestockResult) : GeminiUiState
}

/**
 * ViewModel que expone el estado de la UI y gestiona las operaciones de negocio.
 */
class BotiquinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository

    init {
        val db = BotiquinDatabase.getDatabase(application, viewModelScope)
        repository = ProductRepository(db.productDao())
    }

    private val _selectedTab = MutableStateFlow(BotiquinTab.INVENTARIO)
    val selectedTab: StateFlow<BotiquinTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Estado del Sello de IA con Gemini
    private val _geminiState = MutableStateFlow<GeminiUiState>(GeminiUiState.Idle)
    val geminiState: StateFlow<GeminiUiState> = _geminiState.asStateFlow()

    // Flujo base desde Room Database
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Lista filtrada para la pestaña de Inventario (Función 1)
    val filteredInventory: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true) ||
                    product.notes.contains(query, ignoreCase = true)
            val matchesCategory = category == null || product.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Alertas de vencimiento (Función 2: vence en 30 días o ya está vencido)
    val alertProducts: StateFlow<List<ProductEntity>> = allProducts.combine(_searchQuery) { products, query ->
        products.filter { product ->
            (product.isExpired() || product.isExpiringIn30Days()) &&
                    (query.isBlank() || product.name.contains(query, ignoreCase = true))
        }.sortedBy { it.getDaysUntilExpiry() } // Los más urgentes / vencidos primero
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Lista de reposición (Función 3: agotados, bajo stock o vencidos)
    val restockProducts: StateFlow<List<ProductEntity>> = allProducts.combine(_searchQuery) { products, query ->
        products.filter { product ->
            product.needsRestock() &&
                    (query.isBlank() || product.name.contains(query, ignoreCase = true))
        }.sortedWith(
            compareBy<ProductEntity> {
                // Prioridad médica: 1. Agotados, 2. Vencidos, 3. Stock bajo
                when {
                    it.quantity == 0 -> 0
                    it.isExpired() -> 1
                    else -> 2
                }
            }.thenBy { it.name }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTab(tab: BotiquinTab) {
        _selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    /**
     * Sello de IA: Ejecuta la llamada a Gemini 3.5 Flash para ordenar la reposición
     * por prioridad médica y generar advertencias de almacenamiento con JSON Schema.
     */
    fun analyzeWithGemini() {
        viewModelScope.launch {
            _geminiState.value = GeminiUiState.Loading
            val restockList = restockProducts.value
            val allList = allProducts.value
            val result = GeminiRestockService.analyzeRestock(restockList, allList)
            if (result.source == ResultSource.GEMINI_API) {
                _geminiState.value = GeminiUiState.Success(result)
            } else {
                _geminiState.value = GeminiUiState.Error(result.statusMessage, result)
            }
        }
    }

    /**
     * Requisito 5: Carga datos de prueba (Mock) estáticos sin costo ni necesidad de API Key.
     */
    fun loadMockTestData() {
        val mockResult = GeminiRestockService.getMockTestResult()
        _geminiState.value = GeminiUiState.Success(mockResult)
    }

    /**
     * Limpia o resetea el análisis de IA.
     */
    fun clearGeminiAnalysis() {
        _geminiState.value = GeminiUiState.Idle
    }

    /**
     * Guarda o edita un producto en el botiquín.
     */
    fun saveProduct(
        id: Int = 0,
        name: String,
        quantity: Int,
        unit: String,
        expiryDateMillis: Long,
        category: String,
        notes: String
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                name = name.trim(),
                quantity = quantity.coerceAtLeast(0),
                unit = unit.trim().ifBlank { "unidades" },
                expiryDateMillis = expiryDateMillis,
                category = category.trim().ifBlank { "General" },
                notes = notes.trim()
            )
            repository.insertOrUpdateProduct(product)
        }
    }

    /**
     * Incrementa rápidamente el stock (ej: al comprar o recibir más unidades).
     */
    fun incrementQuantity(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateQuantity(product.id, product.quantity + 1)
        }
    }

    /**
     * Decrementa el stock (ej: al administrar un comprimido o consumir apósito).
     */
    fun decrementQuantity(product: ProductEntity) {
        viewModelScope.launch {
            if (product.quantity > 0) {
                repository.updateQuantity(product.id, product.quantity - 1)
            }
        }
    }

    /**
     * Reabastece agregando una cantidad específica (Función 3).
     */
    fun restockProduct(product: ProductEntity, addedQuantity: Int) {
        viewModelScope.launch {
            val newQty = (product.quantity + addedQuantity).coerceAtLeast(0)
            repository.updateQuantity(product.id, newQty)
        }
    }

    /**
     * Elimina el producto de la base de datos local.
     */
    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}
