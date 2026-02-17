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
            Text("Loading...", modifier = Modifier.padding(padding).padding(16.dp))
        } else {
            val whisky = wt.whisky
            val tastings = wt.tastings.sortedByDescending { it.date }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { WhiskyInfoCard(whisky) }

                if (tastings.isNotEmpty()) {
                    item { AverageScoresCard(tastings) }
                }

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
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun WhiskyInfoCard(whisky: Whisky) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = whisky.whiskyName, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = whisky.distillery,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (whisky.region.isNotBlank() || whisky.country.isNotBlank()) {
                Text(
                    text = listOf(whisky.region, whisky.country).filter { it.isNotBlank() }.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val meta = buildList {
                whisky.age?.let { add("${it}yo") }
                whisky.bottlingYear?.let { add("Bottled $it") }
                if (whisky.batchCode.isNotBlank()) add("Batch: ${whisky.batchCode}")
            }.joinToString("  \u2022  ")
            if (meta.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AverageScoresCard(tastings: List<TastingEntry>) {
    val avgNose = tastings.map { it.noseScore }.average().toFloat()
    val avgPalate = tastings.map { it.palateScore }.average().toFloat()
    val avgFinish = tastings.map { it.finishScore }.average().toFloat()
    val avgOverall = tastings.map { it.effectiveOverallScore }.average().toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Average Scores",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ScoreColumn("Nose", avgNose)
                ScoreColumn("Palate", avgPalate)
                ScoreColumn("Finish", avgFinish)
                ScoreColumn("Overall", avgOverall)
            }
        }
    }
}

@Composable
private fun ScoreColumn(label: String, score: Float) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            text = String.format("%.1f", TastingEntry.roundToHalf(score)),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun TastingEntryCard(
    tasting: TastingEntry,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tasting.date.format(dateFormatter), style = MaterialTheme.typography.titleMedium)
                    if (tasting.alias.isNotBlank()) {
                        Text(
                            text = tasting.alias,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        text = String.format("%.1f", TastingEntry.roundToHalf(tasting.effectiveOverallScore)),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ScoreLabel("Nose", tasting.noseScore)
                ScoreLabel("Palate", tasting.palateScore)
                ScoreLabel("Finish", tasting.finishScore)
            }

            if (tasting.price.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${tasting.price}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Notes preview (compact — full notes visible in edit screen)
            listOf(
                "Nose" to tasting.noseNotes,
                "Palate" to tasting.palateNotes,
                "Finish" to tasting.finishNotes
            ).forEach { (label, notes) ->
                if (!notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$label: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreLabel(label: String, score: Float) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            text = String.format("%.1f", TastingEntry.roundToHalf(score)),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
