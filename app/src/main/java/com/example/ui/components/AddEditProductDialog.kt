package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * Convierte un epoch millis a LocalDate en la zona horaria del sistema.
 */
private fun millisToLocalDate(millis: Long): LocalDate {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

/**
 * Convierte un LocalDate a epoch millis a la medianoche en la zona horaria del sistema.
 */
private fun localDateToMillis(date: LocalDate): Long {
    return date.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

/**
 * Diálogo modal para registrar o editar un producto en el botiquín del hogar.
 * Cumple con la Función 1: Registrar producto con cantidad y fecha de vencimiento.
 *
 * PUNTOS CRÍTICOS RESUELTOS:
 * 1. OutlinedTextField con readOnly consume los eventos táctiles en Compose; se incluye un
 *    Box superpuesto transparente (overlay) e IconButton dedicado para garantizar que cualquier
 *    toque abra el selector de fecha inmediatamente.
 * 2. Se agregan accesos directos (+1 mes, +6 meses, +1 año, +2 años) para agilizar la carga.
 * 3. Se sincronizan las conversiones de UTC (retornadas por Material 3 DatePicker) con la zona horaria local.
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

    // Fecha inicial: la del producto existente o hoy + 6 meses
    val defaultExpiry = remember {
        initialProduct?.expiryDateMillis ?: localDateToMillis(
            LocalDate.now(ZoneId.systemDefault()).plusMonths(6)
        )
    }
    var expiryDateMillis by remember { mutableLongStateOf(defaultExpiry) }

    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    // Fecha formateada en dd/MM/yyyy
    val currentDate = millisToLocalDate(expiryDateMillis)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dateString = currentDate.format(formatter)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialProduct == null) Icons.Default.EditCalendar else Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialProduct == null) "Registrar Producto" else "Editar Producto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
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
                    label = { Text("Nombre del medicamento *") },
                    placeholder = { Text("Ej: Ibuprofeno 400mg, Alcohol, Gasas") },
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
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("input_product_unit")
                    )
                }

                // Sugerencias de unidad
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

                Spacer(modifier = Modifier.height(14.dp))

                // 3. SECCIÓN FECHA DE VENCIMIENTO (Completamente interactiva y accesible)
                Text(
                    text = "Fecha de Vencimiento *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Contenedor Box con overlay transparente para que CUALQUIER toque abra el calendario
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_expiry_box")
                ) {
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha seleccionada") },
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.testTag("btn_open_calendar_icon")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Abrir calendario",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Capa transparente superior para capturar el click en cualquier parte del campo
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón explícito para cambiar fecha en calendario
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_change_expiry_date"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir Calendario para Cambiar Fecha")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Atajos rápidos de vencimiento común
                Text(
                    text = "O elegí un plazo rápido desde hoy:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val today = LocalDate.now(ZoneId.systemDefault())

                    QuickDateChip("+1 mes") {
                        expiryDateMillis = localDateToMillis(today.plusMonths(1))
                    }
                    QuickDateChip("+6 meses") {
                        expiryDateMillis = localDateToMillis(today.plusMonths(6))
                    }
                    QuickDateChip("+1 año") {
                        expiryDateMillis = localDateToMillis(today.plusYears(1))
                    }
                    QuickDateChip("+2 años") {
                        expiryDateMillis = localDateToMillis(today.plusYears(2))
                    }
                    QuickDateChip("Ya vencido (-5d)") {
                        expiryDateMillis = localDateToMillis(today.minusDays(5))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Categoría médica
                Text(
                    text = "Categoría en el botiquín",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
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
                    placeholder = { Text("Ej: En cajón superior, no mezclar") },
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
                Text("Guardar Producto")
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
        // Obtenemos la fecha actualmente configurada en UTC para el DatePickerState
        val initialUtcMillis = remember(expiryDateMillis) {
            val local = millisToLocalDate(expiryDateMillis)
            local.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedUtcMillis ->
                            // Convertir fecha UTC seleccionada a LocalDate
                            val pickedLocalDate = Instant.ofEpochMilli(pickedUtcMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            // Guardar en la zona local a medianoche
                            expiryDateMillis = localDateToMillis(pickedLocalDate)
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("btn_confirm_date")
                ) {
                    Text("Confirmar Fecha")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Volver")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }
}

@Composable
private fun QuickDateChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
