package com.stocksync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocksync.app.R
import com.stocksync.app.data.db.ProductEntity

@Composable
fun QuickSaleScreen(
    viewModel: ProductViewModel,
    onSaleLogged: () -> Unit,
    onAddProduct: () -> Unit,
    onScanBarcode: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    var showSaleDialog by remember { mutableStateOf<ProductEntity?>(null) }
    var saleQuantity by remember { mutableStateOf(1) }
    var paymentMethod by remember { mutableStateOf("CASH") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with scan button
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onScanBarcode) {
                Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.scan))
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                ProductTile(
                    product = product,
                    onClick = {
                        if (product.quantity > 0) {
                            saleQuantity = 1
                            paymentMethod = "CASH"
                            showSaleDialog = product
                        }
                    }
                )
            }
            item {
                // Add product tile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable { onAddProduct() },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFCCCCCC)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF27AE60))
                        Text(stringResource(R.string.add_product), fontSize = 10.sp, color = Color(0xFF888888))
                    }
                }
            }
        }
    }

    // Sale dialog
    showSaleDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showSaleDialog = null },
            title = { Text(product.name) },
            text = {
                Column {
                    Text("Price: R ${product.price}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.quantity), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { saleQuantity = maxOf(1, saleQuantity - 1) }) {
                            Text("-")
                        }
                        Text("$saleQuantity", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 18.sp)
                        OutlinedButton(onClick = { saleQuantity = minOf(product.quantity, saleQuantity + 1) }) {
                            Text("+")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.payment_method), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row {
                        listOf("CASH", "CARD", "OTHER").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logSale(product.id, saleQuantity, product.price, paymentMethod)
                    showSaleDialog = null
                    saleQuantity = 1
                    onSaleLogged()
                }) {
                    Text(stringResource(R.string.confirm_sale))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaleDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ProductTile(product: ProductEntity, onClick: () -> Unit) {
    val isOutOfStock = product.quantity == 0
    val isLowStock = product.quantity < product.lowStockThreshold && !isOutOfStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOutOfStock -> Color(0xFFF5F5F5)
                isLowStock -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(product.name.take(15), fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            Text("R ${product.price}", fontSize = 10.sp, color = Color(0xFF666666))
            Text(
                text = if (isOutOfStock) stringResource(R.string.out_of_stock) else "${product.quantity} ${stringResource(R.string.in_stock)}",
                fontSize = 9.sp,
                color = when {
                    isOutOfStock -> Color(0xFFBBBBBB)
                    isLowStock -> Color(0xFFE67E22)
                    else -> Color(0xFF27AE60)
                }
            )
        }
    }
}
