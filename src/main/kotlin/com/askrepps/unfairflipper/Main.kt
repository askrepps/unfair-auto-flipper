package com.askrepps.unfairflipper

import com.askrepps.unfairflipper.model.AndrewPurchaseStrategy
import com.askrepps.unfairflipper.model.Game
import com.askrepps.unfairflipper.model.GameEnding
import com.askrepps.unfairflipper.util.ConsoleLogger
import com.askrepps.unfairflipper.util.NullLogger
import com.askrepps.unfairflipper.util.formatWithPrecision

private fun Double.toFormattedHours() = (this / 3600.0).formatWithPrecision(3)

fun main(args: Array<String>) {
    var totalRuns = 0
    var totalFlipTime = 0.0
    val logger = if (args.contains("-v") || args.contains("--verbose")) {
        ConsoleLogger()
    } else {
        NullLogger()
    }

    println()
    println("======================================================")
    do {
        val game = Game(AndrewPurchaseStrategy(), logger)
        val result = game.playGame()
        val flipTime = result.state.flipTimeHistory.sumOf { (it.flips * it.durationPerFlip).toDouble() }
        println("Ending: ${result.ending} | Total flips: ${result.state.totalFlips} (${flipTime.toFormattedHours()} hours)")
        totalRuns++
        totalFlipTime += flipTime
    } while (result.ending != GameEnding.TenHeads)
    println("======================================================")
    println()
    println("Total runs: $totalRuns (${totalFlipTime.toFormattedHours()} hours)")
}
