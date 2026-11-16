import SwiftUI
import UserNotifications

@main
struct SignalAlertApp: App {
    @UIApplicationDelegateAdaptor(NotificationDelegate.self) private var notifications
    var body: some Scene { WindowGroup { SignalAlertView() } }
}

final class NotificationDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions options: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }
}

struct SignalAlertView: View {
    private enum Tab: Hashable { case home, shop, titles, avatar, guide, design }
    private enum Demo { case idle, approaching, warning, earned, capped }

    @AppStorage("signal-alert.ledger.v1") private var saved = Data()
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.openURL) private var openURL
    @State private var tab = Tab.home
    @State private var demo = Demo.idle
    @State private var seconds = 10
    @State private var task: Task<Void, Never>?
    @State private var demoStatus = ""
    @State private var bannerStatus = ""
    @State private var schedulingBanner = false
    @State private var previewLevel: Int?

    private let ink = Color(red: 0.27, green: 0.15, blue: 0.12)
    private let accent = Color(red: 0.65, green: 0.20, blue: 0.13)
    private let paper = Color(red: 1, green: 0.97, blue: 0.94)
    private let peach = Color(red: 1, green: 0.87, blue: 0.71)
    private let submissionNotice = "강릉시 시민제안 제출용 시제품입니다. 정식 출시 앱이 아닙니다."
    private let bannerID = "signal-alert.virtual-beacon"

    private var ledger: RewardLedger {
        (try? JSONDecoder().decode(RewardLedger.self, from: saved)) ?? RewardLedger()
    }
    private var today: String {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Asia/Seoul") ?? .gmt
        let day = calendar.dateComponents([.year, .month, .day], from: .now)
        return String(format: "%04d-%02d-%02d", day.year ?? 0, day.month ?? 0, day.day ?? 0)
    }

    var body: some View {
        TabView(selection: $tab) {
            page("신호등 알리미") { home }.tabItem { Label("홈", systemImage: "house") }.tag(Tab.home)
            page("포인트 상점") { shop }.tabItem { Label("상점", systemImage: "bag") }.tag(Tab.shop)
            page("교통안전 칭호") { titles }.tabItem { Label("칭호", systemImage: "medal") }.tag(Tab.titles)
            page("하슬랑 키우기") { avatar }.tabItem { Label("하슬랑", systemImage: "sparkles") }.tag(Tab.avatar)
            page("사용 안내") { guide }.tabItem { Label("안내", systemImage: "info.circle") }.tag(Tab.guide)
            page("디자인 소개") { design }.tabItem { Label("디자인", systemImage: "paintpalette") }.tag(Tab.design)
        }
        .tint(accent)
        .preferredColorScheme(.light)
        .fullScreenCover(isPresented: Binding(get: { demo == .warning || demo == .earned || demo == .capped },
                                               set: { if !$0 { cancelDemo() } })) { warning }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active && (demo == .approaching || demo == .warning) { cancelDemo() }
        }
    }

    private func page<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 15, content: content)
                    .frame(maxWidth: 520)
                    .padding(18)
                    .frame(maxWidth: .infinity)
            }
            .background(paper)
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var home: some View {
        Group {
            HStack(spacing: 10) {
                brandIcon(size: 42).accessibilityHidden(true)
                Text("신호등 알리미").font(.title2.bold())
            }
            Text(submissionNotice).font(.footnote.weight(.semibold))
            VStack(alignment: .leading, spacing: 14) {
                HStack {
                    VStack(alignment: .leading) {
                        Text("사용 가능한 포인트").font(.subheadline).foregroundStyle(peach)
                        Text("\(ledger.balance)점").font(.largeTitle.bold()).accessibilityIdentifier("balance")
                    }
                    Spacer(minLength: 8)
                    Button { tab = .avatar } label: {
                        VStack {
                            PixelAvatar(level: ledger.level, equipped: ledger.equipped)
                                .frame(width: 104, height: 74).clipShape(.rect(cornerRadius: 8))
                            Text("하슬랑 키우기").font(.caption.bold())
                        }
                    }.buttonStyle(.plain)
                }
                HStack {
                    Text("누적 적립 \(ledger.total)점")
                    Spacer()
                    Text("오늘 \(ledger.count(on: today)) / \(RewardLedger.dailyLimit)회")
                }.font(.subheadline.weight(.semibold))
            }
            .padding(18).background(ink, in: .rect(cornerRadius: 22)).foregroundStyle(.white)
            card {
                Text("가상 비콘 체험").font(.headline)
                Text("앱 안에서 가상 접근을 시작해 경고를 10초 유지하면 10점이 쌓여요. 하루 최대 3회예요.")
                    .font(.subheadline)
                Button(demo == .approaching ? "횡단보도로 접근 중…" : "가상 접근 시작", action: startDemo)
                    .buttonStyle(.borderedProminent).disabled(demo != .idle)
                    .accessibilityIdentifier("start-demo")
                if demo == .approaching { Button("체험 취소", action: cancelDemo) }
                if !demoStatus.isEmpty { Text(demoStatus).font(.footnote) }
            }
            card {
                Text("다른 앱 위 알림 체험").font(.headline)
                Text("알림을 예약하고 7초 안에 홈이나 다른 앱으로 이동해 보세요. iOS 배너로 경고가 도착해요.")
                    .font(.subheadline)
                Button("7초 뒤 알림 예약") { Task { await scheduleBanner() } }
                    .buttonStyle(.bordered).disabled(schedulingBanner)
                Button("예약한 알림 취소", action: cancelBanner).font(.subheadline).disabled(schedulingBanner)
                if !bannerStatus.isEmpty { Text(bannerStatus).font(.footnote) }
            }
        }.foregroundStyle(ink)
    }

    private var shop: some View {
        Group {
            card {
                Text("사용 가능한 포인트").font(.subheadline)
                Text("\(ledger.balance)점").font(.title.bold())
            }
            card {
                Text("모바일 교환권 예시").font(.headline)
                Text("편의점 기프티콘")
                Text("치킨 기프티콘")
                Text("아이스크림 기프티콘")
                Text("실제 발급은 서비스 연동 후 이용할 수 있어요.").font(.subheadline)
            }
            ForEach(ShopItem.allCases) { item in
                card {
                    HStack {
                        Image(systemName: item.icon).foregroundStyle(accent)
                        VStack(alignment: .leading) {
                            Text(item.label).bold()
                            Text("\(item.price)점 · 가상 아이템").font(.caption)
                        }
                        Spacer()
                        Button(ledger.owned.contains(item) ? "보유 중" : "교환") { update { $0.redeem(item) } }
                            .buttonStyle(.bordered)
                            .disabled(ledger.owned.contains(item) || ledger.balance < item.price)
                    }
                }
            }
        }
    }

    private var titles: some View {
        ForEach(1..<RewardLedger.thresholds.count, id: \.self) { level in
            let threshold = RewardLedger.thresholds[level]
            card {
                Text(RewardLedger.titles[level]).font(.headline)
                ProgressView(value: Double(min(ledger.total, threshold)), total: Double(threshold))
                Text("누적 \(min(ledger.total, threshold)) / \(threshold)점").font(.subheadline)
            }
        }
    }

    private var avatar: some View {
        let level = previewLevel ?? ledger.level
        return Group {
            card {
                PixelAvatar(level: level, equipped: ledger.equipped).aspectRatio(144.0 / 102, contentMode: .fit)
                Text(RewardLedger.titles[level]).font(.headline)
                Picker("성장 모습", selection: $previewLevel) {
                    Text("내 모습").tag(nil as Int?)
                    ForEach(0..<4, id: \.self) { index in Text("\(index + 1)단계").tag(index as Int?) }
                }.pickerStyle(.segmented)
            }
            ForEach(ShopItem.allCases) { item in
                card {
                    HStack {
                        Text(item.label)
                        Spacer()
                        if ledger.owned.contains(item) {
                            Button(ledger.equipped.contains(item) ? "벗기" : "착용") { update { $0.toggle(item) } }
                        } else {
                            Button("상점") { tab = .shop }
                        }
                    }
                }
            }
        }
    }

    private var guide: some View {
        Group {
            Text(submissionNotice).font(.footnote.weight(.semibold)).foregroundStyle(ink)
            card {
                Text("가상 비콘").font(.headline)
                Text("시제품은 실제 비콘 설치 없이 횡단보도 접근 상황을 재현해요.")
            }
            card {
                Text("아이폰 알림").font(.headline)
                Text("다른 앱을 사용할 때는 iOS 배너로 경고해요. 알림 설정과 집중 모드에 따라 표시 방식이 달라져요.")
                Text("배너를 닫았는지와 화면이 꺼져 있는지는 앱이 확인할 수 없어, 포인트 적립은 앱 안에서만 체험할 수 있어요.")
                Button("아이폰 알림 설정 열기") {
                    if let url = URL(string: UIApplication.openNotificationSettingsURLString) { openURL(url) }
                }
            }
            card {
                Text("실제 설치 구상").font(.headline)
                Text("횡단보도 앞 인도 대기 구역에 비콘을 설치하고, 현장 시험으로 신호가 감지되는 위치와 경고 시점을 조정해요.")
            }
        }
    }

    private var design: some View {
        card {
            brandIcon(size: 170)
                .frame(maxWidth: .infinity).accessibilityLabel("둥근 사각형의 주황색 사이렌 아이콘")
            Text("신호등 알리미").font(.title2.bold())
            Text("5난초 학생들이 만든 이름과 아이콘 중에서 학급 투표로 ‘신호등 알리미’와 사이렌 디자인을 골랐어요.")
        }
    }

    private func brandIcon(size: CGFloat) -> some View {
        Image("Brand").resizable().scaledToFit()
            .frame(width: size, height: size)
            .clipShape(.rect(cornerRadius: size * 0.27))
    }

    private var warning: some View {
        VStack(spacing: 18) {
            brandIcon(size: 76).accessibilityHidden(true)
            Text(demo == .warning ? "횡단보도 앞입니다.\n주변을 봐주세요." :
                 demo == .earned ? "+10 포인트 적립" : "오늘 적립 한도에 도달했어요")
                .font(.title.bold()).foregroundStyle(peach).multilineTextAlignment(.center)
            if demo == .warning {
                Text("\(seconds)초").font(.system(size: 42, weight: .bold, design: .monospaced))
                    .accessibilityIdentifier("countdown")
            }
            Button(demo == .warning ? "경고 닫기" : "닫기") {
                if demo == .warning { demoStatus = "앗! 아쉬워요. 주변을 조심하세요." }
                cancelDemo()
            }
                .buttonStyle(.bordered).tint(peach)
        }
        .foregroundStyle(.white).padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(ink.ignoresSafeArea())
    }

    private func card<Content: View>(@ViewBuilder _ content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 8, content: content)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(15)
            .background(.white, in: .rect(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(peach.opacity(0.65)))
            .foregroundStyle(ink)
    }

    private func update(_ change: (inout RewardLedger) -> Void) {
        var next = ledger
        change(&next)
        if let data = try? JSONEncoder().encode(next) { saved = data }
    }

    private func startDemo() {
        guard demo == .idle else { return }
        demoStatus = ""
        demo = .approaching
        task = Task {
            do {
                try await Task.sleep(for: .seconds(2))
                seconds = 10
                demo = .warning
                let clock = ContinuousClock()
                let start = clock.now
                for elapsed in 1...10 {
                    try await clock.sleep(until: start.advanced(by: .seconds(elapsed)))
                    guard scenePhase == .active, !Task.isCancelled else { return }
                    seconds = 10 - elapsed
                }
                var earned = false
                update { earned = $0.award(on: today) }
                demo = earned ? .earned : .capped
                try await Task.sleep(for: .seconds(3))
                demo = .idle
                task = nil
            } catch { return }
        }
    }

    private func cancelDemo() {
        task?.cancel()
        task = nil
        demo = .idle
    }

    private func scheduleBanner() async {
        schedulingBanner = true
        defer { schedulingBanner = false }
        let center = UNUserNotificationCenter.current()
        do {
            guard try await center.requestAuthorization(options: [.alert, .sound]) else {
                bannerStatus = "설정에서 신호등 알리미의 알림을 허용해 주세요."
                return
            }
            let message = UNMutableNotificationContent()
            message.title = "신호등 알리미"
            message.body = "횡단보도 앞입니다. 주변을 봐주세요."
            message.sound = .default
            try await center.add(UNNotificationRequest(identifier: bannerID, content: message,
                trigger: UNTimeIntervalNotificationTrigger(timeInterval: 7, repeats: false)))
            bannerStatus = "알림을 예약했어요. 다른 앱으로 이동해 보세요."
        } catch {
            bannerStatus = "알림을 예약하지 못했어요. 다시 시도해 주세요."
        }
    }

    private func cancelBanner() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [bannerID])
        bannerStatus = "예약한 체험 알림을 취소했어요."
    }
}

