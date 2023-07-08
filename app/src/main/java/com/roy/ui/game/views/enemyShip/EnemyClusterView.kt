package com.roy.ui.game.views.enemyShip

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.roy.data.GlobalCounter.enemyTimerFlow
import com.roy.data.SoftBodyObjectData
import com.roy.ui.base.BaseCustomView
import com.roy.utils.HapticService
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.*

class EnemyClusterView(
    context: Context,
    attributeSet: AttributeSet? = null,
) :
    BaseCustomView(context = context, attributeSet = attributeSet), com.roy.data.RigidBodyObject {

    companion object {
        var speed = 2F
    }

    private val maxRowsSize = 5

    private var columnSize = 6

    private var rowSize = 1

    lateinit var bulletStore: com.roy.data.BulletStore

    private val hapticService by lazy { HapticService(context) }

    var onCollisionCallBack: OnCollisionCallBack? = null
        set(value) {
            field = value
            collisionDetector.onCollisionCallBack = value
        }

    override val collisionDetector: com.roy.data.CollisionDetector =
        com.roy.data.CollisionDetector(lifeCycleOwner)

    var enemyDetailsCallback: EnemyDetailsCallback? = null

    private val enemyList = mutableListOf(
        EnemyColumn()
    )

    private var translateJob: Job = Job()

    private var firingJob: Job = SupervisorJob()

    var disableInit: Boolean = false

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(/* w = */ w, /* h = */ h, /* oldw = */ oldw, /* oldh = */ oldh)
        if (!disableInit)
            initEnemies()
    }

    init {
        setLayerType(/* layerType = */ LAYER_TYPE_HARDWARE, /* paint = */ null)
        if (rowSize < maxRowsSize) {
            rowSize = com.roy.data.LevelInfo.level + 1
        }
    }

    private fun initEnemies() {
        enemyList.clear()
        repeat(columnSize) { x ->
            val enemiesList = MutableList(rowSize) { y ->
                Enemy.builder(columnSize, measuredWidth, x, y)
            }

            val range = enemiesList.getRangeX()

            enemyList.add(
                EnemyColumn(
                    EnemyLocationRange(range.first, range.second),
                    enemiesList
                )
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        translateJob.cancel()
        firingJob.cancel()
    }

    /**
     * Counter for translating the enemies
     */
    private fun startTranslating() {
        translateJob.cancel()
        translateJob = lifeCycleOwner.customViewLifeCycleScope.launchWhenCreated {
            enemyTimerFlow.collect {
                executeIfActive {
                    enemyList.checkIfYReached(measuredHeight) { hasReachedMax ->
                        if (hasReachedMax) {
                            resetEnemies()
                        }
                        if (enemyList.isNotEmpty()) {
                            translateEnemy(System.currentTimeMillis())
                            invalidate()
                        }
                    }
                }
            }
        }
    }

    private fun fireCanon() {
        if (shouldEmitObjects()) {
            firingJob.cancel()
            firingJob = lifeCycleOwner.customViewLifeCycleScope.launchWhenCreated {
                ticker(delayMillis = 1000, initialDelayMillis = 200).receiveAsFlow().collect {
                    executeIfActive {
                        if (enemyList.isNotEmpty()) {
                            val enemyList = enemyList.random()
                            val enemy = enemyList.enemyList.findLast { it.isVisible }
                            enemy?.let {
                                enemyDetailsCallback?.onCanonReady(enemy.enemyX, enemy.enemyY)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun shouldEmitObjects(): Boolean = com.roy.data.LevelInfo.level != 0

    private fun translateEnemy(millisUntilFinished: Long) {
        enemyList.flattenedForEach { enemy ->
            enemy.translate(millisUntilFinished)
        }
    }

    private fun resetEnemies() {
        enemyList.clear()
        enemyDetailsCallback?.onGameOver()
        hapticService.performHapticFeedback(320)
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        enemyList.flattenedForEach {
            it.onDraw(canvas)
        }
    }

    override fun checkCollision(
        softBodyObjectData: SoftBodyObjectData,
    ) {
        collisionDetector.checkCollision(softBodyObjectData) { softBodyPosition, softBodyObject ->
            enemyList.checkXForEach(softBodyPosition.x) {
                val enemyInLine = it.enemyList.reversed().find {
                    it.checkEnemyYPosition(softBodyPosition.y)
                }

                enemyInLine?.let { enemy ->
                    collisionDetector.onHitRigidBody(softBodyObject)
                    destroyEnemy(enemy)
                    scanForEnemies()
                }
            }
        }
    }

    override fun removeSoftBodyEntry(bullet: UUID) {
        collisionDetector.removeSoftBodyEntry(bullet)
    }

    private fun scanForEnemies() {
        val anyVisible = enemyList.any {
            it.areAnyVisible()
        }
        if (!anyVisible) {
            hapticService.performHapticFeedback(320)
            if (::bulletStore.isInitialized)
                enemyDetailsCallback?.onAllEliminated(bulletStore.getAmmoCount())
        }
    }

    private fun destroyEnemy(enemyInLine: Enemy) {
        enemyList.flattenedForEach {
            if (it == enemyInLine) {
                it.onHit()
            }
        }
        dropGift(enemyInLine)
        hapticService.performHapticFeedback(time = 64, amplitude = 48)
        postInvalidate()
    }

    private fun dropGift(enemyInLine: Enemy) {
        if (enemyInLine.hasDrops && enemyInLine.enemyLife == 0 && shouldEmitObjects()) {
            enemyDetailsCallback?.hasDrop(enemyX = enemyInLine.enemyX, enemyY = enemyInLine.enemyY)
        }
    }

    fun startGame() {
        startTranslating()
        fireCanon()
    }

}

fun List<Enemy>.getRangeX(): Pair<Float, Float> {
    return if (isNotEmpty()) {
        val enemy = get(0)
        Pair(first = enemy.enemyX - enemy.hitBoxRadius, second = enemy.enemyX + enemy.hitBoxRadius)
    } else {
        Pair(first = 0F, second = 0F)
    }
}
