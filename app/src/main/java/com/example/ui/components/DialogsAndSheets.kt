package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.PropertyType
import com.example.data.model.TransactionType
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.SearchFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    currentFilter: SearchFilter,
    onApplyFilter: (SearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTransaction by remember { mutableStateOf(currentFilter.transactionType) }
    var selectedPropertyType by remember { mutableStateOf(currentFilter.propertyType) }
    var maxPrice by remember { mutableStateOf<Double?>(currentFilter.maxPrice) }
    var minBedrooms by remember { mutableStateOf(currentFilter.minBedrooms) }
    var hasParking by remember { mutableStateOf(currentFilter.hasParking ?: false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = CornerRadius.xl, topEnd = CornerRadius.xl),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxxl)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtres de recherche",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = {
                    selectedTransaction = null
                    selectedPropertyType = null
                    maxPrice = null
                    minBedrooms = null
                    hasParking = false
                }) {
                    Text("Réinitialiser", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Transaction Type (Location vs Vente)
            Text(text = "Type de projet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(Spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf(null to "Tous", TransactionType.RENT to "Location", TransactionType.SALE to "Vente").forEach { (type, label) ->
                    val isSelected = selectedTransaction == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTransaction = type },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Property Type
            Text(text = "Type de bien", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(Spacing.xs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = selectedPropertyType == null,
                    onClick = { selectedPropertyType = null },
                    label = { Text("Tous") }
                )
                PropertyType.entries.forEach { pType ->
                    FilterChip(
                        selected = selectedPropertyType == pType,
                        onClick = { selectedPropertyType = pType },
                        label = { Text(pType.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Bedrooms filter
            Text(text = "Nombre minimum de chambres", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(Spacing.xs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = minBedrooms == null,
                    onClick = { minBedrooms = null },
                    label = { Text("Peu importe") }
                )
                listOf(1, 2, 3, 4).forEach { count ->
                    FilterChip(
                        selected = minBedrooms == count,
                        onClick = { minBedrooms = if (minBedrooms == count) null else count },
                        label = { Text("$count+ ch.") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Max Budget (Adaptive for Location vs Vente)
            val isSale = selectedTransaction == TransactionType.SALE
            val unitLabel = if (isSale) "€" else "€/mois"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Budget maximal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = if (maxPrice != null) "${maxPrice!!.toInt()} $unitLabel" else "Non limité",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            // Quick preset chips for budget
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = maxPrice == null,
                    onClick = { maxPrice = null },
                    label = { Text("Illimité") }
                )
                if (isSale) {
                    listOf(180000.0 to "≤ 180 k€", 300000.0 to "≤ 300 k€", 500000.0 to "≤ 500 k€", 900000.0 to "≤ 900 k€").forEach { (price, lbl) ->
                        FilterChip(
                            selected = maxPrice == price,
                            onClick = { maxPrice = if (maxPrice == price) null else price },
                            label = { Text(lbl) }
                        )
                    }
                } else {
                    listOf(700.0 to "≤ 700 €", 1200.0 to "≤ 1 200 €", 2000.0 to "≤ 2 000 €", 3000.0 to "≤ 3 000 €").forEach { (price, lbl) ->
                        FilterChip(
                            selected = maxPrice == price,
                            onClick = { maxPrice = if (maxPrice == price) null else price },
                            label = { Text(lbl) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Parking Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hasParking = !hasParking },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasParking,
                    onCheckedChange = { hasParking = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(text = "Parking ou garage obligatoire", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Apply CTA Button
            Button(
                onClick = {
                    onApplyFilter(
                        currentFilter.copy(
                            transactionType = selectedTransaction,
                            propertyType = selectedPropertyType,
                            maxPrice = maxPrice,
                            minBedrooms = minBedrooms,
                            hasParking = if (hasParking) true else null
                        )
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(CornerRadius.md),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_filter_btn")
            ) {
                Text("Appliquer les critères", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun VisitRequestDialog(
    propertyTitle: String,
    onConfirm: (date: String, time: String, message: String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateOptions = listOf("Demain", "Ce samedi", "Lundi prochain", "Mardi prochain")
    val timeOptions = listOf("10:00", "11:30", "14:30", "16:00", "18:00")

    var selectedDate by remember { mutableStateOf(dateOptions[0]) }
    var selectedTime by remember { mutableStateOf(timeOptions[2]) }
    var message by remember { mutableStateOf("Bonjour, je souhaite visiter ce logement, mon dossier est prêt.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(CornerRadius.xl),
        title = {
            Text(text = "Demande de visite", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    text = "Bien concerné : $propertyTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(text = "Date souhaitée :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    dateOptions.forEach { d ->
                        FilterChip(
                            selected = selectedDate == d,
                            onClick = { selectedDate = d },
                            label = { Text(d, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(text = "Créneau horaire :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    timeOptions.forEach { t ->
                        FilterChip(
                            selected = selectedTime == t,
                            onClick = { selectedTime = t },
                            label = { Text(t, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message au bailleur") },
                    maxLines = 3,
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("visit_message_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDate, selectedTime, message) },
                shape = RoundedCornerShape(CornerRadius.md),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("confirm_visit_btn")
            ) {
                Text("Confirmer la demande", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun ReportDialog(
    propertyTitle: String,
    onConfirm: (reason: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    val reasons = listOf(
        "Prix trompeur ou abusif",
        "Logement déjà loué ou vendu",
        "Photos ou description non conformes",
        "Suspicion d'arnaque / faux bailleur",
        "Autre motif de non-conformité"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(CornerRadius.xl),
        icon = {
            Surface(
                shape = CircleShape,
                color = DesignColors.ErrorContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = null,
                        tint = DesignColors.Error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(text = "Signaler cette annonce", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    text = "Aidez notre équipe de modération à garantir la sécurité de la communauté.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.md))

                reasons.forEach { r ->
                    Surface(
                        shape = RoundedCornerShape(CornerRadius.sm),
                        color = if (selectedReason == r) DesignColors.ErrorContainer.copy(alpha = 0.35f) else Color.Transparent,
                        border = if (selectedReason == r) BorderStroke(1.dp, DesignColors.Error.copy(alpha = 0.4f)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = r }
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                        ) {
                            RadioButton(
                                selected = selectedReason == r,
                                onClick = { selectedReason = r },
                                colors = RadioButtonDefaults.colors(selectedColor = DesignColors.Error)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(text = r, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selectedReason == r) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Détails complémentaires (facultatif)") },
                    maxLines = 3,
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedReason, description) },
                shape = RoundedCornerShape(CornerRadius.md),
                colors = ButtonDefaults.buttonColors(containerColor = DesignColors.Error)
            ) {
                Text("Transmettre le signalement", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
