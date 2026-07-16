export interface HealthConnectPlugin {
  checkAvailability(): Promise<{
    availability: HealthConnectAvailability;
  }>;
  ensureInstalled(): Promise<{
    installed: boolean;
  }>;
  insertRecords(payload: InsertRecordsPayload): Promise<{
    recordIds: string[];
  }>;
  readRecord(options: ReadRecordOptions): Promise<{
    record: StoredRecord;
  }>;
  readRecords(options: ReadRecordsOptions): Promise<{
    records: StoredRecord[];
    pageToken?: string;
  }>;
  getChangesToken(options: GetChangesTokenOptions): Promise<{
    token: string;
  }>;
  getChanges(options: GetChangesOptions): Promise<{
    changes: Change[];
    nextToken: string;
  }>;
  requestHealthPermissions(options: HealthPermissions): Promise<{
    grantedPermissions: HealthPermissions;
    hasAllPermissions: boolean;
  }>;
  checkHealthPermissions(options: HealthPermissions): Promise<{
    grantedPermissions: HealthPermissions;
    hasAllPermissions: boolean;
  }>;
  revokeHealthPermissions(): Promise<void>;
  openHealthConnectSetting(): Promise<void>;
  aggregateGroupByPeriod(options: AggregateGroupByPeriodOptions): Promise<AggregateByPeriodResult>;
  aggregateGroupByDuration(options: AggregateGroupByDurationOptions): Promise<AggregateByPeriodResult>;
  aggregate(options: AggregateOptions): Promise<AggregateResult>;
  checkReadHealthDataHistoryPermission(): Promise<{
    hasPermission: boolean;
  }>;
  requestReadHealthDataHistoryPermission(): Promise<{
    readHealthDataHistoryStatus: ReadHealthDataHistoryPermissionStatus;
  }>;
  checkReadHealthDataInBackgroundPermission(): Promise<{
    hasPermission: boolean;
  }>;
  requestReadHealthDataInBackgroundPermission(): Promise<{
    readHealthDataInBackgroundStatus: ReadHealthDataHistoryPermissionStatus;
  }>;
  openPlayStore(): Promise<void>;
  openHealthConnectSettings(): Promise<void>;
}
export type HealthConnectAvailability = 'Available' | 'NotInstalled' | 'NotSupported';
export type RecordType =
  | 'ActiveCaloriesBurned'
  | 'BasalBodyTemperature'
  | 'BasalMetabolicRate'
  | 'BloodGlucose'
  | 'BloodPressure'
  | 'HeartRate'
  | 'HeartRateVariabilityRmssd'
  | 'Height'
  | 'OxygenSaturation'
  | 'RestingHeartRate'
  | 'SleepSession'
  | 'Steps'
  | 'Vo2Max'
  | 'Weight';

export type InsertRecordsPayload = {
  records: Record[];
};

export type GetChangesTokenOptions = {
  types: RecordType[];
};

export type GetChangesOptions = {
  token: string;
};

export type HealthPermissions = {
  read: RecordType[];
  write: RecordType[];
};

export type ReadRecordOptions = {
  type: RecordType;
  recordId: string;
};

export type ReadRecordsOptions = {
  type: RecordType;
  timeRangeFilter: TimeRangeFilter;
  dataOriginFilter?: string[];
  ascendingOrder?: boolean;
  pageSize?: number;
  pageToken?: string;
};

export type AggregateGroupByPeriodOptions = {
  type: AggregateType;
  timeRangeFilter: LocalTimeRangeFilter;
  timeRangeSlicer: TimeRangeSlicer;
  dataOriginFilter?: string[];
};

export type AggregateGroupByDurationOptions = {
  type: AggregateType;
  timeRangeFilter: TimeRangeFilter;
  durationTimeRangeSlicer: DurationTimeRangeSlicer;
  dataOriginFilter?: string[];
};

export type AggregateOptions = {
  type: AggregateType;
  timeRangeFilter: LocalTimeRangeFilter;
  dataOriginFilter?: string[];
};

