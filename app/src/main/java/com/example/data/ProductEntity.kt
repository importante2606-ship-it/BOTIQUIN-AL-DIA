package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Entidad principal que representa un medicamento o producto de primeros auxilios.
 *
 * NOTA PARA DESARROLLADORES (PUNTO CRÍTICO DE ERROR):
 * 1. La fecha de vencimiento se almacena como milisegundos Epoch UTC ([expiryDateMillis]).
 *    Nunca guardes cadenas de texto tipo "12/10/2026" directamente en SQLite porque
 *    imposibilita ordenar y filtrar por fecha mediante sentencias SQL estándar.
 * 2. Las cantidades deben validarse para que nunca sean números negativos.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: Int,
    val unit: String = "unidades", // unidades, comprimidos, ml, sobres, gotas
    val expiryDateMillis: Long,     // Timestamp en milisegundos de la medianoche del día de vencimiento
    val category: String = "General", // Analgésico, Antiséptico, Curación, etc.
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
) {

    /**
     * Calcula los días restantes hasta la fecha de vencimiento.
     *
     * PUNTO CRÍTICO:
     * No calcular restando milisegundos directamente y dividiendo por (1000 * 60 * 60 * 24)
     * porque los cambios de horario de verano (Daylight Saving Time) y las zonas horarias
     * provocan errores de un día de diferencia. Usamos siempre [LocalDate] y [ChronoUnit.DAYS].
     */
    fun getDaysUntilExpiry(): Long {
        val today = LocalDate.now(ZoneId.systemDefault())
        val expiryDate = Instant.ofEpochMilli(expiryDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return ChronoUnit.DAYS.between(today, expiryDate)
    }

    /**
     * Devuelve verdadero si el producto ya está vencido (fecha anterior a hoy).
     */
    fun isExpired(): Boolean {
        return getDaysUntilExpiry() < 0
    }

    /**
     * Devuelve verdadero si vence dentro de los próximos 30 días (inclusive hoy).
     * Esta es la condición central de la Función 2 requerida.
     */
    fun isExpiringIn30Days(): Boolean {
        val days = getDaysUntilExpiry()
        return days in 0..30
    }

    /**
     * Devuelve verdadero si el producto requiere reposición:
     * - Si está agotado (cantidad == 0)
     * - Si tiene stock crítico (cantidad <= 2)
     * - O si ya está vencido y por lo tanto no debe consumirse
     */
    fun needsRestock(): Boolean {
        return quantity <= 2 || isExpired()
    }

    /**
     * Formatea la fecha de vencimiento a formato latinoamericano legible (dd/MM/yyyy).
     */
    fun getFormattedExpiryDate(): String {
        val localDate = Instant.ofEpochMilli(expiryDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return localDate.format(formatter)
    }
}
