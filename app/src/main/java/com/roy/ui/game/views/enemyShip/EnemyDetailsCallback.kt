package com.roy.ui.game.views.enemyShip

interface EnemyDetailsCallback {
    fun onAllEliminated(ammoCount: Int)
    fun onCanonReady(enemyX: Float, enemyY: Float)
    fun hasDrop(enemyX: Float, enemyY: Float)
    fun onGameOver()
}
