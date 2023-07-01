package com.roy.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object PlayerHealthInfo {

    const val MAX_HEALTH = 20

    private val playerHealth = MutableStateFlow(com.roy.data.PlayerHealthInfo.MAX_HEALTH)

    fun getPlayerHealthFlow(): Flow<Int> = com.roy.data.PlayerHealthInfo.playerHealth

    fun getPlayerHealthValue() = com.roy.data.PlayerHealthInfo.playerHealth.value

    fun onHit() {
        Log.d("Health", "${com.roy.data.PlayerHealthInfo.playerHealth.value}")
        com.roy.data.PlayerHealthInfo.playerHealth.value -= 2
    }

    fun resetHealth() {
        com.roy.data.PlayerHealthInfo.playerHealth.value = 20
    }
}