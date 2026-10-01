package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ProductEntity
import com.example.ui.components.AddEditProductDialog
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.RestockScreen

/**
 * Pantalla principal y contenedor de navegación para "Botiquín al Día".
 *
 * Estructurada en torno a las tres funciones del requerimiento:
 * 1. Registrar producto con cantidad y vencimiento.
 * 2. Alerta de lo que vence en 30 días.
 * 3. Lista de reposición.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotiquinMainApp(
    viewModel: BotiquinViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val inventoryProducts by viewModel.filteredInventory.collectAsStateWithLifecycle()
    val alertProducts by viewModel.alertProducts.collectAsStateWithLifecycle()
    val restockProducts by viewModel.restockProducts.collectAsStateWithLifecycle()

    // Estados para diálogos
    var showAddEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Healing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "BOTIQUÍN AL DÍA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                            )
                            Text(
                                text = "Salud del hogar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                // Pestaña 1: Inventario / Botiquín
                NavigationBarItem(
                    selected = currentTab == BotiquinTab.INVENTARIO,
                    onClick = { viewModel.selectTab(BotiquinTab.INVENTARIO) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = "Inventario del botiquín"
                        )
                    },
                    label = { Text("Botiquín") },
                    modifier = Modifier.testTag("nav_tab_inventory")
                )

                // Pestaña 2: Alertas 30 días
                val alertCount = alertProducts.size
                NavigationBarItem(
                    selected = currentTab == BotiquinTab.ALERTAS,
                    onClick = { viewModel.selectTab(BotiquinTab.ALERTAS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (alertCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFBA1A1A),
                                        contentColor = Color.White
                                    ) {
                                        Text("$alertCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alertas de vencimiento"
                            )
                        }
                    },
                    label = { Text("Alertas (30d)") },
                    modifier = Modifier.testTag("nav_tab_alerts")
                )

                // Pestaña 3: Lista de Reposición
                val restockCount = restockProducts.size
                NavigationBarItem(
                    selected = currentTab == BotiquinTab.REPOSICION,
                    onClick = { viewModel.selectTab(BotiquinTab.REPOSICION) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (restockCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text("$restockCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Lista de reposición"
                            )
                        }
                    },
                    label = { Text("Reposición") },
                    modifier = Modifier.testTag("nav_tab_restock")
                )
            }
        },
        floatingActionButton = {
            // El FAB permite registrar productos en todo momento (Función 1)
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_product")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Registrar nuevo producto en el botiquín"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentTab,
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    BotiquinTab.INVENTARIO -> {
                        InventoryScreen(
                            products = inventoryProducts,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            onSearchChange = { viewModel.updateSearchQuery(it) },
                            onCategorySelect = { viewModel.selectCategory(it) },
                            onIncrement = { viewModel.incrementQuantity(it) },
                            onDecrement = { viewModel.decrementQuantity(it) },
                            onEdit = { product ->
                                productToEdit = product
                                showAddEditDialog = true
                            },
                            onDelete = { product ->
                                productToDelete = product
                            },
                            onAddProductClick = {
                                productToEdit = null
                                showAddEditDialog = true
                            }
                        )
                    }

                    BotiquinTab.ALERTAS -> {
                        AlertsScreen(
                            alertProducts = alertProducts,
                            onIncrement = { viewModel.incrementQuantity(it) },
                            onDecrement = { viewModel.decrementQuantity(it) },
                            onEdit = { product ->
                                productToEdit = product
                                showAddEditDialog = true
                            },
                            onDelete = { product ->
                                productToDelete = product
                            }
                        )
                    }

                    BotiquinTab.REPOSICION -> {
                        RestockScreen(
                            restockProducts = restockProducts,
                            onIncrement = { viewModel.incrementQuantity(it) },
                            onDecrement = { viewModel.decrementQuantity(it) },
                            onEdit = { product ->
                                productToEdit = product
                                showAddEditDialog = true
                            },
                            onDelete = { product ->
                                productToDelete = product
                            },
                            onRestock = { product, qty ->
                                viewModel.restockProduct(product, qty)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal de Registro y Edición (Función 1)
    if (showAddEditDialog) {
        AddEditProductDialog(
            initialProduct = productToEdit,
            onDismiss = {
                showAddEditDialog = false
                productToEdit = null
            },
            onSave = { name, quantity, unit, expiryMillis, category, notes ->
                viewModel.saveProduct(
                    id = productToEdit?.id ?: 0,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    expiryDateMillis = expiryMillis,
                    category = category,
                    notes = notes
                )
                showAddEditDialog = false
                productToEdit = null
            }
        )
    }

    // Diálogo de Confirmación para Eliminar
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar del botiquín") },
            text = {
                Text("¿Estás seguro de que querés eliminar \"${product.name}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    },
                    modifier = Modifier.testTag("btn_confirm_delete")
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
