package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JummoRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createEvent_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = JummoRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createEvent(
                name = "Grand Royal Wedding",
                category = "Wedding",
                eventDate = "2026-12-10",
                location = "Taj Palace, New Delhi"
            )
        }
        assertTrue(createResult.isSuccess)
        val eventId = createResult.getOrThrow()
        assertTrue(eventId.isNotEmpty())
    }

    @Test
    fun getUserEvents_authenticatedOwner_returnsMatchingEvents() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = JummoRepository(firestore)
        val eventId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createEvent(
                name = "Spring Music Festival",
                category = "Festival",
                eventDate = "2026-11-30",
                location = "Bandra Amphitheatre"
            ).getOrThrow()
        }

        val eventsResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getUserEvents() }
        assertTrue(eventsResult.isSuccess)
        assertTrue(eventsResult.getOrThrow().map { it.id }.contains(eventId))
    }

    @Test
    fun observeEvents_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = JummoRepository(firestore)
        val eventId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createEvent(
                name = "Annual Corporate Summit",
                category = "Corporate",
                eventDate = "2026-12-01",
                location = "Convention Center"
            ).getOrThrow()
        }

        val emittedEvents = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeEvents(uid).first { list -> list.any { it.id == eventId } }
        }
        assertTrue(emittedEvents.map { it.id }.contains(eventId))
    }

    @Test
    fun getEventById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = JummoRepository(firestore)
        val eventId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createEvent(
                name = "Private Birthday Soiree",
                category = "Birthday",
                eventDate = "2026-11-28",
                location = "Sky Lounge"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = JummoRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getEventById(eventId) }
        assertTrue(result.isFailure)
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeEvents_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = JummoRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeEvents("unauthenticated_uid").first()
            }
        } catch (e: Throwable) {
            caught = e
        }
        assertNotNull(caught)
        val firestoreException = checkNotNull(caught?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $caught"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    @Test
    fun createAndManageHomeBanners_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = JummoRepository(firestore)

        val banner1Id = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createHomeBanner(
                imageUri = "preset://wedding_cover",
                storagePath = "banners/b1.jpg",
                displayOrder = 0L,
                isActive = true,
                slideDurationSeconds = 4L
            ).getOrThrow()
        }
        val banner2Id = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createHomeBanner(
                imageUri = "preset://festival_cover",
                storagePath = "banners/b2.jpg",
                displayOrder = 1L,
                isActive = true,
                slideDurationSeconds = 4L
            ).getOrThrow()
        }

        val emittedBanners = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeHomeBanners(uid).first { it.size == 2 }
        }
        assertEquals(listOf(banner1Id, banner2Id), emittedBanners.map { it.id })

        // Reorder and update slide duration
        val reordered = listOf(emittedBanners[1], emittedBanners[0])
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { repository.reorderHomeBanners(reordered) }.isSuccess)
        assertTrue(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.updateAllBannersSlideDuration(reordered, 6L)
            }.isSuccess
        )
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { repository.deleteHomeBanner(banner1Id) }.isSuccess)
    }

    @Test
    fun saveDraftPublishAndRollbackWebsiteCustomization_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = JummoRepository(firestore)

        val eventId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createEvent(
                name = "Rahul weds Pooja",
                category = "Wedding",
                eventDate = "15 Dec 2026",
                location = "Udaipur"
            ).getOrThrow()
        }

        val initialDraft = com.example.data.model.WebsiteCustomization(
            id = "web_$eventId",
            userId = uid,
            eventId = eventId,
            eventName = "Rahul weds Pooja",
            studioName = "Jummo Pro Studio",
            photographerName = "Meraj Sir",
            theme = "RoyalPurple",
            layout = "Editorial"
        )

        val draftId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveWebsiteDraft(initialDraft).getOrThrow()
        }
        assertEquals("web_$eventId", draftId)

        // Publish v1
        val v1Snapshot = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishWebsiteCustomization(initialDraft).getOrThrow()
        }
        assertEquals(1L, v1Snapshot.versionNumber)

        // Customize and publish v2
        val updatedDraft = initialDraft.copy(
            id = draftId,
            theme = "MidnightBlack",
            layout = "Masonry",
            publishedVersion = 1L
        )
        val v2Snapshot = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishWebsiteCustomization(updatedDraft).getOrThrow()
        }
        assertEquals(2L, v2Snapshot.versionNumber)
        assertEquals("Masonry", v2Snapshot.layout)

        // Rollback to v1
        val rollbackRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.rollbackWebsiteVersion(updatedDraft.copy(publishedVersion = 2L), v1Snapshot)
        }
        assertTrue(rollbackRes.isSuccess)

        val emittedCustomizations = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeWebsiteCustomizations(uid).first { list ->
                list.any { it.id == draftId && it.publishedVersion == 1L && it.layout == "Editorial" }
            }
        }
        assertTrue(emittedCustomizations.any { it.id == draftId && it.theme == "RoyalPurple" })
    }

    private companion object {
        const val ALICE_EMAIL = "alice.photographer@test.com"
        const val BOB_EMAIL = "bob.photographer@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}

