package com.example.khedmati.data

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
    fun registeredUserReviewIsUpdatedInsteadOfDuplicated() {
        val user = DummyCloudRepository.signIn("Test User", "user@test.local")
        val professionalId = "pro-plumber-1"
        DummyCloudRepository.addOrUpdateReview(professionalId, 4, "First review")
        val countAfterFirst = DummyCloudRepository.reviewsForProfessional(professionalId)
            .count { it.clientId == user.id }

        DummyCloudRepository.addOrUpdateReview(professionalId, 5, "Updated review")
        val userReviews = DummyCloudRepository.reviewsForProfessional(professionalId)
            .filter { it.clientId == user.id }

        assertEquals(1, countAfterFirst)
        assertEquals(1, userReviews.size)
        assertEquals(5, userReviews.single().rating)
        DummyCloudRepository.signOut()
    }

    @Test
    fun sameRegisteredUserCanPublishWorkPostWithLocalImageUri() {
        val user = DummyCloudRepository.signIn("Worker", "worker@test.local")
        DummyCloudRepository.createPost(
            user.professionalId,
            "Finished work",
            "content://local/work-photo.jpg"
        )
        val post = DummyCloudRepository.posts.first()
        assertEquals(user.professionalId, post.professionalId)
        assertEquals("content://local/work-photo.jpg", post.imageUrl)
        DummyCloudRepository.signOut()
    }

    @Test
    fun dummyStorageNeverReturnsHttpUrl() {
        val url = DummyStorageService.uploadImage("test")
        assertTrue(url.startsWith("dummy-storage://"))
    }
}
