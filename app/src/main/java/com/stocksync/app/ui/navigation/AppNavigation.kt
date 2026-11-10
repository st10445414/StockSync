package com.stocksync.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stocksync.app.R

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Biometric : Screen("biometric")
    object Dashboard : Screen("dashboard")
    object Products : Screen("products")
    object Sell : Screen("sell")
    object Settings : Screen("settings")
    object AddProduct : Screen("add_product")
    object ProductDetail : Screen("product_detail/{productId}")
    object SyncStatus : Screen("sync_status")
    object BarcodeScanner : Screen("barcode_scanner")
    object StoreLocation : Screen("store_location")
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, Icons.Default.Home, R.string.nav_home),
    BottomNavItem(Screen.Products.route, Icons.Default.Inventory2, R.string.nav_products),
    BottomNavItem(Screen.Sell.route, Icons.Default.PointOfSale, R.string.nav_sell),
    BottomNavItem(Screen.Settings.route, Icons.Default.Settings, R.string.nav_settings)
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Products.route,
        Screen.Sell.route,
        Screen.Settings.route
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
            // Routes will be connected in MainActivity
        }
    }
}
