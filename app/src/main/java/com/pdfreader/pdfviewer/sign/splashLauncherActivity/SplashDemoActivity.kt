package com.pdfreader.pdfviewer.sign.splashLauncherActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pdfreader.pdfviewer.sign.MainActivity
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.pdfreader.pdfviewer.sign.languageActivity.LanguageActivity
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
import com.pdfreader.pdfviewer.sign.welcomeActivity.WelcomeActivity

class SplashDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_demo)
        /* val languageList = dummyLanguagesCode()
         val systemLang = Resources.getSystem().configuration.locale.language

         if (languageList.contains(systemLang)) {
             PreferencesManager.setString(this, PreferencesManager.PREF_LANGUAGE, systemLang)
         }*/

        AppUtils.changeStatusBarColor(this)
        window.statusBarColor = getColor(R.color.bgCommon)

        AppUtils.changeLanguage(this)

        /*ThreadUtil.startTask({
            checkCondition()
            finish()
        }, 1000)*/
        Handler(Looper.getMainLooper()).postDelayed({
            checkCondition()
            finish()
        }, 1000)
    }

    private fun checkCondition() {
        if (PreferencesManager.getBoolean(
                this,
                PreferencesManager.PREF_GET_START
            )
        ) {
            if (AppUtils.isEmptyString(
                    PreferencesManager.getString(
                        this,
                        PreferencesManager.PREF_LANGUAGE
                    )
                )
            ) {
                startActivity(
                    Intent(this, LanguageActivity::class.java).putExtra(
                        "LANG_DIRECTION",
                        false
                    )
                )
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }
}