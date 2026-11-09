package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@IgnoreExtraProperties
data class PhotographerProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val studioName: String = "Jummo Pro Studio",
    val role: String = "photographer",
    val driveConnected: Boolean = false,
    val driveFolderName: String = "Jummo_Events_Cloud",
    val autoUploadEnabled: Boolean = true,
    val wifiOnlyUpload: Boolean = true,
    val batteryNotLowUpload: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PhotoEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "Wedding",
    val eventDate: String = "",
    val location: String = "",
    val coverType: String = "wedding",
    val coverUri: String = "",
    val status: String = "Active",
    val qrCodeValue: String = "",
    val qrEnabled: Boolean = true,
    val qrExpirationDate: String = "Never",
    val guestAccessMode: String = "FaceSearchOnly",
    val guestAllowDownload: Boolean = true,
    val totalPhotos: Long = 0L,
    val uploadedPhotos: Long = 0L,
    val pendingPhotos: Long = 0L,
    val failedPhotos: Long = 0L,
    val driveSyncStatus: String = "NotConnected",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PhotoUploadRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val fileName: String = "",
    val localUri: String = "",
    val sourceType: String = "MobileCamera",
    val uploadMode: String = "Automatic",
    val status: String = "Pending",
    val progress: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val mimeType: String = "image/jpeg",
    val faceIndexStatus: String = "AwaitingBackend",
    val driveFileId: String = "",
    val errorMessage: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GuestFaceSearchSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val guestLabel: String = "",
    val consentGranted: Boolean = true,
    val selfieUri: String = "",
    val matchStatus: String = "ConsentVerified",
    val matchedPhotoCount: Long = 0L,
    val reportNotes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object EventCategories {
    val ALL = listOf("Wedding", "Birthday", "Festival", "School", "Corporate", "Other")
}

object GuestAccessModes {
    const val FACE_SEARCH_ONLY = "FaceSearchOnly"
    const val VIEW_ALL_PERMITTED = "ViewAllPermitted"
    const val PIN_PROTECTED = "PINProtected"

    val ALL = listOf(FACE_SEARCH_ONLY, VIEW_ALL_PERMITTED, PIN_PROTECTED)

    fun displayLabel(mode: String): String = when (mode) {
        FACE_SEARCH_ONLY -> "Consent Face Search Only"
        VIEW_ALL_PERMITTED -> "View All + Face Search"
        PIN_PROTECTED -> "Organizer Approved Access"
        else -> mode
    }
}
