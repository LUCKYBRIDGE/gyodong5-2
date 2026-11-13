import Foundation

enum ShopItem: String, Codable, CaseIterable, Identifiable {
    case reflector, sticker, bagTag

    var id: Self { self }
    var label: String {
        switch self {
        case .reflector: "반짝이 반사판"
        case .sticker: "횡단보도 스티커"
        case .bagTag: "안전 가방고리"
        }
    }
    var price: Int {
        switch self {
        case .reflector: 10
        case .sticker: 30
        case .bagTag: 60
        }
    }
    var icon: String {
        switch self {
        case .reflector: "sparkle"
        case .sticker: "seal"
        case .bagTag: "tag"
        }
    }
}

struct RewardLedger: Codable {
    static let pointsPerReward = 10
    static let dailyLimit = 3
    static let thresholds = [0, 10, 30, 70]
    static let titles = ["함께 걷기 시작", "안전 첫걸음", "횡단보도 친구", "교통안전 지킴이"]

    private(set) var day = ""
    private(set) var count = 0
    private(set) var total = 0
    private(set) var spent = 0
    private(set) var owned: Set<ShopItem> = []
    private(set) var equipped: Set<ShopItem> = []

    var balance: Int { total - spent }
    var level: Int { Self.thresholds.lastIndex { $0 <= total } ?? 0 }

    func count(on day: String) -> Int { self.day == day ? count : 0 }

    mutating func award(on day: String) -> Bool {
        let today = count(on: day)
        guard today < Self.dailyLimit else { return false }
        self.day = day
        count = today + 1
        total += Self.pointsPerReward
        return true
    }

    mutating func redeem(_ item: ShopItem) {
        guard balance >= item.price, !owned.contains(item) else { return }
        spent += item.price
        owned.insert(item)
    }

    mutating func toggle(_ item: ShopItem) {
        guard owned.contains(item) else { return }
        if equipped.remove(item) == nil { equipped.insert(item) }
    }
}
