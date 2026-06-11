package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import androidx.compose.ui.draw.clip
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TelemetryScreen(
    cpuHistory: List<Float>,
    ramHistory: List<Float>
) {
    val currentCpu = cpuHistory.lastOrNull() ?: 45f
    val currentRam = ramHistory.lastOrNull() ?: 55f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("telemetry_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Identity Header
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "SYSTEM METRICS", style = Typography.labelSmall, color = PrimaryCyan)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "TELEMETRY ENGINE", style = Typography.displayMedium, color = OffWhite)
            }
        }

        // Custom Live Line Chart Panel
        item {
            GlassCard(
                borderColor = PrimaryCyan.copy(alpha = 0.35f),
                testTag = "tactical_telemetry_chart_card"
            ) {
                Text(
                    text = "HARDWARE UTILIATION TIMELINE (LINE CHART)",
                    style = Typography.labelMedium,
                    color = PrimaryCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                TelemetryLineChart(
                    cpuData = cpuHistory,
                    ramData = ramHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(GlassCardColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(0.5.dp, PrimaryCyan.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Chart Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp, 3.dp).background(PrimaryCyan))
                        Text("CPU ALLOC (${Math.round(currentCpu)}%)", style = Typography.labelSmall, color = PrimaryCyan)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp, 3.dp).background(AccentPurple))
                        Text("RAM ALLOC (${Math.round(currentRam)}%)", style = Typography.labelSmall, color = AccentPurple)
                    }
                }
            }
        }

        // Custom Live Radar Graph & Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Radar Chart Card
                Box(modifier = Modifier.weight(1.2f)) {
                    GlassCard(borderColor = AccentPurple.copy(alpha = 0.3f)) {
                        Text(
                            text = "SWARM RADAR COMPLIANCE",
                            style = Typography.labelSmall,
                            color = SpaceGlow
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TelemetryRadarChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }

                // Companion Metadata list
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMeterItem(title = "AGENT THROUGHPUT", percent = 0.88f, color = StatusGreen, label = "88 Ops/Sec")
                    StatMeterItem(title = "DISK OCCUPANCY", percent = 0.34f, color = PrimaryCyan, label = "34.2 GB Locked")
                    StatMeterItem(title = "APPROVAL CONVERGENCE", percent = 0.95f, color = StatusAmber, label = "95.4% Auto Ratio")
                }
            }
        }

        // Security Heatmap Block of Agent Executions
        item {
            GlassCard(borderColor = PrimaryCyan.copy(alpha = 0.2f)) {
                Text(
                    text = "TASK SCHEDULER ACTIVITY HEATMAP Grid",
                    style = Typography.labelMedium,
                    color = PrimaryCyan
                )
                Spacer(modifier = Modifier.height(12.dp))
                CalendarHeatmapGrid()
            }
        }
    }
}

@Composable
fun TelemetryLineChart(
    cpuData: List<Float>,
    ramData: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Draw tactical horizontal & vertical grids
        val gridLinesX = 8
        val gridLinesY = 4

        for (i in 1..gridLinesY) {
            val y = height * (i.toFloat() / (gridLinesY + 1))
            drawLine(
                color = PrimaryCyan.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        for (i in 1..gridLinesX) {
            val x = width * (i.toFloat() / (gridLinesX + 1))
            drawLine(
                color = PrimaryCyan.copy(alpha = 0.08f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
        }

        // Function helper to convert array to plotted paths
        fun buildPath(history: List<Float>, maxVal: Float): Path {
            val path = Path()
            if (history.isEmpty()) return path

            val pointsCount = history.size
            val stepX = width / (pointsCount - 1).coerceAtLeast(1)

            for (idx in history.indices) {
                val value = history[idx]
                // Scale coordinate offset y
                val y = height - (value / maxVal) * height * 0.85f - height * 0.05f
                val x = idx * stepX

                if (idx == 0) {
                    path.moveTo(x, y.coerceAtLeast(0f))
                } else {
                    path.lineTo(x, y.coerceAtLeast(0f))
                }
            }
            return path
        }

        // Compile CPU Path
        val cpuPath = buildPath(cpuData, 100f)
        drawPath(
            path = cpuPath,
            color = PrimaryCyan,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Compile RAM Path
        val ramPath = buildPath(ramData, 100f)
        drawPath(
            path = ramPath,
            color = AccentPurple,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

@Composable
fun TelemetryRadarChart(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = (size.height / 2f) * 0.85f

        // Draw Pentagon Grids (3 levels)
        val pentagonLevels = 3
        val corners = 5
        val angles = List(corners) { (it * 2 * Math.PI / corners - Math.PI / 2) }

        for (lvl in 1..pentagonLevels) {
            val r = maxRadius * (lvl.toFloat() / pentagonLevels)
            val path = Path()
            for (i in 0 until corners) {
                val x = centerX + r * cos(angles[i]).toFloat()
                val y = centerY + r * sin(angles[i]).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(
                path = path,
                color = AccentPurple.copy(alpha = 0.15f * lvl),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw radial grid axes
        for (i in 0 until corners) {
            val endX = centerX + maxRadius * cos(angles[i]).toFloat()
            val endY = centerY + maxRadius * sin(angles[i]).toFloat()
            drawLine(
                color = AccentPurple.copy(alpha = 0.2f),
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Plotted telemetry properties: Throughput, Memory, CPU, Safe compilations, Disk
        val telemetryStats = listOf(0.75f, 0.45f, 0.65f, 0.85f, 0.55f)
        val statPath = Path()
        for (i in 0 until corners) {
            val r = maxRadius * telemetryStats[i]
            val x = centerX + r * cos(angles[i]).toFloat()
            val y = centerY + r * sin(angles[i]).toFloat()
            if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
        }
        statPath.close()

        // Fill plotted pentagon
        drawPath(
            path = statPath,
            brush = Brush.radialGradient(
                colors = listOf(PrimaryCyan.copy(alpha = 0.5f), AccentPurple.copy(alpha = 0.15f)),
                center = Offset(centerX, centerY),
                radius = maxRadius
            )
        )
        drawPath(
            path = statPath,
            color = PrimaryCyan,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun StatMeterItem(
    title: String,
    percent: Float,
    label: String,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = title, style = Typography.labelSmall, color = SpaceMuted, fontSize = 9.sp)
            Text(text = label, style = Typography.labelMedium.copy(fontFamily = TechMonospace), color = color, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(GlassCardColor, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun CalendarHeatmapGrid() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val rows = 5
        val cols = 12

        // Day of Week indicator block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (col in 0 until cols) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "W${col + 1}",
                        style = Typography.labelSmall.copy(fontSize = 7.sp),
                        color = SpaceMuted
                    )
                }
            }
        }

        // Draw Grid blocks
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (c in 0 until cols) {
                    // Random activity level color assignment
                    val intensity = (r * 13 + c * 7) % 4
                    val color = when (intensity) {
                        0 -> GlassCardColor.copy(alpha = 0.2f)
                        1 -> PrimaryCyan.copy(alpha = 0.2f)
                        2 -> PrimaryCyan.copy(alpha = 0.5f)
                        else -> PrimaryCyan
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}
