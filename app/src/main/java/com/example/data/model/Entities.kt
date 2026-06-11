package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val objective: String,
    val priority: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val status: String, // "Planning", "Researching", "Building", "Testing", "Reviewing", "Completed", "Failed"
    val tags: String, // Comma separated list, e.g. "Go, REST API"
    val createdTime: Long,
    val deadline: Long,
    val progress: Float, // 0.0 to 1.0
    val tasksJson: String // Serialized dynamic list: "title|status|progress,..."
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val workspace: String,
    val agentName: String,
    val currentFile: String,
    val activeTask: String,
    val runtime: String,
    val status: String, // "Online", "Offline", "Busy", "Waiting Approval", "Error"
    val cpuUsage: Float,
    val ramUsage: Float,
    val diskUsage: Float
)

@Entity(tableName = "approval_items")
data class ApprovalItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val command: String,
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val reason: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long
)

@Entity(tableName = "approval_rules")
data class ApprovalRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pattern: String, // e.g. "npm install"
    val actionType: String // "AUTO_APPROVE", "REQUIRE_APPROVAL"
)

@Entity(tableName = "prompt_history")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val prompt: String,
    val response: String,
    val status: String, // "SUCCESS", "EXECUTING", "FAILED"
    val timestamp: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String, // "APPROVAL", "BUILD", "MISSION", "ERROR"
    val isRead: Boolean = false
)
