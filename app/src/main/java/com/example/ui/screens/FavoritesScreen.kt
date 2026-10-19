package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.components.PropertyCard
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val myFavorites by viewModel.myFavorites.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val filteredFavorites = myFavorites.filter { item ->
        if (searchQuery.isBlank()) true else {
            item.property.title.contains(searchQuery, ignoreCase = true) ||
            item.property.city.contains(searchQuery, ignoreCase = true) ||
            item.property.district.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("favorites_screen"),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mes Favoris",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = "Vos logements coups de cœur réunis au même endroit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(CornerRadius.pill),
                    color = DesignColors.PrimaryContainer
                ) {
                    Text(
                        text = "${myFavorites.size} biens",
                        color = DesignColors.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    )
                }
            }
        }

        if (myFavorites.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filtrer parmi vos favoris...", fontSize = 13.sp) },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (filteredFavorites.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Favorite,
                    title = if (myFavorites.isEmpty()) "Aucun logement sauvegardé" else "Aucun résultat trouvé",
                    description = if (myFavorites.isEmpty()) "Explorez les annonces de location ou vente et appuyez sur le cœur pour les garder en mémoire."
                    else "Aucun bien en favori ne correspond à votre recherche.",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(filteredFavorites, key = { it.property.id }) { item ->
                PropertyCard(
                    propertyDetails = item,
                    onClick = { viewModel.openPropertyDetail(item.property.id) },
                    onToggleFavorite = { viewModel.toggleFavorite(item.property.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}
