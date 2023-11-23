package com.pdfreader.pdfviewer.sign.multiSelectedActivity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.common.AppUtils
import kotlinx.android.synthetic.main.multi_selection_toolbar.ivMultiCancel

class MultipleSelectedActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_selected)
        AppUtils.changeStatusBarColor(this)
        window.statusBarColor = getColor(R.color.bgCommon)
        initView()
        clickListener()
    }

    private fun initView() {

    }

    private fun clickListener() {
        ivMultiCancel.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivMultiCancel -> finish()
        }
    }
}