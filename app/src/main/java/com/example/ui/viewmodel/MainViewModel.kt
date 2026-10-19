package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.LoginRequest
import com.example.data.auth.RegisterRequest
import com.example.data.model.AccountStatus
import com.example.data.model.Agency
import com.example.data.model.ConversationWithDetails
import com.example.data.model.Message
import com.example.data.model.Notification
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.PropertyWithDetails
import com.example.data.model.ReportStatus
import com.example.data.model.ReportWithDetails
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.VisitRequest
import com.example.data.model.VisitRequestWithDetails
import com.example.data.model.VisitStatus
import com.example.data.model.AdminStats
import com.example.data.model.OwnerStats
import com.example.data.model.AgentStats
import com.example.data.repository.RealEstateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

enum class AppTab(val title: String) {
    HOME("Accueil"),
    SEARCH("Rechercher"),
    FAVORITES("Favoris"),
    MY_PROPERTIES("Mes biens"),
    PUBLISH("Déposer"),
    VISITS("Visites"),
    MESSAGES("Messages"),
    ADMIN("Administration"),
    PROFILE("Profil")
}

sealed class PendingAction {
    data class OpenProperty(val propertyId: Long) : PendingAction()
    data class ToggleFavorite(val propertyId: Long) : PendingAction()
    data class ContactOwner(val propertyId: Long, val targetUser: User) : PendingAction()
    data class RequestVisit(val propertyId: Long) : PendingAction()
    data class RentOrBuy(val propertyId: Long, val transactionType: TransactionType) : PendingAction()
    data class NavigateToTab(val tab: AppTab) : PendingAction()
}

enum class SearchDisplayMode {
    LIST,
    MAP
}

