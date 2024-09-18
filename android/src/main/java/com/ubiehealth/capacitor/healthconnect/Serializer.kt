package com.ubiehealth.capacitor.healthconnect

import android.health.connect.datatypes.AggregationType
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.changes.DeletionChange
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_INT_TO_STRING_MAP
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_STRING_TO_INT_MAP
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.*
import com.getcapacitor.JSObject
import org.json.JSONArray
import org.json.JSONObject
import java.lang.RuntimeException
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset
import kotlin.reflect.KClass

// We need to be in control of type <-> string mappings since we also do the ts typing
// and the internal mapping might (it already has in the past) change.
val RECORDS_TYPE_NAME_MAP: Map<String, KClass<out Record>> =
    mapOf(
        "ActiveCaloriesBurned" to ActiveCaloriesBurnedRecord::class,
        "ExerciseSession" to ExerciseSessionRecord::class,
        "BasalBodyTemperature" to BasalBodyTemperatureRecord::class,
        "BasalMetabolicRate" to BasalMetabolicRateRecord::class,
        "BloodGlucose" to BloodGlucoseRecord::class,
        "BloodPressure" to BloodPressureRecord::class,
        "BodyFat" to BodyFatRecord::class,
        "BodyTemperature" to BodyTemperatureRecord::class,
        "BodyWaterMass" to BodyWaterMassRecord::class,
        "BoneMass" to BoneMassRecord::class,
        "CervicalMucus" to CervicalMucusRecord::class,
        "CyclingPedalingCadence" to
                CyclingPedalingCadenceRecord::class,
        "Distance" to DistanceRecord::class,
        "ElevationGained" to ElevationGainedRecord::class,
        "FloorsClimbed" to FloorsClimbedRecord::class,
        "HeartRate" to HeartRateRecord::class,
        "HeartRateVariabilityRmssd" to HeartRateVariabilityRmssdRecord::class,
        "Height" to HeightRecord::class,
        "Hydration" to HydrationRecord::class,
        "LeanBodyMass" to LeanBodyMassRecord::class,
        "Menstruation" to MenstruationFlowRecord::class,
        "MenstruationPeriod" to MenstruationPeriodRecord::class,
        "Nutrition" to NutritionRecord::class,
        "OvulationTest" to OvulationTestRecord::class,
        "OxygenSaturation" to OxygenSaturationRecord::class,
        "Power" to PowerRecord::class,
        "RespiratoryRate" to RespiratoryRateRecord::class,
        "RestingHeartRate" to RestingHeartRateRecord::class,
        "SexualActivity" to SexualActivityRecord::class,
        "SleepSession" to SleepSessionRecord::class,
        "SleepStage" to SleepStageRecord::class,
        "Speed" to SpeedRecord::class,
        "IntermenstrualBleeding" to IntermenstrualBleedingRecord::class,
        "Steps" to StepsRecord::class,
        "StepsCadence" to StepsCadenceRecord::class,
        "TotalCaloriesBurned" to TotalCaloriesBurnedRecord::class,
        "Vo2Max" to Vo2MaxRecord::class,
        "WheelchairPushes" to WheelchairPushesRecord::class,
        "Weight" to WeightRecord::class,
    )

val RECORDS_CLASS_NAME_MAP: Map<KClass<out Record>, String> =
    RECORDS_TYPE_NAME_MAP.entries.associate { it.value to it.key }

