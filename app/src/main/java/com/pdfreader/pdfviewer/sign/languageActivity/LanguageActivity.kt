package com.pdfreader.pdfviewer.sign.languageActivity

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.example.pdfreader.dummyData.dummyLanguages
import com.example.pdfreader.modalClas.LanguageList
import com.pdfreader.pdfviewer.sign.MainActivity
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager.Companion.PREF_LANGUAGE
import kotlinx.android.synthetic.main.activity_language.rvLanguage
import kotlinx.android.synthetic.main.toolbar_common.ivIcon
import kotlinx.android.synthetic.main.toolbar_common.ivLangBack

class LanguageActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var languageAdapter: LanguageAdapter
    private val data = ArrayList<LanguageList>()
    private var getSelectedItemPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)
        initView()
        clickListener()
    }

    private fun initView() {
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        window.statusBarColor = getColor(R.color.red_black)

        if (intent.getBooleanExtra("LANG_DIRECTION", false)) {
            ivLangBack.visibility = View.VISIBLE
        } else {
            ivLangBack.visibility = View.GONE
        }

        data.addAll(dummyLanguages())

        if (!AppUtils.isEmptyString(PreferencesManager.getString(this, PREF_LANGUAGE))) {
            for (i in data) {
                if (i.langCode.equals(PreferencesManager.getString(this, PREF_LANGUAGE), true)) {
                    i.isSelected = true
                    getSelectedItemPos = data.indexOf(i)
                } else {
                    i.isSelected == false
                }
            }
        } else {
            data[0] = LanguageList(
                data[0].image,
                data[0].languageName,
                data[0].subLanguage,
                data[0].langCode,
                true
            )
        }

        languageAdapter = LanguageAdapter(data, this)
        languageAdapter.setPosition(getSelectedItemPos)
        rvLanguage.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvLanguage.adapter = languageAdapter
    }

    private fun clickListener() {
        ivIcon.setOnClickListener(this)
        ivLangBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivLangBack -> finish()
            R.id.ivIcon -> {
                Log.d(TAG, "CheckkkkDataLang")
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
                getSelectedLanguage()
                AppUtils.changeLanguage(this)
            }
        }
    }

    private fun getSelectedLanguage() {
        var code = Resources.getSystem().configuration.locale.language
        Log.d(TAG, "Languagee>>>> $code")
        for (i in data) {
            if (i.isSelected) {
                code = i.langCode!!
            }
        }
        PreferencesManager.setString(this, PREF_LANGUAGE, code)
    }
}


// Language Code English : en
// Language Code Spanish : es
// Language Code French : fr
// Language Code German : de
// Language Code Italian : it
// Language Code Portuguese : pt
// Language Code Korean : ko