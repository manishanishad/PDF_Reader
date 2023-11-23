package com.pdfreader.pdfviewer.sign.signActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.pdf.PDSSignatureUtils
import com.pdfreader.pdfviewer.sign.pdf.SignatureView
import kotlinx.android.synthetic.main.single_sign_item.view.ivClose
import kotlinx.android.synthetic.main.single_sign_item.view.mainLayout
import java.io.File

class SignAdapter(private val signatures: List<File>, private val context: Context) :
    RecyclerView.Adapter<SignAdapter.MyViewHolder>() {

    private var onClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onClickListener: OnItemClickListener?) {
        this.onClickListener = onClickListener
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var layout: FrameLayout = v.mainLayout
        var deleteSignature: ImageView = v.ivClose
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): MyViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.single_sign_item, viewGroup, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        var signatureView = myViewHolder.layout.getChildAt(0) as SignatureView
        if (signatureView != null) {
            myViewHolder.layout.removeViewAt(0)
        }
        signatureView = PDSSignatureUtils.showFreeHandView(context, signatures[i])
        myViewHolder.layout.addView(signatureView)

        myViewHolder.layout.setOnClickListener(View.OnClickListener { v ->
            if (onClickListener == null) return@OnClickListener
            onClickListener!!.onItemClick(v, signatures[i], myViewHolder.adapterPosition)
        })

        signatureView.setOnClickListener(View.OnClickListener { v ->
            if (onClickListener == null) return@OnClickListener
            onClickListener!!.onItemClick(v, signatures[i], myViewHolder.adapterPosition)
        })

        myViewHolder.deleteSignature.setOnClickListener(View.OnClickListener { v ->
            if (onClickListener == null) return@OnClickListener
            onClickListener!!.onDeleteItemClick(v, signatures[i], myViewHolder.adapterPosition)
        })
    }

    override fun getItemCount(): Int {
        return signatures.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: File?, pos: Int)
        fun onDeleteItemClick(view: View?, obj: File?, pos: Int)
    }
}