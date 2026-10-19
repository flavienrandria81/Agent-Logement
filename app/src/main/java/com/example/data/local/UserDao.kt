package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountStatus
import com.example.data.model.Agency
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: Long): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id AND role = 'ADMIN' LIMIT 1")
    suspend fun getAdminUserByIdSync(id: Long): User?

    @Query("SELECT * FROM users WHERE email = :email AND role = 'ADMIN' LIMIT 1")
    suspend fun getAdminUserByEmailSync(email: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :id AND role = 'ADMIN' AND isSuspended = 0)")
    suspend fun isUserActiveAdmin(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :id AND role = 'ADMIN' AND isSuspended = 0)")
    fun observeIsUserActiveAdmin(id: Long): Flow<Boolean>

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE accountStatus = 'PENDING_APPROVAL' ORDER BY createdAt DESC")
    fun getPendingApprovalUsers(): Flow<List<User>>

    @Query("UPDATE users SET accountStatus = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: Long, status: AccountStatus)

    @Query("UPDATE users SET firstName = :firstName, lastName = :lastName, phone = :phone, bio = :bio WHERE id = :userId")
    suspend fun updateUserProfile(userId: Long, firstName: String, lastName: String, phone: String, bio: String)

    @Query("UPDATE users SET photoUrl = :photoUrl WHERE id = :userId")
    suspend fun updateUserPhoto(userId: Long, photoUrl: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)

    // Agencies
    @Query("SELECT * FROM agencies WHERE id = :id LIMIT 1")
    fun getAgencyById(id: Long): Flow<Agency?>

    @Query("SELECT * FROM agencies WHERE id = :id LIMIT 1")
    suspend fun getAgencyByIdSync(id: Long): Agency?

    @Query("SELECT * FROM agencies ORDER BY name ASC")
    fun getAllAgencies(): Flow<List<Agency>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgencies(agencies: List<Agency>)
}
