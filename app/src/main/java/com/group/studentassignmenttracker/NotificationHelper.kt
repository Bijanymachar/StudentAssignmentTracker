package com.group.studentassignmenttracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_ID = "assignment_reminders"
    const val CHANNEL_NAME = "Assignment Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming assignment deadlines"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, assignment: Assignment) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dueDate = sdf.parse(assignment.dueDate) ?: return
            val now = Date()

            // Schedule reminder 1 day before
            val oneDayBefore = Date(dueDate.time - TimeUnit.DAYS.toMillis(1))
            if (oneDayBefore.after(now)) {
                val delay = oneDayBefore.time - now.time
                val data = workDataOf(
                    "title" to assignment.title,
                    "message" to "Due tomorrow: ${assignment.title} (${assignment.courseUnit})"
                )
                val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("assignment_${assignment.id}")
                    .build()
                WorkManager.getInstance(context).enqueue(request)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Assignment Due"
        val message = inputData.getString("message") ?: "You have an assignment due soon!"

        NotificationHelper.createNotificationChannel(applicationContext)

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📚 $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }
}