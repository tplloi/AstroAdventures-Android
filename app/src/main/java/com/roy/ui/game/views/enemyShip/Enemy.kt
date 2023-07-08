package com.roy.ui.game.views.enemyShip

import android.graphics.Canvas
import android.util.Range
import com.roy.data.Score
import com.roy.ui.game.views.enemyShip.enemyShipDelegates.AlienShip
import com.roy.ui.game.views.enemyShip.enemyShipDelegates.CapitalShip
import com.roy.ui.game.views.enemyShip.enemyShipDelegates.IEnemyShip
import com.roy.ui.game.views.enemyShip.enemyShipDelegates.TieFighter
import kotlin.random.Random

class Enemy {

    var isVisible: Boolean = true

    val hasDrops: Boolean by lazy {
        val chance = Random.nextDouble(0.0, 1.0)
        chance > 0.8
    }

    var enemyLife = Random.nextInt(1, 4)

    val enemyDelegate: IEnemyShip by lazy {
        when (enemyLife) {
            1 -> CapitalShip()
            2 -> AlienShip()
            else -> TieFighter()
        }
    }

    private val points = enemyLife * 25L

    val enemyX: Float
        get() = enemyDelegate.getPositionX()

    val enemyY: Float
        get() = enemyDelegate.getPositionY()

    val hitBoxRadius: Float
        get() = enemyDelegate.hitBoxRadius()

    fun onHit() {
        enemyLife--
        if (enemyLife <= 0) {
            Score.updateScore(points)
        }
        enemyDelegate.onHit(enemyLife)
        isVisible = enemyLife > 0
    }

    companion object {
        fun builder(
            columnSize: Int,
            width: Int,
            positionX: Int,
            positionY: Int,
        ): Enemy {
            return Enemy().apply {
                val boxSize = width / columnSize.toFloat()
                enemyDelegate.setInitialSize(
                    boxSize = boxSize,
                    positionX = positionX,
                    positionY = positionY
                )
            }
        }
    }

    fun onDraw(canvas: Canvas?) {
        if (isVisible) {
            canvas?.let {
                enemyDelegate.onDraw(canvas)
            }
        }
    }

    fun checkEnemyYPosition(bulletY: Float): Boolean {
        return Range(
            /* lower = */ enemyDelegate.getPositionY() - enemyDelegate.hitBoxRadius(),
            /* upper = */ enemyDelegate.getPositionY() + enemyDelegate.hitBoxRadius()
        ).contains(bulletY) && isVisible
    }

    fun translate(offset: Long) {
        enemyDelegate.translate(offset)
    }

}
