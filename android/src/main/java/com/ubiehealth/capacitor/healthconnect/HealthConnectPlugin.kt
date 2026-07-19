package com.ubiehealth.capacitor.healthconnect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
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
import android.util.Log

@CapacitorPlugin(name = "HealthConnect")
class HealthConnectPlugin : Plugin() {
  private var healthConnectClient: HealthConnectClient? = null
  private val permissionContract by lazy {
    PermissionController.createRequestPermissionResultContract()
  }

  @PluginMethod
fun checkAvailability(call: PluginCall) {
    val availability = try {
        when (val status = HealthConnectClient.getSdkStatus(this.context)) {
            HealthConnectClient.SDK_AVAILABLE -> {
                Log.d("HealthConnectPlugin", "Health Connect SDK is available.")
                if (healthConnectClient == null) {
                    healthConnectClient = HealthConnectClient.getOrCreate(this.context.applicationContext)
                    Log.d("HealthConnectPlugin", "HealthConnectClient initialized.")
                }
                "Available"
            }
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Log.w("HealthConnectPlugin", "Health Connect SDK is unavailable.")
                "NotSupported"
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                Log.w("HealthConnectPlugin", "Health Connect provider needs update or is not installed.")
                "NotInstalled"
            }
            else -> {
                Log.e("HealthConnectPlugin", "Unknown SDK status: $status")
                throw RuntimeException("Invalid sdk status: $status")
            }
        }
    } catch (e: Exception) {
        Log.e("HealthConnectPlugin", "Error checking Health Connect availability", e)
        "Error"
    }

