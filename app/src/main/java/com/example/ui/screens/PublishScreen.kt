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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PropertyType
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.MainViewModel

val SAMPLE_PHOTOS = listOf(
    "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80"
)

@Composable
fun PublishScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val editingProperty by viewModel.editingProperty.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedTransaction by remember { mutableStateOf(TransactionType.RENT) }
    var selectedType by remember { mutableStateOf(PropertyType.APARTMENT) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Paris") }
    var district by remember { mutableStateOf("11ème") }
    var areaText by remember { mutableStateOf("65") }
    var bedrooms by remember { mutableIntStateOf(2) }
    var bathrooms by remember { mutableIntStateOf(1) }
    var rooms by remember { mutableIntStateOf(3) }
    var floor by remember { mutableIntStateOf(2) }
    var hasParking by remember { mutableStateOf(false) }

    val selectedImages = remember { mutableStateListOf<String>().apply { add(SAMPLE_PHOTOS[0]) } }
    var customImageUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // When editingProperty changes, populate fields
    LaunchedEffect(editingProperty) {
        editingProperty?.let { p ->
            title = p.title
            description = p.description
            priceText = p.price.toInt().toString()
            selectedTransaction = p.transactionType
            selectedType = p.propertyType
            address = p.address
            city = p.city
            district = p.district
            areaText = p.area.toInt().toString()
            bedrooms = p.bedrooms
            bathrooms = p.bathrooms
            rooms = p.rooms
            floor = p.floor
            hasParking = p.parking
        }
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
                text = "Votre compte est en attente de validation par l'administrateur. Vous ne pouvez pas déposer d'annonce tant qu'un administrateur n'a pas validé votre compte.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
            OutlinedButton(
                onClick = { viewModel.navigateToTab(com.example.ui.viewmodel.AppTab.HOME) },
                shape = RoundedCornerShape(CornerRadius.md)
            ) {
                Text("Retour à l'accueil", fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }

    // Role check: If USER, inform that publishing is reserved for OWNER and AGENT
    if (currentUser?.role == UserRole.USER) {
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
                color = DesignColors.WarningContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = DesignColors.Warning,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Espace Réservé aux Bailleurs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Votre profil actuel est Chercheur de logement (USER). Seuls les propriétaires (OWNER) et les agents immobiliers (AGENT) peuvent publier des annonces.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Button(
                onClick = { viewModel.navigateToTab(com.example.ui.viewmodel.AppTab.HOME) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(CornerRadius.md),
                modifier = Modifier.fillMaxWidth(0.85f).testTag("user_back_home_btn")
            ) {
                Text("Consulter les annonces", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    viewModel.requestAuth("REGISTER")
                },
                shape = RoundedCornerShape(CornerRadius.md),
                modifier = Modifier.fillMaxWidth(0.85f).testTag("user_switch_to_owner_agent_btn")
            ) {
                Text("Créer un compte Propriétaire ou Agent", fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
            .testTag("publish_screen_scroll"),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Page Header
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when {
                            editingProperty != null -> "Modifier l'annonce"
                            currentUser?.role == UserRole.AGENT -> "Nouveau mandat agence"
                            currentUser?.role == UserRole.ADMIN -> "Créer une annonce (Admin)"
                            else -> "Déposer un bien immobilier"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = when {
                            editingProperty != null -> "Mettez à jour les caractéristiques et informations du logement."
                            currentUser?.role == UserRole.AGENT -> "Renseignez les détails du mandat confié à votre agence."
                            currentUser?.role == UserRole.ADMIN -> "Créez une annonce directement avec validation administrative."
                            else -> "Renseignez les détails de votre bien pour le proposer en location ou en vente."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (editingProperty != null) {
                    OutlinedButton(
                        onClick = { viewModel.cancelEditingProperty() },
                        shape = RoundedCornerShape(CornerRadius.sm)
                    ) {
                        Text("Annuler", fontSize = 12.sp)
                    }
                }
            }
        }

        if (errorMessage != null) {
            Card(
                shape = RoundedCornerShape(CornerRadius.md),
                colors = CardDefaults.cardColors(containerColor = DesignColors.ErrorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DesignColors.Error)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(text = errorMessage!!, color = DesignColors.Error, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }

        // Section 1: Transaction & General Info
        FormSectionCard(title = "Type d'offre et catégorie") {
            Text(text = "Projet immobilier :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(Spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf(TransactionType.RENT to "Location", TransactionType.SALE to "Vente").forEach { (type, label) ->
                    val isSelected = selectedTransaction == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTransaction = type },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(text = "Type de bien :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(Spacing.xs))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                items(PropertyType.entries) { pType ->
                    val isSelected = selectedType == pType
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = pType },
                        label = { Text(pType.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Section 2: Details & Pricing
        FormSectionCard(title = "Titre & Description") {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre de l'annonce *") },
                placeholder = { Text("Ex: Superbe appartement haussmannien lumineux") },
                singleLine = true,
                shape = RoundedCornerShape(CornerRadius.md),
                colors = outlinedColors(),
                modifier = Modifier.fillMaxWidth().testTag("title_input")
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description détaillée") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(CornerRadius.md),
                colors = outlinedColors(),
                modifier = Modifier.fillMaxWidth().testTag("description_input")
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(if (selectedTransaction == TransactionType.RENT) "Loyer mensuel (€) *" else "Prix de vente (€) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = outlinedColors(),
                    modifier = Modifier.weight(1f).testTag("price_input")
                )

                OutlinedTextField(
                    value = areaText,
                    onValueChange = { areaText = it },
                    label = { Text("Superficie (m²) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = outlinedColors(),
                    modifier = Modifier.weight(1f).testTag("area_input")
                )
            }
        }

        // Section 3: Localization
        FormSectionCard(title = "Localisation") {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Adresse ou rue") },
                placeholder = { Text("Ex: 14 Rue de Rivoli") },
                singleLine = true,
                shape = RoundedCornerShape(CornerRadius.md),
                colors = outlinedColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ville *") },
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = outlinedColors(),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("Quartier ou Arrondissement") },
                    singleLine = true,
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = outlinedColors(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section 4: Specifications
        FormSectionCard(title = if (selectedType == PropertyType.LAND) "Caractéristiques du terrain" else "Caractéristiques") {
            if (selectedType == PropertyType.LAND) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Terrain viabilisé / constructible", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = hasParking,
                        onCheckedChange = { hasParking = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chambres : $bedrooms", fontWeight = FontWeight.Medium)
                    Row {
                        OutlinedButton(onClick = { if (bedrooms > 0) bedrooms-- }, modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp)) { Text("-") }
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        OutlinedButton(onClick = { bedrooms++ }, modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp)) { Text("+") }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pièces au total : $rooms", fontWeight = FontWeight.Medium)
                    Row {
                        OutlinedButton(onClick = { if (rooms > 1) rooms-- }, modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp)) { Text("-") }
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        OutlinedButton(onClick = { rooms++ }, modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp)) { Text("+") }
                    }
                }

                if (selectedType != PropertyType.HOUSE && selectedType != PropertyType.VILLA) {
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Étage : ${if (floor == 0) "RDC" else "${floor}e"}", fontWeight = FontWeight.Medium)
                        Row {
                            OutlinedButton(onClick = { if (floor > 0) floor-- }, modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp)) { Text("-") }
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            OutlinedButton(onClick = { floor++ }, modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp)) { Text("+") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stationnement / Parking inclus", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = hasParking,
                        onCheckedChange = { hasParking = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Section 5: Photos
        FormSectionCard(title = "Photographies du bien") {
            Text(text = "Sélectionnez parmi nos visuels d'architecture :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(Spacing.sm))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(SAMPLE_PHOTOS) { url ->
                    val isSelected = selectedImages.contains(url)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(CornerRadius.sm))
                            .clickable {
                                if (isSelected && selectedImages.size > 1) selectedImages.remove(url)
                                else if (!isSelected) selectedImages.add(url)
                            }
                    ) {
                        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        if (isSelected) {
                            Surface(color = DesignColors.Secondary.copy(alpha = 0.8f), modifier = Modifier.fillMaxSize()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Submission Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (editingProperty != null) {
                Button(
                    onClick = {
                        val price = priceText.toDoubleOrNull() ?: 1000.0
                        val area = areaText.toDoubleOrNull() ?: 50.0
                        viewModel.saveEditedProperty(
                            propertyId = editingProperty!!.id,
                            title = title,
                            description = description,
                            price = price,
                            propertyType = selectedType,
                            transactionType = selectedTransaction,
                            address = address,
                            city = city,
                            district = district,
                            area = area,
                            bedrooms = bedrooms,
                            bathrooms = bathrooms,
                            rooms = rooms,
                            floor = floor,
                            parking = hasParking,
                            images = selectedImages.toList(),
                            submitForValidation = true
                        )
                    },
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Enregistrer les modifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Veuillez renseigner un titre."
                            return@OutlinedButton
                        }
                        val price = priceText.toDoubleOrNull() ?: 1200.0
                        val area = areaText.toDoubleOrNull() ?: 60.0
                        viewModel.publishProperty(
                            title = title,
                            description = description.ifBlank { "Charmant logement situé à $city." },
                            price = price,
                            propertyType = selectedType,
                            transactionType = selectedTransaction,
                            address = address.ifBlank { "Rue principale" },
                            city = city,
                            district = district,
                            latitude = 48.8566,
                            longitude = 2.3522,
                            area = area,
                            bedrooms = bedrooms,
                            bathrooms = bathrooms,
                            rooms = rooms,
                            floor = floor,
                            parking = hasParking,
                            images = selectedImages.toList(),
                            submitForValidation = false
                        )
                    },
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Brouillon", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Veuillez renseigner un titre."
                            return@Button
                        }
                        val price = priceText.toDoubleOrNull()
                        if (price == null || price <= 0) {
                            errorMessage = "Veuillez entrer un montant valide."
                            return@Button
                        }
                        val area = areaText.toDoubleOrNull()
                        if (area == null || area <= 0) {
                            errorMessage = "Veuillez entrer une surface valide."
                            return@Button
                        }

                        val isLand = selectedType == PropertyType.LAND
                        viewModel.publishProperty(
                            title = title,
                            description = description.ifBlank { "Magnifique logement lumineux et idéalement situé à $city." },
                            price = price,
                            propertyType = selectedType,
                            transactionType = selectedTransaction,
                            address = address.ifBlank { "Avenue des Fleurs" },
                            city = city,
                            district = district,
                            latitude = 48.8566,
                            longitude = 2.3522,
                            area = area,
                            bedrooms = if (isLand) 0 else bedrooms,
                            bathrooms = if (isLand) 0 else bathrooms,
                            rooms = if (isLand) 0 else rooms,
                            floor = if (isLand) 0 else floor,
                            parking = hasParking,
                            images = selectedImages.toList(),
                            submitForValidation = true
                        )
                    },
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Secondary),
                    modifier = Modifier.weight(1.35f).height(50.dp).testTag("submit_publish_btn")
                ) {
                    Text("Soumettre pour validation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(CornerRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            content()
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)
