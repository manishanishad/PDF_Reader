package com.pdfreader.pdfviewer.sign.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ItemViewModel : ViewModel() {
    private val mutableSelectedItemHome = MutableLiveData<String>()
    val selectedItemHome: LiveData<String> get() = mutableSelectedItemHome

    private val mutableSelectedItemRecent = MutableLiveData<String>()
    val selectedItemRecent: LiveData<String> get() = mutableSelectedItemRecent

    private val mutableSelectedItemFavF = MutableLiveData<String>()
    val selectedItemFavF: LiveData<String> get() = mutableSelectedItemFavF

    private val mutableSelectedItemFav = MutableLiveData<String>()
    val selectedItemFav: LiveData<String> get() = mutableSelectedItemFav

    fun onSearchHome(search: String) {
        mutableSelectedItemHome.value = search
    }

    fun onSearchRecent(search: String) {
        mutableSelectedItemRecent.value = search
    }

    fun onSearchFav(search: String) {
        mutableSelectedItemFavF.value = search
    }

    fun onClearSearchH(search: String) {
        mutableSelectedItemHome.value = search
    }

    fun onClearSearchR(search: String) {
        mutableSelectedItemRecent.value = search
    }

    fun onClearSearchF(search: String) {
        mutableSelectedItemFavF.value = search
    }

    fun checkFavPos(checked: String) {
        mutableSelectedItemFav.value = checked
    }

    fun checkFavPosRecent(checked: String) {
        mutableSelectedItemFav.value = checked
    }
}