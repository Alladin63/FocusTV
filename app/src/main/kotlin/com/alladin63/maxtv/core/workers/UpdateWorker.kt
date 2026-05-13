package com.alladin63.maxtv.core.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleUpdate(context)
        }
    }

    companion object {
        fun scheduleUpdate(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdateWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayToMidnight(), TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "playlist_update",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private fun calculateDelayToMidnight(): Long {
            val now = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 4)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
            }
            return (cal.timeInMillis - now).coerceAtLeast(0)
        }
    }
}

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("UpdateWorker: démarrage mise à jour automatique")
            // TODO: Déclencher la mise à jour de la playlist active
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "UpdateWorker error")
            Result.retry()
        }
    }
}
