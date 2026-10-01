package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.ProductCard

/**
 * Pantalla 3: Lista de Reposición (Función 3).
 *
 * Muestra los medicamentos que se quedaron sin unidades, los que están en nivel crítico (<=2)
 * y los que están vencidos y deben ser reemplazados en la farmacia.
 * Incluye además las recomendaciones para el correcto guardado y compatibilidad en el hogar.
 */
@Composable
fun RestockScreen(
    restockProducts: List<ProductEntity>,
    onIncrement: (ProductEntity) -> Unit,
    onDecrement: (ProductEntity) -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit,
    onRestock: (ProductEntity, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var productToRestock by remember { mutableStateOf<ProductEntity?>(null) }
    var restockAmountText by remember { mutableStateOf("10") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Banner Superior con Indicador de Farmacia
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
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

        // Recomendaciones de Seguridad y Almacenamiento en el Hogar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Consejos de guardado en el botiquín",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Evitá el baño: la humedad y calor de la ducha degradan las fórmulas.\n• No guardes antisépticos volátiles (alcohol, agua oxigenada) junto a gasas abiertas.\n• Conservá siempre los medicamentos en su envase original con fecha visible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Lista de Reposición o Estado Vacío
        if (restockProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp),
                                tint = Color(0xFF047857)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¡Botiquín completo!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No hay productos agotados ni vencidos pendientes de reponer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("restock_list")
            ) {
                items(restockProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onIncrement = { onIncrement(product) },
                        onDecrement = { onDecrement(product) },
                        onEdit = { onEdit(product) },
                        onDelete = { onDelete(product) },
                        onQuickRestock = {
                            productToRestock = product
                            restockAmountText = "10"
                        }
                    )
                }
            }
        }
    }

    // Diálogo rápido para ingresar reposición de stock
    productToRestock?.let { product ->
        AlertDialog(
            onDismissRequest = { productToRestock = null },
            title = {
                Text("Reponer Stock: ${product.name}")
            },
            text = {
                Column {
                    Text(
                        text = "Stock actual: ${product.quantity} ${product.unit}. ¿Cuántas unidades compraste?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restockAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) restockAmountText = it },
                        label = { Text("Cantidad a sumar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_restock_amount")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toAdd = restockAmountText.toIntOrNull() ?: 0
                        if (toAdd > 0) {
                            onRestock(product, toAdd)
                        }
                        productToRestock = null
                    },
                    modifier = Modifier.testTag("btn_confirm_restock")
                ) {
                    Text("Sumar al Botiquín")
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
