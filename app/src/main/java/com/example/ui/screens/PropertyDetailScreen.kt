package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.viewmodel.PendingAction
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PropertyType
import com.example.data.model.PropertyWithDetails
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.ui.components.AvatarWithBadge
import com.example.ui.components.ReportDialog
import com.example.ui.components.StatusPill
import com.example.ui.components.VisitRequestDialog
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.MainViewModel

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Landscape

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    propertyDetails: PropertyWithDetails,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val prop = propertyDetails.property
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val images = propertyDetails.images.ifEmpty {
        listOf(com.example.data.model.PropertyImage(propertyId = prop.id, imageUrl = propertyDetails.primaryImageUrl))
    }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { images.size })
    var showVisitDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showRentOrBuyDialog by remember { mutableStateOf(false) }
    var authRequiredAction by remember { mutableStateOf<String?>(null) }
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()

    val autoOpenVisitDialog by viewModel.autoOpenVisitDialog.collectAsStateWithLifecycle()
    val autoOpenRentOrBuyDialog by viewModel.autoOpenRentOrBuyDialog.collectAsStateWithLifecycle()

    LaunchedEffect(autoOpenVisitDialog) {
        if (autoOpenVisitDialog == prop.id) {
            showVisitDialog = true
            viewModel.clearAutoOpenDialogs()
        }
    }

    LaunchedEffect(autoOpenRentOrBuyDialog) {
        if (autoOpenRentOrBuyDialog == prop.id) {
            showRentOrBuyDialog = true
            viewModel.clearAutoOpenDialogs()
        }
    }

    val isMyProperty = prop.ownerId == currentUser?.id || prop.managedByAgentId == currentUser?.id

    BackHandler {
        viewModel.closePropertyDetail()
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = Elevation.overlay,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMyProperty) {
                        OutlinedButton(
                            onClick = {
                                viewModel.closePropertyDetail()
                                viewModel.startEditingProperty(prop)
                            },
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Modifier l'annonce", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        // Bouton Louer / Acheter (Action principale selon type de transaction)
                        val isRent = prop.transactionType == TransactionType.RENT
                        Button(
                            onClick = {
                                if (!isAuthenticated) {
                                    authRequiredAction = if (isRent) "Louer" else "Acheter"
                                } else {
                                    showRentOrBuyDialog = true
                                }
                            },
                            shape = RoundedCornerShape(CornerRadius.md),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRent) DesignColors.Primary else DesignColors.Secondary
                            ),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(50.dp)
                                .testTag("rent_or_buy_btn")
                        ) {
                            Icon(
                                imageVector = if (isRent) Icons.Default.Key else Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = if (isRent) "Louer ce bien" else "Acheter ce bien",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }

                        // Bouton Visite (Protégé : compte requis)
                        OutlinedButton(
                            onClick = {
                                if (!isAuthenticated) {
                                    authRequiredAction = "Réserver une visite"
                                } else {
                                    showVisitDialog = true
                                }
                            },
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("request_visit_btn"),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = "Visite",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Bouton Message / Contacter propriétaire (Protégé : compte requis)
                        OutlinedButton(
                            onClick = {
                                val targetContactUser = propertyDetails.agent ?: propertyDetails.owner
                                if (!isAuthenticated) {
                                    authRequiredAction = "Contacter le bailleur"
                                } else if (targetContactUser != null) {
                                    viewModel.startConversationWithPropertyOwner(prop.id, targetContactUser)
                                }
                            },
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("contact_owner_btn"),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .testTag("property_detail_scroll")
        ) {
            // Immersive Image Carousel with HorizontalPager
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().testTag("detail_image_pager")
                ) { page ->
                    val imgUrl = images.getOrNull(page)?.imageUrl.orEmpty()
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = "${prop.title} - photo ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Top Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                            )
                        )
                )

                // Floating Actions Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.closePropertyDetail() },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            .size(42.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        IconButton(
                            onClick = {
                                if (!isAuthenticated) {
                                    authRequiredAction = "Ajouter aux favoris"
                                } else {
                                    viewModel.toggleFavorite(prop.id)
                                }
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .size(42.dp)
                                .testTag("detail_fav_btn")
                        ) {
                            Icon(
                                imageVector = if (propertyDetails.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favori",
                                tint = if (propertyDetails.isFavorite) Color(0xFFEF4444) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showReportDialog = true },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .size(42.dp)
                                .testTag("detail_report_btn")
                        ) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = "Signaler",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Left / Right Arrows if multiple images
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.sm)
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val prev = if (pagerState.currentPage > 0) pagerState.currentPage - 1 else images.size - 1
                                    pagerState.animateScrollToPage(prev)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                                .testTag("detail_prev_image_btn")
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Photo précédente",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val next = (pagerState.currentPage + 1) % images.size
                                    pagerState.animateScrollToPage(next)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                                .testTag("detail_next_image_btn")
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Photo suivante",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Counter Pill
                if (images.size > 1) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(CornerRadius.pill),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(Spacing.md)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${images.size}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                        )
                    }
                }
            }

            // Thumbnail Strip
            if (images.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    itemsIndexed(images) { index, img ->
                        val isSelected = index == pagerState.currentPage
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.sm),
                            modifier = Modifier
                                .size(58.dp)
                                .clickable {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            AsyncImage(
                                model = img.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(if (isSelected) 2.dp else 0.dp)
                                    .clip(RoundedCornerShape(CornerRadius.xs))
                            )
                        }
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusPill(
                        text = prop.transactionType.label.uppercase(),
                        backgroundColor = if (prop.transactionType == TransactionType.RENT) DesignColors.PrimaryLight else DesignColors.Secondary
                    )

                    StatusPill(
                        text = prop.propertyType.label,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "${prop.viewCount} vues",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = prop.formattedPrice,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = prop.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "${prop.address}, ${prop.district}, ${prop.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Key Specs Grid
                Text(
                    text = "Caractéristiques clés",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    if (prop.area > 0) {
                        SpecChip(icon = Icons.Default.SquareFoot, label = "Superficie", value = "${prop.area.toInt()} m²")
                    }
                    if (prop.bedrooms > 0) {
                        SpecChip(icon = Icons.Default.Bed, label = "Chambres", value = "${prop.bedrooms}")
                    }
                    if (prop.bathrooms > 0) {
                        SpecChip(icon = Icons.Default.Bathtub, label = "Salles d'eau", value = "${prop.bathrooms}")
                    }
                    if (prop.rooms > 0) {
                        SpecChip(icon = Icons.Default.MeetingRoom, label = "Pièces", value = "${prop.rooms}")
                    }
                    if (prop.propertyType != PropertyType.LAND && prop.propertyType != PropertyType.HOUSE && prop.floor > 0) {
                        SpecChip(icon = Icons.Default.Layers, label = "Étage", value = "${prop.floor}e")
                    } else if (prop.propertyType == PropertyType.APARTMENT && prop.floor == 0) {
                        SpecChip(icon = Icons.Default.Layers, label = "Étage", value = "RDC")
                    }
                    if (prop.propertyType != PropertyType.LAND) {
                        SpecChip(
                            icon = Icons.Default.DirectionsCar,
                            label = "Parking",
                            value = if (prop.parking) "Inclus" else "Aucun",
                            highlight = prop.parking
                        )
                    } else {
                        SpecChip(
                            icon = Icons.Default.Landscape,
                            label = "Type",
                            value = "Viabilisé",
                            highlight = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Équipements et caractéristiques
                Text(
                    text = "Équipements et caractéristiques",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    val amenities = when (prop.propertyType) {
                        PropertyType.LAND -> listOf("Terrain viabilisé", "Eau & Électricité", "Libre constructeur", "Fibre optique", "Accès voirie")
                        PropertyType.HOUSE, PropertyType.VILLA -> listOf("Jardin privatif", "Cuisine équipée", "Double vitrage", "Fibre optique", "Chauffage individuel", "Garage")
                        else -> listOf("Cuisine aménagée", "Double vitrage", "Fibre optique", "Interphone / Digicode", "Chauffage performant", "Proche commerces")
                    }
                    amenities.forEach { feature ->
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.pill),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = feature,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "Description du bien",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = prop.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))

                // Contact Profile
                val contactPerson = propertyDetails.agent ?: propertyDetails.owner
                Text(
                    text = if (propertyDetails.agent != null) "Agent immobilier mandaté" else if (propertyDetails.owner != null) "Propriétaire bailleur" else "Contact officiel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Card(
                    shape = RoundedCornerShape(CornerRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarWithBadge(
                            photoUrl = contactPerson?.photoUrl ?: "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=300&q=80",
                            fullName = contactPerson?.fullName ?: "Agence Horizon Immobilier",
                            isVerified = contactPerson?.emailVerified ?: true,
                            size = 56.dp
                        )

                        Spacer(modifier = Modifier.width(Spacing.md))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contactPerson?.fullName ?: "Horizon Immobilier Prestige",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = if (contactPerson?.role == UserRole.AGENT) "Horizon Immobilier Prestige" else if (contactPerson?.role == UserRole.OWNER) "Bailleur certifié" else "Service Transaction & Gestion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(
                                    text = contactPerson?.phone?.ifBlank { "01 42 68 55 00" } ?: "01 42 68 55 00",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVisitDialog) {
        VisitRequestDialog(
            propertyTitle = prop.title,
            onConfirm = { date, time, msg ->
                viewModel.requestVisit(prop.id, date, time, msg)
                showVisitDialog = false
            },
            onDismiss = { showVisitDialog = false }
        )
    }

    if (showReportDialog) {
        ReportDialog(
            propertyTitle = prop.title,
            onConfirm = { reason, desc ->
                viewModel.reportProperty(prop.id, reason, desc)
                showReportDialog = false
            },
            onDismiss = { showReportDialog = false }
        )
    }

    if (showRentOrBuyDialog) {
        RentalOrPurchaseDialog(
            property = prop,
            currentUser = currentUser,
            onConfirm = { proposedPrice, dateOrFinancing, applicantMessage ->
                viewModel.submitRentalOrPurchaseApplication(
                    prop.id,
                    prop.transactionType,
                    proposedPrice,
                    dateOrFinancing,
                    applicantMessage
                )
                showRentOrBuyDialog = false
            },
            onDismiss = { showRentOrBuyDialog = false }
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pour pouvoir \"$actionName\" pour ce logement, vous devez disposer d'un compte actif sur la plateforme.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "Veuillez vous connecter ou créer votre compte pour continuer :",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
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
                            val action = when (actionName) {
                                "Louer", "Acheter" -> PendingAction.RentOrBuy(prop.id, prop.transactionType)
                                "Réserver une visite" -> PendingAction.RequestVisit(prop.id)
                                "Contacter le bailleur" -> {
                                    val target = propertyDetails.agent ?: propertyDetails.owner
                                    if (target != null) PendingAction.ContactOwner(prop.id, target) else PendingAction.OpenProperty(prop.id)
                                }
                                "Ajouter aux favoris" -> PendingAction.ToggleFavorite(prop.id)
                                else -> PendingAction.OpenProperty(prop.id)
                            }
                            viewModel.setPendingAction(action)
                            authRequiredAction = null
                            viewModel.requestAuth("LOGIN")
                        },
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().testTag("auth_dialog_login_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Se connecter", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val action = when (actionName) {
                                "Louer", "Acheter" -> PendingAction.RentOrBuy(prop.id, prop.transactionType)
                                "Réserver une visite" -> PendingAction.RequestVisit(prop.id)
                                "Contacter le bailleur" -> {
                                    val target = propertyDetails.agent ?: propertyDetails.owner
                                    if (target != null) PendingAction.ContactOwner(prop.id, target) else PendingAction.OpenProperty(prop.id)
                                }
                                "Ajouter aux favoris" -> PendingAction.ToggleFavorite(prop.id)
                                else -> PendingAction.OpenProperty(prop.id)
                            }
                            viewModel.setPendingAction(action)
                            authRequiredAction = null
                            viewModel.requestAuth("REGISTER")
                        },
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("auth_dialog_register_btn")
                    ) {
                        Text("Créer un compte", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { authRequiredAction = null },
                    modifier = Modifier.fillMaxWidth().testTag("auth_dialog_dismiss_btn")
                ) {
                    Text("Continuer la visite libre", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun SpecChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(CornerRadius.md),
        color = if (highlight) DesignColors.SecondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (highlight) DesignColors.Secondary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.width(106.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlight) DesignColors.Secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) DesignColors.Secondary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RentalOrPurchaseDialog(
    property: com.example.data.model.Property,
    currentUser: com.example.data.model.User?,
    onConfirm: (proposedPrice: Double, dateOrFinancing: String, applicantMessage: String) -> Unit,
    onDismiss: () -> Unit
) {
    val isRent = property.transactionType == TransactionType.RENT
    var proposedPrice by remember { mutableStateOf(property.price.toInt().toString()) }
    var dateOrFinancing by remember { mutableStateOf(if (isRent) "1er du mois prochain" else "Prêt bancaire pré-accordé") }
    var applicantMessage by remember { mutableStateOf(if (isRent) "Dossier complet, CDI confirmé avec garant." else "Offre ferme sous réserve de financement.") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isRent) "Candidature de location" else "Offre d'achat officielle",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = "${property.title} • ${property.city}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = DesignColors.Error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = if (isRent) "Loyer proposé (€/mois) :" else "Montant de l'offre (€) :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = proposedPrice,
                    onValueChange = { proposedPrice = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("application_price_input")
                )

                Text(
                    text = if (isRent) "Date d'emménagement souhaitée :" else "Plan de financement :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = dateOrFinancing,
                    onValueChange = { dateOrFinancing = it },
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("application_date_financing_input")
                )

                Text(
                    text = "Message et garanties :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = applicantMessage,
                    onValueChange = { applicantMessage = it },
                    minLines = 3,
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("application_message_input")
                )

                if (currentUser != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(CornerRadius.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Demandeur : ${currentUser.fullName}\nTél : ${currentUser.phone} • Email : ${currentUser.email}",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(Spacing.sm),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = proposedPrice.toDoubleOrNull()
                    if (priceVal == null || priceVal <= 0) {
                        errorText = "Veuillez saisir un montant valide."
                        return@Button
                    }
                    if (dateOrFinancing.isBlank()) {
                        errorText = "Veuillez renseigner ce champ."
                        return@Button
                    }
                    onConfirm(priceVal, dateOrFinancing.trim(), applicantMessage.trim())
                },
                shape = RoundedCornerShape(CornerRadius.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRent) DesignColors.Primary else DesignColors.Secondary
                ),
                modifier = Modifier.testTag("submit_application_btn")
            ) {
                Text(
                    text = if (isRent) "Transmettre mon dossier" else "Transmettre mon offre",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

