package com.ailenezareti.panelapp.model

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val parent: Parent)
data class Parent(val id: Int, val full_name: String, val email: String)

data class ChildrenResponse(val children: List<Child>)
data class Child(
    val id: Int,
    val name: String,
    val avatar_color: String?,
    val last_seen: String?
)

data class LocationsResponse(val locations: List<LocationPoint>)
data class LocationPoint(
    val latitude: String,
    val longitude: String,
    val accuracy_m: String?,
    val battery_pct: Int?,
    val recorded_at: String
)

data class CallsResponse(
    val calls: List<CallEntry>,
    val total: Int = 0,
    val count: Int = 0,
    val limit: Int = 100,
    val offset: Int = 0,
    val has_more: Boolean = false
)
data class CallEntry(
    val phone_number: String,
    val contact_name: String?,
    val call_type: String,
    val duration_sec: Int,
    val occurred_at: String
)

data class AlertsResponse(val alerts: List<AlertEntry>)
data class AlertEntry(
    val id: Int,
    val alert_type: String,
    val message: String,
    val is_read: Int,
    val created_at: String
)

data class MarkReadRequest(val id: Int)
data class SimpleStatus(val status: String?, val error: String?)