val AGGREGATE_TYPE_NAME_MAP: Map<String, AggregateMetric<*>> =
        //TODO: Nutrition
        mapOf(
                "ActiveCaloriesTotal" to ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                "DistanceTotal" to DistanceRecord.DISTANCE_TOTAL,
                "ElevationGainedTotal" to ElevationGainedRecord.ELEVATION_GAINED_TOTAL,
                "FloorsClimbedTotal" to FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL,
                "HeartBpmAvg" to HeartRateRecord.BPM_AVG,
                "HeartBpmMin" to HeartRateRecord.BPM_MIN,
                "HeartBpmMax" to HeartRateRecord.BPM_MAX,
                "HeartMeasurementsCount" to HeartRateRecord.MEASUREMENTS_COUNT,
                "HydrationVolumeTotal" to HydrationRecord.VOLUME_TOTAL,
                "PowerAvg" to PowerRecord.POWER_AVG,
                "PowerMin" to PowerRecord.POWER_MIN,
                "PowerMax" to PowerRecord.POWER_MAX,
                "SleepSessionDurationTotal" to SleepSessionRecord.SLEEP_DURATION_TOTAL,
                "StepsCountTotal" to StepsRecord.COUNT_TOTAL,
                "TotalCaloriesBurnedTotal" to TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                "WheelchairPushesCountTotal" to WheelchairPushesRecord.COUNT_TOTAL,
        )

val AGGREGATE_METRIC_NAME_MAP: Map<AggregateMetric<*>, String> =
        AGGREGATE_TYPE_NAME_MAP.entries.associate { it.value to it.key }

internal fun <T> JSONArray.toList(): List<T> {
    return (0 until this.length()).map {
        @Suppress("UNCHECKED_CAST")
        this.get(it) as T
    }
}

internal fun <T> List<T>.toJSONArray(): JSONArray {
    return JSONArray(this)
}

internal fun JSONObject.toRecord(): Record {
    return when (val type = this.get("type")) {
        "ActiveCaloriesBurned" -> ActiveCaloriesBurnedRecord(
            startTime = this.getInstant("startTime"),
            startZoneOffset = this.getZoneOffsetOrNull("startZoneOffset"),
            endTime = this.getInstant("endTime"),
            endZoneOffset = this.getZoneOffsetOrNull("endZoneOffset"),
            energy = this.getEnergy("energy"),
        )
        "BasalBodyTemperature" -> BasalBodyTemperatureRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            temperature = this.getTemperature("temperature"),
            measurementLocation = this.getBodyTemperatureMeasurementLocationInt("measurementLocation"),
        )
        "BasalMetabolicRate" -> BasalMetabolicRateRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            basalMetabolicRate = this.getPower("basalMetabolicRate"),
        )
        "BloodGlucose" -> BloodGlucoseRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            level = this.getBloodGlucose("level"),
            specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_STRING_TO_INT_MAP
                .getOrDefault(this.getString("specimenSource"), BloodGlucoseRecord.SPECIMEN_SOURCE_UNKNOWN),
            mealType = MealType.MEAL_TYPE_STRING_TO_INT_MAP
                .getOrDefault(this.getString("mealType"), MealType.MEAL_TYPE_UNKNOWN),
            relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_STRING_TO_INT_MAP
                .getOrDefault(this.getString("relationToMeal"), BloodGlucoseRecord.RELATION_TO_MEAL_UNKNOWN),
        )
        "BloodPressure" -> BloodPressureRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            systolic = this.getPressure("systolic"),
            diastolic = this.getPressure("diastolic"),
            bodyPosition = BloodPressureRecord.BODY_POSITION_STRING_TO_INT_MAP
                .getOrDefault(this.getString("bodyPosition"), BloodPressureRecord.BODY_POSITION_UNKNOWN),
            measurementLocation = BloodPressureRecord.MEASUREMENT_LOCATION_STRING_TO_INT_MAP
                .getOrDefault(this.getString("measurementLocation"), BloodPressureRecord.MEASUREMENT_LOCATION_UNKNOWN),
        )
        "HeartRate" -> HeartRateRecord(
            startTime = this.getInstant("startTime"),
            startZoneOffset = this.getZoneOffsetOrNull("startZoneOffset"),
            endTime = this.getInstant("endTime"),
            endZoneOffset = this.getZoneOffsetOrNull("endZoneOffset"),
            samples = this.getJSONArray("samples").toList<JSONObject>().map { it.getHearRateRecordSample() }
        )
        "HeartRateVariabilityRmssd" -> HeartRateVariabilityRmssdRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            heartRateVariabilityMillis = this.getDouble("heartRateVariabilityMillis")
        )
        "Height" -> HeightRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            height = this.getLength("height"),
        )
        "OxygenSaturation" -> OxygenSaturationRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            percentage = Percentage(this.getDouble("percentage"))
        )
        "RestingHeartRate" -> RestingHeartRateRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            beatsPerMinute = this.getLong("beatsPerMinute")
        )
        "SleepSession" -> SleepSessionRecord(
            startTime = this.getInstant("startTime"),
            startZoneOffset = this.getZoneOffsetOrNull("startZoneOffset"),
            endTime = this.getInstant("endTime"),
            endZoneOffset = this.getZoneOffsetOrNull("endZoneOffset"),
            title = this.getStringOrNull("title"),
            notes = this.getStringOrNull("notes"),
            stages = this.getJSONArray("stages").toList<JSONObject>().map { it.getSleepSessionRecordStage() }
        )
        "Steps" -> StepsRecord(
            startTime = this.getInstant("startTime"),
            startZoneOffset = this.getZoneOffsetOrNull("startZoneOffset"),
            endTime = this.getInstant("endTime"),
            endZoneOffset = this.getZoneOffsetOrNull("endZoneOffset"),
            count = this.getLong("count"),
        )
        "Vo2Max" -> Vo2MaxRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            vo2MillilitersPerMinuteKilogram = this.getDouble("vo2MillilitersPerMinuteKilogram"),
            measurementMethod = Vo2MaxRecord.MEASUREMENT_METHOD_STRING_TO_INT_MAP.getOrDefault(this.getString("measurementMethod"), Vo2MaxRecord.MEASUREMENT_METHOD_OTHER)
        )
        "Weight" -> WeightRecord(
            time = this.getInstant("time"),
            zoneOffset = this.getZoneOffsetOrNull("zoneOffset"),
            weight = this.getMass("weight"),
        )
        else -> throw IllegalArgumentException("Unexpected record type: $type")
    }
}

