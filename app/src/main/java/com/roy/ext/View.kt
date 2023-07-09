package com.roy.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.*
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList


fun View?.setMargins(
    leftPx: Int, topPx: Int, rightPx: Int, bottomPx: Int,
) {
    this?.let {
        if (it.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = it.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(leftPx, topPx, rightPx, bottomPx)
            it.requestLayout()
        }
    }
}

fun View.getAllChildren(): ArrayList<View> {
    if (this !is ViewGroup) {
        val viewArrayList = ArrayList<View>()
        viewArrayList.add(this)
        return viewArrayList
    }
    val result = ArrayList<View>()
    for (i in 0 until this.childCount) {
        val child = this.getChildAt(i)
        val viewArrayList = ArrayList<View>()
        viewArrayList.add(this)
        viewArrayList.addAll(child.getAllChildren())
        result.addAll(viewArrayList)
    }
    return result
}

fun View.getWidthOfView(): Int {
    this.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    return this.measuredWidth
}

fun View.getHeightOfView(): Int {
    this.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    return this.measuredHeight
}

fun View?.setSizeOfView(
    width: Int? = null, height: Int? = null,
) {
    this?.let { v ->
        width?.let {
            v.layoutParams.width = width
        }
        height?.let {
            v.layoutParams.height = height
        }
        v.requestLayout()
    }
}

fun View.setRipple() {
    val outValue = TypedValue()
    this.context.theme.resolveAttribute(
        /* resid = */ android.R.attr.selectableItemBackground,
        /* outValue = */ outValue,
        /* resolveRefs = */ true
    )
    this.setBackgroundResource(outValue.resourceId)
}

@Suppress("unused")
fun View?.hideSoftInput(
) {
    this?.let {
        val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

fun View.click(
    runnable: Runnable? = null,
) {
    this.setOnClickListener {
        this.playAnim({
            runnable?.run()
        })
    }
}

fun View.playAnim(
    runnableAnimEnd: Runnable? = null,
    maxScale: Float = 1.5f,
    duration: Long = 100L,
) {
    val anim = ValueAnimator.ofFloat(1f, maxScale)
    anim.duration = duration
    anim.addUpdateListener { animation ->
        this.scaleX = animation.animatedValue as Float
        this.scaleY = animation.animatedValue as Float
    }
    anim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            runnableAnimEnd?.run()
        }
    })
    anim.repeatCount = 1
    anim.repeatMode = ValueAnimator.REVERSE
    anim.start()
}

fun testCrash() {
    throw RuntimeException(
        "FIREBASE CRASHLYTICS TEST::" + DateFormat.getDateTimeInstance().format(Date())
    )
}
