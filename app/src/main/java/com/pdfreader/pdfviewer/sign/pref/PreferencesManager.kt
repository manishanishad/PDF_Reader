package com.pdfreader.pdfviewer.sign.pref

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager {
    companion object {
        private var PREF_NAME: String = "pdf_reader"
        const val PREF_GET_START: String = "pref_get_start"
        const val PREF_LANGUAGE: String = "pref_language"
        const val PREF_FAVORITE: String = "pref_favorite"
        const val PREF_RECENT: String = "pref_recent"
        const val SET_POSITION_X: String = "pref_position_x"
        const val SET_POSITION_Y: String = "pref_position_y"
        const val TEXT_COLOR: String = "pref_text_color"
        const val SET_TEXT: String = "pref_set_text"
        const val PREF_SORT: String = "pref_sort_list"
        const val PREF_SORT_A_D: String = "pref_sort_list_a_d"
        const val PREF_RATE: String = "pref_rate"

        fun setBoolean(context: Context, key: String, value: Boolean) {
            val sharePreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharePreferences.edit()
            editor.putBoolean(key, value)
            editor.commit()
        }

        fun getBoolean(context: Context, key: String): Boolean {
            val sharePreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return sharePreferences.getBoolean(key, false)
        }

        fun setString(context: Context, key: String, value: String) {
            val sharePreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharePreferences.edit()
            editor.putString(key, value)
            editor.commit()
        }

        fun getString(context: Context, key: String): String {
            val sharePreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return sharePreferences.getString(key, "")!!
        }
    }
}