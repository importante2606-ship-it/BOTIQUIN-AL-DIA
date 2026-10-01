package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ProductEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val CATEGORIAS_DISPONIBLES = listOf(
    "Analgésico",
    "Antiséptico",
    "Curación",
    "Digestivo",
    "Alergia",
    "General"
)

private val UNIDADES_DISPONIBLES = listOf(
    "comprimidos",
    "unidades",
    "ml",
    "sobres",
    "pomada",
    "gotas"
)

/**
 * Diálogo modal para registrar (o editar) un producto en el botiquín del hogar.
 * Cumple con la Función 1: Registrar producto con cantidad y fecha de vencimiento.
 *
 * PUNTOS CRÍTICOS DE ERROR:
 * 1. El DatePicker de Material 3 devuelve 'selectedDateMillis' en tiempo universal coordinado (UTC).
 *    Si intentas leer directamente con SimpleDateFormat en hora local sin especificar UTC,
 *    en países de América Latina (UTC-3 a UTC-5) la fecha se atrasará exactamente un día.
 * 2. La cantidad debe parsearse con seguridad (evitando NumberFormatException si el usuario borra el campo).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, quantity: Int, unit: String, expiryMillis: Long, category: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var quantityText by remember { mutableStateOf(initialProduct?.quantity?.toString() ?: "1") }
    var selectedUnit by remember { mutableStateOf(initialProduct?.unit ?: "comprimidos") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: "Analgésico") }
    var notes by remember { mutableStateOf(initialProduct?.notes ?: "") }

    // Fecha por defecto: fecha existente o 6 meses a futuro
    val defaultExpiry = remember {
        initialProduct?.expiryDateMillis ?: run {
            LocalDate.now(ZoneId.systemDefault())
                .plusMonths(6)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }
    }
    var expiryDateMillis by remember { mutableLongStateOf(defaultExpiry) }

    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    // Formateador de fecha seguro
    val dateString = remember(expiryDateMillis) {
        val date = Instant.ofEpochMilli(expiryDateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        date.format(formatter)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Registrar Producto" else "Editar Producto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // 1. Nombre del producto
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nombre del medicamento o producto *") },
                    placeholder = { Text("Ej: Ibuprofeno 400mg, Gasas, Alcohol") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("El nombre es obligatorio")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Cantidad y Unidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { text ->
                            // Filtrar solo dígitos positivos
                            if (text.all { it.isDigit() }) {
                                quantityText = text
                            }
                        },
                        label = { Text("Cantidad *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_product_quantity")
                    )

                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = { selectedUnit = it },
                        label = { Text("Presentación") },
                        placeholder = { Text("comprimidos, ml, etc") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("input_product_unit")
                    )
                }

                // Sugerencias rápidas de unidad
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    UNIDADES_DISPONIBLES.forEach { unit ->
                        FilterChip(
                            selected = selectedUnit.equals(unit, ignoreCase = true),
                            onClick = { selectedUnit = unit },
                            label = { Text(unit, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Fecha de Vencimiento (Campo clickable con calendario)
                Text(
                    text = "Fecha de Vencimiento *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = dateString,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vence el (DD/MM/AAAA)") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Seleccionar fecha de vencimiento",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .testTag("input_product_expiry")
                )

                Text(
                    text = "Seleccioná la fecha impresa en la caja o blíster",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Categoría médica
                Text(
                    text = "Categoría en el botiquín",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    CATEGORIAS_DISPONIBLES.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory.equals(cat, ignoreCase = true),
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Notas opcionales
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas o ubicación (opcional)") },
                    placeholder = { Text("Ej: Guardar lejos de calor, cajón 1") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val qty = quantityText.toIntOrNull() ?: 0
                    onSave(
                        name.trim(),
                        qty,
                        selectedUnit.trim().ifBlank { "unidades" },
                        expiryDateMillis,
                        selectedCategory,
                        notes.trim()
                    )
                },
                modifier = Modifier.testTag("btn_save_product")
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Modal DatePickerDialog de Material 3
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedMillis ->
                            // Guardamos la medianoche exacta
                            expiryDateMillis = pickedMillis
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("btn_confirm_date")
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
