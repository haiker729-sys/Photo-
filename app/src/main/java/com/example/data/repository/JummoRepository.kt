package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BannerSliderConfig
import com.example.data.model.BannerVisibilityModes
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.HomeBanner
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.data.model.StorageDestinations
import com.example.data.model.WebsiteCustomization
import com.example.data.model.WebsiteVersionSnapshot
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class JummoRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Photographer must be signed in with Google before accessing Firestore.")
    }

    // --- Photographer Profile ---

    fun observeProfile(userId: String): Flow<PhotographerProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            PhotographerProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureOrSaveProfile(
        displayName: String,
        email: String,
        studioName: String,
        driveConnected: Boolean,
        driveFolderName: String,
        autoUploadEnabled: Boolean,
        wifiOnlyUpload: Boolean,
        batteryNotLowUpload: Boolean
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            val safeName = displayName.trim().ifBlank { "Lead Photographer" }.take(100)
            val safeEmail = email.trim().take(150)
            val safeStudio = studioName.trim().ifBlank { "Jummo Pro Studio" }.take(100)
            val safeFolder = driveFolderName.trim().ifBlank { "Jummo_Events_Cloud" }.take(120)

            if (!existingSnap.exists()) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "studioName" to safeStudio,
                    "role" to "photographer",
                    "driveConnected" to driveConnected,
                    "driveFolderName" to safeFolder,
                    "autoUploadEnabled" to autoUploadEnabled,
                    "wifiOnlyUpload" to wifiOnlyUpload,
                    "batteryNotLowUpload" to batteryNotLowUpload,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mapOf(
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "studioName" to safeStudio,
                    "driveConnected" to driveConnected,
                    "driveFolderName" to safeFolder,
                    "autoUploadEnabled" to autoUploadEnabled,
                    "wifiOnlyUpload" to wifiOnlyUpload,
                    "batteryNotLowUpload" to batteryNotLowUpload,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- Events ---

    fun observeEvents(userId: String = requireUserId()): Flow<List<PhotoEvent>> = flow {
        val path = "events"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PhotoEvent::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getUserEvents(): Result<List<PhotoEvent>> = runCatching {
        val uid = requireUserId()
        val path = "events"
        try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", uid)
                .get()
                .await()
            snapshot.toObjects(
                PhotoEvent::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun getEventById(eventId: String): Result<PhotoEvent?> = runCatching {
        val docRef = db.collection("events").document(eventId)
        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                throw FirebaseFirestoreException(
                    "Event not found or access denied",
                    FirebaseFirestoreException.Code.PERMISSION_DENIED
                )
            }
            snap.toObject(
                PhotoEvent::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createEvent(
        name: String,
        category: String,
        eventDate: String,
        location: String,
        coverType: String = "wedding",
        coverUri: String = "",
        guestAccessMode: String = "FaceSearchOnly",
        guestAllowDownload: Boolean = true,
        qrExpirationDate: String = "Never",
        description: String = "",
        storageDestination: String = StorageDestinations.BOTH,
        guestPin: String = "",
        phoneFolderPath: String = "",
        driveFolderId: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val eventId = "evt_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("events").document(eventId)
        val qrUrl = "https://jummophotos.ai/guest/$eventId"

        val payload = mapOf(
            "userId" to uid,
            "name" to name.trim().ifBlank { "Untitled Event" }.take(120),
            "category" to category,
            "eventDate" to eventDate.trim().ifBlank { "2026-11-24" }.take(40),
            "location" to location.trim().ifBlank { "Main Venue" }.take(160),
            "coverType" to coverType.ifBlank { "wedding" }.take(60),
            "coverUri" to coverUri.take(500),
            "status" to "Active",
            "qrCodeValue" to qrUrl,
            "qrEnabled" to true,
            "qrExpirationDate" to qrExpirationDate.ifBlank { "Never" }.take(40),
            "guestAccessMode" to guestAccessMode,
            "guestAllowDownload" to guestAllowDownload,
            "totalPhotos" to 0L,
            "uploadedPhotos" to 0L,
            "pendingPhotos" to 0L,
            "failedPhotos" to 0L,
            "driveSyncStatus" to if (storageDestination == StorageDestinations.PHONE_FOLDER) "NotConnected" else "Synced",
            "description" to description.trim().take(1000),
            "storageDestination" to storageDestination,
            "guestPin" to guestPin.trim().take(30),
            "phoneFolderPath" to phoneFolderPath.trim().take(500),
            "driveFolderId" to driveFolderId.trim().take(200),
            "downloadEnabled" to guestAllowDownload,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            eventId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateEvent(event: PhotoEvent): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(event.id.isNotBlank()) { "Event ID must not be blank for update" }
        val docRef = db.collection("events").document(event.id)
        try {
            val updatePayload = mapOf(
                "name" to event.name.trim().ifBlank { "Untitled Event" }.take(120),
                "category" to event.category,
                "eventDate" to event.eventDate.trim().ifBlank { "2026-11-24" }.take(40),
                "location" to event.location.trim().ifBlank { "Main Venue" }.take(160),
                "coverType" to event.coverType.ifBlank { "wedding" }.take(60),
                "coverUri" to event.coverUri.take(500),
                "status" to event.status,
                "qrCodeValue" to event.qrCodeValue.take(250),
                "qrEnabled" to event.qrEnabled,
                "qrExpirationDate" to event.qrExpirationDate.take(40),
                "guestAccessMode" to event.guestAccessMode,
                "guestAllowDownload" to event.guestAllowDownload,
                "totalPhotos" to event.totalPhotos,
                "uploadedPhotos" to event.uploadedPhotos,
                "pendingPhotos" to event.pendingPhotos,
                "failedPhotos" to event.failedPhotos,
                "driveSyncStatus" to event.driveSyncStatus,
                "description" to event.description.trim().take(1000),
                "storageDestination" to event.storageDestination,
                "guestPin" to event.guestPin.trim().take(30),
                "phoneFolderPath" to event.phoneFolderPath.trim().take(500),
                "driveFolderId" to event.driveFolderId.trim().take(200),
                "downloadEnabled" to event.downloadEnabled,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updatePayload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("events").document(eventId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Uploads ---

    fun observeUploads(userId: String = requireUserId()): Flow<List<PhotoUploadRecord>> = flow {
        val path = "uploads"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PhotoUploadRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun enqueuePhotoUpload(
        event: PhotoEvent,
        fileName: String,
        localUri: String,
        sourceType: String,
        uploadMode: String,
        fileSizeBytes: Long,
        mimeType: String = "image/jpeg",
        autoCompleteImmediately: Boolean = false
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val cleanFileName = fileName.trim().ifBlank { "IMG_${System.currentTimeMillis()}.JPG" }.take(200)
        val safeBytes = fileSizeBytes.coerceAtLeast(0L)

        // Duplicate upload prevention: check if the exact file for this event is already queued or uploaded
        val existingSnap = db.collection("uploads")
            .whereEqualTo("userId", uid)
            .get()
            .await()
        val duplicateRecord = existingSnap.toObjects(
            PhotoUploadRecord::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).firstOrNull { existing ->
            existing.eventId == event.id &&
                existing.fileName.equals(cleanFileName, ignoreCase = true) &&
                existing.fileSizeBytes == safeBytes &&
                existing.status != "Failed"
        }
        if (duplicateRecord != null) {
            return@runCatching duplicateRecord.id
        }

        val uploadId = "upl_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("uploads").document(uploadId)
        val initialStatus = if (autoCompleteImmediately) "Uploaded" else "Pending"
        val initialProgress = if (autoCompleteImmediately) 100L else 0L
        val faceStatus = if (autoCompleteImmediately) "Indexed" else "AwaitingBackend"

        val payload = mapOf(
            "userId" to uid,
            "eventId" to event.id,
            "eventName" to event.name.take(120),
            "fileName" to cleanFileName,
            "localUri" to localUri.take(500),
            "sourceType" to sourceType,
            "uploadMode" to uploadMode,
            "status" to initialStatus,
            "progress" to initialProgress,
            "fileSizeBytes" to safeBytes,
            "mimeType" to mimeType.ifBlank { "image/jpeg" }.take(60),
            "faceIndexStatus" to faceStatus,
            "driveFileId" to if (autoCompleteImmediately && event.driveSyncStatus == "Synced") "drv_${uploadId.takeLast(8)}" else "",
            "errorMessage" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            val eventRef = db.collection("events").document(event.id)
            val eventUpdates = if (autoCompleteImmediately) {
                mapOf(
                    "totalPhotos" to (event.totalPhotos + 1L),
                    "uploadedPhotos" to (event.uploadedPhotos + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            } else {
                mapOf(
                    "totalPhotos" to (event.totalPhotos + 1L),
                    "pendingPhotos" to (event.pendingPhotos + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            }
            eventRef.update(eventUpdates).await()
            uploadId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateUploadStatus(
        upload: PhotoUploadRecord,
        newStatus: String,
        newProgress: Long,
        errorMessage: String = ""
    ): Result<Unit> = runCatching {
        requireUserId()
        require(upload.id.isNotBlank()) { "Upload ID must not be blank" }
        val docRef = db.collection("uploads").document(upload.id)
        try {
            val faceStatus = if (newStatus == "Uploaded") "Indexed" else upload.faceIndexStatus
            val driveId = if (newStatus == "Uploaded") "drv_${upload.id.takeLast(8)}" else upload.driveFileId
            val payload = mapOf(
                "status" to newStatus,
                "progress" to newProgress.coerceIn(0L, 100L),
                "faceIndexStatus" to faceStatus,
                "driveFileId" to driveId,
                "errorMessage" to errorMessage.take(300),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun pauseUpload(upload: PhotoUploadRecord): Result<Unit> = runCatching {
        requireUserId()
        require(upload.id.isNotBlank()) { "Upload ID must not be blank" }
        val docRef = db.collection("uploads").document(upload.id)
        try {
            docRef.update(
                mapOf(
                    "status" to "Paused",
                    "progress" to upload.progress.coerceIn(0L, 99L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun resumeUpload(upload: PhotoUploadRecord): Result<Unit> = runCatching {
        requireUserId()
        require(upload.id.isNotBlank()) { "Upload ID must not be blank" }
        val docRef = db.collection("uploads").document(upload.id)
        try {
            docRef.update(
                mapOf(
                    "status" to "Pending",
                    "errorMessage" to "",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun reassignUploadEvent(
        upload: PhotoUploadRecord,
        targetEvent: PhotoEvent
    ): Result<Unit> = runCatching {
        requireUserId()
        require(upload.id.isNotBlank()) { "Upload ID must not be blank" }
        require(targetEvent.id.isNotBlank()) { "Target event ID must not be blank" }
        val oldEventId = upload.eventId
        val docRef = db.collection("uploads").document(upload.id)
        try {
            docRef.update(
                mapOf(
                    "eventId" to targetEvent.id,
                    "eventName" to targetEvent.name.take(120),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (oldEventId.isNotBlank() && oldEventId != targetEvent.id) {
                recalculateEventCounters(oldEventId)
            }
            recalculateEventCounters(targetEvent.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun recalculateEventCounters(eventId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val uploadsSnap = db.collection("uploads")
            .whereEqualTo("userId", uid)
            .get()
            .await()
        val eventUploads = uploadsSnap.toObjects(
            PhotoUploadRecord::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).filter { it.eventId == eventId }

        val total = eventUploads.size.toLong()
        val uploaded = eventUploads.count { it.status == "Uploaded" }.toLong()
        val pending = eventUploads.count {
            it.status == "Pending" || it.status == "Uploading" || it.status == "Paused"
        }.toLong()
        val failed = eventUploads.count { it.status == "Failed" }.toLong()

        val eventRef = db.collection("events").document(eventId)
        try {
            eventRef.update(
                mapOf(
                    "totalPhotos" to total,
                    "uploadedPhotos" to uploaded,
                    "pendingPhotos" to pending,
                    "failedPhotos" to failed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, eventRef.path)
            throw e
        }
    }

    suspend fun deleteUploadRecord(upload: PhotoUploadRecord): Result<Unit> = runCatching {
        requireUserId()
        require(upload.id.isNotBlank()) { "Upload ID must not be blank" }
        val docRef = db.collection("uploads").document(upload.id)
        try {
            docRef.delete().await()
            if (upload.eventId.isNotBlank()) {
                recalculateEventCounters(upload.eventId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Guest Consent & Face Search Sessions ---

    fun observeGuestConsents(userId: String = requireUserId()): Flow<List<GuestFaceSearchSession>> = flow {
        val path = "guest_consents"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GuestFaceSearchSession::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createGuestConsentSession(
        eventId: String,
        guestLabel: String,
        selfieUri: String,
        matchedPhotoCount: Long
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val consentId = "gfc_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("guest_consents").document(consentId)
        val payload = mapOf(
            "userId" to uid,
            "eventId" to eventId,
            "guestLabel" to guestLabel.trim().ifBlank { "Event Guest" }.take(100),
            "consentGranted" to true,
            "selfieUri" to selfieUri.take(500),
            "matchStatus" to "ConsentVerified",
            "matchedPhotoCount" to matchedPhotoCount.coerceAtLeast(0L),
            "reportNotes" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            consentId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteGuestFaceData(session: GuestFaceSearchSession): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("guest_consents").document(session.id)
        try {
            docRef.update(
                mapOf(
                    "selfieUri" to "",
                    "matchStatus" to "DataDeleted",
                    "matchedPhotoCount" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun reportIncorrectMatch(
        session: GuestFaceSearchSession,
        notes: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("guest_consents").document(session.id)
        try {
            docRef.update(
                mapOf(
                    "matchStatus" to "ReportedMismatch",
                    "reportNotes" to notes.trim().ifBlank { "Guest flagged an incorrect photo match" }.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Home Dashboard Image-Only Banners (Admin Managed) ---

    fun observeHomeBanners(userId: String = requireUserId()): Flow<List<HomeBanner>> = flow {
        val path = "home_banners"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        HomeBanner::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareBy<HomeBanner> { it.displayOrder }
                            .thenBy { it.createdAt ?: Timestamp(0, 0) }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getUserHomeBanners(): Result<List<HomeBanner>> = runCatching {
        val uid = requireUserId()
        val path = "home_banners"
        try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", uid)
                .get()
                .await()
            snapshot.toObjects(
                HomeBanner::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ).sortedBy { it.displayOrder }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun createHomeBanner(
        imageUri: String,
        storagePath: String,
        displayOrder: Long,
        isActive: Boolean = true,
        slideDurationSeconds: Long = 5L,
        title: String = "",
        visibility: String = BannerVisibilityModes.ALL,
        targetPhotographerIds: List<String> = emptyList(),
        targetPlanIds: List<String> = emptyList(),
        startAt: Timestamp? = null,
        endAt: Timestamp? = null,
        aspectRatio: String = "16:9",
        cropMode: String = "CenterCrop"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val bannerId = "bnr_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("home_banners").document(bannerId)
        val payload = mutableMapOf<String, Any?>(
            "userId" to uid,
            "imageUri" to imageUri.trim().take(500),
            "storagePath" to storagePath.trim().take(300),
            "displayOrder" to displayOrder.coerceIn(0L, 1000L),
            "isActive" to isActive,
            "slideDurationSeconds" to slideDurationSeconds.coerceIn(1L, 30L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (title.isNotBlank()) payload["title"] = title.trim().take(120)
        if (visibility.isNotBlank()) payload["visibility"] = visibility
        if (targetPhotographerIds.isNotEmpty()) payload["targetPhotographerIds"] = targetPhotographerIds.take(50)
        if (targetPlanIds.isNotEmpty()) payload["targetPlanIds"] = targetPlanIds.take(20)
        if (startAt != null) payload["startAt"] = startAt
        if (endAt != null) payload["endAt"] = endAt
        if (aspectRatio.isNotBlank()) payload["aspectRatio"] = aspectRatio
        if (cropMode.isNotBlank()) payload["cropMode"] = cropMode

        try {
            docRef.set(payload).await()
            bannerId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateHomeBanner(banner: HomeBanner): Result<Unit> = runCatching {
        requireUserId()
        require(banner.id.isNotBlank()) { "Banner ID must not be blank" }
        val docRef = db.collection("home_banners").document(banner.id)
        val payload = mutableMapOf<String, Any?>(
            "imageUri" to banner.imageUri.trim().take(500),
            "storagePath" to banner.storagePath.trim().take(300),
            "displayOrder" to banner.displayOrder.coerceIn(0L, 1000L),
            "isActive" to banner.isActive,
            "slideDurationSeconds" to banner.slideDurationSeconds.coerceIn(1L, 30L),
            "updatedAt" to FieldValue.serverTimestamp(),
            "title" to banner.title.trim().take(120),
            "visibility" to banner.visibility,
            "targetPhotographerIds" to banner.targetPhotographerIds.take(50),
            "targetPlanIds" to banner.targetPlanIds.take(20),
            "startAt" to banner.startAt,
            "endAt" to banner.endAt,
            "aspectRatio" to banner.aspectRatio,
            "cropMode" to banner.cropMode
        )
        try {
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun reorderHomeBanners(orderedBanners: List<HomeBanner>): Result<Unit> = runCatching {
        requireUserId()
        val batch = db.batch()
        for ((index, banner) in orderedBanners.withIndex()) {
            if (banner.id.isBlank()) continue
            val docRef = db.collection("home_banners").document(banner.id)
            batch.update(
                docRef,
                mapOf(
                    "displayOrder" to index.toLong().coerceIn(0L, 1000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "home_banners_batch_reorder")
            throw e
        }
    }

    suspend fun updateAllBannersSlideDuration(
        banners: List<HomeBanner>,
        slideDurationSeconds: Long
    ): Result<Unit> = runCatching {
        requireUserId()
        val safeDuration = slideDurationSeconds.coerceIn(1L, 30L)
        val batch = db.batch()
        for (banner in banners) {
            if (banner.id.isBlank()) continue
            val docRef = db.collection("home_banners").document(banner.id)
            batch.update(
                docRef,
                mapOf(
                    "slideDurationSeconds" to safeDuration,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "home_banners_duration_update")
            throw e
        }
    }

    suspend fun deleteHomeBanner(bannerId: String, storagePath: String = ""): Result<Unit> = runCatching {
        requireUserId()
        require(bannerId.isNotBlank()) { "Banner ID must not be blank" }
        val docRef = db.collection("home_banners").document(bannerId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeSliderConfig(): Flow<BannerSliderConfig> = flow {
        val path = "appConfig/bannerSlider"
        emitAll(
            db.document(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(BannerSliderConfig::class.java) ?: BannerSliderConfig()
                }
                .catch {
                    emit(BannerSliderConfig())
                }
        )
    }

    suspend fun saveSliderConfig(config: BannerSliderConfig): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("appConfig").document("bannerSlider")
        val payload = mapOf(
            "autoSlideEnabled" to config.autoSlideEnabled,
            "slideDurationSeconds" to config.slideDurationSeconds.coerceIn(1L, 30L),
            "infiniteLoop" to config.infiniteLoop,
            "transitionType" to config.transitionType,
            "defaultVisibility" to config.defaultVisibility,
            "bannerTextOverlay" to false,
            "updatedAt" to FieldValue.serverTimestamp(),
            "updatedBy" to uid
        )
        docRef.set(payload).await()
    }

    // --- Photographer Website Customizer & Versioned Publishing ---

    fun observeWebsiteCustomizations(userId: String = requireUserId()): Flow<List<WebsiteCustomization>> = flow {
        val path = "website_customizations"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        WebsiteCustomization::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeWebsiteVersions(userId: String = requireUserId()): Flow<List<WebsiteVersionSnapshot>> = flow {
        val path = "website_versions"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        WebsiteVersionSnapshot::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.versionNumber }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    private fun buildWebsiteCustomizationUpdateMap(
        draft: WebsiteCustomization,
        publicationStatus: String,
        publishedVersion: Long,
        publishedUrl: String,
        hasUnpublishedChanges: Boolean
    ): Map<String, Any> {
        val safeEventId = draft.eventId.trim().ifBlank { "studio_default" }.take(128)
        val resolvedTheme = draft.resolvedTheme().ifBlank { "RoyalPurple" }
        val resolvedLayout = draft.resolvedLayout().ifBlank { "Editorial" }
        val resolvedFont = draft.resolvedFont().ifBlank { "PlayfairDisplay" }
        val csv = draft.bannerUrisList().joinToString("|").take(2000)

        return mapOf(
            "eventName" to draft.eventName.trim().ifBlank { "Studio Showcase" }.take(120),
            "brandingEnabled" to draft.brandingEnabled,
            "studioName" to draft.studioName.trim().ifBlank { "Jummo Pro Studio" }.take(100),
            "photographerName" to draft.photographerName.trim().ifBlank { "Meraj Sir" }.take(100),
            "logoUri" to draft.logoUri.trim().take(500),
            "profileImageUri" to draft.profileImageUri.trim().take(500),
            "coverImageUri" to draft.coverImageUri.trim().take(500),
            "bannerUrisCsv" to csv,
            "autoSliderEnabled" to draft.autoSliderEnabled,
            "sliderIntervalSeconds" to draft.sliderIntervalSeconds.coerceIn(1L, 30L),
            "bannerHeightMode" to draft.bannerHeightMode.ifBlank { "Standard" },
            "bannerCropMode" to draft.bannerCropMode.ifBlank { "CenterCrop" },
            "theme" to resolvedTheme,
            "primaryAccentHex" to draft.primaryAccentHex.trim().ifBlank { "#8B20E8" }.take(12),
            "secondaryAccentHex" to draft.secondaryAccentHex.trim().ifBlank { "#F7C747" }.take(12),
            "fontFamily" to resolvedFont.take(60),
            "layout" to resolvedLayout,
            "welcomeMessage" to draft.welcomeMessage.trim().take(500),
            "contactEmail" to draft.contactEmail.trim().take(150),
            "contactPhone" to draft.contactPhone.trim().take(60),
            "studioLocation" to draft.studioLocation.trim().take(160),
            "instagramUrl" to draft.instagramUrl.trim().take(250),
            "portfolioUrl" to draft.portfolioUrl.trim().take(250),
            "faceSearchEnabled" to draft.faceSearchEnabled,
            "galleryEnabled" to draft.galleryEnabled,
            "downloadsEnabled" to draft.downloadsEnabled,
            "shareEnabled" to draft.shareEnabled,
            "guestAccessMode" to draft.guestAccessMode.ifBlank { "FaceSearchOnly" },
            "publicationStatus" to publicationStatus,
            "publishedVersion" to publishedVersion.coerceAtLeast(0L),
            "publishedUrl" to publishedUrl.ifBlank {
                "https://haiker729-sys.github.io/Photo-/?event=$safeEventId"
            }.take(300),
            "hasUnpublishedChanges" to hasUnpublishedChanges,
            "adminAuditNote" to draft.adminAuditNote.trim().take(300),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    suspend fun saveWebsiteDraft(draft: WebsiteCustomization): Result<String> = runCatching {
        val uid = requireUserId()
        val safeEventId = draft.eventId.trim().ifBlank { "studio_default" }.take(120)
        val docId = draft.id.trim().ifBlank { "web_$safeEventId" }
        val docRef = db.collection("website_customizations").document(docId)
        val status = if (draft.publishedVersion > 0L && draft.publicationStatus == "Published") {
            "Published"
        } else {
            "Draft"
        }
        val url = draft.publishedUrl.ifBlank {
            "https://haiker729-sys.github.io/Photo-/?event=$safeEventId"
        }
        val baseUpdate = buildWebsiteCustomizationUpdateMap(
            draft = draft,
            publicationStatus = status,
            publishedVersion = draft.publishedVersion.coerceAtLeast(0L),
            publishedUrl = url,
            hasUnpublishedChanges = true
        )
        try {
            val existingDocs = db.collection("website_customizations")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val existingSnap = existingDocs.documents.find { it.id == docId }
            if (existingSnap == null) {
                val createPayload = baseUpdate + mapOf(
                    "userId" to uid,
                    "eventId" to safeEventId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                docRef.update(baseUpdate).await()
            }
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun publishWebsiteCustomization(
        draft: WebsiteCustomization
    ): Result<WebsiteVersionSnapshot> = runCatching {
        val uid = requireUserId()
        val safeEventId = draft.eventId.trim().ifBlank { "studio_default" }.take(120)
        val docId = draft.id.trim().ifBlank { "web_$safeEventId" }
        val docRef = db.collection("website_customizations").document(docId)
        val publishedUrl = "https://haiker729-sys.github.io/Photo-/?event=$safeEventId"

        try {
            val existingDocs = db.collection("website_customizations")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val existingSnap = existingDocs.documents.find { it.id == docId }
            val previousVersion = if (existingSnap != null) {
                (existingSnap.getLong("publishedVersion") ?: draft.publishedVersion).coerceAtLeast(0L)
            } else {
                0L
            }
            val nextVersion = (maxOf(previousVersion, draft.publishedVersion) + 1L).coerceAtMost(10000L)

            val updateMap = buildWebsiteCustomizationUpdateMap(
                draft = draft,
                publicationStatus = "Published",
                publishedVersion = nextVersion,
                publishedUrl = publishedUrl,
                hasUnpublishedChanges = false
            )
            if (existingSnap == null) {
                val createPayload = updateMap + mapOf(
                    "userId" to uid,
                    "eventId" to safeEventId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                docRef.update(updateMap).await()
            }

            // Create immutable WebsiteVersionSnapshot
            val versionDocId = "ver_${safeEventId}_v${nextVersion}_${System.currentTimeMillis().toString().takeLast(5)}"
            val versionRef = db.collection("website_versions").document(versionDocId)
            val resolvedTheme = draft.resolvedTheme().ifBlank { "RoyalPurple" }
            val resolvedLayout = draft.resolvedLayout().ifBlank { "Editorial" }
            val csv = draft.bannerUrisList().joinToString("|").take(2000)

            val versionPayload = mapOf(
                "userId" to uid,
                "eventId" to safeEventId,
                "customizationId" to docId,
                "versionNumber" to nextVersion,
                "studioName" to draft.studioName.trim().ifBlank { "Jummo Pro Studio" }.take(100),
                "photographerName" to draft.photographerName.trim().ifBlank { "Meraj Sir" }.take(100),
                "logoUri" to draft.logoUri.trim().take(500),
                "coverImageUri" to draft.coverImageUri.trim().take(500),
                "bannerUrisCsv" to csv,
                "theme" to resolvedTheme,
                "layout" to resolvedLayout,
                "welcomeMessage" to draft.welcomeMessage.trim().take(500),
                "guestAccessMode" to draft.guestAccessMode.ifBlank { "FaceSearchOnly" },
                "faceSearchEnabled" to draft.faceSearchEnabled,
                "galleryEnabled" to draft.galleryEnabled,
                "downloadsEnabled" to draft.downloadsEnabled,
                "publishedUrl" to publishedUrl.take(300),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            versionRef.set(versionPayload).await()

            WebsiteVersionSnapshot(
                id = versionDocId,
                userId = uid,
                eventId = safeEventId,
                customizationId = docId,
                versionNumber = nextVersion,
                studioName = draft.studioName.ifBlank { "Jummo Pro Studio" },
                photographerName = draft.photographerName.ifBlank { "Meraj Sir" },
                logoUri = draft.logoUri,
                coverImageUri = draft.coverImageUri,
                bannerUrisCsv = csv,
                theme = resolvedTheme,
                themePreset = resolvedTheme,
                layout = resolvedLayout,
                websiteLayout = resolvedLayout,
                welcomeMessage = draft.welcomeMessage,
                guestAccessMode = draft.guestAccessMode.ifBlank { "FaceSearchOnly" },
                faceSearchEnabled = draft.faceSearchEnabled,
                galleryEnabled = draft.galleryEnabled,
                downloadsEnabled = draft.downloadsEnabled,
                publishedUrl = publishedUrl
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun rollbackWebsiteVersion(
        currentCustomization: WebsiteCustomization,
        targetSnapshot: WebsiteVersionSnapshot
    ): Result<Unit> = runCatching {
        requireUserId()
        val safeEventId = currentCustomization.eventId.ifBlank { targetSnapshot.eventId }.ifBlank { "studio_default" }
        val docId = currentCustomization.id.ifBlank { targetSnapshot.customizationId }.ifBlank { "web_$safeEventId" }
        val docRef = db.collection("website_customizations").document(docId)

        val restoredDraft = currentCustomization.copy(
            studioName = targetSnapshot.studioName,
            photographerName = targetSnapshot.photographerName,
            logoUri = targetSnapshot.logoUri,
            coverImageUri = targetSnapshot.coverImageUri,
            bannerUrisCsv = targetSnapshot.bannerUrisCsv,
            theme = targetSnapshot.theme,
            themePreset = targetSnapshot.theme,
            layout = targetSnapshot.layout,
            websiteLayout = targetSnapshot.layout,
            welcomeMessage = targetSnapshot.welcomeMessage,
            guestAccessMode = targetSnapshot.guestAccessMode,
            faceSearchEnabled = targetSnapshot.faceSearchEnabled,
            galleryEnabled = targetSnapshot.galleryEnabled,
            downloadsEnabled = targetSnapshot.downloadsEnabled,
            publicationStatus = "Published",
            publishedVersion = targetSnapshot.versionNumber,
            publishedUrl = targetSnapshot.publishedUrl.ifBlank {
                "https://haiker729-sys.github.io/Photo-/?event=$safeEventId"
            },
            hasUnpublishedChanges = false
        )
        val updateMap = buildWebsiteCustomizationUpdateMap(
            draft = restoredDraft,
            publicationStatus = "Published",
            publishedVersion = targetSnapshot.versionNumber,
            publishedUrl = restoredDraft.publishedUrl,
            hasUnpublishedChanges = false
        )
        try {
            docRef.update(updateMap).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun setWebsiteSuspensionStatus(
        customization: WebsiteCustomization,
        suspended: Boolean,
        auditNote: String = ""
    ): Result<Unit> = runCatching {
        requireUserId()
        require(customization.id.isNotBlank()) { "Customization ID must not be blank" }
        val docRef = db.collection("website_customizations").document(customization.id)
        val newStatus = if (suspended) "Suspended" else "Published"
        val note = if (suspended) {
            auditNote.ifBlank { "Suspended by Admin Review" }
        } else {
            auditNote.ifBlank { "Active — Verified by Admin" }
        }
        try {
            docRef.update(
                mapOf(
                    "publicationStatus" to newStatus,
                    "adminAuditNote" to note.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }
}
