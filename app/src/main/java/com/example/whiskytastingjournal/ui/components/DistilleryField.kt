package com.example.whiskytastingjournal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whiskytastingjournal.model.Distillery

@Composable
fun DistilleryField(
    value: String,
    onValueChange: (String) -> Unit,
    onDistillerySelected: (Distillery) -> Unit,
    distilleries: List<Distillery>,
    modifier: Modifier = Modifier
) {
    var showSuggestions by remember { mutableStateOf(false) }

    val filtered = remember(value, distilleries) {
        if (value.length < 2) emptyList()
        else {
            val q = value.lowercase()
            distilleries.filter {
                it.name.lowercase().contains(q) ||
                    it.region.lowercase().contains(q) ||
                    it.country.lowercase().contains(q)
            }.take(8)
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                showSuggestions = true
            },
            label = { Text("Distillery") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (showSuggestions && filtered.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(filtered) { distillery ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDistillerySelected(distillery)
                                    showSuggestions = false
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = distillery.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${distillery.region}, ${distillery.country}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
