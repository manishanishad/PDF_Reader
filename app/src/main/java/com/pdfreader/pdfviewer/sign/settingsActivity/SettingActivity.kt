package com.pdfreader.pdfviewer.sign.settingsActivity

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.pdfreader.pdfviewer.sign.BuildConfig
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.languageActivity.LanguageActivity
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
import kotlinx.android.synthetic.main.activity_setting.clLanguage
import kotlinx.android.synthetic.main.activity_setting.clStorage
import kotlinx.android.synthetic.main.activity_setting.llFeedBack
import kotlinx.android.synthetic.main.activity_setting.llInviteFriend
import kotlinx.android.synthetic.main.activity_setting.llPrivacy
import kotlinx.android.synthetic.main.activity_setting.tvSavePathLocation
import kotlinx.android.synthetic.main.activity_setting.tvSelectLanguage
import kotlinx.android.synthetic.main.activity_setting.tvVersion
import kotlinx.android.synthetic.main.home_toolbar.ivDrawerMenu
import kotlinx.android.synthetic.main.home_toolbar.ivSearch
import kotlinx.android.synthetic.main.home_toolbar.tvPdfReader
import java.io.File

class SettingActivity : AppCompatActivity(), View.OnClickListener {

    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        AppUtils.changeStatusBarColor(this)
        window.statusBarColor = getColor(R.color.bgCommon)
        initView()
        clickListener()
    }

    private fun initView() {
        tvVersion.text = getString(R.string._v_).plus(" ").plus(BuildConfig.VERSION_NAME)
        tvPdfReader.text = getString(R.string.settings)

        val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + "sign_check"
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + "sign_check")
        }

        tvSavePathLocation.text = file.toString()

        ivDrawerMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_back))
        ivSearch.visibility = View.GONE

        val langCode = PreferencesManager.getString(this, PreferencesManager.PREF_LANGUAGE)
        Log.d(TAG, "Languagee::>>> $langCode")
        when (langCode) {
            "en" -> tvSelectLanguage.text = "English(US)"
            "es" -> tvSelectLanguage.text = "Spanish"
            "fr" -> tvSelectLanguage.text = "French"
            "de" -> tvSelectLanguage.text = "German"
            "it" -> tvSelectLanguage.text = "Italian"
            "pt" -> tvSelectLanguage.text = "Portuguese"
            "ko" -> tvSelectLanguage.text = "Korean"
        }
    }

    private fun clickListener() {
        ivDrawerMenu.setOnClickListener(this)
        clStorage.setOnClickListener(this)
        clLanguage.setOnClickListener(this)
        llInviteFriend.setOnClickListener(this)
        llFeedBack.setOnClickListener(this)
        llPrivacy.setOnClickListener(this)
    }

    private fun shareApp() {
        try {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return
            }
            mLastClickTime = SystemClock.elapsedRealtime()

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
            var shareMessage =
                "\nHi! I Just checked this app in play store, You must try it out:\n\n"
            shareMessage =
                """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    resources.getString(R.string.pdf_reader)
                )
            )
        } catch (e: Exception) {
        }
    }

    private fun openPrivacyPolicy() {
        val url = "https://sites.google.com/view/pdfappprivacy/home"
        if (url.startsWith("https://") || url.startsWith("http://")) {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivDrawerMenu -> finish()
            R.id.clStorage -> {}
            R.id.clLanguage -> {
                startActivity(
                    Intent(this, LanguageActivity::class.java).putExtra(
                        "LANG_DIRECTION",
                        true
                    )
                )
                //finish()
            }

            R.id.llInviteFriend -> shareApp()
            R.id.llFeedBack -> AppUtils.moveToEmail(this)
            R.id.llPrivacy -> openPrivacyPolicy()
        }
    }
}