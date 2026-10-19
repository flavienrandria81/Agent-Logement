package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class VisitStatus(val label: String) {
    PENDING("En attente"),
    ACCEPTED("Confirmée"),
    REJECTED("Refusée"),
    CANCELLED("Annulée"),
    COMPLETED("Terminée")
}

enum class NotificationType {
    VISIT_REQUEST,
    MESSAGE,
    PROPERTY_STATUS,
    SYSTEM
}

enum class ReportStatus(val label: String) {
    PENDING("En cours d'examen"),
    RESOLVED("Résolu"),
    DISMISSED("Classé sans suite")
}

@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "propertyId"],
    indices = [Index(value = ["propertyId"])],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Favorite(
    val userId: Long,
    val propertyId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "visit_requests",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["propertyId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VisitRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val propertyId: Long,
    val requestedDate: String,
    val requestedTime: String,
    val message: String = "",
    val status: VisitStatus = VisitStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversation_participants",
    primaryKeys = ["conversationId", "userId"],
    indices = [Index(value = ["userId"])],
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ConversationParticipant(
    val conversationId: Long,
    val userId: Long
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["senderId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val senderId: Long,
    val content: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["userId"])],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.SYSTEM,
    val isRead: Boolean = false,
    val relatedEntityId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reports",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["propertyId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Report(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val propertyId: Long,
    val reason: String,
    val description: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val resolvedBy: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "property_views",
    indices = [
        Index(value = ["propertyId"]),
        Index(value = ["userId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PropertyView(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long,
    val userId: Long? = null,
    val viewedAt: Long = System.currentTimeMillis()
)
