package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que desacopla la fuente de datos (DAO) de la lógica de presentación (ViewModel).
 *
 * PUNTO CRÍTICO DE ERROR:
 * Nunca expongas directamente el DAO a la capa UI (Composables o Activities).
 * El repositorio permite testear con dobles de prueba y centralizar reglas de negocio
 * como validaciones de stock mínimo o fechas inválidas antes de escribir en disco.
 */
class ProductRepository(private val dao: ProductDao) {

    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()

    suspend fun insertOrUpdateProduct(product: ProductEntity): Long {
        // Validación de seguridad para que la cantidad nunca sea negativa
        val cleanProduct = product.copy(
            name = product.name.trim(),
            quantity = product.quantity.coerceAtLeast(0)
        )
        return dao.insertProduct(cleanProduct)
    }

    suspend fun updateQuantity(id: Int, newQuantity: Int) {
        val safeQuantity = newQuantity.coerceAtLeast(0)
        dao.updateQuantity(id, safeQuantity)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        dao.deleteProduct(product)
    }

    suspend fun getProductById(id: Int): ProductEntity? {
        return dao.getProductById(id)
    }
}
