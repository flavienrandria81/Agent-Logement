package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.AccountStatus
import com.example.data.model.UserRole
import com.example.ui.components.AvatarWithBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PropertyCard
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsStateWithLifecycle()
    val photoUploadError by viewModel.photoUploadError.collectAsStateWithLifecycle()
    val myFavorites by viewModel.myFavorites.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val agency by viewModel.agencyForCurrentAgent.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPhotoUploadDialog by remember { mutableStateOf(false) }
    var selectedSectionTab by remember { mutableIntStateOf(0) }

    // Si utilisateur non connecté : invite à se connecter
    if (!isAuthenticated || currentUser == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Mon Profil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Connectez-vous pour voir vos informations personnelles, gérer vos biens, vos visites et vos favoris.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Button(
                onClick = { viewModel.setGuestMode(false) },
                shape = RoundedCornerShape(CornerRadius.md),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .testTag("profile_login_btn")
            ) {
                Text("Se connecter / S'inscrire", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedButton(
                onClick = { viewModel.requestAuth("ADMIN_LOGIN") },
                shape = RoundedCornerShape(CornerRadius.md),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .testTag("profile_admin_login_btn")
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Portail Administrateur", fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }

    val user = currentUser!!

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_screen_scroll"),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // En-tête Mon Profil
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mon profil",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Gérez vos informations personnelles et votre compte",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = { showEditProfileDialog = true },
                    shape = RoundedCornerShape(CornerRadius.md),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
                    modifier = Modifier.testTag("edit_profile_button")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Modifier",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Avertissement compte PENDING_APPROVAL
        if (user.accountStatus == AccountStatus.PENDING_APPROVAL) {
            item {
                Card(
                    shape = RoundedCornerShape(CornerRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = DesignColors.WarningContainer.copy(alpha = 0.85f)),
                    border = BorderStroke(1.5.dp, DesignColors.Warning),
                    modifier = Modifier.fillMaxWidth().testTag("pending_approval_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DesignColors.Warning,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Compte en attente de validation",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Votre compte ${user.role.name} nécessite l'autorisation d'un administrateur avant de pouvoir publier ou gérer des mandats.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Carte Principale : Mon Profil
        item {
            Card(
                shape = RoundedCornerShape(CornerRadius.xl),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
                modifier = Modifier.fillMaxWidth().testTag("my_profile_card")
            ) {
                Column(modifier = Modifier.padding(Spacing.xl)) {
                    // Photo de profil et Rôle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box {
                            AvatarWithBadge(
                                photoUrl = user.photoUrl,
                                fullName = user.fullName,
                                isVerified = user.emailVerified,
                                size = 80.dp
                            )
                            // Bouton caméra pour modifier la photo
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(30.dp)
                                    .clickable { showPhotoUploadDialog = true }
                                    .testTag("change_avatar_btn")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Changer la photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(Spacing.lg))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Badges Rôle et Statut
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(CornerRadius.pill),
                                    color = when (user.role) {
                                        UserRole.ADMIN -> DesignColors.Primary
                                        UserRole.AGENT -> DesignColors.Secondary
                                        UserRole.OWNER -> DesignColors.Accent
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                ) {
                                    Text(
                                        text = when (user.role) {
                                            UserRole.ADMIN -> "ADMINISTRATEUR"
                                            UserRole.AGENT -> "AGENT IMMOBILIER"
                                            UserRole.OWNER -> "PROPRIÉTAIRE"
                                            else -> "CHERCHEUR (USER)"
                                        },
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(CornerRadius.pill),
                                    color = when (user.accountStatus) {
                                        AccountStatus.ACTIVE -> DesignColors.Success.copy(alpha = 0.15f)
                                        AccountStatus.PENDING_APPROVAL -> DesignColors.Warning.copy(alpha = 0.2f)
                                        AccountStatus.REJECTED -> DesignColors.Error.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = user.accountStatus.label,
                                        color = when (user.accountStatus) {
                                            AccountStatus.ACTIVE -> DesignColors.Success
                                            AccountStatus.PENDING_APPROVAL -> DesignColors.Warning
                                            AccountStatus.REJECTED -> DesignColors.Error
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // Coordonnées et détails personnels
                    Surface(
                        shape = RoundedCornerShape(CornerRadius.md),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            // Email
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column {
                                    Text("Email", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(user.email, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Téléphone
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column {
                                    Text("Téléphone", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        if (user.phone.isNotBlank()) user.phone else "Non renseigné",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Bio / Présentation si renseignée
                            if (user.bio.isNotBlank()) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Column {
                                        Text("À propos de moi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(user.bio, fontSize = 13.sp)
                                    }
                                }
                            }

                            // Agence si rôle AGENT
                            if (user.role == UserRole.AGENT && agency != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Column {
                                        Text("Agence partenaire", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${agency?.name} (${agency?.address})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Bouton Modifier mon profil
                    Button(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("modify_my_profile_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Modifier mon profil", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Raccourcis rapides selon le rôle connecté
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                when (user.role) {
                    UserRole.OWNER, UserRole.AGENT -> {
                        Button(
                            onClick = {
                                if (user.isApproved) {
                                    viewModel.navigateToTab(AppTab.MY_PROPERTIES)
                                } else {
                                    viewModel.triggerPendingNotice()
                                }
                            },
                            shape = RoundedCornerShape(CornerRadius.md),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isApproved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                if (user.role == UserRole.AGENT) "Mes Mandats" else "Mes Biens",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user.isApproved) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    UserRole.ADMIN -> {
                        Button(
                            onClick = { viewModel.navigateToTab(AppTab.ADMIN) },
                            shape = RoundedCornerShape(CornerRadius.md),
                            colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Primary),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Espace Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {}
                }

                Button(
                    onClick = { viewModel.navigateToTab(AppTab.VISITS) },
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Secondary),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text("Mes Visites", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Onglets : Mes Favoris & Mes Alertes
        item {
            TabRow(
                selectedTabIndex = selectedSectionTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSectionTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(CornerRadius.md))
            ) {
                Tab(
                    selected = selectedSectionTab == 0,
                    onClick = { selectedSectionTab = 0 },
                    text = {
                        Text(
                            text = "Mes Favoris (${myFavorites.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedSectionTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedSectionTab == 1,
                    onClick = { selectedSectionTab = 1 },
                    text = {
                        Text(
                            text = "Mes Alertes (${notifications.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedSectionTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // Contenu Favoris / Alertes
        if (selectedSectionTab == 0) {
            if (myFavorites.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Favorite,
                        title = "Aucun favori enregistré",
                        description = "Appuyez sur le cœur sur un bien pour le retrouver ici.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(myFavorites, key = { it.property.id }) { item ->
                    PropertyCard(
                        propertyDetails = item,
                        onClick = { viewModel.openPropertyDetail(item.property.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(item.property.id) }
                    )
                }
            }
        } else {
            item {
                if (notifications.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Tout marquer comme lu", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (notifications.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Notifications,
                        title = "Aucune alerte reçue",
                        description = "Vos mises à jour de statut, visites et messages apparaîtront ici.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) DesignColors.PrimaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.markNotificationRead(notif.id) }
                    ) {
                        Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (!notif.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = if (!notif.isRead) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Section Déconnexion
        item {
            Card(
                shape = RoundedCornerShape(CornerRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, DesignColors.Error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = "Session & Déconnexion",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Met fin à la session JWT sécurisée sur cet appareil.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Button(
                        onClick = { viewModel.logout() },
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Error),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("logout_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Déconnexion",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Se déconnecter",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Dialogue : Modifier mon profil
    if (showEditProfileDialog) {
        var editFirstName by remember { mutableStateOf(user.firstName) }
        var editLastName by remember { mutableStateOf(user.lastName) }
        var editPhone by remember { mutableStateOf(user.phone) }
        var editBio by remember { mutableStateOf(user.bio) }
        var editError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Modifier mon profil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (editError != null) {
                        Text(editError!!, color = DesignColors.Error, fontSize = 12.sp)
                    }

                    Text("Prénom", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { editFirstName = it; editError = null },
                        singleLine = true,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_firstname")
                    )

                    Text("Nom", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editLastName,
                        onValueChange = { editLastName = it; editError = null },
                        singleLine = true,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_lastname")
                    )

                    Text("Téléphone", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        singleLine = true,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone")
                    )

                    Text("Bio / Présentation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        maxLines = 3,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_bio")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editFirstName.isBlank() || editLastName.isBlank()) {
                            editError = "Le prénom et le nom sont obligatoires."
                            return@Button
                        }
                        viewModel.updateUserProfile(editFirstName, editLastName, editPhone, editBio)
                        showEditProfileDialog = false
                    },
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("save_profile_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text("Enregistrer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialogue : Upload Photo de profil (Sélection d'image ou URL)
    if (showPhotoUploadDialog) {
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            selectedImageUri = uri
        }

        AlertDialog(
            onDismissRequest = { showPhotoUploadDialog = false },
            title = {
                Text(
                    text = "Photo de profil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Prévisualisation de l'image
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Prévisualisation",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (user.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Actuelle",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (photoUploadError != null) {
                        Text(photoUploadError!!, color = DesignColors.Error, fontSize = 12.sp)
                    }

                    // Bouton Sélectionner depuis l'appareil
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("pick_device_photo_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = if (selectedImageUri != null) "Changer d'image" else "Choisir une image depuis l'appareil",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Bouton Supprimer la photo existante
                    if (user.photoUrl.isNotBlank() && selectedImageUri == null) {
                        TextButton(
                            onClick = {
                                viewModel.removeProfilePhoto()
                                showPhotoUploadDialog = false
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = DesignColors.Error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Supprimer ma photo actuelle", color = DesignColors.Error, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = selectedImageUri
                        if (uri != null) {
                            viewModel.uploadProfilePhoto(uri, context)
                            showPhotoUploadDialog = false
                        }
                    },
                    enabled = selectedImageUri != null && !isUploadingPhoto,
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("confirm_upload_photo_btn")
                ) {
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Enregistrer la photo", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoUploadDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
