# capacitor-health-connect

Android Health Connect integration for Capacitor

## Install

```bash
npm install capacitor-health-connect
npx cap sync
```

## API

<docgen-index>

* [`checkAvailability()`](#checkavailability)
* [`ensureInstalled()`](#ensureinstalled)
* [`insertRecords(...)`](#insertrecords)
* [`readRecord(...)`](#readrecord)
* [`readRecords(...)`](#readrecords)
* [`getChangesToken(...)`](#getchangestoken)
* [`getChanges(...)`](#getchanges)
* [`requestHealthPermissions(...)`](#requesthealthpermissions)
* [`checkHealthPermissions(...)`](#checkhealthpermissions)
* [`revokeHealthPermissions()`](#revokehealthpermissions)
* [`openHealthConnectSetting()`](#openhealthconnectsetting)
* [`aggregateGroupByPeriod(...)`](#aggregategroupbyperiod)
* [`aggregateGroupByDuration(...)`](#aggregategroupbyduration)
* [`aggregate(...)`](#aggregate)
* [`checkReadHealthDataHistoryPermission()`](#checkreadhealthdatahistorypermission)
* [`requestReadHealthDataHistoryPermission()`](#requestreadhealthdatahistorypermission)
* [`checkReadHealthDataInBackgroundPermission()`](#checkreadhealthdatainbackgroundpermission)
* [`requestReadHealthDataInBackgroundPermission()`](#requestreadhealthdatainbackgroundpermission)
* [`openPlayStore()`](#openplaystore)
* [`openHealthConnectSettings()`](#openhealthconnectsettings)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### checkAvailability()

```typescript
checkAvailability() => Promise<{ availability: HealthConnectAvailability; }>
```

**Returns:** <code>Promise&lt;{ availability: <a href="#healthconnectavailability">HealthConnectAvailability</a>; }&gt;</code>

--------------------


### ensureInstalled()

```typescript
ensureInstalled() => Promise<{ installed: boolean; }>
```

**Returns:** <code>Promise&lt;{ installed: boolean; }&gt;</code>

--------------------


### insertRecords(...)

```typescript
insertRecords(payload: InsertRecordsPayload) => Promise<{ recordIds: string[]; }>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`payload`** | <code><a href="#insertrecordspayload">InsertRecordsPayload</a></code> |

**Returns:** <code>Promise&lt;{ recordIds: string[]; }&gt;</code>

--------------------


### readRecord(...)

```typescript
readRecord(options: ReadRecordOptions) => Promise<{ record: StoredRecord; }>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#readrecordoptions">ReadRecordOptions</a></code> |

**Returns:** <code>Promise&lt;{ record: <a href="#storedrecord">StoredRecord</a>&lt;<a href="#record">Record</a>&gt;; }&gt;</code>

--------------------


### readRecords(...)

```typescript
readRecords(options: ReadRecordsOptions) => Promise<{ records: StoredRecord[]; pageToken?: string; }>
```

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#readrecordsoptions">ReadRecordsOptions</a></code> |

**Returns:** <code>Promise&lt;{ records: <a href="#storedrecord">StoredRecord</a>&lt;<a href="#record">Record</a>&gt;[]; pageToken?: string; }&gt;</code>

--------------------


### getChangesToken(...)

```typescript
getChangesToken(options: GetChangesTokenOptions) => Promise<{ token: string; }>
```

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code><a href="#getchangestokenoptions">GetChangesTokenOptions</a></code> |

**Returns:** <code>Promise&lt;{ token: string; }&gt;</code>

--------------------


### getChanges(...)

```typescript
getChanges(options: GetChangesOptions) => Promise<{ changes: Change[]; nextToken: string; }>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#getchangesoptions">GetChangesOptions</a></code> |

**Returns:** <code>Promise&lt;{ changes: Change[]; nextToken: string; }&gt;</code>

--------------------


### requestHealthPermissions(...)

```typescript
requestHealthPermissions(options: HealthPermissions) => Promise<{ grantedPermissions: HealthPermissions; hasAllPermissions: boolean; }>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#healthpermissions">HealthPermissions</a></code> |

**Returns:** <code>Promise&lt;{ grantedPermissions: <a href="#healthpermissions">HealthPermissions</a>; hasAllPermissions: boolean; }&gt;</code>

--------------------


### checkHealthPermissions(...)

```typescript
checkHealthPermissions(options: HealthPermissions) => Promise<{ grantedPermissions: HealthPermissions; hasAllPermissions: boolean; }>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#healthpermissions">HealthPermissions</a></code> |

**Returns:** <code>Promise&lt;{ grantedPermissions: <a href="#healthpermissions">HealthPermissions</a>; hasAllPermissions: boolean; }&gt;</code>

--------------------


### revokeHealthPermissions()

```typescript
revokeHealthPermissions() => Promise<void>
```

--------------------


### openHealthConnectSetting()

```typescript
openHealthConnectSetting() => Promise<void>
```

--------------------


### aggregateGroupByPeriod(...)

```typescript
aggregateGroupByPeriod(options: AggregateGroupByPeriodOptions) => Promise<AggregateByPeriodResult>
```

| Param         | Type                                                                                    |
| ------------- | --------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#aggregategroupbyperiodoptions">AggregateGroupByPeriodOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#aggregatebyperiodresult">AggregateByPeriodResult</a>&gt;</code>

--------------------


### aggregateGroupByDuration(...)

```typescript
aggregateGroupByDuration(options: AggregateGroupByDurationOptions) => Promise<AggregateByPeriodResult>
```

| Param         | Type                                                                                        |
| ------------- | ------------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#aggregategroupbydurationoptions">AggregateGroupByDurationOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#aggregatebyperiodresult">AggregateByPeriodResult</a>&gt;</code>

--------------------


### aggregate(...)

```typescript
aggregate(options: AggregateOptions) => Promise<AggregateResult>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#aggregateoptions">AggregateOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#aggregateresult">AggregateResult</a>&gt;</code>

--------------------


### checkReadHealthDataHistoryPermission()

```typescript
checkReadHealthDataHistoryPermission() => Promise<{ hasPermission: boolean; }>
```

**Returns:** <code>Promise&lt;{ hasPermission: boolean; }&gt;</code>

--------------------


### requestReadHealthDataHistoryPermission()

```typescript
requestReadHealthDataHistoryPermission() => Promise<{ readHealthDataHistoryStatus: ReadHealthDataHistoryPermissionStatus; }>
```

**Returns:** <code>Promise&lt;{ readHealthDataHistoryStatus: <a href="#readhealthdatahistorypermissionstatus">ReadHealthDataHistoryPermissionStatus</a>; }&gt;</code>

--------------------


### checkReadHealthDataInBackgroundPermission()

```typescript
checkReadHealthDataInBackgroundPermission() => Promise<{ hasPermission: boolean; }>
```

**Returns:** <code>Promise&lt;{ hasPermission: boolean; }&gt;</code>

--------------------


### requestReadHealthDataInBackgroundPermission()

```typescript
requestReadHealthDataInBackgroundPermission() => Promise<{ readHealthDataInBackgroundStatus: ReadHealthDataHistoryPermissionStatus; }>
```

**Returns:** <code>Promise&lt;{ readHealthDataInBackgroundStatus: <a href="#readhealthdatahistorypermissionstatus">ReadHealthDataHistoryPermissionStatus</a>; }&gt;</code>

--------------------


### openPlayStore()

```typescript
openPlayStore() => Promise<void>
```

--------------------


### openHealthConnectSettings()

```typescript
openHealthConnectSettings() => Promise<void>
```

--------------------


### Type Aliases


#### HealthConnectAvailability

<code>'Available' | 'NotInstalled' | 'NotSupported'</code>


#### InsertRecordsPayload

<code>{ records: Record[]; }</code>


#### Record

Construct a type with a set of properties K of type T

<code>{ [P in K]: T; }</code>


#### StoredRecord

<code><a href="#recordbase">RecordBase</a> & T</code>


#### RecordBase

<code>{ metadata: <a href="#recordmetadata">RecordMetadata</a>; }</code>


#### RecordMetadata

<code>{ id: string; clientRecordId?: string; clientRecordVersion: number; lastModifiedTime: string; dataOrigin: string; }</code>


#### ReadRecordOptions

<code>{ type: <a href="#recordtype">RecordType</a>; recordId: string; }</code>


#### RecordType

<code>'ActiveCaloriesBurned' | 'BasalBodyTemperature' | 'BasalMetabolicRate' | 'BloodGlucose' | 'BloodPressure' | 'HeartRate' | 'HeartRateVariabilityRmssd' | 'Height' | 'OxygenSaturation' | 'RestingHeartRate' | 'SleepSession' | 'Steps' | 'Vo2Max' | 'Weight'</code>


#### ReadRecordsOptions

<code>{ type: <a href="#recordtype">RecordType</a>; timeRangeFilter: <a href="#timerangefilter">TimeRangeFilter</a>; dataOriginFilter?: string[]; ascendingOrder?: boolean; pageSize?: number; pageToken?: string; }</code>


#### TimeRangeFilter

<code>{ type: 'before' | 'after'; timeUTC: string; } | { type: 'between'; startTimeUTC: string; endTimeUTC: string; }</code>


#### GetChangesTokenOptions

<code>{ types: RecordType[]; }</code>


#### Change

<code><a href="#upsertchange">UpsertChange</a> | <a href="#deletechange">DeleteChange</a></code>


#### UpsertChange

<code>{ type: 'Upsert'; record: <a href="#record">Record</a>; }</code>


#### DeleteChange

<code>{ type: 'Delete'; recordId: string; }</code>


#### GetChangesOptions

<code>{ token: string; }</code>


#### HealthPermissions

<code>{ read: RecordType[]; write: RecordType[]; }</code>


#### AggregateByPeriodResult

<code>{ entries: AggregateByPeriodEntry[] }</code>


#### AggregateByPeriodEntry

<code>{ type: <a href="#aggregatetype">AggregateType</a>; startTime: string; endTime: string; result: number; }</code>


#### AggregateType

<code>'ActiveCaloriesTotal' | 'DistanceTotal' | 'ElevationGainedTotal' | 'FloorsClimbedTotal' | 'HeartBpmAvg' | 'HeartBpmMin' | 'HeartBpmMax' | 'HeartMeasurementsCount' | 'HydrationVolumeTotal' | 'PowerAvg' | 'PowerMin' | 'PowerMax' | 'SleepSessionDurationTotal' | 'StepsCountTotal' | 'TotalCaloriesBurnedTotal' | 'WheelchairPushesCountTotal'</code>


#### AggregateGroupByPeriodOptions

<code>{ type: <a href="#aggregatetype">AggregateType</a>; timeRangeFilter: <a href="#localtimerangefilter">LocalTimeRangeFilter</a>; timeRangeSlicer: <a href="#timerangeslicer">TimeRangeSlicer</a>; dataOriginFilter?: string[]; }</code>


#### LocalTimeRangeFilter

<code>{ type: 'before' | 'after'; localTime: string; } | { type: 'between'; localStartTime: string; localEndTime: string; }</code>


#### TimeRangeSlicer

<code>{ period: 'days' | 'months' | 'weeks' | 'years', count: number }</code>


#### AggregateGroupByDurationOptions

<code>{ type: <a href="#aggregatetype">AggregateType</a>; timeRangeFilter: <a href="#timerangefilter">TimeRangeFilter</a>; durationTimeRangeSlicer: <a href="#durationtimerangeslicer">DurationTimeRangeSlicer</a>; dataOriginFilter?: string[]; }</code>


#### DurationTimeRangeSlicer

<code>{ duration: 'days' | 'hours' | 'minutes' | 'seconds' | 'millis'; count: number; }</code>


#### AggregateResult

<code>{ type: <a href="#aggregatetype">AggregateType</a>; startTime: string; endTime: string; result: number; }</code>


#### AggregateOptions

<code>{ type: <a href="#aggregatetype">AggregateType</a>; timeRangeFilter: <a href="#localtimerangefilter">LocalTimeRangeFilter</a>; dataOriginFilter?: string[]; }</code>


#### ReadHealthDataHistoryPermissionStatus

<code>'NotSupported' | 'Granted' | 'Denied'</code>

</docgen-api>
