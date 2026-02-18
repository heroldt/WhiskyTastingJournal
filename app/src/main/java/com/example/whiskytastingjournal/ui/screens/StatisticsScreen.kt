package com.example.whiskytastingjournal.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.whiskytastingjournal.model.AromaTagCount
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.model.WhiskyWithTastings
import com.example.whiskytastingjournal.ui.TastingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: TastingViewModel,
    bottomBar: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allWhiskies by viewModel.allWhiskiesUnfiltered.collectAsState()
    val topAromas by viewModel.topAromaTags.collectAsState()
    val exportImportState by viewModel.exportImportState.collectAsState()

    val allTastings = allWhiskies.flatMap { it.tastings }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(context, it) }
    }

    LaunchedEffect(exportImportState) {
        when (val state = exportImportState) {
            is TastingViewModel.ExportImportState.ExportReady -> {
                val uri = FileProvider.getUriForFile(
                    context, "${context.packageName}.fileprovider", state.file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Backup"))
                viewModel.clearExportImportState()
            }
            is TastingViewModel.ExportImportState.ImportDone -> {
                val r = state.result
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Import complete: ${r.whiskiesImported} whiskies, " +
                            "${r.tastingsImported} tastings, ${r.photosImported} photos"
                    )
                }
                viewModel.clearExportImportState()
            }
            is TastingViewModel.ExportImportState.Error -> {
                scope.launch { snackbarHostState.showSnackbar("Error: ${state.message}") }
                viewModel.clearExportImportState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Statistics", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // --- Collection overview ---
            item {
                val totalWhiskies = allWhiskies.size
                val totalTastings = allTastings.size
                val totalDistilleries = allWhiskies.map { it.whisky.distillery }
                    .filter { it.isNotBlank() }.distinct().size
                val totalCountries = allWhiskies.map { it.whisky.country }
                    .filter { it.isNotBlank() }.distinct().size

                StatCard("Collection") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatNumber(totalWhiskies.toString(), "Whiskies")
                        StatNumber(totalTastings.toString(), "Tastings")
                        StatNumber(totalDistilleries.toString(), "Distilleries")
                        StatNumber(totalCountries.toString(), "Countries")
                    }
                }
            }

            // --- Average scores ---
            if (allTastings.isNotEmpty()) {
                item {
                    val avgNose = allTastings.map { it.noseScore }.average().toFloat()
                    val avgPalate = allTastings.map { it.palateScore }.average().toFloat()
                    val avgFinish = allTastings.map { it.finishScore }.average().toFloat()
                    val avgOverall = allTastings.map { it.effectiveOverallScore }.average().toFloat()

                    StatCard("Average Scores") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatNumber(String.format("%.1f", TastingEntry.roundToHalf(avgNose)), "Nose")
                            StatNumber(String.format("%.1f", TastingEntry.roundToHalf(avgPalate)), "Palate")
                            StatNumber(String.format("%.1f", TastingEntry.roundToHalf(avgFinish)), "Finish")
                            StatNumber(String.format("%.1f", TastingEntry.roundToHalf(avgOverall)), "Overall")
                        }
                    }
                }
            }

            // --- Top 5 whiskies by average score ---
            val topWhiskies = allWhiskies
                .filter { it.tastings.isNotEmpty() }
                .map { wt -> wt to wt.tastings.map { it.effectiveOverallScore }.average().toFloat() }
                .sortedByDescending { it.second }
                .take(5)

            if (topWhiskies.isNotEmpty()) {
                item {
                    StatCard("Top Whiskies") {
                        topWhiskies.forEachIndexed { index, (wt, avgScore) ->
                            if (index > 0) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                            TopWhiskyRow(wt, avgScore)
                        }
                    }
                }
            }

            // --- Most used aromas ---
            if (topAromas.isNotEmpty()) {
                item {
                    StatCard("Most Used Aromas") {
                        AromaBarChart(topAromas)
                    }
                }
            }

            // --- By country ---
            val byCountry = allWhiskies
                .groupBy { it.whisky.country.ifBlank { "Unknown" } }
                .mapValues { it.value.size }
                .entries
                .sortedByDescending { it.value }

            if (byCountry.size > 1) {
                item {
                    StatCard("By Country") {
                        CountryBarChart(byCountry)
                    }
                }
            }

            // --- By region (top regions) ---
            val byRegion = allWhiskies
                .filter { it.whisky.region.isNotBlank() }
                .groupBy { it.whisky.region }
                .mapValues { it.value.size }
                .entries
                .sortedByDescending { it.value }
                .take(8)

            if (byRegion.size > 1) {
                item {
                    StatCard("By Region") {
                        CountryBarChart(byRegion)
                    }
                }
            }

            // --- Backup & Restore ---
            item {
                StatCard("Backup & Restore") {
                    val isLoading = exportImportState is TastingViewModel.ExportImportState.Loading

                    Text(
                        text = "Export all whiskies, tastings and photos as a ZIP file. " +
                            "Import a previously exported ZIP to restore your data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        }
                    } else {
                        Button(
                            onClick = { viewModel.exportData(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Export All Tastings")
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/zip")) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Import from ZIP")
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TopWhiskyRow(wt: WhiskyWithTastings, avgScore: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = wt.whisky.whiskyName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = buildString {
                    append(wt.whisky.distillery)
                    wt.whisky.age?.let { append("  \u2022  ${it}yo") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = String.format("%.1f", TastingEntry.roundToHalf(avgScore)),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun AromaBarChart(aromas: List<AromaTagCount>) {
    val maxCount = aromas.maxOfOrNull { it.count } ?: 1
    aromas.forEach { aroma ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = aroma.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${aroma.count}\u00d7",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = aroma.count.toFloat() / maxCount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CountryBarChart(entries: List<Map.Entry<String, Int>>) {
    val maxCount = entries.maxOfOrNull { it.value } ?: 1
    entries.forEach { (name, count) ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        LinearProgressIndicator(
            progress = count.toFloat() / maxCount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun StatCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun StatNumber(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
