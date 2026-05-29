package com.example.cuan.feature.transaction.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.cuan.ui.components.SecondaryButtonComponent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary

/**
 * Scan Screen - Scan receipt or select from gallery (F-04)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onScanSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processImage(it, context) }
    }

    // Camera launcher (simplified - in real app would use CameraX)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Would process captured image
        }
    }

    LaunchedEffect(uiState.ocrText) {
        uiState.ocrText?.let { text ->
            onScanSuccess(text)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Struk", color = OnSecondary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = OnSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Show selected image if any
            if (uiState.selectedImageUri != null) {
                AsyncImage(
                    model = uiState.selectedImageUri,
                    contentDescription = "Selected image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isProcessing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Secondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "AI sedang membaca struk...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.processSelectedImage(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Secondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Proses Gambar")
                    }
                }
            } else {
                // Show camera/gallery options
                Text(
                    text = "Ambil foto struk atau pilih dari galeri",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Camera Button
                Button(
                    onClick = { /* Would launch camera */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Ambil Foto")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gallery Button
                SecondaryButtonComponent(
                    text = "Pilih dari Galeri",
                    onClick = { galleryLauncher.launch("image/*") },
                    icon = Icons.Default.Image,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error message
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}