internal fun Record.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("type", RECORDS_CLASS_NAME_MAP[this::class])
        obj.put("metadata", this.metadata.toJSONObject())

        when (this) {
            is ActiveCaloriesBurnedRecord -> {
                obj.put("startTime", this.startTime)
                obj.put("startZoneOffset", this.startZoneOffset?.toJSONValue())
                obj.put("endTime", this.endTime)
                obj.put("endZoneOffset", this.endZoneOffset?.toJSONValue())
                obj.put("energy", this.energy.toJSONObject())
            }
            is BasalBodyTemperatureRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("temperature", this.temperature.toJSONObject())
                obj.put("measurementLocation", this.measurementLocation.toBodyTemperatureMeasurementLocationString())
            }
            is BasalMetabolicRateRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("basalMetabolicRate", this.basalMetabolicRate.toJSONObject())
            }
            is BloodGlucoseRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("level", this.level.toJSONObject())
                obj.put("specimenSource", BloodGlucoseRecord.SPECIMEN_SOURCE_INT_TO_STRING_MAP.getOrDefault(this.specimenSource, "unknown"))
                obj.put("mealType", MealType.MEAL_TYPE_INT_TO_STRING_MAP.getOrDefault(this.mealType, "unknown"))
                obj.put("relationToMeal", BloodGlucoseRecord.RELATION_TO_MEAL_INT_TO_STRING_MAP.getOrDefault(this.relationToMeal, "unknown"))
            }
            is BloodPressureRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("systolic", this.systolic.toJSONObject())
                obj.put("diastolic", this.diastolic.toJSONObject())
                obj.put("bodyPosition", BloodPressureRecord.BODY_POSITION_INT_TO_STRING_MAP.getOrDefault(this.bodyPosition, "unknown"))
                obj.put("measurementLocation", BloodPressureRecord.MEASUREMENT_LOCATION_INT_TO_STRING_MAP.getOrDefault(this.measurementLocation, "unknown"))
            }
            is HeartRateRecord -> {
                obj.put("startTime", this.startTime)
                obj.put("startZoneOffset", this.startZoneOffset?.toJSONValue())
                obj.put("endTime", this.endTime)
                obj.put("endZoneOffset", this.endZoneOffset?.toJSONValue())
                obj.put("samples", this.samples.map { it.toJSONObject() }.toJSONArray())
            }
            is HeartRateVariabilityRmssdRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("heartRateVariabilityMillis", this.heartRateVariabilityMillis)
            }
            is HeightRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("height", this.height.toJSONObject())
            }
            is OxygenSaturationRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("percentage", this.percentage.value)
            }
            is RestingHeartRateRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("beatsPerMinute", this.beatsPerMinute)
            }
            is SleepSessionRecord -> {
                obj.put("startTime", this.startTime)
                obj.put("startZoneOffset", this.startZoneOffset?.toJSONValue())
                obj.put("endTime", this.endTime)
                obj.put("endZoneOffset", this.endZoneOffset?.toJSONValue())
                obj.put("title", this.title)
                obj.put("notes", this.notes)
                obj.put("stages", this.stages.map { it.toJSONObject() }.toJSONArray())
            }
            is StepsRecord -> {
                obj.put("startTime", this.startTime)
                obj.put("startZoneOffset", this.startZoneOffset?.toJSONValue())
                obj.put("endTime", this.endTime)
                obj.put("endZoneOffset", this.endZoneOffset?.toJSONValue())
                obj.put("count", this.count)
            }
            is Vo2MaxRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("vo2MillilitersPerMinuteKilogram", this.vo2MillilitersPerMinuteKilogram)
                obj.put("measurementMethod", Vo2MaxRecord.MEASUREMENT_METHOD_INT_TO_STRING_MAP.getOrDefault(this.measurementMethod, "other"))
            }
            is WeightRecord -> {
                obj.put("time", this.time)
                obj.put("zoneOffset", this.zoneOffset?.toJSONValue())
                obj.put("weight", this.weight.toJSONObject())
            }
            else -> throw IllegalArgumentException("Unexpected record class: $${this::class.qualifiedName}")
        }
    }
}

