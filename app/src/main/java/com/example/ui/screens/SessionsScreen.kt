package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PromptEntity
import com.example.data.model.SessionEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricRow
import com.example.ui.components.NeonDivider
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun SessionsScreen(
    sessions: List<SessionEntity>,
    prompts: List<PromptEntity>,
    selectedSessionId: String?,
    onSelectSessionId: (String?) -> Unit,
    onToggleStatus: (SessionEntity) -> Unit,
    onSendPrompt: (String) -> Unit
) {
    var promptInputText by remember { mutableStateOf("") }
    val activeSessId = selectedSessionId ?: sessions.firstOrNull()?.sessionId ?: "SESS-OP-808"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sessions_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading Block
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "COMMUNICATION GRID", style = Typography.labelSmall, color = PrimaryCyan)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "AGENT NETWORK", style = Typography.displayMedium, color = OffWhite)
            }
        }

        // Horizontal Swarm Select Blocks
        item {
            Text(text = "SELECT ACTIVE WORKSPACE SWARM AGENT", style = Typography.labelLarge, color = SpaceGlow)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                sessions.forEach { s ->
                    val isSelected = s.sessionId == activeSessId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                if (isSelected) PrimaryCyan else PrimaryCyan.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) PrimaryCyan.copy(alpha = 0.1f) else GlassCardColor.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectSessionId(s.sessionId) }
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = s.sessionId, style = Typography.labelSmall, color = if (isSelected) PrimaryCyan else SpaceGlow)
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (s.status == "Busy") StatusAmber else if (s.status == "Online") StatusGreen else StatusRed,
                                            CircleShape
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = s.agentName, style = Typography.titleMedium.copy(fontSize = 12.sp), color = OffWhite)
                            Text(text = s.workspace, style = Typography.bodyMedium.copy(fontSize = 10.sp), color = SpaceMuted)
                        }
                    }
                }
            }
        }

        // Active Session Controller Panel
        sessions.find { it.sessionId == activeSessId }?.let { activeSession ->
            item {
                GlassCard(
                    borderColor = PrimaryCyan.copy(alpha = 0.35f),
                    testTag = "session_controller_card"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "TACTICAL EXECUTION LAYER", style = Typography.labelSmall, color = SpaceMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "AGENT ${activeSession.agentName.uppercase()}",
                                style = Typography.titleLarge,
                                color = PrimaryCyan
                            )
                            Text(
                                text = "Current workspace directory: /${activeSession.workspace}",
                                style = Typography.bodyMedium.copy(fontFamily = TechMonospace),
                                color = SpaceGlow
                            )
                        }

                        StatusBadge(status = activeSession.status)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    NeonDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    MetricRow(label = "Active File Lock", value = activeSession.currentFile, valueColor = SpaceGlow)
                    Spacer(modifier = Modifier.height(6.dp))
                    MetricRow(label = "Active Process Routine", value = activeSession.activeTask, isMonospace = false)
                    Spacer(modifier = Modifier.height(6.dp))
                    MetricRow(label = "Runtime Duration Ticker", value = activeSession.runtime)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons Controls Array
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isOnlineOrBusy = activeSession.status == "Online" || activeSession.status == "Busy"
                        Button(
                            onClick = { onToggleStatus(activeSession) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeSession.status == "Busy") StatusAmber else StatusGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (activeSession.status == "Busy") Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Halt",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (activeSession.status == "Busy") "PAUSE" else "RESUME",
                                    style = Typography.labelSmall
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onToggleStatus(activeSession.copy(status = "Offline")) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                            border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stop, contentDescription = "Terminate", modifier = Modifier.size(16.dp))
                                Text("TERMINATE", style = Typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Tactical Prompt Console Overlay
        item {
            Text(text = "CYBERNETIC CO-PILOT PROMPT CONSOLE", style = Typography.labelLarge, color = SpaceGlow)
        }

        item {
            GlassCard(
                borderColor = PrimaryCyan.copy(alpha = 0.25f),
                testTag = "prompt_console_input_card"
            ) {
                Text(
                    text = "Transmit raw instruction payload straight to the compiled execution stream:",
                    style = Typography.bodyMedium,
                    color = SpaceGlow
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = promptInputText,
                    onValueChange = { promptInputText = it },
                    placeholder = { Text("e.g. Implement rate limiting token bucket...", style = Typography.bodyMedium, color = SpaceMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_terminal_field"),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = SpaceMuted,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = SpaceGlow
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (promptInputText.isNotBlank()) {
                                    onSendPrompt(promptInputText)
                                    promptInputText = ""
                                }
                            },
                            modifier = Modifier
                                .background(PrimaryCyan, CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send prompt", tint = Color.Black, modifier = Modifier.size(14.dp))
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                NeonDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PROMPT CONSOLE TRANSMISSION REGISTRY",
                    style = Typography.labelSmall,
                    color = SpaceMuted
                )

                val filteredPrompts = prompts.filter { it.sessionId == activeSessId }
                if (filteredPrompts.isEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No prompts transmitted from this node yet.",
                        style = Typography.bodyMedium,
                        color = SpaceMuted
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    filteredPrompts.take(3).forEach { p ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "OP: ${p.prompt}", style = Typography.labelMedium, color = OffWhite)
                                Text(
                                    text = p.status,
                                    style = Typography.labelSmall,
                                    color = if (p.status == "SUCCESS") StatusGreen else StatusAmber
                                )
                            }
                            Text(text = "RSP: ${p.response}", style = Typography.bodyMedium, color = SpaceGlow)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SpaceMuted.copy(alpha = 0.2f)))
                        }
                    }
                }
            }
        }
    }
}
