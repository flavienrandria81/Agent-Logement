package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PropertyWithDetails
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Spacing
import kotlin.math.sqrt

@Composable
fun InteractiveMapCanvas(
    properties: List<PropertyWithDetails>,
    selectedPropertyId: Long?,
    onSelectProperty: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .background(Color(0xFFE2E8F0), RoundedCornerShape(CornerRadius.lg))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(properties) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height

                        properties.forEachIndexed { index, item ->
                            val x = when (index % 4) {
                                0 -> w * 0.28f
                                1 -> w * 0.72f
                                2 -> w * 0.45f
                                else -> w * 0.82f
                            }
                            val y = when (index % 4) {
                                0 -> h * 0.32f
                                1 -> h * 0.42f
                                2 -> h * 0.68f
                                else -> h * 0.22f
                            }

                            val distance = sqrt((tapOffset.x - x) * (tapOffset.x - x) + (tapOffset.y - y) * (tapOffset.y - y))
                            if (distance < 50f) {
                                onSelectProperty(item.property.id)
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // Stylized Road Grid Network
            val roadColor = Color(0xFFCBD5E1)
            val strokeRoad = Stroke(width = 8f)

            drawLine(roadColor, Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 14f)
            drawLine(roadColor, Offset(0f, h * 0.65f), Offset(w, h * 0.65f), strokeWidth = 12f)
            drawLine(roadColor, Offset(w * 0.3f, 0f), Offset(w * 0.3f, h), strokeWidth = 12f)
            drawLine(roadColor, Offset(w * 0.7f, 0f), Offset(w * 0.7f, h), strokeWidth = 14f)

            // Stylized River / Seine Curve
            val riverColor = Color(0xFFBAE6FD)
            val riverPath = Path().apply {
                moveTo(0f, h * 0.5f)
                cubicTo(w * 0.3f, h * 0.4f, w * 0.6f, h * 0.6f, w, h * 0.52f)
            }
            drawPath(riverPath, riverColor, style = Stroke(width = 24f))

            // Pins for each property
            properties.forEachIndexed { index, item ->
                val isSelected = item.property.id == selectedPropertyId
                val x = when (index % 4) {
                    0 -> w * 0.28f
                    1 -> w * 0.72f
                    2 -> w * 0.45f
                    else -> w * 0.82f
                }
                val y = when (index % 4) {
                    0 -> h * 0.32f
                    1 -> h * 0.42f
                    2 -> h * 0.68f
                    else -> h * 0.22f
                }

                // Outer halo if selected
                if (isSelected) {
                    drawCircle(
                        color = DesignColors.Primary.copy(alpha = 0.3f),
                        radius = 32f,
                        center = Offset(x, y)
                    )
                }

                // Pin circle
                drawCircle(
                    color = if (isSelected) DesignColors.Primary else DesignColors.Secondary,
                    radius = if (isSelected) 20f else 16f,
                    center = Offset(x, y)
                )

                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 7f else 5f,
                    center = Offset(x, y)
                )
            }
        }

        // Map Legend Pill
        Surface(
            shape = RoundedCornerShape(CornerRadius.pill),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.md)
        ) {
            Text(
                text = "Touchez un repère pour inspecter le logement",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
            )
        }
    }
}
