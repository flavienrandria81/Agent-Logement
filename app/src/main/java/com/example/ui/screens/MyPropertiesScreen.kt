package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.AccountStatus
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyWithDetails
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MyPropertiesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val myProperties by viewModel.myProperties.collectAsStateWithLifecycle()
    val ownerStats by viewModel.ownerStats.collectAsStateWithLifecycle()
    val agentStats by viewModel.agentStats.collectAsStateWithLifecycle()
    val agency by viewModel.agencyForCurrentAgent.collectAsStateWithLifecycle()

    val isAgent = currentUser?.role == UserRole.AGENT

    var selectedFilterStatus by remember { mutableStateOf<PropertyStatus?>(null) }
    var propertyToDelete by remember { mutableStateOf<com.example.data.model.Property?>(null) }

    val filteredList = myProperties.filter {
        selectedFilterStatus == null || it.property.status == selectedFilterStatus
    }

    // Protection compte en attente de validation ADMIN
    val isPendingAccount = currentUser != null && !currentUser!!.isApproved && (currentUser!!.role == UserRole.OWNER || currentUser!!.role == UserRole.AGENT)
    if (isPendingAccount) {
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
                color = DesignColors.Warning.copy(alpha = 0.15f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = DesignColors.Warning,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text = "Compte en attente de validation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Votre compte est en attente de validation par l'administrateur. Vos fonctionnalités et votre espace personnel seront activés dès validation.",
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("my_properties_screen"),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Space Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isAgent) "Mes Mandats Agence" else "Mes Biens Immobiliers",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(Spacing.xxs))
                        Text(
                            text = if (isAgent) "Gestion professionnelle des biens attribués à votre agence."
                            else "Gérez vos annonces, vos disponibilités et suivez vos statistiques.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.navigateToTab(AppTab.PUBLISH) },
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
                        modifier = Modifier.testTag("publish_new_property_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(if (isAgent) "Nouveau mandat" else "Ajouter un bien", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

        // AGENT Specific Agency Banner Card
        if (isAgent) {
            item {
                Card(
                    shape = RoundedCornerShape(CornerRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = DesignColors.PrimaryContainer.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, DesignColors.Primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DesignColors.Primary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(Spacing.md))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = agency?.name ?: "Horizon Immobilier Prestige",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Surface(
                                    shape = RoundedCornerShape(CornerRadius.pill),
                                    color = DesignColors.Secondary
                                ) {
                                    Text(
                                        text = "AGENCE AGRÉÉE",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Agent mandaté : ${currentUser?.fullName} • Tél pro : ${currentUser?.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Adresse agence : ${agency?.address ?: "14 Avenue Montaigne, 75008 Paris"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Stats KPI Row
        item {
            if (isAgent) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    MetricCard("Mandats", "${agentStats.managedPropertiesCount}", Icons.Default.Apartment, DesignColors.Primary, Modifier.weight(1f))
                    MetricCard("En ligne", "${agentStats.publishedCount}", Icons.Default.PublishedWithChanges, DesignColors.Secondary, Modifier.weight(1f))
                    MetricCard("Vues", "${agentStats.totalViews}", Icons.Default.Visibility, DesignColors.Accent, Modifier.weight(1f))
                    MetricCard("Visites", "${agentStats.pendingVisitsCount}", Icons.Default.CalendarMonth, DesignColors.Warning, Modifier.weight(1f))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    MetricCard("Total biens", "${ownerStats.totalProperties}", Icons.Default.Home, DesignColors.Primary, Modifier.weight(1f))
                    MetricCard("En ligne", "${ownerStats.publishedCount}", Icons.Default.PublishedWithChanges, DesignColors.Secondary, Modifier.weight(1f))
                    MetricCard("En attente", "${ownerStats.pendingCount}", Icons.Default.HourglassTop, DesignColors.Warning, Modifier.weight(1f))
                    MetricCard("Vues cumulées", "${ownerStats.totalViews}", Icons.Default.Visibility, DesignColors.Accent, Modifier.weight(1f))
                }
            }
        }

        // Status Filter Chips Strip
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilterStatus == null,
                        onClick = { selectedFilterStatus = null },
                        label = { Text("Tous (${myProperties.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                PropertyStatus.entries.forEach { status ->
                    val count = myProperties.count { it.property.status == status }
                    item {
                        FilterChip(
                            selected = selectedFilterStatus == status,
                            onClick = { selectedFilterStatus = status },
                            label = { Text("${status.label} ($count)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Properties List
        if (filteredList.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Home,
                    title = "Aucun bien dans cette catégorie",
                    description = if (isAgent) "Vous n'avez pas de mandat avec ce statut actuellement."
                    else "Déposez une annonce pour la publier ou l'enregistrer en brouillon.",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(filteredList, key = { it.property.id }) { item ->
                val prop = item.property
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.primaryImageUrl,
                                contentDescription = prop.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(CornerRadius.sm))
                            )

                            Spacer(modifier = Modifier.width(Spacing.md))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prop.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${prop.city} (${prop.district}) • ${prop.formattedPrice}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(Spacing.xxs))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    StatusPill(
                                        text = prop.status.label,
                                        backgroundColor = when (prop.status) {
                                            PropertyStatus.PUBLISHED -> DesignColors.Secondary
                                            PropertyStatus.PENDING -> DesignColors.Warning
                                            PropertyStatus.REJECTED -> DesignColors.Error
                                            PropertyStatus.DRAFT -> MaterialTheme.colorScheme.surfaceVariant
                                            else -> DesignColors.PrimaryLight
                                        }
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${prop.viewCount} vues",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Owner / Agent Actions menu
                            IconButton(
                                onClick = { propertyToDelete = prop },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = DesignColors.Error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // Action Buttons Bar according to status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openPropertyDetail(prop.id) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Consulter", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.startEditingProperty(prop) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Modifier", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (prop.status == PropertyStatus.DRAFT) {
                                Button(
                                    onClick = { viewModel.updatePropertyStatus(prop.id, PropertyStatus.PENDING) },
                                    shape = RoundedCornerShape(CornerRadius.sm),
                                    colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Secondary),
                                    modifier = Modifier.weight(1.3f).height(38.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Soumettre", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (prop.status == PropertyStatus.PUBLISHED) {
                                OutlinedButton(
                                    onClick = { viewModel.updatePropertyStatus(prop.id, PropertyStatus.RENTED) },
                                    shape = RoundedCornerShape(CornerRadius.sm),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text("Loué", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.updatePropertyStatus(prop.id, PropertyStatus.SOLD) },
                                    shape = RoundedCornerShape(CornerRadius.sm),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text("Vendu", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }

    if (propertyToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { propertyToDelete = null },
            title = { Text("Supprimer cette annonce ?") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement l'annonce « ${propertyToDelete?.title} » ? Cette action est irréversible.") },
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
                androidx.compose.material3.TextButton(onClick = { propertyToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(CornerRadius.md),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}
