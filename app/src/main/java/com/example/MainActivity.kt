package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.HourglassTop
import com.example.ui.viewmodel.PendingAction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.UserRole
import com.example.data.model.VisitStatus
import com.example.ui.components.AvatarWithBadge
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.MyPropertiesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PropertyDetailScreen
import com.example.ui.screens.PublishScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.VisitsScreen
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LogementApp()
            }
        }
    }
}

enum class AuthScreenState {
    LOGIN,
    REGISTER,
    ADMIN_LOGIN
}

data class BottomBarItem(
    val tab: AppTab,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogementApp(viewModel: MainViewModel = viewModel()) {
    val isSessionChecking by viewModel.isSessionChecking.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val isGuestMode by viewModel.isGuestMode.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedProperty by viewModel.selectedProperty.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val adminStats by viewModel.adminStats.collectAsStateWithLifecycle()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val visitRequests by viewModel.visitRequests.collectAsStateWithLifecycle()
    val pendingRegistrationNotice by viewModel.pendingRegistrationNotice.collectAsStateWithLifecycle()
    val registrationSuccess by viewModel.registrationSuccess.collectAsStateWithLifecycle()
    val requestedAuthMode by viewModel.requestedAuthMode.collectAsStateWithLifecycle()
    val showPublishAuthPrompt by viewModel.showPublishAuthPrompt.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showUserMenu by remember { mutableStateOf(false) }
    var authScreenMode by remember { mutableStateOf<AuthScreenState>(AuthScreenState.LOGIN) }
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }

    val pendingVisitsCount = visitRequests.count { it.request.status == VisitStatus.PENDING }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            authScreenMode = AuthScreenState.LOGIN
            viewModel.clearRegistrationSuccess()
        }
    }

    LaunchedEffect(requestedAuthMode) {
        requestedAuthMode?.let { mode ->
            authScreenMode = when (mode) {
                "REGISTER" -> AuthScreenState.REGISTER
                "ADMIN_LOGIN" -> AuthScreenState.ADMIN_LOGIN
                else -> AuthScreenState.LOGIN
            }
            viewModel.clearRequestedAuthMode()
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val activity = context as? ComponentActivity
        val uri = activity?.intent?.data
        if (uri != null && (uri.path == "/admin/login" || (uri.scheme == "logement" && uri.host == "admin" && uri.path == "/login"))) {
            viewModel.setGuestMode(false)
            authScreenMode = AuthScreenState.ADMIN_LOGIN
        }
    }

    // 1. Session Check / Splash loading state
    if (isSessionChecking) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(CornerRadius.lg),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = Elevation.low,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(Spacing.xs)) {
                        Image(
                            painter = painterResource(id = R.drawable.apex_realty_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(CornerRadius.md)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = "Logement France",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                androidx.compose.material3.CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            }
        }
        return
    }

    // 2. Unauthenticated: Show Login, Register, or Admin Login screens unless exploring as Guest
    if (!isAuthenticated && !isGuestMode) {
        BackHandler {
            viewModel.setGuestMode(true)
        }
        when (authScreenMode) {
            AuthScreenState.LOGIN -> {
                LoginScreen(
                    isLoading = authLoading,
                    errorMessage = authError,
                    onLogin = { email, pwd -> viewModel.login(email, pwd) },
                    onNavigateToRegister = {
                        viewModel.clearAuthError()
                        authScreenMode = AuthScreenState.REGISTER
                    },
                    onContinueAsGuest = {
                        viewModel.setGuestMode(true)
                    },
                    onForgotPassword = {
                        // Information mot de passe oublié
                    },
                    onNavigateToAdminLogin = {
                        viewModel.clearAuthError()
                        authScreenMode = AuthScreenState.ADMIN_LOGIN
                    }
                )
            }
            AuthScreenState.REGISTER -> {
                RegisterScreen(
                    isLoading = authLoading,
                    errorMessage = authError,
                    onRegister = { fName, lName, email, phone, pwd, rRole ->
                        viewModel.register(fName, lName, email, phone, pwd, rRole)
                    },
                    onNavigateToLogin = {
                        viewModel.clearAuthError()
                        authScreenMode = AuthScreenState.LOGIN
                    },
                    onContinueAsGuest = {
                        viewModel.setGuestMode(true)
                    }
                )
            }
            AuthScreenState.ADMIN_LOGIN -> {
                AdminLoginScreen(
                    isLoading = authLoading,
                    errorMessage = authError,
                    onLogin = { email, pwd -> viewModel.loginAsAdmin(email, pwd) },
                    onBackToPublic = {
                        viewModel.clearAuthError()
                        authScreenMode = AuthScreenState.LOGIN
                    }
                )
            }
        }
        return
    }

    // 3. Main Experience (Guest or Authenticated User)
    // Hardware Back Navigation
    BackHandler(enabled = selectedProperty != null) {
        viewModel.closePropertyDetail()
    }

    BackHandler(enabled = selectedProperty == null && currentTab != AppTab.HOME) {
        viewModel.navigateToTab(AppTab.HOME)
    }

    // Dynamic Role-tailored Bottom Tabs
    val role = currentUser?.role ?: UserRole.USER
    val bottomTabs = if (!isAuthenticated) {
        listOf(
            BottomBarItem(AppTab.HOME, Icons.Default.Home, "Accueil"),
            BottomBarItem(AppTab.SEARCH, Icons.Default.Search, "Rechercher"),
            BottomBarItem(AppTab.FAVORITES, Icons.Default.Favorite, "Favoris"),
            BottomBarItem(AppTab.MESSAGES, Icons.AutoMirrored.Filled.Chat, "Messages"),
            BottomBarItem(AppTab.PROFILE, Icons.Default.Person, "Connexion")
        )
    } else when (role) {
        UserRole.USER -> listOf(
            BottomBarItem(AppTab.HOME, Icons.Default.Home, "Accueil"),
            BottomBarItem(AppTab.SEARCH, Icons.Default.Search, "Rechercher"),
            BottomBarItem(AppTab.FAVORITES, Icons.Default.Favorite, "Favoris"),
            BottomBarItem(AppTab.VISITS, Icons.Default.CalendarMonth, "Mes Visites"),
            BottomBarItem(AppTab.PROFILE, Icons.Default.Person, "Profil")
        )
        UserRole.OWNER -> listOf(
            BottomBarItem(AppTab.HOME, Icons.Default.Home, "Accueil"),
            BottomBarItem(AppTab.MY_PROPERTIES, Icons.Default.HomeWork, "Mes biens"),
            BottomBarItem(AppTab.PUBLISH, Icons.Default.Add, "Déposer"),
            BottomBarItem(AppTab.VISITS, Icons.Default.CalendarMonth, "Visites"),
            BottomBarItem(AppTab.PROFILE, Icons.Default.Person, "Profil")
        )
        UserRole.AGENT -> listOf(
            BottomBarItem(AppTab.HOME, Icons.Default.Home, "Accueil"),
            BottomBarItem(AppTab.MY_PROPERTIES, Icons.Default.Apartment, "Mes mandats"),
            BottomBarItem(AppTab.PUBLISH, Icons.Default.Add, "Déposer"),
            BottomBarItem(AppTab.VISITS, Icons.Default.CalendarMonth, "Visites"),
            BottomBarItem(AppTab.PROFILE, Icons.Default.Person, "Profil")
        )
        UserRole.ADMIN -> listOf(
            BottomBarItem(AppTab.ADMIN, Icons.Default.Security, "Dashboard"),
            BottomBarItem(AppTab.HOME, Icons.Default.HomeWork, "Annonces"),
            BottomBarItem(AppTab.PUBLISH, Icons.Default.Add, "Déposer"),
            BottomBarItem(AppTab.VISITS, Icons.Default.CalendarMonth, "Visites"),
            BottomBarItem(AppTab.PROFILE, Icons.Default.Person, "Profil")
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedVisibility(
                visible = selectedProperty == null,
                enter = slideInVertically(
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                    initialOffsetY = { -it }
                ) + fadeIn(tween(250)),
                exit = slideOutVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    targetOffsetY = { -it }
                ) + fadeOut(tween(200))
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = Elevation.low,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentTab == AppTab.VISITS || currentTab == AppTab.ADMIN) {
                                    IconButton(
                                        onClick = { viewModel.navigateToTab(AppTab.HOME) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Retour",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(CornerRadius.xs),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.apex_realty_logo),
                                            contentDescription = "Logo",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(CornerRadius.xs)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                }

                                if (currentTab != AppTab.HOME) {
                                    Text(
                                        text = when (currentTab) {
                                            AppTab.VISITS -> "Visites"
                                            AppTab.ADMIN -> "Supervision Admin"
                                            AppTab.FAVORITES -> "Favoris"
                                            AppTab.MY_PROPERTIES -> if (role == UserRole.AGENT) "Mandats Agence" else "Mes Biens"
                                            AppTab.MESSAGES -> "Messages"
                                            AppTab.PUBLISH -> "Déposer une annonce"
                                            AppTab.SEARCH -> "Recherche"
                                            AppTab.PROFILE -> "Profil"
                                            else -> ""
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = (-0.3).sp
                                    )
                                }
                            }
                        },
                        actions = {
                            // Quick Visits Action
                            IconButton(
                                onClick = { viewModel.navigateToTab(AppTab.VISITS) },
                                modifier = Modifier.size(40.dp).testTag("top_visits_shortcut")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (pendingVisitsCount > 0) {
                                            Badge(
                                                containerColor = DesignColors.Warning,
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Text("$pendingVisitsCount", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "Visites",
                                        tint = if (currentTab == AppTab.VISITS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Quick Admin Shortcut (if user is ADMIN)
                            if (role == UserRole.ADMIN) {
                                IconButton(
                                    onClick = { viewModel.navigateToTab(AppTab.ADMIN) },
                                    modifier = Modifier.size(40.dp).testTag("top_admin_shortcut")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (adminStats.pendingCount > 0) {
                                                Badge(
                                                    containerColor = DesignColors.Warning,
                                                    modifier = Modifier.size(16.dp)
                                                ) {
                                                    Text("${adminStats.pendingCount}", fontSize = 9.sp)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Security,
                                            contentDescription = "Admin",
                                            tint = if (currentTab == AppTab.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Quick Demo Role Switcher button
                            Surface(
                                shape = RoundedCornerShape(CornerRadius.pill),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clickable { showRoleSwitcherDialog = true }
                                    .testTag("top_bar_role_switcher_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when {
                                            !isAuthenticated -> "⚡ Rôles Démo ▾"
                                            role == UserRole.ADMIN -> "👑 Admin ▾"
                                            role == UserRole.AGENT -> "🏢 Agent ▾"
                                            role == UserRole.OWNER -> "🏡 Propriétaire ▾"
                                            else -> "👤 Chercheur ▾"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(Spacing.xs))

                            if (!isAuthenticated) {
                                Surface(
                                    shape = RoundedCornerShape(CornerRadius.pill),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                                    modifier = Modifier.padding(end = Spacing.xs)
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.setGuestMode(false)
                                            authScreenMode = AuthScreenState.ADMIN_LOGIN
                                        },
                                        modifier = Modifier.size(34.dp).testTag("top_bar_admin_portal_btn")
                                    ) {
                                        Icon(
                                            Icons.Default.Security,
                                            contentDescription = "Portail Administrateur",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.setGuestMode(false)
                                        authScreenMode = AuthScreenState.LOGIN
                                    },
                                    shape = RoundedCornerShape(CornerRadius.pill),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.padding(end = Spacing.sm).testTag("top_bar_login_btn")
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text(
                                        text = "Se connecter",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Badge Profil de l'utilisateur connecté
                                Surface(
                                    shape = RoundedCornerShape(CornerRadius.pill),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .padding(end = Spacing.sm)
                                        .clickable { viewModel.navigateToTab(AppTab.PROFILE) }
                                        .testTag("current_user_profile_badge")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!currentUser?.photoUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = currentUser?.photoUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(Spacing.xs))
                                        }
                                        Text(
                                            text = "${currentUser?.firstName ?: "Mon compte"} • ${role.name}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedProperty == null,
                enter = slideInVertically(
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                    initialOffsetY = { it }
                ) + fadeIn(tween(250)),
                exit = slideOutVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    targetOffsetY = { it }
                ) + fadeOut(tween(200))
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = Elevation.high,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = Elevation.none,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        bottomTabs.forEach { item ->
                            val isSelected = currentTab == item.tab
                            val isCenterPublish = item.tab == AppTab.PUBLISH

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateToTab(item.tab) },
                                icon = {
                                    if (isCenterPublish) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = item.label,
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        when (item.tab) {
                                            AppTab.PROFILE -> {
                                                BadgedBox(
                                                    badge = {
                                                        if (unreadNotifs > 0) {
                                                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                                Text("$unreadNotifs", fontSize = 9.sp)
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(item.icon, contentDescription = item.label)
                                                }
                                            }
                                            else -> Icon(item.icon, contentDescription = item.label)
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Notification de compte en attente après inscription
        pendingRegistrationNotice?.let { notice ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissRegistrationNotice() },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = DesignColors.Warning.copy(alpha = 0.15f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = DesignColors.Warning,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Compte en attente de validation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = notice,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = { viewModel.dismissRegistrationNotice() },
                        shape = RoundedCornerShape(CornerRadius.md)
                    ) {
                        Text("J'ai compris", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showPublishAuthPrompt) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissPublishAuthPrompt() },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.HomeWork,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Publication d'annonce",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "La publication d'annonces est réservée aux propriétaires (OWNER) et agents immobiliers (AGENT).",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Connectez-vous si vous disposez déjà d'un compte adapté, ou inscrivez-vous en choisissant le profil Propriétaire ou Agent.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Button(
                            onClick = {
                                viewModel.dismissPublishAuthPrompt()
                                viewModel.setPendingAction(PendingAction.NavigateToTab(AppTab.PUBLISH))
                                viewModel.requestAuth("LOGIN")
                            },
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier.fillMaxWidth().testTag("publish_prompt_login_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Se connecter", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.dismissPublishAuthPrompt()
                                viewModel.setPendingAction(PendingAction.NavigateToTab(AppTab.PUBLISH))
                                viewModel.requestAuth("REGISTER")
                            },
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier.fillMaxWidth().testTag("publish_prompt_register_btn")
                        ) {
                            Text("Créer un compte Propriétaire / Agent", fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissPublishAuthPrompt() },
                        modifier = Modifier.fillMaxWidth().testTag("publish_prompt_dismiss_btn")
                    ) {
                        Text("Continuer la visite libre", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }

        if (showRoleSwitcherDialog) {
            com.example.ui.components.RoleSwitcherDialog(
                currentRole = currentUser?.role,
                onSelectRole = { email, pwd ->
                    viewModel.setGuestMode(false)
                    viewModel.login(email, pwd)
                    showRoleSwitcherDialog = false
                },
                onDismiss = { showRoleSwitcherDialog = false }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedProperty,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetY = { (it * 0.16f).toInt() }
                        ) + fadeIn(
                            animationSpec = tween(320, easing = FastOutSlowInEasing)
                        ) + scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialScale = 0.93f
                        )) togetherWith (
                            fadeOut(animationSpec = tween(220)) +
                            scaleOut(animationSpec = tween(220), targetScale = 0.97f)
                        )
                    } else {
                        (fadeIn(animationSpec = tween(260, delayMillis = 50)) +
                         scaleIn(animationSpec = tween(260, delayMillis = 50), initialScale = 0.97f)
                        ) togetherWith (
                            slideOutVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                targetOffsetY = { (it * 0.20f).toInt() }
                            ) + fadeOut(animationSpec = tween(220)) +
                            scaleOut(animationSpec = tween(240), targetScale = 0.94f)
                        )
                    }
                },
                label = "property_detail_immersive_transition"
            ) { property ->
                if (property != null) {
                    PropertyDetailScreen(
                        propertyDetails = property,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(180)) },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            AppTab.HOME -> HomeScreen(viewModel = viewModel)
                            AppTab.SEARCH -> SearchScreen(viewModel = viewModel)
                            AppTab.FAVORITES -> FavoritesScreen(viewModel = viewModel)
                            AppTab.MY_PROPERTIES -> MyPropertiesScreen(viewModel = viewModel)
                            AppTab.PUBLISH -> PublishScreen(viewModel = viewModel)
                            AppTab.VISITS -> VisitsScreen(viewModel = viewModel)
                            AppTab.MESSAGES -> MessagesScreen(viewModel = viewModel)
                            AppTab.ADMIN -> AdminScreen(viewModel = viewModel)
                            AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