internal fun AggregationResult.toJSONObject(metric: AggregateMetric<*>): JSONObject {
    return JSONObject().also { obj ->
        obj.putOpt("count", this.get(metric))
    }
}
internal fun AggregationResultGroupedByPeriod.toJSONObject(metric: AggregateMetric<*>): JSONObject {
    return JSONObject().also { obj ->
        obj.put("type", AGGREGATE_METRIC_NAME_MAP[metric])
        obj.put("startTime", this.startTime)
        obj.put("endTime", this.endTime)
        obj.put("result", this.result[metric] ?: 0)
    }
}

internal fun Metadata.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("id", this.id)
        obj.put("clientRecordId", this.clientRecordId)
        obj.put("clientRecordVersion", this.clientRecordVersion)
        obj.put("lastModifiedTime", this.lastModifiedTime)
        obj.put("dataOrigin", this.dataOrigin.packageName)
    }
}

internal fun Change.toJSObject(): JSObject {
    return JSObject().also { obj ->
        when (this) {
            is UpsertionChange -> {
                obj.put("type", "Upsert")
                obj.put("record", this.record.toJSONObject())
            }

            is DeletionChange -> {
                obj.put("type", "Delete")
                obj.put("recordId", this.recordId)
            }
        }
    }
}

internal fun JSONObject.getStringOrNull(name: String): String? {
    return if (!this.isNull(name))
        this.getString(name)
    else
        null
}

internal fun JSONObject.getInstant(name: String): Instant {
    return Instant.parse(this.getString(name))
}

internal fun JSONObject.getZoneOffsetOrNull(name: String): ZoneOffset? {
    return if (!this.isNull(name))
        ZoneOffset.of(this.getString(name))
    else
        null
}

internal fun ZoneOffset.toJSONValue(): String {
    return this.id
}

