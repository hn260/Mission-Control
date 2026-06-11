package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.network.ConnectionState
import com.example.data.network.WebSocketService
import com.example.data.repository.ControlRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class MissionControlViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ControlRepository(database.controlDao())
    val webSocketService = WebSocketService()

    // Screen State Navigation
    private val _activeTab = MutableStateFlow("Home") // Home (Command Center), Missions, Sessions, Telemetry, More
    val activeTab: StateFlow<String> = _activeTab

    private val _activeScreen = MutableStateFlow<String?>(null) // e.g. "files", "architecture", "rules", "templates", "settings", "approvals"
    val activeScreen: StateFlow<String?> = _activeScreen

    private val _selectedMissionId = MutableStateFlow<Int?>(null)
    val selectedMissionId: StateFlow<Int?> = _selectedMissionId

    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId

    // Room Database Observables
    val missions: StateFlow<List<MissionEntity>> = repository.allMissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<SessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvalItems: StateFlow<List<ApprovalItemEntity>> = repository.allApprovalItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvalRules: StateFlow<List<ApprovalRuleEntity>> = repository.allApprovalRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prompts: StateFlow<List<PromptEntity>> = repository.allPrompts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val webSocketState: StateFlow<ConnectionState> = webSocketService.connectionState

    // Live Telemetry history buffers for custom canvases (radar/line graphs)
    private val _cpuHistory = MutableStateFlow<List<Float>>(List(10) { 15f + Random.nextFloat() * 40f })
    val cpuHistory: StateFlow<List<Float>> = _cpuHistory

    private val _ramHistory = MutableStateFlow<List<Float>>(List(10) { 30f + Random.nextFloat() * 30f })
    val ramHistory: StateFlow<List<Float>> = _ramHistory

    // Voice control states
    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening

    private val _voiceOutputText = MutableStateFlow("")
    val voiceOutputText: StateFlow<String> = _voiceOutputText

    // Search bar state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // WebSocket URL setting
    private val _webSocketUrlSetting = MutableStateFlow("ws://127.0.0.1:8181/control")
    val webSocketUrlSetting: StateFlow<String> = _webSocketUrlSetting

    // Screenshot Stream Index to mock live IDE changes (Index 1 to 5)
    private val _screenshotStreamIndex = MutableStateFlow(1)
    val screenshotStreamIndex: StateFlow<Int> = _screenshotStreamIndex

    init {
        viewModelScope.launch {
            // First run, populate DB
            repository.checkAndPreloadData()
            // Start simulation cycles for live telemetry and dashboard animations
            startTelemetrySimulation()
        }

        // Connect to WebSocket messages internally
        viewModelScope.launch {
            webSocketService.incomingMessages.collect { message ->
                // Handle commands sent from desktop agent
                processIncomingWebSocketMessage(message)
            }
        }
    }

    // Setters
    fun setActiveTab(tab: String) {
        _activeTab.value = tab
        _activeScreen.value = null // reset nested screens
    }

    fun openScreen(screen: String?) {
        _activeScreen.value = screen
    }

    fun selectMission(id: Int?) {
        _selectedMissionId.value = id
    }

    fun selectSession(id: String?) {
        _selectedSessionId.value = id
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateWebSocketUrl(url: String) {
        _webSocketUrlSetting.value = url
    }

    // WebSocket Controls
    fun toggleWebSocketConnection() {
        if (webSocketState.value is ConnectionState.Connected) {
            webSocketService.disconnect()
        } else {
            webSocketService.connect(_webSocketUrlSetting.value)
        }
    }

    // Business Commands
    fun addNotification(title: String, message: String, type: String) {
        viewModelScope.launch {
            repository.insertNotification(
                NotificationEntity(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    type = type
                )
            )
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication())
            db.controlDao().clearNotifications()
        }
    }

    fun sendPromptDirect(promptText: String) {
        if (promptText.isBlank()) return
        val activeSess = _selectedSessionId.value ?: "SESS-OP-808"

        viewModelScope.launch {
            // Check if websocket is active
            if (webSocketState.value is ConnectionState.Connected) {
                webSocketService.sendMessage("{\"action\":\"prompt\", \"session\":\"$activeSess\", \"text\":\"$promptText\"}")
            }

            // Save in history
            repository.insertPrompt(
                PromptEntity(
                    sessionId = activeSess,
                    prompt = promptText,
                    response = "Evaluating specifications... Action queued in stream.",
                    status = "EXECUTING",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Simulate immediate activity on current session
            sessions.value.find { it.sessionId == activeSess }?.let { currentSession ->
                repository.updateSession(
                    currentSession.copy(
                        status = "Busy",
                        activeTask = "Command: $promptText",
                        cpuUsage = 85.5f
                    )
                )
            }

            addNotification(
                title = "Prompt Sent",
                message = "Command streamed directly to session context: '$promptText'",
                type = "BUILD"
            )
        }
    }

    fun approveItem(item: ApprovalItemEntity) {
        viewModelScope.launch {
            repository.updateApprovalItem(item.copy(status = "APPROVED"))

            if (webSocketState.value is ConnectionState.Connected) {
                webSocketService.sendMessage("{\"action\":\"approval_result\", \"command\":\"${item.command}\", \"status\":\"APPROVED\"}")
            }

            // Add notification
            addNotification(
                title = "Action Approved",
                message = "Command approved: '${item.command}' has been marked safe.",
                type = "VAL"
            )

            // Simulate session getting busy with output
            val affectedSession = sessions.value.firstOrNull { it.status == "Waiting Approval" }
            if (affectedSession != null) {
                repository.updateSession(
                    affectedSession.copy(
                        status = "Busy",
                        cpuUsage = 72.8f,
                        activeTask = "Running approved command: ${item.command}"
                    )
                )
            }
        }
    }

    fun rejectItem(item: ApprovalItemEntity) {
        viewModelScope.launch {
            repository.updateApprovalItem(item.copy(status = "REJECTED"))

            if (webSocketState.value is ConnectionState.Connected) {
                webSocketService.sendMessage("{\"action\":\"approval_result\", \"command\":\"${item.command}\", \"status\":\"REJECTED\"}")
            }

            addNotification(
                title = "Action Rejected",
                message = "Security policy enforced: Action denied for command '${item.command}'.",
                type = "ERROR"
            )

            // Release affected session to Online
            val affectedSession = sessions.value.firstOrNull { it.status == "Waiting Approval" }
            if (affectedSession != null) {
                repository.updateSession(
                    affectedSession.copy(
                        status = "Online",
                        cpuUsage = 2.4f,
                        activeTask = "Idle - Security halt applied"
                    )
                )
            }
        }
    }

    fun addCustomRule(pattern: String, actionType: String) {
        viewModelScope.launch {
            repository.insertApprovalRule(
                ApprovalRuleEntity(
                    pattern = pattern,
                    actionType = actionType
                )
            )
            addNotification(
                title = "Security Policy Added",
                message = "Pattern '$pattern' classified as $actionType",
                type = "VAL"
            )
        }
    }

    fun deleteCustomRule(rule: ApprovalRuleEntity) {
        viewModelScope.launch {
            repository.deleteApprovalRule(rule)
        }
    }

    fun createMission(
        title: String,
        desc: String,
        priority: String,
        objective: String,
        tags: String,
        fullSpecText: String?,
        voiceText: String?
    ) {
        viewModelScope.launch {
            var decomp = "Scaffold Project|Planning|0,Setup Configuration Framework|Planning|0,Define API specs|Planning|0"
            if (fullSpecText != null && fullSpecText.isNotBlank()) {
                // Multi-thousand word specifications decompose into elaborate tasks!
                decomp = "Synthesize Ω-Architecture Specification|Completed|100,Design Process Sandboxing Pipes|Planning|0,Configure Dynamic Thread Ring Allocation|Planning|0,Compile Sandbox Virtualizer Modules|Planning|0,Register Core telemetry nodes|Planning|0,Conduct Aerospace stress simulations|Planning|0"
            } else if (voiceText != null && voiceText.isNotBlank()) {
                decomp = "Analyze Voice specification payload|Completed|100,Implement voice requirement bindings|Planning|0,Verify speech UI interface structures|Planning|0"
            }

            val titleSafe = title.ifBlank { "Remote Mission Alpha" }
            val descSafe = desc.ifBlank { "User remotely deployed specifications script command loop." }
            val currentTags = tags.ifBlank { "Workspace,Go,Dynamic" }

            val newM = MissionEntity(
                title = titleSafe,
                description = descSafe,
                objective = objective.ifBlank { "Deliver reliable compiles and architectural blueprints." },
                priority = priority,
                status = "Planning",
                tags = currentTags,
                createdTime = System.currentTimeMillis(),
                deadline = System.currentTimeMillis() + 648000000,
                progress = 0.05f,
                tasksJson = decomp
            )
            repository.insertMission(newM)

            addNotification(
                title = "Mission Launched",
                message = "New tactical engineering task launched: '$titleSafe'",
                type = "MISSION"
            )

            // Pre-assign high CPU to sessions
            sessions.value.firstOrNull()?.let { s ->
                repository.updateSession(
                    s.copy(
                        status = "Busy",
                        activeTask = "Planning workspace resources for: $titleSafe"
                    )
                )
            }
        }
    }

    fun deployTemplate(templateName: String) {
        val templateData = when (templateName) {
            "Full Stack SaaS" -> Triple(
                "Launch Client SaaS Node",
                "Scaffold modern client-facing application interface, configure billing portals API, and bind cloud db sync brokers.",
                "HIGH"
            ).let { t ->
                listOf(
                    t.first, t.second, t.third,
                    "Establish full OAuth connectivity and deploy test sandbox.", "Next.js,OAuth,Cloud,SaaS",
                    "Configure Next.js edge routers|Completed|100,Secure credentials vault keys|Completed|100,Optimize dashboard graphs layers|Building|30,Link Firestore cloud streams|Planning|0,Setup telemetry log filters|Planning|0"
                )
            }
            "Ω-OS Microkernel Core" -> Triple(
                "Build Aerospace microkernel thread-allocator",
                "Develop thread pools, priority scheduling tables, and direct hardware clock frequency scaling layers for aircraft systems.",
                "CRITICAL"
            ).let { t ->
                listOf(
                    t.first, t.second, t.third,
                    "Compile and verify Rust microkernel compilation state.", "Rust,OS,Microkernel,Oboe",
                    "Define thread queues models|Completed|100,Establish spinlock clock vectors|Completed|100,Design core scheduler routines|Building|85,Run thread deadlocking tests|Planning|0,Compile release target firmware|Planning|0"
                )
            }
            "AI Agent CRM Swarm" -> Triple(
                "Autonomous Multi-Agent CRM Controller",
                "Integrate recursive customer profile parsing agents. Coordinate workspace file edits to optimize CRM data pipes.",
                "MEDIUM"
            ).let { t ->
                listOf(
                    t.first, t.second, t.third,
                    "Auto-generate visual interactive contact flow graphs.", "Python,AI,LLM,LangChain",
                    "Setup contact profile buffers|Completed|100,Configure LangChain pipeline|Building|50,Verify response rate thresholds|Planning|0"
                )
            }
            else -> Triple(
                "General Workspace Routine Build",
                "Run standard clean rebuild, pull latest remote dependency tags, and execute unit testing script loops.",
                "LOW"
            ).let { t ->
                listOf(
                    t.first, t.second, t.third,
                    "Clean local environment compiler cache.", "C++,Unix,Nginx",
                    "Clean cache folders|Completed|100,Go tidy imports|Planning|0,Run unit tests array|Planning|0"
                )
            }
        }

        val title = templateData[0]
        val desc = templateData[1]
        val priority = templateData[2]
        val objective = templateData[3]
        val tags = templateData[4]
        val tasks = templateData[5]

        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication())
            db.controlDao().insertMission(
                MissionEntity(
                    title = title,
                    description = desc,
                    objective = objective,
                    priority = priority,
                    status = "Planning",
                    tags = tags,
                    createdTime = System.currentTimeMillis(),
                    deadline = System.currentTimeMillis() + 172800000,
                    progress = 0.40f,
                    tasksJson = tasks
                )
            )

            addNotification(
                title = "Template Launched",
                message = "Deployed Template '$templateName' successfully.",
                type = "MISSION"
            )
        }
    }

    fun toggleSessionStatus(session: SessionEntity) {
        viewModelScope.launch {
            val newStatus = when (session.status) {
                "Online" -> "Busy"
                "Busy" -> "Online"
                "Offline" -> "Online"
                else -> "Online"
            }
            repository.updateSession(
                session.copy(
                    status = newStatus,
                    activeTask = if (newStatus == "Online") "Idle" else "Autonomous specification task execution"
                )
            )
        }
    }

    // EMERGENCY HALT
    fun emergencyStopAll() {
        viewModelScope.launch {
            // Disconnect socket
            webSocketService.disconnect()

            // Update all session states to Offline / Stop executing immediately
            val allS = sessions.value
            for (s in allS) {
                repository.updateSession(
                    s.copy(
                        status = "Offline",
                        activeTask = "EMERGENCY POWER OUTAGE INITIATED BY OPERATOR",
                        cpuUsage = 0f,
                        ramUsage = 0f
                    )
                )
            }

            // Put active building missions to Failed/Stopped
            val allM = missions.value
            for (m in allM) {
                if (m.status == "Building" || m.status == "Researching") {
                    repository.updateMission(
                        m.copy(status = "Failed")
                    )
                }
            }

            // Reject all pending approvals
            val allA = approvalItems.value
            for (a in allA) {
                if (a.status == "PENDING") {
                    repository.updateApprovalItem(
                        a.copy(status = "REJECTED")
                    )
                }
            }

            addNotification(
                title = "EMERGENCY HALT TRIGGERED",
                message = "All autonomous agent execution loops killed. Sessions disconnected. Host processes isolated.",
                type = "ERROR"
            )
        }
    }

    // Voice simulation
    fun triggerVoiceListening() {
        if (_isVoiceListening.value) {
            // Terminate and analyze
            _isVoiceListening.value = false
            val recognized = _voiceOutputText.value
            if (recognized.isNotBlank()) {
                executeVoiceCommand(recognized)
            }
        } else {
            _isVoiceListening.value = true
            _voiceOutputText.value = ""
            // Mock incremental recognized strings
            viewModelScope.launch {
                val voiceSentences = listOf(
                    "launch",
                    "launch a new",
                    "launch a new mission for",
                    "launch a new mission for Ω-OS sound synthesizer engine"
                )
                for (chunk in voiceSentences) {
                    if (!_isVoiceListening.value) break
                    kotlinx.coroutines.delay(800)
                    _voiceOutputText.value = chunk
                }
            }
        }
    }

    private fun executeVoiceCommand(command: String) {
        val lower = command.lowercase()
        viewModelScope.launch {
            when {
                lower.contains("pause") || lower.contains("stop") -> {
                    emergencyStopAll()
                    addNotification("Voice Command Success", "Analyzed voice input. Emergency cut commands transmitted.", "ERROR")
                }
                lower.contains("approve") -> {
                    val pending = approvalItems.value.firstOrNull { it.status == "PENDING" }
                    if (pending != null) {
                        approveItem(pending)
                        addNotification("Voice Command Success", "Analyzed voice: 'Approve newest command'. Action taken.", "VAL")
                    } else {
                        addNotification("Voice Command Error", "Analyzed voice. No pending actions found to approve.", "ERROR")
                    }
                }
                lower.contains("mission") || lower.contains("launch") || lower.contains("synth") -> {
                    createMission(
                        title = "Voice Mission: Sound Synthesizer Engine",
                        desc = "Automatic mission generated by speech recognition payload.",
                        priority = "MEDIUM",
                        objective = "Build acoustic core oscillator vectors.",
                        tags = "Voice,Audio,Core",
                        fullSpecText = null,
                        voiceText = "Voice input command captured text successfully: $command"
                    )
                }
                else -> {
                    addNotification("Voice Command Processed", "Synthesized instruction query: '$command' matched no direct action rules.", "BUILD")
                }
            }
        }
    }

    // Real-Time Telemetry & Agent Simulation Cycle
    private suspend fun startTelemetrySimulation() {
        while (true) {
            kotlinx.coroutines.delay(2500)

            // Randomize graph buffer
            val newC = _cpuHistory.value.toMutableList()
            newC.removeAt(0)
            newC.add(30f + Random.nextFloat() * 55f)
            _cpuHistory.value = newC

            val newR = _ramHistory.value.toMutableList()
            newR.removeAt(0)
            newR.add(45f + Random.nextFloat() * 15f)
            _ramHistory.value = newR

            // Mock screenshot indexing cycle (loops 1 to 5)
            _screenshotStreamIndex.value = if (_screenshotStreamIndex.value >= 5) 1 else _screenshotStreamIndex.value + 1

            // Simulated Agent Loop updates
            val currentSessions = sessions.value.toMutableList()
            if (currentSessions.isNotEmpty()) {
                // If the first agent is busy, let's randomly increment some missions progress!
                val activeMissions = missions.value.filter { it.status == "Building" || it.status == "Testing" }
                if (activeMissions.isNotEmpty()) {
                    val targetM = activeMissions.random()
                    val nextProgress = (targetM.progress + 0.01f).coerceAtMost(0.99f)

                    // Parse tasks, augment progress
                    val tasks = targetM.tasksJson.split(",").map { tStr ->
                        val parts = tStr.split("|")
                        if (parts.size >= 3) {
                            val tTitle = parts[0]
                            val tStatus = parts[1]
                            val tProg = parts[2].toIntOrNull() ?: 0
                            if (tStatus == "Building" || tStatus == "Testing") {
                                val nextPr = (tProg + 5).coerceAtMost(99)
                                "$tTitle|$tStatus|$nextPr"
                            } else {
                                tStr
                            }
                        } else {
                            tStr
                        }
                    }.joinToString(",")

                    repository.updateMission(
                        targetM.copy(
                            progress = nextProgress,
                            tasksJson = tasks
                        )
                    )
                }

                // Randomly modify current building file of session to make display live too
                val listFiles = listOf(
                    "kernel/sched.rs", "kernel/mem.rs", "drivers/gpu.rs", "Cargo.toml",
                    "middleware/limiter.go", "db/spanner.go", "config/gateway.yaml",
                    "MainActivity.kt", "Theme.kt", "Dashboard.kt"
                )

                for (i in currentSessions.indices) {
                    val s = currentSessions[i]
                    if (s.status == "Busy") {
                        val nextCpu = (50f + Random.nextFloat() * 35f).coerceIn(40f, 98f)
                        val nextRam = (40f + Random.nextFloat() * 20f).coerceIn(30f, 95f)
                        val nextFile = listFiles.random()

                        repository.updateSession(
                            s.copy(
                                cpuUsage = Math.round(nextCpu * 10f) / 10f,
                                ramUsage = Math.round(nextRam * 10f) / 10f,
                                currentFile = nextFile
                            )
                        )
                    } else if (s.status == "Online") {
                        val nextCpu = (1f + Random.nextFloat() * 4f).coerceIn(1f, 8f)
                        repository.updateSession(
                            s.copy(
                                cpuUsage = Math.round(nextCpu * 10f) / 10f
                            )
                        )
                    }
                }

                // Chance to trigger an unsolicited Approval Requirement command
                if (Random.nextFloat() > 0.85f && !approvalItems.value.any { it.status == "PENDING" }) {
                    val dangerousCmds = listOf(
                        "rm -rf /docker/temp/compiled_objs" to "Requested cleanup of workspace caches.",
                        "git push main development --force" to "Forced merge of modular OS display branch to fix render locks.",
                        "database migrations migrate_production" to "Applying structural billing database schemes manually.",
                        "npm install crypto-js --save" to "Import cryptographic modules for credentials tokens."
                    )
                    val pick = dangerousCmds.random()

                    // Check if any rules exist to auto approve this!
                    val matchingRule = approvalRules.value.find { rule ->
                        pick.first.startsWith(rule.pattern) || pick.first.contains(rule.pattern)
                    }

                    if (matchingRule != null && matchingRule.actionType == "AUTO_APPROVE") {
                        // Auto Approved! Save straight as APPROVED
                        repository.insertApprovalItem(
                            ApprovalItemEntity(
                                command = pick.first,
                                riskLevel = "LOW",
                                reason = pick.second,
                                status = "APPROVED",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        addNotification(
                            title = "Policy Auto-Approved",
                            message = "Action '${pick.first}' authorized automatically by security policy.",
                            type = "VAL"
                        )
                    } else {
                        // Needs approval. Insert as PENDING and change a session status to "Waiting Approval"
                        repository.insertApprovalItem(
                            ApprovalItemEntity(
                                command = pick.first,
                                riskLevel = if (pick.first.contains("force") || pick.first.contains("rm")) "CRITICAL" else "MEDIUM",
                                reason = pick.second,
                                status = "PENDING",
                                timestamp = System.currentTimeMillis()
                            )
                        )

                        currentSessions.firstOrNull { it.status == "Busy" }?.let { s ->
                            repository.updateSession(
                                s.copy(
                                    status = "Waiting Approval",
                                    cpuUsage = 4.2f,
                                    activeTask = "HALTED: Waiting user authorization for command action: ${pick.first}"
                                )
                            )
                        }

                        addNotification(
                            title = "Authorization Required",
                            message = "Dangerous action detected: operator authorization required to run: '${pick.first}'",
                            type = "APPROVAL"
                        )
                    }
                }
            }
        }
    }

    private fun processIncomingWebSocketMessage(msg: String) {
        // Simple incoming message router
        // In real workspace this allows external IDE to append items, complete tasks, inject live status
        viewModelScope.launch {
            if (msg.contains("alarm") || msg.contains("error")) {
                addNotification("Remote Host Alert", "Desktop machine reported: $msg", "ERROR")
            } else {
                addNotification("Desktop Signal Received", "Workspace telemetry packet decoded.", "BUILD")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}
