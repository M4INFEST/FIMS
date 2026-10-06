package com.example.fims.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fims.data.TransactionType
import com.example.fims.viewmodel.DashboardUiState
import com.example.fims.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToItems: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToStockIn: () -> Unit,
    onNavigateToStockOut: () -> Unit,
    onNavigateToWastage: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FIMS Enterprise", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(paddingValues)
        ) {
            Crossfade(targetState = uiState, label = "DashboardStateAnimation") { state ->
                when (state) {
                    is DashboardUiState.Loading -> DashboardShimmerLoading()
                    is DashboardUiState.Error -> DashboardError(state.message)
                    is DashboardUiState.Success -> DashboardContent(
                        state = state,
                        onNavigateToItems = onNavigateToItems,
                        onNavigateToAlerts = onNavigateToAlerts,
                        onNavigateToStockIn = onNavigateToStockIn,
                        onNavigateToStockOut = onNavigateToStockOut,
                        onNavigateToWastage = onNavigateToWastage,
                        onNavigateToHistory = onNavigateToHistory
                    )
                }
            }
        }
    }
}

/**
 * Enterprise Dashboard Content featuring advanced Canvas charts, scrolling feeds, and grids.
 */
@Composable
fun DashboardContent(
    state: DashboardUiState.Success,
    onNavigateToItems: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToStockIn: () -> Unit,
    onNavigateToStockOut: () -> Unit,
    onNavigateToWastage: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Advanced Custom Canvas Chart Segment
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Inventory Health", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Complex Canvas Drawing
                    DonutChart(
                        normal = state.normalStockCount,
                        low = state.lowStockCount,
                        outOfStock = state.outOfStockCount
                    )
                }
            }
        }

        // Animated Counters Segment
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AnimatedCounterCard(
                    title = "Total Items",
                    value = state.totalItems,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                AnimatedCounterCard(
                    title = "Near Expiry",
                    value = state.nearExpiryCount,
                    color = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f)
                )
                AnimatedCounterCard(
                    title = "Expired",
                    value = state.expiredCount,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Grid Segment
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(220.dp), // Fixed height to prevent nested lazy scrolling exception
                userScrollEnabled = false
            ) {
                item { ActionTile("Items", Icons.AutoMirrored.Filled.List, MaterialTheme.colorScheme.primary, onNavigateToItems) }
                item { ActionTile("In", Icons.Default.Add, Color(0xFF388E3C), onNavigateToStockIn) }
                item { ActionTile("Out", Icons.Default.ShoppingCart, Color(0xFFF57C00), onNavigateToStockOut) }
                item { ActionTile("Wastage", Icons.Default.Delete, Color.Red, onNavigateToWastage) }
                item { ActionTile("Alerts", Icons.Default.Warning, MaterialTheme.colorScheme.error, onNavigateToAlerts) }
                item { ActionTile("History", Icons.Default.Info, MaterialTheme.colorScheme.secondary, onNavigateToHistory) }
            }
        }

        item {
            Text("Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Feed Segment
        items(state.recentTransactions) { txItem ->
            val isPositive = txItem.transaction.type == TransactionType.STOCK_IN
            val iconColor = if (isPositive) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
            val sign = if (isPositive) "+" else "-"

            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sign, color = iconColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(txItem.itemName, fontWeight = FontWeight.Bold)
                        Text(txItem.transaction.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("$sign${txItem.transaction.quantity}", fontWeight = FontWeight.Bold, color = iconColor)
                }
            }
        }
        
        if (state.recentTransactions.isEmpty()) {
            item {
                Text("No recent activity.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Advanced Custom Donut Chart using Jetpack Compose Canvas.
 */
@Composable
fun DonutChart(normal: Int, low: Int, outOfStock: Int) {
    val total = (normal + low + outOfStock).toFloat()
    
    // Animation angles
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "ChartAnimation"
    )

    val errorColor = MaterialTheme.colorScheme.error

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (total == 0f) {
                drawArc(
                    color = Color.LightGray,
                    startAngle = 0f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                )
                return@Canvas
            }

            val normalAngle = (normal / total) * 360f
            val lowAngle = (low / total) * 360f
            val outAngle = (outOfStock / total) * 360f

            var startAngle = -90f

            // Draw Normal (Green)
            if (normal > 0) {
                drawArc(
                    color = Color(0xFF388E3C),
                    startAngle = startAngle,
                    sweepAngle = normalAngle * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 50f, cap = StrokeCap.Round)
                )
                startAngle += normalAngle
            }
            // Draw Low (Amber)
            if (low > 0) {
                drawArc(
                    color = Color(0xFFF57C00),
                    startAngle = startAngle,
                    sweepAngle = lowAngle * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 50f, cap = StrokeCap.Round)
                )
                startAngle += lowAngle
            }
            // Draw Out of Stock (Red)
            if (outOfStock > 0) {
                drawArc(
                    color = errorColor,
                    startAngle = startAngle,
                    sweepAngle = outAngle * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 50f, cap = StrokeCap.Round)
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(total.toInt().toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Total", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Animated Number Counter for robust analytics display.
 */
@Composable
fun AnimatedCounterCard(title: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    var animValue by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(value) {
        animValue = value
    }
    
    val count by animateIntAsState(
        targetValue = animValue, 
        animationSpec = tween(durationMillis = 1500),
        label = "CounterAnimation"
    )

    ElevatedCard(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ActionTile(title: String, icon: ImageVector, iconTint: Color, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.height(100.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Enterprise Shimmer Effect for Loading State.
 */
@Composable
fun DashboardShimmerLoading() {
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "Alpha"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(24.dp)).background(Color.Gray.copy(alpha = alpha)))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray.copy(alpha = alpha)))
            Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray.copy(alpha = alpha)))
            Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray.copy(alpha = alpha)))
        }
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray.copy(alpha = alpha)))
    }
}

@Composable
fun DashboardError(msg: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Error loading dashboard: $msg", color = MaterialTheme.colorScheme.error)
    }
}