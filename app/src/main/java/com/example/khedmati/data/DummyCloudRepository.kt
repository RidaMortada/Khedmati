package com.example.khedmati.data

import com.example.khedmati.model.Category
import com.example.khedmati.model.DummyUser
import com.example.khedmati.model.LocalizedText
import com.example.khedmati.model.LocationMode
import com.example.khedmati.model.NotificationItem
import com.example.khedmati.model.Post
import com.example.khedmati.model.PriceMode
import com.example.khedmati.model.Professional
import com.example.khedmati.model.Review
import com.example.khedmati.model.Service
import com.example.khedmati.model.UserRole
import java.util.UUID

object DummyCloudRepository {
    const val DATABASE_STATUS = "DUMMY DATABASE - all data stays in memory on this device process"
    const val NOTIFICATION_STATUS = "DUMMY NOTIFICATIONS - no push provider is contacted"

    var currentUser: DummyUser? = null
        private set

    private val savedProfessionalIds = linkedSetOf<String>()
    private val likedPostIds = linkedSetOf<String>()

    val categories = listOf(
        Category("electrician", LocalizedText("Electrician", "كهربائي", "Électricien"), "⚡"),
        Category("plumber", LocalizedText("Plumber", "سبّاك", "Plombier"), "🔧"),
        Category("cleaner", LocalizedText("Cleaner", "تنظيف", "Nettoyage"), "🧹"),
        Category("mechanic", LocalizedText("Mechanic", "ميكانيكي", "Mécanicien"), "🚗"),
        Category("ac", LocalizedText("Air conditioning", "تكييف", "Climatisation"), "❄️")
    )

