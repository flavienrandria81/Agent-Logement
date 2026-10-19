package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Spacing

data class DemoRoleOption(
    val title: String,
    val subtitle: String,
    val email: String,
    val password: String,
    val emoji: String,
    val role: UserRole,
    val roleBadge: String,
    val badgeColor: Color
)

@Composable
fun RoleSwitcherDialog(
    currentRole: UserRole?,
    onSelectRole: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val demoAccounts = listOf(
        DemoRoleOption(
            title = "Administrateur (ADMIN)",
            subtitle = "Supervision, modération annonces, gestion des users, visites et graphiques",
            email = "sophie.bernard@logement-admin.fr",
            password = "admin123",
            emoji = "👑",
            role = UserRole.ADMIN,
            roleBadge = "ADMIN",
            badgeColor = DesignColors.Primary
        ),
        DemoRoleOption(
            title = "Propriétaire (OWNER)",
            subtitle = "Espace « Mes biens », ajout de logement, modification et suppression",
            email = "claire.martin@proprietaire.fr",
            password = "password123",
            emoji = "🏡",
            role = UserRole.OWNER,
            roleBadge = "OWNER",
            badgeColor = DesignColors.Accent
        ),
        DemoRoleOption(
            title = "Agent Immobilier (AGENT)",
            subtitle = "Espace « Mes mandats », agence Horizon Prestige, gestion de portefeuille",
            email = "marc.lefebvre@agence-horizon.fr",
            password = "password123",
            emoji = "🏢",
            role = UserRole.AGENT,
            roleBadge = "AGENT",
            badgeColor = DesignColors.Secondary
        ),
        DemoRoleOption(
            title = "Locataire / Chercheur (USER)",
            subtitle = "Consultation publique, demandes de visite et offres d'achat",
            email = "jean.dupont@email.fr",
            password = "password123",
            emoji = "👤",
            role = UserRole.USER,
            roleBadge = "USER",
            badgeColor = MaterialTheme.colorScheme.primary
        ),
        DemoRoleOption(
            title = "Propriétaire En Attente",
            subtitle = "Compte en attente d'approbation (test du blocage avant validation)",
            email = "thomas.roussel@proprietaire.fr",
            password = "password123",
            emoji = "⏳",
            role = UserRole.OWNER,
            roleBadge = "EN ATTENTE",
            badgeColor = DesignColors.Warning
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Changer de rôle (1 Clic)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = "Basculez instantanément vers un profil pour tester ses opérations CRUD réelles :",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.xs))

                demoAccounts.forEach { option ->
                    val isSelected = currentRole == option.role && option.roleBadge != "EN ATTENTE"
                    Card(
                        shape = RoundedCornerShape(CornerRadius.md),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRole(option.email, option.password) }
                            .testTag("role_switcher_${option.role.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = option.badgeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(option.emoji, fontSize = 16.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(Spacing.sm))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = option.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Surface(
                                        shape = RoundedCornerShape(CornerRadius.pill),
                                        color = option.badgeColor
                                    ) {
                                        Text(
                                            text = option.roleBadge,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = option.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }

                            if (isSelected) {
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Actif",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
