package com.ezivia.launcher.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ezivia.utilities.reminders.ReminderRepository

class RemindersRestoreWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            val repository = ReminderRepository(applicationContext)
            val scheduler = ReminderAlarmScheduler(applicationContext)
            scheduler.reschedule(repository.getReminders())
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        private const val WORK_NAME = "reminders_restore_work"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<RemindersRestoreWorker>().build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
