package com.example.data.repository

import com.example.data.dao.ControlDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ControlRepository(private val controlDao: ControlDao) {

    val allMissions: Flow<List<MissionEntity>> = controlDao.getAllMissions()
    val allSessions: Flow<List<SessionEntity>> = controlDao.getAllSessions()
    val allApprovalItems: Flow<List<ApprovalItemEntity>> = controlDao.getAllApprovalItems()
    val allApprovalRules: Flow<List<ApprovalRuleEntity>> = controlDao.getAllApprovalRules()
    val allPrompts: Flow<List<PromptEntity>> = controlDao.getAllPrompts()
    val allNotifications: Flow<List<NotificationEntity>> = controlDao.getAllNotifications()

    // Setup initial showcase data if empty
    suspend fun checkAndPreloadData() {
        val currentMissions = allMissions.firstOrNull()
        if (currentMissions.isNullOrEmpty()) {
            preloadShowcaseData()
        }
    }

    private suspend fun preloadShowcaseData() {
        // Preload Missions
        val m1 = MissionEntity(
            title = "Ω-OS: Custom Microkernel Operating System",
            description = "A military-spec minimalist operating system microkernel written in pure Rust for mission-critical aerospace compute layers.",
            objective = "Reach complete hardware scheduling compliance and visual display driver rendering loops.",
            priority = "CRITICAL",
            status = "Building",
            tags = "Rust,OS,Aerospace,Microkernel",
            createdTime = System.currentTimeMillis() - 72000000,
            deadline = System.currentTimeMillis() + 864000000,
            progress = 0.64f,
            tasksJson = "Scaffold Project|Completed|100,Design Memory Allocator|Completed|100,Implement Scheduler|Building|80,Design Custom CLI Desktop|Planning|0,Write File System Driver|Planning|0,Build Framebuffer Visualizer|Planning|0"
        )
        val m2 = MissionEntity(
            title = "Autonomous Cloud API Gateway Core",
            description = "A distributed high-throughput reverse proxy and API controller with real-time rate-limiting, custom token bucket validation, and visual log pipes.",
            objective = "Build, load-test, and deploy a robust Go router module to our cloud cluster.",
            priority = "HIGH",
            status = "Testing",
            tags = "Go,Docker,Cloud,WebSockets",
            createdTime = System.currentTimeMillis() - 36000000,
            deadline = System.currentTimeMillis() + 432000000,
            progress = 0.85f,
            tasksJson = "Configure Endpoint Structures|Completed|100,Add JWT Cryptographic Layer|Completed|100,Implement Token Bucket Limiter|Completed|100,Create Live Websocket Stream Pipe|Completed|100,Perform Load Testing Suite|Testing|65,Deploy Kubernetes Cluster|Planning|0"
        )
        val m3 = MissionEntity(
            title = "Interactive Cosmic Sound Synthesizer UI",
            description = "Scaffold a Jetpack Compose synthesizer interface with animated modular routing visualizers, tactile sliders, and high-performance Oboe audio bindings.",
            objective = "Provide a gorgeous futuristic mobile interface for our companion audio hardware engine.",
            priority = "MEDIUM",
            status = "Planning",
            tags = "Kotlin,C++,Audio,ComposeUI",
            createdTime = System.currentTimeMillis() - 10000000,
            deadline = System.currentTimeMillis() + 259200000,
            progress = 0.12f,
            tasksJson = "Draft Color Schemes and Theme Tokens|Completed|100,Setup Modular Synthesizer Canvas|Planning|15,Configure Synth Oscillator Core Bindings|Planning|0,Refactor UI Touch Targets to 48dp|Planning|0"
        )
        controlDao.insertMission(m1)
        controlDao.insertMission(m2)
        controlDao.insertMission(m3)

        // Preload Sessions
        val s1 = SessionEntity(
            sessionId = "SESS-OP-808",
            workspace = "omega-os",
            agentName = "Genesis Alpha",
            currentFile = "kernel/sched.rs",
            activeTask = "Optimize multicore context switcher",
            runtime = "04:12:35",
            status = "Busy",
            cpuUsage = 68.4f,
            ramUsage = 52.1f,
            diskUsage = 24.5f
        )
        val s2 = SessionEntity(
            sessionId = "SESS-SEC-404",
            workspace = "api-gateway",
            agentName = "Nexus Beta",
            currentFile = "middleware/limiter.go",
            activeTask = "Audit token bucket rate limiter concurrency logs",
            runtime = "01:24:10",
            status = "Waiting Approval",
            cpuUsage = 5.2f,
            ramUsage = 34.6f,
            diskUsage = 15.1f
        )
        val s3 = SessionEntity(
            sessionId = "SESS-DEV-202",
            workspace = "synth-ambient",
            agentName = "Aura Gamma",
            currentFile = "MainActivity.kt",
            activeTask = "Refactor compose theme color palettes",
            runtime = "00:08:45",
            status = "Online",
            cpuUsage = 1.8f,
            ramUsage = 18.2f,
            diskUsage = 12.0f
        )
        controlDao.insertSession(s1)
        controlDao.insertSession(s2)
        controlDao.insertSession(s3)

        // Preload Approval Items
        controlDao.insertApprovalItem(
            ApprovalItemEntity(
                command = "rm -rf /docker/workspace/dist-cache",
                riskLevel = "HIGH",
                reason = "Purge compiled native workspace cache prior to linking release firmware binaries.",
                status = "PENDING",
                timestamp = System.currentTimeMillis() - 120000
            )
        )
        controlDao.insertApprovalItem(
            ApprovalItemEntity(
                command = "git push production main --force",
                riskLevel = "CRITICAL",
                reason = "Bypass branch restrictions to forcefully hotfix recursive kernel allocator memory leaks during high-stress simulation.",
                status = "PENDING",
                timestamp = System.currentTimeMillis() - 60000
            )
        )
        controlDao.insertApprovalItem(
            ApprovalItemEntity(
                command = "npm install @magicui/globe --save",
                riskLevel = "LOW",
                reason = "Acquire robust orbital animation canvas library for aerospace navigation telemetry visuals.",
                status = "PENDING",
                timestamp = System.currentTimeMillis() - 10000
            )
        )
        controlDao.insertApprovalItem(
            ApprovalItemEntity(
                command = "go mod tidy && go build .",
                riskLevel = "LOW",
                reason = "Verify newly added encryption libraries compilation compatibility.",
                status = "APPROVED",
                timestamp = System.currentTimeMillis() - 500000
            )
        )

        // Preload Approval Rules
        controlDao.insertApprovalRule(ApprovalRuleEntity(pattern = "npm install", actionType = "AUTO_APPROVE"))
        controlDao.insertApprovalRule(ApprovalRuleEntity(pattern = "go mod tidy", actionType = "AUTO_APPROVE"))
        controlDao.insertApprovalRule(ApprovalRuleEntity(pattern = "cargo build", actionType = "AUTO_APPROVE"))
        controlDao.insertApprovalRule(ApprovalRuleEntity(pattern = "git push", actionType = "REQUIRE_APPROVAL"))
        controlDao.insertApprovalRule(ApprovalRuleEntity(pattern = "rm -rf", actionType = "REQUIRE_APPROVAL"))
        controlDao.insertApprovalRule(ApprovalRuleEntity(pattern = "database migrations", actionType = "REQUIRE_APPROVAL"))
        controlDao.insertApprovalRule(ApprovalRuleEntity(pattern = "deployments", actionType = "REQUIRE_APPROVAL"))

        // Preload Prompt History
        controlDao.insertPrompt(
            PromptEntity(
                sessionId = "SESS-OP-808",
                prompt = "Implement spinlock core state synchronization in kernel sched.",
                response = "Successfully deployed spinlocked sync mutex under 'kernel/sched.rs'. Re-routing tasks.",
                status = "SUCCESS",
                timestamp = System.currentTimeMillis() - 1800000
            )
        )
        controlDao.insertPrompt(
            PromptEntity(
                sessionId = "SESS-OP-808",
                prompt = "Draft telemetry console charts scaffold.",
                response = "Canvas layout engineered successfully. Awaiting approval of canvas cache purging.",
                status = "SUCCESS",
                timestamp = System.currentTimeMillis() - 900000
            )
        )

        // Preload Notifications
        controlDao.insertNotification(
            NotificationEntity(
                title = "Approval Required",
                message = "Agent Genesis Alpha requested immediate approval for critical system operation: 'git push production main --force'.",
                timestamp = System.currentTimeMillis() - 60000,
                type = "APPROVAL"
            )
        )
        controlDao.insertNotification(
            NotificationEntity(
                title = "Health Alert: Memory High",
                message = "Session SESS-OP-808 reported system memory utilization exceedance (84%).",
                timestamp = System.currentTimeMillis() - 360000,
                type = "ERROR"
            )
        )
        controlDao.insertNotification(
            NotificationEntity(
                title = "Build Completed",
                message = "Go router gateway module compiles successfully with zero warnings.",
                timestamp = System.currentTimeMillis() - 600000,
                type = "BUILD"
            )
        )
    }

    // Direct Database Manipulations
    suspend fun insertMission(mission: MissionEntity) = controlDao.insertMission(mission)
    suspend fun updateMission(mission: MissionEntity) = controlDao.updateMission(mission)
    suspend fun deleteMission(mission: MissionEntity) = controlDao.deleteMission(mission)

    suspend fun insertSession(session: SessionEntity) = controlDao.insertSession(session)
    suspend fun updateSession(session: SessionEntity) = controlDao.updateSession(session)
    suspend fun deleteSession(session: SessionEntity) = controlDao.deleteSession(session)

    suspend fun insertApprovalItem(item: ApprovalItemEntity) = controlDao.insertApprovalItem(item)
    suspend fun updateApprovalItem(item: ApprovalItemEntity) = controlDao.updateApprovalItem(item)
    suspend fun deleteApprovalItem(item: ApprovalItemEntity) = controlDao.deleteApprovalItem(item)

    suspend fun insertApprovalRule(rule: ApprovalRuleEntity) = controlDao.insertApprovalRule(rule)
    suspend fun deleteApprovalRule(rule: ApprovalRuleEntity) = controlDao.deleteApprovalRule(rule)

    suspend fun insertPrompt(prompt: PromptEntity) = controlDao.insertPrompt(prompt)
    suspend fun clearPromptHistory() = controlDao.clearPromptHistory()

    suspend fun insertNotification(notification: NotificationEntity) = controlDao.insertNotification(notification)
    suspend fun updateNotification(notification: NotificationEntity) = controlDao.updateNotification(notification)
}
