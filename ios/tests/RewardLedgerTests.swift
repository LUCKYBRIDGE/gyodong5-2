import Foundation

@main
struct RewardLedgerTests {
    static func main() throws {
        var ledger = RewardLedger()
        let firstDay = "2026-11-13"
        let nextDay = "2026-11-14"

        precondition(ledger.balance == 0 && ledger.level == 0)
        ledger.redeem(.reflector)
        ledger.toggle(.reflector)
        precondition(ledger.owned.isEmpty && ledger.equipped.isEmpty)

        for _ in 0..<RewardLedger.dailyLimit { precondition(ledger.award(on: firstDay)) }
        precondition(!ledger.award(on: firstDay))
        precondition(ledger.balance == 30 && ledger.level == 2)

        ledger.redeem(.reflector)
        ledger.redeem(.reflector)
        ledger.toggle(.reflector)
        precondition(ledger.balance == 20 && ledger.total == 30 && ledger.level == 2)
        precondition(ledger.equipped == [.reflector])

        let encoded = try JSONEncoder().encode(ledger)
        var restored = try JSONDecoder().decode(RewardLedger.self, from: encoded)
        precondition(restored.equipped == [.reflector] && restored.count(on: firstDay) == 3)
        precondition(restored.count(on: nextDay) == 0 && restored.award(on: nextDay))
        precondition(restored.balance == 30 && restored.count(on: nextDay) == 1)
        restored.toggle(.reflector)
        precondition(restored.equipped.isEmpty && restored.owned == [.reflector])
        print("iOS reward ledger checks passed")
    }
}
