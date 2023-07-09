package com.roy.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.roy.R

fun Context.isDefaultLauncher(): Boolean {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)
    val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.packageManager.resolveActivity(
            intent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
        )
    } else {
        @Suppress("DEPRECATION")
        this.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    }

    val currentLauncherName = resolveInfo?.activityInfo?.packageName
    if (currentLauncherName == this.packageName) {
        return true
    }
    return false
}

//mo hop thoai de select launcher default
fun Activity.chooseLauncher(cls: Class<*>) {
    val componentName = ComponentName(this, cls)
    this.packageManager.setComponentEnabledSetting(
        /* p0 = */ componentName,
        /* p1 = */ PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        /* p2 = */ PackageManager.DONT_KILL_APP
    )
    val selector = Intent(Intent.ACTION_MAIN)
    selector.addCategory(Intent.CATEGORY_HOME)
    selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    this.startActivity(selector)
    this.packageManager.setComponentEnabledSetting(
        /* p0 = */ componentName,
        /* p1 = */ PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
        /* p2 = */ PackageManager.DONT_KILL_APP
    )
}

//go mot app bat ky nao do
@Suppress("unused")
fun Activity.uninstallApp(
    packageName: String,
) {
    val intent = Intent(Intent.ACTION_DELETE)
    intent.data = Uri.parse("package:$packageName")
    this.startActivity(intent)
}

fun Activity.getScreenOrientation(): Int {
    return this.resources.configuration.orientation
}

@Suppress("unused")
fun Activity.setSoftInputMode(
    mode: Int,
) {
    this.window.setSoftInputMode(mode)
}

fun Activity.rateApp(
    packageName: String,
) {
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")
            )
        )
    } catch (e: android.content.ActivityNotFoundException) {
        e.printStackTrace()
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}

fun Activity.moreApp(
    nameOfDeveloper: String = "Roy93Group",
) {
    val uri = "https://play.google.com/store/apps/developer?id=$nameOfDeveloper"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    this.startActivity(intent)
}

