package com.example.whiskytastingjournal.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun TastingWheel(
    attributes: Map<String, Float>,
    maxValue: Float = 10f,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val strokeColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface
    val dotColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(attributes) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val labels = attributes.keys.toList()
    val values = attributes.values.toList()
    val count = labels.size

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(24.dp)
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = min(centerX, centerY) * 0.75f
        val angleStep = 2 * PI.toFloat() / count
        // Start from top (negative Y axis)
        val startAngle = -PI.toFloat() / 2f

        // Draw grid rings
        val ringCount = 5
        for (ring in 1..ringCount) {
            val ringRadius = radius * ring / ringCount
            val ringPath = Path()
            for (i in 0 until count) {
                val angle = startAngle + i * angleStep
                val x = centerX + ringRadius * cos(angle)
                val y = centerY + ringRadius * sin(angle)
                if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = gridColor,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw axis lines
        for (i in 0 until count) {
            val angle = startAngle + i * angleStep
            val endX = centerX + radius * cos(angle)
            val endY = centerY + radius * sin(angle)
            drawLine(
                color = gridColor,
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw data polygon (animated)
        val progress = animationProgress.value
        val dataPath = Path()
        val dataPoints = mutableListOf<Offset>()

        for (i in 0 until count) {
            val normalizedValue = (values[i] / maxValue).coerceIn(0f, 1f) * progress
            val angle = startAngle + i * angleStep
            val x = centerX + radius * normalizedValue * cos(angle)
            val y = centerY + radius * normalizedValue * sin(angle)
            dataPoints.add(Offset(x, y))
            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        // Fill
        drawPath(path = dataPath, color = fillColor)

        // Stroke
        drawPath(
            path = dataPath,
            color = strokeColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Data point dots
        dataPoints.forEach { point ->
            drawCircle(
                color = dotColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }

        // Labels
        for (i in 0 until count) {
            val angle = startAngle + i * angleStep
            val labelRadius = radius + 20.dp.toPx()
            val x = centerX + labelRadius * cos(angle)
            val y = centerY + labelRadius * sin(angle)
            drawLabel(
                text = labels[i],
                x = x,
                y = y,
                color = labelColor
            )
        }
    }
}

private fun DrawScope.drawLabel(
    text: String,
    x: Float,
    y: Float,
    color: Color
) {
    val paint = android.graphics.Paint().apply {
        this.color = color.hashCode()
        textSize = 12.dp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
        text,
        x,
        y + paint.textSize / 3f,
        paint
    )
}
