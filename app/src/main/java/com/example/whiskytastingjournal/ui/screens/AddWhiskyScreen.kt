package com.example.whiskytastingjournal.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.whiskytastingjournal.model.Distillery
import com.example.whiskytastingjournal.model.Whisky
import com.example.whiskytastingjournal.ui.components.DistilleryField
import com.example.whiskytastingjournal.util.scaleDownPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWhiskyScreen(
    onSave: (Whisky) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allDistilleries = remember { Distillery.loadAll(context) }

    val newWhiskyId = remember { UUID.randomUUID().toString() }
    val photoDir = remember { context.getExternalFilesDir("photos")?.also { it.mkdirs() } }
    val photoFile = remember { File(photoDir ?: context.filesDir, "${newWhiskyId}_bottle.jpg") }
    val photoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var cameraPermissionDenied by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            scope.launch {
                withContext(Dispatchers.IO) { scaleDownPhoto(photoFile) }
                photoPath = photoFile.absolutePath
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraPermissionDenied = false
            cameraLauncher.launch(photoUri)
        } else {
            cameraPermissionDenied = true
        }
    }

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionDenied = false
            cameraLauncher.launch(photoUri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var distillery by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var whiskyName by remember { mutableStateOf("") }
    var batchCode by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var bottlingYearStr by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Whisky", style = MaterialTheme.typography.titleLarge) },
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

            // Bottle photo
            OutlinedButton(
                onClick = { launchCamera() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (photoPath != null) "Retake Bottle Photo" else "Take Bottle Photo")
            }
            if (cameraPermissionDenied) {
                Text(
                    text = "Camera permission denied. Enable it in system settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            photoPath?.let { path ->
                AsyncImage(
                    model = File(path),
                    contentDescription = "Bottle photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(
                            Whisky(
                                id = newWhiskyId,
                                distillery = distillery,
                                country = country,
                                region = region,
                                whiskyName = whiskyName,
                                batchCode = batchCode,
                                age = ageStr.toIntOrNull(),
                                bottlingYear = bottlingYearStr.toIntOrNull(),
                                photoPath = photoPath
                            )
                        )
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
