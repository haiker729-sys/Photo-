package com.example.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
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

            val queueToProcess = allUploads.filter {
                it.status == "Pending" || it.status == "Failed" || it.status == "Uploading"
            }

            val affectedEvents = mutableSetOf<String>()

            for (record in queueToProcess) {
                affectedEvents.add(record.eventId)
                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploading",
                    newProgress = 45L,
                    errorMessage = ""
                )
                delay(180L)
                repository.updateUploadStatus(
                    upload = record,
                    newStatus = "Uploaded",
                    newProgress = 100L,
                    errorMessage = ""
                )
            }

            for (eventId in affectedEvents) {
                repository.recalculateEventCounters(eventId)
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

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_ONE_TIME_WORK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun configurePeriodicAutoUpload(
        context: Context,
        enabled: Boolean,
        wifiOnly: Boolean,
        batteryNotLow: Boolean
    ) {
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
