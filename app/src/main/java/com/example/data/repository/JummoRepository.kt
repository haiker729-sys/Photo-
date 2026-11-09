package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
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
        qrExpirationDate: String = "Never"
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
            "driveSyncStatus" to "NotConnected",
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
        val uploadId = "upl_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("uploads").document(uploadId)
        val initialStatus = if (autoCompleteImmediately) "Uploaded" else "Pending"
        val initialProgress = if (autoCompleteImmediately) 100L else 0L
        val faceStatus = if (autoCompleteImmediately) "Indexed" else "AwaitingBackend"

        val payload = mapOf(
            "userId" to uid,
            "eventId" to event.id,
            "eventName" to event.name.take(120),
            "fileName" to fileName.trim().ifBlank { "IMG_${System.currentTimeMillis()}.JPG" }.take(200),
            "localUri" to localUri.take(500),
            "sourceType" to sourceType,
            "uploadMode" to uploadMode,
            "status" to initialStatus,
            "progress" to initialProgress,
            "fileSizeBytes" to fileSizeBytes.coerceAtLeast(0L),
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
        val pending = eventUploads.count { it.status == "Pending" || it.status == "Uploading" }.toLong()
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
}
