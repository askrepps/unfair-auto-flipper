package com.askrepps.unfairflipper.model

import com.askrepps.unfairflipper.util.Logger
import com.askrepps.unfairflipper.util.centsToFormattedCurrency
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random

data class FlipTimeEntry(val flips: Long, val durationPerFlip: Float)

data class GameState(
    var totalFlips: Long,
    var balance: Int,
    var coinValue: Int,
    var headsChance: Float,
    var flipDuration: Float,
    var multiplierPerStreak: Float,
    var currentHeadsStreak: Int,
    val store: List<StoreItem>
) {

    private val _flipTimeHistory = mutableListOf<FlipTimeEntry>()
    val flipTimeHistory: List<FlipTimeEntry>
        get() = _flipTimeHistory

    fun captureFlipTimes(logger: Logger) {
        val flipsAtCurrentDuration = totalFlips - flipTimeHistory.sumOf { it.flips }
        _flipTimeHistory.add(FlipTimeEntry(flipsAtCurrentDuration, flipDuration))
        logger.log("Performed $flipsAtCurrentDuration flips at $flipDuration seconds each")
    }
}

enum class GameEnding(val cumulativeChance: Float) {
    TenHeads(0.1f),
    CoinExplode(0.4f),
    CoinEdge(0.6f),
    CoinTooHigh(0.8f),
    Eggbug(1.0f)
}

data class GameResult(
    val ending: GameEnding,
    val state: GameState
)

class Game(
    private val purchaseStrategy: PurchaseStrategy,
    private val logger: Logger,
    seed: Int = System.currentTimeMillis().toInt()
) {

    private val state = GameState(
        totalFlips = 0L,
        balance = 0,
        coinValue = 1,
        headsChance = 0.2f,
        flipDuration = 2.0f,
        multiplierPerStreak = 1.0f,
        currentHeadsStreak = 0,
        store = listOf(
            CoinValueStoreItem(logger),
            HeadsChanceStoreItem(logger),
            FlipSpeedStoreItem(logger),
            StreakMultiplierStoreItem(logger)
        )
    )

    private val randomGenerator = Random(seed)

    fun playGame(): GameResult {
        var ending: GameEnding? = null
        while (ending == null) {
            purchaseStrategy.checkStore(state)
            ending = flipCoin()
        }
        return GameResult(ending, state)
    }

    private fun flipCoin(): GameEnding? {
        if (randomGenerator.nextFloat() < state.headsChance) {
            val score = calculateHeadsScore()
            state.balance += score
            state.currentHeadsStreak++
            logger.log("Heads x${state.currentHeadsStreak}! Earned ${score.centsToFormattedCurrency()} (balance = ${state.balance.centsToFormattedCurrency()})")
        } else {
            state.currentHeadsStreak = 0
            logger.log("Tails...")
        }
        state.totalFlips++
        return checkEnding()
    }

    private fun calculateHeadsScore() =
        state.coinValue * ceil(state.multiplierPerStreak.pow(state.currentHeadsStreak)).toInt()

    private fun checkEnding(): GameEnding? {
        if (state.currentHeadsStreak < 10) {
            return null
        }
        state.captureFlipTimes(logger)
        val randomValue = randomGenerator.nextFloat()
        return GameEnding.entries
            .sortedBy { it.cumulativeChance }
            .find { randomValue < it.cumulativeChance }
            .also { logger.log("Ending selected: $it") }
    }
}
