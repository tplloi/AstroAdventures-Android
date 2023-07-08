package com.roy.ui

sealed class ScreenStates {
    object AppInit : ScreenStates()
    object GameMenu : ScreenStates()
    object LevelStart : ScreenStates()
    object StartLevelZero : ScreenStates()
    object StartGame : ScreenStates()
    object ViewHighScores : ScreenStates()
    object LevelStartWarp : ScreenStates()
    data class LevelComplete(val bulletCount: Int) : ScreenStates()
    object YouDied : ScreenStates()
    object RanOutOfAmmo : ScreenStates()
    object GameOver : ScreenStates()
}
