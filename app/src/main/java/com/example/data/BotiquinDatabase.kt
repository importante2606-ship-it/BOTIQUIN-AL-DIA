package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Base de datos Room para la persistencia local de "Botiquín al Día".
 *
 * PUNTO CRÍTICO DE ERROR:
 * La base de datos debe ser un Singleton estricto con @Volatile.
 * Si se instancian múltiples copias de RoomDatabase para el mismo archivo SQLite,
 * se corre el riesgo de bloqueos de archivo (database locked) o corrupción de datos.
 */
@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class BotiquinDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: BotiquinDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BotiquinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BotiquinDatabase::class.java,
                    "botiquin_al_dia_db"
                )
                    .addCallback(BotiquinDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback para precargar datos iniciales representativos del hogar
     * si la base de datos acaba de crearse, permitiendo probar las 3 funciones de inmediato.
     */
    private class BotiquinDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.productDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: ProductDao) {
            val now = LocalDate.now(ZoneId.systemDefault())

            // Función 1: Producto vigente con stock saludable
            val paracetamolExpiry = now.plusMonths(8).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dao.insertProduct(
                ProductEntity(
                    name = "Paracetamol 500 mg",
                    quantity = 20,
                    unit = "comprimidos",
                    expiryDateMillis = paracetamolExpiry,
                    category = "Analgésico",
                    notes = "Caja en estante principal"
                )
            )

            // Función 2: Producto por vencer en 15 días (Alerta 30 días)
            val alcoholExpiry = now.plusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dao.insertProduct(
                ProductEntity(
                    name = "Alcohol Etílico 70%",
                    quantity = 1,
                    unit = "botella 250ml",
                    expiryDateMillis = alcoholExpiry,
                    category = "Antiséptico",
                    notes = "Próximo a vencer. Mantener alejado de gasas sin sellar."
                )
            )

            // Función 2 y 3: Producto ya vencido hace 7 días (Alerta crítica + Reposición)
            val ibuprofenoExpiry = now.minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dao.insertProduct(
                ProductEntity(
                    name = "Ibuprofeno 400 mg",
                    quantity = 4,
                    unit = "comprimidos",
                    expiryDateMillis = ibuprofenoExpiry,
                    category = "Analgésico",
                    notes = "¡Vencido! Descartar y reponer urgente."
                )
            )

            // Función 3: Producto agotado (0 unidades) que va directo a Reposición
            val gasasExpiry = now.plusMonths(12).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dao.insertProduct(
                ProductEntity(
                    name = "Gasas Estériles (paquete)",
                    quantity = 0,
                    unit = "sobres",
                    expiryDateMillis = gasasExpiry,
                    category = "Curación",
                    notes = "Agotado en última curación"
                )
            )

            // Curitas con stock bajo (1 unidad)
            val curitasExpiry = now.plusMonths(14).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dao.insertProduct(
                ProductEntity(
                    name = "Curitas Adhesivas",
                    quantity = 2,
                    unit = "unidades",
                    expiryDateMillis = curitasExpiry,
                    category = "Curación",
                    notes = "Quedan pocas para reponer"
                )
            )
        }
    }
}
