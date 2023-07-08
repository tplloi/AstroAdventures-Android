package com.roy.ui.game.views.playerShip

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.drawable.PictureDrawable
import android.hardware.SensorEvent
import android.util.AttributeSet
import android.util.Range
import com.roy.R
import com.roy.data.DropType
import com.roy.data.SoftBodyObjectData
import com.roy.data.SoftBodyObjectType
import com.roy.ui.base.BaseCustomView
import com.roy.ui.game.views.enemyShip.OnCollisionCallBack
import com.roy.utils.AccelerometerManager
import com.roy.utils.HapticService
import com.roy.utils.lowPass
import com.roy.utils.map
import java.util.*
import kotlin.math.roundToInt

class SpaceShipView(
    context: Context,
    attributeSet: AttributeSet? = null,
) :
    BaseCustomView(context, attributeSet), com.roy.data.RigidBodyObject {

    var onCollisionCallBack: OnCollisionCallBack? = null
        set(value) {
            field = value
            collisionDetector.onCollisionCallBack = value
        }

    override val collisionDetector: com.roy.data.CollisionDetector =
        com.roy.data.CollisionDetector(lifeCycleOwner)

    private var accelerometerManager: AccelerometerManager? = null
    lateinit var bulletStore: com.roy.data.BulletStore
    private var currentShipPosition: Float = 0F
    private val bodyPaint = Paint().apply {
        color = Color.parseColor("#DEDEDE")
        isAntiAlias = false
        isDither = false
    }
    private val bodyPaintStroke = Paint().apply {
        color = Color.parseColor("#DEDEDE")
        style = Paint.Style.STROKE
        isAntiAlias = false
        isDither = false
    }
    private val wingsPaintOutline = Paint().apply {
        color = Color.parseColor("#0069DE")
        style = Paint.Style.STROKE
        strokeWidth = 2F
        isAntiAlias = false
        isDither = false
    }
    private val jetPaint = Paint().apply {
        color = Color.parseColor("#F24423")
        isAntiAlias = false
        strokeWidth = 8F
        isDither = false
        setShadowLayer(/* radius = */ 10F, /* dx = */
            0F, /* dy = */
            10F, /* shadowColor = */
            Color.MAGENTA
        )
    }
    private var streamLinedTopPoint = 0f
    private var bodyTopPoint = 0f
    private var wingWidth = 0F
    private var halfWidth = 0F
    private var halfHeight = 0F
    private var missileSize = 0F
    private lateinit var spaceShipPicture: Picture
    private lateinit var pictureDrawable: PictureDrawable
    private var gravityValue = FloatArray(1)
    private var translationXValue = 0F
    private val hapticService by lazy { HapticService(context) }
    private var displayRect = Rect()
    var processAccelerometerValues = true

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        context.theme.obtainStyledAttributes(
            /* set = */ attributeSet,
            /* attrs = */ R.styleable.SpaceShipView,
            /* defStyleAttr = */ 0, /* defStyleRes = */ 0
        ).apply {
            try {
                processAccelerometerValues =
                    getBoolean(R.styleable.SpaceShipView_processAccelerometer, true)
            } finally {
                recycle()
            }
        }
    }

    private fun initPicture() {
        spaceShipPicture = Picture()
        pictureDrawable = PictureDrawable(spaceShipPicture)
        val canvas = spaceShipPicture.beginRecording(measuredWidth, measuredHeight)
        canvas.let {
            drawExhaust(it)
            drawStreamlinedBody(it)
            drawBody(it)
            drawMisc(it)
            drawShipWings(it)

        }
        spaceShipPicture.endRecording()
        pictureDrawable = PictureDrawable(spaceShipPicture)
        postInvalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        accelerometerManager?.stopListening()
    }

    override fun onDraw(canvas: Canvas?) {
        if (isInEditMode) {
            return
        }
        canvas?.let {
            pictureDrawable.bounds = displayRect
            pictureDrawable.draw(canvas)
        }
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        halfWidth = w / 2F
        getDrawingRect(displayRect)
        halfHeight = h / 2F
        currentShipPosition = halfWidth
        streamLinedTopPoint = h / 4F
        bodyTopPoint = h / 3F
        wingWidth = w / 15F
        missileSize = h / 8F
        mainBodyYRange =
            Range(
                /* lower = */ top + streamLinedTopPoint,
                /* upper = */ (top + halfHeight + bodyTopPoint) - missileSize
            )
        wingsYRange =
            Range(
                /* lower = */ (top + halfHeight + bodyTopPoint) - missileSize,
                /* upper = */top + halfHeight + bodyTopPoint
            )
        initPicture()
    }

    private fun drawStreamlinedBody(it: Canvas) {
        bodyPaintStroke.strokeWidth = 10F
        it.drawLine(
            /* startX = */ halfWidth,
            /* startY = */ streamLinedTopPoint,
            /* stopX = */ halfWidth,
            /* stopY = */ measuredHeight - streamLinedTopPoint,
            /* paint = */ bodyPaintStroke
        )
    }

    fun getShipX() = currentShipPosition

    private fun drawBody(it: Canvas) {
        bodyPaintStroke.strokeWidth = 24F
        it.drawLine(
            /* startX = */ halfWidth,
            /* startY = */ bodyTopPoint,
            /* stopX = */ halfWidth,
            /* stopY = */ measuredHeight - bodyTopPoint,
            /* paint = */ bodyPaintStroke
        )
    }

    private fun drawMisc(canvas: Canvas) {
        var startY = halfHeight + bodyTopPoint
        var startX = halfWidth - wingWidth
        canvas.drawMissile(startX, startY)

        startX = halfWidth + wingWidth
        canvas.drawMissile(startX, startY)

        startX = (halfWidth - wingWidth / 2)
        startY = (halfHeight + bodyTopPoint / 3F)
        canvas.drawMissile(startX, startY)

        startX = (halfWidth + wingWidth / 2)
        canvas.drawMissile(startX, startY)
    }

    private fun drawExhaust(canvas: Canvas) {
        val path = Path()

        val topPoint = halfHeight + streamLinedTopPoint / 2

        path.moveTo(
            /* x = */ halfWidth,
            /* y = */ topPoint
        ) // Top

        path.lineTo(
            /* x = */ halfWidth - wingWidth / 10,
            /* y = */ topPoint
        )

        path.lineTo(
            /* x = */ halfWidth - wingWidth / 5,
            /* y = */ halfHeight + streamLinedTopPoint
        )

        path.lineTo(
            /* x = */ halfWidth,
            /* y = */ measuredHeight - bodyTopPoint
        )

        path.moveTo(
            /* x = */ halfWidth + wingWidth / 10,
            /* y = */ topPoint
        ) // Top

        path.lineTo(
            /* x = */ halfWidth + wingWidth / 5,
            /* y = */ halfHeight + streamLinedTopPoint
        )

        path.lineTo(
            /* x = */ halfWidth,
            /* y = */ measuredHeight - bodyTopPoint
        )

        path.close()

        canvas.drawPath(path, jetPaint)
    }

    private fun Canvas.drawMissile(startX: Float, startY: Float) {
        drawLine(
            /* startX = */ startX,
            /* startY = */ startY,
            /* stopX = */ startX,
            /* stopY = */ startY - missileSize,
            /* paint = */ jetPaint
        )
    }

    private fun drawShipWings(canvas: Canvas) {
        val path = Path()

        path.moveTo(halfWidth, halfHeight - bodyTopPoint / 3) // Top

        path.lineTo(
            /* x = */ halfWidth - wingWidth,
            /* y = */ halfHeight + bodyTopPoint
        ) // Left

        path.lineTo(
            /* x = */ halfWidth,
            /* y = */ halfHeight + streamLinedTopPoint / 2
        ) // Return to mid

        path.lineTo(
            /* x = */ halfWidth + wingWidth,
            /* y = */ halfHeight + bodyTopPoint
        ) // Right

        path.close()

        canvas.drawPath(path, bodyPaint)
        canvas.drawPath(path, wingsPaintOutline)
    }

    fun getShipY(): Float = bodyTopPoint

    private fun processSensorEvents(sensorEvent: SensorEvent) {
        lowPass(sensorEvent.values, gravityValue)
        if (processAccelerometerValues) {
            processValues()
        } else {
            if (gravityValue[0] < -3F || gravityValue[0] > 3F) {
                processAccelerometerValues = true
                if (!isCallBackInvoked) {
                    gravityValue[0] = 0F
                    levelZeroCallBackPlayer?.onTilted()
                }
            }
        }
    }

    private fun processValues() {
        translationXValue = map(
            value = gravityValue[0],
            in_min = 6F,
            in_max = -6F,
            out_min = -wingWidth,
            out_max = measuredWidth + wingWidth
        )
        if (translationXValue > wingWidth && translationXValue < measuredWidth - wingWidth) {
            currentShipPosition = translationXValue
            mainBodyXRange = Range(
                currentShipPosition - 24,
                currentShipPosition + 24
            )

            leftWingsXRange = Range(currentShipPosition - wingWidth, mainBodyXRange.lower)
            rightWingsXRange = Range(mainBodyXRange.upper, currentShipPosition + wingWidth)

            displayRect.set(
                /* left = */ (translationXValue - halfWidth).roundToInt(),
                /* top = */ 0,
                /* right = */ (translationXValue + halfWidth).roundToInt(),
                /* bottom = */ measuredHeight
            )
            invalidate()
        }
    }

    private fun addAccelerometerListener() {
        accelerometerManager = AccelerometerManager(context.applicationContext) { sensorEvent ->
            processSensorEvents(sensorEvent)
        }
    }

    fun startGame() {
        addAccelerometerListener()
        accelerometerManager?.startListening()
    }

    private var mainBodyXRange = Range(0F, 0F)
    private var mainBodyYRange = Range(0F, 0F)
    private var leftWingsXRange = Range(0F, 0F)
    private var rightWingsXRange = Range(0F, 0F)
    private var wingsYRange = Range(0F, 0F)

    override fun checkCollision(
        softBodyObjectData: SoftBodyObjectData,
    ) {
        collisionDetector.checkCollision(softBodyObjectData) { softBodyPosition, softBodyObject ->

            if (softBodyPosition.y.roundToInt() > top) {
                if (mainBodyYRange.contains(softBodyPosition.y))
                    if (mainBodyXRange.contains(softBodyPosition.x)) {
                        onPlayerHit(softBodyObject)
                    }

                if (wingsYRange.contains(softBodyPosition.y))
                    if (leftWingsXRange.contains(
                            softBodyPosition.x
                        ) || rightWingsXRange.contains(softBodyPosition.x)
                    ) {
                        onPlayerHit(softBodyObject)
                    }

            }
        }
    }

    private fun onPlayerHit(softBodyObject: SoftBodyObjectData) {
        when (softBodyObject.objectType) {
            SoftBodyObjectType.BULLET -> {
                onHitCallBack()
                hapticService.performHapticFeedback(64, 48)
                com.roy.data.PlayerHealthInfo.onHit()
            }

            is SoftBodyObjectType.DROP -> {
                when (softBodyObject.objectType.dropType) {
                    is DropType.Ammo -> {
                        onAmmoCollectedCallback.invoke()
                        hapticService.performHapticFeedback(128, 48)
                        if (::bulletStore.isInitialized)
                            bulletStore.addAmmo(softBodyObject.objectType.dropType.ammoCount)
                    }
                }
            }
        }
        collisionDetector.onHitRigidBody(softBodyObject)

    }

    override fun removeSoftBodyEntry(bullet: UUID) {
        collisionDetector.removeSoftBodyEntry(bullet)
    }

    var levelZeroCallBackPlayer: LevelZeroCallBackPlayer? = null
        set(value) {
            field = value
            isCallBackInvoked = false
        }

    private var isCallBackInvoked = true

    var onHitCallBack: () -> Unit = {}

    var onAmmoCollectedCallback: () -> Unit = {}
}