type RecordBase = {
  metadata: RecordMetadata;
};

export type StoredRecord<T extends Record = Record> = RecordBase & T;

export type ActiveCaloriesBurnedRecord = {
  type: 'ActiveCaloriesBurned';
  startTime: string;
  startZoneOffset?: string;
  endTime: string;
  endZoneOffset?: string;
  energy: Energy;
};

export type BasalBodyTemperatureRecord = {
  type: 'BasalBodyTemperature';
  time: string;
  zoneOffset?: string;
  temperature: Temperature;
  measurementLocation:
    | 'unknown'
    | 'armpit'
    | 'finger'
    | 'forehead'
    | 'mouth'
    | 'rectum'
    | 'temporal_artery'
    | 'toe'
    | 'ear'
    | 'wrist'
    | 'vagina';
};

export type BasalMetabolicRateRecord = {
  type: 'BasalMetabolicRate';
  time: string;
  zoneOffset?: string;
  basalMetabolicRate: Power;
};

export type BloodGlucoseRecord = {
  type: 'BloodGlucose';
  time: string;
  zoneOffset?: string;
  level: BloodGlucose;
  specimenSource: 'unknown' | 'interstitial_fluid' | 'plasma' | 'serum' | 'tears' | 'whole_blood';
  mealType: 'unknown' | 'breakfast' | 'lunch' | 'dinner' | 'snack';
  relationToMeal: 'unknown' | 'general' | 'fasting' | 'before_meal' | 'after_meal';
};

export type BloodPressureRecord = {
  type: 'BloodPressure';
  time: string;
  zoneOffset?: string;
  systolic: Pressure;
  diastolic: Pressure;
  bodyPosition: 'unknown' | 'standing_up' | 'sitting_down' | 'lying_down' | 'reclining';
  measurementLocation: 'unknown' | 'left_wrist' | 'right_wrist' | 'left_upper_arm' | 'right_upper_arm';
};

export type HeightRecord = {
  type: 'Height';
  time: string;
  zoneOffset?: string;
  height: Length;
};

export type HeartRateRecord = {
  type: 'HeartRate';
  startTime: string;
  startZoneOffset?: string;
  endTime: string;
  endZoneOffset?: string;
  samples: HeartRateSample[];
};

export type HeartRateVariabilityRmssdRecord = {
  type: 'HeartRateVariabilityRmssd';
  time: string;
  zoneOffset?: string;
  heartRateVariabilityMillis: number;
};

export type SleepSessionRecord = {
  type: 'SleepSession';
  startTime: string;
  startZoneOffset?: string;
  endTime: string;
  endZoneOffset?: string;
  title?: string;
  notes?: string;
  stages: SleepSessionStage[];
};

export type StepsRecord = {
  type: 'Steps';
  startTime: string;
  startZoneOffset?: string;
  endTime: string;
  endZoneOffset?: string;
  count: number;
};

export type Vo2MaxRecord = {
  type: 'Vo2Max';
  time: string;
  zoneOffset?: string;
  vo2MillilitersPerMinuteKilogram: number;
  measurementMethod:
    | 'metabolic_cart'
    | 'heart_rate_ratio'
    | 'cooper_test'
    | 'multistage_fitness_test'
    | 'rockport_fitness_test'
    | 'other';
};

export type WeightRecord = {
  type: 'Weight';
  time: string;
  zoneOffset?: string;
  weight: Mass;
};

// Union type som samlar alla records
export type Record =
  | ActiveCaloriesBurnedRecord
  | BasalBodyTemperatureRecord
  | BasalMetabolicRateRecord
  | BloodGlucoseRecord
  | BloodPressureRecord
  | HeightRecord
  | HeartRateRecord
  | HeartRateVariabilityRmssdRecord
  | SleepSessionRecord
  | StepsRecord
  | Vo2MaxRecord
  | WeightRecord;

export type RecordMetadata = {
  id: string;
  clientRecordId?: string;
  clientRecordVersion: number;
  lastModifiedTime: string;
  dataOrigin: string;
};

