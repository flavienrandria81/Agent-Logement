package com.example.data.model

data class PropertyWithDetails(
    val property: Property,
    val images: List<PropertyImage> = emptyList(),
    val owner: User? = null,
    val agent: User? = null,
    val isFavorite: Boolean = false
) {
    val primaryImageUrl: String
        get() = images.firstOrNull { it.isPrimary }?.imageUrl
            ?: images.firstOrNull()?.imageUrl
            ?: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80"
}

data class VisitRequestWithDetails(
    val request: VisitRequest,
    val property: Property,
    val requester: User,
    val propertyImageUrl: String
)

data class ConversationWithDetails(
    val conversation: Conversation,
    val property: Property,
    val otherUser: User,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0
)

data class ReportWithDetails(
    val report: Report,
    val property: Property,
    val reportingUser: User,
    val resolverUser: User? = null
)

data class AdminStats(
    val totalUsersCount: Int = 0,
    val ownersCount: Int = 0,
    val agentsCount: Int = 0,
    val regularUsersCount: Int = 0,
    val adminsCount: Int = 0,
    val totalProperties: Int = 0,
    val housesCount: Int = 0,
    val apartmentsCount: Int = 0,
    val landsCount: Int = 0,
    val commercialCount: Int = 0,
    val forRentCount: Int = 0,
    val forSaleCount: Int = 0,
    val pendingCount: Int = 0,
    val publishedCount: Int = 0,
    val rejectedCount: Int = 0,
    val rentedCount: Int = 0,
    val soldCount: Int = 0,
    val archivedCount: Int = 0,
    val visitRequestsCount: Int = 0,
    val rentalAndPurchaseRequestsCount: Int = 0,
    val totalReportsCount: Int = 0,
    val totalViewsCount: Int = 0
)

data class OwnerStats(
    val totalProperties: Int = 0,
    val publishedCount: Int = 0,
    val pendingCount: Int = 0,
    val rentedOrSoldCount: Int = 0,
    val totalViews: Int = 0,
    val pendingVisitsCount: Int = 0
)

data class AgentStats(
    val managedPropertiesCount: Int = 0,
    val publishedCount: Int = 0,
    val pendingVisitsCount: Int = 0,
    val activeConversationsCount: Int = 0,
    val totalViews: Int = 0
)
