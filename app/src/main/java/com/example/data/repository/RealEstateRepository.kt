package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.local.InteractionDao
import com.example.data.local.PropertyDao
import com.example.data.local.UserDao
import com.example.data.model.Agency
import com.example.data.model.Conversation
import com.example.data.model.ConversationParticipant
import com.example.data.model.ConversationWithDetails
import com.example.data.model.Favorite
import com.example.data.model.Message
import com.example.data.model.Notification
import com.example.data.model.NotificationType
import com.example.data.model.Property
import com.example.data.model.PropertyImage
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.PropertyView
import com.example.data.model.PropertyWithDetails
import com.example.data.model.Report
import com.example.data.model.ReportStatus
import com.example.data.model.ReportWithDetails
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.VisitRequest
import com.example.data.model.VisitRequestWithDetails
import com.example.data.model.VisitStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RealEstateRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userDao: UserDao = database.userDao()
    private val propertyDao: PropertyDao = database.propertyDao()
    private val interactionDao: InteractionDao = database.interactionDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            DatabaseInitializer.seedDatabaseIfEmpty(database)
        }
    }

    // Users
    fun getUserById(id: Long): Flow<User?> = userDao.getUserById(id)
    suspend fun getUserByIdSync(id: Long): User? = userDao.getUserByIdSync(id)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    fun getAgencyById(id: Long): Flow<Agency?> = userDao.getAgencyById(id)
    fun getAllAgencies(): Flow<List<Agency>> = userDao.getAllAgencies()

    // Properties
    suspend fun getPropertyByIdSync(id: Long): Property? = propertyDao.getPropertyByIdSync(id)
    fun getPublishedProperties(): Flow<List<PropertyWithDetails>> {
        return combine(
            propertyDao.getPropertiesByStatus(PropertyStatus.PUBLISHED),
            userDao.getAllUsers()
        ) { properties, users ->
            properties.map { prop ->
                val images = propertyDao.getImagesForPropertySync(prop.id)
                val owner = users.firstOrNull { it.id == prop.ownerId }
                val agent = prop.managedByAgentId?.let { agentId -> users.firstOrNull { it.id == agentId } }
                PropertyWithDetails(
                    property = prop,
                    images = images,
                    owner = owner,
                    agent = agent
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getAllPropertiesWithDetails(): Flow<List<PropertyWithDetails>> {
        return combine(
            propertyDao.getAllProperties(),
            userDao.getAllUsers()
        ) { properties, users ->
            properties.map { prop ->
                val images = propertyDao.getImagesForPropertySync(prop.id)
                val owner = users.firstOrNull { it.id == prop.ownerId }
                val agent = prop.managedByAgentId?.let { agentId -> users.firstOrNull { it.id == agentId } }
                PropertyWithDetails(
                    property = prop,
                    images = images,
                    owner = owner,
                    agent = agent
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPropertiesForOwner(ownerId: Long): Flow<List<PropertyWithDetails>> {
        return combine(
            propertyDao.getPropertiesByOwner(ownerId),
            userDao.getAllUsers()
        ) { properties, users ->
            properties.map { prop ->
                val images = propertyDao.getImagesForPropertySync(prop.id)
                val owner = users.firstOrNull { it.id == prop.ownerId }
                PropertyWithDetails(
                    property = prop,
                    images = images,
                    owner = owner
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPropertiesForAgent(agentId: Long): Flow<List<PropertyWithDetails>> {
        return combine(
            propertyDao.getPropertiesByAgent(agentId),
            userDao.getAllUsers()
        ) { properties, users ->
            properties.map { prop ->
                val images = propertyDao.getImagesForPropertySync(prop.id)
                val owner = users.firstOrNull { it.id == prop.ownerId }
                val agent = users.firstOrNull { it.id == agentId }
                PropertyWithDetails(
                    property = prop,
                    images = images,
                    owner = owner,
                    agent = agent
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPendingProperties(): Flow<List<PropertyWithDetails>> {
        return combine(
            propertyDao.getPropertiesByStatus(PropertyStatus.PENDING),
            userDao.getAllUsers()
        ) { properties, users ->
            properties.map { prop ->
                val images = propertyDao.getImagesForPropertySync(prop.id)
                val owner = users.firstOrNull { it.id == prop.ownerId }
                PropertyWithDetails(
                    property = prop,
                    images = images,
                    owner = owner
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPropertyDetails(propertyId: Long, currentUserId: Long? = null): Flow<PropertyWithDetails?> {
        return combine(
            propertyDao.getPropertyById(propertyId),
            userDao.getAllUsers()
        ) { prop, users ->
            if (prop == null) return@combine null
            val images = propertyDao.getImagesForPropertySync(prop.id)
            val owner = users.firstOrNull { it.id == prop.ownerId }
            val agent = prop.managedByAgentId?.let { agentId -> users.firstOrNull { it.id == agentId } }
            val isFav = if (currentUserId != null) {
                interactionDao.getFavorite(currentUserId, prop.id) != null
            } else false

            PropertyWithDetails(
                property = prop,
                images = images,
                owner = owner,
                agent = agent,
                isFavorite = isFav
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun recordPropertyView(propertyId: Long, userId: Long?) = withContext(Dispatchers.IO) {
        propertyDao.incrementViewCount(propertyId)
        interactionDao.insertPropertyView(PropertyView(propertyId = propertyId, userId = userId))
    }

    // Filter properties
    fun searchProperties(
        query: String?,
        propertyType: PropertyType?,
        transactionType: TransactionType?,
        maxPrice: Double?,
        minBedrooms: Int?,
        hasParking: Boolean?
    ): Flow<List<PropertyWithDetails>> {
        val cleanQuery = if (query.isNullOrBlank()) null else query.trim()
        return combine(
            propertyDao.filterProperties(
                city = cleanQuery,
                type = propertyType,
                transaction = transactionType,
                maxPrice = maxPrice,
                minBedrooms = minBedrooms,
                hasParking = hasParking
            ),
            userDao.getAllUsers()
        ) { properties, users ->
            properties.map { prop ->
                val images = propertyDao.getImagesForPropertySync(prop.id)
                val owner = users.firstOrNull { it.id == prop.ownerId }
                val agent = prop.managedByAgentId?.let { agentId -> users.firstOrNull { it.id == agentId } }
                PropertyWithDetails(
                    property = prop,
                    images = images,
                    owner = owner,
                    agent = agent
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // Creation & Update with Permissions
    suspend fun createProperty(
        userId: Long,
        title: String,
        description: String,
        price: Double,
        propertyType: PropertyType,
        transactionType: TransactionType,
        address: String,
        city: String,
        district: String,
        latitude: Double,
        longitude: Double,
        area: Double,
        bedrooms: Int,
        bathrooms: Int,
        rooms: Int,
        floor: Int,
        parking: Boolean,
        images: List<String>,
        submitForValidation: Boolean
    ): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        if (user.role == UserRole.USER) {
            return@withContext Result.failure(Exception("Seuls les propriétaires et agents peuvent publier un bien"))
        }
        if (user.role != UserRole.ADMIN && user.accountStatus != com.example.data.model.AccountStatus.ACTIVE) {
            return@withContext Result.failure(Exception("Votre compte est en attente d'approbation par un administrateur."))
        }

        val initialStatus = if (submitForValidation) PropertyStatus.PENDING else PropertyStatus.DRAFT

        val property = Property(
            ownerId = if (user.role == UserRole.OWNER) userId else userId,
            managedByAgentId = if (user.role == UserRole.AGENT) userId else null,
            title = title,
            description = description,
            price = price,
            propertyType = propertyType,
            transactionType = transactionType,
            status = initialStatus,
            address = address,
            city = city,
            district = district,
            latitude = latitude,
            longitude = longitude,
            area = area,
            bedrooms = bedrooms,
            bathrooms = bathrooms,
            rooms = rooms,
            floor = floor,
            parking = parking
        )

        val propId = propertyDao.insertProperty(property)

        val imageEntities = images.mapIndexed { index, url ->
            PropertyImage(
                propertyId = propId,
                imageUrl = url,
                isPrimary = index == 0,
                displayOrder = index
            )
        }
        if (imageEntities.isNotEmpty()) {
            propertyDao.insertPropertyImages(imageEntities)
        }

        Result.success(propId)
    }

    suspend fun updateProperty(
        userId: Long,
        propertyId: Long,
        title: String,
        description: String,
        price: Double,
        propertyType: PropertyType,
        transactionType: TransactionType,
        address: String,
        city: String,
        district: String,
        area: Double,
        bedrooms: Int,
        bathrooms: Int,
        rooms: Int,
        floor: Int,
        parking: Boolean,
        images: List<String>,
        submitForValidation: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        val existing = propertyDao.getPropertyByIdSync(propertyId) ?: return@withContext Result.failure(Exception("Bien introuvable"))

        // Strict role & ownership check: OWNER can only edit their own, AGENT only managed properties, ADMIN can edit all
        val canEdit = when (user.role) {
            UserRole.ADMIN -> true
            UserRole.OWNER -> existing.ownerId == userId
            UserRole.AGENT -> existing.managedByAgentId == userId
            UserRole.USER -> false
        }

        if (!canEdit) {
            return@withContext Result.failure(Exception("Accès refusé : vous n'êtes pas autorisé à modifier ce bien"))
        }

        val newStatus = if (submitForValidation) {
            PropertyStatus.PENDING
        } else if (existing.status == PropertyStatus.PUBLISHED) {
            // Keep published unless re-validation requested
            PropertyStatus.PUBLISHED
        } else {
            existing.status
        }

        val updated = existing.copy(
            title = title,
            description = description,
            price = price,
            propertyType = propertyType,
            transactionType = transactionType,
            status = newStatus,
            address = address,
            city = city,
            district = district,
            area = area,
            bedrooms = bedrooms,
            bathrooms = bathrooms,
            rooms = rooms,
            floor = floor,
            parking = parking,
            updatedAt = System.currentTimeMillis()
        )

        propertyDao.updateProperty(updated)

        if (images.isNotEmpty()) {
            propertyDao.deleteImagesForProperty(propertyId)
            val imageEntities = images.mapIndexed { index, url ->
                PropertyImage(
                    propertyId = propertyId,
                    imageUrl = url,
                    isPrimary = index == 0,
                    displayOrder = index
                )
            }
            propertyDao.insertPropertyImages(imageEntities)
        }

        Result.success(Unit)
    }

    suspend fun updatePropertyStatus(
        userId: Long,
        propertyId: Long,
        newStatus: PropertyStatus
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        val property = propertyDao.getPropertyByIdSync(propertyId) ?: return@withContext Result.failure(Exception("Bien introuvable"))

        // Permissions rule:
        // ADMIN can publish/reject/archive
        // OWNER/AGENT can submit (PENDING), mark RENTED, SOLD, DRAFT, ARCHIVED
        // USER cannot change status
        val isAllowed = when (user.role) {
            UserRole.ADMIN -> true
            UserRole.OWNER -> {
                property.ownerId == userId && (newStatus in listOf(PropertyStatus.PENDING, PropertyStatus.DRAFT, PropertyStatus.RENTED, PropertyStatus.SOLD, PropertyStatus.ARCHIVED))
            }
            UserRole.AGENT -> {
                property.managedByAgentId == userId && (newStatus in listOf(PropertyStatus.PENDING, PropertyStatus.DRAFT, PropertyStatus.RENTED, PropertyStatus.SOLD, PropertyStatus.ARCHIVED))
            }
            UserRole.USER -> false
        }

        if (!isAllowed) {
            return@withContext Result.failure(Exception("Permission refusée pour changer ce statut"))
        }

        propertyDao.updatePropertyStatus(propertyId, newStatus)

        // Notification if Admin approved/rejected
        if (user.role == UserRole.ADMIN) {
            val title = if (newStatus == PropertyStatus.PUBLISHED) "Annonce validée !" else "Statut de votre annonce mis à jour"
            val msg = if (newStatus == PropertyStatus.PUBLISHED) {
                "Votre annonce '${property.title}' a été validée par la modération et est maintenant visible."
            } else {
                "Le statut de votre annonce '${property.title}' est désormais : ${newStatus.label}"
            }
            interactionDao.insertNotification(
                Notification(
                    userId = property.ownerId,
                    title = title,
                    message = msg,
                    type = NotificationType.PROPERTY_STATUS,
                    relatedEntityId = propertyId
                )
            )
        }

        Result.success(Unit)
    }

    suspend fun deleteProperty(userId: Long, propertyId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        val property = propertyDao.getPropertyByIdSync(propertyId) ?: return@withContext Result.failure(Exception("Bien introuvable"))

        val canDelete = when (user.role) {
            UserRole.ADMIN -> true
            UserRole.OWNER -> property.ownerId == userId
            UserRole.AGENT -> property.managedByAgentId == userId
            UserRole.USER -> false
        }

        if (!canDelete) {
            return@withContext Result.failure(Exception("Vous n'avez pas le droit de supprimer ce bien"))
        }

        propertyDao.deleteProperty(propertyId)
        Result.success(Unit)
    }

    // Favorites
    suspend fun toggleFavorite(userId: Long, propertyId: Long) = withContext(Dispatchers.IO) {
        val existing = interactionDao.getFavorite(userId, propertyId)
        if (existing != null) {
            interactionDao.deleteFavorite(userId, propertyId)
        } else {
            interactionDao.insertFavorite(Favorite(userId = userId, propertyId = propertyId))
        }
    }

    fun getFavoritesForUser(userId: Long): Flow<List<PropertyWithDetails>> {
        return combine(
            interactionDao.getFavoritesForUser(userId),
            propertyDao.getAllProperties(),
            userDao.getAllUsers()
        ) { favs, properties, users ->
            val favPropIds = favs.map { it.propertyId }.toSet()
            properties.filter { it.id in favPropIds }.map { prop ->
                val images = propertyDao.getImagesForPropertySync(prop.id)
                val owner = users.firstOrNull { it.id == prop.ownerId }
                val agent = prop.managedByAgentId?.let { agentId -> users.firstOrNull { it.id == agentId } }
                PropertyWithDetails(
                    property = prop,
                    images = images,
                    owner = owner,
                    agent = agent,
                    isFavorite = true
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // Visits
    suspend fun requestVisit(
        userId: Long,
        propertyId: Long,
        requestedDate: String,
        requestedTime: String,
        message: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        val property = propertyDao.getPropertyByIdSync(propertyId) ?: return@withContext Result.failure(Exception("Bien introuvable"))

        val req = VisitRequest(
            userId = userId,
            propertyId = propertyId,
            requestedDate = requestedDate,
            requestedTime = requestedTime,
            message = message,
            status = VisitStatus.PENDING
        )
        val id = interactionDao.insertVisitRequest(req)

        // Notify owner and agent
        val targetNotifyId = property.managedByAgentId ?: property.ownerId
        interactionDao.insertNotification(
            Notification(
                userId = targetNotifyId,
                title = "Nouvelle demande de visite",
                message = "${user.fullName} souhaite visiter ${property.title} le $requestedDate à $requestedTime.",
                type = NotificationType.VISIT_REQUEST,
                relatedEntityId = id
            )
        )

        Result.success(id)
    }

    suspend fun updateVisitStatus(
        userId: Long,
        requestId: Long,
        status: VisitStatus
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        val allVisits = interactionDao.getAllVisits().firstOrNull() ?: emptyList()
        val visit = allVisits.firstOrNull { it.id == requestId } ?: return@withContext Result.failure(Exception("Demande introuvable"))
        val property = propertyDao.getPropertyByIdSync(visit.propertyId) ?: return@withContext Result.failure(Exception("Bien introuvable"))

        // Can update: Owner/Agent of the property, or Admin, or the Requester themselves (can only CANCEL)
        val isManager = property.ownerId == userId || property.managedByAgentId == userId || user.role == UserRole.ADMIN
        val isRequester = visit.userId == userId

        if (!isManager && !(isRequester && status == VisitStatus.CANCELLED)) {
            return@withContext Result.failure(Exception("Action non autorisée"))
        }

        interactionDao.updateVisitStatus(requestId, status)

        // Notify seeker
        if (isManager) {
            val statusLabel = when (status) {
                VisitStatus.ACCEPTED -> "acceptée !"
                VisitStatus.REJECTED -> "refusée"
                VisitStatus.COMPLETED -> "effectuée"
                VisitStatus.CANCELLED -> "annulée"
                else -> "mise à jour"
            }
            interactionDao.insertNotification(
                Notification(
                    userId = visit.userId,
                    title = "Visite $statusLabel",
                    message = "Votre demande de visite pour '${property.title}' a été $statusLabel.",
                    type = NotificationType.VISIT_REQUEST,
                    relatedEntityId = requestId
                )
            )
        }

        Result.success(Unit)
    }

    fun getAllVisits(): Flow<List<VisitRequest>> = interactionDao.getAllVisits()

    fun getAllMessages(): Flow<List<Message>> = interactionDao.getAllMessages()

    fun getVisitsWithDetails(userId: Long, role: UserRole): Flow<List<VisitRequestWithDetails>> {
        val visitsFlow = when (role) {
            UserRole.USER -> interactionDao.getVisitsByUser(userId)
            UserRole.OWNER, UserRole.AGENT -> interactionDao.getVisitsForOwner(userId)
            UserRole.ADMIN -> interactionDao.getAllVisits()
        }

        return combine(
            visitsFlow,
            propertyDao.getAllProperties(),
            userDao.getAllUsers()
        ) { visits, properties, users ->
            visits.mapNotNull { visit ->
                val prop = properties.firstOrNull { it.id == visit.propertyId } ?: return@mapNotNull null
                val requester = users.firstOrNull { it.id == visit.userId } ?: return@mapNotNull null
                val images = propertyDao.getImagesForPropertySync(prop.id)
                val imgUrl = images.firstOrNull { it.isPrimary }?.imageUrl
                    ?: images.firstOrNull()?.imageUrl
                    ?: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=400&q=80"

                VisitRequestWithDetails(
                    request = visit,
                    property = prop,
                    requester = requester,
                    propertyImageUrl = imgUrl
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // Messaging
    suspend fun getOrCreateConversation(currentUserId: Long, propertyId: Long, recipientId: Long): Long = withContext(Dispatchers.IO) {
        val existing = interactionDao.getConversationForProperty(propertyId)
        if (existing != null) {
            val participants = interactionDao.getParticipants(existing.id)
            if (participants.any { it.userId == currentUserId }) {
                return@withContext existing.id
            }
        }

        val convId = interactionDao.insertConversation(Conversation(propertyId = propertyId))
        interactionDao.insertParticipant(ConversationParticipant(conversationId = convId, userId = currentUserId))
        interactionDao.insertParticipant(ConversationParticipant(conversationId = convId, userId = recipientId))
        convId
    }

    suspend fun sendMessage(conversationId: Long, senderId: Long, content: String): Long = withContext(Dispatchers.IO) {
        val msgId = interactionDao.insertMessage(
            Message(
                conversationId = conversationId,
                senderId = senderId,
                content = content
            )
        )
        interactionDao.updateConversationTimestamp(conversationId)

        // Notify participants
        val participants = interactionDao.getParticipants(conversationId)
        val sender = userDao.getUserByIdSync(senderId)
        participants.filter { it.userId != senderId }.forEach { p ->
            interactionDao.insertNotification(
                Notification(
                    userId = p.userId,
                    title = "Nouveau message de ${sender?.fullName ?: "Utilisateur"}",
                    message = content.take(60),
                    type = NotificationType.MESSAGE,
                    relatedEntityId = conversationId
                )
            )
        }

        msgId
    }

    fun getConversationsForUser(userId: Long): Flow<List<ConversationWithDetails>> {
        return combine(
            interactionDao.getConversationsForUser(userId),
            propertyDao.getAllProperties(),
            userDao.getAllUsers()
        ) { convs, properties, users ->
            convs.mapNotNull { conv ->
                val prop = properties.firstOrNull { it.id == conv.propertyId } ?: return@mapNotNull null
                val participants = interactionDao.getParticipants(conv.id)
                val otherParticipantId = participants.firstOrNull { it.userId != userId }?.userId
                    ?: return@mapNotNull null
                val otherUser = users.firstOrNull { it.id == otherParticipantId } ?: return@mapNotNull null
                val lastMsg = interactionDao.getLastMessageForConversation(conv.id)

                ConversationWithDetails(
                    conversation = conv,
                    property = prop,
                    otherUser = otherUser,
                    lastMessage = lastMsg
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getMessagesForConversation(conversationId: Long): Flow<List<Message>> {
        return interactionDao.getMessagesForConversation(conversationId)
    }

    suspend fun markMessagesAsRead(conversationId: Long, currentUserId: Long) = withContext(Dispatchers.IO) {
        interactionDao.markMessagesAsRead(conversationId, currentUserId)
    }

    // Reports
    suspend fun createReport(userId: Long, propertyId: Long, reason: String, description: String): Long = withContext(Dispatchers.IO) {
        interactionDao.insertReport(
            Report(
                userId = userId,
                propertyId = propertyId,
                reason = reason,
                description = description
            )
        )
    }

    fun getAllReportsWithDetails(): Flow<List<ReportWithDetails>> {
        return combine(
            interactionDao.getAllReports(),
            propertyDao.getAllProperties(),
            userDao.getAllUsers()
        ) { reports, properties, users ->
            reports.mapNotNull { r ->
                val prop = properties.firstOrNull { it.id == r.propertyId } ?: return@mapNotNull null
                val reportingUser = users.firstOrNull { it.id == r.userId } ?: return@mapNotNull null
                val resolver = r.resolvedBy?.let { resId -> users.firstOrNull { it.id == resId } }
                ReportWithDetails(
                    report = r,
                    property = prop,
                    reportingUser = reportingUser,
                    resolverUser = resolver
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun resolveReport(adminId: Long, reportId: Long, status: ReportStatus): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByIdSync(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Seul un administrateur peut modérer les signalements"))
        }
        interactionDao.updateReportStatus(reportId, status, adminId)
        Result.success(Unit)
    }

    // Notifications
    fun getNotificationsForUser(userId: Long): Flow<List<Notification>> = interactionDao.getNotificationsForUser(userId)
    fun getUnreadNotificationCount(userId: Long): Flow<Int> = interactionDao.getUnreadNotificationCount(userId)
    suspend fun markNotificationRead(id: Long) = withContext(Dispatchers.IO) { interactionDao.markNotificationAsRead(id) }
    suspend fun markAllNotificationsRead(userId: Long) = withContext(Dispatchers.IO) { interactionDao.markAllNotificationsAsRead(userId) }

    // Admin User Management
    suspend fun toggleUserVerification(adminId: Long, targetUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByIdSync(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Action réservée aux administrateurs"))
        }
        val target = userDao.getUserByIdSync(targetUserId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        userDao.updateUser(target.copy(emailVerified = !target.emailVerified))
        Result.success(Unit)
    }

    suspend fun toggleUserSuspension(adminId: Long, targetUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByIdSync(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Action réservée aux administrateurs"))
        }
        if (adminId == targetUserId) {
            return@withContext Result.failure(Exception("Impossible de suspendre son propre compte administrateur"))
        }
        val target = userDao.getUserByIdSync(targetUserId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        userDao.updateUser(target.copy(isSuspended = !target.isSuspended))
        Result.success(Unit)
    }

    suspend fun updateUserRole(adminId: Long, targetUserId: Long, newRole: UserRole): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByIdSync(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Action réservée aux administrateurs"))
        }
        val target = userDao.getUserByIdSync(targetUserId) ?: return@withContext Result.failure(Exception("Utilisateur non trouvé"))
        userDao.updateUser(target.copy(role = newRole))
        Result.success(Unit)
    }

    suspend fun deleteUser(adminId: Long, targetUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByIdSync(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Action réservée aux administrateurs"))
        }
        if (adminId == targetUserId) {
            return@withContext Result.failure(Exception("Impossible de supprimer son propre compte administrateur"))
        }
        userDao.deleteUserById(targetUserId)
        Result.success(Unit)
    }

    suspend fun adminDeleteReportedProperty(adminId: Long, reportId: Long, propertyId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByIdSync(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Action réservée aux administrateurs"))
        }
        propertyDao.deleteProperty(propertyId)
        interactionDao.updateReportStatus(reportId, ReportStatus.RESOLVED, adminId)
        Result.success(Unit)
    }

    // Pending User Accounts (Owner & Agent approval by ADMIN)
    fun getPendingApprovalUsers(): Flow<List<User>> = userDao.getPendingApprovalUsers()

    suspend fun approveUser(adminId: Long, targetUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByIdSync(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Action réservée aux administrateurs"))
        }
        userDao.updateUserStatus(targetUserId, com.example.data.model.AccountStatus.ACTIVE)
        Result.success(Unit)
    }

    suspend fun rejectUser(adminId: Long, targetUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = userDao.getUserByIdSync(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Action réservée aux administrateurs"))
        }
        userDao.updateUserStatus(targetUserId, com.example.data.model.AccountStatus.REJECTED)
        Result.success(Unit)
    }

    suspend fun updateUserProfile(userId: Long, firstName: String, lastName: String, phone: String, bio: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (firstName.isBlank() || lastName.isBlank()) {
            return@withContext Result.failure(Exception("Prénom et nom requis"))
        }
        userDao.updateUserProfile(userId, firstName.trim(), lastName.trim(), phone.trim(), bio.trim())
        Result.success(Unit)
    }

    suspend fun updateUserPhoto(userId: Long, photoUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        userDao.updateUserPhoto(userId, photoUrl)
        Result.success(Unit)
    }
}
