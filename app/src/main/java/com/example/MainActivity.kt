package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.network.ConnectionState
import com.example.ui.screens.*
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SpaceMuted
import com.example.viewmodel.MissionControlViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MissionControlViewModel = viewModel()

                // State extraction with Lifecycle awareness
                val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
                val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
                val selectedMissionId by viewModel.selectedMissionId.collectAsStateWithLifecycle()
                val selectedSessionId by viewModel.selectedSessionId.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val webSocketUrl by viewModel.webSocketUrlSetting.collectAsStateWithLifecycle()

                val cpuHistory by viewModel.cpuHistory.collectAsStateWithLifecycle()
                val ramHistory by viewModel.ramHistory.collectAsStateWithLifecycle()

                val isVoiceListening by viewModel.isVoiceListening.collectAsStateWithLifecycle()
                val voiceOutputText by viewModel.voiceOutputText.collectAsStateWithLifecycle()
                val screenshotIndex by viewModel.screenshotStreamIndex.collectAsStateWithLifecycle()

                val missions by viewModel.missions.collectAsStateWithLifecycle()
                val sessions by viewModel.sessions.collectAsStateWithLifecycle()
                val approvalItems by viewModel.approvalItems.collectAsStateWithLifecycle()
                val approvalRules by viewModel.approvalRules.collectAsStateWithLifecycle()
                val prompts by viewModel.prompts.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()

                val wsStateFlow by viewModel.webSocketState.collectAsStateWithLifecycle()
                val wsStateText = when (wsStateFlow) {
                    is ConnectionState.Disconnected -> "Disconnected"
                    is ConnectionState.Connecting -> "Connecting"
                    is ConnectionState.Connected -> "Connected"
                    is ConnectionState.Error -> "Error: ${(wsStateFlow as ConnectionState.Error).message}"
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground),
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkBackground,
                            contentColor = SpaceMuted,
                            tonalElevation = 10.dp,
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            val items = listOf(
                                Triple("Home", Icons.Default.Dashboard, "home_nav_button"),
                                Triple("Missions", Icons.Default.RocketLaunch, "missions_nav_button"),
                                Triple("Sessions", Icons.Default.Dns, "sessions_nav_button"),
                                Triple("Telemetry", Icons.Default.QueryStats, "telemetry_nav_button"),
                                Triple("More", Icons.Default.Apps, "more_nav_button")
                            )

                            items.forEach { (tabName, icon, tag) ->
                                val selected = activeTab == tabName
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.setActiveTab(tabName) },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = "$tabName tab",
                                            tint = if (selected) PrimaryCyan else SpaceMuted
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tabName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected) PrimaryCyan else SpaceMuted
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = PrimaryCyan.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag(tag)
                                )
                            }
                        }
                    },
                    containerColor = DarkBackground,
                    contentWindowInsets = WindowInsets.statusBars
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(DarkBackground)
                    ) {
                        when (activeTab) {
                            "Home" -> HomeScreen(
                                sessions = sessions,
                                missions = missions,
                                notifications = notifications,
                                onNavigateToTab = { viewModel.setActiveTab(it) },
                                onNavigateToSub = { viewModel.openScreen(it) },
                                onSelectSession = { viewModel.selectSession(it) }
                            )
                            "Missions" -> MissionsScreen(
                                allMissions = missions,
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.updateSearchQuery(it) },
                                onLaunchMission = { title, desc, priority, objective, tags, fullSpec, voice ->
                                    viewModel.createMission(title, desc, priority, objective, tags, fullSpec, voice)
                                },
                                onDeployTemplate = { viewModel.deployTemplate(it) },
                                selectedId = selectedMissionId,
                                onSelectId = { viewModel.selectMission(it) },
                                isVoiceListening = isVoiceListening,
                                voiceOutputText = voiceOutputText,
                                onTriggerVoice = { viewModel.triggerVoiceListening() }
                            )
                            "Sessions" -> SessionsScreen(
                                sessions = sessions,
                                prompts = prompts,
                                selectedSessionId = selectedSessionId,
                                onSelectSessionId = { viewModel.selectSession(it) },
                                onToggleStatus = { viewModel.toggleSessionStatus(it) },
                                onSendPrompt = { viewModel.sendPromptDirect(it) }
                            )
                            "Telemetry" -> TelemetryScreen(
                                cpuHistory = cpuHistory,
                                ramHistory = ramHistory
                            )
                            "More" -> MoreScreen(
                                activeScreen = activeScreen,
                                onOpenScreen = { viewModel.openScreen(it) },
                                approvalItems = approvalItems,
                                onApproveItem = { viewModel.approveItem(it) },
                                onRejectItem = { viewModel.rejectItem(it) },
                                approvalRules = approvalRules,
                                onAddRule = { pattern, action -> viewModel.addCustomRule(pattern, action) },
                                onDeleteRule = { viewModel.deleteCustomRule(rule = it) },
                                onDeployTemplate = { viewModel.deployTemplate(it) },
                                webSocketUrl = webSocketUrl,
                                onUpdateWebSocketUrl = { viewModel.updateWebSocketUrl(it) },
                                onToggleWebSocket = { viewModel.toggleWebSocketConnection() },
                                webSocketState = wsStateText,
                                screenshotIndex = screenshotIndex,
                                onEmergencyStop = { viewModel.emergencyStopAll() },
                                onClearAllLogs = {
                                    viewModel.clearNotifications()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
