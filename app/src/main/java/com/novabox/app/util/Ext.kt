package com.novabox.app.util

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast

object Prefs {
    private lateinit var sp: SharedPreferences

    fun init(ctx: Context) {
        sp = ctx.applicationContext.getSharedPreferences("novabox", Context.MODE_PRIVATE)
    }

    var userAgent: String
        get() = sp.getString("ua", DEFAULT_UA) ?: DEFAULT_UA
        set(v) = sp.edit().putString("ua", v).apply()

    var referer: String
        get() = sp.getString("referer", "") ?: ""
        set(v) = sp.edit().putString("referer", v).apply()

    var cookie: String
        get() = sp.getString("cookie", "") ?: ""
        set(v) = sp.edit().putString("cookie", v).apply()

    var autoPlay: Boolean
        get() = sp.getBoolean("auto_play", true)
        set(v) = sp.edit().putBoolean("auto_play", v).apply()

    var bannerEnable: Boolean
        get() = sp.getBoolean("banner", true)
        set(v) = sp.edit().putBoolean("banner", v).apply()

    var lastSourceId: String
        get() = sp.getString("last_source", "") ?: ""
        set(v) = sp.edit().putString("last_source", v).apply()

    const val DEFAULT_UA =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"
}

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
