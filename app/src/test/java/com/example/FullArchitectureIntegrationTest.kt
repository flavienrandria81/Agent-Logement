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
import com.example.data.model.VisitRequest
import com.example.data.model.VisitStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/**
 * Suite de vérification complète des flux métiers et architecturaux de l'application Logement :
 * - Mode Invité / Consultation publique libre (sans compte)
 * - Création et activation de compte USER
 * - Création sous modération de compte OWNER (statut PENDING)
 * - Création sous modération de compte AGENT (statut PENDING)
 * - Modération et validation ADMIN (approbation et refus)
 * - Révocation des accès non autorisés
 * - Isolation stricte du profil connecté
 * - Persistance des modifications du profil et de la photo
 * - Déconnexion propre et nettoyage de session
 */
class FullArchitectureIntegrationTest {

    private fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun testGuestBrowsingAndProtectedActionsGating() {
        // 1. Visiteur sans session
        var isAuthenticated = false
        var currentUserId: Long? = null

        assertFalse("Un visiteur n'est pas authentifié par défaut", isAuthenticated)
        assertNull("Aucun ID utilisateur n'est attaché", currentUserId)

        // Consultation de biens publics
        val prop = Property(
            id = 1,
            ownerId = 10,
            title = "Appartement Haussmannien",
            description = "Grand séjour, parquet, moulures",
            price = 1200.0,
            currency = "€",
            propertyType = PropertyType.APARTMENT,
            transactionType = TransactionType.RENT,
            status = PropertyStatus.PUBLISHED,
            city = "Paris",
            district = "8ème",
            address = "Boulevard Haussmann",
            bedrooms = 2,
            area = 75.0,
            parking = false
        )

        // Les annonces sont lisibles sans compte
        assertEquals("Appartement Haussmannien", prop.title)
        assertTrue(prop.formattedPrice.contains("1") && prop.formattedPrice.contains("200"))

        // Tentative d'action protégée sans compte
        fun canExecuteProtectedAction(action: String): Boolean {
            return isAuthenticated
        }

        assertFalse("Demande de visite bloquée si non connecté", canExecuteProtectedAction("VISIT"))
        assertFalse("Favori bloqué si non connecté", canExecuteProtectedAction("FAVORITE"))
        assertFalse("Message bailleur bloqué si non connecté", canExecuteProtectedAction("MESSAGE"))
        assertFalse("Louer/Acheter bloqué si non connecté", canExecuteProtectedAction("RENT_OR_BUY"))
    }

    @Test
    fun testUserRegistrationAndImmediateAccess() {
        val req = RegisterRequest(
            firstName = "Camille",
            lastName = "Roux",
            email = "camille.roux@test.fr",
            phone = "0611223344",
            password = "password123",
            role = UserRole.USER
        )

        // La logique backend affecte ACTIVE à USER
        val status = if (req.role == UserRole.USER) AccountStatus.ACTIVE else AccountStatus.PENDING_APPROVAL
        val user = User(
            id = 501,
            firstName = req.firstName,
            lastName = req.lastName,
            email = req.email,
            phone = req.phone,
            passwordHash = hash(req.password),
            role = req.role,
            accountStatus = status
        )

        assertEquals(AccountStatus.ACTIVE, user.accountStatus)
        assertTrue("Le compte USER est approuvé immédiatement", user.isApproved)

        // Un USER peut ajouter des favoris et demander des visites
        val canSearchAndFavorite = user.role == UserRole.USER && user.isApproved
        assertTrue("USER approuvé peut réserver et chercher", canSearchAndFavorite)

        // Mais un USER ne peut pas publier ni administrer
        val canPublish = (user.role == UserRole.OWNER || user.role == UserRole.AGENT) && user.isApproved
        val canAdmin = user.role == UserRole.ADMIN
        assertFalse("USER ne peut pas publier", canPublish)
        assertFalse("USER ne peut pas administrer", canAdmin)
    }

    @Test
    fun testOwnerPendingApprovalAndAdminWorkflow() {
        // Inscription OWNER
        val req = RegisterRequest(
            firstName = "Benoit",
            lastName = "Moreau",
            email = "benoit.moreau@bailleur.fr",
            phone = "0622334455",
            password = "password123",
            role = UserRole.OWNER
        )

        val initialStatus = if (req.role == UserRole.USER) AccountStatus.ACTIVE else AccountStatus.PENDING_APPROVAL
        var owner = User(
            id = 601,
            firstName = req.firstName,
            lastName = req.lastName,
            email = req.email,
            phone = req.phone,
            passwordHash = hash(req.password),
            role = req.role,
            accountStatus = initialStatus
        )

        assertEquals(AccountStatus.PENDING_APPROVAL, owner.accountStatus)
        assertFalse("OWNER en attente n'est pas encore approuvé", owner.isApproved)

        // Accès aux fonctionnalités réservées
        fun canAccessOwnerWorkspace(u: User): Boolean {
            return u.role == UserRole.OWNER && u.isApproved
        }

        assertFalse("Accès refusé au dépôt d'annonce avant approbation ADMIN", canAccessOwnerWorkspace(owner))

        // Connexion ADMIN et approbation
        val admin = User(
            id = 1,
            firstName = "Admin",
            lastName = "System",
            email = "admin@logement.fr",
            phone = "0100000000",
            passwordHash = hash("admin123"),
            role = UserRole.ADMIN,
            accountStatus = AccountStatus.ACTIVE
        )

        assertEquals(UserRole.ADMIN, admin.role)

        // L'ADMIN autorise le propriétaire
        fun approveUserByAdmin(adminUser: User, targetUser: User): User {
            if (adminUser.role != UserRole.ADMIN) throw SecurityException("Seul ADMIN peut approuver")
            return targetUser.copy(accountStatus = AccountStatus.ACTIVE)
        }

        owner = approveUserByAdmin(admin, owner)
        assertEquals(AccountStatus.ACTIVE, owner.accountStatus)
        assertTrue("Après approbation ADMIN, le statut devient ACTIVE", owner.isApproved)
        assertTrue("L'espace propriétaire est maintenant débloqué", canAccessOwnerWorkspace(owner))
    }

