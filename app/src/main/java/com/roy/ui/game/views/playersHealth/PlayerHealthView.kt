package com.roy.ui.game.views.playersHealth

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import com.roy.R
import com.roy.data.PlayerHealthInfo.MAX_HEALTH
import com.roy.ui.base.BaseCustomView
import com.roy.utils.map
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class PlayerHealthView constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : BaseCustomView(context, attributeSet) {

    private val heartPaint by lazy {
        Paint().apply {
            color = Color.parseColor("#DD3D1F")
        }
    }
    private val circlePaint by lazy {
        Paint().apply {
            color = ResourcesCompat.getColor(context.resources, R.color.shipShadowColor, null)
            strokeWidth = 2F
            style = Paint.Style.STROKE
        }
    }
    private val healthProgress by lazy {
        Paint().apply {
            color = Color.parseColor("#DD3D1F")
            style = Paint.Style.STROKE
            strokeWidth = measuredHeight / 4F
            if (isHardwareAccelerated)
                setShadowLayer(
                    /* radius = */ 12F,
                    /* dx = */ 0F,
                    /* dy = */ 0F,
                    /* shadowColor = */ color
                )
        }
    }
    var onHealthEmpty: (() -> Unit)? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode)
            startObservingHealth()
    }

    var progressLength = 0F

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressLength = map(
            value = com.roy.data.PlayerHealthInfo.getPlayerHealthValue(),
            in_min = 0,
            in_max = MAX_HEALTH,
            out_min = measuredHeight,
            out_max = measuredWidth
        )
    }

    private var valueAnimator: ValueAnimator? = null

    private fun startObservingHealth() {
        lifeCycleOwner.customViewLifeCycleScope.launchWhenCreated {
            com.roy.data.PlayerHealthInfo.getPlayerHealthFlow().collect { life ->
                launch {
                    if (life <= 0) {
                        onHealthEmpty?.invoke()
                    }
                    val progress = map(
                        value = life,
                        in_min = 0,
                        in_max = MAX_HEALTH,
                        out_min = measuredHeight,
                        out_max = measuredWidth
                    )
                    animateProgress(progress)
                }
            }
        }
    }

    private fun animateProgress(progress: Float) {
        if (progressLength != progress) {
            valueAnimator?.cancel()

            valueAnimator = ValueAnimator.ofFloat(progressLength, progress)
                .setDuration(500L).apply {
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener {
                        val currentProgress = it.animatedValue
                        if (currentProgress is Float) {
                            progressLength = currentProgress
                            postInvalidate()
                        }
                    }
                    start()
                }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        val radius = measuredHeight / 2F
        canvas?.drawCircle(
            /* cx = */ radius + paddingLeft,
            /* cy = */ radius + paddingTop,
            /* radius = */ radius,
            /* paint = */ circlePaint
        )
        canvas?.drawLine(measuredHeight.toFloat(), radius, progressLength, radius, healthProgress)
        val path = createHeartPath(
            width = 2 * (radius.roundToInt() + paddingLeft),
            height = measuredHeight
        )
        canvas?.drawPath(/* path = */ path, /* paint = */ heartPaint)
    }

    private fun createHeartPath(width: Int, height: Int): Path {
        val path = Path()
        val bottomPointX = width / 2F
        val bottomPointY = 0.9F * height
        val midPointLength = 0.4 * height
        val topSidePointLength = 0.7 * height

        val controlPointHeight = midPointLength * 0.6

        //start point
        path.moveTo(bottomPointX, bottomPointY)

        var angle = 225.0

        //left mid point
        val midPointLeftX = bottomPointX + midPointLength * cos(Math.toRadians(angle))
        val midPointLeftY = bottomPointY + midPointLength * sin(Math.toRadians(angle))
        path.lineTo(/* x = */ midPointLeftX.toFloat(), /* y = */ midPointLeftY.toFloat())

        angle = 220.0

        //control point left
        val controlPointLeftX =
            midPointLeftX + controlPointHeight * cos(Math.toRadians(angle))
        val controlPointLeftY =
            midPointLeftY + controlPointHeight * sin(Math.toRadians(angle))

        angle = 235.0

        //top left point
        val topLeftPointX = bottomPointX + topSidePointLength * cos(Math.toRadians(angle))
        val topLeftPointY = bottomPointY + topSidePointLength * sin(Math.toRadians(angle))

        path.quadTo(
            /* x1 = */ controlPointLeftX.toFloat(),
            /* y1 = */ controlPointLeftY.toFloat(),
            /* x2 = */ topLeftPointX.toFloat(),
            /* y2 = */ topLeftPointY.toFloat()
        )

        //top control point left

        val offsetXControlPoint = width * 0.2F
        val offsetYControlPoint = 0F

        val controlPointTopX = width / 2 - offsetXControlPoint

        //mid point top
        val midTopX = width / 2F
        val midTopY = height * 0.3F
        path.quadTo(
            /* x1 = */ controlPointTopX,
            /* y1 = */ offsetYControlPoint,
            /* x2 = */ midTopX,
            /* y2 = */ midTopY
        )
        //back to start
        path.lineTo(bottomPointX, bottomPointY)


        angle = 315.0

        //right mid point
        val midPointRightX = bottomPointX + midPointLength * cos(Math.toRadians(angle))
        val midPointRightY = bottomPointY + midPointLength * sin(Math.toRadians(angle))
        path.lineTo(midPointRightX.toFloat(), midPointRightY.toFloat())

        angle = 320.0

        //control point right
        val controlPointRightX =
            midPointRightX + controlPointHeight * cos(Math.toRadians(angle))
        val controlPointRightY =
            midPointRightY + controlPointHeight * sin(Math.toRadians(angle))

        angle = 305.0

        //top right point
        val topRightPointX = bottomPointX + topSidePointLength * cos(Math.toRadians(angle))
        val topRightPointY = bottomPointY + topSidePointLength * sin(Math.toRadians(angle))

        path.quadTo(
            /* x1 = */ controlPointRightX.toFloat(),
            /* y1 = */ controlPointRightY.toFloat(),
            /* x2 = */ topRightPointX.toFloat(),
            /* y2 = */ topRightPointY.toFloat()
        )

        //top control point right

        val controlPointTopXRight = width / 2 + offsetXControlPoint

        path.quadTo(
            /* x1 = */ controlPointTopXRight,
            /* y1 = */ offsetYControlPoint,
            /* x2 = */ midTopX,
            /* y2 = */ midTopY
        )

        return path
    }
}
