package com.roy.ext

import android.content.Context
import android.content.Intent
import android.net.Uri

const val URL_POLICY_NOTION =
    "https://loitp.notion.site/loitp/Privacy-Policy-319b1cd8783942fa8923d2a3c9bce60f/"
//const val URL_POLICY_NOTION = "https://roy93group.page.link/term_privacy_and_policy"

/*
         * send email support
         */
fun Context?.sendEmail(
) {
    val emailIntent = Intent(Intent.ACTION_SENDTO)
    emailIntent.data = Uri.parse("mailto: www.muathu@gmail.com")
    this?.startActivity(Intent.createChooser(emailIntent, "Send feedback"))
}

fun Context.openBrowserPolicy(
) {
    this.openUrlInBrowser(url = URL_POLICY_NOTION)
}

fun Context?.openUrlInBrowser(
    url: String?
) {
    if (this == null || url.isNullOrEmpty()) {
        return
    }
    try {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