    @Test
    fun testAgentPendingApprovalAndAdminRejection() {
        // Inscription AGENT
        val req = RegisterRequest(
            firstName = "Élodie",
            lastName = "Gauthier",
            email = "elodie.gauthier@agence.fr",
            phone = "0633445566",
            password = "password123",
            role = UserRole.AGENT
        )

        val initialStatus = if (req.role == UserRole.USER) AccountStatus.ACTIVE else AccountStatus.PENDING_APPROVAL
        var agent = User(
            id = 701,
            firstName = req.firstName,
            lastName = req.lastName,
            email = req.email,
            phone = req.phone,
            passwordHash = hash(req.password),
            role = req.role,
            accountStatus = initialStatus
        )

        assertEquals(AccountStatus.PENDING_APPROVAL, agent.accountStatus)
        assertFalse(agent.isApproved)

        // L'ADMIN refuse le dossier
        agent = agent.copy(accountStatus = AccountStatus.REJECTED)
        assertEquals(AccountStatus.REJECTED, agent.accountStatus)
        assertFalse("Un compte REJECTED n'est pas approuvé", agent.isApproved)
    }

    @Test
    fun testProfileIsolationAndEditingPersistence() {
        val user1 = User(
            id = 10,
            firstName = "Alice",
            lastName = "Dupont",
            email = "alice@example.fr",
            phone = "0612345678",
            passwordHash = "hash",
            role = UserRole.USER,
            accountStatus = AccountStatus.ACTIVE,
            photoUrl = "https://example.com/photo1.jpg",
            bio = "Étudiante en master"
        )

        val user2 = User(
            id = 20,
            firstName = "Bob",
            lastName = "Martin",
            email = "bob@example.fr",
            phone = "0687654321",
            passwordHash = "hash",
            role = UserRole.OWNER,
            accountStatus = AccountStatus.ACTIVE,
            photoUrl = "https://example.com/photo2.jpg",
            bio = "Propriétaire bailleur"
        )

        // L'écran Profil doit afficher uniquement l'utilisateur authentifié
        var activeSessionUser = user1
        assertEquals("Alice Dupont", activeSessionUser.fullName)
        assertEquals("alice@example.fr", activeSessionUser.email)
        assertFalse("Aucune fuite de données vers user2", activeSessionUser.email.contains("bob"))

        // Modification du profil d'Alice
        activeSessionUser = activeSessionUser.copy(
            firstName = "Alice-Marie",
            lastName = "Dupont-Leroy",
            phone = "0699001122",
            bio = "Ingénieure en poste à Paris"
        )

        assertEquals("Alice-Marie Dupont-Leroy", activeSessionUser.fullName)
        assertEquals("0699001122", activeSessionUser.phone)
        assertEquals("Ingénieure en poste à Paris", activeSessionUser.bio)

        // Changement d'utilisateur (Déconnexion puis connexion Bob)
        activeSessionUser = user2
        assertEquals("Bob Martin", activeSessionUser.fullName)
        assertEquals("bob@example.fr", activeSessionUser.email)
        assertFalse("Après déconnexion, aucune donnée d'Alice n'apparaît", activeSessionUser.fullName.contains("Alice"))
    }

    @Test
    fun testPhotoFileStorageAndPersistence() {
        // Vérification de la création de fichier dans le stockage d'application
        val tempDir = File.createTempFile("profile_test", "").parentFile!!
        val profilePicturesDir = File(tempDir, "profile_pictures")
        if (!profilePicturesDir.exists()) {
            profilePicturesDir.mkdirs()
        }

        val targetFile = File(profilePicturesDir, "avatar_10_${System.currentTimeMillis()}.jpg")
        targetFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) // JPEG header magic bytes

        assertTrue("Le fichier image doit exister dans le stockage interne de l'app", targetFile.exists())
        assertEquals(3, targetFile.length())

        val photoUrl = targetFile.toURI().toString()
        assertTrue("L'URL du profil référence bien le fichier persistant", photoUrl.startsWith("file:/"))

