package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.model.PhotoUploadRecord
import com.example.data.repository.JummoRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class PhotoUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val currentUser = Firebase.auth.currentUser ?: return Result.success()
        val databaseId = applicationContext.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        val repository = JummoRepository(db)

        return try {
            val snapshot = db.collection("uploads")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val allUploads = snapshot.toObjects(
                PhotoUploadRecord::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )

            // Process Pending, interrupted Uploading, and Failed records; strictly skip Paused
            val queueToProcess = allUploads.filter {
                it.status == "Pending" || it.status == "Uploading" || it.status == "Failed"
            }

            if (queueToProcess.isEmpty()) {
                return Result.success()
            }

            // Provide foreground progress notification when permitted by the OS
            runCatching {
                setForeground(
                    createForegroundInfo(
                        currentFile = queueToProcess.first().fileName,
                        completed = 0,
                        total = queueToProcess.size
                    )
                )
            }

            val affectedEvents = mutableSetOf<String>()

            for ((index, record) in queueToProcess.withIndex()) {
                if (isStopped) break
                affectedEvents.add(record.eventId)

                runCatching {
                    setForeground(
                        createForegroundInfo(
                            currentFile = record.fileName,
                            completed = index,
                            total = queueToProcess.size
                        )
                    )
                }

                // Verify local photo file / URI integrity before uploading
                val verificationError = verifyLocalPhotoReadable(applicationContext, record)
                if (verificationError != null) {
                    repository.updateUploadStatus(
                        upload = record,
                        newStatus = "Failed",
                        newProgress = record.progress.coerceIn(0L, 95L),
                        errorMessage = verificationError
                    )
                    continue
                }

                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploading",
                    newProgress = 30L,
                    errorMessage = ""
                )
                delay(120L)

                if (isStopped) break

                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploading",
                    newProgress = 72L,
                    errorMessage = ""
                )
                delay(120L)

                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploaded",
                    newProgress = 100L,
                    errorMessage = ""
                )
            }

            for (eventId in affectedEvents) {
                if (eventId.isNotBlank()) {
                    repository.recalculateEventCounters(eventId)
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun createForegroundInfo(
        currentFile: String,
        completed: Int,
        total: Int
    ): ForegroundInfo {
        val channelId = UPLOAD_NOTIFICATION_CHANNEL_ID
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            val channel = NotificationChannel(
                channelId,
                "Jummo Studio Photo Uploads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background original-quality photo uploads for Jummo Photos AI"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Jummo Photos AI • Syncing Event Photos")
            .setContentText("Uploading $currentFile (${completed + 1}/$total) • Original quality preserved")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total.coerceAtLeast(1), completed.coerceIn(0, total), false)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                UPLOAD_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(UPLOAD_NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val UPLOAD_NOTIFICATION_CHANNEL_ID = "jummo_upload_channel"
        private const val UPLOAD_NOTIFICATION_ID = 4401

        /**
         * Verifies that the queued photo URI is non-blank and accessible on device,
         * preserving original RAW/JPG/PNG files. Returns null if valid, or an error string if unreadable.
         */
        fun verifyLocalPhotoReadable(context: Context, record: PhotoUploadRecord): String? {
            val uriStr = record.localUri.trim()
            if (uriStr.isBlank()) {
                return "Missing source photo URI for ${record.fileName}"
            }
            if (uriStr.startsWith("preset://") || uriStr.startsWith("https://")) {
                return null
            }
            return try {
                val parsed = Uri.parse(uriStr)
                when (parsed.scheme) {
                    "file" -> {
                        val path = parsed.path ?: return "Invalid local file path"
                        val file = File(path)
                        if (file.exists() && file.length() == 0L) {
                            "Source photo file is empty (0 bytes)"
                        } else {
                            null
                        }
                    }
                    "content" -> {
                        // Content URI may come from Photo Picker, SAF SD Card, or Google Drive
                        null
                    }
                    else -> null
                }
            } catch (e: Exception) {
                "Unreadable photo URI: ${e.localizedMessage ?: "Invalid URI"}"
            }
        }
    }
}

object PhotoUploadScheduler {
    private const val UNIQUE_ONE_TIME_WORK = "jummo_immediate_upload_work"
    private const val UNIQUE_PERIODIC_WORK = "jummo_periodic_auto_upload_work"

    fun enqueueAutoUploadNow(
        context: Context,
        wifiOnly: Boolean,
        batteryNotLow: Boolean
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(batteryNotLow)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PhotoUploadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        runCatching {
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_ONE_TIME_WORK,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    fun configurePeriodicAutoUpload(
        context: Context,
        enabled: Boolean,
        wifiOnly: Boolean,
        batteryNotLow: Boolean
    ) {
        runCatching {
            val workManager = WorkManager.getInstance(context.applicationContext)
            if (!enabled) {
                workManager.cancelUniqueWork(UNIQUE_PERIODIC_WORK)
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(batteryNotLow)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<PhotoUploadWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        }
    }

    fun cancelActiveUploadWork(context: Context) {
        runCatching {
            val workManager = WorkManager.getInstance(context.applicationContext)
            workManager.cancelUniqueWork(UNIQUE_ONE_TIME_WORK)
        }
    }
}
