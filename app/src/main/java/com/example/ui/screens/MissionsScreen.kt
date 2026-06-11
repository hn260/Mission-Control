package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MissionEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricRow
import com.example.ui.components.NeonDivider
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun MissionsScreen(
    missions: List<List<MissionEntity>> = emptyList(), // Fallback list
    allMissions: List<MissionEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLaunchMission: (title: String, desc: String, priority: String, objective: String, tags: String, fullSpec: String?, voiceText: String?) -> Unit,
    onDeployTemplate: (String) -> Unit,
    selectedId: Int?,
    onSelectId: (Int?) -> Unit,
    isVoiceListening: Boolean,
    voiceOutputText: String,
    onTriggerVoice: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredMissions = if (searchQuery.isBlank()) {
        allMissions
    } else {
        allMissions.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.tags.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PrimaryCyan,
                contentColor = Color.Black,
                modifier = Modifier
                    .testTag("launch_mission_fab")
                    .padding(bottom = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Launch Mission")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = "TACTICAL EXECUTION", style = Typography.labelSmall, color = PrimaryCyan)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "MISSION BOARD", style = Typography.displayMedium, color = OffWhite)
                }
            }

            // Quick Search Section
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field_missions"),
                    placeholder = { Text("Search missions, tags, priorities...", color = SpaceMuted, style = Typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = SpaceMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = SpaceMuted)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = PrimaryCyan.copy(alpha = 0.2f),
                        focusedContainerColor = GlassCardColor.copy(alpha = 0.3f),
                        unfocusedContainerColor = GlassCardColor.copy(alpha = 0.2f),
                        focusedLabelColor = PrimaryCyan,
                        unfocusedLabelColor = SpaceMuted,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = SpaceGlow
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Quick Deploy Templates Core
            item {
                Text(text = "LAUNCH FROM STRATEGIC TEMPLATE", style = Typography.labelLarge, color = SpaceGlow)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TemplateCompactCard(name = "Full Stack SaaS", desc = "React + Go Router", onClick = { onDeployTemplate("Full Stack SaaS") })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TemplateCompactCard(name = "Ω-OS Kernel", desc = "Rust Microkernel", onClick = { onDeployTemplate("Ω-OS Microkernel Core") })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TemplateCompactCard(name = "AI Agent CRM", desc = "Python Langchain", onClick = { onDeployTemplate("AI Agent CRM Swarm") })
                    }
                }
            }

            // Missions Heading
            item {
                Text(
                    text = "SPECIFICATION DIRECTIVES (${filteredMissions.size})",
                    style = Typography.labelLarge,
                    color = SpaceGlow
                )
            }

            // Render Mission lists
            if (filteredMissions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GlassCardColor.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No scheduling missions match constraints.", style = Typography.bodyMedium, color = SpaceMuted)
                        }
                    }
                }
            } else {
                items(filteredMissions) { m ->
                    MissionsListItem(
                        mission = m,
                        isExpanded = selectedId == m.id,
                        onClick = {
                            if (selectedId == m.id) onSelectId(null) else onSelectId(m.id)
                        }
                    )
                }
            }
        }
    }

    // Launch Custom Mission Dialog
    if (showCreateDialog) {
        var missionTitle by remember { mutableStateOf("") }
        var missionDesc by remember { mutableStateOf("") }
        var missionObjective by remember { mutableStateOf("") }
        var missionPriority by remember { mutableStateOf("HIGH") }
        var missionTags by remember { mutableStateOf("") }

        // Expandable specifications tab
        var activeInputMode by remember { mutableStateOf("text") } // "text", "voice", "specs"
        var specPayload by remember { mutableStateOf<String?>(null) } // omega-os file spec

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SurfaceSlate,
            title = {
                Text("DEPLOY NEW TACTICAL DIRECTIVE", style = Typography.headlineMedium, color = PrimaryCyan)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = missionTitle,
                            onValueChange = { missionTitle = it },
                            label = { Text("Directive Name", color = SpaceMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryCyan,
                                unfocusedBorderColor = SpaceMuted,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = SpaceGlow
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = missionObjective,
                            onValueChange = { missionObjective = it },
                            label = { Text("Primary Objective Goals", color = SpaceMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryCyan,
                                unfocusedBorderColor = SpaceMuted,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = SpaceGlow
                            )
                        )
                    }

                    item {
                        // Priority Selector
                        Text("Missions Risk Level Priority:", style = Typography.labelSmall, color = SpaceMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("LOW", "MEDIUM", "HIGH", "CRITICAL").forEach { priority ->
                                val active = missionPriority == priority
                                val color = when (priority) {
                                    "LOW" -> StatusGreen
                                    "MEDIUM" -> SpaceGlow
                                    "HIGH" -> StatusAmber
                                    else -> StatusRed
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (active) color else color.copy(alpha = 0.25f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .background(if (active) color.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { missionPriority = priority }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = priority, color = if (active) color else SpaceMuted, style = Typography.labelSmall.copy(fontSize = 9.sp))
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = missionTags,
                            onValueChange = { missionTags = it },
                            label = { Text("Aesthetic Tags (Comma separated)", color = SpaceMuted) },
                            placeholder = { Text("e.g. Go, REST, DB", color = SpaceMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryCyan,
                                unfocusedBorderColor = SpaceMuted,
                                focusedTextColor = OffWhite,
                                unfocusedTextColor = SpaceGlow
                            )
                        )
                    }

                    item {
                        // Input Selector Tabs
                        TabRow(
                            selectedTabIndex = when (activeInputMode) {
                                "text" -> 0
                                "specs" -> 1
                                else -> 2
                            },
                            contentColor = PrimaryCyan,
                            containerColor = Color.Transparent,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeInputMode == "text") 0 else if (activeInputMode == "specs") 1 else 2]),
                                    color = PrimaryCyan
                                )
                            }
                        ) {
                            Tab(selected = activeInputMode == "text", onClick = { activeInputMode = "text" }) {
                                Text("PROMPT", modifier = Modifier.padding(8.dp), style = Typography.labelSmall)
                            }
                            Tab(selected = activeInputMode == "specs", onClick = { activeInputMode = "specs" }) {
                                Text("Ω_SPECIFICATION", modifier = Modifier.padding(8.dp), style = Typography.labelSmall)
                            }
                            Tab(selected = activeInputMode == "voice", onClick = { activeInputMode = "voice" }) {
                                Text("SAY_CONTROL", modifier = Modifier.padding(8.dp), style = Typography.labelSmall)
                            }
                        }
                    }

                    item {
                        when (activeInputMode) {
                            "text" -> {
                                OutlinedTextField(
                                    value = missionDesc,
                                    onValueChange = { missionDesc = it },
                                    label = { Text("Instructions Prompt Script", color = SpaceMuted) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryCyan,
                                        unfocusedBorderColor = SpaceMuted,
                                        focusedTextColor = OffWhite,
                                        unfocusedTextColor = SpaceGlow
                                    )
                                )
                            }
                            "specs" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, PrimaryCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .background(GlassCardColor.copy(alpha = 0.1f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        "Multi-thousand word engineering specifications simulator (e.g. Ω-OS full compliance bundle).",
                                        style = Typography.bodyMedium,
                                        color = SpaceGlow
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    if (specPayload == null) {
                                        Button(
                                            onClick = {
                                                specPayload = "PRODUCT_SPECIFICATIONIFICATION_BLUEPRINT_VER_4_ALL_COMPLIANT_MICROKERNEL_OS_STRUCTURE..."
                                                missionTitle = "Ω-OS Core Blueprint Implementation"
                                                missionObjective = "Decompose and execute microkernel scheduler structures with full telemetry feedback arrays."
                                                missionTags = "Rust,OS,SpaceOps"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan.copy(alpha = 0.15f), contentColor = PrimaryCyan),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CloudUpload, contentDescription = "Upload spec")
                                                Text("SIMULATE UPLOAD Ω-BLUEPRINT", style = Typography.labelSmall)
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("SUCCESS: spec_file_omega.md (3,412 words)", color = StatusGreen, style = Typography.labelSmall)
                                            IconButton(onClick = { specPayload = null }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete spec", tint = StatusRed)
                                            }
                                        }
                                    }
                                }
                            }
                            "voice" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .background(GlassCardColor.copy(alpha = 0.1f))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Hold to stream vocal coordinates to instructions. Example: 'Launch a new Go API Gateway with telemetry.'",
                                        style = Typography.bodyMedium,
                                        color = SpaceGlow
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = onTriggerVoice,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isVoiceListening) StatusRed else AccentPurple
                                        )
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isVoiceListening) Icons.Default.MicOff else Icons.Default.Mic,
                                                contentDescription = "Voice"
                                            )
                                            Text(
                                                text = if (isVoiceListening) "HALT SPEECH CAPTURE" else "ENGAGE SAY CONTROL",
                                                style = Typography.labelSmall
                                            )
                                        }
                                    }

                                    if (voiceOutputText.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "TRANSCRIPTION RECORDED:",
                                            style = Typography.labelSmall,
                                            color = SpaceMuted
                                        )
                                        Text(
                                            text = "\"$voiceOutputText\"",
                                            style = Typography.bodyMedium,
                                            color = PrimaryCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val specText = if (activeInputMode == "specs") specPayload else null
                        val vText = if (activeInputMode == "voice") voiceOutputText else null
                        onLaunchMission(missionTitle, missionDesc, missionPriority, missionObjective, missionTags, specText, vText)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = Color.Black)
                ) {
                    Text("LAUNCH DIRECTIVE", style = Typography.labelSmall)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = SpaceMuted, style = Typography.labelSmall)
                }
            }
        )
    }
}

