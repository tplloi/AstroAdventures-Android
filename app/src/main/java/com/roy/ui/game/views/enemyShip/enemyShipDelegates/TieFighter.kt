package com.roy.ui.game.views.enemyShip.enemyShipDelegates

import android.graphics.*
import com.roy.ui.game.views.enemyShip.EnemyClusterView
import kotlin.random.Random

class TieFighter : IEnemyShip {

    private val drawRect = RectF(
        /* left = */ 0F, /* top = */ 0F, /* right = */ 0F, /* bottom = */ 0F
    )

    var enemyY = 0F

    var enemyX = 0F

    private var coreRadius = 0F

    private var bridgeHeight = 0F


    private val mainColor = Color.rgb(
        Random.nextInt(128, 255),
        Random.nextInt(128, 255),
        Random.nextInt(128, 255)
    )

    private val paint by lazy {
        Paint().apply {
            color = mainColor
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            isAntiAlias = false
            isDither = false
//            setShadowLayer(14F, 0F, 0F, color)
        }
    }

    private val strokePaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            color = mainColor
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            isAntiAlias = false
            strokeWidth = 2F
            isDither = false
//            setShadowLayer(14F, 0F, 0F, color)
        }
    }

    override fun onHit(enemyLife: Int) {
        paint.alpha = 70 * enemyLife
    }

    override fun onDraw(canvas: Canvas) {
        drawBridge(canvas)
        drawWings(canvas)
        canvas.drawCircle(
            /* cx = */ enemyX,
            /* cy = */enemyY,
            /* radius = */coreRadius / 2F,
            /* paint = */paint
        )
    }

    private fun drawWings(canvas: Canvas?) {
        val yStart = enemyY - coreRadius
        val yEnd = enemyY + coreRadius
        canvas?.drawLine(
            /* startX = */ enemyX - coreRadius,
            /* startY = */ yStart,
            /* stopX = */ enemyX - coreRadius,
            /* stopY = */ yEnd,
            /* paint = */ strokePaint
        )
        canvas?.drawLine(
            /* startX = */ enemyX + coreRadius,
            /* startY = */ yStart,
            /* stopX = */ enemyX + coreRadius,
            /* stopY = */ yEnd,
            /* paint = */ strokePaint
        )
    }

    private fun drawBridge(canvas: Canvas?) {
        val path = Path()
        path.moveTo(enemyX, enemyY - bridgeHeight)
        path.lineTo(enemyX - coreRadius, enemyY - 2)
        path.lineTo(enemyX - coreRadius, enemyY + 2)
        path.lineTo(enemyX, enemyY + bridgeHeight)
        path.lineTo(enemyX + coreRadius, enemyY + 2)
        path.lineTo(enemyX + coreRadius, enemyY - 2)
        path.close()
        canvas?.drawPath(path, paint)
    }

    override fun translate(offset: Long) {
        enemyY += EnemyClusterView.speed
        drawRect.offset(/* dx = */ 0F, /* dy = */ EnemyClusterView.speed)
    }

    override fun setInitialSize(
        boxSize: Float,
        positionX: Int,
        positionY: Int,
    ) {
        drawRect.set(
            /* left = */ boxSize * positionX,
            /* top = */ boxSize * positionY,
            /* right = */ boxSize * (positionX + 1),
            /* bottom = */ boxSize * (positionY + 1),
        )
        enemyX = drawRect.centerX()
        enemyY = drawRect.centerY()
        coreRadius = drawRect.width() / 4F
        bridgeHeight = coreRadius / 6
    }

    override fun getPositionX(): Float = enemyX
    override fun getPositionY(): Float = enemyY

    override fun hitBoxRadius(): Float = coreRadius
}
