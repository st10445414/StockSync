package com.stocksync.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stocksync.app.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var thresholdInput by remember { mutableStateOf(settings.lowStockThreshold.toString()) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // General
        SectionHeader(stringResource(R.string.settings_general))
        SettingsCard(stringResource(R.string.settings_language), settings.language.uppercase()) {
            showLanguageDialog = true
        }
        SettingsCard(stringResource(R.string.settings_currency), settings.currency) {
            showCurrencyDialog = true
        }

        // Notifications
        SectionHeader(stringResource(R.string.settings_notifications))
        ToggleCard(
            label = stringResource(R.string.settings_low_stock_alerts),
            checked = settings.notifyLowStock,
            onToggle = { viewModel.updateNotifyLowStock(it) }
        )
        ToggleCard(
            label = stringResource(R.string.settings_daily_summary),
            checked = settings.notifyDailySummary,
            onToggle = { viewModel.updateNotifyDailySummary(it) }
        )
        ToggleCard(
            label = stringResource(R.string.settings_sync_confirmations),
            checked = settings.notifySyncComplete,
            onToggle = { viewModel.updateNotifySyncComplete(it) }
        )

        // Stock
        SectionHeader(stringResource(R.string.settings_stock))
        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.settings_low_stock_threshold), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = thresholdInput,
                        onValueChange = { thresholdInput = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        thresholdInput.toIntOrNull()?.let { viewModel.updateLowStockThreshold(it) }
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }

        // Security
        SectionHeader(stringResource(R.string.settings_security))
        ToggleCard(
            label = stringResource(R.string.settings_biometric_lock),
            checked = settings.biometricLock,
            onToggle = { viewModel.updateBiometricLock(it) }
        )

        // Account
        SectionHeader(stringResource(R.string.settings_account))
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            TextButton(onClick = { viewModel.signOut(); onSignOut() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.sign_out), color = Color(0xFFC0392B), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    listOf("en" to "English", "zu" to "isiZulu").forEach { (code, name) ->
                        TextButton(
                            onClick = {
                                viewModel.updateLanguage(code)
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(stringResource(R.string.settings_currency)) },
            text = {
                Column {
                    listOf("ZAR" to "ZAR (R)", "USD" to "USD ($)", "EUR" to "EUR (\u20AC)").forEach { (code, name) ->
                        TextButton(
                            onClick = {
                                viewModel.updateCurrency(code)
                                showCurrencyDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF27AE60),
        modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun SettingsCard(label: String, value: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp)
            Text(value, fontSize = 14.sp, color = Color(0xFF888888))
        }
    }
}

@Composable
private fun ToggleCard(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp)
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
