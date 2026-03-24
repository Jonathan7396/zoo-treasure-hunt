package com.example.zootreasurehunt.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.app.NotificationManager
import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
class CongratulationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        val animalName = inputData.getString("ANIMAL_NAME") ?: "Animal"

        triggerNotification(animalName)

        return Result.success()
    }
    private fun triggerNotification(animalName: String) {

        val channelId = "zoo_hunt_channel"

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Zoo Hunt Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Great Job!")
            .setContentText("You found the $animalName!")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}