internal fun HeartRateRecord.Sample.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("time", this.time)
        obj.put("beatsPerMinute", this.beatsPerMinute)
    }
}

internal fun JSONObject.getHearRateRecordSample(): HeartRateRecord.Sample {
    val time = this.getInstant("time")
    val bpm = this.getLong("beatsPerMinute")
    return HeartRateRecord.Sample(time, bpm)
}

internal fun SleepSessionRecord.Stage.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("startTime", this.startTime)
        obj.put("endTime", this.endTime)

        obj.put("stage", SleepSessionRecord.STAGE_TYPE_INT_TO_STRING_MAP.getOrDefault(this.stage, "unknown"))
    }
}

internal fun JSONObject.getSleepSessionRecordStage(): SleepSessionRecord.Stage {
    val starTime = this.getInstant("startTime")
    val endTime = this.getInstant("endTime")
    val stage = SleepSessionRecord.STAGE_TYPE_STRING_TO_INT_MAP.getOrDefault(this.getString("stage"), SleepSessionRecord.STAGE_TYPE_UNKNOWN)
    return SleepSessionRecord.Stage(starTime,endTime, stage)
}

internal fun JSONObject.getLength(name: String): Length {
    val obj = requireNotNull(this.getJSONObject(name))
    val unit = obj.getString("unit")
    val value = obj.getDouble("value")
    return when (unit) {
        "meter" -> Length.meters(value)
        "kilometer" -> Length.kilometers(value)
        "mile" -> Length.miles(value)
        "inch" -> Length.inches(value)
        "feet" -> Length.feet(value)
        else -> throw IllegalArgumentException("Unexpected mass unit: $unit")
    }
}

internal fun Length.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("unit", "meter") // TODO: support other units
        obj.put("value", this.inMeters)
    }
}

internal fun JSONObject.getMass(name: String): Mass {
    val obj = requireNotNull(this.getJSONObject(name))
    val unit = obj.getString("unit")
    val value = obj.getDouble("value")
    return when (unit) {
        "gram" -> Mass.grams(value)
        "kilogram" -> Mass.kilograms(value)
        "milligram" -> Mass.milligrams(value)
        "microgram" -> Mass.micrograms(value)
        "ounce" -> Mass.ounces(value)
        "pound" -> Mass.pounds(value)
        else -> throw IllegalArgumentException("Unexpected mass unit: $unit")
    }
}

internal fun Mass.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("unit", "gram") // TODO: support other units
        obj.put("value", this.inGrams)
    }
}

internal fun BloodGlucose.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("unit", "milligramsPerDeciliter") // TODO: support other units
        obj.put("value", this.inMilligramsPerDeciliter)
    }
}

internal fun JSONObject.getBloodGlucose(name: String): BloodGlucose {
    val obj = requireNotNull(this.getJSONObject(name))

    val value = obj.getDouble("value")
    return when (val unit = obj.getString("unit")) {
        "milligramsPerDeciliter" -> BloodGlucose.milligramsPerDeciliter(value)
        "millimolesPerLiter" -> BloodGlucose.millimolesPerLiter(value)
        else -> throw RuntimeException("Invalid BloodGlucose unit: $unit")
    }
}

internal fun Energy.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("unit", "calories") // TODO: support other units
        obj.put("value", this.inCalories)
    }
}

internal fun JSONObject.getEnergy(name: String): Energy {
    val obj = requireNotNull(this.getJSONObject(name))

    val value = obj.getDouble("value")
    return when (val unit = obj.getString("unit")) {
        "calories" -> Energy.calories(value)
        "kilocalories" -> Energy.kilocalories(value)
        "joules" -> Energy.joules(value)
        "kilojoules" -> Energy.kilojoules(value)
        else -> throw RuntimeException("Invalid Energy unit: $unit")
    }
}

internal fun Temperature.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("unit", "celsius") // TODO: support other units
        obj.put("value", this.inCelsius)
    }
}

