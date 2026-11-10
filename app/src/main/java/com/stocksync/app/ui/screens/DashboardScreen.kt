package com.stocksync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocksync.app.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToLowStock: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToLocation: () -> Unit = {}
) {
    val todaySalesTotal by viewModel.todaySalesTotal.collectAsState()
    val transactionCount by viewModel.transactionCount.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val productCount by viewModel.productCount.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()
    val threshold by viewModel.lowStockThreshold.collectAsState()

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZA")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Today's sales summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.today_sales),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currencyFormat.format(todaySalesTotal)} | $transactionCount ${stringResource(R.string.transactions)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF27AE60)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Low stock tile
                Card(
                    modifier = Modifier.weight(1f).clickable { onNavigateToLowStock() }
                        .semantics { role = Role.Button; contentDescription = "Low stock: $lowStockCount items" },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.low_stock), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$lowStockCount ${stringResource(R.string.items)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
                    }
                }

                // Products tile
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.products_total), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$productCount ${stringResource(R.string.total)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2980B9))
                    }
                }

                // Pending tile
                Card(
                    modifier = Modifier.weight(1f).clickable { onNavigateToSync() }
                        .semantics { role = Role.Button; contentDescription = if (pendingCount > 0) "$pendingCount pending sync" else "All synced" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (pendingCount > 0) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.pending), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$pendingCount", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = if (pendingCount > 0) Color(0xFFC0392B) else Color(0xFF888888))
                    }
                }
            }
        }

        item {
            // Top sellers card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.top_sellers), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (topProducts.isEmpty()) {
                        Text(stringResource(R.string.no_sales_today), fontSize = 12.sp, color = Color(0xFF888888))
                    } else {
                        topProducts.forEachIndexed { index, (name, qty) ->
                            Text(
                                text = "${index + 1}. $name — $qty sold",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Sync status indicator
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToSync() }
                    .semantics { role = Role.Button },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pendingCount == 0) {
                    Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.synced_status), tint = Color(0xFF27AE60))
                    Text(" ${stringResource(R.string.synced)}", fontSize = 12.sp, color = Color(0xFF27AE60))
                } else {
                    Icon(Icons.Default.Pending, contentDescription = "$pendingCount pending", tint = Color(0xFFC0392B))
                    Text(" ${pendingCount} ${stringResource(R.string.sync_pending)}", fontSize = 12.sp, color = Color(0xFFC0392B))
                }
            }
        }

        item {
            // Store location button
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToLocation() }
                    .semantics { role = Role.Button; contentDescription = "Store location" },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.store_location_button), tint = Color(0xFF2980B9))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.store_location), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
