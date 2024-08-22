export interface HealthConnectPlugin {
  checkAvailability(): Promise<{
    availability: HealthConnectAvailability;
  }>;
  insertRecords(options: { records: Record[] }): Promise<{
    recordIds: string[];
  }>;
  readRecord(options: { type: RecordType; recordId: string }): Promise<{
    record: StoredRecord;
  }>;
  readRecords(options: {
    type: RecordType;
    timeRangeFilter: TimeRangeFilter;
    dataOriginFilter?: string[];
    ascendingOrder?: boolean;
    pageSize?: number;
    pageToken?: string;
  }): Promise<{
    records: StoredRecord[];
    pageToken?: string;
  }>;
  getChangesToken(options: { types: RecordType[] }): Promise<{
    token: string;
  }>;
  getChanges(options: { token: string }): Promise<{
    changes: Change[];
    nextToken: string;
  }>;
  requestHealthPermissions(options: { read: RecordType[]; write: RecordType[] }): Promise<{
    grantedPermissions: string[];
    hasAllPermissions: boolean;
  }>;
  checkHealthPermissions(options: { read: RecordType[]; write: RecordType[] }): Promise<{
    grantedPermissions: string[];
    hasAllPermissions: boolean;
  }>;
  revokeHealthPermissions(): Promise<void>;
  openHealthConnectSetting(): Promise<void>;
}
export type HealthConnectAvailability = 'Available' | 'NotInstalled' | 'NotSupported';
export type RecordType = 
  | 'ActiveCaloriesBurned'
  | 'BasalBodyTemperature'
  | 'BasalMetabolicRate'
  | 'BloodGlucose'
  | 'BloodPressure'
  | 'HeartRate'
  | 'HeartRateVariabilityRmssdRecord'
  | 'Height'
  | 'OxygenSaturation'
  | 'RestingHeartRate'
  | 'SleepSession'
  | 'Steps'
  | 'Vo2Max'
  | 'Weight';
type RecordBase = {
  metadata: RecordMetadata;
};
type StoredRecord = RecordBase & Record;
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

export type HeartRateSample = {
  time: string;
  beatsPerMinute: numbe;
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