    val professionals = mutableListOf(
        Professional(
            id = "pro-electric-1",
            publicName = LocalizedText("Beirut Electric Solutions", "حلول بيروت الكهربائية", "Solutions Électriques Beyrouth"),
            description = LocalizedText(
                "Residential and commercial electrical maintenance, fault finding and panel upgrades.",
                "صيانة كهربائية للمنازل والمؤسسات، كشف الأعطال وتحديث لوحات التوزيع.",
                "Maintenance électrique résidentielle et commerciale, diagnostic et modernisation des tableaux."
            ),
            primaryCategoryId = "electrician",
            services = mutableListOf(
                Service(
                    "svc-e1",
                    LocalizedText("Electrical inspection", "كشف كهربائي", "Inspection électrique"),
                    LocalizedText("On-site inspection and fault diagnosis.", "فحص موقعي وتشخيص الأعطال.", "Inspection sur place et diagnostic."),
                    PriceMode.STARTING_FROM,
                    minAmount = 25.0,
                    currency = "USD",
                    priceDetails = LocalizedText("Final price depends on the fault and parts.", "السعر النهائي يعتمد على العطل والقطع.", "Le prix final dépend de la panne et des pièces.")
                ),
                Service(
                    "svc-e2",
                    LocalizedText("Panel upgrade", "تحديث لوحة الكهرباء", "Mise à niveau du tableau"),
                    LocalizedText("Replacement and organization of distribution panels.", "استبدال وتنظيم لوحات التوزيع.", "Remplacement et organisation des tableaux de distribution."),
                    PriceMode.NONE,
                    priceDetails = LocalizedText("Price after inspection.", "السعر بعد المعاينة.", "Prix après inspection.")
                )
            ),
            locationLabel = LocalizedText("Hamra, Beirut", "الحمرا، بيروت", "Hamra, Beyrouth"),
            governorate = "Beirut",
            serviceRadiusKm = 18,
            locationMode = LocationMode.APPROXIMATE,
            rating = 4.8,
            reviewCount = 31,
            phone = "+961 3 555 101",
            socialUrl = "https://www.instagram.com/",
            languages = mutableListOf("Arabic", "English", "French")
        ),
        Professional(
            id = "pro-plumber-1",
            publicName = LocalizedText("Bekaa Plumbing", "سباكة البقاع", "Plomberie de la Bekaa"),
            description = LocalizedText(
                "Leak repair, pump installation and bathroom plumbing across central Bekaa.",
                "تصليح تسربات وتركيب مضخات وأعمال سباكة الحمامات في البقاع الأوسط.",
                "Réparation de fuites, installation de pompes et plomberie de salles de bain dans la Bekaa centrale."
            ),
            primaryCategoryId = "plumber",
            services = mutableListOf(
                Service(
                    "svc-p1",
                    LocalizedText("Leak repair", "تصليح تسرب", "Réparation de fuite"),
                    LocalizedText("Diagnosis and repair of visible household leaks.", "تشخيص وتصليح التسربات المنزلية الظاهرة.", "Diagnostic et réparation des fuites domestiques visibles."),
                    PriceMode.RANGE,
                    minAmount = 20.0,
                    maxAmount = 80.0,
                    currency = "USD"
                )
            ),
            locationLabel = LocalizedText("Zahle area", "منطقة زحلة", "Région de Zahlé"),
            governorate = "Bekaa",
            serviceRadiusKm = 35,
            locationMode = LocationMode.CITY_ONLY,
            rating = 4.5,
            reviewCount = 18,
            phone = "+961 71 220 330",
            socialUrl = "https://www.facebook.com/",
            languages = mutableListOf("Arabic", "French")
        ),
        Professional(
            id = "pro-cleaner-1",
            publicName = LocalizedText("Cedars Clean Team", "فريق الأرز للتنظيف", "Équipe Cedars Nettoyage"),
            description = LocalizedText(
                "Home and office cleaning with flexible weekly plans.",
                "تنظيف منازل ومكاتب مع خطط أسبوعية مرنة.",
                "Nettoyage de maisons et bureaux avec formules hebdomadaires flexibles."
            ),
            primaryCategoryId = "cleaner",
            services = mutableListOf(
                Service(
                    "svc-c1",
                    LocalizedText("Home cleaning", "تنظيف منزل", "Nettoyage de maison"),
                    LocalizedText("General cleaning for apartments and houses.", "تنظيف عام للشقق والمنازل.", "Nettoyage général des appartements et maisons."),
                    PriceMode.FIXED,
                    minAmount = 1500000.0,
                    currency = "LBP",
                    priceDetails = LocalizedText("Example price for a small apartment.", "سعر تقريبي لشقة صغيرة.", "Prix indicatif pour un petit appartement.")
                )
            ),
            locationLabel = LocalizedText("Jounieh", "جونية", "Jounieh"),
            governorate = "Keserwan-Jbeil",
            serviceRadiusKm = 20,
            locationMode = LocationMode.EXACT,
            rating = 0.0,
            reviewCount = 0,
            phone = "+961 81 440 550",
            socialUrl = "https://www.instagram.com/",
            languages = mutableListOf("Arabic", "English")
        ),
        Professional(
            id = "pro-mechanic-1",
            publicName = LocalizedText("Tripoli Auto Care", "طرابلس للعناية بالسيارات", "Tripoli Auto Care"),
            description = LocalizedText(
                "General mechanical diagnostics and preventive maintenance.",
                "تشخيص ميكانيكي عام وصيانة وقائية.",
                "Diagnostic mécanique général et entretien préventif."
            ),
            primaryCategoryId = "mechanic",
            services = mutableListOf(
                Service(
                    "svc-m1",
                    LocalizedText("Computer diagnosis", "فحص كمبيوتر", "Diagnostic électronique"),
                    LocalizedText("OBD scan and initial mechanical diagnosis.", "فحص OBD وتشخيص ميكانيكي أولي.", "Scan OBD et premier diagnostic mécanique."),
                    PriceMode.DESCRIPTION_ONLY,
                    priceDetails = LocalizedText("Contact us for a quote after vehicle details are provided.", "تواصل معنا للسعر بعد تزويدنا بتفاصيل السيارة.", "Contactez-nous pour le prix après les détails du véhicule.")
                )
            ),
            locationLabel = LocalizedText("Tripoli", "طرابلس", "Tripoli"),
            governorate = "North",
            serviceRadiusKm = 25,
            locationMode = LocationMode.APPROXIMATE,
            rating = 4.2,
            reviewCount = 9,
            phone = "+961 70 900 112",
            socialUrl = "https://www.facebook.com/",
            languages = mutableListOf("Arabic", "English")
        )
    )

