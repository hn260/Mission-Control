package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MissionEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.SessionEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricRow
import com.example.ui.components.NeonDivider
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    sessions: List<SessionEntity>,
    missions: List<MissionEntity>,
    notifications: List<NotificationEntity>,
    onNavigateToTab: (String) -> Unit,
    onNavigateToSub: (String) -> Unit,
    onSelectSession: (String) -> Unit
) {
    // Current general agent state
    val overallStatus = when {
        sessions.any { it.status == "Waiting Approval" } -> "Waiting Approval"
        sessions.any { it.status == "Busy" } -> "Busy"
        sessions.any { it.status == "Error" } -> "Error"
        sessions.any { it.status == "Online" } -> "Online"
        else -> "Offline"
    }

    val activeSession = sessions.firstOrNull()
    val activeWorkspace = activeSession?.workspace ?: "N/A"
    val activeTask = activeSession?.activeTask ?: "No active routine detected"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Identity Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "MISSION CONTROL",
                        style = Typography.labelSmall,
                        color = PrimaryCyan
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "COMMANDER ALPHA",
                        style = Typography.displayMedium,
                        color = OffWhite
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(status = overallStatus)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ID: AGENT-4492-X",
                        style = Typography.labelSmall.copy(fontSize = 8.sp),
                        color = SpaceMuted
                    )
                }
            }
        }

        // Workspace Tactical Overview Card
        item {
            GlassCard(
                borderColor = PrimaryCyan.copy(alpha = 0.4f),
                testTag = "workspace_control_card"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT WORKSPACE SIGNAL",
                            style = Typography.labelMedium,
                            color = PrimaryCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "/host/workspace/$activeWorkspace",
                            style = Typography.titleMedium.copy(fontFamily = TechMonospace),
                            color = OffWhite
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToSub("settings") },
                        modifier = Modifier
                            .background(PrimaryCyan.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Workspace Settings",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                NeonDivider()
                Spacer(modifier = Modifier.height(12.dp))

                MetricRow(label = "Active Source Branch", value = "git: main", valueColor = StatusGreen)
                Spacer(modifier = Modifier.height(6.dp))
                MetricRow(label = "Command Agent Mode", value = "Autonomous Supervisor", valueColor = AccentPurple)
                Spacer(modifier = Modifier.height(6.dp))
                MetricRow(label = "Execution Pipeline Task", value = activeTask, isMonospace = false)
            }
        }

        // Telemetry Center Panel Callout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HARDWARE ENGINE TELEMETRY",
                    style = Typography.labelLarge,
                    color = SpaceGlow
                )
                TextButton(onClick = { onNavigateToTab("Telemetry") }) {
                    Text("FULL LOGS >", style = Typography.labelSmall, color = PrimaryCyan)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CPU Gauge Card
                Box(modifier = Modifier.weight(1f)) {
                    val cpuVal = activeSession?.cpuUsage ?: 0f
                    TelemetryMiniCard(
                        title = "CPU LOG",
                        metric = "${cpuVal}%",
                        color = PrimaryCyan,
                        progress = cpuVal / 100f
                    )
                }

                // RAM Gauge Card
                Box(modifier = Modifier.weight(1f)) {
                    val ramVal = activeSession?.ramUsage ?: 0f
                    TelemetryMiniCard(
                        title = "RAM STACK",
                        metric = "${ramVal}%",
                        color = AccentPurple,
                        progress = ramVal / 100f
                    )
                }
            }
        }

        // Secondary Telemetry Quick-Read Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SimpleStatCard(
                        label = "Pending Approvals",
                        value = "${sessions.count { it.status == "Waiting Approval" }} ACTION",
                        color = StatusAmber
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SimpleStatCard(
                        label = "Active Sessions",
                        value = "${sessions.count { it.status != "Offline" }} ACTIVE",
                        color = StatusGreen
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SimpleStatCard(
                        label = "Success Yield",
                        value = "94.2%",
                        color = PrimaryCyan
                    )
                }
            }
        }

        // Active Missions Callout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE LAUNCH MISSIONS",
                    style = Typography.labelLarge,
                    color = SpaceGlow
                )
                TextButton(onClick = { onNavigateToTab("Missions") }) {
                    Text("PLAN ROOM >", style = Typography.labelSmall, color = PrimaryCyan)
                }
            }
        }

        // Short scroll of active missions
        item {
            if (missions.none { it.status != "Completed" && it.status != "Failed" }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassCardColor.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No active engineering missions launched.", style = Typography.bodyMedium, color = SpaceMuted)
                    }
                }
            }
        }

        items(missions.filter { it.status != "Completed" && it.status != "Failed" }.take(2)) { m ->
            MissionsListItem(mission = m, onClick = {
                onNavigateToTab("Missions")
            })
        }

        // Chronicle Operations Feed
        item {
            Text(
                text = "CHRONICLE OPERATIONS FEED",
                style = Typography.labelLarge,
                color = SpaceGlow
            )
        }

        if (notifications.isEmpty()) {
            item {
                Text(
                    text = "No log data streamed in current session.",
                    style = Typography.bodyMedium,
                    color = SpaceMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        items(notifications.take(6)) { notif ->
            ActivityFeedItem(notif = notif)
        }
    }
}

@Composable
fun TelemetryMiniCard(
    title: String,
    metric: String,
    color: Color,
    progress: Float
) {
    GlassCard(borderColor = color.copy(alpha = 0.3f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, style = Typography.labelSmall, color = SpaceMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = metric, style = Typography.titleLarge.copy(fontFamily = TechMonospace), color = color)
            }

            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(32.dp)) {
                    drawCircle(
                        color = color.copy(alpha = 0.15f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleStatCard(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .background(GlassCardColor.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Column {
            Text(text = label, style = Typography.labelSmall, fontSize = 8.sp, color = SpaceMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = Typography.labelMedium, fontSize = 11.sp, color = color)
        }
    }
}

@Composable
fun ActivityFeedItem(notif: NotificationEntity) {
    val indicatorColor = when (notif.type) {
        "APPROVAL" -> StatusAmber
        "BUILD" -> PrimaryCyan
        "MISSION" -> StatusGreen
        "ERROR" -> StatusRed
        else -> SpaceMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tactical Bullet Point
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .background(indicatorColor, CircleShape)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notif.title,
                    style = Typography.titleMedium.copy(fontSize = 13.sp),
                    color = OffWhite
                )

                // Render mock relative ticker
                Text(
                    text = "LIVE FEED",
                    style = Typography.labelSmall.copy(fontSize = 9.sp),
                    color = indicatorColor
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notif.message,
                color = SpaceGlow,
                style = Typography.bodyMedium.copy(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(SpaceMuted.copy(alpha = 0.3f))
            )
        }
    }
}
