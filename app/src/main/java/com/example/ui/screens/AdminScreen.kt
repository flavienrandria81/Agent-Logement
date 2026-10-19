package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.AccountStatus
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.PropertyWithDetails
import com.example.data.model.ReportStatus
import com.example.data.model.ReportWithDetails
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.VisitRequestWithDetails
import com.example.data.model.VisitStatus
import com.example.ui.components.AdminAnalyticsDashboard
import com.example.ui.components.AvatarWithBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val adminStats by viewModel.adminStats.collectAsStateWithLifecycle()
    val pendingProperties by viewModel.pendingProperties.collectAsStateWithLifecycle()
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val pendingApprovalUsers by viewModel.pendingApprovalUsers.collectAsStateWithLifecycle()
    val visitRequests by viewModel.visitRequests.collectAsStateWithLifecycle()

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    var propertyToDelete by remember { mutableStateOf<Property?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    // Search and filters for properties tab
    var propertySearchQuery by remember { mutableStateOf("") }
    var propertyStatusFilter by remember { mutableStateOf<PropertyStatus?>(null) }
    var propertyTypeFilter by remember { mutableStateOf<PropertyType?>(null) }

    // Search and filters for users tab
    var userSearchQuery by remember { mutableStateOf("") }
    var userRoleFilter by remember { mutableStateOf<String>("TOUS") }

    // Search and filters for visits tab
    var visitsStatusFilter by remember { mutableStateOf<VisitStatus?>(null) }
    var visitsSubTab by remember { mutableIntStateOf(0) }

    // Règle stricte d'accès : Seul le rôle ADMIN authentifié peut accéder à cet écran
    if (currentUser?.role != UserRole.ADMIN) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = DesignColors.Error.copy(alpha = 0.15f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = DesignColors.Error,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text = "Accès Refusé",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Cet espace de supervision est strictement réservé aux comptes avec le rôle Administrateur.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
            OutlinedButton(
                onClick = { viewModel.navigateToTab(AppTab.HOME) },
                shape = RoundedCornerShape(CornerRadius.md)
            ) {
                Text("Retour à l'accueil", fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // En-tête fixe du Dashboard Administrateur
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = Elevation.low,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.pill),
                            color = DesignColors.PrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = DesignColors.Primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ESPACE ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DesignColors.Primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Supervision & Pilotage",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { viewModel.navigateToTab(AppTab.PUBLISH) },
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
                        modifier = Modifier.testTag("admin_create_property_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Créer annonce", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.size(36.dp).testTag("admin_logout_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Déconnexion",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Barre d'onglets de gestion scrollable
        ScrollableTabRow(
            selectedTabIndex = selectedAdminTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = Spacing.sm,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedAdminTab == 0,
                onClick = { selectedAdminTab = 0 },
                text = {
                    Text(
                        text = "Tableau de bord",
                        fontWeight = if (selectedAdminTab == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedAdminTab == 1,
                onClick = { selectedAdminTab = 1 },
                text = {
                    Text(
                        text = "Gestion des annonces (${allProperties.size})",
                        fontWeight = if (selectedAdminTab == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedAdminTab == 2,
                onClick = { selectedAdminTab = 2 },
                text = {
                    Text(
                        text = "Utilisateurs (${allUsers.size})" + if (pendingApprovalUsers.isNotEmpty()) " • ${pendingApprovalUsers.size} attente" else "",
                        fontWeight = if (selectedAdminTab == 2) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedAdminTab == 3,
                onClick = { selectedAdminTab = 3 },
                text = {
                    Text(
                        text = "Demandes & Visites (${visitRequests.size})",
                        fontWeight = if (selectedAdminTab == 3) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedAdminTab == 4,
                onClick = { selectedAdminTab = 4 },
                text = {
                    val pendingReportsCount = allReports.count { it.report.status == ReportStatus.PENDING }
                    Text(
                        text = "Signalements ($pendingReportsCount)",
                        fontWeight = if (selectedAdminTab == 4) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            )
        }

        // Contenu des onglets
        when (selectedAdminTab) {
            // =========================================================================
            // TAB 0 : TABLEAU DE BORD (KPIS RÉELS & GRAPHIQUES INTERACTIFS)
            // =========================================================================
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    Text(
                        text = "Indicateurs Clés en Temps Réel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Ligne 1 : Parc Immobilier
                    Text(
                        text = "Parc Immobilier & Offres",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        KpiCard("Total Biens", "${adminStats.totalProperties}", Icons.Default.HomeWork, DesignColors.Primary, DesignColors.PrimaryContainer, Modifier.width(96.dp))
                        KpiCard("Maisons", "${adminStats.housesCount}", Icons.Default.Home, DesignColors.Secondary, DesignColors.SecondaryContainer, Modifier.width(96.dp))
                        KpiCard("Apparts", "${adminStats.apartmentsCount}", Icons.Default.Apartment, DesignColors.Accent, DesignColors.Accent.copy(alpha = 0.15f), Modifier.width(96.dp))
                        KpiCard("Terrains", "${adminStats.landsCount}", Icons.Default.HomeWork, DesignColors.Primary, DesignColors.PrimaryContainer, Modifier.width(96.dp))
                        KpiCard("À Louer", "${adminStats.forRentCount}", Icons.Default.Key, DesignColors.Primary, DesignColors.PrimaryContainer, Modifier.width(96.dp))
                        KpiCard("À Vendre", "${adminStats.forSaleCount}", Icons.Default.Home, DesignColors.Secondary, DesignColors.SecondaryContainer, Modifier.width(96.dp))
                    }

                    // Ligne 2 : Statuts de publication
                    Text(
                        text = "Statuts des Annonces",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        KpiCard("En attente", "${adminStats.pendingCount}", Icons.Default.HourglassTop, DesignColors.Warning, DesignColors.WarningContainer, Modifier.weight(1f))
                        KpiCard("Publiées", "${adminStats.publishedCount}", Icons.Default.PublishedWithChanges, DesignColors.Secondary, DesignColors.SecondaryContainer, Modifier.weight(1f))
                        KpiCard("Refusées", "${adminStats.rejectedCount}", Icons.Default.Block, DesignColors.Error, DesignColors.ErrorContainer, Modifier.weight(1f))
                        KpiCard("Loué/Vendu", "${adminStats.rentedCount + adminStats.soldCount}", Icons.Default.CheckCircle, DesignColors.Primary, DesignColors.PrimaryContainer, Modifier.weight(1f))
                    }

                    // Ligne 3 : Communauté & Demandes
                    Text(
                        text = "Utilisateurs, Demandes & Activité",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        KpiCard("Inscrits", "${adminStats.totalUsersCount}", Icons.Default.Group, DesignColors.Primary, DesignColors.PrimaryContainer, Modifier.width(100.dp))
                        KpiCard("Propriétaires", "${adminStats.ownersCount}", Icons.Default.Person, DesignColors.Secondary, DesignColors.SecondaryContainer, Modifier.width(100.dp))
                        KpiCard("Agents Immo", "${adminStats.agentsCount}", Icons.Default.Business, DesignColors.Accent, DesignColors.Accent.copy(alpha = 0.15f), Modifier.width(100.dp))
                        KpiCard("Candidatures", "${adminStats.rentalAndPurchaseRequestsCount}", Icons.Default.Key, DesignColors.Primary, DesignColors.PrimaryContainer, Modifier.width(100.dp))
                        KpiCard("Visites", "${adminStats.visitRequestsCount}", Icons.Default.CalendarMonth, DesignColors.Secondary, DesignColors.SecondaryContainer, Modifier.width(100.dp))
                        KpiCard("Signalements", "${adminStats.totalReportsCount}", Icons.Default.ReportProblem, DesignColors.Error, DesignColors.ErrorContainer, Modifier.width(100.dp))
                    }

                    // Section Graphiques Modernes et Interactifs
                    Text(
                        text = "Analyses & Graphiques Avancés",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    AdminAnalyticsDashboard(
                        adminStats = adminStats,
                        allProperties = allProperties,
                        reports = allReports,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxl))
                }
            }

            // =========================================================================
            // TAB 1 : GESTION DES ANNONCES (CRUD COMPLET AVEC RECHERCHE ET ACTIONS)
            // =========================================================================
            1 -> {
                val filteredProps = remember(allProperties, propertySearchQuery, propertyStatusFilter, propertyTypeFilter) {
                    allProperties.filter { item ->
                        val p = item.property
                        val matchesQuery = propertySearchQuery.isBlank() ||
                                p.title.contains(propertySearchQuery, ignoreCase = true) ||
                                p.city.contains(propertySearchQuery, ignoreCase = true) ||
                                (item.owner?.fullName?.contains(propertySearchQuery, ignoreCase = true) == true)
                        val matchesStatus = propertyStatusFilter == null || p.status == propertyStatusFilter
                        val matchesType = propertyTypeFilter == null || p.propertyType == propertyTypeFilter
                        matchesQuery && matchesStatus && matchesType
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize().testTag("admin_properties_management_list")
                ) {
                    // Barre de recherche
                    item {
                        OutlinedTextField(
                            value = propertySearchQuery,
                            onValueChange = { propertySearchQuery = it },
                            placeholder = { Text("Rechercher par titre, ville, propriétaire...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (propertySearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { propertySearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier.fillMaxWidth().testTag("admin_property_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        )
                    }

                    // Filtres par statut
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = propertyStatusFilter == null,
                                    onClick = { propertyStatusFilter = null },
                                    label = { Text("Tous statuts (${allProperties.size})", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            PropertyStatus.entries.forEach { status ->
                                val count = allProperties.count { it.property.status == status }
                                item {
                                    FilterChip(
                                        selected = propertyStatusFilter == status,
                                        onClick = { propertyStatusFilter = if (propertyStatusFilter == status) null else status },
                                        label = { Text("${status.label} ($count)", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Filtres par type de bien
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = propertyTypeFilter == null,
                                    onClick = { propertyTypeFilter = null },
                                    label = { Text("Tous types", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DesignColors.Secondary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            PropertyType.entries.forEach { type ->
                                val count = allProperties.count { it.property.propertyType == type }
                                if (count > 0) {
                                    item {
                                        FilterChip(
                                            selected = propertyTypeFilter == type,
                                            onClick = { propertyTypeFilter = if (propertyTypeFilter == type) null else type },
                                            label = { Text("${type.label} ($count)", fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = DesignColors.Secondary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredProps.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.HomeWork,
                                title = "Aucune annonce trouvée",
                                description = "Aucun bien ne correspond aux filtres appliqués.",
                                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl)
                            )
                        }
                    } else {
                        items(filteredProps, key = { it.property.id }) { item ->
                            val prop = item.property
                            Card(
                                shape = RoundedCornerShape(CornerRadius.lg),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(Spacing.md)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = item.primaryImageUrl,
                                            contentDescription = prop.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(CornerRadius.sm))
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.md))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prop.title,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "${prop.city} (${prop.district}) • ${prop.formattedPrice}",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "${prop.propertyType.label} • ${prop.transactionType.label} • Par : ${item.owner?.fullName ?: "Inconnu"}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        StatusPill(
                                            text = prop.status.label,
                                            backgroundColor = when (prop.status) {
                                                PropertyStatus.PUBLISHED -> DesignColors.SecondaryContainer
                                                PropertyStatus.PENDING -> DesignColors.WarningContainer
                                                PropertyStatus.REJECTED -> DesignColors.ErrorContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(Spacing.sm))

                                    // Ligne d'actions CRUD pour l'Admin
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Consulter
                                        OutlinedButton(
                                            onClick = { viewModel.openPropertyDetail(prop.id) },
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text("Consulter", fontSize = 11.sp)
                                        }

                                        // 2. Modifier (Formulaire complet de modification)
                                        OutlinedButton(
                                            onClick = { viewModel.startEditingProperty(prop) },
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Modifier", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        // 3. Approuver / Publier ou Suspendre
                                        if (prop.status != PropertyStatus.PUBLISHED) {
                                            Button(
                                                onClick = { viewModel.updatePropertyStatus(prop.id, PropertyStatus.PUBLISHED) },
                                                modifier = Modifier.weight(1.1f).height(34.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Success),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Approuver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewModel.updatePropertyStatus(prop.id, PropertyStatus.REJECTED) },
                                                modifier = Modifier.weight(1.1f).height(34.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DesignColors.Warning),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                            ) {
                                                Text("Suspendre", fontSize = 11.sp)
                                            }
                                        }

                                        // 4. Supprimer (avec dialogue de confirmation)
                                        IconButton(
                                            onClick = { propertyToDelete = prop },
                                            modifier = Modifier.size(34.dp).testTag("admin_delete_prop_${prop.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = DesignColors.Error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 2 : GESTION DES UTILISATEURS (AUTORISATION, SUSPENSION, SUPPRESSION)
            // =========================================================================
            2 -> {
                val filteredUsers = remember(allUsers, userSearchQuery, userRoleFilter) {
                    allUsers.filter { u ->
                        val matchesQuery = userSearchQuery.isBlank() ||
                                u.fullName.contains(userSearchQuery, ignoreCase = true) ||
                                u.email.contains(userSearchQuery, ignoreCase = true) ||
                                u.phone.contains(userSearchQuery, ignoreCase = true)
                        val matchesRole = when (userRoleFilter) {
                            "PENDING" -> u.accountStatus == AccountStatus.PENDING_APPROVAL
                            "OWNER" -> u.role == UserRole.OWNER
                            "AGENT" -> u.role == UserRole.AGENT
                            "USER" -> u.role == UserRole.USER
                            "ADMIN" -> u.role == UserRole.ADMIN
                            else -> true
                        }
                        matchesQuery && matchesRole
                    }.sortedWith(
                        compareByDescending<User> { it.accountStatus == AccountStatus.PENDING_APPROVAL }
                            .thenBy { it.id }
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize().testTag("admin_users_list")
                ) {
                    // Champ de recherche utilisateurs
                    item {
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Rechercher par nom, email, téléphone...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (userSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { userSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier.fillMaxWidth().testTag("admin_user_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        )
                    }

                    // Filtres par rôle
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val roleOptions = listOf(
                                "TOUS" to "Tous (${allUsers.size})",
                                "PENDING" to "En attente (${pendingApprovalUsers.size})",
                                "OWNER" to "Propriétaires (${allUsers.count { it.role == UserRole.OWNER }})",
                                "AGENT" to "Agents (${allUsers.count { it.role == UserRole.AGENT }})",
                                "USER" to "Chercheurs (${allUsers.count { it.role == UserRole.USER }})"
                            )

                            roleOptions.forEach { (key, label) ->
                                item {
                                    FilterChip(
                                        selected = userRoleFilter == key,
                                        onClick = { userRoleFilter = key },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = if (key == "PENDING") DesignColors.Warning else MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (filteredUsers.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Person,
                                title = "Aucun utilisateur trouvé",
                                description = "Aucun compte ne correspond à vos critères de recherche.",
                                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl)
                            )
                        }
                    } else {
                        items(filteredUsers, key = { it.id }) { user ->
                            UserManagementCard(
                                user = user,
                                isCurrentAdmin = user.id == currentUser?.id,
                                onApprove = { viewModel.approveUser(user.id) },
                                onReject = { viewModel.rejectUser(user.id) },
                                onToggleSuspension = { viewModel.toggleUserSuspension(user.id) },
                                onChangeRole = { newRole -> viewModel.updateUserRole(user.id, newRole) },
                                onDelete = { userToDelete = user }
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 3 : GESTION DES DEMANDES ET DES VISITES
            // =========================================================================
            3 -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(Spacing.md)
                ) {
                    // Sous-onglets : Visites / Candidatures
                    TabRow(
                        selectedTabIndex = visitsSubTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(CornerRadius.md))
                    ) {
                        Tab(
                            selected = visitsSubTab == 0,
                            onClick = { visitsSubTab = 0 },
                            text = { Text("Demandes de Visite (${visitRequests.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = visitsSubTab == 1,
                            onClick = { visitsSubTab = 1 },
                            text = { Text("Candidatures Location / Achat (${adminStats.rentalAndPurchaseRequestsCount})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    if (visitsSubTab == 0) {
                        // Filtres par statut de visite
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm)
                        ) {
                            item {
                                FilterChip(
                                    selected = visitsStatusFilter == null,
                                    onClick = { visitsStatusFilter = null },
                                    label = { Text("Toutes (${visitRequests.size})", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
                                )
                            }
                            VisitStatus.entries.forEach { status ->
                                val count = visitRequests.count { it.request.status == status }
                                item {
                                    FilterChip(
                                        selected = visitsStatusFilter == status,
                                        onClick = { visitsStatusFilter = if (visitsStatusFilter == status) null else status },
                                        label = { Text("${status.label} ($count)", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
                                    )
                                }
                            }
                        }

                        val filteredVisits = remember(visitRequests, visitsStatusFilter) {
                            visitRequests.filter { visitsStatusFilter == null || it.request.status == visitsStatusFilter }
                        }

                        if (filteredVisits.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.CalendarMonth,
                                title = "Aucune visite",
                                description = "Aucune demande de visite ne correspond au filtre sélectionné.",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                modifier = Modifier.fillMaxSize().testTag("admin_visits_list")
                            ) {
                                items(filteredVisits, key = { it.request.id }) { item ->
                                    val req = item.request
                                    val prop = item.property
                                    Card(
                                        shape = RoundedCornerShape(CornerRadius.md),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(Spacing.md)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = item.propertyImageUrl,
                                                    contentDescription = prop.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(CornerRadius.sm))
                                                )
                                                Spacer(modifier = Modifier.width(Spacing.sm))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = prop.title, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 13.sp)
                                                    Text(text = "${prop.city} • Demandeur : ${item.requester.fullName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(text = "Tél : ${item.requester.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                }
                                                StatusPill(
                                                    text = req.status.label,
                                                    backgroundColor = when (req.status) {
                                                        VisitStatus.ACCEPTED -> DesignColors.SecondaryContainer
                                                        VisitStatus.PENDING -> DesignColors.WarningContainer
                                                        VisitStatus.REJECTED -> DesignColors.ErrorContainer
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(Spacing.xs))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Créneau : ${req.requestedDate} à ${req.requestedTime}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                )
                                                if (req.message.isNotBlank()) {
                                                    Text(
                                                        text = "« ${req.message.take(30)} »",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(Spacing.xs))
                                            // Actions Administrateur sur la visite
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { viewModel.openPropertyDetail(prop.id) },
                                                    modifier = Modifier.weight(1f).height(32.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("Voir bien", fontSize = 11.sp)
                                                }
                                                if (req.status == VisitStatus.PENDING) {
                                                    Button(
                                                        onClick = { viewModel.updateVisitStatus(req.id, VisitStatus.ACCEPTED) },
                                                        modifier = Modifier.weight(1f).height(32.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Success),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Confirmer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { viewModel.updateVisitStatus(req.id, VisitStatus.REJECTED) },
                                                        modifier = Modifier.weight(1f).height(32.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DesignColors.Error),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Refuser", fontSize = 11.sp)
                                                    }
                                                } else {
                                                    OutlinedButton(
                                                        onClick = { viewModel.updateVisitStatus(req.id, VisitStatus.CANCELLED) },
                                                        modifier = Modifier.weight(1f).height(32.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Annuler", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Section Candidatures Location & Achat
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            modifier = Modifier.fillMaxSize().testTag("admin_applications_list")
                        ) {
                            items(allProperties.filter { it.property.status == PropertyStatus.PUBLISHED }.take(10), key = { it.property.id }) { item ->
                                val prop = item.property
                                Card(
                                    shape = RoundedCornerShape(CornerRadius.md),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(Spacing.md)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = prop.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )
                                            StatusPill(
                                                text = if (prop.transactionType == com.example.data.model.TransactionType.RENT) "LOCATION" else "ACHAT",
                                                backgroundColor = if (prop.transactionType == com.example.data.model.TransactionType.RENT) DesignColors.PrimaryContainer else DesignColors.SecondaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(Spacing.xxs))
                                        Text(
                                            text = "Prix demandé : ${prop.formattedPrice} • Propriétaire : ${item.owner?.fullName ?: "Bailleur"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(Spacing.xs))
                                        Surface(
                                            shape = RoundedCornerShape(CornerRadius.xs),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Dossiers de candidatures et offres d'achat transmises aux bailleurs pour cette annonce.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 4 : SIGNALEMENTS
            // =========================================================================
            4 -> {
                if (allReports.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Check,
                        title = "Aucun signalement",
                        description = "Tous les signalements ont été traités par l'administration.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxSize().testTag("admin_reports_list")
                    ) {
                        items(allReports, key = { it.report.id }) { item ->
                            ReportCard(
                                reportWithDetails = item,
                                onResolve = { viewModel.resolveReport(item.report.id, ReportStatus.RESOLVED) },
                                onDismiss = { viewModel.resolveReport(item.report.id, ReportStatus.DISMISSED) },
                                onViewProperty = { viewModel.openPropertyDetail(item.property.id) },
                                onDeleteProperty = {
                                    viewModel.adminDeleteReportedProperty(item.report.id, item.property.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogue de confirmation de suppression d'annonce
    if (propertyToDelete != null) {
        AlertDialog(
            onDismissRequest = { propertyToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DesignColors.Error) },
            title = { Text("Supprimer l'annonce ?") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement l'annonce « ${propertyToDelete?.title} » ? Cette action supprimera également les images et données associées dans la base de données.") },
            confirmButton = {
                Button(
                    onClick = {
                        propertyToDelete?.let { viewModel.deleteProperty(it.id) }
                        propertyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Error)
                ) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { propertyToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialogue de confirmation de suppression d'utilisateur
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DesignColors.Error) },
            title = { Text("Supprimer l'utilisateur ?") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement ${userToDelete?.fullName} ? Ses annonces et ses demandes associées seront retirées.") },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.let { viewModel.deleteUser(it.id) }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Error)
                ) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(CornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            Surface(
                shape = RoundedCornerShape(CornerRadius.xs),
                color = backgroundColor,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun ReportCard(
    reportWithDetails: ReportWithDetails,
    onResolve: () -> Unit,
    onDismiss: () -> Unit,
    onViewProperty: () -> Unit,
    onDeleteProperty: () -> Unit
) {
    val rep = reportWithDetails.report
    val prop = reportWithDetails.property

    Card(
        shape = RoundedCornerShape(CornerRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(
                    text = rep.reason,
                    backgroundColor = DesignColors.ErrorContainer,
                    contentColor = DesignColors.Error
                )

                StatusPill(
                    text = rep.status.label,
                    backgroundColor = when (rep.status) {
                        ReportStatus.PENDING -> DesignColors.Warning
                        ReportStatus.RESOLVED -> DesignColors.Success
                        ReportStatus.DISMISSED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "Bien : ${prop.title} (${prop.city})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (rep.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "\"${rep.description}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Signalé par : ${reportWithDetails.reportingUser.fullName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            if (rep.status == ReportStatus.PENDING) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    OutlinedButton(
                        onClick = onViewProperty,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Voir bien", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Classer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onResolve,
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Secondary),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Résoudre", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDeleteProperty,
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Error),
                        modifier = Modifier.weight(1.2f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Supprimer bien", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserManagementCard(
    user: User,
    isCurrentAdmin: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onToggleSuspension: () -> Unit,
    onChangeRole: (UserRole) -> Unit,
    onDelete: () -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(CornerRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isSuspended) DesignColors.ErrorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (user.isSuspended) DesignColors.Error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarWithBadge(
                    photoUrl = user.photoUrl,
                    fullName = user.fullName,
                    size = 48.dp,
                    isVerified = user.emailVerified
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.isSuspended) {
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Surface(
                                shape = RoundedCornerShape(CornerRadius.pill),
                                color = DesignColors.Error
                            ) {
                                Text(
                                    text = "SUSPENDU",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(text = user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = user.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Role Pill with Dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(CornerRadius.pill),
                        color = when (user.role) {
                            UserRole.ADMIN -> DesignColors.Primary
                            UserRole.AGENT -> DesignColors.Secondary
                            UserRole.OWNER -> DesignColors.Accent
                            UserRole.USER -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.clickable { if (!isCurrentAdmin) showRoleMenu = true }
                    ) {
                        Text(
                            text = "${user.role.name} ▼",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        UserRole.entries.forEach { roleOption ->
                            DropdownMenuItem(
                                text = { Text(roleOption.name, fontWeight = if (user.role == roleOption) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    onChangeRole(roleOption)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Statut de validation & Actions d'approbation ADMIN
            val isPendingApproval = user.accountStatus == AccountStatus.PENDING_APPROVAL
            if (isPendingApproval) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Surface(
                    shape = RoundedCornerShape(CornerRadius.sm),
                    color = DesignColors.Warning.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, DesignColors.Warning.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = DesignColors.Warning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "En attente d'approbation ADMIN (${user.role.name})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (!isCurrentAdmin) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPendingApproval) {
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(CornerRadius.sm),
                            colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Success),
                            modifier = Modifier.height(34.dp).testTag("approve_user_${user.id}"),
                            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = 0.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Autoriser", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(Spacing.xs))

                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(CornerRadius.sm),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DesignColors.Error),
                            modifier = Modifier.height(34.dp).testTag("reject_user_${user.id}"),
                            contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = 0.dp)
                        ) {
                            Text("Refuser", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(Spacing.xs))
                    }

                    OutlinedButton(
                        onClick = onToggleSuspension,
                        shape = RoundedCornerShape(CornerRadius.sm),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (user.isSuspended) DesignColors.Success else DesignColors.Warning
                        ),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = if (user.isSuspended) Icons.Default.CheckCircle else Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = if (user.isSuspended) "Rétablir" else "Suspendre",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp).testTag("delete_user_${user.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer utilisateur",
                            tint = DesignColors.Error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
