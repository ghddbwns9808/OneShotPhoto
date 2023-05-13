package org.techtown.oneshotphoto

import android.app.Application

class App: Application() {
    companion object{
        lateinit var prefs: AutoLoginSharedPrefs
    }
    override fun onCreate() {
        prefs = AutoLoginSharedPrefs(applicationContext)
        super.onCreate()
    }
}