    val res = JSObject().apply {
        put("availability", availability)
    }
    call.resolve(res)
}

  private fun ensureClientOrReject(call: PluginCall): Boolean {
    return when (HealthConnectClient.getSdkStatus(context)) {
      HealthConnectClient.SDK_AVAILABLE -> {
        if (healthConnectClient == null) {
          healthConnectClient = HealthConnectClient.getOrCreate(context)
        }
        true
      }
      HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
        call.reject("Health Connect is not installed or needs update.")
        false
      }
      HealthConnectClient.SDK_UNAVAILABLE -> {
        call.reject("Health Connect is not supported on this device.")
        false
      }
      else -> {
        call.reject("Unexpected SDK status.")
        false
      }
    }
  }

  @PluginMethod
  fun ensureInstalled(call: PluginCall) {
    val intent = installationIntent()
    startActivityForResult(call, intent, "ensureInstalledCallback")
  }

  @ActivityCallback
  fun ensureInstalledCallback(call: PluginCall, activityResult: ActivityResult) {
    val result =
            JSObject().apply { put("installed", activityResult.resultCode == Activity.RESULT_OK) }
    call.resolve(result)
  }

  @PluginMethod
  fun insertRecords(call: PluginCall) {

    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val records = call.getArray("records").toList<JSONObject>().map { it.toRecord() }
        val result = healthConnectClient!!.insertRecords(records)

        val res = JSObject().apply { put("recordIds", result.recordIdsList) }
        call.resolve(res)
      } catch (e: Exception) {
        call.reject("error inserting record", e)
      }
    }
  }

  @PluginMethod
  fun readRecord(call: PluginCall) {
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val type =
                call.getString("type").let {
                  RECORDS_TYPE_NAME_MAP[it]
                          ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                }

        val result =
                healthConnectClient!!.readRecord(
                        recordType = type,
                        recordId = requireNotNull(call.getString("recordId"))
                )

        val res = JSObject().apply { this.put("record", result.record.toJSONObject()) }
        call.resolve(res)
      } catch (e: Exception) {
        call.reject("error reading record", e)
      }
    }
  }

  @PluginMethod
  fun readRecords(call: PluginCall) {
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val type =
                call.getString("type").let {
                  RECORDS_TYPE_NAME_MAP[it]
                          ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                }
        val request =
                ReadRecordsRequest(
                        recordType = type,
                        timeRangeFilter = call.data.getLocalTimeRangeFilter("timeRangeFilter"),
                        dataOriginFilter = call.data.getDataOriginFilter("dataOriginFilter"),
                        ascendingOrder = call.getBoolean("ascendingOrder") ?: true,
                        pageSize = call.getInt("pageSize") ?: 1000,
                        pageToken = call.getString("pageToken"),
                )
        val result = healthConnectClient!!.readRecords(request)

        val res =
                JSObject().apply {
                  val records = result.records.map { it.toJSONObject() }.toJSONArray()
                  this.put("records", records)
                  this.put("pageToken", result.pageToken)
                }
        call.resolve(res)
      } catch (e: Exception) {
        call.reject("error reading record", e)
      }
    }
  }

  @PluginMethod
  fun aggregateGroupByDuration(call: PluginCall) {
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val type =
                call.getString("type").let {
                  AGGREGATE_TYPE_NAME_MAP[it]
                          ?: throw IllegalArgumentException("Unexpected AggregateType: $it")
                }

        val request =
                AggregateGroupByDurationRequest(
                        metrics = setOf(type),
                        timeRangeFilter = call.data.getTimeRangeFilter("timeRangeFilter"),
                        dataOriginFilter = call.data.getDataOriginFilter("dataOriginFilter"),
                        timeRangeSlicer =
                                call.data.getDurationTimeSlicer("durationTimeRangeSlicer"),
                )

        val result = healthConnectClient!!.aggregateGroupByDuration(request)

        val res =
                JSObject().apply {
                  val entries = result.map { it.toJSONObject(type) }.toJSONArray()
                  this.put("entries", entries)
                }

        call.resolve(res)
      } catch (e: Exception) {
        call.reject(e.localizedMessage + ' ' + e.message)
      }
    }
  }

  @PluginMethod
  fun aggregateGroupByPeriod(call: PluginCall) {
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val type =
                call.getString("type").let {
                  AGGREGATE_TYPE_NAME_MAP[it]
                          ?: throw IllegalArgumentException("Unexpected AggregateType: $it")
                }

        val request =
                AggregateGroupByPeriodRequest(
                        metrics = setOf(type),
                        timeRangeFilter = call.data.getLocalTimeRangeFilter("timeRangeFilter"),
                        dataOriginFilter = call.data.getDataOriginFilter("dataOriginFilter"),
                        timeRangeSlicer = call.data.getTimeRangeSlicer("timeRangeSlicer"),
                )

        val result = healthConnectClient!!.aggregateGroupByPeriod(request)

        val res =
                JSObject().apply {
                  val entries = result.map { it.toJSONObject(type) }.toJSONArray()
                  this.put("entries", entries)
                }

        call.resolve(res)
      } catch (e: Exception) {
        call.reject(e.localizedMessage + ' ' + e.message)
      }
    }
  }

  @PluginMethod
  fun aggregate(call: PluginCall) {
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val type =
                call.getString("type").let {
                  AGGREGATE_TYPE_NAME_MAP[it]
                          ?: throw IllegalArgumentException("Unexpected AggregateType: $it")
                }
        val timeRangeFilter = call.data.getLocalTimeRangeFilter("timeRangeFilter")

        val request =
                AggregateRequest(
                        metrics = setOf(type),
                        timeRangeFilter = timeRangeFilter,
                        dataOriginFilter = call.data.getDataOriginFilter("dataOriginFilter")
                )

        val result = healthConnectClient!!.aggregate(request)

        val res =
                JSObject().apply {
                  this.put("type", AGGREGATE_METRIC_NAME_MAP[type])
                  this.put("startTime", timeRangeFilter.localStartTime.toString())
                  this.put("endTime", timeRangeFilter.localEndTime.toString())
                  this.put("result", result[type])
                }
        call.resolve(res)
      } catch (e: Exception) {
        call.reject(e.localizedMessage + ' ' + e.message)
      }
    }
  }

  @PluginMethod
  fun getChangesToken(call: PluginCall) {
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val types =
                call.getArray("types")
                        .toList<String>()
                        .map {
                          RECORDS_TYPE_NAME_MAP[it]
                                  ?: throw IllegalArgumentException("Unexpected RecordType: $it")
                        }
                        .toSet()
        val request =
                ChangesTokenRequest(
                        recordTypes = types,
                        dataOriginFilters = call.data.getDataOriginFilter("dataOriginFilter"),
                )
        val token = healthConnectClient!!.getChangesToken(request)

        val res = JSObject().apply { this.put("token", token) }
        call.resolve(res)
      } catch (e: Exception) {
        call.reject("error getting change token", e)
      }
    }
  }

  @PluginMethod
  fun getChanges(call: PluginCall) {
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        var token = requireNotNull(call.getString("token"))
        val changes =
                flow {
                          do {
                            val result =
                                    healthConnectClient!!.getChanges(
                                            changesToken = token,
                                    )
                            emit(result.changes)
                            token = result.nextChangesToken
                          } while (result.hasMore)
                        }
                        .toList()
                        .flatten()

        val res =
                JSObject().apply {
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
    intent.data =
            Uri.parse("market://details")
                    .buildUpon()
                    .appendQueryParameter("id", "com.google.android.apps.healthdata")
                    .appendQueryParameter("url", "healthconnect://onboarding")
                    .build()
    intent.putExtra("overlay", true)
    intent.putExtra("callerId", context.packageName)
    return intent
  }

  @PluginMethod
  fun requestHealthPermissions(call: PluginCall) {
    try {
      val sdkStatus = HealthConnectClient.getSdkStatus(this.context)
      if (sdkStatus == HealthConnectClient.SDK_UNAVAILABLE) {
        val res =
                JSObject().apply {
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

      val readPermissions =
              call.getArray("read")
                      .toList<String>()
                      .map {
                        HealthPermission.getReadPermission(
                                recordType = RECORDS_TYPE_NAME_MAP[it]
                                                ?: throw IllegalArgumentException(
                                                        "Unexpected RecordType: $it"
                                                )
                        )
                      }
                      .toSet()
      val writePermissions =
              call.getArray("write")
                      .toList<String>()
                      .map {
                        HealthPermission.getWritePermission(
                                recordType = RECORDS_TYPE_NAME_MAP[it]
                                                ?: throw IllegalArgumentException(
                                                        "Unexpected RecordType: $it"
                                                )
                        )
                      }
                      .toSet()

      val intent = permissionContract.createIntent(this.context, readPermissions + writePermissions)

      startActivityForResult(call, intent, "handleRequestPermission")
    } catch (e: Exception) {
      call.reject("error requesting permissions", e)
    }
  }

  @ActivityCallback
  fun handleInstalled(call: PluginCall, result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      requestHealthPermissions(call)
    } else {
      val res =
              JSObject().apply {
                put("grantedPermissions", JSArray())
                put("hasAllPermissions", false)
              }
      call.resolve(res)
    }
  }

  @ActivityCallback
  fun handleRequestPermission(call: PluginCall, result: ActivityResult) {
    try {
      val reqReadPermissions =
              call.getArray("read").toList<String>().associateBy {
                HealthPermission.getReadPermission(
                        recordType = RECORDS_TYPE_NAME_MAP[it]
                                        ?: throw IllegalArgumentException(
                                                "Unexpected RecordType: $it"
                                        )
                )
              }
      val reqWritePermissions =
              call.getArray("write").toList<String>().associateBy {
                HealthPermission.getWritePermission(
                        recordType = RECORDS_TYPE_NAME_MAP[it]
                                        ?: throw IllegalArgumentException(
                                                "Unexpected RecordType: $it"
                                        )
                )
              }

      val grantedPermissions =
              permissionContract.parseResult(result.resultCode, result.data).toSet()
      val hasAllPermissions =
              grantedPermissions.containsAll(reqReadPermissions.keys + reqWritePermissions.keys)

      val grantedPermissionsResult =
              JSObject().apply {
                put(
                        "read",
                        JSArray(
                                reqReadPermissions
                                        .filterKeys { grantedPermissions.contains(it) }
                                        .values
                        )
                )
                put(
                        "write",
                        JSArray(
                                reqWritePermissions
                                        .filterKeys { grantedPermissions.contains(it) }
                                        .values
                        )
                )
              }

      val res =
              JSObject().apply {
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
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val reqReadPermissions =
                call.getArray("read").toList<String>().associateBy {
                  HealthPermission.getReadPermission(
                          recordType = RECORDS_TYPE_NAME_MAP[it]
                                          ?: throw IllegalArgumentException(
                                                  "Unexpected RecordType: $it"
                                          )
                  )
                }
        val reqWritePermissions =
                call.getArray("write").toList<String>().associateBy {
                  HealthPermission.getWritePermission(
                          recordType = RECORDS_TYPE_NAME_MAP[it]
                                          ?: throw IllegalArgumentException(
                                                  "Unexpected RecordType: $it"
                                          )
                  )
                }

        val grantedPermissions = healthConnectClient!!.permissionController.getGrantedPermissions()
        val hasAllPermissions =
                grantedPermissions.containsAll(reqReadPermissions.keys + reqWritePermissions.keys)

        val grantedPermissionsResult =
                JSObject().apply {
                  put(
                          "read",
                          JSArray(
                                  reqReadPermissions
                                          .filterKeys { grantedPermissions.contains(it) }
                                          .values
                          )
                  )
                  put(
                          "write",
                          JSArray(
                                  reqWritePermissions
                                          .filterKeys { grantedPermissions.contains(it) }
                                          .values
                          )
                  )
                }

        val res =
                JSObject().apply {
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
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        healthConnectClient!!.permissionController.revokeAllPermissions()
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

  private fun checkHealthPermission(call: PluginCall, permission: String) {
    if (!ensureClientOrReject(call)) return

    this.activity.lifecycleScope.launch {
      try {
        val grantedPermissions = healthConnectClient!!.permissionController.getGrantedPermissions()
        val hasPermission = grantedPermissions.contains(permission)
        val res = JSObject().apply { put("hasPermission", hasPermission) }
        call.resolve(res)
      } catch (e: Exception) {
        call.reject("error checking permission $permission", e)
      }
    }
  }

  @PluginMethod
  fun checkReadHealthDataHistoryPermission(call: PluginCall) {
    return this.checkHealthPermission(call, HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY)
  }

  @PluginMethod
  fun checkReadHealthDataInBackgroundPermission(call: PluginCall) {
    return this.checkHealthPermission(
            call,
            HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
    )
  }

  private fun requestHealthPermission(
          call: PluginCall,
          feature: Int,
          permission: String,
          resultPropertyName: String,
          callbackName: String
  ) {
    if (!ensureClientOrReject(call)) return

    if (healthConnectClient!!.features.getFeatureStatus(feature) !=
                    HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
    ) {
      val res = JSObject().apply { put(resultPropertyName, "NotSupported") }
      call.resolve(res)
      return
    }

    val requiredPermissions = setOf(permission)
    val intent = permissionContract.createIntent(this.context, requiredPermissions)

    startActivityForResult(call, intent, callbackName)
  }

  private fun handleRequestHealthPermissionResult(
          call: PluginCall,
          result: ActivityResult,
          requestedPermission: String,
          resultPropertyName: String
  ) {
    try {
      val grantedPermissions =
              permissionContract.parseResult(result.resultCode, result.data).toSet()

      if (requestedPermission in grantedPermissions) {
        val res = JSObject().apply { put(resultPropertyName, "Granted") }
        call.resolve(res)
      } else {
        val res = JSObject().apply { put(resultPropertyName, "Denied") }
        call.resolve(res)
      }
    } catch (e: Exception) {
      call.reject("error requesting permission $requestedPermission", e)
    }
  }

  @PluginMethod
  fun requestReadHealthDataHistoryPermission(call: PluginCall) {

    return this.requestHealthPermission(
            call,
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_HISTORY,
            HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY,
            "readHealthDataHistoryStatus",
            "handleReadHealthDataHistoryPermissionResult"
    )
  }

  @ActivityCallback
  private fun handleReadHealthDataHistoryPermissionResult(
          call: PluginCall,
          result: ActivityResult
  ) {
    return this.handleRequestHealthPermissionResult(
            call,
            result,
            HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY,
            "readHealthDataHistoryStatus",
    )
  }

  @PluginMethod
  fun requestReadHealthDataInBackgroundPermission(call: PluginCall) {
    return this.requestHealthPermission(
            call,
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND,
            HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
            "readHealthDataInBackgroundStatus",
            "handleReadHealthDataInBackgroundPermissionResult"
    )
  }

  @ActivityCallback
  private fun handleReadHealthDataInBackgroundPermissionResult(
          call: PluginCall,
          result: ActivityResult
  ) {
    return this.handleRequestHealthPermissionResult(
            call,
            result,
            HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
            "readHealthDataInBackgroundStatus",
    )
  }

  @PluginMethod
  fun openPlayStore(call: PluginCall) {
    val uri =
            Uri.parse(
                    "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"
            )
    val installIntent = Intent(Intent.ACTION_VIEW, uri)
    this.context.startActivity(installIntent)
    call.resolve()
  }
}
