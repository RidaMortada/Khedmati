package com.example.khedmati.model

enum class UserRole { CLIENT, PROFESSIONAL }
enum class PriceMode { NONE, FIXED, STARTING_FROM, RANGE, DESCRIPTION_ONLY }
enum class LocationMode { EXACT, APPROXIMATE, CITY_ONLY }

data class LocalizedText(
    val en: String,
    val ar: String,
    val fr: String
) {
    fun resolve(language: String): String = when (language.lowercase()) {
        "ar" -> ar
        "fr" -> fr
        else -> en
    }
}

data class Category(
    val id: String,
    val name: LocalizedText,
    val icon: String
)

data class Service(
    val id: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val priceMode: PriceMode,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val currency: String? = null,
    val priceDetails: LocalizedText? = null
)

data class Professional(
    val id: String,
    var publicName: LocalizedText,
    var description: LocalizedText,
    val primaryCategoryId: String,
    val extraCategoryIds: MutableList<String> = mutableListOf(),
    val services: MutableList<Service> = mutableListOf(),
    var locationLabel: LocalizedText,
    var governorate: String,
    var serviceRadiusKm: Int,
    var locationMode: LocationMode,
    var rating: Double,
    var reviewCount: Int,
    var phone: String,
    var socialUrl: String,
    var profileImageUrl: String? = null,
    var coverImageUrl: String? = null,
    val languages: MutableList<String> = mutableListOf(),
    var isActive: Boolean = true
)

data class Post(
    val id: String,
    val professionalId: String,
    var text: LocalizedText,
    var imageUrl: String? = null,
    var likes: Int = 0,
    var comments: Int = 0,
    val publishedLabel: LocalizedText,
    var isActive: Boolean = true
)

data class Review(
    val id: String,
    val professionalId: String,
    val clientId: String,
    var rating: Int,
    var text: String,
    val authorName: String
)

data class NotificationItem(
    val id: String,
    val title: LocalizedText,
    val body: LocalizedText,
    var isRead: Boolean = false
)

data class DummyUser(
    val id: String,
    var displayName: String,
    val role: UserRole,
    val email: String,
    val professionalId: String? = null
)
