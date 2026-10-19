package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    USER,
    OWNER,
    AGENT,
    ADMIN
}

enum class AccountStatus(val label: String) {
    ACTIVE("Actif"),
    PENDING_APPROVAL("En attente d'approbation"),
    REJECTED("Refusé")
}

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["agencyId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Agency::class,
            parentColumns = ["id"],
            childColumns = ["agencyId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val passwordHash: String = "hashed_default_pwd",
    val role: UserRole = UserRole.USER,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val photoUrl: String = "",
    val bio: String = "",
    val emailVerified: Boolean = true,
    val isSuspended: Boolean = false,
    val agencyId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = "$firstName $lastName"

    val isApproved: Boolean
        get() = accountStatus == AccountStatus.ACTIVE
}

@Entity(tableName = "agencies")
data class Agency(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val logoUrl: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
