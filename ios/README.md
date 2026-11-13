# 신호등 알리미 iPhone 시제품

강릉시 시민제안 제출용 시제품입니다. 정식 출시 앱이 아닙니다.

iOS 17 이상용 네이티브 시제품입니다. 앱 안의 가상 횡단보도 접근, 10초 경고 유지와 포인트 적립, 하슬랑 성장·꾸미기, 포인트 상점과 디자인 소개를 볼 수 있습니다. 다른 앱 위에 표시되는 iOS 배너는 홈 탭에서 7초 뒤 알림을 예약해 체험할 수 있습니다. 포인트는 앱 안의 체험에서 적립됩니다. 실제 비콘 감지와 모바일 교환권 발급은 연동 전입니다.

## 내 아이폰에서 실행

1. Mac의 Xcode 16 이상에서 `SignalAlert.xcodeproj`를 엽니다.
2. Xcode **Settings → Accounts**에서 Apple 계정에 로그인합니다.
3. 프로젝트의 **SignalAlert → Signing & Capabilities**에서 **Automatically manage signing**을 켜고 Team을 선택합니다. 번들 ID를 등록할 수 없으면 팀에서 사용할 고유 ID로 바꿉니다.
4. 아이폰을 연결하고 신뢰를 허용합니다. 필요하면 iPhone의 **설정 → 개인정보 보호 및 보안 → 개발자 모드**를 켭니다.
5. Xcode에서 연결한 아이폰을 실행 기기로 선택하고 **Run (⌘R)**을 누릅니다.

## 설치 링크 배포

[Apple Developer Program](https://developer.apple.com/programs/) 팀을 사용해 Xcode에서 서명 후 **Product → Archive**로 아카이브를 만들고 App Store Connect에 업로드합니다. 외부 공개용 [TestFlight 설치 링크](https://developer.apple.com/help/app-store-connect/test-a-beta-version/invite-external-testers)는 첫 빌드가 베타 심사를 통과한 뒤 외부 테스트 그룹에서 만들 수 있습니다.