    val posts = mutableListOf(
        Post(
            "post-1",
            "pro-electric-1",
            LocalizedText(
                "A clean distribution panel upgrade completed today in Beirut.",
                "أنهينا اليوم تحديثاً منظماً للوحة توزيع كهربائية في بيروت.",
                "Mise à niveau soignée d'un tableau électrique terminée aujourd'hui à Beyrouth."
            ),
            imageUrl = "dummy-storage://seed/electrical-panel.jpg",
            likes = 22,
            comments = 4,
            publishedLabel = LocalizedText("2 hours ago", "منذ ساعتين", "Il y a 2 heures")
        ),
        Post(
            "post-2",
            "pro-plumber-1",
            LocalizedText(
                "Tip: a small hidden leak can raise pump usage significantly.",
                "نصيحة: التسرب الصغير المخفي قد يرفع استهلاك المضخة بشكل ملحوظ.",
                "Conseil : une petite fuite cachée peut augmenter fortement l'utilisation de la pompe."
            ),
            imageUrl = "dummy-storage://seed/plumbing.jpg",
            likes = 15,
            comments = 2,
            publishedLabel = LocalizedText("Yesterday", "أمس", "Hier")
        ),
        Post(
            "post-3",
            "pro-cleaner-1",
            LocalizedText(
                "Weekly home-cleaning slots are available in Jounieh.",
                "تتوفر مواعيد أسبوعية لتنظيف المنازل في جونية.",
                "Des créneaux hebdomadaires de nettoyage sont disponibles à Jounieh."
            ),
            imageUrl = "dummy-storage://seed/cleaning.jpg",
            likes = 8,
            comments = 1,
            publishedLabel = LocalizedText("3 days ago", "منذ 3 أيام", "Il y a 3 jours")
        )
    )

    val reviews = mutableListOf(
        Review("review-1", "pro-electric-1", "seed-client-1", 5, "Very responsive and professional.", "Maya"),
        Review("review-2", "pro-electric-1", "seed-client-2", 4, "Good work and clear explanation.", "Karim"),
        Review("review-3", "pro-plumber-1", "seed-client-3", 5, "Solved the leak quickly.", "Nadine")
    )

    val notifications = mutableListOf(
        NotificationItem(
            "n-1",
            LocalizedText("Welcome to Khedmati", "أهلاً بك في خدمتي", "Bienvenue sur Khedmati"),
            LocalizedText("This prototype uses simulated notifications only.", "هذا النموذج يستخدم إشعارات تجريبية فقط.", "Ce prototype utilise uniquement des notifications simulées.")
        )
    )

    fun restoreSession(name: String, email: String, role: UserRole) {
        currentUser = DummyUser(
            id = "local-user",
            displayName = name,
            role = role,
            email = email,
            professionalId = if (role == UserRole.PROFESSIONAL) "pro-electric-1" else null
        )
    }

    fun signIn(name: String, email: String, role: UserRole): DummyUser {
        val user = DummyUser(
            id = "local-user",
            displayName = name,
            role = role,
            email = email,
            professionalId = if (role == UserRole.PROFESSIONAL) "pro-electric-1" else null
        )
        currentUser = user
        notifications.add(
            0,
            NotificationItem(
                UUID.randomUUID().toString(),
                LocalizedText("Signed in", "تم تسجيل الدخول", "Connexion effectuée"),
                LocalizedText("Dummy authentication completed locally.", "تم تسجيل الدخول التجريبي محلياً.", "L'authentification fictive a été effectuée localement.")
            )
        )
        return user
    }

    fun signOut() {
        currentUser = null
        savedProfessionalIds.clear()
        likedPostIds.clear()
    }

    fun categoryName(categoryId: String, language: String): String {
        return categories.firstOrNull { it.id == categoryId }?.name?.resolve(language) ?: categoryId
    }

    fun professionalById(id: String): Professional? = professionals.firstOrNull { it.id == id }

    fun postsForProfessional(id: String): List<Post> = posts.filter { it.professionalId == id && it.isActive }

    fun searchProfessionals(
        query: String,
        categoryId: String?,
        governorate: String?,
        minimumRating: Double,
        language: String
    ): List<Professional> {
        val normalized = query.trim().lowercase()
        return professionals
            .filter { it.isActive }
            .filter { categoryId.isNullOrBlank() || it.primaryCategoryId == categoryId || categoryId in it.extraCategoryIds }
            .filter { governorate.isNullOrBlank() || it.governorate.equals(governorate, ignoreCase = true) }
            .filter { it.reviewCount == 0 || it.rating >= minimumRating }
            .filter { pro ->
                if (normalized.isBlank()) true
                else {
                    val haystack = buildString {
                        append(pro.publicName.resolve(language)).append(' ')
                        append(pro.description.resolve(language)).append(' ')
                        append(categoryName(pro.primaryCategoryId, language)).append(' ')
                        pro.services.forEach { append(it.title.resolve(language)).append(' ') }
                    }.lowercase()
                    normalized in haystack
                }
            }
            .sortedWith(compareByDescending<Professional> { it.rating }.thenByDescending { it.reviewCount })
    }

