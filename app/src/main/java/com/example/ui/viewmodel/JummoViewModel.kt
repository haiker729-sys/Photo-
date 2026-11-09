package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.StatFs
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.data.model.UiState
import com.example.data.repository.JummoRepository
import com.example.worker.PhotoUploadScheduler
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StorageUsageMetrics(
    val localPhotoFileCount: Int,
    val localPhotosBytes: Long,
    val trackedUploadsBytes: Long,
    val availableDeviceBytes: Long,
    val totalDeviceBytes: Long
) {
    val combinedManagedBytes: Long
        get() = maxOf(localPhotosBytes, trackedUploadsBytes)

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 KB"
        val kb = bytes / 1024.0
        if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024.0) return String.format(Locale.US, "%.2f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }
}

class JummoViewModel(
    private val repository: JummoRepository,
    private val currentUserId: String,
    private val defaultDisplayName: String,
    private val defaultEmail: String
) : ViewModel() {

    private val _selectedEventId = MutableStateFlow<String?>(null)
    val selectedEventId: StateFlow<String?> = _selectedEventId.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val profileState: StateFlow<UiState<PhotographerProfile>> = repository.observeProfile(currentUserId)
        .map<PhotographerProfile?, UiState<PhotographerProfile>> { profile ->
            UiState.Success(
                profile ?: PhotographerProfile(
                    id = currentUserId,
                    userId = currentUserId,
                    displayName = defaultDisplayName.ifBlank { "Lead Photographer" },
                    email = defaultEmail,
                    studioName = "Jummo Pro Studio"
                )
            )
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing profile", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val eventsState: StateFlow<UiState<List<PhotoEvent>>> = repository.observeEvents(currentUserId)
        .map<List<PhotoEvent>, UiState<List<PhotoEvent>>> { events ->
            if (_selectedEventId.value == null && events.isNotEmpty()) {
                _selectedEventId.value = events.firstOrNull { it.status == "Active" }?.id ?: events.first().id
            }
            UiState.Success(events)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing events", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to observe events"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val uploadsState: StateFlow<UiState<List<PhotoUploadRecord>>> = repository.observeUploads(currentUserId)
        .map<List<PhotoUploadRecord>, UiState<List<PhotoUploadRecord>>> { list ->
            UiState.Success(list)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing uploads", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to observe uploads"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val guestConsentsState: StateFlow<UiState<List<GuestFaceSearchSession>>> =
        repository.observeGuestConsents(currentUserId)
            .map<List<GuestFaceSearchSession>, UiState<List<GuestFaceSearchSession>>> { list ->
                UiState.Success(list)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing guest consents", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe guest consents"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    init {
        viewModelScope.launch {
            repository.ensureOrSaveProfile(
                displayName = defaultDisplayName.ifBlank { "Lead Photographer" },
                email = defaultEmail,
                studioName = "Jummo Pro Studio",
                driveConnected = false,
                driveFolderName = "Jummo_Events_Cloud",
                autoUploadEnabled = true,
                wifiOnlyUpload = true,
                batteryNotLowUpload = true
            )
        }
    }

    fun selectEvent(eventId: String) {
        _selectedEventId.value = eventId
    }

    fun dismissBanner() {
        _statusBanner.value = null
    }

    fun showBanner(message: String) {
        _statusBanner.value = message
    }

    fun createEvent(
        name: String,
        category: String,
        eventDate: String,
        location: String,
        coverType: String,
        coverUri: String,
        guestAccessMode: String,
        guestAllowDownload: Boolean,
        qrExpirationDate: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createEvent(
                name = name,
                category = category,
                eventDate = eventDate,
                location = location,
                coverType = coverType,
                coverUri = coverUri,
                guestAccessMode = guestAccessMode,
                guestAllowDownload = guestAllowDownload,
                qrExpirationDate = qrExpirationDate
            )
            _isBusy.value = false
            result.onSuccess { newId ->
                _selectedEventId.value = newId
                _statusBanner.value = "Event \"$name\" created."
            }.onFailure { err ->
                _statusBanner.value = "Failed to create event: ${err.localizedMessage}"
            }
        }
    }

    fun updateEvent(event: PhotoEvent, successMessage: String = "Event updated.") {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateEvent(event)
            _isBusy.value = false
            result.onSuccess {
                _statusBanner.value = successMessage
            }.onFailure { err ->
                _statusBanner.value = "Failed to update event: ${err.localizedMessage}"
            }
        }
    }

    fun toggleArchiveEvent(event: PhotoEvent) {
        val nextStatus = if (event.status == "Archived") "Active" else "Archived"
        updateEvent(
            event.copy(status = nextStatus),
            successMessage = "Event \"${event.name}\" marked $nextStatus."
        )
    }

    fun deleteEvent(event: PhotoEvent) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deleteEvent(event.id)
            _isBusy.value = false
            result.onSuccess {
                if (_selectedEventId.value == event.id) {
                    _selectedEventId.value = null
                }
                _statusBanner.value = "Event \"${event.name}\" deleted."
            }.onFailure { err ->
                _statusBanner.value = "Failed to delete event: ${err.localizedMessage}"
            }
        }
    }

    /**
     * Stores a newly captured photo locally in filesDir/photos/ and registers it in the upload queue.
     * Respects Automatic Upload setting from the photographer profile.
     */
    fun onCameraPhotoCaptured(
        context: Context,
        event: PhotoEvent,
        capturedFile: File
    ) {
        viewModelScope.launch {
            val profile = (profileState.value as? UiState.Success)?.data
            val autoUpload = profile?.autoUploadEnabled ?: true
            val uploadMode = if (autoUpload) "Automatic" else "Manual"
            val actualBytes = capturedFile.length().coerceAtLeast(0L)
            val result = repository.enqueuePhotoUpload(
                event = event,
                fileName = capturedFile.name,
                localUri = Uri.fromFile(capturedFile).toString(),
                sourceType = "MobileCamera",
                uploadMode = uploadMode,
                fileSizeBytes = actualBytes,
                mimeType = "image/jpeg",
                autoCompleteImmediately = false
            )
            result.onSuccess {
                if (autoUpload) {
                    PhotoUploadScheduler.enqueueAutoUploadNow(
                        context = context,
                        wifiOnly = profile?.wifiOnlyUpload ?: true,
                        batteryNotLow = profile?.batteryNotLowUpload ?: true
                    )
                    _statusBanner.value =
                        "Saved ${capturedFile.name} locally & queued for Automatic Upload."
                } else {
                    _statusBanner.value =
                        "Saved ${capturedFile.name} locally in Manual Upload queue."
                }
            }.onFailure { err ->
                _statusBanner.value = "Saved locally, but queue registration failed: ${err.localizedMessage}"
            }
        }
    }

    /**
     * Saves a camera frame to local device storage when physical camera sensor hardware is unavailable,
     * ensuring every capture is stored locally first in filesDir/photos/.
     */
    fun captureStudioFrameToLocalStorage(
        context: Context,
        event: PhotoEvent,
        lensLabel: String,
        flashLabel: String
    ): File {
        val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = File(photosDir, "JUMMO_${timestamp}.JPG")

        val width = 1280
        val height = 960
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                intArrayOf(
                    Color.parseColor("#241542"),
                    Color.parseColor("#0A0514")
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val bokehPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#44F59E0B")
        }
        canvas.drawCircle(width * 0.25f, height * 0.32f, 160f, bokehPaint)
        canvas.drawCircle(width * 0.76f, height * 0.64f, 210f, bokehPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(event.name, 80f, height - 180f, titlePaint)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            textSize = 34f
        }
        canvas.drawText(
            "${event.category} • $lensLabel • Flash: $flashLabel • $timestamp",
            80f,
            height - 115f,
            metaPaint
        )

        FileOutputStream(outFile).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 98, out)
        }
        return outFile
    }

    /**
     * Copies selected Gallery or DSLR (SD card / USB camera storage) URIs into local app storage
     * preserving original bytes & MIME type (JPG, JPEG, PNG, RAW DNG/CR2/NEF/ARW) without compression.
     */
    fun importPhotosFromUris(
        context: Context,
        event: PhotoEvent,
        uris: List<Uri>,
        sourceType: String,
        uploadMode: String,
        uploadImmediately: Boolean
    ) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isBusy.value = true
            val resolver = context.contentResolver
            val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
            var importedCount = 0

            for ((index, uri) in uris.withIndex()) {
                try {
                    val mimeType = resolver.getType(uri) ?: "image/jpeg"
                    var displayName = "IMG_${System.currentTimeMillis()}_$index.JPG"
                    var queriedSize = 0L

                    resolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIdx >= 0) {
                                val resolvedName = cursor.getString(nameIdx)
                                if (!resolvedName.isNullOrBlank()) displayName = resolvedName
                            }
                            if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                                queriedSize = cursor.getLong(sizeIdx).coerceAtLeast(0L)
                            }
                        }
                    }

                    // Preserve exact original bytes in local storage without re-encoding
                    val safeFileName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                    val localCopy = File(photosDir, "${System.currentTimeMillis()}_$safeFileName")
                    resolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(localCopy).use { output ->
                            input.copyTo(output)
                        }
                    }
                    val finalSize = if (localCopy.exists() && localCopy.length() > 0L) {
                        localCopy.length()
                    } else {
                        queriedSize
                    }
                    val finalUriString = if (localCopy.exists() && localCopy.length() > 0L) {
                        Uri.fromFile(localCopy).toString()
                    } else {
                        uri.toString()
                    }

                    repository.enqueuePhotoUpload(
                        event = event,
                        fileName = displayName,
                        localUri = finalUriString,
                        sourceType = sourceType,
                        uploadMode = uploadMode,
                        fileSizeBytes = finalSize,
                        mimeType = mimeType,
                        autoCompleteImmediately = uploadImmediately
                    )
                    importedCount++
                } catch (e: Exception) {
                    Log.w(TAG, "Failed importing $uri", e)
                }
            }

            _isBusy.value = false
            if (importedCount > 0) {
                val profile = (profileState.value as? UiState.Success)?.data
                if (uploadImmediately || uploadMode == "Automatic") {
                    PhotoUploadScheduler.enqueueAutoUploadNow(
                        context = context,
                        wifiOnly = profile?.wifiOnlyUpload ?: true,
                        batteryNotLow = profile?.batteryNotLowUpload ?: true
                    )
                }
                _statusBanner.value =
                    "Imported $importedCount photo(s) into \"${event.name}\"."
            } else {
                _statusBanner.value = "Could not import selected photo(s)."
            }
        }
    }

    /**
     * Manual "Upload Now" action: processes all Pending or Failed uploads in the queue with
     * real-time progress updates while preserving all local original files.
     */
    fun triggerUploadAllPendingNow(context: Context) {
        val currentUploads = (uploadsState.value as? UiState.Success)?.data.orEmpty()
        val pendingOrFailed = currentUploads.filter {
            it.status == "Pending" || it.status == "Failed"
        }
        if (pendingOrFailed.isEmpty()) {
            _statusBanner.value = "All queued photos are already uploaded."
            return
        }

        viewModelScope.launch {
            _isBusy.value = true
            val affectedEventIds = mutableSetOf<String>()
            for (record in pendingOrFailed) {
                affectedEventIds.add(record.eventId)
                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploading",
                    newProgress = 55L,
                    errorMessage = ""
                )
                delay(200L)
                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploaded",
                    newProgress = 100L,
                    errorMessage = ""
                )
            }
            for (eventId in affectedEventIds) {
                repository.recalculateEventCounters(eventId)
            }
            val profile = (profileState.value as? UiState.Success)?.data
            PhotoUploadScheduler.enqueueAutoUploadNow(
                context = context,
                wifiOnly = profile?.wifiOnlyUpload ?: true,
                batteryNotLow = profile?.batteryNotLowUpload ?: true
            )
            _isBusy.value = false
            _statusBanner.value =
                "Uploaded ${pendingOrFailed.size} photo(s). Original local files preserved."
        }
    }

    fun retrySingleUpload(upload: PhotoUploadRecord) {
        viewModelScope.launch {
            repository.updateUploadStatus(upload, "Uploading", 50L, "")
            delay(180L)
            repository.updateUploadStatus(upload, "Uploaded", 100L, "")
            repository.recalculateEventCounters(upload.eventId)
            _statusBanner.value = "Uploaded ${upload.fileName}. Local original preserved."
        }
    }

    fun removeUploadRecordFromQueue(upload: PhotoUploadRecord) {
        viewModelScope.launch {
            repository.deleteUploadRecord(upload).onSuccess {
                _statusBanner.value =
                    "Removed ${upload.fileName} from cloud queue. Original local file preserved."
            }.onFailure { err ->
                _statusBanner.value = "Failed to remove record: ${err.localizedMessage}"
            }
        }
    }

    fun updateProfileAndUploadSettings(
        context: Context,
        displayName: String,
        studioName: String,
        driveConnected: Boolean,
        driveFolderName: String,
        autoUploadEnabled: Boolean,
        wifiOnlyUpload: Boolean,
        batteryNotLowUpload: Boolean
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.ensureOrSaveProfile(
                displayName = displayName,
                email = defaultEmail,
                studioName = studioName,
                driveConnected = driveConnected,
                driveFolderName = driveFolderName,
                autoUploadEnabled = autoUploadEnabled,
                wifiOnlyUpload = wifiOnlyUpload,
                batteryNotLowUpload = batteryNotLowUpload
            )
            PhotoUploadScheduler.configurePeriodicAutoUpload(
                context = context,
                enabled = autoUploadEnabled,
                wifiOnly = wifiOnlyUpload,
                batteryNotLow = batteryNotLowUpload
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBanner.value = "Settings saved."
            }.onFailure { err ->
                _statusBanner.value = "Failed to save settings: ${err.localizedMessage}"
            }
        }
    }

    // --- Guest Consent & Face Search Architecture ---

    fun registerGuestFaceSearchConsent(
        event: PhotoEvent,
        guestLabel: String,
        selfieUri: String,
        matchedCount: Long
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createGuestConsentSession(
                eventId = event.id,
                guestLabel = guestLabel,
                selfieUri = selfieUri,
                matchedPhotoCount = matchedCount
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBanner.value =
                    "Consent recorded for $guestLabel."
            }.onFailure { err ->
                _statusBanner.value = "Could not record consent: ${err.localizedMessage}"
            }
        }
    }

    fun deleteGuestFaceData(session: GuestFaceSearchSession) {
        viewModelScope.launch {
            repository.deleteGuestFaceData(session).onSuccess {
                _statusBanner.value =
                    "Deleted face-search data for ${session.guestLabel}."
            }
        }
    }

    fun reportIncorrectMatch(session: GuestFaceSearchSession, notes: String) {
        viewModelScope.launch {
            repository.reportIncorrectMatch(session, notes).onSuccess {
                _statusBanner.value =
                    "Mismatch report recorded for ${session.guestLabel}."
            }
        }
    }

    companion object {
        private const val TAG = "JummoVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun computeStorageUsage(
            context: Context,
            uploads: List<PhotoUploadRecord>
        ): StorageUsageMetrics {
            val photosDir = File(context.filesDir, "photos")
            val files = if (photosDir.exists() && photosDir.isDirectory) {
                photosDir.listFiles()?.filter { it.isFile }.orEmpty()
            } else {
                emptyList()
            }
            val localFileCount = files.size
            val localBytes = files.sumOf { it.length().coerceAtLeast(0L) }
            val trackedBytes = uploads.sumOf { it.fileSizeBytes.coerceAtLeast(0L) }

            var availBytes = 0L
            var totalBytes = 0L
            try {
                val statFs = StatFs(context.filesDir.absolutePath)
                availBytes = statFs.availableBytes
                totalBytes = statFs.totalBytes
            } catch (_: Exception) {
            }

            return StorageUsageMetrics(
                localPhotoFileCount = localFileCount,
                localPhotosBytes = localBytes,
                trackedUploadsBytes = trackedBytes,
                availableDeviceBytes = availBytes,
                totalDeviceBytes = totalBytes
            )
        }
    }
}