internal fun JSONObject.getTemperature(name: String): Temperature {
    val obj = requireNotNull(this.getJSONObject(name))

    val value = obj.getDouble("value")
    return when (val unit = obj.getString("unit")) {
        "celsius" -> Temperature.celsius(value)
        "fahrenheit" -> Temperature.fahrenheit(value)
        else -> throw RuntimeException("Invalid Temperature unit: $unit")
    }
}

internal fun Int.toBodyTemperatureMeasurementLocationString(): String {
    return MEASUREMENT_LOCATION_INT_TO_STRING_MAP.getOrDefault(this, "unknown")
}

internal fun JSONObject.getBodyTemperatureMeasurementLocationInt(name: String): Int {
    val str = requireNotNull(this.getString(name))
    return MEASUREMENT_LOCATION_STRING_TO_INT_MAP.getOrDefault(str, BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_UNKNOWN)
}

internal fun Power.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("unit", "kilocaloriesPerDay") // TODO: support other units
        obj.put("value", this.inKilocaloriesPerDay)
    }
}

internal fun JSONObject.getPower(name: String): Power {
    val obj = requireNotNull(this.getJSONObject(name))

    val value = obj.getDouble("value")
    return when (val unit = obj.getString("unit")) {
        "kilocaloriesPerDay" -> Power.kilocaloriesPerDay(value)
        "watts" -> Power.watts(value)
        else -> throw RuntimeException("Invalid Power unit: $unit")
    }
}

internal fun Pressure.toJSONObject(): JSONObject {
    return JSONObject().also { obj ->
        obj.put("unit", "millimetersOfMercury") // TODO: support other units
        obj.put("value", this.inMillimetersOfMercury)
    }
}

internal fun JSONObject.getPressure(name: String): Pressure {
    val obj = requireNotNull(this.getJSONObject(name))

    val value = obj.getDouble("value")
    return when (val unit = obj.getString("unit")) {
        "millimetersOfMercury" -> Pressure.millimetersOfMercury(value)
        else -> throw RuntimeException("Invalid Pressure unit: $unit")
    }
}

internal fun JSONObject.getTimeRangeFilter(name: String): TimeRangeFilter {
    val obj = requireNotNull(this.getJSONObject(name))
    return when (val type = obj.getString("type")) {
        "before" -> TimeRangeFilter.before(obj.getInstant("time"))
        "after" -> TimeRangeFilter.after(obj.getInstant("time"))
        "between" -> TimeRangeFilter.between(obj.getInstant("startTime"), obj.getInstant("endTime"))
        else -> throw IllegalArgumentException("Unexpected TimeRange type: $type")
    }
}

internal fun JSONObject.getLocalTimeRangeFilter(name: String): TimeRangeFilter {
    val obj = requireNotNull(this.getJSONObject(name))
    return when (val type = obj.getString("type")) {
        "before" -> TimeRangeFilter.before(LocalDateTime.parse(obj.getString("time")))
        "after" -> TimeRangeFilter.after(LocalDateTime.parse(obj.getString("time")))
        "between" -> TimeRangeFilter.between(obj.getInstant("startTime"), obj.getInstant("endTime"))
        else -> throw IllegalArgumentException("Unexpected TimeRange type: $type")
    }
}

internal fun JSONObject.getTimeRangeSlicer(name: String): Period {
    val obj = requireNotNull(this.getJSONObject(name))

    return when (val period = obj.getString("period")) {
        "months" -> Period.ofMonths(obj.getInt("count"))
        "days" -> Period.ofDays(obj.getInt("count"))
        "weeks" -> Period.ofWeeks(obj.getInt("count"))
        "years" -> Period.ofYears(obj.getInt("count"))
        else -> throw IllegalArgumentException("Unexpected TimeRangePeriod type: $period")
    }
}

internal fun JSObject.getDataOriginFilter(name: String): Set<DataOrigin> {
    return this.optJSONArray(name)?.toList<String>()?.map { DataOrigin(it) }?.toSet() ?: emptySet()
}
