package com.stocksync.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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

@Composable
fun SyncStatusScreen(
    pendingCount: Int,
    lastSyncTime: String,
    onBack: () -> Unit,
    onSyncNow: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(stringResource(R.string.sync_status), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pendingCount == 0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (pendingCount == 0) Color(0xFF27AE60) else Color(0xFFE67E22)
                        )
                        Text(
                            text = " " + (if (pendingCount == 0) stringResource(R.string.all_synced) else "$pendingCount ${stringResource(R.string.pending_changes)}"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${stringResource(R.string.last_sync)}: $lastSyncTime", fontSize = 12.sp, color = Color(0xFF888888))
                }
            }

            // Pending changes list
            Text(
                text = "${stringResource(R.string.pending_changes)} ($pendingCount)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (pendingCount == 0) {
                Text(stringResource(R.string.no_pending), fontSize = 12.sp, color = Color(0xFF999999))
            }

            // Sync now button
            Button(
                onClick = onSyncNow,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.sync_now))
            }
        }
    }
}
