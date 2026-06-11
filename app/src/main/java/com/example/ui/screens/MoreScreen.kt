package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApprovalItemEntity
import com.example.data.model.ApprovalRuleEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricRow
import com.example.ui.components.NeonDivider
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

// Operational module metadata
data class ControlModule(
    val id: String,
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun MoreScreen(
    activeScreen: String?,
    onOpenScreen: (String?) -> Unit,
    approvalItems: List<ApprovalItemEntity>,
    onApproveItem: (ApprovalItemEntity) -> Unit,
    onRejectItem: (ApprovalItemEntity) -> Unit,
    approvalRules: List<ApprovalRuleEntity>,
    onAddRule: (pattern: String, type: String) -> Unit,
    onDeleteRule: (ApprovalRuleEntity) -> Unit,
    onDeployTemplate: (String) -> Unit,
    webSocketUrl: String,
    onUpdateWebSocketUrl: (String) -> Unit,
    onToggleWebSocket: () -> Unit,
    webSocketState: String,
    screenshotIndex: Int,
    onEmergencyStop: () -> Unit,
    onClearAllLogs: () -> Unit
) {
    val modules = listOf(
        ControlModule("approvals", "Approvals Hub", "Process dangerous operations", Icons.Default.Security, StatusAmber),
        ControlModule("rules", "Policy Manager", "Configure security auto-approvals", Icons.Default.Rule, PrimaryCyan),
        ControlModule("files", "Diff Review & Live File", "Inspect and approve core source files", Icons.Default.Code, AccentPurple),
        ControlModule("architecture", "Architecture Graph", "Explore system dependency blue-nodes", Icons.Default.Hub, PrimaryCyan),
        ControlModule("screenshots", "Screenshot Streaming", "Watch live host terminal output", Icons.Default.Monitor, SecondaryBlue),
        ControlModule("templates", "Mission Templates", "Select preloaded project scopes", Icons.Default.RocketLaunch, StatusGreen),
        ControlModule("settings", "Workspace Settings", "Wipe logs & manage connection ports", Icons.Default.Settings, SpaceGlow),
        ControlModule("emergency", "EMERGENCY HALT", "INSTANT UNILATERAL AGENT CUT-OFF", Icons.Default.Bolt, StatusRed)
    )

    AnimatedContent(
        targetState = activeScreen,
        transitionSpec = {
            if (targetState == null) {
                (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
            } else {
                (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            }
        },
        label = "SubScreenNavigator"
    ) { screenId ->
        if (screenId == null) {
            // Main control module grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("more_screen_main_view"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = "OPERATIONAL ARRAYS", style = Typography.labelSmall, color = PrimaryCyan)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "TACTICAL MODULES", style = Typography.displayMedium, color = OffWhite)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(modules) { mod ->
                        ModuleGridCard(module = mod, onClick = { onOpenScreen(mod.id) })
                    }
                }
            }
        } else {
            // Sub-screen layout container
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground)
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onOpenScreen(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryCyan)
                        }
                        Text(
                            text = modules.find { it.id == screenId }?.name?.uppercase() ?: "TACTICAL VIEW",
                            style = Typography.titleLarge,
                            color = PrimaryCyan
                        )
                    }
                },
                containerColor = Color.Transparent
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(DarkBackground)
                ) {
                    when (screenId) {
                        "approvals" -> ApprovalsView(approvalItems, onApproveItem, onRejectItem)
                        "rules" -> RulesView(approvalRules, onAddRule, onDeleteRule)
                        "files" -> CodeReviewView()
                        "architecture" -> ArchitectureView()
                        "screenshots" -> ScreenshotsView(screenshotIndex)
                        "templates" -> TemplatesView(onDeployTemplate)
                        "settings" -> SettingsView(webSocketUrl, onUpdateWebSocketUrl, onToggleWebSocket, webSocketState, onClearAllLogs)
                        "emergency" -> EmergencyView(onEmergencyStop)
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleGridCard(
    module: ControlModule,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .border(1.dp, module.accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassCardColor.copy(alpha = 0.5f),
                        GlassCardColor.copy(alpha = 0.2f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .height(110.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = module.icon,
                contentDescription = module.name,
                tint = module.accent,
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = module.name,
                    style = Typography.titleMedium.copy(fontSize = 13.sp),
                    color = OffWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = module.desc,
                    style = Typography.bodyMedium.copy(fontSize = 9.sp),
                    color = SpaceMuted,
                    maxLines = 2
                )
            }
        }
    }
}

// Sub-view 1: Approvals View
@Composable
fun ApprovalsView(
    items: List<ApprovalItemEntity>,
    onApprove: (ApprovalItemEntity) -> Unit,
    onReject: (ApprovalItemEntity) -> Unit
) {
    val pending = items.filter { it.status == "PENDING" }
    val history = items.filter { it.status != "PENDING" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("COMMANDS AUTHORIZATION DESK", style = Typography.labelLarge, color = PrimaryCyan)
            Text("Review and authorize high-risk actions queued by agents", style = Typography.bodyMedium, color = SpaceMuted)
        }

        item {
            Text("AWAITING OPERATOR SIGNATURE (${pending.size})", style = Typography.labelSmall, color = StatusAmber)
        }

        if (pending.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassCardColor.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No pending action authorizations verified.", style = Typography.bodyMedium, color = SpaceMuted)
                    }
                }
            }
        } else {
            items(pending) { item ->
                GlassCard(
                    borderColor = if (item.riskLevel == "CRITICAL" || item.riskLevel == "HIGH") StatusRed.copy(alpha = 0.4f) else StatusAmber.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RISK ENVELOPE: ${item.riskLevel}", style = Typography.labelMedium, color = if (item.riskLevel == "CRITICAL") StatusRed else StatusAmber)
                        StatusBadge(status = item.status)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.command,
                        style = Typography.titleMedium.copy(fontFamily = TechMonospace, color = OffWhite)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "RATIONALE: ${item.reason}", style = Typography.bodyMedium, color = SpaceGlow)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onApprove(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SIGN & EXPEDITE", style = Typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { onReject(item) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                            border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("REJECT ACTION", style = Typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Historic Signed Logs
        item {
            Text("COMPLETED REGISTRIES HISTORY", style = Typography.labelSmall, color = SpaceMuted)
        }

        items(history) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, SpaceMuted.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .background(GlassCardColor.copy(alpha = 0.1f))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = item.command, style = Typography.labelMedium.copy(fontSize = 11.sp), color = SpaceGlow)
                        Text(text = "Expedited: ${item.reason}", style = Typography.bodyMedium.copy(fontSize = 10.sp), color = SpaceMuted)
                    }
                    StatusBadge(status = item.status)
                }
            }
        }
    }
}