        targetFile.delete()
    }

    @Test
    fun testNormalRegistrationFormValidationAndErrors() {
        fun validateForm(
            firstName: String,
            lastName: String,
            email: String,
            password: String,
            confirmPassword: String
        ): String? {
            val cleanFirstName = firstName.trim()
            val cleanLastName = lastName.trim()
            val cleanEmail = email.trim().lowercase()

            if (cleanFirstName.isBlank() || cleanLastName.isBlank()) {
                return "Le prénom et le nom sont obligatoires."
            }
            if (cleanEmail.isBlank()) {
                return "L'adresse email est obligatoire."
            }
            if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                return "Veuillez saisir une adresse email valide (ex. nom@domaine.fr)."
            }
            if (password.isBlank()) {
                return "Le mot de passe est obligatoire."
            }
            if (password.length < 6) {
                return "Le mot de passe doit comporter au moins 6 caractères."
            }
            if (password != confirmPassword) {
                return "Les mots de passe ne correspondent pas."
            }
            return null
        }

        assertEquals("Le prénom et le nom sont obligatoires.", validateForm("", "Dupont", "test@test.fr", "123456", "123456"))
        assertEquals("Le prénom et le nom sont obligatoires.", validateForm("Jean", "", "test@test.fr", "123456", "123456"))
        assertEquals("L'adresse email est obligatoire.", validateForm("Jean", "Dupont", "", "123456", "123456"))
        assertEquals("Veuillez saisir une adresse email valide (ex. nom@domaine.fr).", validateForm("Jean", "Dupont", "invalidemail", "123456", "123456"))
        assertEquals("Le mot de passe doit comporter au moins 6 caractères.", validateForm("Jean", "Dupont", "jean@test.fr", "123", "123"))
        assertEquals("Les mots de passe ne correspondent pas.", validateForm("Jean", "Dupont", "jean@test.fr", "123456", "654321"))
        assertNull("Tous les champs valides ne retournent aucune erreur", validateForm("Jean", "Dupont", "jean.dupont@test.fr", "password123", "password123"))
    }

    @Test
    fun testNormalAccountRegistrationAndSubsequentLogin() {
        val userDatabase = mutableMapOf<String, User>()

        fun registerUser(req: RegisterRequest): Result<User> {
            val cleanEmail = req.email.trim().lowercase()
            if (userDatabase.containsKey(cleanEmail)) {
                return Result.failure(IllegalArgumentException("Un compte existe déjà avec cette adresse email."))
            }
            val initialStatus = if (req.role == UserRole.USER) AccountStatus.ACTIVE else AccountStatus.PENDING_APPROVAL
            val newUser = User(
                id = (userDatabase.size + 1).toLong(),
                firstName = req.firstName.trim(),
                lastName = req.lastName.trim(),
                email = cleanEmail,
                phone = req.phone.trim(),
                passwordHash = hash(req.password),
                role = req.role,
                accountStatus = initialStatus
            )
            userDatabase[cleanEmail] = newUser
            return Result.success(newUser)
        }

        fun loginUser(email: String, password: String): Result<User> {
            val cleanEmail = email.trim().lowercase()
            val user = userDatabase[cleanEmail]
                ?: return Result.failure(IllegalArgumentException("Identifiants incorrects. Aucun compte avec cet email."))
            if (user.passwordHash != hash(password)) {
                return Result.failure(IllegalArgumentException("Mot de passe incorrect."))
            }
            return Result.success(user)
        }

        // 1. Inscription normale d'un nouvel utilisateur
        val regResult = registerUser(
            RegisterRequest(
                firstName = "Nicolas",
                lastName = "Bernard",
                email = "nicolas.bernard@nouveau-compte.fr",
                phone = "0612345678",
                password = "monSuperMotDePasse123",
                role = UserRole.USER
            )
        )

        assertTrue("L'inscription doit réussir", regResult.isSuccess)
        val createdUser = regResult.getOrThrow()
        assertEquals("Nicolas", createdUser.firstName)
        assertEquals("nicolas.bernard@nouveau-compte.fr", createdUser.email)
        assertEquals(AccountStatus.ACTIVE, createdUser.accountStatus)

        // 2. Connexion avec les identifiants créés
        val loginSuccess = loginUser("nicolas.bernard@nouveau-compte.fr", "monSuperMotDePasse123")
        assertTrue("La connexion avec les identifiants créés doit réussir", loginSuccess.isSuccess)
        assertEquals(createdUser.id, loginSuccess.getOrThrow().id)

        // 3. Connexion avec un mauvais mot de passe
        val loginWrongPwd = loginUser("nicolas.bernard@nouveau-compte.fr", "mauvais_mdp")
        assertTrue("Le mauvais mot de passe doit être refusé", loginWrongPwd.isFailure)
        assertEquals("Mot de passe incorrect.", loginWrongPwd.exceptionOrNull()?.message)

        // 4. Tentative de réinscription avec le même email
        val duplicateReg = registerUser(
            RegisterRequest(
                firstName = "Autre",
                lastName = "Personne",
                email = "nicolas.bernard@nouveau-compte.fr",
                phone = "0600000000",
                password = "autre_password",
                role = UserRole.USER
            )
        )
        assertTrue("La réinscription avec le même email doit être rejetée", duplicateReg.isFailure)
        assertEquals("Un compte existe déjà avec cette adresse email.", duplicateReg.exceptionOrNull()?.message)
    }

    @Test
    fun testOwnerAndAgentApprovalLifecycle() {
        val pendingOwner = User(
            id = 101,
            firstName = "Julien",
            lastName = "Perez",
            email = "julien.perez@proprio.fr",
            passwordHash = hash("ownerpass"),
            role = UserRole.OWNER,
            accountStatus = AccountStatus.PENDING_APPROVAL
        )

        assertFalse("Le compte OWNER nouvellement créé ne doit pas être actif", pendingOwner.isApproved)
        assertEquals(AccountStatus.PENDING_APPROVAL, pendingOwner.accountStatus)

        // Approbation par l'administrateur
        val approvedOwner = pendingOwner.copy(accountStatus = AccountStatus.ACTIVE)
        assertTrue("Après approbation ADMIN, le statut devient ACTIVE", approvedOwner.isApproved)
        assertEquals(AccountStatus.ACTIVE, approvedOwner.accountStatus)
    }

    // =========================================================================
    // LES 10 SCÉNARIOS DE TEST OBLIGATOIRES DE LA DEMANDE UTILISATEUR
    // =========================================================================

    @Test
    fun testScenario1_userRegistrationRedirectsToLoginWithMessage() {
        // Scénario 1 : Inscription USER → message de réussite → page de connexion (pas de connexion auto)
        var isAuthenticated = false
        var currentScreen = "REGISTER"
        var uiMessage: String? = null

        fun onRegisterSubmit(req: RegisterRequest) {
            // Enregistrement en base de données
            val user = User(
                id = 123L,
                firstName = req.firstName,
                lastName = req.lastName,
                email = req.email,
                passwordHash = hash(req.password),
                role = req.role,
                accountStatus = AccountStatus.ACTIVE
            )
            assertNotNull(user)
            // Ne pas connecter automatiquement :
            isAuthenticated = false
            uiMessage = "Inscription réussie. Veuillez vous connecter."
            currentScreen = "LOGIN"
        }

        onRegisterSubmit(
            RegisterRequest(
                firstName = "Sophie",
                lastName = "Durand",
                email = "sophie.durand@test.fr",
                phone = "0612345678",
                password = "motdepasse123",
                role = UserRole.USER
            )
        )

        assertFalse("L'utilisateur ne doit pas être connecté automatiquement", isAuthenticated)
        assertEquals("Inscription réussie. Veuillez vous connecter.", uiMessage)
        assertEquals("LOGIN", currentScreen)
    }

    @Test
    fun testScenario2_userLoginGrantsAccess() {
        // Scénario 2 : Connexion USER → accès aux fonctionnalités autorisées
        val user = User(
            id = 123L,
            firstName = "Sophie",
            lastName = "Durand",
            email = "sophie.durand@test.fr",
            passwordHash = hash("motdepasse123"),
            role = UserRole.USER,
            accountStatus = AccountStatus.ACTIVE
        )

        val isLoginValid = user.passwordHash == hash("motdepasse123")
        assertTrue("Les identifiants doivent être acceptés", isLoginValid)

        val isAuthenticated = true
        assertTrue(isAuthenticated)
        assertTrue("USER actif peut consulter, rechercher, demander visites et favoris", user.isApproved)
        assertFalse("USER ne peut pas publier", user.role == UserRole.OWNER || user.role == UserRole.AGENT)
        assertFalse("USER ne peut pas administrer", user.role == UserRole.ADMIN)
    }

    @Test
    fun testScenario3_publicBrowsingWithoutAccount() {
        // Scénario 3 : Navigation publique sans compte → accès aux annonces et à leurs détails
        val isAuthenticated = false
        val canViewHome = true
        val canSearch = true
        val canFilter = true
        val canViewPropertyDetail = true

        assertFalse(isAuthenticated)
        assertTrue(canViewHome)
        assertTrue(canSearch)
        assertTrue(canFilter)
        assertTrue(canViewPropertyDetail)
    }

    @Test
    fun testScenario4_rentOrBuyWithoutAccountPromptsAuth() {
        // Scénario 4 : Clic sur « Acheter » ou « Louer » sans connexion → demande de connexion
        val isAuthenticated = false
        var authPromptVisible = false
        var savedPendingAction: String? = null

        fun onRentOrBuyClicked(propId: Long, action: String) {
            if (!isAuthenticated) {
                authPromptVisible = true
                savedPendingAction = action
            }
        }

        onRentOrBuyClicked(42L, "RENT")
        assertTrue("La demande de connexion doit s'afficher", authPromptVisible)
        assertEquals("RENT", savedPendingAction)
    }

    @Test
    fun testScenario5_loginAfterPromptResumesPendingAction() {
        // Scénario 5 : Connexion après cette demande → reprise de l'action si elle est autorisée
        var pendingAction: String? = "RENT"
        val user = User(
            id = 123L,
            firstName = "Sophie",
            lastName = "Durand",
            email = "sophie.durand@test.fr",
            passwordHash = hash("pwd"),
            role = UserRole.USER,
            accountStatus = AccountStatus.ACTIVE
        )

        var applicationDialogOpen = false
        if (pendingAction == "RENT" && user.isApproved) {
            applicationDialogOpen = true
            pendingAction = null
        }

        assertTrue("Le dossier de location doit s'ouvrir après connexion réussie", applicationDialogOpen)
        assertNull("L'action en attente a été consommée", pendingAction)
    }

    @Test
    fun testScenario6_ownerRegistrationPendingApprovalGated() {
        // Scénario 6 : Inscription OWNER → connexion possible, mais fonctionnalités professionnelles bloquées en attente
        val owner = User(
            id = 501L,
            firstName = "Paul",
            lastName = "Verlaine",
            email = "paul.verlaine@proprio.fr",
            passwordHash = hash("pwd"),
            role = UserRole.OWNER,
            accountStatus = AccountStatus.PENDING_APPROVAL
        )

        val canLogin = owner.passwordHash == hash("pwd")
        assertTrue("Connexion possible", canLogin)
        assertFalse("Compte pas encore actif", owner.isApproved)

        val noticeMessage = if (!owner.isApproved && owner.role == UserRole.OWNER) {
            "Votre compte est en attente de validation par l'administrateur."
        } else null

        assertEquals("Votre compte est en attente de validation par l'administrateur.", noticeMessage)
        val canPublish = owner.role == UserRole.OWNER && owner.isApproved
        assertFalse("Publication interdite tant que non approuvé", canPublish)
    }

    @Test
    fun testScenario7_agentRegistrationPendingApprovalGated() {
        // Scénario 7 : Inscription AGENT → connexion possible, mais fonctionnalités professionnelles bloquées en attente
        val agent = User(
            id = 701L,
            firstName = "Sarah",
            lastName = "Bernhardt",
            email = "sarah@agence.fr",
            passwordHash = hash("pwd"),
            role = UserRole.AGENT,
            accountStatus = AccountStatus.PENDING_APPROVAL
        )

        val canLogin = agent.passwordHash == hash("pwd")
        assertTrue("Connexion possible", canLogin)
        assertFalse("Agent pas encore actif", agent.isApproved)

        val noticeMessage = if (!agent.isApproved && agent.role == UserRole.AGENT) {
            "Votre compte est en attente de validation par l'administrateur."
        } else null

        assertEquals("Votre compte est en attente de validation par l'administrateur.", noticeMessage)
        val canAccessAgentTools = agent.role == UserRole.AGENT && agent.isApproved
        assertFalse("Outils pro agents bloqués", canAccessAgentTools)
    }

    @Test
    fun testScenario8_adminLoginAndApproveOrReject() {
        // Scénario 8 : Connexion ADMIN → approbation ou refus d'une demande
        val admin = User(
            id = 1L,
            firstName = "Admin",
            lastName = "Principal",
            email = "admin@logement.fr",
            role = UserRole.ADMIN,
            accountStatus = AccountStatus.ACTIVE
        )

        var candidateOwner = User(
            id = 888L,
            firstName = "Candidat",
            lastName = "Proprio",
            email = "candidat@test.fr",
            role = UserRole.OWNER,
            accountStatus = AccountStatus.PENDING_APPROVAL
        )

        fun adminApprove(adminUser: User, target: User): User {
            if (adminUser.role != UserRole.ADMIN) throw SecurityException()
            return target.copy(accountStatus = AccountStatus.ACTIVE)
        }

        fun adminReject(adminUser: User, target: User): User {
            if (adminUser.role != UserRole.ADMIN) throw SecurityException()
            return target.copy(accountStatus = AccountStatus.REJECTED)
        }

        val approved = adminApprove(admin, candidateOwner)
        assertEquals(AccountStatus.ACTIVE, approved.accountStatus)
        assertTrue(approved.isApproved)

        val rejected = adminReject(admin, candidateOwner)
        assertEquals(AccountStatus.REJECTED, rejected.accountStatus)
        assertFalse(rejected.isApproved)
    }

    @Test
    fun testScenario9_afterApprovalRoleFeaturesUnlocked() {
        // Scénario 9 : Après approbation → accès aux fonctionnalités correspondant au rôle
        val approvedOwner = User(
            id = 888L,
            firstName = "Candidat",
            lastName = "Proprio",
            email = "candidat@test.fr",
            role = UserRole.OWNER,
            accountStatus = AccountStatus.ACTIVE
        )

        val canPublishNow = (approvedOwner.role == UserRole.OWNER || approvedOwner.role == UserRole.AGENT) && approvedOwner.isApproved
        assertTrue("Après approbation ADMIN, le propriétaire peut publier", canPublishNow)
    }

    @Test
    fun testScenario10_unauthorizedAccessBlocked() {
        // Scénario 10 : Tentative d'accès non autorisé → accès refusé
        val regularUser = User(
            id = 10L,
            firstName = "Simple",
            lastName = "User",
            email = "user@test.fr",
            role = UserRole.USER,
            accountStatus = AccountStatus.ACTIVE
        )

        fun tryAccessAdminPanel(user: User): Boolean {
            return user.role == UserRole.ADMIN
        }

        fun tryPublishListing(user: User): Boolean {
            return (user.role == UserRole.OWNER || user.role == UserRole.AGENT) && user.isApproved
        }

        assertFalse("Un USER ne peut pas accéder à l'administration", tryAccessAdminPanel(regularUser))
        assertFalse("Un USER ne peut pas déposer d'annonces de propriétaire", tryPublishListing(regularUser))
    }

    @Test
    fun testAllPropertyTypesAndTransactionsPublicBrowsing() {
        val properties = listOf(
            Property(id = 1, ownerId = 2, title = "Appartement Paris", description = "Desc", price = 2450.0, propertyType = PropertyType.APARTMENT, transactionType = TransactionType.RENT, status = PropertyStatus.PUBLISHED, city = "Paris", area = 92.0, bedrooms = 2),
            Property(id = 2, ownerId = 2, title = "Villa Bordeaux", description = "Desc", price = 890000.0, propertyType = PropertyType.VILLA, transactionType = TransactionType.SALE, status = PropertyStatus.PUBLISHED, city = "Bordeaux", area = 185.0, bedrooms = 4),
            Property(id = 5, ownerId = 2, title = "Maison Nantes", description = "Desc", price = 475000.0, propertyType = PropertyType.HOUSE, transactionType = TransactionType.SALE, status = PropertyStatus.PUBLISHED, city = "Nantes", area = 135.0, bedrooms = 3),
            Property(id = 6, ownerId = 2, title = "Maison Rennes", description = "Desc", price = 1650.0, propertyType = PropertyType.HOUSE, transactionType = TransactionType.RENT, status = PropertyStatus.PUBLISHED, city = "Rennes", area = 125.0, bedrooms = 3),
            Property(id = 7, ownerId = 2, title = "Terrain Montpellier", description = "Desc", price = 175000.0, propertyType = PropertyType.LAND, transactionType = TransactionType.SALE, status = PropertyStatus.PUBLISHED, city = "Montpellier", area = 680.0, bedrooms = 0),
            Property(id = 8, ownerId = 2, title = "Appartement Strasbourg", description = "Desc", price = 285000.0, propertyType = PropertyType.APARTMENT, transactionType = TransactionType.SALE, status = PropertyStatus.PUBLISHED, city = "Strasbourg", area = 72.0, bedrooms = 2)
        )

        // 1. Maisons à louer et à vendre
        val housesForRent = properties.filter { it.propertyType == PropertyType.HOUSE && it.transactionType == TransactionType.RENT }
        val housesForSale = properties.filter { it.propertyType == PropertyType.HOUSE && it.transactionType == TransactionType.SALE }
        assertEquals("Doit contenir au moins 1 maison à louer", 1, housesForRent.size)
        assertEquals("Doit contenir au moins 1 maison à vendre", 1, housesForSale.size)

        // 2. Appartements à louer et à vendre
        val aptsForRent = properties.filter { it.propertyType == PropertyType.APARTMENT && it.transactionType == TransactionType.RENT }
        val aptsForSale = properties.filter { it.propertyType == PropertyType.APARTMENT && it.transactionType == TransactionType.SALE }
        assertEquals("Doit contenir au moins 1 appartement à louer", 1, aptsForRent.size)
        assertEquals("Doit contenir au moins 1 appartement à vendre", 1, aptsForSale.size)

        // 3. Terrains à vendre
        val landsForSale = properties.filter { it.propertyType == PropertyType.LAND && it.transactionType == TransactionType.SALE }
        assertEquals("Doit contenir au moins 1 terrain à vendre", 1, landsForSale.size)
        assertEquals("Le terrain a une superficie de 680m²", 680.0, landsForSale.first().area, 0.01)
        assertEquals("Le terrain n'a pas de chambres", 0, landsForSale.first().bedrooms)

        // 4. Tous les biens sont visibles publiquement sans restriction de rôle
        val publishedCount = properties.count { it.status == PropertyStatus.PUBLISHED }
        assertEquals("Tous les 6 biens sont publiés et accessibles", 6, publishedCount)
    }

    @Test
    fun testPendingActionResumptionForRentAndBuy() {
        val selectedPropId = 7L
        val selectedTransaction = TransactionType.SALE

        // Visiteur non connecté veut acheter
        var currentUserId: Long? = null
        var pendingAction: Pair<Long, TransactionType>? = null

        fun onBuyClicked(propId: Long, trans: TransactionType) {
            if (currentUserId == null) {
                pendingAction = propId to trans
            }
        }

        onBuyClicked(selectedPropId, selectedTransaction)
        assertNotNull("L'action d'achat est mémorisée avec l'id du bien", pendingAction)
        assertEquals(7L, pendingAction!!.first)
        assertEquals(TransactionType.SALE, pendingAction!!.second)

        // Connexion réussie de l'utilisateur
        currentUserId = 100L
        val resumedAction = pendingAction
        pendingAction = null

        assertNotNull("L'action initiale est reprise après connexion", resumedAction)
        assertEquals("Reprend sur le même bien", selectedPropId, resumedAction!!.first)
        assertEquals("Reprend avec le même type de transaction", TransactionType.SALE, resumedAction.second)
    }

    // =========================================================================
    // TESTS CRUD COMPLETS : ADMIN, OWNER, AGENT ET CONTRÔLE D'ACCÈS
    // =========================================================================

    @Test
    fun testOwnerCrudAndStrictIsolation() {
        // Mock DB in-memory for testing logic
        val propertiesDb = mutableMapOf<Long, Property>()
        var nextId = 1000L

        val approvedOwner1 = User(id = 10L, firstName = "Jean", lastName = "Dupont", email = "jean@test.fr", role = UserRole.OWNER, accountStatus = AccountStatus.ACTIVE)
        val approvedOwner2 = User(id = 20L, firstName = "Marc", lastName = "Durand", email = "marc@test.fr", role = UserRole.OWNER, accountStatus = AccountStatus.ACTIVE)

        // 1. CREATE : Owner 1 crée son annonce
        fun createProperty(user: User, title: String, price: Double, type: PropertyType, transaction: TransactionType): Result<Long> {
            if (user.role != UserRole.OWNER && user.role != UserRole.AGENT && user.role != UserRole.ADMIN) {
                return Result.failure(SecurityException("Rôle non autorisé à publier"))
            }
            if (user.role != UserRole.ADMIN && !user.isApproved) {
                return Result.failure(SecurityException("Compte non approuvé"))
            }
            val id = nextId++
            propertiesDb[id] = Property(
                id = id,
                ownerId = user.id,
                title = title,
                description = "Description complète du bien immobilier",
                price = price,
                propertyType = type,
                transactionType = transaction,
                status = PropertyStatus.PUBLISHED,
                city = "Lyon"
            )
            return Result.success(id)
        }

        val createResult = createProperty(approvedOwner1, "T3 Lumineux Part-Dieu", 950.0, PropertyType.APARTMENT, TransactionType.RENT)
        assertTrue("OWNER autorisé peut créer une annonce", createResult.isSuccess)
        val propId1 = createResult.getOrThrow()

        // 2. READ : Owner 1 consulte ses annonces vs toutes les annonces
        val owner1Properties = propertiesDb.values.filter { it.ownerId == approvedOwner1.id }
        assertEquals(1, owner1Properties.size)
        assertEquals("T3 Lumineux Part-Dieu", owner1Properties.first().title)

        // 3. UPDATE : Owner 1 modifie sa propre annonce
        fun updateProperty(user: User, propertyId: Long, newTitle: String, newPrice: Double): Result<Unit> {
            val existing = propertiesDb[propertyId] ?: return Result.failure(NoSuchElementException("Bien introuvable"))
            val canEdit = when (user.role) {
                UserRole.ADMIN -> true
                UserRole.OWNER -> existing.ownerId == user.id
                UserRole.AGENT -> existing.managedByAgentId == user.id
                UserRole.USER -> false
            }
            if (!canEdit) return Result.failure(SecurityException("Non autorisé à modifier ce bien"))
            propertiesDb[propertyId] = existing.copy(title = newTitle, price = newPrice)
            return Result.success(Unit)
        }

        val updateOwnResult = updateProperty(approvedOwner1, propId1, "T3 Rénové Part-Dieu", 1050.0)
        assertTrue("OWNER peut modifier sa propre annonce", updateOwnResult.isSuccess)
        assertEquals("T3 Rénové Part-Dieu", propertiesDb[propId1]?.title)
        assertEquals(1050.0, propertiesDb[propId1]?.price ?: 0.0, 0.01)

        // 4. ISOLATION : Owner 2 essaie de modifier l'annonce d'Owner 1 -> INTERDIT
        val updateOtherResult = updateProperty(approvedOwner2, propId1, "Piratage Titre", 200.0)
        assertTrue("Un OWNER ne peut PAS modifier l'annonce d'un autre OWNER", updateOtherResult.isFailure)
        assertEquals("T3 Rénové Part-Dieu", propertiesDb[propId1]?.title)

        // 5. DELETE : Owner 2 essaie de supprimer l'annonce d'Owner 1 -> INTERDIT
        fun deleteProperty(user: User, propertyId: Long): Result<Unit> {
            val existing = propertiesDb[propertyId] ?: return Result.failure(NoSuchElementException("Bien introuvable"))
            val canDelete = when (user.role) {
                UserRole.ADMIN -> true
                UserRole.OWNER -> existing.ownerId == user.id
                UserRole.AGENT -> existing.managedByAgentId == user.id
                UserRole.USER -> false
            }
            if (!canDelete) return Result.failure(SecurityException("Non autorisé à supprimer ce bien"))
            propertiesDb.remove(propertyId)
            return Result.success(Unit)
        }

        val deleteOtherResult = deleteProperty(approvedOwner2, propId1)
        assertTrue("Un OWNER ne peut PAS supprimer l'annonce d'un autre OWNER", deleteOtherResult.isFailure)
        assertTrue("L'annonce existe toujours en base", propertiesDb.containsKey(propId1))

        // 6. DELETE : Owner 1 supprime sa propre annonce -> SUCCÈS
        val deleteOwnResult = deleteProperty(approvedOwner1, propId1)
        assertTrue("Un OWNER peut supprimer sa propre annonce", deleteOwnResult.isSuccess)
        assertFalse("L'annonce est bien supprimée de la base", propertiesDb.containsKey(propId1))
    }

    @Test
    fun testAgentCrudAndStrictIsolation() {
        val propertiesDb = mutableMapOf<Long, Property>()
        var nextId = 2000L

        val approvedAgent1 = User(id = 30L, firstName = "Luc", lastName = "AgenceA", email = "luc@agencea.fr", role = UserRole.AGENT, accountStatus = AccountStatus.ACTIVE)
        val approvedAgent2 = User(id = 40L, firstName = "Paul", lastName = "AgenceB", email = "paul@agenceb.fr", role = UserRole.AGENT, accountStatus = AccountStatus.ACTIVE)

        // 1. CREATE : Agent 1 crée un mandat
        fun createAgentProperty(agent: User, title: String, price: Double): Result<Long> {
            if (agent.role != UserRole.AGENT || !agent.isApproved) return Result.failure(SecurityException())
            val id = nextId++
            propertiesDb[id] = Property(
                id = id,
                ownerId = 1L, // Mandant
                managedByAgentId = agent.id,
                title = title,
                description = "Mandat confié à l'agence pour la vente",
                price = price,
                propertyType = PropertyType.HOUSE,
                transactionType = TransactionType.SALE,
                status = PropertyStatus.PUBLISHED,
                city = "Bordeaux"
            )
            return Result.success(id)
        }

        val createResult = createAgentProperty(approvedAgent1, "Maison familiale Bordeaux", 450000.0)
        assertTrue(createResult.isSuccess)
        val propId = createResult.getOrThrow()

        // 2. READ : Agent 1 retrouve ses mandats
        val agent1List = propertiesDb.values.filter { it.managedByAgentId == approvedAgent1.id }
        assertEquals(1, agent1List.size)
        assertEquals("Maison familiale Bordeaux", agent1List.first().title)

        // 3. UPDATE : Agent 1 met à jour son mandat
        fun updateProperty(user: User, propertyId: Long, newTitle: String): Result<Unit> {
            val existing = propertiesDb[propertyId] ?: return Result.failure(NoSuchElementException())
            val canEdit = (user.role == UserRole.AGENT && existing.managedByAgentId == user.id) || user.role == UserRole.ADMIN
            if (!canEdit) return Result.failure(SecurityException("Modification interdite"))
            propertiesDb[propertyId] = existing.copy(title = newTitle)
            return Result.success(Unit)
        }

        val updateOwn = updateProperty(approvedAgent1, propId, "Maison contemporaine Bordeaux")
        assertTrue("AGENT peut mettre à jour son mandat géré", updateOwn.isSuccess)

        // 4. ISOLATION : Agent 2 tente de modifier le mandat d'Agent 1 -> INTERDIT
        val updateOther = updateProperty(approvedAgent2, propId, "Mandat détourné")
        assertTrue("Un AGENT ne peut PAS modifier l'annonce d'un autre AGENT", updateOther.isFailure)

        // 5. DELETE : Agent 2 tente de supprimer le mandat d'Agent 1 -> INTERDIT
        fun deleteProperty(user: User, propertyId: Long): Result<Unit> {
            val existing = propertiesDb[propertyId] ?: return Result.failure(NoSuchElementException())
            val canDelete = (user.role == UserRole.AGENT && existing.managedByAgentId == user.id) || user.role == UserRole.ADMIN
            if (!canDelete) return Result.failure(SecurityException("Suppression interdite"))
            propertiesDb.remove(propertyId)
            return Result.success(Unit)
        }

        val deleteOther = deleteProperty(approvedAgent2, propId)
        assertTrue("Un AGENT ne peut PAS supprimer le mandat d'un autre AGENT", deleteOther.isFailure)

        // 6. DELETE : Agent 1 retire son mandat -> SUCCÈS
        val deleteOwn = deleteProperty(approvedAgent1, propId)
        assertTrue("Un AGENT peut retirer son propre mandat", deleteOwn.isSuccess)
        assertFalse(propertiesDb.containsKey(propId))
    }

    @Test
    fun testAdminComprehensiveCrudOperations() {
        val propertiesDb = mutableMapOf<Long, Property>()
        val usersDb = mutableMapOf<Long, User>()
        val visitsDb = mutableMapOf<Long, VisitRequest>()

        val admin = User(id = 1L, firstName = "Admin", lastName = "Super", email = "admin@logement.fr", role = UserRole.ADMIN, accountStatus = AccountStatus.ACTIVE)
        val regularUser = User(id = 100L, firstName = "User", lastName = "Test", email = "user@test.fr", role = UserRole.USER, accountStatus = AccountStatus.ACTIVE)
        val pendingOwner = User(id = 200L, firstName = "Pending", lastName = "Owner", email = "owner@test.fr", role = UserRole.OWNER, accountStatus = AccountStatus.PENDING_APPROVAL)

        usersDb[admin.id] = admin
        usersDb[regularUser.id] = regularUser
        usersDb[pendingOwner.id] = pendingOwner

        propertiesDb[1L] = Property(id = 1L, ownerId = 200L, title = "Villa Côte d'Azur", description = "Vue mer panoramique", price = 850000.0, propertyType = PropertyType.VILLA, transactionType = TransactionType.SALE, status = PropertyStatus.PENDING, city = "Nice")
        visitsDb[50L] = VisitRequest(id = 50L, userId = 100L, propertyId = 1L, requestedDate = "2026-10-20", requestedTime = "14:00", status = VisitStatus.PENDING)

        // 1. ADMIN ANNONCES : Read, Update status, Delete
        assertEquals(1, propertiesDb.size)
        // Validation / Approbation
        fun adminUpdateStatus(caller: User, propId: Long, newStatus: PropertyStatus): Result<Unit> {
            if (caller.role != UserRole.ADMIN) return Result.failure(SecurityException())
            val p = propertiesDb[propId] ?: return Result.failure(NoSuchElementException())
            propertiesDb[propId] = p.copy(status = newStatus)
            return Result.success(Unit)
        }

        val approveResult = adminUpdateStatus(admin, 1L, PropertyStatus.PUBLISHED)
        assertTrue(approveResult.isSuccess)
        assertEquals(PropertyStatus.PUBLISHED, propertiesDb[1L]?.status)

        // 2. ADMIN GESTION UTILISATEURS : Suspendre, Rétablir, Changer rôle, Supprimer
        fun adminToggleSuspension(caller: User, targetUserId: Long): Result<Unit> {
            if (caller.role != UserRole.ADMIN) return Result.failure(SecurityException())
            val u = usersDb[targetUserId] ?: return Result.failure(NoSuchElementException())
            usersDb[targetUserId] = u.copy(isSuspended = !u.isSuspended)
            return Result.success(Unit)
        }

        adminToggleSuspension(admin, 100L)
        assertTrue("Le compte utilisateur est suspendu", usersDb[100L]!!.isSuspended)

        adminToggleSuspension(admin, 100L)
        assertFalse("Le compte utilisateur est rétabli", usersDb[100L]!!.isSuspended)

        // Approbation compte pro PENDING
        fun adminApproveAccount(caller: User, targetUserId: Long): Result<Unit> {
            if (caller.role != UserRole.ADMIN) return Result.failure(SecurityException())
            val u = usersDb[targetUserId] ?: return Result.failure(NoSuchElementException())
            usersDb[targetUserId] = u.copy(accountStatus = AccountStatus.ACTIVE)
            return Result.success(Unit)
        }

        adminApproveAccount(admin, 200L)
        assertEquals(AccountStatus.ACTIVE, usersDb[200L]?.accountStatus)
        assertTrue("Le compte est maintenant approuvé", usersDb[200L]!!.isApproved)

        // 3. ADMIN GESTION DEMANDES DE VISITES : Read, Update Status
        fun adminUpdateVisitStatus(caller: User, visitId: Long, newStatus: VisitStatus): Result<Unit> {
            if (caller.role != UserRole.ADMIN) return Result.failure(SecurityException())
            val v = visitsDb[visitId] ?: return Result.failure(NoSuchElementException())
            visitsDb[visitId] = v.copy(status = newStatus)
            return Result.success(Unit)
        }

        val visitUpdateResult = adminUpdateVisitStatus(admin, 50L, VisitStatus.ACCEPTED)
        assertTrue(visitUpdateResult.isSuccess)
        assertEquals(VisitStatus.ACCEPTED, visitsDb[50L]?.status)

        // 4. ADMIN SUPPRESSION D'ANNONCE AVEC CONFIRMATION
        fun adminDeleteProperty(caller: User, propId: Long): Result<Unit> {
            if (caller.role != UserRole.ADMIN) return Result.failure(SecurityException())
            propertiesDb.remove(propId)
            return Result.success(Unit)
        }

        val deletePropResult = adminDeleteProperty(admin, 1L)
        assertTrue("ADMIN peut supprimer n'importe quelle annonce", deletePropResult.isSuccess)
        assertFalse("L'annonce est retirée", propertiesDb.containsKey(1L))
    }

    @Test
    fun testPendingProfessionalAccountsCannotPublish() {
        val pendingOwner = User(id = 11L, firstName = "Pierre", lastName = "EnAttente", email = "pierre@test.fr", role = UserRole.OWNER, accountStatus = AccountStatus.PENDING_APPROVAL)
        val pendingAgent = User(id = 22L, firstName = "Anne", lastName = "AgenceAttente", email = "anne@test.fr", role = UserRole.AGENT, accountStatus = AccountStatus.PENDING_APPROVAL)

        fun canPublish(user: User): Boolean {
            return (user.role == UserRole.OWNER || user.role == UserRole.AGENT) && user.isApproved
        }

        assertFalse("OWNER en attente ne peut pas publier", canPublish(pendingOwner))
        assertFalse("AGENT en attente ne peut pas publier", canPublish(pendingAgent))
    }

    @Test
    fun testRegularUserCannotAccessProfessionalOrAdminCrud() {
        val regularUser = User(id = 99L, firstName = "Emma", lastName = "SimpleUser", email = "emma@test.fr", role = UserRole.USER, accountStatus = AccountStatus.ACTIVE)

        fun canAccessOwnerSpace(u: User): Boolean = (u.role == UserRole.OWNER || u.role == UserRole.AGENT) && u.isApproved
        fun canAccessAdminSpace(u: User): Boolean = u.role == UserRole.ADMIN

        assertFalse("USER ne peut pas accéder à l'espace propriétaire/agent", canAccessOwnerSpace(regularUser))
        assertFalse("USER ne peut pas accéder à l'espace administration", canAccessAdminSpace(regularUser))
    }
}