export type AggregateType =
  | 'ActiveCaloriesTotal'
  | 'DistanceTotal'
  | 'ElevationGainedTotal'
  | 'FloorsClimbedTotal'
  | 'HeartBpmAvg'
  | 'HeartBpmMin'
  | 'HeartBpmMax'
  | 'HeartMeasurementsCount'
  | 'HydrationVolumeTotal'
  | 'PowerAvg'
  | 'PowerMin'
  | 'PowerMax'
  | 'SleepSessionDurationTotal'
  | 'StepsCountTotal'
  | 'TotalCaloriesBurnedTotal'
  | 'WheelchairPushesCountTotal';

export type UpsertChange = {
  type: 'Upsert';
  record: Record;
};

export type DeleteChange = {
  type: 'Delete';
  recordId: string;
};

export type Change = UpsertChange | DeleteChange;

/**
 * Filters records by their own local (zone-agnostic) wall-clock time, e.g. for calendar-day bucketing
 * via `AggregateGroupByPeriodOptions`.
 *
 * `localTime`/`localStartTime`/`localEndTime` must be ISO-8601 instant strings (e.g. the output of
 * `Date.prototype.toISOString()`). They are converted to local wall-clock digits using `zoneId` before
 * being compared against each record's own local time, so `zoneId` should be the IANA time zone id
 * (e.g. `"Europe/Stockholm"`) of whatever calendar day you actually want - not necessarily the device's
 * current time zone. Contrast with `TimeRangeFilter`, whose `*UTC` fields are matched as plain
 * zone-agnostic instants with no local-time conversion.
 */
export type LocalTimeRangeFilter =
  | {
      type: 'before' | 'after';
      localTime: string;
      zoneId: string;
    }
  | {
      type: 'between';
      localStartTime: string;
      localEndTime: string;
      zoneId: string;
    };

export type TimeRangeFilter =
  | {
      type: 'before' | 'after';
      timeUTC: string;
    }
  | {
      type: 'between';
      startTimeUTC: string;
      endTimeUTC: string;
    };

export type TimeRangeSlicer = {
  period: 'days' | 'months' | 'weeks' | 'years';
  count: number;
};

export type DurationTimeRangeSlicer = {
  duration: 'days' | 'hours' | 'minutes' | 'seconds' | 'millis';
  count: number;
};

export type HeartRateSample = {
  time: string;
  beatsPerMinute: number;
};
export type SleepSessionStage = {
  startTime: string;
  endTime: string;
  stage: 'awake' | 'sleeping' | 'out_of_bed' | 'light' | 'deep' | 'rem' | 'awake_in_bed' | 'unknown';
};
export type Energy = {
  unit: 'calories' | 'kilocalories' | 'joules' | 'kilojoules';
  value: number;
};
export type Temperature = {
  unit: 'celsius' | 'fahrenheit';
  value: number;
};
export type Power = {
  unit: 'kilocaloriesPerDay' | 'watts';
  value: number;
};
export type Pressure = {
  unit: 'millimetersOfMercury';
  value: number;
};
export type Length = {
  unit: 'meter' | 'kilometer' | 'mile' | 'inch' | 'feet';
  value: number;
};
export type Mass = {
  unit: 'gram' | 'kilogram' | 'milligram' | 'microgram' | 'ounce' | 'pound';
  value: number;
};
export type BloodGlucose = {
  unit: 'milligramsPerDeciliter' | 'millimolesPerLiter';
  value: number;
};

export type ReadHealthDataHistoryPermissionStatus = 'NotSupported' | 'Granted' | 'Denied';

export type AggregateByPeriodResult = {
  entries: AggregateByPeriodEntry[];
};

export type AggregateByPeriodEntry = {
  type: AggregateType;
  startTime: string;
  endTime: string;
  result: number;
};

export type AggregateResult = {
  type: AggregateType;
  startTime: string;
  endTime: string;
  result: number;
};

export {};
