package com.stocksync.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocksync.app.R
import com.stocksync.app.data.db.ProductEntity

@Composable
fun ProductDetailScreen(
    product: ProductEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStockIn: (Int) -> Unit,
    onLogSale: (Int, String) -> Unit
) {
    var showStockInDialog by remember { mutableStateOf(false) }
    var showSaleDialog by remember { mutableStateOf(false) }
    var stockInQty by remember { mutableStateOf("10") }
    var saleQty by remember { mutableStateOf("1") }
    var salePaymentMethod by remember { mutableStateOf("CASH") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color(0xFFC0392B))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product name and category
            Text(product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            product.category?.let {
                Text(it, fontSize = 12.sp, color = Color(0xFF888888))
            }
            product.barcode?.let {
                Text("${stringResource(R.string.barcode_label)}: $it", fontSize = 12.sp, color = Color(0xFF888888))
            }

            // Price card
            InfoCard(stringResource(R.string.price_label), "R ${product.price}")
            InfoCard(stringResource(R.string.current_stock), "${product.quantity}")
            InfoCard(stringResource(R.string.low_stock_threshold), "${product.lowStockThreshold}")

            // Action buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showStockInDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
                ) {
                    Text(stringResource(R.string.stock_in))
                }
                Button(
                    onClick = { showSaleDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22))
                ) {
                    Text(stringResource(R.string.log_sale))
                }
            }
        }
    }

    if (showStockInDialog) {
        AlertDialog(
            onDismissRequest = { showStockInDialog = false },
            title = { Text(stringResource(R.string.stock_in)) },
            text = {
                OutlinedTextField(
                    value = stockInQty,
                    onValueChange = { stockInQty = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.quantity)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    stockInQty.toIntOrNull()?.let { onStockIn(it) }
                    showStockInDialog = false
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showStockInDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showSaleDialog) {
        AlertDialog(
            onDismissRequest = { showSaleDialog = false },
            title = { Text(stringResource(R.string.log_sale)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = saleQty,
                        onValueChange = { saleQty = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.quantity)) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.payment_method), fontWeight = FontWeight.Bold)
                    Row {
                        listOf("CASH", "CARD", "OTHER").forEach { method ->
                            FilterChip(
                                selected = salePaymentMethod == method,
                                onClick = { salePaymentMethod = method },
                                label = { Text(method) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    saleQty.toIntOrNull()?.let { onLogSale(it, salePaymentMethod) }
                    showSaleDialog = false
                }) { Text(stringResource(R.string.confirm_sale)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaleDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
