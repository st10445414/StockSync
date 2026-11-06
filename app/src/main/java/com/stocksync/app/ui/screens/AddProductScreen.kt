package com.stocksync.app.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocksync.app.R
import com.stocksync.app.data.db.ProductEntity
import java.io.ByteArrayOutputStream

@Composable
fun AddProductScreen(
    onBack: () -> Unit,
    onSave: (name: String, price: Double, quantity: Int, category: String?, barcode: String?, photoBlob: ByteArray?) -> Unit,
    onScanBarcode: () -> Unit = {},
    initialBarcode: String? = null,
    existingProduct: ProductEntity? = null
) {
    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var price by remember { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    var quantity by remember { mutableStateOf(existingProduct?.quantity?.toString() ?: "") }
    var category by remember { mutableStateOf(existingProduct?.category ?: "") }
    var barcode by remember { mutableStateOf(existingProduct?.barcode ?: initialBarcode ?: "") }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var photoBlob by remember { mutableStateOf<ByteArray?>(existingProduct?.photoBlob) }
    var photoBitmap by remember {
        mutableStateOf<Bitmap?>(existingProduct?.photoBlob?.let { BitmapFactory.decodeByteArray(it, 0, it.size) })
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            photoBitmap = bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            photoBlob = stream.toByteArray()
            Log.d("AddProductScreen", "Photo captured, BLOB size: ${photoBlob?.size} bytes")
        }
    }

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
                Text(
                    text = stringResource(if (existingProduct != null) R.string.edit_product else R.string.add_product),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    nameError = name.isBlank()
                    priceError = price.isBlank() || price.toDoubleOrNull() == null
                    if (!nameError && !priceError) {
                        onSave(
                            name.trim(),
                            price.toDouble(),
                            quantity.toIntOrNull() ?: 0,
                            category.ifBlank { null },
                            barcode.ifBlank { null },
                            photoBlob
                        )
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save),
                        tint = Color(0xFF27AE60))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                onClick = { cameraLauncher.launch(null) }
            ) {
                if (photoBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = photoBitmap!!.asImageBitmap(),
                        contentDescription = stringResource(R.string.add_photo),
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.add_photo), fontSize = 12.sp, color = Color(0xFFAAAAAA))
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text(stringResource(R.string.product_name)) },
                isError = nameError,
                supportingText = { if (nameError) Text(stringResource(R.string.error_name_required)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' }; priceError = false },
                label = { Text(stringResource(R.string.product_price)) },
                isError = priceError,
                supportingText = { if (priceError) Text(stringResource(R.string.error_price_required)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.product_quantity)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.product_category)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text(stringResource(R.string.product_barcode)) },
                trailingIcon = {
                    IconButton(onClick = onScanBarcode) {
                        Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.scan))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
