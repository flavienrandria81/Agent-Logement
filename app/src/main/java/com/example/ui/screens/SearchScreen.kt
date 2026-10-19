package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import com.example.data.model.PropertyType
import com.example.data.model.TransactionType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.components.PropertyCard
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SearchDisplayMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProperties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val displayMode by viewModel.searchDisplayMode.collectAsStateWithLifecycle()
    val selectedPropertyId by viewModel.selectedPropertyId.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    var authRequiredAction by remember { mutableStateOf<String?>(null) }

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OutlinedTextField(
                    value = searchFilter.query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Ville, quartier, mot-clé...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchFilter.query.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_query_input")
                )

                // Filter Sheet Toggle Button
                Surface(
                    shape = RoundedCornerShape(CornerRadius.md),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.fillMaxSize().testTag("filter_button")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Filtres",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Quick Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAllSelected = searchFilter.propertyType == null && searchFilter.transactionType == null
                FilterChip(
                    selected = isAllSelected,
                    onClick = {
                        viewModel.updatePropertyType(null)
                        viewModel.updateTransactionType(null)
                    },
                    label = { Text("Tous", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(CornerRadius.pill),
                    modifier = Modifier.testTag("search_chip_all")
                )

                val isRentSelected = searchFilter.transactionType == TransactionType.RENT
                FilterChip(
                    selected = isRentSelected,
                    onClick = {
                        viewModel.updateTransactionType(if (isRentSelected) null else TransactionType.RENT)
                    },
                    label = { Text("Locations", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(CornerRadius.pill),
                    modifier = Modifier.testTag("search_chip_rent")
                )

                val isSaleSelected = searchFilter.transactionType == TransactionType.SALE
                FilterChip(
                    selected = isSaleSelected,
                    onClick = {
                        viewModel.updateTransactionType(if (isSaleSelected) null else TransactionType.SALE)
                    },
                    label = { Text("Ventes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(CornerRadius.pill),
                    modifier = Modifier.testTag("search_chip_sale")
                )

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
                            modifier = Modifier.size(14.dp),
                            tint = if (isHouseSelected) Color.White else MaterialTheme.colorScheme.primary
                        )
                    },
                    label = { Text("Maisons", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(CornerRadius.pill),
                    modifier = Modifier.testTag("search_chip_houses")
                )

                val isAptSelected = searchFilter.propertyType == PropertyType.APARTMENT
                FilterChip(
                    selected = isAptSelected,
                    onClick = {
                        viewModel.updatePropertyType(if (isAptSelected) null else PropertyType.APARTMENT)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Apartment,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isAptSelected) Color.White else MaterialTheme.colorScheme.primary
                        )
                    },
                    label = { Text("Appartements", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(CornerRadius.pill),
                    modifier = Modifier.testTag("search_chip_apartments")
                )

                val isLandSelected = searchFilter.propertyType == PropertyType.LAND
                FilterChip(
                    selected = isLandSelected,
                    onClick = {
                        viewModel.updatePropertyType(if (isLandSelected) null else PropertyType.LAND)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Landscape,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isLandSelected) Color.White else MaterialTheme.colorScheme.primary
                        )
                    },
                    label = { Text("Terrains", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(CornerRadius.pill),
                    modifier = Modifier.testTag("search_chip_land")
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Subheader Controls: Count & List/Map Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProperties.size} logement(s) trouvé(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                // List vs Map Toggle Switch
                Surface(
                    shape = RoundedCornerShape(CornerRadius.pill),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        val isList = displayMode == SearchDisplayMode.LIST
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.pill),
                            color = if (isList) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .testTag("list_mode_btn")
                        ) {
                            IconButton(
                                onClick = { viewModel.setSearchDisplayMode(SearchDisplayMode.LIST) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.FormatListBulleted,
                                    contentDescription = "Liste",
                                    tint = if (isList) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(CornerRadius.pill),
                            color = if (!isList) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .testTag("map_mode_btn")
                        ) {
                            IconButton(
                                onClick = { viewModel.setSearchDisplayMode(SearchDisplayMode.MAP) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = "Carte",
                                    tint = if (!isList) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // View Mode: Map or List
        if (displayMode == SearchDisplayMode.MAP) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg)) {
                InteractiveMapCanvas(
                    properties = filteredProperties,
                    selectedPropertyId = selectedPropertyId,
                    onSelectProperty = { propId -> viewModel.openPropertyDetail(propId) },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        } else {
            if (filteredProperties.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Search,
                    title = "Aucun logement ne correspond",
                    description = "Essayez d'élargir vos filtres ou de modifier votre terme de recherche.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize().testTag("search_results_list")
                ) {
                    items(filteredProperties, key = { it.property.id }) { item ->
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
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            sheetState = sheetState,
            currentFilter = searchFilter,
            onApplyFilter = { newFilter -> viewModel.applySearchFilter(newFilter) },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (authRequiredAction != null) {
        val actionName = authRequiredAction!!
        AlertDialog(
            onDismissRequest = { authRequiredAction = null },
            icon = {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
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
                    Text("Continuer la recherche", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}