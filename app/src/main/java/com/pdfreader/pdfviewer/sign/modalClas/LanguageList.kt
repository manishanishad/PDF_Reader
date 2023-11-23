package com.example.pdfreader.modalClas

import java.io.Serializable
import java.util.Date

data class LanguageList(
    val image: Int = 0,
    val languageName: String? = null,
    val subLanguage: String? = null,
    val langCode: String? = null,
    var isSelected: Boolean = false
) {
    fun removeOtherSelection(setSelectedPosition: Int, langList: List<LanguageList>) {
        var i = 0;
        val l = langList.size
        while (i < l) {
            langList[i].isSelected = false
            i++
        }
    }
}

data class PdfList(
    var pdfName: String? = null,
    val pdfDate: Date? = null,
    var pdfTime: Long? = null,
    val pdfSize: String? = null,
    val pdfRealSize: Long? = null,
    val pdfExt: String? = null,
    var absPath: String? = null,
    var isFav: Boolean = false,
) : Serializable

data class SignList(
    val signImage: String
)