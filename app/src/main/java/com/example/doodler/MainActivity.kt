package com.example.doodler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoodleScreen()
        }
    }
}

@Composable
fun DoodleScreen() {
    var brushColor by remember { mutableStateOf(Color.Black) }
    var brushSize by remember { mutableStateOf(8f) }
    var backgroundColor by remember { mutableStateOf(Color.White) }

    // Paths state management
    val paths = remember { mutableStateListOf<DoodlePath>() }
    val savedPaths = remember { mutableStateListOf<DoodlePath>() }
    val currentPath = remember { mutableStateOf<DoodlePath?>(null) }

    Scaffold(
        topBar = {
            ToolPanel(
                brushColor = brushColor,
                onColorChange = { brushColor = it },
                brushSize = brushSize,
                onBrushSizeChange = { brushSize = it },
                onClearCanvas = {
                    paths.clear() // Clears unsaved paths only
                },
                onClearAll = {
                    paths.clear()
                    savedPaths.clear() // Clears everything
                },
                onSaveCanvas = {
                    savedPaths.addAll(paths) // Save all unsaved paths
                    paths.clear() // Move to saved paths
                },
                backgroundColor = backgroundColor,
                onBackgroundColorChange = { backgroundColor = it }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DoodleCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                paths = paths,
                savedPaths = savedPaths,
                currentPath = currentPath,
                brushColor = brushColor,
                brushSize = brushSize
            )
        }
    }
}

@Composable
fun ToolPanel(
    brushColor: Color,
    onColorChange: (Color) -> Unit,
    brushSize: Float,
    onBrushSizeChange: (Float) -> Unit,
    onClearCanvas: () -> Unit,
    onClearAll: () -> Unit,
    onSaveCanvas: () -> Unit,
    backgroundColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp)
    ) {
        Text(
            text = "Drawing Tools",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Brush Color")
                Row {
                    listOf(Color.Black, Color.Red, Color.Green, Color.Blue, Color.Yellow).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                .clickable { onColorChange(color) }
                                .padding(4.dp)
                        )
                    }
                }
            }

            Column {
                Text("Brush Size: ${brushSize.toInt()} px")
                Slider(
                    value = brushSize,
                    onValueChange = onBrushSizeChange,
                    valueRange = 4f..50f,
                    modifier = Modifier.width(200.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Background Color")
                Row {
                    listOf(Color.White, Color.LightGray, Color.Yellow, Color.Cyan).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                .clickable { onBackgroundColorChange(color) }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onSaveCanvas) {
                Text("Save")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onClearCanvas) {
                Text("Clear")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onClearAll) {
                Text("Clear All")
            }
        }
    }
}



@Composable
fun DoodleCanvas(
    modifier: Modifier,
    paths: MutableList<DoodlePath>,
    savedPaths: List<DoodlePath>,
    currentPath: MutableState<DoodlePath?>,
    brushColor: Color,
    brushSize: Float
) {
    Canvas(
        modifier = modifier.pointerInput(brushColor, brushSize) {
            detectDragGestures(
                onDragStart = { offset ->
                    val newPath = DoodlePath(
                        path = Path().apply { moveTo(offset.x, offset.y) },
                        color = brushColor,
                        size = brushSize
                    )
                    currentPath.value = newPath
                    paths.add(newPath)
                },
                onDrag = { change, _ ->
                    change.consume()
                    currentPath.value?.path?.lineTo(change.position.x, change.position.y)
                },
                onDragEnd = {
                    currentPath.value = null
                }
            )
        }
    ) {
        // Draw saved paths
        savedPaths.forEach { doodlePath ->
            drawPath(
                path = doodlePath.path,
                color = doodlePath.color,
                style = Stroke(width = doodlePath.size)
            )
        }

        // Draw current unsaved paths
        paths.forEach { doodlePath ->
            drawPath(
                path = doodlePath.path,
                color = doodlePath.color,
                style = Stroke(width = doodlePath.size)
            )
        }

        // Draw the path being actively drawn
        currentPath.value?.let { doodlePath ->
            drawPath(
                path = doodlePath.path,
                color = doodlePath.color,
                style = Stroke(width = doodlePath.size)
            )
        }
    }
}

data class DoodlePath(
    val path: Path,
    val color: Color,
    val size: Float
)

@Preview(showBackground = true)
@Composable
fun PreviewDoodleScreen() {
    DoodleScreen()
}
