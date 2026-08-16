package com.ailenezareti.panelapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ailenezareti.panelapp.R
import com.ailenezareti.panelapp.model.LocationPoint
import com.ailenezareti.panelapp.ui.MainActivity

object LocationNotificationManager {
    const val CHANNEL_ID = "gps_updates"
    const val EXTRA_OPEN_LOCATION = "open_location"
    const val EXTRA_CHILD_ID = "child_id"
    const val EXTRA_LATITUDE = "latitude"
    const val EXTRA_LONGITUDE = "longitude"
    const val EXTRA_RECORDED_AT = "recorded_at"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GPS yenilənmələri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Yeni lokasiya məlumatı gəldikdə bildiriş göstərir"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun show(
        context: Context,
        childId: Int,
        childName: String,
        point: LocationPoint
    ) {
        createChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val lat = point.latitude.toDoubleOrNull() ?: return
        val lon = point.longitude.toDoubleOrNull() ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_LOCATION, true)
            putExtra(EXTRA_CHILD_ID, childId)
            putExtra(EXTRA_LATITUDE, lat)
            putExtra(EXTRA_LONGITUDE, lon)
            putExtra(EXTRA_RECORDED_AT, point.recorded_at)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            childId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val time = point.recorded_at
            .replace("T", " ")
            .replace("Z", "")
            .substringBefore("+")
            .let { if (it.length >= 16) it.substring(11, 16) else it }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_my_location)
            .setContentTitle("Yeni GPS yenilənməsi")
            .setContentText("$childName • Son mövqe $time-da yeniləndi")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify(10_000 + childId, notification)
    }

    fun showBatteryLow(context: Context, childId: Int, childName: String, battery: Int) {
        createChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("Batareya aşağıdır")
            .setContentText("$childName · batareya $battery%-dir")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(20_000 + childId, notification)
    }

    fun showOffline(context: Context, childId: Int, childName: String, recordedAt: String) {
        createChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("GPS məlumatı gecikir")
            .setContentText("$childName · son GPS: ${recordedAt.replace("T", " ").take(16)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(30_000 + childId, notification)
    }
}
