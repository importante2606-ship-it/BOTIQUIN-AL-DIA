package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para las operaciones sobre productos del botiquín.
 *
 * PUNTO CRÍTICO DE ERROR:
 * Las funciones de modificación (insert, update, delete) SIEMPRE deben ser 'suspend'
 * para ejecutarse fuera del hilo principal (Main Thread). De lo contrario, Android lanzará
 * un fatal 'IllegalStateException: Cannot access database on the main thread'.
 * Las consultas que retornan Flow se ejecutan en segundo plano automáticamente gracias a Room.
 */
@Dao
interface ProductDao {

    /**
     * Obtiene todos los productos ordenados primero por fecha de vencimiento (los más próximos arriba).
     */
    @Query("SELECT * FROM products ORDER BY expiryDateMillis ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    /**
     * Inserta un nuevo producto. Con REPLACE, si ya existe un ID coincidente,
     * se actualiza sin lanzar excepción SQLiteConstraintException.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    /**
     * Actualiza un producto existente.
     */
    @Update
    suspend fun updateProduct(product: ProductEntity)

    /**
     * Elimina un producto del botiquín.
     */
    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    /**
     * Actualiza rápidamente únicamente la cantidad en stock.
     */
    @Query("UPDATE products SET quantity = :newQuantity WHERE id = :id")
    suspend fun updateQuantity(id: Int, newQuantity: Int)

    /**
     * Obtiene un producto por su clave primaria.
     */
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductEntity?

    /**
     * Cuenta total de productos para verificar si la base está vacía en el primer inicio.
     */
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}
