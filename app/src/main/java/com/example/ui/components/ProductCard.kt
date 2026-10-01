package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ProductEntity
import kotlin.math.abs

/**
 * Tarjeta individual para mostrar un medicamento o elemento del botiquín.
 * Muestra claramente la cantidad, unidad, fecha formateada y el semáforo de vencimiento.
 */
@Composable
fun ProductCard(
    product: ProductEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickRestock: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val daysUntilExpiry = product.getDaysUntilExpiry()
    val isExpired = product.isExpired()
    val isExpiringSoon = product.isExpiringIn30Days()

    // Configuración del semáforo visual según fecha de vencimiento
    val (statusColor, statusBg, statusText, statusIcon) = when {
        isExpired -> {
            val daysAgo = abs(daysUntilExpiry)
            val msg = if (daysAgo == 0L) "¡VENCE HOY!" else "VENCIDO hace $daysAgo días"
            Quad(
                Color(0xFFBA1A1A), // Rojo crítico
                Color(0xFFFFDAD6),
                msg,
                Icons.Default.ErrorOutline
            )
        }
        isExpiringSoon -> {
            val msg = if (daysUntilExpiry == 0L) "¡Vence hoy!" else "Vence en $daysUntilExpiry días"
            Quad(
                Color(0xFFB45309), // Ámbar/Naranja advertencia
                Color(0xFFFEF3C7),
                msg,
                Icons.Default.WarningAmber
            )
        }
        else -> {
            Quad(
                Color(0xFF047857), // Verde seguro
                Color(0xFFD1FAE5),
                "Vence: ${product.getFormattedExpiryDate()}",
                Icons.Default.Info
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila Superior: Categoría y Semáforo de Vencimiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip de Categoría
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = product.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Chip de Semáforo de Vencimiento (Alerta 30 días o Vencido)
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nombre del producto
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Notas o ubicación en el hogar si existen
            if (product.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila de Gestión de Stock (Cantidad y controles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Control de Cantidad y Unidad
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecrement,
                        enabled = product.quantity > 0,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (product.quantity > 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                                shape = CircleShape
                            )
                            .testTag("btn_decrement_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Disminuir cantidad",
                            tint = if (product.quantity > 0) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .background(
                                color = if (product.quantity == 0) Color(0xFFFFDAD6) else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (product.quantity == 0) "Agotado" else "${product.quantity} ${product.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (product.quantity == 0) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                            .testTag("btn_increment_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar cantidad",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Acciones: Editar, Eliminar o Botón de Reabastecer rápido
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onQuickRestock != null) {
                        OutlinedButton(
                            onClick = onQuickRestock,
                            modifier = Modifier.padding(end = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "+ Reponer", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("btn_edit_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar producto",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("btn_delete_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar producto",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
