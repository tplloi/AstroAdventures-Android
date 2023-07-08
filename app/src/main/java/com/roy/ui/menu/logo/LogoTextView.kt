package com.roy.ui.menu.logo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class LogoTextView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : AppCompatTextView(context, attributeSet, defStyle) {

    private val borderPaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 10F
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isDither = false
            isAntiAlias = false
            color = Color.parseColor("#E4962B")
            if (isHardwareAccelerated)
                setShadowLayer(
                    /* radius = */ 12F,
                    /* dx = */0F,
                    /* dy = */0F,
                    /* shadowColor = */color
                )
        }
    }
    private val logoPathHandlerList: MutableList<LogoPathHandler> = mutableListOf()

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        logoPathHandlerList.clear()
        logoPathHandlerList.add(
            LogoPathHandler(
                measuredWidth = w.toFloat(),
                measuredHeight = h.toFloat(),
                initialPointX = 0F,
                initialPointY = 0F,
                pathLength = w.toFloat()
            )
        )
        logoPathHandlerList.add(
            LogoPathHandler(
                measuredWidth = w.toFloat(),
                measuredHeight = h.toFloat(),
                initialPointX = w.toFloat(),
                initialPointY = h.toFloat(),
                pathLength = w.toFloat()
            )
        )
        if (h != 0)
            borderPaint.pathEffect = CornerPathEffect(12f)
    }

    enum class Direction {
        Right,
        Down,
        Left,
        UP
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isInEditMode) {
            return
        }
        canvas?.let {
            logoPathHandlerList.forEach {
                it.startDrawingPath { path ->
                    canvas.drawPath(path, borderPaint)
                }
            }
            postInvalidate()
        }
    }

}
