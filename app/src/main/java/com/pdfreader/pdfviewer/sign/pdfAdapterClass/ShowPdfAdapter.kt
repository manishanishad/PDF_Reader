package com.pdfreader.pdfviewer.sign.pdfAdapterClass

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfreader.modalClas.PdfList
import com.google.gson.Gson
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.common.AppUtils.Companion.toSimpleString
import com.pdfreader.pdfviewer.sign.multiSelectedActivity.MultipleSelectedActivity
import com.pdfreader.pdfviewer.sign.pdfViewer.PdfViewActivity
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager.Companion.PREF_FAVORITE
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager.Companion.PREF_RECENT
import com.reader.office.constant.MainConstant
import com.reader.office.officereader.AppActivity
import kotlinx.android.synthetic.main.item_pdf_list.view.ivPdfFav
import kotlinx.android.synthetic.main.item_pdf_list.view.ivPdfIcon
import kotlinx.android.synthetic.main.item_pdf_list.view.ivPdfMenu
import kotlinx.android.synthetic.main.item_pdf_list.view.tvPdfDate
import kotlinx.android.synthetic.main.item_pdf_list.view.tvPdfName
import kotlinx.android.synthetic.main.item_pdf_list.view.tvPdfSize
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


@SuppressLint("SimpleDateFormat")
class ShowPdfAdapter(
    private var dummyList: ArrayList<PdfList>,
    private var pdfList: ArrayList<PdfList>,
    private val context: Context
) :
    RecyclerView.Adapter<ShowPdfAdapter.MyViewHolder>() {

    private var onClickListener: OnItemClickListener? = null
    private val gson = Gson()
    private var formatter: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")

    fun setOnItemClickListener(onClickListener: OnItemClickListener) {
        this.onClickListener = onClickListener
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfImage: ImageView = itemView.ivPdfIcon
        val pdfName: TextView = itemView.tvPdfName
        val pdfDate: TextView = itemView.tvPdfDate
        val pdfSize: TextView = itemView.tvPdfSize
        val pdfFav: ImageView = itemView.ivPdfFav
        val pdfMenu: ImageView = itemView.ivPdfMenu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_pdf_list, parent, false)
        return MyViewHolder(view)
    }

    interface OnItemClickListener {
        fun onFavClick(view: View?, obj: ArrayList<PdfList>, pos: Int)
        fun onMenuDialog(view: View?, obj: ArrayList<PdfList>, pos: Int)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.pdfName.text = pdfList[position].pdfName
        holder.pdfDate.text = pdfList[position].pdfDate?.let { formatter.format(it) }
        holder.pdfSize.text = pdfList[position].pdfSize.toString()
        when (pdfList[position].pdfExt) {
            "pdf" -> holder.pdfImage.ivPdfIcon.setImageResource(R.drawable.ic_pdf)
            "docx", "doc" -> holder.pdfImage.ivPdfIcon.setImageResource(R.drawable.ic_docs)
            "xlsx", "xls" -> holder.pdfImage.ivPdfIcon.setImageResource(R.drawable.ic_xls)
            "ppt", "pptx" -> holder.pdfImage.ivPdfIcon.setImageResource(R.drawable.ic_ppt)
            "txt" -> holder.pdfImage.ivPdfIcon.setImageResource(R.drawable.ic_txt)
        }

        if (pdfList[position].isFav) {
            holder.pdfFav.ivPdfFav.setImageResource(R.drawable.ic_fill_star)
        } else {
            holder.pdfFav.ivPdfFav.setImageResource(R.drawable.ic_unfill_star)
        }

        holder.pdfFav.setOnClickListener(
            View.OnClickListener { v ->
                if (onClickListener == null) return@OnClickListener
                onClickListener!!.onFavClick(v, pdfList, holder.adapterPosition)
            })

        holder.pdfMenu.setOnClickListener(
            View.OnClickListener { v ->
                if (onClickListener == null) return@OnClickListener
                onClickListener!!.onMenuDialog(v, pdfList, holder.adapterPosition)
            })

        holder.itemView.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            calendar.time = Date()
            val mSec: Int = calendar.get(Calendar.MILLISECOND)

            pdfList[position].pdfTime = mSec.toLong()

            addRecent(pdfList[position].absPath.toString())

            when (pdfList[position].pdfExt) {
                "pdf" -> {
                    val intent = Intent(context, PdfViewActivity::class.java)
                    intent.putExtra("fileType", 2)
                    intent.putExtra("fileName", pdfList[position].pdfName)
                    intent.putExtra("fileDate", pdfList[position].pdfDate?.toSimpleString())
                    intent.putExtra("fileExt", pdfList[position].pdfExt)
                    intent.putExtra("fileSize", pdfList[position].pdfSize)
                    intent.putExtra("fileFav", pdfList[position].isFav)
                    intent.putExtra("itemPosition", position)
                    intent.putExtra(MainConstant.INTENT_FILED_FILE_PATH, pdfList[position].absPath)
                    (context as Activity).startActivityForResult(intent, 147)
                }

                else -> {
                    val intent = Intent(context, AppActivity::class.java)
                    intent.putExtra("fileType", 2)
                    intent.putExtra("fileName", pdfList[position].pdfName)
                    intent.putExtra("fileDate", pdfList[position].pdfDate?.toSimpleString())
                    intent.putExtra("fileExt", pdfList[position].pdfExt)
                    intent.putExtra("fileSize", pdfList[position].pdfSize)
                    intent.putExtra("fileFav", pdfList[position].isFav)
                    intent.putExtra("itemPosition", position)
                    Log.d(TAG, "PathhhhhhFileee:::: ${pdfList[position].absPath}")
                    intent.putExtra(MainConstant.INTENT_FILED_FILE_PATH, pdfList[position].absPath)
                    (context as Activity).startActivityForResult(intent, 147)
                }
            }
        }

        holder.itemView.setOnLongClickListener {
            context.startActivity(Intent(context, MultipleSelectedActivity::class.java))
            true
        }
    }

    fun deletedData(deletedItem: String?, position: Int) {
        var i = 0
        var j = 0
        val l = dummyList.size
        while (i < l) {
            if (dummyList[i].pdfName == deletedItem) {
                j = i
            }
            i++
        }
        if (dummyList.size != 0) {
            pdfList.removeAt(position)
            dummyList.removeAt(j)
        } else {
            pdfList.removeAt(position)
        }
        notifyDataSetChanged()
    }

    fun updateFav(favPath: String?, checkFav: Boolean, updatedList: ArrayList<PdfList>) {
        updatedList.let {
            for ((index, pdfList1) in it.withIndex()) {
                if (pdfList1.absPath.equals(favPath)) {
                    it[index].isFav = checkFav
                    notifyItemChanged(index)
                    break
                }
            }
        }
    }

    fun updateRename(renamePath: String?, name: String, updatedList: ArrayList<PdfList>) {
        updatedList.let {
            for ((index, pdfList1) in it.withIndex()) {
                if (pdfList1.absPath.equals(renamePath)) {
                    it[index].pdfName = name
                    notifyItemChanged(index)
                    break
                }
            }
        }
    }

    fun addPreferenceList(path: Boolean, absPath: String) {
        val favorList: Array<String>? = gson.fromJson(
            PreferencesManager.getString(context, PREF_FAVORITE),
            Array<String>::class.java
        )
        var categoryList = favorList?.toCollection(ArrayList())
        if (path) {
            if (!categoryList.isNullOrEmpty()) {
                categoryList.add(absPath)
            } else {
                categoryList = arrayListOf()
                categoryList.add(absPath)
            }
        } else {
            if (categoryList!!.size > 0) {
                removeData(absPath, categoryList)
            }
        }
        PreferencesManager.setString(context, PREF_FAVORITE, gson.toJson(categoryList))
    }

    private fun addRecent(absPath: String) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = Date()
        val mSec: Int = calendar.get(Calendar.MILLISECOND)

        val newList: ArrayList<PdfList>

        val recentList: Array<PdfList>? = gson.fromJson(
            PreferencesManager.getString(context, PREF_RECENT),
            Array<PdfList>::class.java
        )

        val addList = recentList?.toCollection(ArrayList())

        if (addList != null) {
            var isMatch = false
            if (addList.size > 0) {
                addList.forEach {
                    if (it.absPath == absPath) {
                        it.pdfTime = mSec.toLong()
                    } else {
                        isMatch = true
                    }
                }
                if (isMatch) {
                    addList.add(PdfList("", Date(), mSec.toLong(), "", 0, "", absPath, false))
                }
            } else {
                newList = arrayListOf()
                newList.add(PdfList("", Date(), mSec.toLong(), "", 0, "", absPath, false))
                PreferencesManager.setString(context, PREF_RECENT, gson.toJson(newList))
            }

        } else {
            newList = arrayListOf()
            newList.add(PdfList("", Date(), mSec.toLong(), "", 0, "", absPath, false))
            PreferencesManager.setString(context, PREF_RECENT, gson.toJson(newList))
        }

        if (addList != null && addList.size > 0) {
            PreferencesManager.setString(context, PREF_RECENT, gson.toJson(addList))
        }
    }

    private fun removeData(absPath: String, categoryList: ArrayList<String>?) {
        var i = 0
        var j = 0
        val l = categoryList!!.size
        while (i < l) {
            if (categoryList[i] == absPath) {
                j = i
            }
            i++
        }
        categoryList.removeAt(j)
    }

    override fun getItemCount(): Int {
        return pdfList.size
    }

    fun setNewData(list: ArrayList<PdfList>) {
        pdfList = list
        notifyDataSetChanged()
    }
}