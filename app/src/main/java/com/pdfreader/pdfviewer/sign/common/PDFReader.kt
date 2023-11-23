package com.pdfreader.pdfviewer.sign.common

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal

class PDFReader : Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        OneSignal.initWithContext(this)
        OneSignal.setAppId("04992535-83ec-40c8-a452-1d0cafabfd35")
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //MultiDex.install(this)
    }
}