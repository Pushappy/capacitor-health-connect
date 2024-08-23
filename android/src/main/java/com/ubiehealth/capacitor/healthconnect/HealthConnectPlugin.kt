package com.ubiehealth.capacitor.healthconnect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.lifecycle.lifecycleScope
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.json.JSONObject

@CapacitorPlugin(name = "HealthConnect")
class HealthConnectPlugin : Plugin() {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(this.context.applicationContext) }
    private val permissionContract by lazy {
        PermissionController.createRequestPermissionResultContract()
    }

    @PluginMethod
    fun checkAvailability(call: PluginCall) {
        val availability = when (val status = HealthConnectClient.getSdkStatus(this.context)) {
            HealthConnectClient.SDK_AVAILABLE -> "Available"
            HealthConnectClient.SDK_UNAVAILABLE -> "NotSupported"
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "NotInstalled"
            // Impossible, ok to crash
            else -> throw RuntimeException("Invalid sdk status: $status")
        }

        val res = JSObject().apply {
            put("availability", availability)
        }
        call.resolve(res)
    }

    @PluginMethod
    fun ensureInstalled(call: PluginCall) {
        val intent = installationIntent();
        startActivityForResult(call, intent, "ensureInstalledCallback")
    }

    @ActivityCallback
    fun ensureInstalledCallback(call: PluginCall, activityResult: ActivityResult) {
        val result = JSObject().apply {
            put("installed", activityResult.resultCode == Activity.RESULT_OK)
        }
        call.resolve(result)
    }

    @PluginMethod
    fun insertRecords(call: PluginCall) {
        this.activity.lifecycleScope.launch {
            try {
                val records = call.getArray("records").toList<JSONObject>().map { it.toRecord() }
                val result = healthConnectClient.insertRecords(records)

                val res = JSObject().apply {
                    put("recordIds", result.recordIdsList)
                }
                call.resolve(res)
            } catch (e: Exception) {
                call.reject("error inserting record", e);
            }
        }
    }

    @PluginMethod
    fun readRecord(call: PluginCall) {
        this.activity.lifecycleScope.launch {
            try {
                val type = call.getString("type").let {
                    RECORDS_TYPE_NAME_MAP[it]
                        ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                }

                val result = healthConnectClient.readRecord(
                    recordType = type, recordId = requireNotNull(call.getString("recordId"))
                )

                val res = JSObject().apply {
                    this.put("record", result.record.toJSONObject())
                }
                call.resolve(res)
            } catch (e: Exception) {
                call.reject("error reading record", e);
            }
        }
    }

    @PluginMethod
    fun readRecords(call: PluginCall) {
        this.activity.lifecycleScope.launch {
            try {
                val type = call.getString("type").let {
                    RECORDS_TYPE_NAME_MAP[it]
                        ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                }
                val request = ReadRecordsRequest(
                    recordType = type,
                    timeRangeFilter = call.data.getTimeRangeFilter("timeRangeFilter"),
                    dataOriginFilter = call.data.getDataOriginFilter("dataOriginFilter"),
                    ascendingOrder = call.getBoolean("ascendingOrder") ?: true,
                    pageSize = call.getInt("pageSize") ?: 1000,
                    pageToken = call.getString("pageToken"),
                )
                val result = healthConnectClient.readRecords(request)

                val res = JSObject().apply {
                    val records = result.records.map { it.toJSONObject() }.toJSONArray()
                    this.put("records", records)
                    this.put("pageToken", result.pageToken)
                }
                call.resolve(res)
            } catch (e: Exception) {
                call.reject("error reading record", e);
            }
        }
    }

    @PluginMethod
    fun getChangesToken(call: PluginCall) {
        this.activity.lifecycleScope.launch {
            try {
                val types = call.getArray("types").toList<String>().map {
                    RECORDS_TYPE_NAME_MAP[it]
                        ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                }.toSet()
                val request = ChangesTokenRequest(
                    recordTypes = types,
                    dataOriginFilters = call.data.getDataOriginFilter("dataOriginFilter"),
                )
                val token = healthConnectClient.getChangesToken(request)

                val res = JSObject().apply {
                    this.put("token", token)
                }
                call.resolve(res)
            } catch (e: Exception) {
                call.reject("error getting change token", e);

            }
        }
    }


    @PluginMethod
    fun getChanges(call: PluginCall) {
        this.activity.lifecycleScope.launch {
            try {
                var token = requireNotNull(call.getString("token"))
                val changes = flow {
                    do {
                        val result = healthConnectClient.getChanges(
                            changesToken = token,
                        )
                        emit(result.changes)
                        token = result.nextChangesToken
                    } while (result.hasMore)
                }.toList().flatten()

                val res = JSObject().apply {
                    put("changes", changes.map { it.toJSObject() }.toJSONArray())
                    put("nextToken", token)
                }
                call.resolve(res)
            } catch (e: Exception) {
                call.reject("error getting changes", e)

            }
        }
    }

    private fun installationIntent(): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setPackage("com.android.vending")
        intent.data = Uri.parse("market://details").buildUpon()
            .appendQueryParameter("id", "com.google.android.apps.healthdata")
            .appendQueryParameter("url", "healthconnect://onboarding").build()
        intent.putExtra("overlay", true)
        intent.putExtra("callerId", context.packageName)
        return intent
    }

    @PluginMethod
    fun requestHealthPermissions(call: PluginCall) {
        try {
            val sdkStatus = HealthConnectClient.getSdkStatus(this.context)
            if (sdkStatus == HealthConnectClient.SDK_UNAVAILABLE) {
                val res = JSObject().apply {
                    put("grantedPermissions", JSArray())
                    put("hasAllPermissions", false)
                }
                call.resolve(res)
                return
            }

            if (sdkStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                val intent = installationIntent()
                startActivityForResult(call, intent, "handleInstalled")
                return
            }

            val readPermissions = call.getArray("read").toList<String>().map {
                HealthPermission.getReadPermission(
                    recordType = RECORDS_TYPE_NAME_MAP[it]
                        ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                )
            }.toSet()
            val writePermissions = call.getArray("write").toList<String>().map {
                HealthPermission.getWritePermission(
                    recordType = RECORDS_TYPE_NAME_MAP[it]
                        ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                )
            }.toSet()

            val intent = permissionContract.createIntent(
                this.context, readPermissions + writePermissions
            )

            startActivityForResult(call, intent, "handleRequestPermission")
        } catch (e: Exception) {
            call.reject("error requesting permissions", e);
        }
    }

    @ActivityCallback
    fun handleInstalled(call: PluginCall, result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            requestHealthPermissions(call)
        } else {
            val res = JSObject().apply {
                put("grantedPermissions", JSArray())
                put("hasAllPermissions", false)
            }
            call.resolve(res)
        }
    }


    @ActivityCallback
    fun handleRequestPermission(call: PluginCall, result: ActivityResult) {
        try {
            val reqReadPermissions = call.getArray("read").toList<String>().associateBy {
                HealthPermission.getReadPermission(
                    recordType = RECORDS_TYPE_NAME_MAP[it]
                        ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                )
            }
            val reqWritePermissions = call.getArray("write").toList<String>().associateBy {
                HealthPermission.getWritePermission(
                    recordType = RECORDS_TYPE_NAME_MAP[it]
                        ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                )
            }

            val grantedPermissions =
                permissionContract.parseResult(result.resultCode, result.data).toSet()
            val hasAllPermissions =
                grantedPermissions.containsAll(reqReadPermissions.keys + reqWritePermissions.keys)

            val grantedPermissionsResult = JSObject().apply {
                put(
                    "read",
                    JSArray(reqReadPermissions.filterKeys { grantedPermissions.contains(it) }.values)
                )
                put(
                    "write",
                    JSArray(reqWritePermissions.filterKeys { grantedPermissions.contains(it) }.values)
                )
            }

            val res = JSObject().apply {
                put("grantedPermissions", grantedPermissionsResult)
                put("hasAllPermissions", hasAllPermissions)
            }
            call.resolve(res)
        } catch (e: Exception) {
            call.reject("error requesting permission", e)
        }
    }

    @PluginMethod
    fun checkHealthPermissions(call: PluginCall) {
        this.activity.lifecycleScope.launch {
            try {
                val reqReadPermissions = call.getArray("read").toList<String>().associateBy {
                    HealthPermission.getReadPermission(
                        recordType = RECORDS_TYPE_NAME_MAP[it]
                            ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                    )
                }
                val reqWritePermissions = call.getArray("write").toList<String>().associateBy {
                    HealthPermission.getWritePermission(
                        recordType = RECORDS_TYPE_NAME_MAP[it]
                            ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                    )
                }

                val grantedPermissions =
                    healthConnectClient.permissionController.getGrantedPermissions()
                val hasAllPermissions =
                    grantedPermissions.containsAll(reqReadPermissions.keys + reqWritePermissions.keys)

                val grantedPermissionsResult = JSObject().apply {
                    put(
                        "read",
                        JSArray(reqReadPermissions.filterKeys { grantedPermissions.contains(it) }.values)
                    )
                    put(
                        "write",
                        JSArray(reqWritePermissions.filterKeys { grantedPermissions.contains(it) }.values)
                    )
                }

                val res = JSObject().apply {
                    put("grantedPermissions", grantedPermissionsResult)
                    put("hasAllPermissions", hasAllPermissions)
                }
                call.resolve(res)
            } catch (e: Exception) {
                call.reject("error checking permissions", e)
            }
        }
    }

    @PluginMethod
    fun revokeHealthPermissions(call: PluginCall) {
        this.activity.lifecycleScope.launch {
            try {
                healthConnectClient.permissionController.revokeAllPermissions()
                call.resolve()
            } catch (e: Exception) {
                call.reject("error revoking permissions", e)
            }
        }
    }

    @PluginMethod
    fun openHealthConnectSetting(call: PluginCall) {
        val action = HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS
        val intent = Intent(action)
        this.context.startActivity(intent)

        call.resolve()
    }
}