private struct PixelAvatar: View {
    let level: Int
    let equipped: Set<ShopItem>
    private static let variants: [[[Int]]] = {
        guard let url = Bundle.main.url(forResource: "Avatar", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let variants = try? JSONDecoder().decode([[[Int]]].self, from: data) else { return [] }
        return variants
    }()

    var body: some View {
        let flags = ShopItem.allCases.enumerated().reduce(0) { bits, entry in
            equipped.contains(entry.element) ? bits | (1 << entry.offset) : bits
        }
        let index = level * 8 + flags
        Canvas { context, size in
            guard Self.variants.indices.contains(index) else { return }
            let scale = min(size.width / 144, size.height / 102)
            let x = (size.width - 144 * scale) / 2
            let y = (size.height - 102 * scale) / 2
            for block in Self.variants[index] {
                let color = block[4]
                let rect = CGRect(x: x + CGFloat(block[0]) * scale, y: y + CGFloat(block[1]) * scale,
                                  width: CGFloat(block[2]) * scale, height: CGFloat(block[3]) * scale)
                context.fill(Path(rect), with: .color(Color(red: Double(color >> 16) / 255,
                    green: Double((color >> 8) & 255) / 255, blue: Double(color & 255) / 255)))
            }
        }.accessibilityLabel("하슬랑 픽셀 캐릭터")
    }
}
