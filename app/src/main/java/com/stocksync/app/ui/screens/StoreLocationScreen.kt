package com.stocksync.app.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.stocksync.app.R

@Composable
fun StoreLocationScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            loading = true
            fetchLocation(context) { lat, lon, err ->
                loading = false
                if (err != null) errorMsg = err else { latitude = lat; longitude = lon }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        else {
            loading = true
            fetchLocation(context) { lat, lon, err ->
                loading = false
                if (err != null) errorMsg = err else { latitude = lat; longitude = lon }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(stringResource(R.string.store_location), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF27AE60),
                modifier = Modifier.size(64.dp)
            )

            if (!hasLocationPermission) {
                Text(stringResource(R.string.location_permission_required), fontSize = 16.sp)
                Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                    Text(stringResource(R.string.grant_permission))
                }
            } else if (loading) {
                CircularProgressIndicator()
                Text(stringResource(R.string.fetching_location), fontSize = 14.sp)
            } else if (errorMsg != null) {
                Text(errorMsg!!, fontSize = 14.sp, color = Color(0xFFC0392B))
                Button(onClick = {
                    loading = true
                    errorMsg = null
                    fetchLocation(context) { lat, lon, err ->
                        loading = false
                        if (err != null) errorMsg = err else { latitude = lat; longitude = lon }
                    }
                }) { Text(stringResource(R.string.retry)) }
            } else if (latitude != null && longitude != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.store_location), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${stringResource(R.string.latitude)}: ${"%.6f".format(latitude)}", fontSize = 14.sp)
                        Text("${stringResource(R.string.longitude)}: ${"%.6f".format(longitude)}", fontSize = 14.sp)
                    }
                }
                // Static map placeholder showing coordinates
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE67E22), modifier = Modifier.size(48.dp))
                            Text(stringResource(R.string.map_placeholder), fontSize = 12.sp, color = Color(0xFF666666))
                            Text("${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}", fontSize = 12.sp, color = Color(0xFF666666))
                        }
                    }
                }
                Button(
                    onClick = {
                        loading = true
                        fetchLocation(context) { lat, lon, err ->
                            loading = false
                            if (err != null) errorMsg = err else { latitude = lat; longitude = lon }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
                ) { Text(stringResource(R.string.refresh_location)) }
            }
        }
    }
}

private fun fetchLocation(
    context: Context,
    onResult: (Double, Double, String?) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    try {
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    onResult(location.latitude, location.longitude, null)
                } else {
                    onResult(0.0, 0.0, "Unable to get location. Try again.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("StoreLocation", "Location fetch failed", e)
                onResult(0.0, 0.0, e.message)
            }
    } catch (e: SecurityException) {
        onResult(0.0, 0.0, "Location permission denied")
    }
}
