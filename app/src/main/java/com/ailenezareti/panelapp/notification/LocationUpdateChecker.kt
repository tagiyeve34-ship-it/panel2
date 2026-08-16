package com.ailenezareti.panelapp.notification

import android.content.Context
import com.ailenezareti.panelapp.Prefs
import com.ailenezareti.panelapp.api.ApiClient
import java.text.SimpleDateFormat
import java.util.Locale

object LocationUpdateChecker {
    suspend fun check(context: Context, notifyOnChange: Boolean = true) {
        if (!Prefs.isLoggedIn(context)) return
        val childrenResponse = ApiClient.get(context).getChildren()
        if (!childrenResponse.isSuccessful) return
        val children = childrenResponse.body()?.children ?: return

        for (child in children) {
            try {
                val response = ApiClient.get(context).getLocations(child.id, "3h")
                if (!response.isSuccessful) continue
                val latest = response.body()?.locations?.firstOrNull() ?: continue

                val previous = Prefs.lastLocationSeen(context, child.id)
                if (previous.isBlank()) {
                    Prefs.setLastLocationSeen(context, child.id, latest.recorded_at)
                } else if (latest.recorded_at != previous) {
                    Prefs.setLastLocationSeen(context, child.id, latest.recorded_at)
                    if (notifyOnChange) LocationNotificationManager.show(context, child.id, child.name, latest)
                }

                val batteryKey = "battery_${child.id}"
                val battery = latest.battery_pct
                if (battery != null && battery <= 15 && !Prefs.warningFlag(context, batteryKey)) {
                    Prefs.setWarningFlag(context, batteryKey, true)
                    if (notifyOnChange) LocationNotificationManager.showBatteryLow(context, child.id, child.name, battery)
                } else if (battery != null && battery >= 20) {
                    Prefs.setWarningFlag(context, batteryKey, false)
                }

                val offlineKey = "offline_${child.id}"
                val offline = minutesAgo(latest.recorded_at) > 30
                if (offline && !Prefs.warningFlag(context, offlineKey)) {
                    Prefs.setWarningFlag(context, offlineKey, true)
                    if (notifyOnChange) LocationNotificationManager.showOffline(context, child.id, child.name, latest.recorded_at)
                } else if (!offline) {
                    Prefs.setWarningFlag(context, offlineKey, false)
                }
            } catch (_: Exception) { }
        }
    }

    private fun minutesAgo(value: String): Long {
        val clean = value.replace("T", " ").replace("Z", "").substringBefore("+").take(19)
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(clean)
            ((System.currentTimeMillis() - (date?.time ?: 0)) / 60000L).coerceAtLeast(0)
        } catch (_: Exception) { 0 }
    }
}
