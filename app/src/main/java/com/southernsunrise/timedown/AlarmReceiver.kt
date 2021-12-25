package com.southernsunrise.timedown

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val i: Intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, i, 0)
        intent.putExtra("Ringtone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        val defaultSoundUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.alarmsound)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "TimeDown")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("Timer finished")
            .setContentText("Click to finish the timer")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        var notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(111, builder.build())



    }
}