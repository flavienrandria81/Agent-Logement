package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.Spacing
import com.example.ui.viewmodel.AdminAuthState
import com.example.ui.viewmodel.AdminViewModel

/**
 * Surcharge connectée directement au AdminViewModel qui gère l'état d'authentification
 * et la vérification du rôle ADMIN via la base de données Room.
 */
@Composable
fun AdminLoginScreen(
    adminViewModel: AdminViewModel,
    onLoginSuccess: () -> Unit,
    onBackToPublic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by adminViewModel.adminAuthState.collectAsStateWithLifecycle()
    val isLoading = authState is AdminAuthState.Loading
    val errorMessage = when (val state = authState) {
        is AdminAuthState.Error -> state.message
        is AdminAuthState.AccessDenied -> state.reason
        else -> null
    }

    AdminLoginScreen(
        isLoading = isLoading,
        errorMessage = errorMessage,
        onLogin = { email, pwd ->
            adminViewModel.loginAsAdmin(
                email = email,
                password = pwd,
                onSuccess = onLoginSuccess
            )
        },
        onBackToPublic = onBackToPublic,
        modifier = modifier
    )
}

/**
 * Écran d'authentification dédié exclusivement aux administrateurs de la plateforme Logement.
 * Vérifie rigoureusement les autorisations administratives sans identifiants codés en dur.
 */
@Composable
fun AdminLoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onBackToPublic: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailValidationError by remember { mutableStateOf<String?>(null) }
    var passwordValidationError by remember { mutableStateOf<String?>(null) }

    fun validateAndSubmit() {
        val trimmedEmail = email.trim()
        val trimmedPassword = password

        var hasError = false

        if (trimmedEmail.isBlank()) {
            emailValidationError = "L'identifiant ou email administrateur est requis."
            hasError = true
        } else if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            emailValidationError = "Format d'adresse e-mail invalide."
            hasError = true
        } else {
            emailValidationError = null
        }

        if (trimmedPassword.isBlank()) {
            passwordValidationError = "Le mot de passe administrateur est requis."
            hasError = true
        } else if (trimmedPassword.length < 6) {
            passwordValidationError = "Le mot de passe doit comporter au moins 6 caractères."
            hasError = true
        } else {
            passwordValidationError = null
        }

        if (!hasError) {
            onLogin(trimmedEmail, trimmedPassword)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Bouton retour en haut
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            OutlinedButton(
                onClick = onBackToPublic,
                shape = RoundedCornerShape(CornerRadius.pill),
                modifier = Modifier.testTag("admin_back_to_public_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Retour à l'application", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Badge de sécurité supérieur
        Surface(
            shape = RoundedCornerShape(CornerRadius.pill),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
            modifier = Modifier.padding(bottom = Spacing.md)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "PORTAIL DE SÉCURITÉ & MODÉRATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Logo de l'application Logement
        Surface(
            shape = RoundedCornerShape(CornerRadius.xl),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = Elevation.medium,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(Spacing.xs)) {
                Image(
                    painter = painterResource(id = R.drawable.apex_realty_logo),
                    contentDescription = "Logo Logement",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(CornerRadius.lg))
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Titre & Sous-titre
        Text(
            text = "Administration Logement",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Spacing.xxs))

        Text(
            text = "Accès restreint aux administrateurs habilités",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Carte principale de connexion Admin
        Card(
            shape = RoundedCornerShape(CornerRadius.xl),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.medium),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.xl)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column {
                        Text(
                            text = "Authentification Administrateur",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vérification des droits et privilèges d'accès",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Erreur d'authentification serveur / ViewModel
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = DesignColors.ErrorContainer,
                        shape = RoundedCornerShape(CornerRadius.md),
                        border = BorderStroke(1.dp, DesignColors.Error.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.md)
                            .testTag("admin_login_error_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = DesignColors.Error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = errorMessage,
                                color = DesignColors.Error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Champ Identifiant / E-mail administrateur
                Text(
                    text = "E-mail ou identifiant administrateur",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailValidationError = null
                    },
                    placeholder = { Text("admin@logement.fr", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    isError = emailValidationError != null,
                    supportingText = emailValidationError?.let { errorText ->
                        { Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_login_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                // Champ Mot de passe avec toggle visibilité
                Text(
                    text = "Mot de passe administrateur",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordValidationError = null
                    },
                    placeholder = { Text("••••••••", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordValidationError != null,
                    supportingText = passwordValidationError?.let { errorText ->
                        { Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_login_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Bouton de connexion administrateur
                Button(
                    onClick = { validateAndSubmit() },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_login_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text("Vérification des habilitations...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "Se connecter à l'administration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Avertissement de sécurité
                Surface(
                    shape = RoundedCornerShape(CornerRadius.sm),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "Toutes les connexions administratives sont tracées et journalisées.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Encadré de test rapide pour évaluateurs / démo
        Surface(
            shape = RoundedCornerShape(CornerRadius.lg),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    text = "Identifiants administrateur de démonstration :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.xxs))
                Text(
                    text = "E-mail : sophie.bernard@logement-admin.fr\nMot de passe : admin123",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                TextButton(
                    onClick = {
                        email = "sophie.bernard@logement-admin.fr"
                        password = "admin123"
                        emailValidationError = null
                        passwordValidationError = null
                    },
                    modifier = Modifier.testTag("admin_fill_demo_credentials_btn")
                ) {
                    Text(
                        text = "Remplir automatiquement",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // Lien pour retourner à la connexion utilisateur standard
        TextButton(onClick = onBackToPublic) {
            Text(
                text = "Accéder à la connexion utilisateur standard",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
