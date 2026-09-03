package com.editor.es.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.editor.es.desktop.DesktopManager
import com.editor.es.ui.theme.EditorEsPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("idle") }
    val logs = remember { mutableStateListOf<String>() }
    var canOpen by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Linux Desktop", color = EditorEsPalette.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = EditorEsPalette.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EditorEsPalette.abyss)
            )
        },
        containerColor = EditorEsPalette.abyss
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (DesktopManager.isRunning()) return@Button
                        status = "starting..."
                        canOpen = false
                        logs.clear()
                        DesktopManager.startDesktop(
                            context = context,
                            onLine = { line ->
                                if (line.isNotBlank()) logs.add(line)
                            },
                            onDone = { success, error ->
                                if (success) {
                                    status = "running"
                                    canOpen = true
                                } else {
                                    status = "failed: ${error ?: "unknown"}"
                                }
                            }
                        )
                    },
                    enabled = !DesktopManager.isRunning() && status != "starting...",
                    colors = ButtonDefaults.buttonColors(containerColor = EditorEsPalette.mint)
                ) {
                    Icon(Icons.Filled.PlayArrow, null)
                    Text("Start", modifier = Modifier.padding(start = 4.dp))
                }
                Button(
                    onClick = {
                        DesktopManager.stopDesktop(context)
                        status = "stopped"
                        canOpen = false
                    },
                    enabled = DesktopManager.isRunning(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Stop, null)
                    Text("Stop", modifier = Modifier.padding(start = 4.dp))
                }
                Button(
                    onClick = { DesktopManager.openViewer(context) },
                    enabled = canOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = EditorEsPalette.mint)
                ) {
                    Icon(Icons.Filled.DesktopWindows, null)
                    Text("Open Desktop", modifier = Modifier.padding(start = 4.dp))
                }
            }
            Text(
                text = "Status: $status",
                color = EditorEsPalette.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0A1F26), MaterialTheme.shapes.medium)
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                logs.forEach { line ->
                    Text(
                        text = line,
                        color = EditorEsPalette.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}
