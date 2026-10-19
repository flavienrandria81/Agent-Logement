package com.example.data.local

import com.example.data.model.AccountStatus
import com.example.data.model.Agency
import com.example.data.model.Conversation
import com.example.data.model.ConversationParticipant
import com.example.data.model.Favorite
import com.example.data.model.Message
import com.example.data.model.Notification
import com.example.data.model.NotificationType
import com.example.data.model.Property
import com.example.data.model.PropertyImage
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.Report
import com.example.data.model.ReportStatus
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.auth.JwtManager
import com.example.data.model.VisitRequest
import com.example.data.model.VisitStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {
    suspend fun seedDatabaseIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val userDao = database.userDao()
        val existing = userDao.getUserByIdSync(1)
        if (existing != null) return@withContext

        // 1. Agencies
        val agencies = listOf(
            Agency(id = 1, name = "Horizon Immobilier Prestige", address = "14 Avenue Montaigne, 75008 Paris", phone = "01 42 68 55 00"),
            Agency(id = 2, name = "Agence des Chartrons", address = "28 Cours Portal, 33000 Bordeaux", phone = "05 56 44 20 10")
        )
        userDao.insertAgencies(agencies)

        // 2. Initial Platform Accounts (Hashés selon la norme de production SHA-256)
        val defaultHash = JwtManager.hashPassword("password123")
        val adminHash = JwtManager.hashPassword("admin123")

        val users = listOf(
            User(
                id = 1,
                firstName = "Jean",
                lastName = "Dupont",
                email = "jean.dupont@email.fr",
                phone = "06 12 34 56 78",
                passwordHash = defaultHash,
                role = UserRole.USER,
                photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
                emailVerified = true
            ),
            User(
                id = 2,
                firstName = "Claire",
                lastName = "Martin",
                email = "claire.martin@proprietaire.fr",
                phone = "06 98 76 54 32",
                passwordHash = defaultHash,
                role = UserRole.OWNER,
                photoUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=300&q=80",
                emailVerified = true
            ),
            User(
                id = 3,
                firstName = "Marc",
                lastName = "Lefebvre",
                email = "marc.lefebvre@agence-horizon.fr",
                phone = "06 45 67 89 01",
                passwordHash = defaultHash,
                role = UserRole.AGENT,
                photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80",
                agencyId = 1,
                emailVerified = true
            ),
            User(
                id = 4,
                firstName = "Sophie",
                lastName = "Bernard",
                email = "sophie.bernard@logement-admin.fr",
                phone = "01 80 90 00 11",
                passwordHash = adminHash,
                role = UserRole.ADMIN,
                photoUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80",
                emailVerified = true
            ),
            User(
                id = 5,
                firstName = "Thomas",
                lastName = "Roussel",
                email = "thomas.roussel@proprietaire.fr",
                phone = "06 77 88 99 00",
                passwordHash = defaultHash,
                role = UserRole.OWNER,
                accountStatus = AccountStatus.PENDING_APPROVAL,
                photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80",
                emailVerified = true
            ),
            User(
                id = 6,
                firstName = "Julien",
                lastName = "Mercier",
                email = "julien.mercier@cabinet-immo.fr",
                phone = "06 55 44 33 22",
                passwordHash = defaultHash,
                role = UserRole.AGENT,
                accountStatus = AccountStatus.PENDING_APPROVAL,
                photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=300&q=80",
                agencyId = 2,
                emailVerified = true
            )
        )
        userDao.insertUsers(users)

        // 3. Properties
        val propertyDao = database.propertyDao()
        val properties = listOf(
            Property(
                id = 1,
                ownerId = 2,
                title = "Appartement Haussmannien Lumineux",
                description = "Magnifique appartement de réception avec parquet point de Hongrie, moulures d'époque et cheminées en marbre. Double séjour baigné de lumière, cuisine haut de gamme indépendante et suite parentale avec dressing.",
                price = 2450.0,
                propertyType = PropertyType.APARTMENT,
                transactionType = TransactionType.RENT,
                status = PropertyStatus.PUBLISHED,
                address = "18 Rue Oberkampf",
                city = "Paris",
                district = "11ème",
                latitude = 48.8647,
                longitude = 2.3708,
                area = 92.0,
                bedrooms = 2,
                bathrooms = 1,
                rooms = 4,
                floor = 3,
                parking = false,
                viewCount = 142
            ),
            Property(
                id = 2,
                ownerId = 2,
                managedByAgentId = 3,
                title = "Villa Contemporaine avec Piscine",
                description = "Splendide villa d'architecte contemporaine construite sur un terrain arboré de 800m². Grandes baies vitrées ouvrant sur terrasse en teck, piscine à débordement et prestations thermiques passives.",
                price = 890000.0,
                propertyType = PropertyType.VILLA,
                transactionType = TransactionType.SALE,
                status = PropertyStatus.PUBLISHED,
                address = "42 Avenue des Fleurs",
                city = "Bordeaux",
                district = "Chartrons",
                latitude = 44.8566,
                longitude = -0.5750,
                area = 185.0,
                bedrooms = 4,
                bathrooms = 3,
                rooms = 6,
                floor = 0,
                parking = true,
                viewCount = 312
            ),
            Property(
                id = 3,
                ownerId = 2,
                managedByAgentId = 3,
                title = "Loft Industriel Rénové",
                description = "Ancien atelier d'artiste transformé en loft d'exception avec plus de 4m de hauteur sous plafond. Verrières métalliques, espace bureau en mezzanine et climatisation réversible.",
                price = 1680.0,
                propertyType = PropertyType.APARTMENT,
                transactionType = TransactionType.RENT,
                status = PropertyStatus.PUBLISHED,
                address = "8 Quai Rambaud",
                city = "Lyon",
                district = "Confluence",
                latitude = 45.7485,
                longitude = 4.8198,
                area = 78.0,
                bedrooms = 1,
                bathrooms = 1,
                rooms = 2,
                floor = 1,
                parking = true,
                viewCount = 98
            ),
            Property(
                id = 4,
                ownerId = 2,
                title = "Studio Meublé Hyper-Centre",
                description = "Studio rénové avec goût au pied des commerces et du métro. Espace optimisé avec lit escamotable, kitchenette équipée et salle d'eau avec douche à l'italienne.",
                price = 650.0,
                propertyType = PropertyType.STUDIO,
                transactionType = TransactionType.RENT,
                status = PropertyStatus.PENDING, // En attente de validation par Admin
                address = "12 Rue de la République",
                city = "Marseille",
                district = "Vieux-Port",
                latitude = 43.2965,
                longitude = 5.3698,
                area = 24.0,
                bedrooms = 1,
                bathrooms = 1,
                rooms = 1,
                floor = 4,
                parking = false,
                viewCount = 19
            ),
            Property(
                id = 5,
                ownerId = 2,
                title = "Maison Familiale avec Jardin Arboré",
                description = "Maison de charme 5 pièces située dans une impasse calme. Grande cuisine familiale, salon avec cheminée, jardin sans vis-à-vis et garage fermé.",
                price = 475000.0,
                propertyType = PropertyType.HOUSE,
                transactionType = TransactionType.SALE,
                status = PropertyStatus.PUBLISHED,
                address = "5 Impasse des Lilas",
                city = "Nantes",
                district = "Procè",
                latitude = 47.2280,
                longitude = -1.5830,
                area = 135.0,
                bedrooms = 3,
                bathrooms = 2,
                rooms = 5,
                floor = 0,
                parking = true,
                viewCount = 87
            ),
            Property(
                id = 6,
                ownerId = 2,
                managedByAgentId = 3,
                title = "Maison Contemporaine avec Patio",
                description = "Maison moderne à louer dans un quartier résidentiel très prisé. Vaste séjour lumineux donnant sur patio végétalisé, suite parentale au rez-de-chaussée et deux chambres à l'étage.",
                price = 1650.0,
                propertyType = PropertyType.HOUSE,
                transactionType = TransactionType.RENT,
                status = PropertyStatus.PUBLISHED,
                address = "24 Rue de Fougères",
                city = "Rennes",
                district = "Thabor",
                latitude = 48.1173,
                longitude = -1.6778,
                area = 125.0,
                bedrooms = 3,
                bathrooms = 2,
                rooms = 4,
                floor = 0,
                parking = true,
                viewCount = 64
            ),
            Property(
                id = 7,
                ownerId = 2,
                managedByAgentId = 3,
                title = "Grand Terrain Constructible Viabilisé",
                description = "Rare à la vente ! Beau terrain plat de 680m² entièrement viabilisé (eau, électricité, tout-à-l'égout, fibre optique). Libre de constructeur, exposition Sud-Ouest avec vue dégagée.",
                price = 175000.0,
                propertyType = PropertyType.LAND,
                transactionType = TransactionType.SALE,
                status = PropertyStatus.PUBLISHED,
                address = "Chemin des Oliviers",
                city = "Montpellier",
                district = "Castelnau",
                latitude = 43.6108,
                longitude = 3.8767,
                area = 680.0,
                bedrooms = 0,
                bathrooms = 0,
                rooms = 0,
                floor = 0,
                parking = false,
                viewCount = 115
            ),
            Property(
                id = 8,
                ownerId = 2,
                title = "Appartement T3 Rénové avec Balcon",
                description = "Superbe appartement 3 pièces traversant dans un immeuble récent de standing. Salon parqueté ouvrant sur balcon ensoleillé, cuisine moderne équipée et deux chambres spacieuses.",
                price = 285000.0,
                propertyType = PropertyType.APARTMENT,
                transactionType = TransactionType.SALE,
                status = PropertyStatus.PUBLISHED,
                address = "15 Avenue des Vosges",
                city = "Strasbourg",
                district = "Neustadt",
                latitude = 48.5880,
                longitude = 7.7560,
                area = 72.0,
                bedrooms = 2,
                bathrooms = 1,
                rooms = 3,
                floor = 2,
                parking = true,
                viewCount = 76
            )
        )
        propertyDao.insertProperties(properties)

        // 4. Property Images
        val images = listOf(
            PropertyImage(propertyId = 1, imageUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80", isPrimary = true, displayOrder = 0),
            PropertyImage(propertyId = 1, imageUrl = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 1),
            PropertyImage(propertyId = 1, imageUrl = "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 2),

            PropertyImage(propertyId = 2, imageUrl = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80", isPrimary = true, displayOrder = 0),
            PropertyImage(propertyId = 2, imageUrl = "https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 1),
            PropertyImage(propertyId = 2, imageUrl = "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 2),

            PropertyImage(propertyId = 3, imageUrl = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80", isPrimary = true, displayOrder = 0),
            PropertyImage(propertyId = 3, imageUrl = "https://images.unsplash.com/photo-1560185007-cde436f6a4d0?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 1),

            PropertyImage(propertyId = 4, imageUrl = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80", isPrimary = true, displayOrder = 0),
            PropertyImage(propertyId = 4, imageUrl = "https://images.unsplash.com/photo-1502005229762-ee1b2b9ba0cb?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 1),

            PropertyImage(propertyId = 5, imageUrl = "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80", isPrimary = true, displayOrder = 0),
            PropertyImage(propertyId = 5, imageUrl = "https://images.unsplash.com/photo-1598228723793-52759bba239c?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 1),
            PropertyImage(propertyId = 5, imageUrl = "https://images.unsplash.com/photo-1600585154526-990dced4db0d?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 2),

            PropertyImage(propertyId = 6, imageUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=800&q=80", isPrimary = true, displayOrder = 0),
            PropertyImage(propertyId = 6, imageUrl = "https://images.unsplash.com/photo-1600566753376-12c8ab7fb75b?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 1),

            PropertyImage(propertyId = 7, imageUrl = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=800&q=80", isPrimary = true, displayOrder = 0),
            PropertyImage(propertyId = 7, imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 1),

            PropertyImage(propertyId = 8, imageUrl = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80", isPrimary = true, displayOrder = 0),
            PropertyImage(propertyId = 8, imageUrl = "https://images.unsplash.com/photo-1560448204-603b3fc33ddc?auto=format&fit=crop&w=800&q=80", isPrimary = false, displayOrder = 1)
        )
        propertyDao.insertPropertyImages(images)

        // 5. Interactions
        val interactionDao = database.interactionDao()
        interactionDao.insertFavorite(Favorite(userId = 1, propertyId = 1))
        interactionDao.insertFavorite(Favorite(userId = 1, propertyId = 2))

        // Visit Requests
        interactionDao.insertVisitRequest(
            VisitRequest(
                id = 1,
                userId = 1,
                propertyId = 1,
                requestedDate = "Samedi 11 Octobre",
                requestedTime = "14:30",
                message = "Bonjour Madame Martin, je suis très intéressé par votre appartement Haussmannien. Mon dossier de location est complet et solvable.",
                status = VisitStatus.PENDING
            )
        )
        interactionDao.insertVisitRequest(
            VisitRequest(
                id = 2,
                userId = 1,
                propertyId = 2,
                requestedDate = "Mardi 14 Octobre",
                requestedTime = "10:00",
                message = "Bonjour Marc, nous souhaitons planifier une contre-visite de la villa contemporaine avec notre architecte.",
                status = VisitStatus.ACCEPTED
            )
        )

        // Conversation & Messages
        val convId = interactionDao.insertConversation(Conversation(id = 1, propertyId = 1))
        interactionDao.insertParticipant(ConversationParticipant(conversationId = convId, userId = 1))
        interactionDao.insertParticipant(ConversationParticipant(conversationId = convId, userId = 2))

        interactionDao.insertMessage(Message(conversationId = convId, senderId = 1, content = "Bonjour Madame Martin, le bien est-il toujours disponible à la location ?"))
        interactionDao.insertMessage(Message(conversationId = convId, senderId = 2, content = "Bonjour Jean ! Oui tout à fait, nous organisons les visites ce week-end."))
        interactionDao.insertMessage(Message(conversationId = convId, senderId = 1, content = "Parfait, je viens de vous envoyer une demande de créneau via l'application."))

        // Notifications
        interactionDao.insertNotification(
            Notification(
                userId = 1,
                title = "Visite acceptée !",
                message = "L'agent Marc Lefebvre a confirmé votre visite pour la Villa Contemporaine.",
                type = NotificationType.VISIT_REQUEST
            )
        )
        interactionDao.insertNotification(
            Notification(
                userId = 2,
                title = "Nouvelle demande de visite",
                message = "Jean Dupont souhaite visiter votre Appartement Haussmannien.",
                type = NotificationType.VISIT_REQUEST
            )
        )

        // Report
        interactionDao.insertReport(
            Report(
                id = 1,
                userId = 1,
                propertyId = 4,
                reason = "Photos ou description non conformes",
                description = "Le studio semble déjà loué sur d'autres annonces ou ne correspond pas à l'adresse indiquée.",
                status = ReportStatus.PENDING
            )
        )
    }
}
