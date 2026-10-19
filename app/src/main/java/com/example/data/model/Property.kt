package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

enum class PropertyType(val label: String) {
    APARTMENT("Appartement"),
    HOUSE("Maison"),
    VILLA("Villa"),
    STUDIO("Studio"),
    COMMERCIAL("Local commercial"),
    LAND("Terrain")
}

enum class TransactionType(val label: String) {
    RENT("Location"),
    SALE("Vente")
}

enum class PropertyStatus(val label: String) {
    DRAFT("Brouillon"),
    PENDING("En attente"),
    PUBLISHED("Publié"),
    REJECTED("Refusé"),
    RENTED("Loué"),
    SOLD("Vendu"),
    ARCHIVED("Archivé")
}

@Entity(
    tableName = "properties",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["managedByAgentId"]),
        Index(value = ["city"]),
        Index(value = ["status"]),
        Index(value = ["transactionType"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["managedByAgentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Property(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    val managedByAgentId: Long? = null,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String = "€",
    val propertyType: PropertyType = PropertyType.APARTMENT,
    val transactionType: TransactionType = TransactionType.RENT,
    val status: PropertyStatus = PropertyStatus.DRAFT,
    val address: String = "",
    val city: String = "",
    val district: String = "",
    val latitude: Double = 48.8566,
    val longitude: Double = 2.3522,
    val area: Double = 0.0,
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val rooms: Int = 0,
    val floor: Int = 0,
    val parking: Boolean = false,
    val viewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val formattedPrice: String
        get() {
            val formatter = NumberFormat.getIntegerInstance(Locale.FRANCE)
            val formatted = formatter.format(price.toInt())
            return if (transactionType == TransactionType.RENT) "$formatted € / mois" else "$formatted €"
        }
}

@Entity(
    tableName = "property_images",
    indices = [Index(value = ["propertyId"])],
    foreignKeys = [
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PropertyImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long,
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0
)
