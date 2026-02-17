package com.example.whiskytastingjournal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.whiskytastingjournal.model.SenseType
import com.example.whiskytastingjournal.model.TastingAroma
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.ui.TastingViewModel
import com.example.whiskytastingjournal.ui.components.AromaTagSelector
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTastingScreen(
    tastingId: String,
    viewModel: TastingViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val allTags by viewModel.aromaTags.collectAsState()

    var loaded by remember { mutableStateOf(false) }
    var originalEntry by remember { mutableStateOf<TastingEntry?>(null) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var alias by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var noseScore by remember { mutableFloatStateOf(5f) }
    var palateScore by remember { mutableFloatStateOf(5f) }
    var finishScore by remember { mutableFloatStateOf(5f) }
    var overallManual by remember { mutableFloatStateOf(5f) }
    var overallManualEdited by remember { mutableStateOf(false) }

    var noseNotes by remember { mutableStateOf("") }
    var palateNotes by remember { mutableStateOf("") }
    var finishNotes by remember { mutableStateOf("") }

    var noseAromas by remember { mutableStateOf(setOf<String>()) }
    var palateAromas by remember { mutableStateOf(setOf<String>()) }
    var finishAromas by remember { mutableStateOf(setOf<String>()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    LaunchedEffect(tastingId) {
        viewModel.getTastingById(tastingId)?.let { entry ->
            originalEntry = entry
            selectedDate = entry.date
            alias = entry.alias
            price = entry.price
            noseScore = entry.noseScore
            palateScore = entry.palateScore
            finishScore = entry.finishScore
            overallManual = entry.effectiveOverallScore
            overallManualEdited = entry.overallScoreUser != null
            noseNotes = entry.noseNotes ?: ""
            palateNotes = entry.palateNotes ?: ""
            finishNotes = entry.finishNotes ?: ""
            loaded = true
        }
        val aromas = viewModel.getAromasForTasting(tastingId)
        noseAromas = aromas.filter { it.senseType == SenseType.NOSE.name }.map { it.aromaId }.toSet()
        palateAromas = aromas.filter { it.senseType == SenseType.PALATE.name }.map { it.aromaId }.toSet()
        finishAromas = aromas.filter { it.senseType == SenseType.FINISH.name }.map { it.aromaId }.toSet()
    }

    val autoScore = TastingEntry.computeOverallScoreAuto(noseScore, palateScore, finishScore)
    if (!overallManualEdited) overallManual = autoScore

    fun buildAromas(): List<TastingAroma> = buildList {
        noseAromas.forEach { add(TastingAroma(tastingId, it, SenseType.NOSE.name)) }
        palateAromas.forEach { add(TastingAroma(tastingId, it, SenseType.PALATE.name)) }
        finishAromas.forEach { add(TastingAroma(tastingId, it, SenseType.FINISH.name)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Tasting", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete tasting")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Loading...") }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // --- Tasting Details ---
                SectionCard("Tasting Details") {
                    OutlinedTextField(
                        value = selectedDate.format(dateFormatter),
                        onValueChange = {},
                        label = { Text("Date of Tasting") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text("Alias (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = { Text("$") }
                    )
                }

                // --- Nose ---
                SectionCard("Nose (Smell)") {
                    ScoreSlider("Nose Score", noseScore) { noseScore = it }
                    Text("Aromas", style = MaterialTheme.typography.bodyMedium)
                    AromaTagSelector(allTags, noseAromas, onToggle = { id ->
                        noseAromas = if (id in noseAromas) noseAromas - id else noseAromas + id
                    })
                    OutlinedTextField(
                        value = noseNotes,
                        onValueChange = { noseNotes = it },
                        label = { Text("Nose notes (optional)") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                }

                // --- Palate ---
                SectionCard("Palate (Taste)") {
                    ScoreSlider("Palate Score", palateScore) { palateScore = it }
                    Text("Aromas", style = MaterialTheme.typography.bodyMedium)
                    AromaTagSelector(allTags, palateAromas, onToggle = { id ->
                        palateAromas = if (id in palateAromas) palateAromas - id else palateAromas + id
                    })
                    OutlinedTextField(
                        value = palateNotes,
                        onValueChange = { palateNotes = it },
                        label = { Text("Palate notes (optional)") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                }

                // --- Finish ---
                SectionCard("Finish (Aftertaste)") {
                    ScoreSlider("Finish Score", finishScore) { finishScore = it }
                    Text("Aromas", style = MaterialTheme.typography.bodyMedium)
                    AromaTagSelector(allTags, finishAromas, onToggle = { id ->
                        finishAromas = if (id in finishAromas) finishAromas - id else finishAromas + id
                    })
                    OutlinedTextField(
                        value = finishNotes,
                        onValueChange = { finishNotes = it },
                        label = { Text("Finish notes (optional)") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                }

                // --- Overall Score ---
                SectionCard("Overall Score") {
                    Text(
                        text = "Auto: ${String.format("%.1f", TastingEntry.roundToHalf(autoScore))} " +
                            "(Nose 30% + Palate 50% + Finish 20%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ScoreSlider(
                        label = "Your Overall Score",
                        value = overallManual,
                        onValueChange = {
                            overallManual = it
                            overallManualEdited = true
                        }
                    )
                    if (overallManualEdited) {
                        TextButton(onClick = {
                            overallManualEdited = false
                            overallManual = autoScore
                        }) { Text("Reset to auto") }
                    }
                }

                // --- Save / Cancel ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            originalEntry?.let { orig ->
                                val updated = orig.copy(
                                    date = selectedDate,
                                    alias = alias,
                                    price = price,
                                    noseScore = noseScore,
                                    palateScore = palateScore,
                                    finishScore = finishScore,
                                    overallScoreAuto = autoScore,
                                    overallScoreUser = if (overallManualEdited) overallManual else null,
                                    noseNotes = noseNotes.ifBlank { null },
                                    palateNotes = palateNotes.ifBlank { null },
                                    finishNotes = finishNotes.ifBlank { null }
                                )
                                viewModel.updateTastingWithAromas(updated, buildAromas())
                            }
                            onSaved()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save") }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // --- Date picker ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // --- Delete dialog ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Tasting") },
            text = { Text("Are you sure you want to delete this tasting? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    originalEntry?.let { viewModel.deleteTasting(it) }
                    showDeleteDialog = false
                    onSaved()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