// Sub-view 2: Rules View
@Composable
fun RulesView(
    rules: List<ApprovalRuleEntity>,
    onAddRule: (String, String) -> Unit,
    onDeleteRule: (ApprovalRuleEntity) -> Unit
) {
    var keywordPattern by remember { mutableStateOf("") }
    var actionTypeSelection by remember { mutableStateOf("AUTO_APPROVE") } // AUTO_APPROVE, REQUIRE_APPROVAL

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("SECURITY RULES ARCHITECT", style = Typography.labelLarge, color = PrimaryCyan)
            Text("Map automated action policies based on code scopes and command keywords", style = Typography.bodyMedium, color = SpaceMuted)
        }

        // Add Rule Form Box
        item {
            GlassCard(borderColor = PrimaryCyan.copy(alpha = 0.25f)) {
                Text("ADD TACTICAL REGULATORY FILTER", style = Typography.labelSmall, color = SpaceGlow)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keywordPattern,
                    onValueChange = { keywordPattern = it },
                    label = { Text("Command Keyword / Scope Match String", color = SpaceMuted) },
                    placeholder = { Text("e.g. npm install, go mod", color = SpaceMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = SpaceMuted,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = SpaceGlow
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Policy Enforcement Class:", style = Typography.labelSmall, color = SpaceMuted)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("AUTO_APPROVE", "REQUIRE_APPROVAL").forEach { action ->
                        val selected = action == actionTypeSelection
                        val color = if (action == "AUTO_APPROVE") StatusGreen else StatusAmber
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, if (selected) color else color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .background(if (selected) color.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { actionTypeSelection = action }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = action, color = if (selected) color else SpaceMuted, style = Typography.labelSmall.copy(fontSize = 9.sp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (keywordPattern.isNotBlank()) {
                            onAddRule(keywordPattern, actionTypeSelection)
                            keywordPattern = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("REGISTER SAFETY RULE", style = Typography.labelSmall)
                }
            }
        }

        // Rules lists
        item {
            Text("ACTIVE SECURITY POLICIES (${rules.size})", style = Typography.labelSmall, color = SpaceMuted)
        }

        items(rules) { rule ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, PrimaryCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .background(GlassCardColor.copy(alpha = 0.2f))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Match: '${rule.pattern}'", style = Typography.titleMedium.copy(fontSize = 13.sp), color = OffWhite)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Action: ${rule.actionType}", style = Typography.bodyMedium.copy(fontSize = 11.sp), color = SpaceMuted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(status = rule.actionType)
                        IconButton(onClick = { onDeleteRule(rule) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// Sub-view 3: Diff & Live File
@Composable
fun CodeReviewView() {
    var selectTab by remember { mutableStateOf("diff") } // "diff", "live"

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabRow(
            selectedTabIndex = if (selectTab == "diff") 0 else 1,
            contentColor = PrimaryCyan,
            containerColor = Color.Transparent
        ) {
            Tab(selected = selectTab == "diff", onClick = { selectTab = "diff" }) {
                Text("WORKSPACE DIFF REVIEW", modifier = Modifier.padding(10.dp), style = Typography.labelSmall)
            }
            Tab(selected = selectTab == "live", onClick = { selectTab = "live" }) {
                Text("LIVE CODE MONITOR VIEW", modifier = Modifier.padding(10.dp), style = Typography.labelSmall)
            }
        }

        if (selectTab == "diff") {
            // Mock Side by Side Inline Diff view
            GlassCard(borderColor = AccentPurple.copy(alpha = 0.3f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("git diff: kernel/sched.rs", style = Typography.titleMedium, color = OffWhite)
                        Text("SESS-OP-808 - Active workspace changes", style = Typography.bodyMedium, color = SpaceMuted)
                    }
                    Box(modifier = Modifier.background(StatusAmber.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("STAGED", color = StatusAmber, style = Typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                NeonDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Render fake colored logs diff
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF020617))
                        .padding(8.dp)
                ) {
                    Text("@@ -14,8 +14,9 @@ pub fn schedule_tasks() {", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = SpaceMuted)
                    Text("     let active_queue = get_global_scheduler_array();", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = OffWhite)
                    Text("-    if active_queue.is_empty() {", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = StatusRed)
                    Text("-        return; // wait clock", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = StatusRed)
                    Text("+    if active_queue.len() == 0 {", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = StatusGreen)
                    Text("+        // Spinlock optimized wait block", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = StatusGreen)
                    Text("+        core::hint::spin_loop();", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = StatusGreen)
                    Text("+        return;", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = StatusGreen)
                    Text("     }", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = OffWhite)
                    Text("     process_queue(active_queue);", style = Typography.bodyMedium.copy(fontFamily = TechMonospace), color = OffWhite)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("APPROVE DIFF", style = Typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = {},
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                        border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text("REVISE CODE", style = Typography.labelSmall)
                    }
                }
            }
        } else {
            // Live code viewport read-only monitor
            GlassCard(borderColor = PrimaryCyan.copy(alpha = 0.3f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Lock: kernel/sched.rs", style = Typography.titleMedium, color = OffWhite)
                        Text("Agent: Genesis Alpha - Mode: READ-ONLY MONITOR", style = Typography.bodyMedium, color = SpaceMuted)
                    }
                    Box(modifier = Modifier.background(PrimaryCyan.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("LIVE SYNCING", color = PrimaryCyan, style = Typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                MetricRow(label = "Cursor Column Lock:", value = "Line 18, Col 9", valueColor = PrimaryCyan)
                MetricRow(label = "Active Subroutine Scope:", value = "fn schedule_tasks()", valueColor = AccentPurple)

                Spacer(modifier = Modifier.height(10.dp))

                // Viewport container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF020617))
                        .padding(8.dp)
                ) {
                    val codeLines = listOf(
                        "1: #![no_std]",
                        "2: use core::sync::atomic::{AtomicBool, Ordering};",
                        "3: ",
                        "4: pub struct ThreadQueue {",
                        "5:     lock: AtomicBool,",
                        "6:     threads: [Option<Thread>; 16],",
                        "7: }",
                        "8: ",
                        "9: impl ThreadQueue {",
                        "10:    pub fn acquire_lock(&self) {",
                        "11:        while self.lock.swap(true, Ordering::Acquire) {",
                        "12:            core::hint::spin_loop(); // OPTIMIZED RUNNING STATE",
                        "13:        }",
                        "14:    }",
                        "15: }"
                    )
                    codeLines.forEach { line ->
                        Text(
                            text = line,
                            style = Typography.bodyMedium.copy(fontFamily = TechMonospace),
                            color = if (line.contains("OPTIMIZED")) PrimaryCyan else SpaceGlow
                        )
                    }
                }
            }
        }
    }
}

// Sub-view 4: Architecture Maps
@Composable
fun ArchitectureView() {
    var selectedNode by remember { mutableStateOf("Gateway") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text("SWARM DEPLOYMENT DEPENDENCY MAP", style = Typography.labelLarge, color = PrimaryCyan)
            Text("Tap system nodes to inspect remote modular couplings and data queues", style = Typography.bodyMedium, color = SpaceMuted)
        }

        // Canvas container for interactive nodes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, PrimaryCyan.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .background(Color(0xFF030712))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Draw connecting coupling networks
                drawLine(color = PrimaryCyan.copy(alpha = 0.3f), start = Offset(cx - 100f, cy), end = Offset(cx, cy - 60f), strokeWidth = 1.5.dp.toPx())
                drawLine(color = PrimaryCyan.copy(alpha = 0.3f), start = Offset(cx + 100f, cy), end = Offset(cx, cy - 60f), strokeWidth = 1.5.dp.toPx())
                drawLine(color = PrimaryCyan.copy(alpha = 0.3f), start = Offset(cx, cy - 60f), end = Offset(cx, cy + 60f), strokeWidth = 1.5.dp.toPx())

                // Render decorative radar rings
                drawCircle(color = AccentPurple.copy(alpha = 0.05f), center = Offset(cx, cy), radius = 140f)
            }

            // Interactive Buttons Overlayed as Nodes
            NodeButton(text = "Gateway", x = 110, y = 100, selected = selectedNode == "Gateway", onClick = { selectedNode = "Gateway" })
            NodeButton(text = "Allocator", x = 210, y = 40, selected = selectedNode == "Allocator", onClick = { selectedNode = "Allocator" })
            NodeButton(text = "Db Mesh", x = 310, y = 100, selected = selectedNode == "Db Mesh", onClick = { selectedNode = "Db Mesh" })
            NodeButton(text = "Telemetry", x = 210, y = 160, selected = selectedNode == "Telemetry", onClick = { selectedNode = "Telemetry" })
        }

        // Node detail readout card
        GlassCard(borderColor = PrimaryCyan.copy(alpha = 0.3f)) {
            val (title, coupling, logs, safety) = when (selectedNode) {
                "Gateway" -> Quadruple(
                    "AUTONOMOUS REVERSE ROUTER PROXY",
                    "Locks: go-gateway/main.go ports :8080 -> :8181",
                    "Coupled to: [Allocator Service, Telemetry Pipeline]",
                    "Enforces token bucket rate limits in real time."
                )
                "Allocator" -> Quadruple(
                    "PHYSICAL ALLOCATOR SCHEDULER",
                    "Locks: rust-microkernel/sched.rs threads 0..16",
                    "Coupled to: [Gateway Core, Db Mesh Broker]",
                    "Enforces spinlocked thread queue locks compliance."
                )
                "Db Mesh" -> Quadruple(
                    "SPANNER DISTRIBUTED PERSISTENCE",
                    "Locks: go-gateway/db/spanner.go keyspaces primary",
                    "Coupled to: [Allocator Core]",
                    "Enforces offline sync storage tables backups."
                )
                else -> Quadruple(
                    "LOGS AND METRICS PIPELINE PIPES",
                    "Locks: MainActivity.kt telemetry metrics buffer",
                    "Coupled to: [Gateway Core]",
                    "Pushes microsecond diagnostics records."
                )
            }

            Text(text = "SELECTED COMPONENT: $selectedNode", style = Typography.labelSmall, color = SpaceMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = Typography.titleLarge, color = PrimaryCyan)
            Spacer(modifier = Modifier.height(8.dp))
            NeonDivider()
            Spacer(modifier = Modifier.height(8.dp))

            MetricRow(label = "Hardware Target Layer", value = coupling, isMonospace = false)
            Spacer(modifier = Modifier.height(4.dp))
            MetricRow(label = "Active Downstream Links", value = logs, isMonospace = false)
            Spacer(modifier = Modifier.height(4.dp))
            MetricRow(label = "Aesthetic Integrity Policy", value = safety, isMonospace = false)
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun BoxScope.NodeButton(
    text: String,
    x: Int,
    y: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = (x - 50).dp, y = (y - 18).dp)
            .border(
                1.dp,
                if (selected) PrimaryCyan else SpaceMuted.copy(alpha = 0.4f),
                RoundedCornerShape(6.dp)
            )
            .background(
                if (selected) PrimaryCyan.copy(alpha = 0.2f) else GlassCardColor.copy(alpha = 0.8f),
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .size(width = 100.dp, height = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = Typography.labelSmall.copy(fontSize = 9.sp),
            color = if (selected) PrimaryCyan else OffWhite
        )
    }
}

// Sub-view 5: Screenshot Streaming (V2)
@Composable
fun ScreenshotsView(screenshotIndex: Int) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text("HOST WORKSPACE DIRECT DISPLAY STREAM", style = Typography.labelLarge, color = PrimaryCyan)
            Text("Live terminal outputs and core compilation buffers pulled periodically", style = Typography.bodyMedium, color = SpaceMuted)
        }

        // Live Graphic rendering simulated terminal
        GlassCard(borderColor = PrimaryCyan.copy(alpha = 0.3f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "FRAME BUFFER #$screenshotIndex LOCKED", style = Typography.labelSmall, color = SpaceMuted)
                Text(text = "REFRESH: AUTO ACTIVE", style = Typography.labelSmall, color = StatusGreen)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Box Mocking Terminal Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF010409))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("root@antigravity-ide:/workspace/omega-os$ make build-release", color = SpaceMuted, style = Typography.bodyMedium.copy(fontFamily = TechMonospace))
                    Spacer(modifier = Modifier.height(4.dp))

                    val lines = when (screenshotIndex) {
                        1 -> listOf(
                            "[LOCKS] cargo build --target=x86_64-unknown-none",
                            "   Compiling core v0.1.0 (/workspace/core)",
                            "   Compiling spinlocks v0.1.0 (/workspace/locks)",
                            "   Compiling sched v0.1.0 (/workspace/kernel)",
                            "WARNING: spinlock duration exceeds optimal limits under stress!"
                        )
                        2 -> listOf(
                            "[TESTS] cargo test --all",
                            "   Running unit tests for memory page allocator...",
                            "     test memory::allocator::test_oob_safety ... ok",
                            "     test scheduler::tests::test_multicore_locks ... ok",
                            "     test thread::spin_deadlocking ... ok"
                        )
                        3 -> listOf(
                            "[SERVICE] go build -o bld/gateway .",
                            "   Checking go.mod dependencies...",
                            "   Verification complete: checksum matched core",
                            "   Building binary bld/gateway ... link successful.",
                            "System ready to load rate limiter configurations."
                        )
                        4 -> listOf(
                            "[DOCKER] docker-compose up -d --build",
                            "   Creating database service ... created",
                            "   Creating redis gateway cache ... created",
                            "   Creating omega-ide-compilers ... created",
                            "STABILITY RATIO: 100% HEALTH CHECK PASS"
                        )
                        else -> listOf(
                            "[SECURITY AUDIT] npm audit signatures",
                            "   Audited 212 modules in 420ms ... zero hazards.",
                            "   Rule filter Auto-Approved npm install ... safe.",
                            "Host compiles completely insulated."
                        )
                    }

                    lines.forEach { line ->
                        val isAlert = line.contains("WARNING") || line.contains("hazard")
                        val isPass = line.contains("ok") || line.contains("successful") || line.contains("HEALTH")
                        val col = if (isAlert) StatusRed else if (isPass) StatusGreen else OffWhite
                        Text(text = line, color = col, style = Typography.bodyMedium.copy(fontFamily = TechMonospace, fontSize = 11.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Tip: Zooming and file expansion are managed by the Desktop Agent execution loop. Stream frequency matches workspace task complexity.", style = Typography.bodyMedium.copy(fontSize = 11.sp), color = SpaceMuted)
        }
    }
}

// Sub-view 6: Templates View
@Composable
fun TemplatesView(onDeploy: (String) -> Unit) {
    val temps = listOf(
        "Full Stack SaaS" to "Preloads client dashboard Next.js routers and Go router middleware pipelines.",
        "Ω-OS Microkernel Core" to "Preloads aerospace microkernel schedulers, thread allocation and clock spinlock tables.",
        "AI Agent CRM Swarm" to "Preloads multi-agent LangChain CRM nodes and task automation chains."
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("PRELOAD ROUTINE BLUEPRINTS LIBRARY", style = Typography.labelLarge, color = PrimaryCyan)
            Text("Deploy structured files maps and recommended pipelines with a single tap:", style = Typography.bodyMedium, color = SpaceMuted)
        }

        items(temps) { pair ->
            GlassCard(borderColor = PrimaryCyan.copy(alpha = 0.2f)) {
                Text(text = pair.first, style = Typography.titleLarge, color = PrimaryCyan)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = pair.second, style = Typography.bodyMedium, color = SpaceGlow)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onDeploy(pair.first) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DEPLOY SPECIFICATIONS", style = Typography.labelSmall)
                }
            }
        }
    }
}

// Sub-view 7: Settings View
@Composable
fun SettingsView(
    url: String,
    onUrlChange: (String) -> Unit,
    onToggle: () -> Unit,
    state: String,
    onClearAllLogs: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("GATEWAY AND PORTS MANAGER", style = Typography.labelLarge, color = PrimaryCyan)
            Text("Sync remote mobile control with live Antigravity IDE instances via WebSocket streams", style = Typography.bodyMedium, color = SpaceMuted)
        }

        GlassCard(borderColor = PrimaryCyan.copy(alpha = 0.35f)) {
            Text("WEBSOCKET CONTROL SYSTEM", style = Typography.labelSmall, color = SpaceMuted)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text("Control Gateway URL", color = SpaceMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryCyan,
                    unfocusedBorderColor = SpaceMuted,
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = SpaceGlow
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            MetricRow(label = "WebSocket Connection State:", value = state, valueColor = if (state == "Connected") StatusGreen else StatusRed)

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state == "Connected") StatusRed else PrimaryCyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (state == "Connected") "DISCONNECT PORT" else "CONNECT TO PORT", style = Typography.labelSmall)
            }
        }

        GlassCard(borderColor = SpaceMuted.copy(alpha = 0.3f)) {
            Text("WORKSPACE PURGE AND FACTORY DEFAULT", style = Typography.labelSmall, color = StatusRed)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Wipe and factory reset all active local SQLite databases (Missions history, prompts logs, and clearance registry rules).", style = Typography.bodyMedium, color = SpaceGlow)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClearAllLogs,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("FACTORY WIPE ALL REGISTRIES", style = Typography.labelSmall)
            }
        }
    }
}

