package com.roy.data

object LevelInfo {

    var hasPlayedTutorial = false

    var level = 0

    fun resetLevel() {
        com.roy.data.LevelInfo.level = 1
    }

    fun increment() {
        com.roy.data.LevelInfo.level++
    }
}