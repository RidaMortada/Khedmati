package com.example.khedmati.data

import com.example.khedmati.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyCloudRepositoryTest {

    @Test
    fun categorySearchReturnsOnlyMatchingProfessionals() {
        val results = DummyCloudRepository.searchProfessionals(
            query = "",
            categoryId = "plumber",
            governorate = null,
            minimumRating = 0.0,
            language = "en"
        )
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.primaryCategoryId == "plumber" || "plumber" in it.extraCategoryIds })
    }

    @Test
    fun clientReviewIsUpdatedInsteadOfDuplicated() {
        DummyCloudRepository.signIn("Test Client", "client@test.local", UserRole.CLIENT)
        val professionalId = "pro-plumber-1"
        DummyCloudRepository.addOrUpdateReview(professionalId, 4, "First review")
        val countAfterFirst = DummyCloudRepository.reviewsForProfessional(professionalId)
            .count { it.clientId == "local-user" }

        DummyCloudRepository.addOrUpdateReview(professionalId, 5, "Updated review")
        val clientReviews = DummyCloudRepository.reviewsForProfessional(professionalId)
            .filter { it.clientId == "local-user" }

        assertEquals(1, countAfterFirst)
        assertEquals(1, clientReviews.size)
        assertEquals(5, clientReviews.single().rating)
        DummyCloudRepository.signOut()
    }

    @Test
    fun dummyStorageNeverReturnsHttpUrl() {
        val url = DummyStorageService.uploadImage("test")
        assertTrue(url.startsWith("dummy-storage://"))
    }
}
