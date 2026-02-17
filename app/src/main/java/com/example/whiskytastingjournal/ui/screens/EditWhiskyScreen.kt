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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.whiskytastingjournal.model.Distillery
import com.example.whiskytastingjournal.model.Whisky
import com.example.whiskytastingjournal.ui.TastingViewModel
import com.example.whiskytastingjournal.ui.components.DistilleryField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWhiskyScreen(
    whiskyId: String,
    viewModel: TastingViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val allDistilleries = remember { Distillery.loadAll(context) }

    var loaded by remember { mutableStateOf(false) }
    var originalWhisky by remember { mutableStateOf<Whisky?>(null) }

    var distillery by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var whiskyName by remember { mutableStateOf("") }
    var batchCode by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var bottlingYearStr by remember { mutableStateOf("") }

    LaunchedEffect(whiskyId) {
        viewModel.getWhiskyById(whiskyId)?.let { whisky ->
            originalWhisky = whisky
            distillery = whisky.distillery
            country = whisky.country
            region = whisky.region
            whiskyName = whisky.whiskyName
            batchCode = whisky.batchCode
            ageStr = whisky.age?.toString() ?: ""
            bottlingYearStr = whisky.bottlingYear?.toString() ?: ""
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Whisky", style = MaterialTheme.typography.titleLarge) },
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
        if (!loaded) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Bottle Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                DistilleryField(
                    value = distillery,
                    onValueChange = { distillery = it },
                    onDistillerySelected = { d ->
                        distillery = d.name
                        country = d.country
                        region = d.region
                    },
                    distilleries = allDistilleries
                )

                if (country.isNotBlank()) {
                    Text(
                        text = "$region, $country",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = whiskyName,
                    onValueChange = { whiskyName = it },
                    label = { Text("Whisky Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = batchCode,
                    onValueChange = { batchCode = it },
                    label = { Text("Batch/Bottle Code (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Age (years)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("yo") },
                        placeholder = { Text("optional") }
                    )
                    OutlinedTextField(
                        value = bottlingYearStr,
                        onValueChange = { bottlingYearStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Bottling Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("optional") }
                    )
                }

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
                        onClick = {
                            originalWhisky?.let { orig ->
                                viewModel.updateWhisky(
                                    orig.copy(
                                        distillery = distillery,
                                        country = country,
                                        region = region,
                                        whiskyName = whiskyName,
                                        batchCode = batchCode,
                                        age = ageStr.toIntOrNull(),
                                        bottlingYear = bottlingYearStr.toIntOrNull()
                                    )
                                )
                            }
                            onSaved()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = distillery.isNotBlank() && whiskyName.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
