package com.example.fims.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fims.data.BatchInfo
import com.example.fims.viewmodel.AlertsAndHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsAndHistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val expiredBatches by viewModel.expiredBatches.collectAsState()
    val nearExpiryBatches by viewModel.nearExpiryBatches.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Alerts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    titleContentColor = MaterialTheme.colorScheme.onError,
                    navigationIconContentColor = MaterialTheme.colorScheme.onError
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Expired Batches (${expiredBatches.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (expiredBatches.isEmpty()) {
                item { Text("No expired batches found.", modifier = Modifier.padding(start = 8.dp)) }
            }
            items(expiredBatches) { batch ->
                AlertBatchRow(batch, isExpired = true)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Expiring Soon (${nearExpiryBatches.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (nearExpiryBatches.isEmpty()) {
                item { Text("No batches expiring in the next 3 days.", modifier = Modifier.padding(start = 8.dp)) }
            }
            items(nearExpiryBatches) { batch ->
                AlertBatchRow(batch, isExpired = false)
            }
        }
    }
}

@Composable
fun AlertBatchRow(info: BatchInfo, isExpired: Boolean) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(info.batch.expiryDate))

    val cardColor = if (isExpired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val onCardColor = if (isExpired) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor, contentColor = onCardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${info.itemName} (Batch: ${info.batch.batchNo})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Expires: $dateStr",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "${info.batch.quantity} ${info.itemUnit}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}