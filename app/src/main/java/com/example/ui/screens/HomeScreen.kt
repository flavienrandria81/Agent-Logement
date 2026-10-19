package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.PropertyType
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.PropertyCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SearchDisplayMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeSlide(
    val id: String,
    val imageRes: Int,
    val badge: String,
    val badgeColor: Color,
    val title: String,
    val subtitle: String,
    val ctaText: String,
    val ctaIcon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val publishedProperties by viewModel.publishedProperties.collectAsStateWithLifecycle()
    val filteredProperties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    var authRequiredAction by remember { mutableStateOf<String?>(null) }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val coroutineScope = rememberCoroutineScope()

    // Vérifier si un filtre personnalisé est actif
    val isFilteringActive = searchFilter.query.isNotBlank() ||
            searchFilter.propertyType != null ||
            searchFilter.maxPrice != null ||
            searchFilter.transactionType != null

    // Biens affichés : filtrés en direct si recherche/filtre actif, sinon populaires
    val displayProperties = if (isFilteringActive) {
        filteredProperties
    } else {
        publishedProperties.sortedByDescending { it.property.viewCount }
    }

    // Configuration des diapositives en haut selon le rôle
    val slides = when (currentUser?.role) {
        UserRole.OWNER -> listOf(
            HomeSlide(
                id = "owner_create",
                imageRes = R.drawable.hero_real_estate,
                badge = "ESPACE PROPRIÉTAIRE",
                badgeColor = DesignColors.Secondary,
                title = "Gérez vos Annonces Facilement",
                subtitle = "Créez vos fiches biens, importez vos photos et recevez directement les demandes.",
                ctaText = "Déposer une annonce",
                ctaIcon = Icons.Default.HomeWork,
                onClick = { viewModel.navigateToTab(AppTab.PUBLISH) }
            ),
            HomeSlide(
                id = "owner_listings",
                imageRes = R.drawable.slide_luxury_villa,
                badge = "SUIVI & VISITES",
                badgeColor = DesignColors.Primary,
                title = "Vos Visites & Statistiques",
                subtitle = "Acceptez ou refusez les créneaux et suivez les vues en temps réel.",
                ctaText = "Voir mes biens",
                ctaIcon = Icons.Default.Home,
                onClick = { viewModel.navigateToTab(AppTab.MY_PROPERTIES) }
            ),
            HomeSlide(
                id = "owner_prestiges",
                imageRes = R.drawable.slide_modern_apartment,
                badge = "COLLECTION PRESTIGE",
                badgeColor = DesignColors.Accent,
                title = "Valorisez vos Biens Immobiliers",
                subtitle = "Présentez vos propriétés sous leur meilleur jour avec des visuels soignés.",
                ctaText = "Mes annonces",
                ctaIcon = Icons.Default.Apartment,
                onClick = { viewModel.navigateToTab(AppTab.MY_PROPERTIES) }
            )
        )
        UserRole.AGENT -> listOf(
            HomeSlide(
                id = "agent_mandates",
                imageRes = R.drawable.hero_real_estate,
                badge = "ESPACE MANDATAIRE",
                badgeColor = DesignColors.Secondary,
                title = "Gérez vos Mandats Agence",
                subtitle = "Consultez les biens rattachés à votre agence et pilotez votre portefeuille.",
                ctaText = "Gérer mes mandats",
                ctaIcon = Icons.Default.Apartment,
                onClick = { viewModel.navigateToTab(AppTab.MY_PROPERTIES) }
            ),
            HomeSlide(
                id = "agent_visits",
                imageRes = R.drawable.slide_luxury_villa,
                badge = "AGENDA VISITES",
                badgeColor = DesignColors.Primary,
                title = "Planning Visites Acquéreurs",
                subtitle = "Validez les demandes de visites des chercheurs qualifiés en direct.",
                ctaText = "Ouvrir l'agenda",
                ctaIcon = Icons.Default.CalendarMonth,
                onClick = { viewModel.navigateToTab(AppTab.VISITS) }
            ),
            HomeSlide(
                id = "agent_messages",
                imageRes = R.drawable.slide_modern_apartment,
                badge = "AGENCE PARTENAIRE",
                badgeColor = DesignColors.Accent,
                title = "Accompagnement d'Excellence",
                subtitle = "Répondez aux candidats locataires et acheteurs via la messagerie.",
                ctaText = "Messagerie",
                ctaIcon = Icons.Default.HomeWork,
                onClick = { viewModel.navigateToTab(AppTab.MESSAGES) }
            )
        )
        UserRole.ADMIN -> listOf(
            HomeSlide(
                id = "admin_dashboard",
                imageRes = R.drawable.hero_real_estate,
                badge = "SUPERVISION PLATEFORME",
                badgeColor = DesignColors.Warning,
                title = "Tableau de Bord Administrateur",
                subtitle = "Modérez les annonces en attente, gérez les signalements et les utilisateurs.",
                ctaText = "Ouvrir le Dashboard",
                ctaIcon = Icons.Default.HomeWork,
                onClick = { viewModel.navigateToTab(AppTab.ADMIN) }
            ),
            HomeSlide(
                id = "admin_validation",
                imageRes = R.drawable.slide_luxury_villa,
                badge = "CONTRÔLE QUALITÉ",
                badgeColor = DesignColors.Secondary,
                title = "Validation des Nouvelles Annonces",
                subtitle = "Assurez la conformité des prix, caractéristiques et photos publiées.",
                ctaText = "Modérer les annonces",
                ctaIcon = Icons.Default.Home,
                onClick = { viewModel.navigateToTab(AppTab.ADMIN) }
            ),
            HomeSlide(
                id = "admin_analytics",
                imageRes = R.drawable.slide_modern_apartment,
                badge = "STATISTIQUES & RAPPORTS",
                badgeColor = DesignColors.Primary,
                title = "Indicateurs Clés en Temps Réel",
                subtitle = "Visualisez la dynamique du parc immobilier et les flux d'échanges.",
                ctaText = "Voir les statistiques",
                ctaIcon = Icons.AutoMirrored.Filled.TrendingUp,
                onClick = { viewModel.navigateToTab(AppTab.ADMIN) }
            )
        )
        else -> listOf(
            HomeSlide(
                id = "user_villas",
                imageRes = R.drawable.slide_luxury_villa,
                badge = "COLLECTION PRESTIGE",
                badgeColor = DesignColors.Secondary,
                title = "Villas & Demeures d'Exception",
                subtitle = "Piscines, jardins d'architecte et prestations haut de gamme sélectionnées avec soin.",
                ctaText = "Explorer les villas",
                ctaIcon = Icons.Default.Home,
                onClick = {
                    viewModel.updatePropertyType(PropertyType.HOUSE)
                    viewModel.navigateToTab(AppTab.SEARCH)
                }
            ),
            HomeSlide(
                id = "user_apartments",
                imageRes = R.drawable.slide_modern_apartment,
                badge = "TENDANCES URBAINES",
                badgeColor = DesignColors.Primary,
                title = "Appartements & Lofts Lumineux",
                subtitle = "Emplacements prisés en centre-ville, terrasses et aménagements récents.",
                ctaText = "Voir les appartements",
                ctaIcon = Icons.Default.Apartment,
                onClick = {
                    viewModel.updatePropertyType(PropertyType.APARTMENT)
                    viewModel.navigateToTab(AppTab.SEARCH)
                }
            ),
            HomeSlide(
                id = "user_search",
                imageRes = R.drawable.hero_real_estate,
                badge = "IMMOBILIER VÉRIFIÉ 100%",
                badgeColor = DesignColors.Accent,
                title = "Trouvez Votre Futur Chez-Vous",
                subtitle = "Vente, location et visites sécurisées sans mauvaises surprises ni frais cachés.",
                ctaText = "Lancer une recherche",
                ctaIcon = Icons.Default.Search,
                onClick = { viewModel.navigateToTab(AppTab.SEARCH) }
            ),
            HomeSlide(
                id = "user_visits",
                imageRes = R.drawable.slide_luxury_villa,
                badge = "VISITES EN DIRECT",
                badgeColor = DesignColors.Secondary,
                title = "Planifiez vos Visites en 1 Clic",
                subtitle = "Choisissez vos créneaux en direct avec les propriétaires et agents certifiés.",
                ctaText = "Mes demandes de visite",
                ctaIcon = Icons.Default.CalendarMonth,
                onClick = { viewModel.navigateToTab(AppTab.VISITS) }
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })

    // Défilement automatique fluide toutes les 5 secondes
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000)
            if (!pagerState.isScrollInProgress && slides.isNotEmpty()) {
                val next = (pagerState.currentPage + 1) % slides.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(bottom = Spacing.xxxl)
    ) {
        // Welcome Header Bar avec Logo épuré (symbole uniquement) et Persona
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(CornerRadius.sm),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.apex_realty_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(CornerRadius.sm)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    Column {
                        val greetingTitle = when (currentUser?.role) {
                            UserRole.OWNER -> "Bonjour 👋"
                            UserRole.AGENT -> "Bonjour 👋"
                            UserRole.ADMIN -> "Bonjour 👋"
                            else -> "Bonjour 👋"
                        }
                        val greetingSubtitle = when (currentUser?.role) {
                            UserRole.OWNER -> "Gérez vos biens"
                            UserRole.AGENT -> "Votre activité immobilière"
                            UserRole.ADMIN -> "Tableau de bord de la plateforme"
                            else -> "Trouvez votre prochain logement"
                        }

                        Text(
                            text = "$greetingTitle ${currentUser?.firstName ?: ""}".trim(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = greetingSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(CornerRadius.pill),
                    color = DesignColors.PrimaryContainer,
                    modifier = Modifier.clickable { viewModel.navigateToTab(AppTab.PROFILE) }
                ) {
                    Text(
                        text = currentUser?.role?.name ?: "USER",
                        color = DesignColors.Primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    )
                }
            }
        }

        // =========================================================================
        // BARRE DE RECHERCHE EN HAUT AVEC FILTRES RAPIDES (Appartement, Maison, Budget)
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = Spacing.md)
            ) {
                // Champ de recherche moderne
                OutlinedTextField(
                    value = searchFilter.query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Rechercher par ville, quartier, mot-clé...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchFilter.query.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Effacer la recherche",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Tous les filtres",
                                    tint = if (searchFilter.maxPrice != null || searchFilter.propertyType != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.lg),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar")
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Filtres rapides : Type de logement (Appartement, Maison) et Budget
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filtre rapide : Appartement
                    val isApartmentSelected = searchFilter.propertyType == PropertyType.APARTMENT
                    FilterChip(
                        selected = isApartmentSelected,
                        onClick = {
                            viewModel.updatePropertyType(if (isApartmentSelected) null else PropertyType.APARTMENT)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Apartment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isApartmentSelected) Color.White else MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("Appartement", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(CornerRadius.pill),
                        modifier = Modifier.testTag("home_filter_chip_apartment")
                    )

                    // Filtre rapide : Maison
                    val isHouseSelected = searchFilter.propertyType == PropertyType.HOUSE
                    FilterChip(
                        selected = isHouseSelected,
                        onClick = {
                            viewModel.updatePropertyType(if (isHouseSelected) null else PropertyType.HOUSE)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isHouseSelected) Color.White else MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("Maison", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(CornerRadius.pill),
                        modifier = Modifier.testTag("home_filter_chip_house")
                    )

                    // Filtre rapide : Terrain
                    val isLandSelected = searchFilter.propertyType == PropertyType.LAND
                    FilterChip(
                        selected = isLandSelected,
                        onClick = {
                            viewModel.updatePropertyType(if (isLandSelected) null else PropertyType.LAND)
                        },
                        label = { Text("Terrain", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(CornerRadius.pill),
                        modifier = Modifier.testTag("home_filter_chip_land")
                    )

                    // Filtre rapide : Budget
                    val isBudgetActive = searchFilter.maxPrice != null
                    FilterChip(
                        selected = isBudgetActive,
                        onClick = { showBudgetDialog = true },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isBudgetActive) Color.White else DesignColors.Secondary
                            )
                        },
                        label = {
                            Text(
                                if (isBudgetActive) "Max ${searchFilter.maxPrice!!.toInt()} €" else "Budget max",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DesignColors.Secondary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(CornerRadius.pill),
                        modifier = Modifier.testTag("home_filter_chip_budget")
                    )

                    // Paliers de budget directs
                    val budgetPresets = listOf(
                        800.0 to "≤ 800 €",
                        1500.0 to "≤ 1 500 €",
                        300000.0 to "≤ 300 k€"
                    )
                    budgetPresets.forEach { (price, label) ->
                        val isPresetSelected = searchFilter.maxPrice == price
                        FilterChip(
                            selected = isPresetSelected,
                            onClick = {
                                viewModel.updateMaxPrice(if (isPresetSelected) null else price)
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(CornerRadius.pill)
                        )
                    }

                    // Bouton Réinitialiser si des filtres sont actifs
                    if (isFilteringActive) {
                        SuggestionChip(
                            onClick = { viewModel.clearSearchFilters() },
                            label = { Text("Effacer filtres ✕", fontSize = 11.sp, color = DesignColors.Error) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = DesignColors.Error.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(CornerRadius.pill),
                            border = null,
                            modifier = Modifier.testTag("home_filter_clear_button")
                        )
                    }
                }

                // Bannière de statut si recherche active
                if (isFilteringActive) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${displayProperties.size} bien(s) correspondant(s)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            TextButton(
                                onClick = { viewModel.navigateToTab(AppTab.SEARCH) },
                                contentPadding = PaddingValues(horizontal = Spacing.xs, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Voir sur la carte", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section Slides en haut de la page d'accueil
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .height(240.dp)
                    .clip(RoundedCornerShape(CornerRadius.xl))
                    .testTag("home_hero_slides")
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val slide = slides[page]
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = slide.imageRes),
                            contentDescription = slide.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dégradé sombre pour lisibilité optimale
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.45f),
                                            Color.Black.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )

                        // Contenu de la diapositive
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Surface(
                                color = slide.badgeColor,
                                shape = RoundedCornerShape(CornerRadius.pill)
                            ) {
                                Text(
                                    text = slide.badge,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Text(
                                text = slide.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = slide.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            Button(
                                onClick = slide.onClick,
                                shape = RoundedCornerShape(CornerRadius.md),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("slide_cta_${slide.id}")
                            ) {
                                Icon(
                                    slide.ctaIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(
                                    slide.ctaText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))
                        }
                    }
                }

                // Boutons de navigation Flèches Gauche / Droite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xs)
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val prev = if (pagerState.currentPage > 0) pagerState.currentPage - 1 else slides.size - 1
                                pagerState.animateScrollToPage(prev)
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                            .testTag("slide_prev_button")
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Précédent",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val next = (pagerState.currentPage + 1) % slides.size
                                pagerState.animateScrollToPage(next)
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                            .testTag("slide_next_button")
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Suivant",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Indicateurs de pagination (Dots) en bas à droite
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = Spacing.lg, bottom = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 20.dp else 6.dp)
                                .clip(RoundedCornerShape(CornerRadius.pill))
                                .background(
                                    if (isSelected) Color.White else Color.White.copy(alpha = 0.45f)
                                )
                                .clickable {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                }
                                .testTag("slide_dot_$index")
                        )
                    }
                }
            }
        }

        // =========================================================================
        // ACCÈS DIRECT AUX ESPACES PROFESSIONNELS & OPÉRATIONS CRUD
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(CornerRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                    .testTag("home_crud_spaces_card")
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Column {
                                Text(
                                    text = "Espaces Métier & Gestion CRUD",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Accédez aux opérations CRUD selon chaque rôle :",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        // 1. Bouton Espace ADMIN
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.sm),
                            color = DesignColors.PrimaryContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DesignColors.Primary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (currentUser?.role != UserRole.ADMIN) {
                                        viewModel.login("sophie.bernard@logement-admin.fr", "admin123")
                                    }
                                    viewModel.navigateToTab(AppTab.ADMIN)
                                }
                                .testTag("home_shortcut_admin_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👑", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("ADMINISTRATEUR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DesignColors.Primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(CornerRadius.pill),
                                            color = DesignColors.Primary
                                        ) {
                                            Text(
                                                "CRUD COMPLET",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text("Gestion annonces (modérer, éditer, supprimer), utilisateurs, visites & graphiques", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DesignColors.Primary, modifier = Modifier.size(18.dp))
                            }
                        }

                        // 2. Bouton Espace OWNER (Mes biens)
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.sm),
                            color = DesignColors.Accent.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DesignColors.Accent.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (currentUser?.role != UserRole.OWNER) {
                                        viewModel.login("claire.martin@proprietaire.fr", "password123")
                                    }
                                    viewModel.navigateToTab(AppTab.MY_PROPERTIES)
                                }
                                .testTag("home_shortcut_owner_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("PROPRIÉTAIRE (Mes biens)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(CornerRadius.pill),
                                            color = DesignColors.Accent
                                        ) {
                                            Text(
                                                "CRUD BIENS",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text("Consulter mes biens, bouton Ajouter un bien, Modifier et Supprimer (Room)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }

                        // 3. Bouton Espace AGENT (Mes mandats)
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.sm),
                            color = DesignColors.Secondary.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DesignColors.Secondary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (currentUser?.role != UserRole.AGENT) {
                                        viewModel.login("marc.lefebvre@agence-horizon.fr", "password123")
                                    }
                                    viewModel.navigateToTab(AppTab.MY_PROPERTIES)
                                }
                                .testTag("home_shortcut_agent_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏢", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("AGENT IMMOBILIER (Mes mandats)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(CornerRadius.pill),
                                            color = DesignColors.Secondary
                                        ) {
                                            Text(
                                                "CRUD MANDATS",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text("Agence Horizon, nouveau mandat, modifier et retirer mes mandats (Room)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Quick Category Selector Chips (Tous les biens, Locations, Ventes, etc.)
        item {
            Column(modifier = Modifier.padding(top = Spacing.sm)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    FilterChip(
                        selected = searchFilter.transactionType == null,
                        onClick = { viewModel.updateTransactionType(null) },
                        label = { Text("Toutes transactions") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = searchFilter.transactionType == TransactionType.RENT,
                        onClick = { viewModel.updateTransactionType(TransactionType.RENT) },
                        label = { Text("Locations") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = searchFilter.transactionType == TransactionType.SALE,
                        onClick = { viewModel.updateTransactionType(TransactionType.SALE) },
                        label = { Text("Ventes") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )

                    PropertyType.entries.forEach { pType ->
                        FilterChip(
                            selected = searchFilter.propertyType == pType,
                            onClick = {
                                viewModel.updatePropertyType(if (searchFilter.propertyType == pType) null else pType)
                            },
                            label = { Text(pType.label) }
                        )
                    }
                }
            }
        }

        // Section: Listings Header
        item {
            Spacer(modifier = Modifier.height(Spacing.lg))
            SectionHeader(
                title = if (isFilteringActive) "Résultats de recherche 🔍" else "Les plus consultés 🔥",
                subtitle = if (isFilteringActive) "Biens correspondant à vos critères actuels" else "Sélection des biens les plus attractifs cette semaine",
                actionText = "Voir tout",
                onActionClick = { viewModel.navigateToTab(AppTab.SEARCH) }
            )
        }

        if (displayProperties.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Home,
                    title = if (isFilteringActive) "Aucun bien ne correspond à ces critères" else "Aucune annonce disponible",
                    description = if (isFilteringActive) "Essayez d'ajuster votre budget ou d'élargir votre recherche géographique." else "Les nouvelles annonces publiées apparaîtront ici dès validation.",
                    modifier = Modifier.padding(Spacing.lg)
                )
            }
        } else {
            items(displayProperties, key = { it.property.id }) { item ->
                Box(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)) {
                    PropertyCard(
                        propertyDetails = item,
                        onClick = { viewModel.openPropertyDetail(item.property.id) },
                        onToggleFavorite = {
                            if (!isAuthenticated) {
                                authRequiredAction = "Ajouter aux favoris"
                            } else {
                                viewModel.toggleFavorite(item.property.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // =========================================================================
    // DIALOGUE SÉLECTEUR DE BUDGET RAPIDE
    // =========================================================================
    if (showBudgetDialog) {
        var tempCustomPrice by remember {
            mutableStateOf(searchFilter.maxPrice?.toInt()?.toString() ?: "")
        }

        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = {
                Text(
                    text = "Définir un budget maximum",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(
                        text = "Sélectionnez un palier ou saisissez un montant personnalisé :",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Paliers pour location
                    Text(text = "Locations mensuelles :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        listOf(600.0, 900.0, 1300.0, 2000.0).forEach { price ->
                            Button(
                                onClick = {
                                    viewModel.updateMaxPrice(price)
                                    showBudgetDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (searchFilter.maxPrice == price) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (searchFilter.maxPrice == price) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("${price.toInt()}€", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Paliers pour achat
                    Text(text = "Achat / Vente :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        listOf(150000.0, 250000.0, 400000.0, 600000.0).forEach { price ->
                            Button(
                                onClick = {
                                    viewModel.updateMaxPrice(price)
                                    showBudgetDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (searchFilter.maxPrice == price) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (searchFilter.maxPrice == price) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("${(price / 1000).toInt()}k€", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Montant sur-mesure
                    OutlinedTextField(
                        value = tempCustomPrice,
                        onValueChange = { tempCustomPrice = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Montant exact en €") },
                        placeholder = { Text("Ex: 1250") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempCustomPrice.toDoubleOrNull()
                        viewModel.updateMaxPrice(parsed)
                        showBudgetDialog = false
                    }
                ) {
                    Text("Appliquer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.updateMaxPrice(null)
                        showBudgetDialog = false
                    }
                ) {
                    Text("Effacer budget")
                }
            }
        )
    }

    // Feuille complète de filtres
    if (showFilterSheet) {
        FilterBottomSheet(
            sheetState = sheetState,
            currentFilter = searchFilter,
            onApplyFilter = { newFilter ->
                viewModel.applySearchFilter(newFilter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (authRequiredAction != null) {
        val actionName = authRequiredAction!!
        AlertDialog(
            onDismissRequest = { authRequiredAction = null },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Connexion requise",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Pour pouvoir \"$actionName\", vous devez disposer d'un compte actif sur la plateforme.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Button(
                        onClick = {
                            authRequiredAction = null
                            viewModel.requestAuth("LOGIN")
                        },
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Se connecter", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            authRequiredAction = null
                            viewModel.requestAuth("REGISTER")
                        },
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Créer un compte", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { authRequiredAction = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuer sans compte", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
