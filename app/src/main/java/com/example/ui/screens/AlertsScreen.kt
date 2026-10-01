package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Pantalla 2: Alertas de Vencimiento a 30 días (Función 2).
 *
 * Muestra prioritariamente:
 * 1. Lo que ya venció (crítico para descartar).
 * 2. Lo que vence dentro de los próximos 30 días (para planificar uso o reposición).
 */
@Composable
fun AlertsScreen(
    alertProducts: List<ProductEntity>,
    onIncrement: (ProductEntity) -> Unit,
    onDecrement: (ProductEntity) -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val expiredList = alertProducts.filter { it.isExpired() }
    val expiringSoonList = alertProducts.filter { it.isExpiringIn30Days() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Banner Superior Informativo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7) // Ámbar suave
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
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Vigilancia de 30 Días",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Los medicamentos vencidos pierden efectividad y pueden generar compuestos perjudiciales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }

        // Resumen de Métricas de Alerta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Métrica: Vencidos
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (expiredList.isNotEmpty()) Color(0xFFFFDAD6) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${expiredList.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (expiredList.isNotEmpty()) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ya vencidos",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (expiredList.isNotEmpty()) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Métrica: Vence en 30 días
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (expiringSoonList.isNotEmpty()) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${expiringSoonList.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (expiringSoonList.isNotEmpty()) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Vence en ≤30 días",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (expiringSoonList.isNotEmpty()) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Lista de Alertas o Estado Vacío (Todo en regla)
        if (alertProducts.isEmpty()) {
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
                        text = "¡Botiquín en regla!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No tenés ningún producto vencido ni por vencer en los próximos 30 días.",
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
                modifier = Modifier.testTag("alerts_list")
            ) {
                // Sección de Vencidos
                if (expiredList.isNotEmpty()) {
                    item {
                        Text(
                            text = "🚨 Descartar (Vencidos)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBA1A1A),
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

                    items(expiredList, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onIncrement = { onIncrement(product) },
                            onDecrement = { onDecrement(product) },
                            onEdit = { onEdit(product) },
                            onDelete = { onDelete(product) }
                        )
                    }
                }

                // Sección de Próximos a Vencer
                if (expiringSoonList.isNotEmpty()) {
                    item {
                        Text(
                            text = "⚠️ Vence en menos de 30 días",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }

                    items(expiringSoonList, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onIncrement = { onIncrement(product) },
                            onDecrement = { onDecrement(product) },
                            onEdit = { onEdit(product) },
                            onDelete = { onDelete(product) }
                        )
                    }
                }
            }
        }
    }
}
