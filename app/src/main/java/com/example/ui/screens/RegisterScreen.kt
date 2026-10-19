package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.CornerRadius
import com.example.ui.theme.DesignColors
import com.example.ui.theme.Elevation
import com.example.ui.theme.Spacing

@Composable
fun RegisterScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (firstName: String, lastName: String, email: String, phone: String, password: String, role: UserRole) -> Unit,
    onNavigateToLogin: () -> Unit,
    onContinueAsGuest: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var ownerManagementType by remember { mutableStateOf("Particulier bailleur") }
    var ownerCity by remember { mutableStateOf("") }
    var agencyName by remember { mutableStateOf("Horizon Immobilier Prestige") }
    var licenseNumber by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back to Login Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateToLogin) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
            }
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "Création de compte",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Card(
            shape = RoundedCornerShape(CornerRadius.xl),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.xl)) {
                Text(
                    text = "Inscription",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Rejoignez la plateforme immobilière",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Error Banner
                val displayedError = localError ?: errorMessage
                if (!displayedError.isNullOrBlank()) {
                    Surface(
                        color = DesignColors.ErrorContainer,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = DesignColors.Error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = displayedError,
                                color = DesignColors.Error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Role Selector
                Text(
                    text = "Choisissez votre profil :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    val publicRoles = listOf(
                        UserRole.USER to "Chercheur",
                        UserRole.OWNER to "Propriétaire",
                        UserRole.AGENT to "Agent Pro"
                    )

                    publicRoles.forEach { (role, label) ->
                        val isSelected = selectedRole == role
                        val activeColor = when (role) {
                            UserRole.USER -> DesignColors.Primary
                            UserRole.OWNER -> DesignColors.Secondary
                            UserRole.AGENT -> DesignColors.Accent
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            shape = RoundedCornerShape(CornerRadius.md),
                            color = if (isSelected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedRole = role }
                                .padding(vertical = 2.dp)
                                .testTag("select_role_${role.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = role.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Information dynamique selon le rôle sélectionné
                Surface(
                    shape = RoundedCornerShape(CornerRadius.md),
                    color = when (selectedRole) {
                        UserRole.USER -> DesignColors.Success.copy(alpha = 0.12f)
                        UserRole.OWNER, UserRole.AGENT -> DesignColors.Warning.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (selectedRole == UserRole.USER) Icons.Default.Info else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (selectedRole == UserRole.USER) DesignColors.Success else DesignColors.Warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = when (selectedRole) {
                                UserRole.USER -> "Le profil USER ne nécessite aucune validation ADMIN. Votre compte sera immédiatement actif."
                                UserRole.OWNER -> "Le profil OWNER nécessite la validation d'un ADMIN. Votre compte sera créé en attente d'approbation."
                                UserRole.AGENT -> "Le profil AGENT nécessite la validation d'un ADMIN. Votre compte sera créé en attente d'approbation."
                                else -> ""
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Name fields in row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Prénom", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it; localError = null },
                            placeholder = { Text("Jean", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier.fillMaxWidth().testTag("register_firstname_input")
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nom", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it; localError = null },
                            placeholder = { Text("Dupont", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(CornerRadius.md),
                            modifier = Modifier.fillMaxWidth().testTag("register_lastname_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Email field
                Text("Adresse email", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; localError = null },
                    placeholder = { Text("jean.dupont@email.fr", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("register_email_input")
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Phone field
                Text("Numéro de téléphone", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("06 12 34 56 78", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("register_phone_input")
                )

                // Champs spécifiques au profil OWNER (Propriétaire)
                if (selectedRole == UserRole.OWNER) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text("Type de gestion patrimoniale", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = ownerManagementType,
                        onValueChange = { ownerManagementType = it },
                        placeholder = { Text("Particulier bailleur, SCI familiale...", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("register_owner_type_input")
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text("Ville principale de vos biens", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = ownerCity,
                        onValueChange = { ownerCity = it },
                        placeholder = { Text("Ex: Paris, Bordeaux, Lyon...", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("register_owner_city_input")
                    )
                }

                // Champs spécifiques au profil AGENT (Agent immobilier)
                if (selectedRole == UserRole.AGENT) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text("Nom de l'agence immobilière", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = agencyName,
                        onValueChange = { agencyName = it },
                        placeholder = { Text("Ex: Horizon Immobilier Prestige", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("register_agent_agency_input")
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text("Numéro de carte professionnelle ou SIRET", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        placeholder = { Text("Ex: CPI 7501 2024 000 012 345", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(CornerRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("register_agent_license_input")
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Password field
                Text("Mot de passe", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; localError = null },
                    placeholder = { Text("Au moins 6 caractères", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("register_password_input")
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Confirm Password field
                Text("Confirmer le mot de passe", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; localError = null },
                    placeholder = { Text("Retapez votre mot de passe", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(CornerRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("register_confirm_password_input")
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Submit Button
                Button(
                    onClick = {
                        val cleanFirstName = firstName.trim()
                        val cleanLastName = lastName.trim()
                        val cleanEmail = email.trim().lowercase()
                        val cleanPhone = phone.trim()

                        if (cleanFirstName.isBlank() || cleanLastName.isBlank()) {
                            localError = "Le prénom et le nom sont obligatoires."
                            return@Button
                        }
                        if (cleanEmail.isBlank()) {
                            localError = "L'adresse email est obligatoire."
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                            localError = "Veuillez saisir une adresse email valide (ex. nom@domaine.fr)."
                            return@Button
                        }
                        if (password.isBlank()) {
                            localError = "Le mot de passe est obligatoire."
                            return@Button
                        }
                        if (password.length < 6) {
                            localError = "Le mot de passe doit comporter au moins 6 caractères."
                            return@Button
                        }
                        if (password != confirmPassword) {
                            localError = "Les mots de passe ne correspondent pas."
                            return@Button
                        }

                        localError = null
                        onRegister(cleanFirstName, cleanLastName, cleanEmail, cleanPhone, password, selectedRole)
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(CornerRadius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("register_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Créer mon compte", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Vous avez déjà un compte ?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onNavigateToLogin) {
                Text("Se connecter", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        if (onContinueAsGuest != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            TextButton(onClick = onContinueAsGuest) {
                Text(
                    text = "Consulter librement sans créer de compte",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
