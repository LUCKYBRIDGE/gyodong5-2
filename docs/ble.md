# 횡단보도 비콘 감지 설계

현재 데모는 `BeaconSource.Demo`를 사용한다. 시작 7초 뒤 진입을 알리고, 22초 뒤 이탈을 알린다. 감지 중지나 재시작 시 예약된 이벤트를 취소한다.

## 실제 BLE 비콘 연결

1. 횡단보도마다 등록한 비콘의 BLE 광고 식별자를 기준으로 `BluetoothLeScanner`와 `ScanFilter`를 사용한다. iBeacon이면 광고의 제조사 데이터에서 UUID, major, minor를 확인한다. 광고를 받는 방식이므로 페어링 없이 감지할 수 있다. [Android BLE 백그라운드 감지](https://developer.android.com/develop/connectivity/bluetooth/ble/background)
2. 연속 수신한 RSSI의 중앙값을 사용한다. 초기 시험값으로 −68 dBm 이상이면 진입, −75 dBm 미만이면 이탈로 처리한다. 중간 값에서는 직전 상태를 유지한다. 등록된 광고를 3초 동안 수신하지 못하면 이탈로 처리한다. 이 값들은 현장 보정 전의 시험값이다.
3. 실제 소스도 `BeaconSource`를 구현해 메인 스레드에서 진입과 이탈을 전달한다. 감지 종료 시 스캔과 타임아웃을 취소한다. 스캔 실패나 블루투스 비활성화, 권한 철회 시에는 이탈로 처리해 진행 중인 포인트 적립을 중단한다.
4. 실제 스캔 도입 시 Android 12 이상에서는 `BLUETOOTH_SCAN`을 요청한다. 등록된 횡단보도의 근접 여부를 얻는 기능이므로 `neverForLocation`을 선언하지 않고 위치 권한 요건을 함께 적용한다. 이전 버전의 위치 권한과 백그라운드 요건은 지원 범위에 맞춰 적용한다. 계속 감지하는 방식은 사용자가 앱을 열고 시작하는 포그라운드 서비스에서 먼저 검증하고, 실제 센서 사용에 맞는 서비스 유형과 권한을 선언한다. [Bluetooth 권한](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions) · [포그라운드 서비스 유형](https://developer.android.com/develop/background-work/services/fgs/service-types)

RSSI는 장애물이나 휴대폰을 쥔 방향, 기종에 따라 변동한다. 양쪽 인도에서 경고가 뜨는 시점, 인도를 따라 지나가거나 차량으로 통과할 때의 오작동, 기기별 감지 지연과 배터리 사용량을 설치 지점마다 확인한다.