fun Activity.shareApp(
) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
        var sAux = "\n${this.getString(R.string.more_app_loitpp)}\n\n"
        sAux = sAux + "https://play.google.com/store/apps/details?id=" + this.packageName
        intent.putExtra(Intent.EXTRA_TEXT, sAux)
        this.startActivity(Intent.createChooser(intent, "Vui lòng chọn"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.share(
    msg: String,
) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name))
        // String sAux = "\nỨng dụng này rất bổ ích, thân mời bạn tải về cài đặt để trải nghiệm\n\n";
        // sAux = sAux + "https://play.google.com/store/apps/details?id=" + activity.getPackageName();
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        this.startActivity(Intent.createChooser(intent, "Share via"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// like fanpage
fun Activity?.likeFacebookFanpage(
) {
    this?.apply {
        val facebookIntent = Intent(Intent.ACTION_VIEW)
        val facebookUrl = getFacebookPageURL()
        facebookIntent.data = Uri.parse(facebookUrl)
        startActivity(facebookIntent)
    }
}

fun Context.getFacebookPageURL(): String {
    val facebookUrl = "https://www.facebook.com/hoidammedocsach"
    val facebookPageId = "hoidammedocsach"
    val packageManager = this.packageManager
    return try {
        val versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode
        if (versionCode >= 3002850) {
            "fb://facewebmodal/f?href=$facebookUrl"
        } else {
            "fb://page/$facebookPageId"
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        facebookUrl
    }
}

val screenWidth: Int
    get() = Resources.getSystem().displayMetrics.widthPixels

val screenHeight: Int
    get() = Resources.getSystem().displayMetrics.heightPixels

//fun Context.getStatusBarHeight(): Int {
//    var result = 0
//    val resourceId = this.resources.getIdentifier(
//        "status_bar_height", "dimen", "android"
//    )
//    if (resourceId > 0) {
//        result = this.resources.getDimensionPixelSize(resourceId)
//    }
//    return result
//}

fun Context.getScreenHeightIncludeNavigationBar(): Int {
    val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val outPoint = Point()
    // include navigation bar
    display.getRealSize(outPoint)
    val mRealSizeHeight: Int = if (outPoint.y > outPoint.x) {
        outPoint.y
        // mRealSizeWidth = outPoint.x;
    } else {
        outPoint.x
        // mRealSizeWidth = outPoint.y;
    }
    return mRealSizeHeight
}

@SuppressLint("ObsoleteSdkInt")
fun Activity.showStatusBar(
) {
    if (Build.VERSION.SDK_INT < 16) {
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    } else {
        val decorView = this.window.decorView
        // Show Status Bar.
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
    }
}

@SuppressLint("ObsoleteSdkInt")
fun Activity.hideStatusBar(
) {
    // Hide Status Bar
    if (Build.VERSION.SDK_INT < 16) {
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    } else {
        val decorView = this.window.decorView
        // Hide Status Bar.
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
    }
}

fun Activity.toggleFullscreen(
) {
    val attrs = this.window.attributes
    attrs.flags = attrs.flags xor WindowManager.LayoutParams.FLAG_FULLSCREEN
    // attrs.flags ^= WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
    // attrs.flags ^= WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
    this.window.attributes = attrs
    /*if (isFullScreen(activity)) {
        hideNavigationBar(activity)
    } else {
        showNavigationBar(activity)
    }*/
}

fun Activity.toggleFullscreen(
    isFullScreen: Boolean,
) {
    if (isFullScreen) {
        this.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    } else {
        this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}

fun Activity.hideNavigationBar(
) {
    // set navigation bar status, remember to disable "setNavigationBarTintEnabled"
    val flags =
        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    // This work only for android 4.4+
    this.window.decorView.systemUiVisibility = flags

    // Code below is to handle presses of Volume up or Volume down.
    // Without this, after pressing volume buttons, the navigation bar will
    // show up and won't hide
    val decorView = this.window.decorView
    decorView.setOnSystemUiVisibilityChangeListener { visibility ->
        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
            decorView.systemUiVisibility = flags
        }
    }
}

fun Activity.showNavigationBar(
) {
    // set navigation bar status, remember to disable "setNavigationBarTintEnabled"
    val flags =
        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    // This work only for android 4.4+
    this.window.decorView.systemUiVisibility = flags

    // Code below is to handle presses of Volume up or Volume down.
    // Without this, after pressing volume buttons, the navigation bar will
    // show up and won't hide
    val decorView = this.window.decorView
    decorView.setOnSystemUiVisibilityChangeListener { visibility ->
        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
            decorView.systemUiVisibility = flags
        }
    }
}

@SuppressLint("ObsoleteSdkInt")
fun Activity.hideDefaultControls(
) {
    val window = this.window ?: return
    window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    val decorView = window.decorView
    var uiOptions = decorView.systemUiVisibility
    if (Build.VERSION.SDK_INT >= 14) {
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
    }
    if (Build.VERSION.SDK_INT >= 16) {
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    if (Build.VERSION.SDK_INT >= 19) {
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
    decorView.systemUiVisibility = uiOptions
}

@SuppressLint("ObsoleteSdkInt")
fun Activity.showDefaultControls(
) {
    val window = this.window ?: return
    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    val decorView = window.decorView
    var uiOptions = decorView.systemUiVisibility
    if (Build.VERSION.SDK_INT >= 14) {
        uiOptions = uiOptions and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
    }
    if (Build.VERSION.SDK_INT >= 16) {
        uiOptions = uiOptions and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
    }
    if (Build.VERSION.SDK_INT >= 19) {
        uiOptions = uiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
    }
    decorView.systemUiVisibility = uiOptions
}

//fun Activity.setKeyboardVisibilityListener(onKeyboardVisibilityListener: OnKeyboardVisibilityListener) {
//    val parentView = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
//    parentView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//        private var alreadyOpen = false
//        private val defaultKeyboardHeightDP = 100
//        private val EstimatedKeyboardDP =
//            defaultKeyboardHeightDP + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 48 else 0
//        private val rect = Rect()
//        override fun onGlobalLayout() {
//            val estimatedKeyboardHeight = TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP,
//                EstimatedKeyboardDP.toFloat(),
//                parentView.resources.displayMetrics
//            ).toInt()
//            parentView.getWindowVisibleDisplayFrame(rect)
//            val heightDiff = parentView.rootView.height - (rect.bottom - rect.top)
//            val isShown = heightDiff >= estimatedKeyboardHeight
//            if (isShown == alreadyOpen) {
//                return
//            }
//            alreadyOpen = isShown
//            onKeyboardVisibilityListener.onVisibilityChanged(isShown)
//        }
//    })
//}
