package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Conversation
import com.example.data.model.ConversationParticipant
import com.example.data.model.Favorite
import com.example.data.model.Message
import com.example.data.model.Notification
import com.example.data.model.PropertyView
import com.example.data.model.Report
import com.example.data.model.ReportStatus
import com.example.data.model.VisitRequest
import com.example.data.model.VisitStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {
    // Favorites
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesForUser(userId: Long): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND propertyId = :propertyId LIMIT 1")
    suspend fun getFavorite(userId: Long, propertyId: Long): Favorite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite): Long

    @Query("DELETE FROM favorites WHERE userId = :userId AND propertyId = :propertyId")
    suspend fun deleteFavorite(userId: Long, propertyId: Long)

    // Visit Requests
    @Query("SELECT * FROM visit_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getVisitsByUser(userId: Long): Flow<List<VisitRequest>>

    @Query("SELECT vr.* FROM visit_requests vr INNER JOIN properties p ON vr.propertyId = p.id WHERE (p.ownerId = :ownerId OR p.managedByAgentId = :ownerId) ORDER BY vr.createdAt DESC")
    fun getVisitsForOwner(ownerId: Long): Flow<List<VisitRequest>>

    @Query("SELECT * FROM visit_requests ORDER BY createdAt DESC")
    fun getAllVisits(): Flow<List<VisitRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitRequest(request: VisitRequest): Long

    @Query("UPDATE visit_requests SET status = :status WHERE id = :requestId")
    suspend fun updateVisitStatus(requestId: Long, status: VisitStatus)

    @Query("DELETE FROM visit_requests WHERE id = :requestId")
    suspend fun deleteVisitRequest(requestId: Long)

    // Conversations & Messages
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Query("SELECT c.* FROM conversations c INNER JOIN conversation_participants cp ON c.id = cp.conversationId WHERE cp.userId = :userId ORDER BY c.updatedAt DESC")
    fun getConversationsForUser(userId: Long): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE propertyId = :propertyId LIMIT 1")
    suspend fun getConversationForProperty(propertyId: Long): Conversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation): Long

    @Query("UPDATE conversations SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateConversationTimestamp(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ConversationParticipant)

    @Query("SELECT * FROM conversation_participants WHERE conversationId = :conversationId")
    suspend fun getParticipants(conversationId: Long): List<ConversationParticipant>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<Message>>

    @Query("SELECT * FROM messages ORDER BY createdAt DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessageForConversation(conversationId: Long): Message?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :currentUserId")
    suspend fun markMessagesAsRead(conversationId: Long, currentUserId: Long)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadNotificationCount(userId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: Long)

    // Reports
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report): Long

    @Query("UPDATE reports SET status = :status, resolvedBy = :adminId WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: Long, status: ReportStatus, adminId: Long)

    // Property Views
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPropertyView(view: PropertyView)
}
