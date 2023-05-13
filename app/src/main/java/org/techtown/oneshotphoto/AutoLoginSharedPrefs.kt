package org.techtown.oneshotphoto

import android.content.Context

class AutoLoginSharedPrefs(context: Context) {
    private val prefsName = "accountInfo"
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    var autoLogin: Boolean?
        get() = prefs.getBoolean("autoLogin", false)
        set(value) {
            prefs.edit().putBoolean("autoLogin", value!!).apply()
        }
}