package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ControlDao {
    // Missions
    @Query("SELECT * FROM missions ORDER BY createdTime DESC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Delete
    suspend fun deleteMission(mission: MissionEntity)

    @Query("DELETE FROM missions")
    suspend fun clearMissions()

    // Sessions
    @Query("SELECT * FROM sessions ORDER BY sessionId ASC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun clearSessions()

    // Approval Items
    @Query("SELECT * FROM approval_items ORDER BY timestamp DESC")
    fun getAllApprovalItems(): Flow<List<ApprovalItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalItem(item: ApprovalItemEntity)

    @Update
    suspend fun updateApprovalItem(item: ApprovalItemEntity)

    @Delete
    suspend fun deleteApprovalItem(item: ApprovalItemEntity)

    @Query("DELETE FROM approval_items")
    suspend fun clearApprovalItems()

    // Approval Rules
    @Query("SELECT * FROM approval_rules ORDER BY id ASC")
    fun getAllApprovalRules(): Flow<List<ApprovalRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalRule(rule: ApprovalRuleEntity)

    @Update
    suspend fun updateApprovalRule(rule: ApprovalRuleEntity)

    @Delete
    suspend fun deleteApprovalRule(rule: ApprovalRuleEntity)

    @Query("DELETE FROM approval_rules")
    suspend fun clearApprovalRules()

    // Prompt History
    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    fun getAllPrompts(): Flow<List<PromptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity)

    @Delete
    suspend fun deletePrompt(prompt: PromptEntity)

    @Query("DELETE FROM prompt_history")
    suspend fun clearPromptHistory()

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()
}
