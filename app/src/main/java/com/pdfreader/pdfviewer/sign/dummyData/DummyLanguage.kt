package com.example.pdfreader.dummyData

import android.content.res.Resources
import com.example.pdfreader.modalClas.LanguageList
import com.pdfreader.pdfviewer.sign.R

val deviceLang = Resources.getSystem().configuration.locale.language

fun dummyLanguages(): ArrayList<LanguageList> {
    val list: ArrayList<LanguageList> = ArrayList()
    /*list.add(
        LanguageList(
            R.drawable.ic_default,
            "Default",
            "System Language",
            Resources.getSystem().configuration.locale.language
        )
    )*/
    list.add(
        LanguageList(
            R.drawable.ic_english,
            "English",
            "English",
            "en"
        )
    )
    list.add(
        LanguageList(
            R.drawable.ic_spanish,
            "Español",
            "Spanish",
            "es"
        )
    )
    list.add(
        LanguageList(
            R.drawable.ic_french,
            "Françias",
            "French",
            "fr"
        )
    )
    list.add(
        LanguageList(
            R.drawable.ic_german,
            "Duetsch",
            "German",
            "de"
        )
    )
    list.add(
        LanguageList(
            R.drawable.ic_italian,
            "Italiano",
            "Italian",
            "it",
        )
    )
    list.add(
        LanguageList(
            R.drawable.ic_portuguese,
            "Português",
            "Portuguese",
            "pt"
        )
    )
    list.add(
        LanguageList(
            R.drawable.ic_korean,
            "한국어",
            "Korean",
            "ko"
        )
    )
    return list
}

fun dummyLanguagesCode(): ArrayList<String> {
    val list1: ArrayList<String> = ArrayList()
    list1.add("en")
    list1.add("es")
    list1.add("fr")
    list1.add("de")
    list1.add("it")
    list1.add("pt")
    list1.add("ko")
    return list1
}