// Sub-view 8: Emergency View
@Composable
fun EmergencyView(
    onEmergencyStop: () -> Unit
) {
    var confirmed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .border(2.dp, StatusRed, CircleShape)
                .background(StatusRed.copy(alpha = 0.1f), CircleShape)
                .size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Dangerous, contentDescription = "Emergency Danger", tint = StatusRed, modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("CRITICAL: UNILATERAL AGENT CUT-OFF", style = Typography.displayMedium, color = StatusRed)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Initiating unilateral stop overrides all linked desktop agents. Disconnects high-performance port grids, wipes pending terminal commands cache memory, and enforces rigid software execution freezes.",
            style = Typography.bodyMedium,
            color = SpaceGlow
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = confirmed,
                onCheckedChange = { confirmed = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = StatusRed,
                    uncheckedColor = SpaceMuted,
                    checkmarkColor = Color.Black
                )
            )
            Text("AUTHORIZE EMERGENCY SYSTEM TERMINATION PROTOCOL", style = Typography.labelSmall, color = SpaceGlow)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (confirmed) {
                    onEmergencyStop()
                    confirmed = false
                }
            },
            enabled = confirmed,
            colors = ButtonDefaults.buttonColors(
                containerColor = StatusRed,
                contentColor = Color.Black,
                disabledContainerColor = StatusRed.copy(alpha = 0.25f),
                disabledContentColor = SpaceMuted
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = "Kill all agents")
                Text("EXECUTE UNILATERAL HALT", style = Typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
