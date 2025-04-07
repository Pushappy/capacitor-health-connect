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
  aggregateGroupByPeriod(options: AggregateGroupByPeriodOptions): Promise<any>;
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
  timeRangeFilter: TimeRangeFilter;
  timeRangeSlicer: TimeRangeSlicer;
  dataOriginFilter?: string[];
}

type RecordBase = {
  metadata: RecordMetadata;
};
export type StoredRecord = RecordBase & Record;
export type Record =
  | {
      type: 'ActiveCaloriesBurned';
      startTime: string;
      startZoneOffset?: string;
      endTime: string;
      endZoneOffset?: string;
      energy: Energy;
    }
  | {
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
    }
  | {
      type: 'BasalMetabolicRate';
      time: string;
      zoneOffset?: string;
      basalMetabolicRate: Power;
    }
  | {
      type: 'BloodGlucose';
      time: string;
      zoneOffset?: string;
      level: BloodGlucose;
      specimenSource:
        | 'unknown'
        | 'interstitial_fluid'
        | 'capillary_blood'
        | 'plasma'
        | 'serum'
        | 'tears'
        | 'whole_blood';
      mealType: 'unknown' | 'breakfast' | 'lunch' | 'dinner' | 'snack';
      relationToMeal: 'unknown' | 'general' | 'fasting' | 'before_meal' | 'after_meal';
    }
  | {
      type: 'BloodPressure';
      time: string;
      zoneOffset?: string;
      systolic: Pressure;
      diastolic: Pressure;
      bodyPosition: 'unknown' | 'standing_up' | 'sitting_down' | 'lying_down' | 'reclining';
      measurementLocation: 'unknown' | 'left_wrist' | 'right_wrist' | 'left_upper_arm' | 'right_upper_arm';
    }
  | {
      type: 'Height';
      time: string;
      zoneOffset?: string;
      height: Length;
    }
  | {
      type: 'HeartRate';
      startTime: string;
      startZoneOffset?: string;
      endTime: string;
      endZoneOffset?: string;
      samples: HeartRateSample[]
    }
  | {
      type: 'HeartRateVariabilityRmssd';
      time: string;
      zoneOffset?: string;
      heartRateVariabilityMillis: number;
    }
  | {
      type: 'SleepSession';
      startTime: string;
      startZoneOffset?: string;
      endTime: string;
      endZoneOffset?: string;
      title?: string;
      notes?: string;
      stages: SleepSessionStage[];
    }
  | {
      type: 'Steps';
      startTime: string;
      startZoneOffset?: string;
      endTime: string;
      endZoneOffset?: string;
      count: number;
    }
  | {
      type: 'Vo2Max';
      time: string;
      zoneOffset?: string;
      vo2MillilitersPerMinuteKilogram: number;
      measurementMethod: 'metabolic_cart' | 'heart_rate_ratio' | 'cooper_test' | 'multistage_fitness_test' | 'rockport_fitness_test' | 'other';
    }
  | {
      type: 'Weight';
      time: string;
      zoneOffset?: string;
      weight: Mass;
    };
export type RecordMetadata = {
  id: string;
  clientRecordId?: string;
  clientRecordVersion: number;
  lastModifiedTime: string;
  dataOrigin: string;
};

export type AggregateType =
  'ActiveCaloriesTotal' |
  'DistanceTotal' |
  'ElevationGainedTotal' |
  'FloorsClimbedTotal' |
  'HeartBpmAvg' |
  'HeartBpmMin' |
  'HeartBpmMax' |
  'HeartMeasurementsCount' |
  'HydrationVolumeTotal' |
  'PowerAvg' |
  'PowerMin' |
  'PowerMax' |
  'SleepSessionDurationTotal' |
  'StepsCountTotal' |
  'TotalCaloriesBurnedTotal' |
  'WheelchairPushesCountTotal'

export type Change =
  | {
      type: 'Upsert';
      record: Record;
    }
  | {
      type: 'Delete';
      recordId: string;
    };
export type TimeRangeFilter =
  | {
      type: 'before' | 'after';
      time: string;
    }
  | {
      type: 'between';
      startTime: string;
      endTime: string;
    };

export type TimeRangeSlicer = {
  period: 'days' | 'months' | 'weeks' | 'years',
  count: number
}

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
export {};