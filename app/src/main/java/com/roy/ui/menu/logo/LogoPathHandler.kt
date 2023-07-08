package com.roy.ui.menu.logo

import android.graphics.Path
import kotlin.math.abs

class LogoPathHandler(
    private val measuredWidth: Float,
    val measuredHeight: Float,
    var initialPointX: Float,
    var initialPointY: Float,
    private val pathLength: Float,
) {

    var segments: Int = 1

    var drawPath = Path()

    fun startDrawingPath(invalidate: (Path) -> Unit) {
        drawPath.reset()
        drawPath.moveTo(/* x = */ initialPointX, /* y = */ initialPointY)
        drawPath(
            drawPath = drawPath,
            startX = initialPointX,
            startY = initialPointY,
            drawLength = pathLength
        )
        translateAhead()
        invalidate(drawPath)
    }

    private fun translateAhead() {
        when (getDirectionForPath(startX = initialPointX, startY = initialPointY)) {
            LogoTextView.Direction.Right -> {
                initialPointX++
            }

            LogoTextView.Direction.Down -> {
                initialPointY++
            }

            LogoTextView.Direction.Left -> {
                initialPointX--
            }

            LogoTextView.Direction.UP -> {
                initialPointY--
            }
        }
    }

    private fun drawPath(
        drawPath: Path,
        startX: Float,
        startY: Float,
        drawLength: Float,
    ) {
        segments = 1
        val direction = getDirectionForPath(startX, startY)
        val maxLength = getMaxLength(direction)
        if (segments == 1) {
            when (direction) {
                LogoTextView.Direction.Right -> {
                    if (startX + drawLength > maxLength) {
                        val newDrawLength = maxLength - startX
                        drawPath.lineTo(maxLength, startY)
                        segments++
                        drawPath(
                            drawPath = drawPath,
                            startX = maxLength,
                            startY = startY,
                            drawLength = drawLength - newDrawLength
                        )
                    } else {
                        drawPath.lineTo(/* x = */ startX + drawLength, /* y = */ startY)
                    }

                }

                LogoTextView.Direction.Down -> {
                    if (startY + drawLength > maxLength) {
                        val newDrawLength = maxLength - startY
                        drawPath.lineTo(startX, maxLength)
                        segments++
                        drawPath(
                            drawPath = drawPath,
                            startX = startX,
                            startY = maxLength,
                            drawLength = drawLength - newDrawLength
                        )
                    } else {
                        drawPath.lineTo(/* x = */ startX, /* y = */ startY + drawLength)
                    }
                }

                LogoTextView.Direction.Left -> {
                    if (startX - drawLength < maxLength) {
                        val newLength = abs(startX - drawLength)
                        drawPath.lineTo(maxLength, startY)
                        segments++
                        drawPath(
                            drawPath = drawPath,
                            startX = maxLength,
                            startY = startY,
                            drawLength = newLength
                        )
                    } else {
                        drawPath.lineTo(/* x = */ startX - drawLength, /* y = */ startY)
                    }
                }

                LogoTextView.Direction.UP -> {
                    if (startY - drawLength < maxLength) {
                        val newLength = abs(startY - drawLength)
                        drawPath.lineTo(startX, maxLength)
                        segments++
                        drawPath(
                            drawPath = drawPath,
                            startX = startX,
                            startY = maxLength,
                            drawLength = newLength
                        )
                    } else {
                        drawPath.lineTo(/* x = */ startX, /* y = */ startY - drawLength)
                    }
                }
            }
        }
    }

    private fun getMaxLength(direction: LogoTextView.Direction) = when (direction) {
        LogoTextView.Direction.Right -> measuredWidth
        LogoTextView.Direction.Down -> measuredHeight
        LogoTextView.Direction.Left -> 0F
        LogoTextView.Direction.UP -> 0F
    }

    private fun getDirectionForPath(
        startX: Float,
        startY: Float,
    ): LogoTextView.Direction {
        return when {
            startX == 0F && startY == 0F -> {
                LogoTextView.Direction.Right
            }

            startX >= measuredWidth && startY >= 0F && startY < measuredHeight -> {
                LogoTextView.Direction.Down
            }

            startX > 0F && startX <= measuredWidth && startY >= measuredHeight -> {
                LogoTextView.Direction.Left
            }

            startX <= 0F && startY > 0F && startY <= measuredHeight -> {
                LogoTextView.Direction.UP
            }

            else -> {
                LogoTextView.Direction.Right
            }
        }
    }
}
