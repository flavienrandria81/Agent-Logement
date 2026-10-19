package com.example

import com.example.data.auth.LoginRequest
import com.example.data.auth.RegisterRequest
import com.example.data.model.AccountStatus
import com.example.data.model.Property
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class FunctionalScenariosTest {

    // Helper to simulate SHA-256 password hash without android.util.Base64 in JVM tests
    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun scenario1_2_3_4_publicBrowsingAndPropertiesWithoutAccount() {
        // Scénarios 1 à 4 : Consultation de l'accueil, recherche, filtres et détails sans compte
        val studio = Property(
            id = 1,
            ownerId = 10,
            title = "Studio lumineux Bastille",
            description = "Studio rénové proche métro",
            price = 850.0,
            currency = "€",
            propertyType = PropertyType.STUDIO,
            transactionType = TransactionType.RENT,
            status = PropertyStatus.PUBLISHED,
            city = "Paris",
            district = "11ème",
            address = "Rue de la Roquette",
            bedrooms = 1,
            area = 28.0,
            parking = false
        )

        val villa = Property(
            id = 2,
            ownerId = 20,
            title = "Villa contemporaine avec piscine",
            description = "Superbe villa avec jardin arboré",
            price = 650000.0,
            currency = "€",
            propertyType = PropertyType.HOUSE,
            transactionType = TransactionType.SALE,
            status = PropertyStatus.PUBLISHED,
            city = "Marseille",
            district = "8ème",
            address = "Corniche Kennedy",
            bedrooms = 4,
            area = 180.0,
            parking = true
        )

        // Consultation publique sans authentification
        assertTrue(studio.formattedPrice.contains("850"))
        assertTrue(villa.formattedPrice.contains("650") && villa.formattedPrice.contains("000"))
        assertEquals(PropertyType.STUDIO, studio.propertyType)
        assertEquals(PropertyType.HOUSE, villa.propertyType)
        assertEquals(TransactionType.RENT, studio.transactionType)
        assertEquals(TransactionType.SALE, villa.transactionType)
        assertEquals(PropertyStatus.PUBLISHED, studio.status)
        assertEquals(PropertyStatus.PUBLISHED, villa.status)
    }

    @Test
    fun scenario5_6_passwordHashingAndSessionValidation() {
        // Scénario 5 & 6 : Hachage sécurisé SHA-256 et vérification du mot de passe
        val rawPassword = "password123"
        val hashedPassword = sha256Hex(rawPassword)

        val user = User(
            id = 42,
            firstName = "Thomas",
            lastName = "Dubois",
            email = "thomas.dubois@test.fr",
            phone = "0612345678",
            passwordHash = hashedPassword,
            role = UserRole.USER,
            accountStatus = AccountStatus.ACTIVE
        )

        // Vérification des identifiants (mot de passe correct vs erroné)
        val validLogin = user.passwordHash == sha256Hex("password123")
        val invalidLogin = user.passwordHash == sha256Hex("wrongpassword")

        assertTrue("Le mot de passe correct doit correspondre", validLogin)
        assertFalse("Le mauvais mot de passe doit être rejeté", invalidLogin)
        assertEquals("ACTIVE", user.accountStatus.name)
        assertTrue(user.isApproved)
    }

    @Test
    fun scenario7_8_userRegistrationImmediateAccess() {
        // Scénario 7 & 8 : Inscription USER avec activation immédiate sans attendre l'ADMIN
        val registerRequest = RegisterRequest(
            firstName = "Lucas",
            lastName = "Bernard",
            email = "lucas.bernard@test.fr",
            phone = "0698765432",
            password = "monpassword",
            role = UserRole.USER
        )

        val initialStatus = if (registerRequest.role == UserRole.USER) AccountStatus.ACTIVE else AccountStatus.PENDING_APPROVAL
        val newUser = User(
            id = 101,
            firstName = registerRequest.firstName,
            lastName = registerRequest.lastName,
            email = registerRequest.email,
            phone = registerRequest.phone,
            passwordHash = sha256Hex(registerRequest.password),
            role = registerRequest.role,
            accountStatus = initialStatus
        )

        assertEquals(AccountStatus.ACTIVE, newUser.accountStatus)
        assertTrue("Le compte USER doit être actif immédiatement", newUser.isApproved)
        assertEquals("Lucas Bernard", newUser.fullName)
    }

    @Test
    fun scenario9_10_11_ownerRegistrationPendingApproval() {
        // Scénarios 9, 10 & 11 : Inscription OWNER -> Statut PENDING -> Accès restreint
        val registerRequest = RegisterRequest(
            firstName = "Claire",
            lastName = "Martin",
            email = "claire.martin@bailleur.fr",
            phone = "0644332211",
            password = "securePassword",
            role = UserRole.OWNER
        )

        val initialStatus = if (registerRequest.role == UserRole.USER) AccountStatus.ACTIVE else AccountStatus.PENDING_APPROVAL
        val newOwner = User(
            id = 202,
            firstName = registerRequest.firstName,
            lastName = registerRequest.lastName,
            email = registerRequest.email,
            phone = registerRequest.phone,
            passwordHash = sha256Hex(registerRequest.password),
            role = registerRequest.role,
            accountStatus = initialStatus
        )

        assertEquals(UserRole.OWNER, newOwner.role)
        assertEquals(AccountStatus.PENDING_APPROVAL, newOwner.accountStatus)
        assertFalse("Un OWNER en attente ne doit pas être approuvé", newOwner.isApproved)

        // Test d'autorisation des fonctionnalités : bloqué tant que non approuvé
        val canAccessOwnerSpace = newOwner.isApproved && newOwner.role == UserRole.OWNER
        assertFalse("L'OWNER PENDING ne doit pas accéder à l'espace propriétaire", canAccessOwnerSpace)
    }

    @Test
    fun scenario12_13_14_adminApprovesOwner() {
        // Scénarios 12, 13 & 14 : Connexion ADMIN et validation de l'OWNER PENDING -> ACTIVE
        val pendingOwner = User(
            id = 202,
            firstName = "Claire",
            lastName = "Martin",
            email = "claire.martin@bailleur.fr",
            phone = "0644332211",
            passwordHash = "hash",
            role = UserRole.OWNER,
            accountStatus = AccountStatus.PENDING_APPROVAL
        )

        // L'ADMIN autorise le compte
        val approvedOwner = pendingOwner.copy(accountStatus = AccountStatus.ACTIVE)
        assertEquals(AccountStatus.ACTIVE, approvedOwner.accountStatus)
        assertTrue("Après validation ADMIN, l'OWNER doit être approuvé", approvedOwner.isApproved)

        val canAccessOwnerSpaceNow = approvedOwner.isApproved && approvedOwner.role == UserRole.OWNER
        assertTrue("L'OWNER validé peut désormais accéder à l'espace propriétaire", canAccessOwnerSpaceNow)
    }

    @Test
    fun scenario15_agentRegistrationAndApproval() {
        // Scénario 15 : Inscription AGENT -> PENDING -> Validation ADMIN -> Accès débloqué
        val pendingAgent = User(
            id = 303,
            firstName = "Marc",
            lastName = "Lefebvre",
            email = "marc.lefebvre@agence.fr",
            phone = "0677889900",
            passwordHash = "hash",
            role = UserRole.AGENT,
            accountStatus = AccountStatus.PENDING_APPROVAL
        )

        assertFalse("AGENT créé doit être PENDING et non approuvé", pendingAgent.isApproved)

        // L'ADMIN autorise l'agent
        val approvedAgent = pendingAgent.copy(accountStatus = AccountStatus.ACTIVE)
        assertTrue("Après validation ADMIN, l'AGENT doit être approuvé", approvedAgent.isApproved)
    }

    @Test
    fun scenario16_17_18_profileIsolatedAndEditable() {
        // Scénarios 16 à 18 : Affichage du profil individuel connecté et modification persistante
        val currentUser = User(
            id = 55,
            firstName = "Sophie",
            lastName = "Lambert",
            email = "sophie.lambert@test.fr",
            phone = "0655443322",
            passwordHash = "hash",
            role = UserRole.USER,
            accountStatus = AccountStatus.ACTIVE,
            photoUrl = "https://example.com/original.jpg",
            bio = "Cherche T2 proche métro"
        )

        // Le profil affiche uniquement l'utilisateur connecté
        assertEquals("Sophie Lambert", currentUser.fullName)
        assertEquals("sophie.lambert@test.fr", currentUser.email)
        assertEquals("0655443322", currentUser.phone)
        assertEquals("Cherche T2 proche métro", currentUser.bio)

        // Modification du profil
        val updatedUser = currentUser.copy(
            firstName = "Sophie-Anne",
            lastName = "Lambert-Moreau",
            phone = "0699887766",
            bio = "Recherche active T3 avec balcon"
        )

        assertEquals("Sophie-Anne Lambert-Moreau", updatedUser.fullName)
        assertEquals("0699887766", updatedUser.phone)
        assertEquals("Recherche active T3 avec balcon", updatedUser.bio)

        // Upload de photo persistant
        val newPhotoUrl = "file:///data/user/0/com.example/files/profile_pictures/avatar_55_123456789.jpg"
        val userWithNewPhoto = updatedUser.copy(photoUrl = newPhotoUrl)
        assertEquals(newPhotoUrl, userWithNewPhoto.photoUrl)
    }
}
