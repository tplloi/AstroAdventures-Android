package com.roy.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.receiveAsFlow

object GlobalCounter {

    private val enemyTimer = ticker(35, 1000, Dispatchers.Default)

    val enemyTimerFlow = com.roy.data.GlobalCounter.enemyTimer.receiveAsFlow()

    private val starsBackgroundTimer = ticker(30, 1000, Dispatchers.Default)

    val starsBackgroundTimerFlow = com.roy.data.GlobalCounter.starsBackgroundTimer.receiveAsFlow()

}