    fun isSaved(professionalId: String): Boolean = professionalId in savedProfessionalIds

    fun toggleSaved(professionalId: String): Boolean {
        return if (professionalId in savedProfessionalIds) {
            savedProfessionalIds.remove(professionalId)
            false
        } else {
            savedProfessionalIds.add(professionalId)
            true
        }
    }

    fun savedProfessionals(): List<Professional> = professionals.filter { it.id in savedProfessionalIds }

    fun isLiked(postId: String): Boolean = postId in likedPostIds

    fun toggleLike(postId: String): Boolean {
        val post = posts.firstOrNull { it.id == postId } ?: return false
        return if (postId in likedPostIds) {
            likedPostIds.remove(postId)
            post.likes = (post.likes - 1).coerceAtLeast(0)
            false
        } else {
            likedPostIds.add(postId)
            post.likes += 1
            true
        }
    }

    fun addComment(postId: String, text: String) {
        val post = posts.firstOrNull { it.id == postId } ?: return
        if (text.isBlank()) return
        post.comments += 1
        notifications.add(
            0,
            NotificationItem(
                UUID.randomUUID().toString(),
                LocalizedText("Comment added", "تمت إضافة التعليق", "Commentaire ajouté"),
                LocalizedText("Your comment was saved in the dummy repository.", "تم حفظ تعليقك في المستودع التجريبي.", "Votre commentaire a été enregistré dans le dépôt fictif.")
            )
        )
    }

    fun reviewsForProfessional(professionalId: String): List<Review> = reviews.filter { it.professionalId == professionalId }

    fun addOrUpdateReview(professionalId: String, rating: Int, text: String) {
        val user = currentUser ?: return
        if (user.role != UserRole.CLIENT || rating !in 1..5 || text.isBlank()) return
        val existing = reviews.firstOrNull { it.professionalId == professionalId && it.clientId == user.id }
        if (existing == null) {
            reviews.add(
                Review(
                    id = UUID.randomUUID().toString(),
                    professionalId = professionalId,
                    clientId = user.id,
                    rating = rating,
                    text = text.trim(),
                    authorName = user.displayName
                )
            )
        } else {
            existing.rating = rating
            existing.text = text.trim()
        }
        recomputeRating(professionalId)
    }

    private fun recomputeRating(professionalId: String) {
        val professional = professionalById(professionalId) ?: return
        val related = reviewsForProfessional(professionalId)
        professional.reviewCount = related.size
        professional.rating = if (related.isEmpty()) 0.0 else related.map { it.rating }.average()
    }

    fun updateProfessional(
        id: String,
        name: String,
        description: String,
        phone: String,
        radiusKm: Int,
        locationMode: LocationMode,
        profileImageUrl: String?
    ) {
        val professional = professionalById(id) ?: return
        professional.publicName = LocalizedText(name, name, name)
        professional.description = LocalizedText(description, description, description)
        professional.phone = phone
        professional.serviceRadiusKm = radiusKm
        professional.locationMode = locationMode
        if (profileImageUrl != null) professional.profileImageUrl = profileImageUrl
    }

    fun addService(professionalId: String, title: String, amount: Double?, currency: String?) {
        val professional = professionalById(professionalId) ?: return
        professional.services.add(
            Service(
                id = UUID.randomUUID().toString(),
                title = LocalizedText(title, title, title),
                description = LocalizedText("Added from the local prototype.", "تمت الإضافة من النموذج المحلي.", "Ajouté depuis le prototype local."),
                priceMode = if (amount == null) PriceMode.NONE else PriceMode.FIXED,
                minAmount = amount,
                currency = currency
            )
        )
    }

    fun createPost(professionalId: String, text: String, imageUrl: String?) {
        if (text.isBlank()) return
        posts.add(
            0,
            Post(
                id = UUID.randomUUID().toString(),
                professionalId = professionalId,
                text = LocalizedText(text, text, text),
                imageUrl = imageUrl,
                publishedLabel = LocalizedText("Just now", "الآن", "À l'instant")
            )
        )
    }
}
