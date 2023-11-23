package com.pdfreader.pdfviewer.sign.welcomeActivity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.pdfreader.pdfviewer.sign.languageActivity.LanguageActivity
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
import kotlinx.android.synthetic.main.activity_welcome.btnGetStart
import kotlinx.android.synthetic.main.activity_welcome.tvPrivacyPolicy
import kotlinx.android.synthetic.main.activity_welcome.welCheckbox

class WelcomeActivity : AppCompatActivity(), View.OnClickListener {

    val PERMISSION_REQ_CODE = 20151

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        AppUtils.changeStatusBarColor(this)
        window.statusBarColor = getColor(R.color.bgCommon)
        clickListener()
    }

    private fun clickListener() {
        tvPrivacyPolicy.setOnClickListener(this)
        btnGetStart.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvPrivacyPolicy -> {
                val url = "https://sites.google.com/view/pdfappprivacy/home"
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    val uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }

            R.id.btnGetStart -> {
                if (welCheckbox.isChecked) {
                    PreferencesManager.setBoolean(
                        this,
                        PreferencesManager.PREF_GET_START,
                        true
                    )
                    startActivity(
                        Intent(
                            this,
                            LanguageActivity::class.java
                        ).putExtra("LANG_DIRECTION", false)
                    )
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.please_accept_terms),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                /*if (welCheckbox.isChecked) {
                    PreferencesManager.setBoolean(
                        this@WelcomeActivity,
                        PreferencesManager.PREF_GET_START,
                        true
                    )
                    if (AppUtils.isPermissionGranted(this)) {
                        startActivity(Intent(this, LanguageActivity::class.java))
                        finish()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent =
                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivityForResult(intent, PERMISSION_REQ_CODE)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.please_accept_terms),
                        Toast.LENGTH_SHORT
                    ).show()
                }*/
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQ_CODE) {
            startActivity(
                Intent(this, LanguageActivity::class.java).putExtra(
                    "LANG_DIRECTION",
                    false
                )
            )
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startActivity(
                    Intent(this, LanguageActivity::class.java).putExtra(
                        "LANG_DIRECTION",
                        false
                    )
                )
                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.storage_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}