@Composable
fun MissionsListItem(
    mission: MissionEntity,
    isExpanded: Boolean = false,
    onClick: () -> Unit
) {
    val progressPercent = Math.round(mission.progress * 100f)

    val borderSelected = if (isExpanded) PrimaryCyan.copy(alpha = 0.5f) else PrimaryCyan.copy(alpha = 0.15f)

    GlassCard(
        borderColor = borderSelected,
        onClick = onClick,
        testTag = "mission_card_${mission.id}"
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = mission.title, style = Typography.titleMedium, color = OffWhite)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Priority Level: ${mission.priority}", style = Typography.labelSmall, color = SpaceMuted)
                }

                // Status Block
                StatusBadge(status = mission.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = mission.description, style = Typography.bodyMedium, color = SpaceGlow)
            Spacer(modifier = Modifier.height(8.dp))

            // Progress percentage bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { mission.progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (mission.status == "Failed") StatusRed else PrimaryCyan,
                    trackColor = GlassCardColor
                )

                Text(
                    text = "$progressPercent%",
                    style = Typography.labelSmall,
                    color = if (mission.status == "Failed") StatusRed else PrimaryCyan
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            // Render dynamic tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                mission.tags.split(",").forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(PrimaryCyan.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, PrimaryCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = tag, color = PrimaryCyan, style = Typography.labelSmall.copy(fontSize = 8.sp))
                    }
                }
            }

            // Expanded Decomposition Tasks Tree
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    NeonDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "MISSION INTERACTIVE TASK TREE DECOMPOSITION", style = Typography.labelSmall, color = SpaceMuted)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tasks parsed from tasksJson
                    mission.tasksJson.split(",").forEachIndexed { index, task ->
                        val parts = task.split("|")
                        if (parts.size >= 3) {
                            val taskTitle = parts[0]
                            val taskStatus = parts[1]
                            val taskProgress = parts[2].toIntOrNull() ?: 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (taskStatus == "Completed" || taskProgress >= 100) Icons.Default.CheckCircle else Icons.Default.Pending,
                                        contentDescription = "Status icon",
                                        tint = if (taskStatus == "Completed" || taskProgress >= 100) StatusGreen else StatusAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(text = taskTitle, style = Typography.bodyMedium, color = OffWhite)
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (taskProgress >= 100) "DONE" else "$taskProgress%",
                                        style = Typography.labelSmall,
                                        color = if (taskProgress >= 100) StatusGreen else StatusAmber
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (taskProgress >= 100) StatusGreen else StatusAmber,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    MetricRow(label = "Directive Primary Goal", value = mission.objective, isMonospace = false)
                }
            }
        }
    }
}

@Composable
fun TemplateCompactCard(
    name: String,
    desc: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(1.dp, PrimaryCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .background(GlassCardColor.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            Text(text = name, style = Typography.titleMedium.copy(fontSize = 12.sp), color = PrimaryCyan)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, style = Typography.bodyMedium.copy(fontSize = 9.sp), color = SpaceMuted, maxLines = 1)
        }
    }
}
