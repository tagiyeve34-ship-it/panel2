package com.ailenezareti.panelapp

import android.content.Context

object Prefs {
    private const val FILE = "panel_prefs"
    private const val KEY_TOKEN = "api_token"
    private const val KEY_PARENT_NAME = "parent_name"
    private const val KEY_ACTIVE_CHILD = "active_child_id"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun token(ctx: Context): String = prefs(ctx).getString(KEY_TOKEN, "") ?: ""
    fun setToken(ctx: Context, token: String) = prefs(ctx).edit().putString(KEY_TOKEN, token).apply()
    fun clearToken(ctx: Context) = prefs(ctx).edit().remove(KEY_TOKEN).apply()
    fun isLoggedIn(ctx: Context): Boolean = token(ctx).isNotBlank()

    fun parentName(ctx: Context): String = prefs(ctx).getString(KEY_PARENT_NAME, "") ?: ""
    fun setParentName(ctx: Context, name: String) = prefs(ctx).edit().putString(KEY_PARENT_NAME, name).apply()

    fun activeChildId(ctx: Context): Int = prefs(ctx).getInt(KEY_ACTIVE_CHILD, -1)
    fun setActiveChildId(ctx: Context, id: Int) = prefs(ctx).edit().putInt(KEY_ACTIVE_CHILD, id).apply()
    fun lastLocationSeen(ctx: Context, childId: Int): String =
        prefs(ctx).getString("last_location_seen_$childId", "") ?: ""

    fun setLastLocationSeen(ctx: Context, childId: Int, recordedAt: String) =
        prefs(ctx).edit().putString("last_location_seen_$childId", recordedAt).apply()

    fun warningFlag(ctx: Context, key: String): Boolean = prefs(ctx).getBoolean("warning_$key", false)
    fun setWarningFlag(ctx: Context, key: String, value: Boolean) = prefs(ctx).edit().putBoolean("warning_$key", value).apply()

}
