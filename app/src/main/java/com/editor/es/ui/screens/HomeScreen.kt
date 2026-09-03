package com.editor.es.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editor.es.R
import com.editor.es.ui.components.EditorEsButton
import com.editor.es.ui.components.EntranceItem
import com.editor.es.ui.dialogs.CreateProjectDialog
import com.editor.es.ui.dialogs.OpenProjectSheet
import com.editor.es.ui.navigation.EditorEsRoute
import com.editor.es.ui.theme.EditorEsPalette

private val LogoBrush = Brush.linearGradient(
    colors = listOf(EditorEsPalette.mint, EditorEsPalette.mint.copy(alpha = 0.45f))
)

@Composable
fun HomeScreen(onNavigate: (EditorEsRoute) -> Unit, onProjectCreated: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showOpenSheet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EntranceItem(visible = visible, delayMillis = 0) {
            EditorEsLogo(modifier = Modifier.size(88.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        EntranceItem(visible = visible, delayMillis = 90) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = EditorEsPalette.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        EntranceItem(visible = visible, delayMillis = 160) {
            Text(
                text = stringResource(R.string.home_tagline),
                fontSize = 14.sp,
                color = EditorEsPalette.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(56.dp))
        EntranceItem(visible = visible, delayMillis = 240) {
            EditorEsButton(
                primary = true,
                label = stringResource(R.string.create_project),
                iconRes = R.drawable.add,
                onClick = { showCreateDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        EntranceItem(visible = visible, delayMillis = 320) {
            EditorEsButton(
                label = stringResource(R.string.open_project),
                iconRes = R.drawable.outline_folder,
                onClick = { showOpenSheet = true }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        EntranceItem(visible = visible, delayMillis = 400) {
            EditorEsButton(
                label = stringResource(R.string.terminal),
                iconRes = R.drawable.terminal,
                onClick = { onNavigate(EditorEsRoute.Terminal) }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        EntranceItem(visible = visible, delayMillis = 480) {
            EditorEsButton(
                label = stringResource(R.string.desktop),
                iconRes = R.drawable.terminal,
                onClick = { onNavigate(EditorEsRoute.Desktop) }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        EntranceItem(visible = visible, delayMillis = 560) {
            EditorEsButton(
                label = stringResource(R.string.settings),
                iconRes = R.drawable.settings,
                onClick = { onNavigate(EditorEsRoute.Settings) }
            )
        }
    }
    if (showCreateDialog) {
        CreateProjectDialog(
            onClose = { showCreateDialog = false },
            onCreated = { path ->
                showCreateDialog = false
                onProjectCreated(path)
            }
        )
    }
    if (showOpenSheet) {
        OpenProjectSheet(
            onDismiss = { showOpenSheet = false },
            onOpen = { path ->
                showOpenSheet = false
                onProjectCreated(path)
            }
        )
    }
}

@Composable
private fun EditorEsLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.05f
        drawLine(brush = LogoBrush, start = Offset(size.width * 0.38f, size.height * 0.24f), end = Offset(size.width * 0.20f, size.height * 0.5f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(brush = LogoBrush, start = Offset(size.width * 0.20f, size.height * 0.5f), end = Offset(size.width * 0.38f, size.height * 0.76f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(brush = LogoBrush, start = Offset(size.width * 0.62f, size.height * 0.24f), end = Offset(size.width * 0.80f, size.height * 0.5f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(brush = LogoBrush, start = Offset(size.width * 0.80f, size.height * 0.5f), end = Offset(size.width * 0.62f, size.height * 0.76f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(brush = LogoBrush, start = Offset(size.width * 0.57f, size.height * 0.20f), end = Offset(size.width * 0.43f, size.height * 0.80f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    }
}
