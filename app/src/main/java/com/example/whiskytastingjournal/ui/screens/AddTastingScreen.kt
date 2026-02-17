package com.example.whiskytastingjournal.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.ui.TastingViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTastingScreen(
    whiskyId: String,
    viewModel: TastingViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var alias by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var sweetness by remember { mutableFloatStateOf(5f) }
    var smokiness by remember { mutableFloatStateOf(5f) }
    var fruitiness by remember { mutableFloatStateOf(5f) }
    var spice by remember { mutableFloatStateOf(5f) }
    var body by remember { mutableFloatStateOf(5f) }
    var finish by remember { mutableFloatStateOf(5f) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateEntry by remember { mutableStateOf<TastingEntry?>(null) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    fun buildEntry(id: String? = null): TastingEntry {
        return TastingEntry(
            id = id ?: TastingEntry().id,
            whiskyId = whiskyId,
            date = selectedDate,
            alias = alias,
            notes = notes,
            price = price,
            sweetness = sweetness,
            smokiness = smokiness,
            fruitiness = fruitiness,
            spice = spice,
            body = body,
            finish = finish
        )
    }

    fun trySave() {
        scope.launch {
            val existing = viewModel.findDuplicateTasting(
                whiskyId,
                selectedDate.toString(),
                alias
            )
            if (existing != null) {
                duplicateEntry = existing
                showDuplicateDialog = true
            } else {
                viewModel.addTasting(buildEntry())
                onSaved()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Tasting", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SectionHeader("Tasting Details")

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
                label = { Text("Alias (optional, e.g. \"Whiskey Fair 2023\")") },
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

            Spacer(modifier = Modifier.height(4.dp))

            SectionHeader("Tasting Notes")

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Your tasting notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(4.dp))

            SectionHeader("Flavor Profile")

            FlavorSlider("Sweetness", sweetness) { sweetness = it }
            FlavorSlider("Smokiness", smokiness) { smokiness = it }
            FlavorSlider("Fruitiness", fruitiness) { fruitiness = it }
            FlavorSlider("Spice", spice) { spice = it }
            FlavorSlider("Body", body) { body = it }
            FlavorSlider("Finish", finish) { finish = it }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { trySave() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Duplicate Tasting") },
            text = { Text("A tasting with this date and alias already exists. Duplicate or overwrite?") },
            confirmButton = {
                TextButton(onClick = {
                    // Overwrite: use the existing entry's id
                    duplicateEntry?.let { existing ->
                        viewModel.updateTasting(buildEntry(id = existing.id))
                    }
                    showDuplicateDialog = false
                    onSaved()
                }) {
                    Text("Overwrite")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showDuplicateDialog = false
                    }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        // Duplicate: save as new entry anyway
                        viewModel.addTasting(buildEntry())
                        showDuplicateDialog = false
                        onSaved()
                    }) {
                        Text("Duplicate")
                    }
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun FlavorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = String.format("%.0f", value),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
