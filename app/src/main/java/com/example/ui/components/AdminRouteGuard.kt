package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.AdminAuthState
import com.example.ui.viewmodel.AdminViewModel

/**
 * Composant de protection (Route Guard) réutilisable pour toutes les routes actuelles et futures du dashboard administrateur.
 * Vérifie de manière réactive et continue l'autorisation ADMIN dans la base de données Room.
 * Si l'accès n'est pas autorisé ou si le rôle change, l'accès est immédiatement bloqué.
 */
@Composable
fun AdminRouteGuard(
    adminViewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (User) -> Unit
) {
    val isAuthorized by adminViewModel.isAdminAuthorized.collectAsStateWithLifecycle()
    val currentAdminUser by adminViewModel.currentAdminUser.collectAsStateWithLifecycle()
    val authState by adminViewModel.adminAuthState.collectAsStateWithLifecycle()

    when {
        // En cours de vérification de session
        authState is AdminAuthState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = "Vérification des habilitations administrateur...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Accès accordé : l'utilisateur possède réellement le rôle ADMIN dans la base Room
        isAuthorized && currentAdminUser != null && currentAdminUser?.role == UserRole.ADMIN -> {
            content(currentAdminUser!!)
        }

        // Accès refusé : Rôle non-administrateur (USER, OWNER, AGENT) ou non connecté
        else -> {
            val deniedReason = when (val state = authState) {
                is AdminAuthState.AccessDenied -> state.reason
                is AdminAuthState.Error -> state.message
                else -> "Cet espace est strictement réservé aux comptes possédant le rôle Administrateur dans la base de données."
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(Spacing.xl)
                    .testTag("admin_route_access_denied_view"),
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
                            contentDescription = "Accès Refusé",
                            tint = DesignColors.Error,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                Text(
                    text = "Accès Restreint — Administrateur Requis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = deniedReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Spacing.md)
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.testTag("admin_guard_back_button")
                ) {
                    Text("Retour à l'espace public", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
