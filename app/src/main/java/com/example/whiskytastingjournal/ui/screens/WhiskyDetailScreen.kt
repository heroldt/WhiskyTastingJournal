package com.example.whiskytastingjournal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.model.Whisky
import com.example.whiskytastingjournal.model.WhiskyWithTastings
import com.example.whiskytastingjournal.ui.TastingViewModel
import com.example.whiskytastingjournal.ui.components.TastingWheel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiskyDetailScreen(
    whiskyId: String,
    viewModel: TastingViewModel,
    onEditWhisky: (String) -> Unit,
    onAddTasting: (String) -> Unit,
    onEditTasting: (String) -> Unit,
    onBack: () -> Unit
) {
    var whiskyWithTastings by remember { mutableStateOf<WhiskyWithTastings?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(whiskyId) {
        whiskyWithTastings = viewModel.getWhiskyWithTastings(whiskyId)
    }

    val wt = whiskyWithTastings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = wt?.whisky?.whiskyName ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditWhisky(whiskyId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit whisky")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete whisky")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddTasting(whiskyId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add tasting")
            }
        }
    ) { padding ->
        if (wt == null) {
            Text(
                "Loading...",
                modifier = Modifier.padding(padding).padding(16.dp)
            )
        } else {
            val whisky = wt.whisky
            val tastings = wt.tastings.sortedByDescending { it.date }
            val latestTasting = tastings.firstOrNull()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Whisky info header
                item {
                    Spacer(modifier = Modifier.height(0.dp))
                    WhiskyInfoCard(whisky)
                }

                // Tasting wheel showing average across all tastings
                if (tastings.isNotEmpty()) {
                    item {
                        val avgAttributes = mapOf(
                            "Sweetness" to tastings.map { it.sweetness }.average().toFloat(),
                            "Smokiness" to tastings.map { it.smokiness }.average().toFloat(),
                            "Fruitiness" to tastings.map { it.fruitiness }.average().toFloat(),
                            "Spice" to tastings.map { it.spice }.average().toFloat(),
                            "Body" to tastings.map { it.body }.average().toFloat(),
                            "Finish" to tastings.map { it.finish }.average().toFloat()
                        )
                        DetailSection("Tasting Wheel (Average)") {
                            TastingWheel(
                                attributes = avgAttributes,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Tastings list header
                item {
                    Text(
                        text = "Tastings (${tastings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (tastings.isEmpty()) {
                    item {
                        Text(
                            text = "No tastings yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(tastings, key = { it.id }) { tasting ->
                        TastingEntryCard(
                            tasting = tasting,
                            onClick = { onEditTasting(tasting.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Whisky") },
            text = { Text("Are you sure you want to delete this whisky and all its tastings? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWhiskyById(whiskyId)
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WhiskyInfoCard(whisky: Whisky) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = whisky.whiskyName,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = whisky.distillery,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (whisky.region.isNotBlank() || whisky.country.isNotBlank()) {
                Text(
                    text = listOf(whisky.region, whisky.country)
                        .filter { it.isNotBlank() }
                        .joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (whisky.batchCode.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Batch: ${whisky.batchCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TastingEntryCard(tasting: TastingEntry, onClick: () -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tasting.date.format(dateFormatter),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (tasting.alias.isNotBlank()) {
                        Text(
                            text = tasting.alias,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = String.format("%.1f", tasting.overallScore),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (tasting.price.isNotBlank()) {
                Text(
                    text = "$${tasting.price}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (tasting.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tasting.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tasting.flavorAttributes.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = String.format("%.0f", value),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = value / 10f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
