package com.askrepps.unfairflipper.model

import com.askrepps.unfairflipper.util.Logger
import com.askrepps.unfairflipper.util.centsToFormattedCurrency

enum class StoreItemType { CoinValue, HeadsChance, FlipSpeed, StreakMultiplier }

abstract class StoreItem(
    val type: StoreItemType,
    private val prices: List<Int>,
    protected val logger: Logger
) {

    var currentLevel: Int = 0
        private set

    fun isAffordable(balance: Int): Boolean =
        currentLevel < prices.size && prices[currentLevel] <= balance

    fun purchaseItem(state: GameState) {
        if (isAffordable(state.balance)) {
            val cost = prices[currentLevel]
            state.balance -= cost
            logger.log("Purchased $type for ${cost.centsToFormattedCurrency()} (balance = ${state.balance.centsToFormattedCurrency()})")
            applyEffect(state)
            currentLevel++
        }
    }

    abstract fun applyEffect(state: GameState)
}

class CoinValueStoreItem(logger: Logger) : StoreItem(
    type = StoreItemType.CoinValue,
    prices = listOf(25, 1_00, 6_25, 100_00),
    logger
) {

    private val coinValues = listOf(5, 10, 25, 1_00)

    override fun applyEffect(state: GameState) {
        val newValue = coinValues[currentLevel]
        state.coinValue = newValue
        logger.log("Set coin value to $${String.format("%.2f", newValue / 100.0f)}")
    }
}

class HeadsChanceStoreItem(logger: Logger) : StoreItem(
    type = StoreItemType.HeadsChance,
    prices = listOf(1, 10, 1_00, 10_00, 100_00, 1_000_00, 10_000_00, 100_000_00),
    logger
) {

    override fun applyEffect(state: GameState) {
        val newChance = state.headsChance + 0.05f
        state.headsChance = newChance
        logger.log("Set heads chance to ${newChance * 100.0f}%")
    }
}

class FlipSpeedStoreItem(logger: Logger) : StoreItem(
    type = StoreItemType.FlipSpeed,
    prices = listOf(1, 10, 1_00, 10_00, 100_00),
    logger
) {

    override fun applyEffect(state: GameState) {
        state.captureFlipTimes(logger)
        val newDuration = state.flipDuration - 0.2f
        state.flipDuration = newDuration
        logger.log("Set flip duration to $newDuration seconds")
    }
}

class StreakMultiplierStoreItem(logger: Logger) : StoreItem(
    type = StoreItemType.StreakMultiplier,
    prices = listOf(1, 10, 1_00, 10_00, 100_00),
    logger
) {

    override fun applyEffect(state: GameState) {
        val newMultiplier = state.multiplierPerStreak + 0.5f
        state.multiplierPerStreak = newMultiplier
        logger.log("Set streak multiplier to ${newMultiplier}x")
    }
}

interface PurchaseStrategy {

    fun checkStore(state: GameState)
}

class AndrewPurchaseStrategy : PurchaseStrategy {

    private fun getItemPriority(type: StoreItemType) = when (type) {
        StoreItemType.CoinValue -> 0
        StoreItemType.HeadsChance -> 1
        StoreItemType.FlipSpeed -> 2
        StoreItemType.StreakMultiplier -> 3
    }

    override fun checkStore(state: GameState) {
        val prioritizedStore = state.store.sortedBy { getItemPriority(it.type) }
        do {
            val selectedItem = prioritizedStore.find { it.isAffordable(state.balance) }
            selectedItem?.purchaseItem(state)
        } while (selectedItem != null)
    }
}
