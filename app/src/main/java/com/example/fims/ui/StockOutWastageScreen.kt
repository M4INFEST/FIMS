package com.example.fims.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fims.viewmodel.TransactionsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOutWastageScreen(
    viewModel: TransactionsViewModel,
    isWastage: Boolean,
    onNavigateBack: () -> Unit
) {
    val items by viewModel.activeItems.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val title = if (isWastage) "Record Wastage" else "Stock Out"
    val containerColor = if (isWastage) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    val onContainerColor = if (isWastage) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onTertiary

    var expanded by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<Int?>(null) }
    var selectedItemName by remember { mutableStateOf("") }

    var quantity by remember { mutableStateOf("") }
    
    var reasonExpanded by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf(if (isWastage) "Expired" else "Kitchen Use") }
    val wastageReasons = listOf("Expired", "Spoiled", "Damaged", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = onContainerColor,
                    navigationIconContentColor = onContainerColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = containerColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedItemName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Food Item") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            items.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        selectedItemId = item.itemId
                                        selectedItemName = item.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isWastage) {
                        ExposedDropdownMenuBox(
                            expanded = reasonExpanded,
                            onExpandedChange = { reasonExpanded = !reasonExpanded }
                        ) {
                            OutlinedTextField(
                                value = reason,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Reason") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = reasonExpanded,
                                onDismissRequest = { reasonExpanded = false }
                            ) {
                                wastageReasons.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            reason = r
                                            reasonExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Purpose (e.g. Kitchen Use)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val id = selectedItemId
                            val qty = quantity.toIntOrNull()
                            
                            if (id == null || qty == null || reason.isBlank()) {
                                Toast.makeText(context, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            coroutineScope.launch {
                                val result = viewModel.saveStockOutOrWastage(id, qty, isWastage, reason)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "$title Successful", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                } else {
                                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = onContainerColor)
                    ) {
                        Text("Confirm $title", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}