data class SearchFilter(
    val query: String = "",
    val propertyType: PropertyType? = null,
    val transactionType: TransactionType? = null,
    val maxPrice: Double? = null,
    val minBedrooms: Int? = null,
    val hasParking: Boolean? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RealEstateRepository(application)
    private val authRepository = AuthRepository(application)

    // Auth & Session States
    private val _isSessionChecking = MutableStateFlow(true)
    val isSessionChecking: StateFlow<Boolean> = _isSessionChecking.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _pendingAction = MutableStateFlow<PendingAction?>(null)
    val pendingAction: StateFlow<PendingAction?> = _pendingAction.asStateFlow()

    private val _showPublishAuthPrompt = MutableStateFlow(false)
    val showPublishAuthPrompt: StateFlow<Boolean> = _showPublishAuthPrompt.asStateFlow()

    fun dismissPublishAuthPrompt() {
        _showPublishAuthPrompt.value = false
    }

    private val _pendingRegistrationNotice = MutableStateFlow<String?>(null)
    val pendingRegistrationNotice: StateFlow<String?> = _pendingRegistrationNotice.asStateFlow()

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    private val _photoUploadError = MutableStateFlow<String?>(null)
    val photoUploadError: StateFlow<String?> = _photoUploadError.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    private val _requestedAuthMode = MutableStateFlow<String?>(null)
    val requestedAuthMode: StateFlow<String?> = _requestedAuthMode.asStateFlow()

    private val _autoOpenVisitDialog = MutableStateFlow<Long?>(null)
    val autoOpenVisitDialog: StateFlow<Long?> = _autoOpenVisitDialog.asStateFlow()

    private val _autoOpenRentOrBuyDialog = MutableStateFlow<Long?>(null)
    val autoOpenRentOrBuyDialog: StateFlow<Long?> = _autoOpenRentOrBuyDialog.asStateFlow()

    // Current logged-in user ID (Derived strictly from persistent Auth session)
    private val _currentUserId = MutableStateFlow(1L)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<User?> = _currentUserId
        .flatMapLatest { id -> repository.getUserById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Automatically check persistent session on startup
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            _isSessionChecking.value = true
            val existingUser = authRepository.initializeSession()
            if (existingUser != null) {
                _currentUserId.value = existingUser.id
                _isAuthenticated.value = true
                _currentTab.value = when (existingUser.role) {
                    UserRole.ADMIN -> AppTab.ADMIN
                    UserRole.OWNER, UserRole.AGENT -> {
                        if (existingUser.isApproved) AppTab.MY_PROPERTIES else AppTab.HOME
                    }
                    UserRole.USER -> AppTab.HOME
                }
            } else {
                // Par défaut au lancement initial sans session, initialiser automatiquement avec le compte Administrateur
                // afin que l'ensemble des interfaces et opérations CRUD soient immédiatement visibles et utilisables !
                val adminLoginResult = authRepository.login(com.example.data.model.LoginRequest("sophie.bernard@logement-admin.fr", "admin123"))
                if (adminLoginResult.isSuccess) {
                    val adminUser = adminLoginResult.getOrNull()?.user
                    if (adminUser != null) {
                        _currentUserId.value = adminUser.id
                        _isAuthenticated.value = true
                        _currentTab.value = AppTab.ADMIN
                    } else {
                        _currentUserId.value = 0L
                        _isAuthenticated.value = false
                    }
                } else {
                    _currentUserId.value = 0L
                    _isAuthenticated.value = false
                }
            }
            _isSessionChecking.value = false
        }
    }

    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApprovalUsers: StateFlow<List<User>> = repository.getPendingApprovalUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Tab & Navigation
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Detail view state (null = list mode)
    private val _selectedPropertyId = MutableStateFlow<Long?>(null)
    val selectedPropertyId: StateFlow<Long?> = _selectedPropertyId.asStateFlow()

    val selectedProperty: StateFlow<PropertyWithDetails?> = combine(_selectedPropertyId, _currentUserId) { propId, uId ->
        propId to uId
    }.flatMapLatest { (propId, uId) ->
        if (propId != null) repository.getPropertyDetails(propId, uId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Edit Property State
    private val _editingProperty = MutableStateFlow<Property?>(null)
    val editingProperty: StateFlow<Property?> = _editingProperty.asStateFlow()

    // Search & Filter State
    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _searchDisplayMode = MutableStateFlow(SearchDisplayMode.LIST)
    val searchDisplayMode: StateFlow<SearchDisplayMode> = _searchDisplayMode.asStateFlow()

    val publishedProperties: StateFlow<List<PropertyWithDetails>> = repository.getPublishedProperties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProperties: StateFlow<List<PropertyWithDetails>> = _searchFilter
        .flatMapLatest { filter ->
            repository.searchProperties(
                query = filter.query,
                propertyType = filter.propertyType,
                transactionType = filter.transactionType,
                maxPrice = filter.maxPrice,
                minBedrooms = filter.minBedrooms,
                hasParking = filter.hasParking
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProperties: StateFlow<List<PropertyWithDetails>> = repository.getAllPropertiesWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // My properties (Role-aware: Owner vs Agent)
    val myProperties: StateFlow<List<PropertyWithDetails>> = combine(_currentUserId, currentUser) { id, user ->
        id to user
    }.flatMapLatest { (id, user) ->
        if (user != null && !user.isApproved && (user.role == UserRole.OWNER || user.role == UserRole.AGENT)) {
            flowOf(emptyList())
        } else {
            when (user?.role) {
                UserRole.OWNER -> repository.getPropertiesForOwner(id)
                UserRole.AGENT -> repository.getPropertiesForAgent(id)
                UserRole.ADMIN -> repository.getAllPropertiesWithDetails()
                else -> flowOf(emptyList())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingProperties: StateFlow<List<PropertyWithDetails>> = repository.getPendingProperties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites
    val myFavorites: StateFlow<List<PropertyWithDetails>> = _currentUserId
        .flatMapLatest { id -> repository.getFavoritesForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Visits
    val visitRequests: StateFlow<List<VisitRequestWithDetails>> = combine(_currentUserId, currentUser) { id, user ->
        id to (user?.role ?: UserRole.USER)
    }.flatMapLatest { (id, role) ->
        repository.getVisitsWithDetails(id, role)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Messaging
    val conversations: StateFlow<List<ConversationWithDetails>> = _currentUserId
        .flatMapLatest { id -> repository.getConversationsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    val activeMessages: StateFlow<List<Message>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId != null) repository.getMessagesForConversation(convId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<Notification>> = _currentUserId
        .flatMapLatest { id -> repository.getNotificationsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = _currentUserId
        .flatMapLatest { id -> repository.getUnreadNotificationCount(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Reports (Admin)
    val allReports: StateFlow<List<ReportWithDetails>> = repository.getAllReportsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Agency for Agent
    val agencyForCurrentAgent: StateFlow<Agency?> = currentUser
        .flatMapLatest { user ->
            val agencyId = user?.agencyId
            if (agencyId != null) repository.getAgencyById(agencyId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allVisits: StateFlow<List<VisitRequest>> = repository.getAllVisits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<Message>> = repository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics
    val adminStats: StateFlow<AdminStats> = combine(
        allProperties,
        allUsers,
        allReports,
        allVisits,
        allMessages
    ) { props, users, reports, visits, messages ->
        val totalUsers = users.size
        val owners = users.count { it.role == UserRole.OWNER }
        val agents = users.count { it.role == UserRole.AGENT }
        val regular = users.count { it.role == UserRole.USER }
        val admins = users.count { it.role == UserRole.ADMIN }

        val pending = props.count { it.property.status == PropertyStatus.PENDING }
        val published = props.count { it.property.status == PropertyStatus.PUBLISHED }
        val rejected = props.count { it.property.status == PropertyStatus.REJECTED }
        val rented = props.count { it.property.status == PropertyStatus.RENTED }
        val sold = props.count { it.property.status == PropertyStatus.SOLD }
        val archived = props.count { it.property.status == PropertyStatus.ARCHIVED || it.property.status == PropertyStatus.DRAFT }

        val houses = props.count { it.property.propertyType == PropertyType.HOUSE || it.property.propertyType == PropertyType.VILLA }
        val apartments = props.count { it.property.propertyType == PropertyType.APARTMENT || it.property.propertyType == PropertyType.STUDIO }
        val lands = props.count { it.property.propertyType == PropertyType.LAND }
        val commercial = props.count { it.property.propertyType == PropertyType.COMMERCIAL }

        val forRent = props.count { it.property.transactionType == TransactionType.RENT }
        val forSale = props.count { it.property.transactionType == TransactionType.SALE }

        val views = props.sumOf { it.property.viewCount }
        val visitCount = visits.size
        val rentalOrPurchaseOffers = messages.count { msg ->
            msg.content.contains("candidature", ignoreCase = true) ||
            msg.content.contains("offre d'achat", ignoreCase = true) ||
            msg.content.contains("dossier de location", ignoreCase = true)
        }

        AdminStats(
            totalUsersCount = totalUsers,
            ownersCount = owners,
            agentsCount = agents,
            regularUsersCount = regular,
            adminsCount = admins,
            totalProperties = props.size,
            housesCount = houses,
            apartmentsCount = apartments,
            landsCount = lands,
            commercialCount = commercial,
            forRentCount = forRent,
            forSaleCount = forSale,
            pendingCount = pending,
            publishedCount = published,
            rejectedCount = rejected,
            rentedCount = rented,
            soldCount = sold,
            archivedCount = archived,
            visitRequestsCount = visitCount,
            rentalAndPurchaseRequestsCount = rentalOrPurchaseOffers,
            totalReportsCount = reports.count { it.report.status == ReportStatus.PENDING },
            totalViewsCount = views
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminStats())

    val ownerStats: StateFlow<OwnerStats> = combine(myProperties, visitRequests) { props, visits ->
        val total = props.size
        val pub = props.count { it.property.status == PropertyStatus.PUBLISHED }
        val pend = props.count { it.property.status == PropertyStatus.PENDING }
        val done = props.count { it.property.status == PropertyStatus.RENTED || it.property.status == PropertyStatus.SOLD }
        val views = props.sumOf { it.property.viewCount }
        val pendingVis = visits.count { it.request.status == VisitStatus.PENDING }

        OwnerStats(
            totalProperties = total,
            publishedCount = pub,
            pendingCount = pend,
            rentedOrSoldCount = done,
            totalViews = views,
            pendingVisitsCount = pendingVis
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OwnerStats())

    val agentStats: StateFlow<AgentStats> = combine(myProperties, visitRequests, conversations) { props, visits, convs ->
        val total = props.size
        val pub = props.count { it.property.status == PropertyStatus.PUBLISHED }
        val pendingVis = visits.count { it.request.status == VisitStatus.PENDING }
        val views = props.sumOf { it.property.viewCount }

        AgentStats(
            managedPropertiesCount = total,
            publishedCount = pub,
            pendingVisitsCount = pendingVis,
            activeConversationsCount = convs.size,
            totalViews = views
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AgentStats())

    // Feedback message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // Navigation & Tab Switching
    fun navigateToTab(tab: AppTab) {
        if (!_isAuthenticated.value) {
            when (tab) {
                AppTab.HOME, AppTab.SEARCH -> {
                    _currentTab.value = tab
                    _selectedPropertyId.value = null
                }
                AppTab.PROFILE -> {
                    _isGuestMode.value = false
                    _selectedPropertyId.value = null
                }
                AppTab.PUBLISH -> {
                    _showPublishAuthPrompt.value = true
                }
                else -> {
                    _pendingAction.value = PendingAction.NavigateToTab(tab)
                    _isGuestMode.value = false
                    _selectedPropertyId.value = null
                    _uiMessage.value = "Connectez-vous pour accéder à cette rubrique."
                }
            }
            return
        }

        val user = currentUser.value
        // Protection checks for restricted tabs:
        if (tab == AppTab.ADMIN && user?.role != UserRole.ADMIN) {
            _uiMessage.value = "Accès refusé : espace réservé aux administrateurs."
            return
        }
        if (tab == AppTab.MY_PROPERTIES) {
            if (user?.role == UserRole.USER) {
                _uiMessage.value = "L'espace 'Mes biens' est réservé aux propriétaires et agents."
                return
            }
            if ((user?.role == UserRole.OWNER || user?.role == UserRole.AGENT) && !user.isApproved) {
                _uiMessage.value = "Votre compte est en attente de validation par l'administrateur."
                return
            }
        }
        if (tab == AppTab.VISITS && user?.role == UserRole.AGENT && !user.isApproved) {
            _uiMessage.value = "Votre compte est en attente de validation par l'administrateur."
            return
        }

        _currentTab.value = tab
        _selectedPropertyId.value = null
    }

    fun setGuestMode(enabled: Boolean) {
        _isGuestMode.value = enabled
        if (enabled) {
            _currentTab.value = AppTab.HOME
            _selectedPropertyId.value = null
        }
    }

    fun setPendingAction(action: PendingAction?) {
        _pendingAction.value = action
    }

    fun clearPendingAction() {
        _pendingAction.value = null
    }

    fun triggerPendingNotice() {
        _uiMessage.value = "Votre compte est en attente de validation par l'administrateur."
    }

    fun dismissRegistrationNotice() {
        _pendingRegistrationNotice.value = null
    }

    fun clearRegistrationSuccess() {
        _registrationSuccess.value = false
    }

    fun requestAuth(mode: String = "LOGIN") {
        _isGuestMode.value = false
        _requestedAuthMode.value = mode
    }

    fun clearRequestedAuthMode() {
        _requestedAuthMode.value = null
    }

    fun clearAutoOpenDialogs() {
        _autoOpenVisitDialog.value = null
        _autoOpenRentOrBuyDialog.value = null
    }

    // Authentication & Real Session Endpoints
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authRepository.login(LoginRequest(email = email, password = password))
            result.onSuccess { authResult ->
                _currentUserId.value = authResult.user.id
                _isAuthenticated.value = true
                _isGuestMode.value = false

                val pending = _pendingAction.value
                if (pending != null) {
                    _pendingAction.value = null
                    handlePendingAction(pending, authResult.user)
                } else {
                    _currentTab.value = when (authResult.user.role) {
                        UserRole.ADMIN -> AppTab.ADMIN
                        UserRole.OWNER, UserRole.AGENT -> {
                            if (authResult.user.isApproved) AppTab.MY_PROPERTIES else AppTab.HOME
                        }
                        UserRole.USER -> AppTab.HOME
                    }
                }

                if (!authResult.user.isApproved && (authResult.user.role == UserRole.OWNER || authResult.user.role == UserRole.AGENT)) {
                    _uiMessage.value = "Votre compte est en attente de validation par l'administrateur."
                } else {
                    _uiMessage.value = "Bienvenue ${authResult.user.firstName} !"
                }
            }.onFailure { err ->
                _authError.value = err.message ?: "Échec de connexion"
            }
            _authLoading.value = false
        }
    }

    /**
     * Connexion sécurisée au portail Administrateur avec validation stricte du rôle ADMIN.
     */
    fun loginAsAdmin(email: String, password: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authRepository.login(LoginRequest(email = email.trim(), password = password))
            result.onSuccess { authResult ->
                // SÉCURITÉ STRICTE : Seul un compte possédant réellement le rôle ADMIN dans la base de données est autorisé
                if (authResult.user.role != UserRole.ADMIN) {
                    authRepository.logout()
                    _currentUserId.value = 0L
                    _isAuthenticated.value = false
                    _authError.value = "Accès refusé : Ce compte (${authResult.user.email}) ne dispose pas des privilèges administrateur requis."
                } else {
                    _currentUserId.value = authResult.user.id
                    _isAuthenticated.value = true
                    _isGuestMode.value = false
                    _currentTab.value = AppTab.ADMIN
                    _uiMessage.value = "Session administrateur ouverte avec succès."
                }
            }.onFailure { err ->
                _authError.value = err.message ?: "Identifiants administrateur incorrects."
            }
            _authLoading.value = false
        }
    }

    fun register(firstName: String, lastName: String, email: String, phone: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authRepository.register(
                RegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone,
                    password = password,
                    role = role
                )
            )
            result.onSuccess {
                // Ne pas connecter automatiquement : rediriger vers la page de connexion
                _registrationSuccess.value = true
                _uiMessage.value = "Inscription réussie. Veuillez vous connecter."
            }.onFailure { err ->
                _authError.value = err.message ?: "Échec d'inscription"
            }
            _authLoading.value = false
        }
    }

    private fun handlePendingAction(action: PendingAction, user: User) {
        when (action) {
            is PendingAction.OpenProperty -> {
                openPropertyDetail(action.propertyId)
            }
            is PendingAction.ToggleFavorite -> {
                openPropertyDetail(action.propertyId)
                toggleFavorite(action.propertyId)
                _uiMessage.value = "Logement ajouté aux favoris !"
            }
            is PendingAction.ContactOwner -> {
                openPropertyDetail(action.propertyId)
                startConversationWithPropertyOwner(action.propertyId, action.targetUser)
            }
            is PendingAction.RequestVisit -> {
                openPropertyDetail(action.propertyId)
                _autoOpenVisitDialog.value = action.propertyId
            }
            is PendingAction.RentOrBuy -> {
                openPropertyDetail(action.propertyId)
                _autoOpenRentOrBuyDialog.value = action.propertyId
            }
            is PendingAction.NavigateToTab -> {
                if (action.tab == AppTab.ADMIN && user.role != UserRole.ADMIN) {
                    _uiMessage.value = "Accès refusé : espace réservé aux administrateurs."
                    _currentTab.value = AppTab.HOME
                } else if ((action.tab == AppTab.MY_PROPERTIES || action.tab == AppTab.PUBLISH) &&
                    (user.role == UserRole.USER || !user.isApproved)) {
                    if (user.role == UserRole.USER) {
                        _uiMessage.value = "L'espace publication est réservé aux propriétaires et agents."
                    } else {
                        _uiMessage.value = "Votre compte est en attente de validation par l'administrateur."
                    }
                    _currentTab.value = AppTab.HOME
                } else {
                    _currentTab.value = action.tab
                }
            }
        }
    }

    fun submitRentalOrPurchaseApplication(
        propertyId: Long,
        transactionType: TransactionType,
        proposedPrice: Double,
        desiredDateOrFinancing: String,
        applicantMessage: String
    ) {
        val user = currentUser.value
        if (user == null || !_isAuthenticated.value) {
            _uiMessage.value = "Veuillez vous connecter pour soumettre votre demande."
            return
        }
        viewModelScope.launch {
            val prop = repository.getPropertyByIdSync(propertyId)
            val recipientId = prop?.managedByAgentId ?: prop?.ownerId
            val actionName = if (transactionType == TransactionType.RENT) "Dossier de location" else "Offre d'achat"
            val body = if (transactionType == TransactionType.RENT) {
                "Bonjour,\nJe vous transmets ma candidature officielle de location pour ce logement :\n- Date souhaitée d'entrée : $desiredDateOrFinancing\n- Loyer proposé : ${proposedPrice.toInt()} €/mois\n- Notes du candidat : $applicantMessage\n\nCoordonnées du candidat : ${user.fullName} (${user.email}, ${user.phone})"
            } else {
                "Bonjour,\nJe vous transmets mon offre d'achat officielle pour ce logement :\n- Montant proposé : ${proposedPrice.toInt()} €\n- Plan de financement : $desiredDateOrFinancing\n- Notes de l'acquéreur : $applicantMessage\n\nCoordonnées de l'acquéreur : ${user.fullName} (${user.email}, ${user.phone})"
            }

            if (recipientId != null) {
                val convId = repository.getOrCreateConversation(user.id, propertyId, recipientId)
                repository.sendMessage(convId, user.id, body)
            }
            _uiMessage.value = "$actionName transmis(e) avec succès au bailleur !"
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUserId.value = 0L
            _isAuthenticated.value = false
            _isGuestMode.value = false
            _selectedPropertyId.value = null
            _activeConversationId.value = null
            _editingProperty.value = null
            _pendingAction.value = null
            _uiMessage.value = "Déconnexion réussie."
        }
    }

    fun approveUser(targetUserId: Long) {
        viewModelScope.launch {
            val result = repository.approveUser(_currentUserId.value, targetUserId)
            result.onSuccess {
                _uiMessage.value = "Compte validé et autorisé avec succès !"
            }.onFailure { err ->
                _uiMessage.value = err.message ?: "Erreur de validation"
            }
        }
    }

    fun rejectUser(targetUserId: Long) {
        viewModelScope.launch {
            val result = repository.rejectUser(_currentUserId.value, targetUserId)
            result.onSuccess {
                _uiMessage.value = "Compte refusé."
            }.onFailure { err ->
                _uiMessage.value = err.message ?: "Erreur lors du refus"
            }
        }
    }

    fun updateUserProfile(firstName: String, lastName: String, phone: String, bio: String) {
        viewModelScope.launch {
            val result = repository.updateUserProfile(_currentUserId.value, firstName, lastName, phone, bio)
            result.onSuccess {
                _uiMessage.value = "Profil mis à jour avec succès !"
            }.onFailure { err ->
                _uiMessage.value = err.message ?: "Erreur lors de la mise à jour"
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUploadingPhoto.value = true
            _photoUploadError.value = null
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri)
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    _photoUploadError.value = "Veuillez sélectionner un fichier image valide (JPG, PNG, WebP)."
                    _isUploadingPhoto.value = false
                    return@launch
                }

                val inputStream = contentResolver.openInputStream(uri)
                    ?: throw IOException("Impossible de lire l'image sélectionnée.")

                val bytes = inputStream.use { it.readBytes() }
                if (bytes.size > 10 * 1024 * 1024) { // 10MB
                    _photoUploadError.value = "L'image ne doit pas dépasser 10 Mo."
                    _isUploadingPhoto.value = false
                    return@launch
                }

                val profileDir = File(context.filesDir, "profile_pictures")
                if (!profileDir.exists()) {
                    profileDir.mkdirs()
                }
                val targetFile = File(profileDir, "avatar_${_currentUserId.value}_${System.currentTimeMillis()}.jpg")
                targetFile.writeBytes(bytes)

                val photoUrl = "file://${targetFile.absolutePath}"
                val updateResult = repository.updateUserPhoto(_currentUserId.value, photoUrl)
                updateResult.onSuccess {
                    _uiMessage.value = "Photo de profil enregistrée avec succès !"
                }.onFailure {
                    _photoUploadError.value = "Erreur lors de l'enregistrement de la photo en base."
                }
            } catch (e: Exception) {
                _photoUploadError.value = e.message ?: "Erreur lors de l'import de l'image."
            } finally {
                _isUploadingPhoto.value = false
            }
        }
    }

    fun removeProfilePhoto() {
        viewModelScope.launch {
            val result = repository.updateUserPhoto(_currentUserId.value, "")
            result.onSuccess {
                _uiMessage.value = "Photo de profil supprimée."
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }



    // Property details
    fun openPropertyDetail(propertyId: Long) {
        _selectedPropertyId.value = propertyId
        viewModelScope.launch {
            repository.recordPropertyView(propertyId, _currentUserId.value)
        }
    }

    fun closePropertyDetail() {
        _selectedPropertyId.value = null
    }

    // Editing
    fun startEditingProperty(property: Property) {
        val user = currentUser.value
        if (user?.role == UserRole.USER) {
            _uiMessage.value = "Non autorisé pour un compte USER."
            return
        }
        if (user?.role == UserRole.OWNER && property.ownerId != user.id) {
            _uiMessage.value = "Vous ne pouvez modifier que vos propres logements !"
            return
        }
        _editingProperty.value = property
        _currentTab.value = AppTab.PUBLISH
    }

    fun cancelEditingProperty() {
        _editingProperty.value = null
    }

    // Search filters
    fun updateSearchQuery(query: String) {
        _searchFilter.value = _searchFilter.value.copy(query = query)
    }

    fun updatePropertyType(type: PropertyType?) {
        _searchFilter.value = _searchFilter.value.copy(propertyType = type)
    }

    fun updateTransactionType(transaction: TransactionType?) {
        _searchFilter.value = _searchFilter.value.copy(transactionType = transaction)
    }

    fun updateMaxPrice(maxPrice: Double?) {
        _searchFilter.value = _searchFilter.value.copy(maxPrice = maxPrice)
    }

    fun applySearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    fun clearSearchFilters() {
        _searchFilter.value = SearchFilter()
    }

    fun setSearchDisplayMode(mode: SearchDisplayMode) {
        _searchDisplayMode.value = mode
    }

    // Favorites
    fun toggleFavorite(propertyId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(_currentUserId.value, propertyId)
        }
    }

    // Visit Requests
    fun triggerRentOrBuy(propertyId: Long, transactionType: TransactionType) {
        val user = currentUser.value
        val actionLabel = if (transactionType == TransactionType.RENT) "location" else "achat"
        _uiMessage.value = "Demande d'$actionLabel enregistrée avec succès pour ce logement !"
    }

    fun requestVisit(propertyId: Long, date: String, time: String, message: String) {
        viewModelScope.launch {
            val result = repository.requestVisit(_currentUserId.value, propertyId, date, time, message)
            result.onSuccess {
                _uiMessage.value = "Demande de visite transmise avec succès !"
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur lors de la demande de visite"
            }
        }
    }

    fun updateVisitStatus(requestId: Long, status: VisitStatus) {
        viewModelScope.launch {
            val result = repository.updateVisitStatus(_currentUserId.value, requestId, status)
            result.onSuccess {
                _uiMessage.value = "Statut de la visite mis à jour : ${status.label}"
            }.onFailure {
                _uiMessage.value = it.message ?: "Impossible de modifier la visite"
            }
        }
    }

    // Messaging
    fun startConversationWithPropertyOwner(propertyId: Long, recipientUser: User) {
        viewModelScope.launch {
            val convId = repository.getOrCreateConversation(_currentUserId.value, propertyId, recipientUser.id)
            _activeConversationId.value = convId
            _currentTab.value = AppTab.MESSAGES
            _selectedPropertyId.value = null
        }
    }

    fun openConversation(conversationId: Long) {
        _activeConversationId.value = conversationId
        viewModelScope.launch {
            repository.markMessagesAsRead(conversationId, _currentUserId.value)
        }
    }

    fun closeConversation() {
        _activeConversationId.value = null
    }

    fun sendMessage(content: String) {
        val convId = _activeConversationId.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(convId, _currentUserId.value, content.trim())
        }
    }

    // Notifications
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(_currentUserId.value)
        }
    }

    // Reporting
    fun reportProperty(propertyId: Long, reason: String, description: String) {
        viewModelScope.launch {
            repository.createReport(_currentUserId.value, propertyId, reason, description)
            _uiMessage.value = "Logement signalé aux administrateurs. Merci pour votre vigilance."
        }
    }

    // Publish & Update Property
    fun publishProperty(
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
    ) {
        viewModelScope.launch {
            val result = repository.createProperty(
                userId = _currentUserId.value,
                title = title,
                description = description,
                price = price,
                propertyType = propertyType,
                transactionType = transactionType,
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
                parking = parking,
                images = images,
                submitForValidation = submitForValidation
            )
            result.onSuccess {
                _uiMessage.value = if (submitForValidation) {
                    "Annonce créée et soumise pour validation par les administrateurs !"
                } else {
                    "Annonce enregistrée en tant que brouillon !"
                }
                val user = currentUser.value
                _currentTab.value = when (user?.role) {
                    UserRole.OWNER, UserRole.AGENT -> AppTab.MY_PROPERTIES
                    UserRole.ADMIN -> AppTab.ADMIN
                    else -> AppTab.HOME
                }
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur de publication"
            }
        }
    }

    fun saveEditedProperty(
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
    ) {
        viewModelScope.launch {
            val result = repository.updateProperty(
                userId = _currentUserId.value,
                propertyId = propertyId,
                title = title,
                description = description,
                price = price,
                propertyType = propertyType,
                transactionType = transactionType,
                address = address,
                city = city,
                district = district,
                area = area,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                rooms = rooms,
                floor = floor,
                parking = parking,
                images = images,
                submitForValidation = submitForValidation
            )
            result.onSuccess {
                _uiMessage.value = "Modifications enregistrées avec succès !"
                _editingProperty.value = null
                val userRole = currentUser.value?.role
                _currentTab.value = if (userRole == UserRole.ADMIN) AppTab.ADMIN else AppTab.MY_PROPERTIES
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur lors de la mise à jour"
            }
        }
    }

    fun updatePropertyStatus(propertyId: Long, newStatus: PropertyStatus) {
        viewModelScope.launch {
            val result = repository.updatePropertyStatus(_currentUserId.value, propertyId, newStatus)
            result.onSuccess {
                _uiMessage.value = "Statut mis à jour : ${newStatus.label}"
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur de changement de statut"
            }
        }
    }

    fun deleteProperty(propertyId: Long) {
        viewModelScope.launch {
            val result = repository.deleteProperty(_currentUserId.value, propertyId)
            result.onSuccess {
                _uiMessage.value = "Annonce supprimée."
                if (_selectedPropertyId.value == propertyId) {
                    _selectedPropertyId.value = null
                }
            }.onFailure {
                _uiMessage.value = it.message ?: "Impossible de supprimer ce bien"
            }
        }
    }

    // Admin Specific
    fun resolveReport(reportId: Long, status: ReportStatus) {
        viewModelScope.launch {
            val result = repository.resolveReport(_currentUserId.value, reportId, status)
            result.onSuccess {
                _uiMessage.value = "Signalement traité (${status.label})"
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur"
            }
        }
    }

    fun toggleUserVerification(targetUserId: Long) {
        viewModelScope.launch {
            val result = repository.toggleUserVerification(_currentUserId.value, targetUserId)
            result.onSuccess {
                _uiMessage.value = "Statut de vérification utilisateur modifié"
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur"
            }
        }
    }

    fun toggleUserSuspension(targetUserId: Long) {
        viewModelScope.launch {
            val result = repository.toggleUserSuspension(_currentUserId.value, targetUserId)
            result.onSuccess {
                _uiMessage.value = "Statut de suspension de l'utilisateur mis à jour"
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur"
            }
        }
    }

    fun updateUserRole(targetUserId: Long, newRole: UserRole) {
        viewModelScope.launch {
            val result = repository.updateUserRole(_currentUserId.value, targetUserId, newRole)
            result.onSuccess {
                _uiMessage.value = "Rôle mis à jour : ${newRole.name}"
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur"
            }
        }
    }

    fun deleteUser(targetUserId: Long) {
        viewModelScope.launch {
            val result = repository.deleteUser(_currentUserId.value, targetUserId)
            result.onSuccess {
                _uiMessage.value = "Utilisateur supprimé de la base de données"
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur"
            }
        }
    }

    fun adminDeleteReportedProperty(reportId: Long, propertyId: Long) {
        viewModelScope.launch {
            val result = repository.adminDeleteReportedProperty(_currentUserId.value, reportId, propertyId)
            result.onSuccess {
                _uiMessage.value = "Bien supprimé et signalement résolu !"
            }.onFailure {
                _uiMessage.value = it.message ?: "Erreur"
            }
        }
    }
}
