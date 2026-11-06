package com.stocksync.app

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.unit.sp
import com.stocksync.app.data.api.ApiClient
import com.stocksync.app.data.db.StockSyncDatabase
import com.stocksync.app.data.preferences.SettingsManager
import com.stocksync.app.data.preferences.TokenManager
import com.stocksync.app.data.repository.StockSyncRepository
import com.stocksync.app.ui.navigation.Screen
import com.stocksync.app.ui.navigation.bottomNavItems
import com.stocksync.app.ui.screens.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        setContent {
            StockSyncTheme {
                AppContent()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }
}

@Composable
fun StockSyncTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF27AE60),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE8F5E9),
            onPrimaryContainer = Color(0xFF1E8449),
            secondary = Color(0xFF2980B9),
            onSecondary = Color.White,
            background = Color(0xFFF8F9FA),
            onBackground = Color(0xFF1A1A1A),
            surface = Color.White,
            onSurface = Color(0xFF1A1A1A),
            error = Color(0xFFC0392B),
            onError = Color.White
        ),
        content = content
    )
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val tokenManager = remember { TokenManager(context) }
    val db = remember { StockSyncDatabase.getDatabase(context) }
    val settingsManager = remember { SettingsManager(context) }
    val repository = remember { StockSyncRepository(ApiClient.api, db, tokenManager) }

    val startDestination = remember { if (tokenManager.isLoggedIn()) Screen.Dashboard.route else Screen.Login.route }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route, Screen.Products.route,
        Screen.Sell.route, Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.labelResId), fontSize = 10.sp) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF27AE60),
                                selectedTextColor = Color(0xFF27AE60)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                val authViewModel: AuthViewModel = viewModel {
                    AuthViewModel(repository)
                }
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        Log.d("AppContent", "Login success, navigating to biometric")
                        navController.navigate(Screen.Biometric.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Biometric.route) {
                Log.d("AppContent", "Biometric route composed")
                BiometricScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Biometric.route) { inclusive = true }
                        }
                    },
                    onAuthSkip = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Biometric.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel {
                    DashboardViewModel(repository, db.productDao(), db.saleDao(), db.pendingSyncDao(), settingsManager)
                }
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToLowStock = { navController.navigate(Screen.Products.route) },
                    onNavigateToSync = { navController.navigate(Screen.SyncStatus.route) },
                    onNavigateToLocation = { navController.navigate(Screen.StoreLocation.route) }
                )
            }

            composable(Screen.Products.route) {
                val viewModel: ProductViewModel = viewModel {
                    ProductViewModel(repository, db.productDao())
                }
                ProductListScreen(
                    viewModel = viewModel,
                    onProductClick = { product ->
                        navController.navigate("product_detail/${product.id}")
                    },
                    onAddProduct = { navController.navigate(Screen.AddProduct.route) }
                )
            }

            composable(Screen.Sell.route) {
                val viewModel: ProductViewModel = viewModel {
                    ProductViewModel(repository, db.productDao())
                }
                QuickSaleScreen(
                    viewModel = viewModel,
                    onSaleLogged = { },
                    onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                    onScanBarcode = { navController.navigate(Screen.BarcodeScanner.route) }
                )
            }

            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel {
                    SettingsViewModel(settingsManager, repository)
                }
                SettingsScreen(
                    viewModel = viewModel,
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AddProduct.route) {
                val viewModel: ProductViewModel = viewModel {
                    ProductViewModel(repository, db.productDao())
                }
                val scannedBarcode = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>("scanned_barcode")
                AddProductScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { name, price, qty, category, barcode, photoBlob ->
                        viewModel.addProduct(name, price, qty, category, barcode, photoBlob)
                        navController.popBackStack()
                    },
                    onScanBarcode = { navController.navigate(Screen.BarcodeScanner.route) },
                    initialBarcode = scannedBarcode
                )
            }

            composable(Screen.ProductDetail.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
                val viewModel: ProductViewModel = viewModel {
                    ProductViewModel(repository, db.productDao())
                }
                val product = remember { mutableStateOf<com.stocksync.app.data.db.ProductEntity?>(null) }
                LaunchedEffect(productId) {
                    product.value = db.productDao().getProductById(productId)
                }
                product.value?.let { p ->
                    ProductDetailScreen(
                        product = p,
                        onBack = { navController.popBackStack() },
                        onEdit = {
                            navController.navigate("edit_product/${p.id}")
                        },
                        onDelete = {
                            viewModel.deleteProduct(p.id)
                            navController.popBackStack()
                        },
                        onStockIn = { qty -> viewModel.adjustStock(p.id, qty, "Restock") },
                        onLogSale = { qty, method -> viewModel.logSale(p.id, qty, p.price, method) }
                    )
                }
            }

            composable("edit_product/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
                val viewModel: ProductViewModel = viewModel {
                    ProductViewModel(repository, db.productDao())
                }
                val product = remember { mutableStateOf<com.stocksync.app.data.db.ProductEntity?>(null) }
                LaunchedEffect(productId) {
                    product.value = db.productDao().getProductById(productId)
                }
                product.value?.let { p ->
                    AddProductScreen(
                        onBack = { navController.popBackStack() },
                        onSave = { name, price, qty, category, barcode, photoBlob ->
                            viewModel.updateProduct(
                                p.copy(
                                    name = name,
                                    price = price,
                                    quantity = qty,
                                    category = category,
                                    barcode = barcode,
                                    photoBlob = photoBlob ?: p.photoBlob
                                )
                            )
                            navController.popBackStack()
                        },
                        onScanBarcode = { navController.navigate(Screen.BarcodeScanner.route) },
                        initialBarcode = p.barcode,
                        existingProduct = p
                    )
                }
            }

            composable(Screen.SyncStatus.route) {
                val pendingCount by db.pendingSyncDao().getPendingCount().collectAsState(initial = 0)
                var syncTriggered by remember { mutableStateOf(false) }
                var lastSyncTime by remember { mutableStateOf("Never") }
                LaunchedEffect(syncTriggered) {
                    if (syncTriggered) {
                        val result = repository.syncPending()
                        result.onSuccess {
                            val now = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                            lastSyncTime = now
                        }
                        syncTriggered = false
                    }
                }
                SyncStatusScreen(
                    pendingCount = pendingCount,
                    lastSyncTime = lastSyncTime,
                    onBack = { navController.popBackStack() },
                    onSyncNow = { syncTriggered = true }
                )
            }

            composable(Screen.BarcodeScanner.route) {
                BarcodeScannerScreen(
                    onBack = { navController.popBackStack() },
                    onBarcodeScanned = { barcode ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.StoreLocation.route) {
                StoreLocationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
