package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Property
import com.example.data.model.PropertyImage
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties WHERE status = :status ORDER BY createdAt DESC")
    fun getPropertiesByStatus(status: PropertyStatus): Flow<List<Property>>

    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getPropertiesByOwner(ownerId: Long): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE ownerId = :ownerId")
    suspend fun getPropertiesByOwnerSync(ownerId: Long): List<Property>

    @Query("SELECT * FROM properties WHERE managedByAgentId = :agentId ORDER BY createdAt DESC")
    fun getPropertiesByAgent(agentId: Long): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE managedByAgentId = :agentId")
    suspend fun getPropertiesByAgentSync(agentId: Long): List<Property>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    fun getPropertyById(id: Long): Flow<Property?>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    suspend fun getPropertyByIdSync(id: Long): Property?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<Property>)

    @Update
    suspend fun updateProperty(property: Property)

    @Query("UPDATE properties SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePropertyStatus(id: Long, status: PropertyStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE properties SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementViewCount(id: Long)

    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun deleteProperty(id: Long)

    // Images
    @Query("SELECT * FROM property_images WHERE propertyId = :propertyId ORDER BY displayOrder ASC")
    fun getImagesForProperty(propertyId: Long): Flow<List<PropertyImage>>

    @Query("SELECT * FROM property_images WHERE propertyId = :propertyId ORDER BY displayOrder ASC")
    suspend fun getImagesForPropertySync(propertyId: Long): List<PropertyImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPropertyImage(image: PropertyImage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPropertyImages(images: List<PropertyImage>)

    @Query("DELETE FROM property_images WHERE propertyId = :propertyId")
    suspend fun deleteImagesForProperty(propertyId: Long)

    // Filter Query
    @Query("""
        SELECT * FROM properties 
        WHERE status = 'PUBLISHED'
        AND (:city IS NULL OR city LIKE '%' || :city || '%' OR district LIKE '%' || :city || '%' OR address LIKE '%' || :city || '%' OR title LIKE '%' || :city || '%')
        AND (:type IS NULL OR propertyType = :type)
        AND (:transaction IS NULL OR transactionType = :transaction)
        AND (:maxPrice IS NULL OR price <= :maxPrice)
        AND (:minBedrooms IS NULL OR bedrooms >= :minBedrooms)
        AND (:hasParking IS NULL OR parking = :hasParking)
        ORDER BY createdAt DESC
    """)
    fun filterProperties(
        city: String? = null,
        type: PropertyType? = null,
        transaction: TransactionType? = null,
        maxPrice: Double? = null,
        minBedrooms: Int? = null,
        hasParking: Boolean? = null
    ): Flow<List<Property>>
}
