package com.sulv.lifeplan

import android.app.Application
import di.initKoin
import org.koin.android.ext.koin.androidContext

class LifePlanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@LifePlanApplication)
        }
    }
}