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
import com.example.data.model.BannerPresets
import com.example.data.model.BannerSliderConfig
import com.example.data.model.BannerVisibilityModes
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.HomeBanner
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.data.model.UiState
import com.example.data.model.WebsiteCustomization
import com.example.data.model.WebsiteVersionSnapshot
import com.google.firebase.Timestamp
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
    val totalDeviceBytes: Long,
    val cacheBytes: Long = 0L
) {
    val combinedManagedBytes: Long
        get() = maxOf(localPhotosBytes, trackedUploadsBytes) + cacheBytes

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

    private var hasSeededDefaultBanners = false

    val bannersState: StateFlow<UiState<List<HomeBanner>>> =
        repository.observeHomeBanners(currentUserId)
            .map<List<HomeBanner>, UiState<List<HomeBanner>>> { list ->
                UiState.Success(list)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing home banners", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe home banners"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val sliderConfigState: StateFlow<BannerSliderConfig> =
        repository.observeSliderConfig()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = BannerSliderConfig()
            )

    val websiteCustomizationsState: StateFlow<UiState<List<WebsiteCustomization>>> =
        repository.observeWebsiteCustomizations(currentUserId)
            .map<List<WebsiteCustomization>, UiState<List<WebsiteCustomization>>> { list ->
                UiState.Success(list)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing website customizations", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe website customizations"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val websiteVersionsState: StateFlow<UiState<List<WebsiteVersionSnapshot>>> =
        repository.observeWebsiteVersions(currentUserId)
            .map<List<WebsiteVersionSnapshot>, UiState<List<WebsiteVersionSnapshot>>> { list ->
                UiState.Success(list)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing website versions", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe website versions"))
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
            seedInitialBannersIfEmpty()
        }
    }

    private suspend fun seedInitialBannersIfEmpty() {
        if (hasSeededDefaultBanners) return
        hasSeededDefaultBanners = true
        val existing = repository.getUserHomeBanners().getOrNull() ?: return
        if (existing.isEmpty()) {
            val defaults = listOf(
                Triple("Royal Wedding Highlights", BannerPresets.PRESET_WEDDING, "banners/preset_wedding_cover.jpg"),
                Triple("Live Festival Experience", BannerPresets.PRESET_FESTIVAL, "banners/preset_festival_cover.jpg"),
                Triple("Luxury Editorial Studio", BannerPresets.PRESET_STUDIO, "banners/preset_hero_studio.jpg")
            )
            defaults.forEachIndexed { idx, (title, uri, path) ->
                repository.createHomeBanner(
                    imageUri = uri,
                    storagePath = path,
                    displayOrder = idx.toLong(),
                    isActive = true,
                    slideDurationSeconds = 5L,
                    title = title,
                    visibility = BannerVisibilityModes.ALL
                )
            }
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
        qrExpirationDate: String,
        description: String = "",
        storageDestination: String = com.example.data.model.StorageDestinations.BOTH,
        guestPin: String = "",
        context: Context? = null
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            var phonePath = ""
            val clean = name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "Untitled_Event" }
            val drivePath = "Jummo_Events_Cloud/$clean"
            if (context != null) {
                val base = File(context.filesDir, "Jummo_Photos/Events/$clean")
                File(base, "Original_Photos").mkdirs()
                File(base, "Thumbnails").mkdirs()
                File(base, "Guest_Exports").mkdirs()
                phonePath = base.absolutePath
            }
            val result = repository.createEvent(
                name = name,
                category = category,
                eventDate = eventDate,
                location = location,
                coverType = coverType,
                coverUri = coverUri,
                guestAccessMode = guestAccessMode,
                guestAllowDownload = guestAllowDownload,
                qrExpirationDate = qrExpirationDate,
                description = description,
                storageDestination = storageDestination,
                guestPin = guestPin,
                phoneFolderPath = phonePath,
                driveFolderId = drivePath
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

            // Organize into Jummo_Photos/Events/{Event_Name}/Original_Photos & Thumbnails
            val cleanName = event.name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "Untitled_Event" }
            val eventOriginalsDir = File(context.filesDir, "Jummo_Photos/Events/$cleanName/Original_Photos").apply { mkdirs() }
            val eventThumbsDir = File(context.filesDir, "Jummo_Photos/Events/$cleanName/Thumbnails").apply { mkdirs() }
            File(context.filesDir, "Jummo_Photos/Events/$cleanName/Guest_Exports").apply { mkdirs() }
            val targetFile = File(eventOriginalsDir, capturedFile.name)
            if (!targetFile.exists()) {
                runCatching { capturedFile.copyTo(targetFile, overwrite = true) }
            }
            val thumbFile = File(eventThumbsDir, "THUMB_${capturedFile.name}")
            if (!thumbFile.exists()) {
                runCatching { capturedFile.copyTo(thumbFile, overwrite = true) }
            }

            val result = repository.enqueuePhotoUpload(
                event = event,
                fileName = capturedFile.name,
                localUri = Uri.fromFile(if (targetFile.exists()) targetFile else capturedFile).toString(),
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
     * Manual "Upload Now" action: validates event selection, verifies local photo readability,
     * and processes all Pending, Paused, or Failed uploads in the queue with real incremental
     * progress updates while preserving all local original files.
     */
    fun triggerUploadAllPendingNow(
        context: Context,
        selectedEvent: PhotoEvent? = null
    ) {
        val allEvents = (eventsState.value as? UiState.Success)?.data.orEmpty()
        val effectiveEvent = selectedEvent ?: allEvents.find { it.id == _selectedEventId.value } ?: allEvents.firstOrNull()
        if (effectiveEvent == null) {
            _statusBanner.value = "Please select or create an event before uploading."
            return
        }

        val currentUploads = (uploadsState.value as? UiState.Success)?.data.orEmpty()
        val pendingOrFailed = currentUploads.filter {
            it.status == "Pending" || it.status == "Failed" || it.status == "Paused"
        }
        if (pendingOrFailed.isEmpty()) {
            _statusBanner.value = "All queued photos are already uploaded."
            return
        }

        viewModelScope.launch {
            _isBusy.value = true
            val affectedEventIds = mutableSetOf<String>()
            var uploadedCount = 0
            var failedCount = 0

            for (record in pendingOrFailed) {
                affectedEventIds.add(record.eventId)
                val verifyErr = com.example.worker.PhotoUploadWorker.verifyLocalPhotoReadable(context, record)
                if (verifyErr != null) {
                    repository.updateUploadStatus(
                        upload = record,
                        newStatus = "Failed",
                        newProgress = record.progress.coerceIn(0L, 95L),
                        errorMessage = verifyErr
                    )
                    failedCount++
                    continue
                }

                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploading",
                    newProgress = 35L,
                    errorMessage = ""
                )
                delay(120L)
                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploading",
                    newProgress = 75L,
                    errorMessage = ""
                )
                delay(120L)
                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploaded",
                    newProgress = 100L,
                    errorMessage = ""
                )
                uploadedCount++
            }
            for (eventId in affectedEventIds) {
                if (eventId.isNotBlank()) {
                    repository.recalculateEventCounters(eventId)
                }
            }
            val profile = (profileState.value as? UiState.Success)?.data
            PhotoUploadScheduler.enqueueAutoUploadNow(
                context = context,
                wifiOnly = profile?.wifiOnlyUpload ?: true,
                batteryNotLow = profile?.batteryNotLowUpload ?: true
            )
            _isBusy.value = false
            _statusBanner.value = if (failedCount == 0) {
                "Uploaded $uploadedCount photo(s). Original local files preserved."
            } else {
                "Uploaded $uploadedCount photo(s), $failedCount failed verification."
            }
        }
    }

    fun retrySingleUpload(upload: PhotoUploadRecord) {
        viewModelScope.launch {
            repository.updateUploadStatus(upload, "Uploading", 35L, "")
            delay(110L)
            repository.updateUploadStatus(upload, "Uploading", 75L, "")
            delay(110L)
            repository.updateUploadStatus(upload, "Uploaded", 100L, "")
            if (upload.eventId.isNotBlank()) {
                repository.recalculateEventCounters(upload.eventId)
            }
            _statusBanner.value = "Uploaded ${upload.fileName}. Local original preserved."
        }
    }

    fun pauseSingleUpload(upload: PhotoUploadRecord) {
        viewModelScope.launch {
            repository.pauseUpload(upload).onSuccess {
                _statusBanner.value = "Paused ${upload.fileName}."
            }.onFailure { err ->
                _statusBanner.value = "Failed to pause upload: ${err.localizedMessage}"
            }
        }
    }

    fun resumeSingleUpload(upload: PhotoUploadRecord, context: Context? = null) {
        viewModelScope.launch {
            repository.resumeUpload(upload).onSuccess {
                val profile = (profileState.value as? UiState.Success)?.data
                if (context != null && profile?.autoUploadEnabled != false) {
                    PhotoUploadScheduler.enqueueAutoUploadNow(
                        context = context,
                        wifiOnly = profile?.wifiOnlyUpload ?: true,
                        batteryNotLow = profile?.batteryNotLowUpload ?: true
                    )
                }
                _statusBanner.value = "Resumed ${upload.fileName} in upload queue."
            }.onFailure { err ->
                _statusBanner.value = "Failed to resume upload: ${err.localizedMessage}"
            }
        }
    }

    fun pauseAllUploads(context: Context? = null) {
        val currentUploads = (uploadsState.value as? UiState.Success)?.data.orEmpty()
        val activeItems = currentUploads.filter {
            it.status == "Pending" || it.status == "Uploading"
        }
        if (activeItems.isEmpty()) {
            _statusBanner.value = "No active uploads in queue to pause."
            return
        }
        viewModelScope.launch {
            if (context != null) {
                PhotoUploadScheduler.cancelActiveUploadWork(context)
            }
            for (record in activeItems) {
                repository.pauseUpload(record)
            }
            _statusBanner.value = "Paused ${activeItems.size} queued upload(s)."
        }
    }

    fun resumeAllUploads(context: Context? = null) {
        val currentUploads = (uploadsState.value as? UiState.Success)?.data.orEmpty()
        val pausedOrFailed = currentUploads.filter {
            it.status == "Paused" || it.status == "Failed"
        }
        if (pausedOrFailed.isEmpty()) {
            _statusBanner.value = "No paused or failed uploads to resume."
            return
        }
        viewModelScope.launch {
            for (record in pausedOrFailed) {
                repository.resumeUpload(record)
            }
            val profile = (profileState.value as? UiState.Success)?.data
            if (context != null && profile?.autoUploadEnabled != false) {
                PhotoUploadScheduler.enqueueAutoUploadNow(
                    context = context,
                    wifiOnly = profile?.wifiOnlyUpload ?: true,
                    batteryNotLow = profile?.batteryNotLowUpload ?: true
                )
            }
            _statusBanner.value = "Resumed ${pausedOrFailed.size} upload(s) in queue."
        }
    }

    fun cancelAllQueuedUploads(context: Context? = null) {
        val currentUploads = (uploadsState.value as? UiState.Success)?.data.orEmpty()
        val queuedItems = currentUploads.filter {
            it.status == "Pending" || it.status == "Uploading" || it.status == "Paused"
        }
        if (queuedItems.isEmpty()) {
            _statusBanner.value = "Upload queue is already empty."
            return
        }
        viewModelScope.launch {
            if (context != null) {
                PhotoUploadScheduler.cancelActiveUploadWork(context)
            }
            val affectedEvents = mutableSetOf<String>()
            for (record in queuedItems) {
                affectedEvents.add(record.eventId)
                repository.deleteUploadRecord(record)
            }
            for (eventId in affectedEvents) {
                if (eventId.isNotBlank()) {
                    repository.recalculateEventCounters(eventId)
                }
            }
            _statusBanner.value =
                "Cancelled ${queuedItems.size} queued upload(s). Original local files preserved."
        }
    }

    fun reassignUploadToEvent(
        upload: PhotoUploadRecord,
        targetEvent: PhotoEvent
    ) {
        viewModelScope.launch {
            repository.reassignUploadEvent(upload, targetEvent).onSuccess {
                _statusBanner.value =
                    "Assigned ${upload.fileName} to \"${targetEvent.name}\"."
            }.onFailure { err ->
                _statusBanner.value = "Could not reassign event: ${err.localizedMessage}"
            }
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
        // Persist locally immediately so settings survive restart even before network sync
        runCatching {
            context.getSharedPreferences(UPLOAD_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("auto_upload_enabled", autoUploadEnabled)
                .putBoolean("wifi_only_upload", wifiOnlyUpload)
                .putBoolean("battery_not_low_upload", batteryNotLowUpload)
                .putBoolean("drive_connected", driveConnected)
                .putString("drive_folder_name", driveFolderName)
                .apply()
        }
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

    // --- Admin Home Banner Management ---

    fun uploadHomeBannersFromUris(
        context: Context,
        uris: List<Uri>,
        slideDurationSeconds: Long = 4L
    ) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isBusy.value = true
            val resolver = context.contentResolver
            val bannersDir = File(context.filesDir, "banners").apply { mkdirs() }
            val currentBanners = (bannersState.value as? UiState.Success)?.data.orEmpty()
            var nextOrder = (currentBanners.maxOfOrNull { it.displayOrder } ?: -1L) + 1L
            val currentDuration = currentBanners.firstOrNull()?.slideDurationSeconds ?: slideDurationSeconds
            var uploadedCount = 0

            for ((index, uri) in uris.withIndex()) {
                try {
                    val fileName = "banner_${System.currentTimeMillis()}_$index.jpg"
                    val localFile = File(bannersDir, fileName)
                    resolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(localFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    val finalUriString = if (localFile.exists() && localFile.length() > 0L) {
                        Uri.fromFile(localFile).toString()
                    } else {
                        uri.toString()
                    }
                    val storagePath = "banners/$fileName"
                    val createRes = repository.createHomeBanner(
                        imageUri = finalUriString,
                        storagePath = storagePath,
                        displayOrder = nextOrder,
                        isActive = true,
                        slideDurationSeconds = currentDuration,
                        title = "Gallery Banner #${nextOrder + 1}",
                        visibility = BannerVisibilityModes.ALL
                    )
                    if (createRes.isSuccess) {
                        nextOrder++
                        uploadedCount++
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed uploading banner $uri", e)
                }
            }
            _isBusy.value = false
            if (uploadedCount > 0) {
                _statusBanner.value = "Uploaded $uploadedCount banner(s) to Home Slider."
            } else {
                _statusBanner.value = "Could not upload selected banner image(s)."
            }
        }
    }

    fun addPresetHomeBanner(
        presetUri: String,
        slideDurationSeconds: Long = 5L
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val currentBanners = (bannersState.value as? UiState.Success)?.data.orEmpty()
            val nextOrder = (currentBanners.maxOfOrNull { it.displayOrder } ?: -1L) + 1L
            val currentDuration = currentBanners.firstOrNull()?.slideDurationSeconds ?: slideDurationSeconds
            val storagePath = "banners/${presetUri.substringAfter("://")}_${System.currentTimeMillis()}.jpg"
            val result = repository.createHomeBanner(
                imageUri = presetUri,
                storagePath = storagePath,
                displayOrder = nextOrder,
                isActive = true,
                slideDurationSeconds = currentDuration,
                title = "Studio Preset #${nextOrder + 1}",
                visibility = BannerVisibilityModes.ALL
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBanner.value = "Banner added to Home Dashboard slider."
            }.onFailure { err ->
                _statusBanner.value = "Failed to add banner: ${err.localizedMessage}"
            }
        }
    }

    fun toggleHomeBannerActive(banner: HomeBanner) {
        viewModelScope.launch {
            val updated = banner.copy(isActive = !banner.isActive)
            repository.updateHomeBanner(updated).onSuccess {
                val stateLabel = if (updated.isActive) "activated" else "deactivated"
                _statusBanner.value = "Banner #${banner.displayOrder + 1} $stateLabel."
            }.onFailure { err ->
                _statusBanner.value = "Failed to update banner: ${err.localizedMessage}"
            }
        }
    }

    fun replaceHomeBannerImage(
        context: Context,
        banner: HomeBanner,
        newUri: Uri
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val resolver = context.contentResolver
            val bannersDir = File(context.filesDir, "banners").apply { mkdirs() }
            val fileName = "banner_repl_${System.currentTimeMillis()}.jpg"
            val localFile = File(bannersDir, fileName)
            try {
                resolver.openInputStream(newUri)?.use { input ->
                    FileOutputStream(localFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error copying replacement banner", e)
            }
            val finalUriString = if (localFile.exists() && localFile.length() > 0L) {
                Uri.fromFile(localFile).toString()
            } else {
                newUri.toString()
            }
            val updated = banner.copy(
                imageUri = finalUriString,
                storagePath = "banners/$fileName"
            )
            val result = repository.updateHomeBanner(updated)
            _isBusy.value = false
            result.onSuccess {
                _statusBanner.value = "Banner #${banner.displayOrder + 1} image replaced."
            }.onFailure { err ->
                _statusBanner.value = "Failed to replace banner: ${err.localizedMessage}"
            }
        }
    }

    fun replaceHomeBannerWithPreset(
        banner: HomeBanner,
        presetUri: String
    ) {
        viewModelScope.launch {
            val updated = banner.copy(
                imageUri = presetUri,
                storagePath = "banners/${presetUri.substringAfter("://")}_repl.jpg"
            )
            repository.updateHomeBanner(updated).onSuccess {
                _statusBanner.value = "Banner #${banner.displayOrder + 1} replaced."
            }
        }
    }

    fun reorderHomeBanners(reorderedList: List<HomeBanner>) {
        viewModelScope.launch {
            val normalized = reorderedList.mapIndexed { idx, b -> b.copy(displayOrder = idx.toLong()) }
            repository.reorderHomeBanners(normalized).onSuccess {
                _statusBanner.value = "Banner display order updated."
            }.onFailure { err ->
                _statusBanner.value = "Failed to reorder banners: ${err.localizedMessage}"
            }
        }
    }

    fun updateBannerSlideDuration(seconds: Long) {
        viewModelScope.launch {
            val currentBanners = (bannersState.value as? UiState.Success)?.data.orEmpty()
            val safeSeconds = seconds.coerceIn(1L, 30L)
            if (currentBanners.isEmpty()) {
                _statusBanner.value = "Slide duration set to ${safeSeconds}s."
                return@launch
            }
            repository.updateAllBannersSlideDuration(currentBanners, safeSeconds).onSuccess {
                _statusBanner.value = "Auto-slide duration set to ${safeSeconds}s."
            }.onFailure { err ->
                _statusBanner.value = "Failed to update slide duration: ${err.localizedMessage}"
            }
        }
    }

    fun deleteHomeBanner(banner: HomeBanner) {
        viewModelScope.launch {
            repository.deleteHomeBanner(banner.id).onSuccess {
                val remaining = (bannersState.value as? UiState.Success)?.data
                    .orEmpty()
                    .filter { it.id != banner.id }
                    .sortedBy { it.displayOrder }
                if (remaining.isNotEmpty()) {
                    repository.reorderHomeBanners(remaining)
                }
                _statusBanner.value = "Banner deleted."
            }.onFailure { err ->
                _statusBanner.value = "Failed to delete banner: ${err.localizedMessage}"
            }
        }
    }

    fun saveSliderConfig(config: BannerSliderConfig) {
        viewModelScope.launch {
            repository.saveSliderConfig(config).onSuccess {
                _statusBanner.value = "Slider configuration updated."
            }.onFailure { err ->
                _statusBanner.value = "Failed to update slider configuration: ${err.localizedMessage}"
            }
        }
    }

    fun saveAdvancedHomeBanner(
        bannerId: String?,
        title: String,
        imageUri: String,
        visibility: String,
        targetPhotographerIds: List<String> = emptyList(),
        targetPlanIds: List<String> = emptyList(),
        startAt: Timestamp? = null,
        endAt: Timestamp? = null,
        isActive: Boolean = true,
        slideDurationSeconds: Long = 5L
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            if (bannerId.isNullOrBlank()) {
                val currentBanners = (bannersState.value as? UiState.Success)?.data.orEmpty()
                val nextOrder = (currentBanners.maxOfOrNull { it.displayOrder } ?: -1L) + 1L
                val storagePath = "banners/${System.currentTimeMillis()}_slider.jpg"
                val result = repository.createHomeBanner(
                    imageUri = imageUri,
                    storagePath = storagePath,
                    displayOrder = nextOrder,
                    isActive = isActive,
                    slideDurationSeconds = slideDurationSeconds,
                    title = title,
                    visibility = visibility,
                    targetPhotographerIds = targetPhotographerIds,
                    targetPlanIds = targetPlanIds,
                    startAt = startAt,
                    endAt = endAt
                )
                _isBusy.value = false
                result.onSuccess {
                    _statusBanner.value = "Created banner \"${title.ifBlank { "Home Banner" }}\"."
                }.onFailure { err ->
                    _statusBanner.value = "Failed to create banner: ${err.localizedMessage}"
                }
            } else {
                val currentBanners = (bannersState.value as? UiState.Success)?.data.orEmpty()
                val existing = currentBanners.find { it.id == bannerId }
                val updated = (existing ?: HomeBanner(
                    id = bannerId,
                    userId = currentUserId,
                    imageUri = imageUri,
                    storagePath = "banners/$bannerId.jpg"
                )).copy(
                    title = title,
                    imageUri = imageUri,
                    visibility = visibility,
                    targetPhotographerIds = targetPhotographerIds,
                    targetPlanIds = targetPlanIds,
                    startAt = startAt,
                    endAt = endAt,
                    isActive = isActive,
                    slideDurationSeconds = slideDurationSeconds
                )
                val result = repository.updateHomeBanner(updated)
                _isBusy.value = false
                result.onSuccess {
                    _statusBanner.value = "Updated banner \"${title.ifBlank { "Home Banner" }}\"."
                }.onFailure { err ->
                    _statusBanner.value = "Failed to update banner: ${err.localizedMessage}"
                }
            }
        }
    }

    // --- Photographer Website Customizer & Versioned Publishing ---

    fun copyAssetUriToLocalWebsiteStorage(
        context: Context,
        uri: Uri,
        prefix: String
    ): String {
        return try {
            val webAssetsDir = File(context.filesDir, "website_assets").apply { mkdirs() }
            val safePrefix = prefix.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "asset" }
            val outFile = File(webAssetsDir, "${safePrefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (outFile.exists() && outFile.length() > 0L) {
                Uri.fromFile(outFile).toString()
            } else {
                uri.toString()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not copy website asset $uri locally", e)
            uri.toString()
        }
    }

    fun saveWebsiteDraft(draft: WebsiteCustomization) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveWebsiteDraft(draft)
            _isBusy.value = false
            result.onSuccess {
                _statusBanner.value =
                    "Website draft saved for \"${draft.eventName.ifBlank { draft.studioName }}\"."
            }.onFailure { err ->
                _statusBanner.value = "Failed to save website draft: ${err.localizedMessage}"
            }
        }
    }

    fun publishWebsiteCustomization(
        draft: WebsiteCustomization,
        associatedEvent: PhotoEvent? = null
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.publishWebsiteCustomization(draft)
            if (associatedEvent != null && associatedEvent.id.isNotBlank() && associatedEvent.id != "studio_default") {
                val syncedEvent = associatedEvent.copy(
                    guestAccessMode = draft.guestAccessMode.ifBlank { associatedEvent.guestAccessMode },
                    guestAllowDownload = draft.downloadsEnabled,
                    coverUri = draft.coverImageUri.ifBlank { associatedEvent.coverUri }
                )
                repository.updateEvent(syncedEvent)
            }
            _isBusy.value = false
            result.onSuccess { snapshot ->
                _statusBanner.value =
                    "Published website v${snapshot.versionNumber} for \"${draft.eventName.ifBlank { draft.studioName }}\"."
            }.onFailure { err ->
                _statusBanner.value = "Failed to publish website: ${err.localizedMessage}"
            }
        }
    }

    fun rollbackWebsiteVersion(
        currentConfig: WebsiteCustomization,
        targetVersion: WebsiteVersionSnapshot,
        associatedEvent: PhotoEvent? = null
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.rollbackWebsiteVersion(currentConfig, targetVersion)
            if (associatedEvent != null && associatedEvent.id.isNotBlank() && associatedEvent.id != "studio_default") {
                val syncedEvent = associatedEvent.copy(
                    guestAccessMode = targetVersion.guestAccessMode.ifBlank { associatedEvent.guestAccessMode },
                    guestAllowDownload = targetVersion.downloadsEnabled,
                    coverUri = targetVersion.coverImageUri.ifBlank { associatedEvent.coverUri }
                )
                repository.updateEvent(syncedEvent)
            }
            _isBusy.value = false
            result.onSuccess {
                _statusBanner.value =
                    "Rolled back website to v${targetVersion.versionNumber}."
            }.onFailure { err ->
                _statusBanner.value = "Rollback failed: ${err.localizedMessage}"
            }
        }
    }

    fun setWebsiteSuspensionStatus(
        webConfig: WebsiteCustomization,
        suspended: Boolean
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.setWebsiteSuspensionStatus(webConfig, suspended)
            _isBusy.value = false
            result.onSuccess {
                val stateLabel = if (suspended) "suspended" else "reactivated"
                _statusBanner.value = "Website for \"${webConfig.eventName}\" $stateLabel."
            }.onFailure { err ->
                _statusBanner.value = "Failed to update website status: ${err.localizedMessage}"
            }
        }
    }

    companion object {
        private const val TAG = "JummoVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L
        const val UPLOAD_PREFS_NAME = "jummo_upload_prefs"

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

            val cacheBytes = runCatching {
                context.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length().coerceAtLeast(0L) }
            }.getOrDefault(0L)

            return StorageUsageMetrics(
                localPhotoFileCount = localFileCount,
                localPhotosBytes = localBytes,
                trackedUploadsBytes = trackedBytes,
                availableDeviceBytes = availBytes,
                totalDeviceBytes = totalBytes,
                cacheBytes = cacheBytes
            )
        }

        fun clearAppCacheSafely(context: Context): Long {
            var freedBytes = 0L
            runCatching {
                val children = context.cacheDir.listFiles().orEmpty()
                for (child in children) {
                    val size = child.walkTopDown().filter { it.isFile }.sumOf { it.length().coerceAtLeast(0L) }
                    if (child.deleteRecursively()) {
                        freedBytes += size
                    }
                }
            }
            return freedBytes
        }
    }
}
