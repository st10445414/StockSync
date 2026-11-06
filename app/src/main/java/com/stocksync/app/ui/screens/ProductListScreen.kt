package com.stocksync.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.stocksync.app.R
import com.stocksync.app.data.db.ProductEntity

@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    onProductClick: (ProductEntity) -> Unit,
    onAddProduct: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct, containerColor = Color(0xFF27AE60)) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product), tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductEntity, onClick: () -> Unit) {
    val isLowStock = product.quantity < product.lowStockThreshold
    val isOutOfStock = product.quantity == 0

    // Decode BLOB photo to bitmap for display
    val photoBitmap = product.photoBlob?.let {
        remember(product.id, product.photoBlob) { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOutOfStock -> Color(0xFFFFEBEE)
                isLowStock -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product photo thumbnail (from BLOB storage)
            if (photoBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = photoBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.product_photo),
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.price_format, product.price),
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "${product.quantity} ${stringResource(R.string.in_stock)}",
                    fontSize = 11.sp,
                    color = when {
                        isOutOfStock -> Color(0xFFC0392B)
                        isLowStock -> Color(0xFFE67E22)
                        else -> Color(0xFF27AE60)
                    }
                )
            }
            if (isLowStock || isOutOfStock) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = if (isOutOfStock) stringResource(R.string.out_of_stock_warning) else stringResource(R.string.low_stock_warning),
                    tint = if (isOutOfStock) Color(0xFFC0392B) else Color(0xFFE67E22),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
