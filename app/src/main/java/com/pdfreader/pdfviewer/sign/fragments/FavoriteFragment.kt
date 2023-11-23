package com.pdfreader.pdfviewer.sign.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.example.pdfreader.modalClas.PdfList
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.pdfAdapterClass.ShowPdfAdapter
import com.pdfreader.pdfviewer.sign.viewModel.ItemViewModel
import kotlinx.android.synthetic.main.confirmation_dialog.btnCancel
import kotlinx.android.synthetic.main.confirmation_dialog.btnDelete
import kotlinx.android.synthetic.main.details_dialog.btnDetailOk
import kotlinx.android.synthetic.main.details_dialog.tvDate
import kotlinx.android.synthetic.main.details_dialog.tvFilePath
import kotlinx.android.synthetic.main.details_dialog.tvName
import kotlinx.android.synthetic.main.details_dialog.tvSize
import kotlinx.android.synthetic.main.dialog_menu.ivDelete
import kotlinx.android.synthetic.main.dialog_menu.ivDetails
import kotlinx.android.synthetic.main.dialog_menu.ivPdfFav
import kotlinx.android.synthetic.main.dialog_menu.ivPdfIcon
import kotlinx.android.synthetic.main.dialog_menu.ivRename
import kotlinx.android.synthetic.main.dialog_menu.ivShare
import kotlinx.android.synthetic.main.dialog_menu.llDelete
import kotlinx.android.synthetic.main.dialog_menu.llDetails
import kotlinx.android.synthetic.main.dialog_menu.llRename
import kotlinx.android.synthetic.main.dialog_menu.llShare
import kotlinx.android.synthetic.main.dialog_menu.tvPdfDate
import kotlinx.android.synthetic.main.dialog_menu.tvPdfName
import kotlinx.android.synthetic.main.dialog_menu.tvPdfSize
import kotlinx.android.synthetic.main.fragment_favorite.llEmptyFav
import kotlinx.android.synthetic.main.fragment_favorite.rvFavorite
import kotlinx.android.synthetic.main.rename_dialog.btnRenameCancel
import kotlinx.android.synthetic.main.rename_dialog.btnSave
import kotlinx.android.synthetic.main.rename_dialog.etName
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("SimpleDateFormat")
class FavoriteFragment : Fragment() {

    private lateinit var showPdfAdapter: ShowPdfAdapter
    private var list: ArrayList<PdfList>? = null
    private lateinit var viewModel: ItemViewModel
    private var favoriteList: ArrayList<PdfList>? = null
    private var mLastClickTime: Long = 0
    private var formatter: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
    private var dateFormatter: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy")
    private val onItemClickListerner: ShowPdfAdapter.OnItemClickListener =
        object : ShowPdfAdapter.OnItemClickListener {
            override fun onFavClick(view: View?, obj: ArrayList<PdfList>, pos: Int) {
                if (view != null) {
                    if (!obj[pos].isFav) {
                        obj[pos].isFav = true
                        viewModel.checkFavPos("${"true"} + ${obj[pos].pdfName.toString()}")
                        showPdfAdapter.addPreferenceList(true, obj[pos].absPath.toString())
                    } else {
                        obj[pos].isFav = false
                        viewModel.checkFavPos("${"false"} + ${obj[pos].pdfName.toString()}")
                        showPdfAdapter.addPreferenceList(false, obj[pos].absPath.toString())
                        obj.removeAt(pos)
                        showPdfAdapter.notifyItemRemoved(pos)
                        setVisibilityOfEmptyView()
                    }
                }
            }

            override fun onMenuDialog(view: View?, obj: ArrayList<PdfList>, pos: Int) {
                openMenuDialog(obj, pos)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        run {
            viewModel = ViewModelProvider(requireActivity())[ItemViewModel::class.java]
        }
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        viewModel.selectedItemFavF.observe(requireActivity()) {
            Log.d(ContentValues.TAG, "TextttttFF:::: $it")
            if (!AppUtils.isEmptyString(it)) {
                searchListData(it.toString().trimStart())
            } else {
                setEmptyData()
            }
        }
    }

    private fun initView() {
        list?.clear()
        //list = arguments?.getSerializable("PdfList") as? ArrayList<PdfList>
        list = AppUtils.filePdflist
        favoriteList =
            list?.filter { pdfList: PdfList -> pdfList.isFav } as ArrayList<PdfList>?
        setAdapter()
    }

    private fun searchListData(text: String) {
        val filteredList: ArrayList<PdfList> = ArrayList()
        if (text.isEmpty()) {
            favoriteList?.let { showPdfAdapter.setNewData(it) }
        } else {
            for (item in favoriteList!!) {
                if (item.pdfName?.lowercase(Locale.ROOT)
                        ?.contains(text.lowercase(Locale.ROOT)) == true
                ) {
                    filteredList.add(item)
                } else {
                    filteredList.remove(item)
                }
            }

            if (filteredList.isNotEmpty()) {
                showPdfAdapter.setNewData(filteredList)
                llEmptyFav?.visibility = View.GONE
                //setEmptyData()
            } else {
                context?.let {
                    showPdfAdapter = ShowPdfAdapter(arrayListOf(), arrayListOf(), it)
                    rvFavorite?.adapter = showPdfAdapter
                    llEmptyFav?.visibility = View.VISIBLE
                }
            }
            showPdfAdapter.setOnItemClickListener(onItemClickListerner)
        }
    }

    private fun setEmptyData() {
        context?.let {
            showPdfAdapter = ShowPdfAdapter(arrayListOf(), favoriteList!!, it)
            rvFavorite?.adapter = showPdfAdapter
            llEmptyFav?.visibility = View.GONE
        }
        showPdfAdapter.setOnItemClickListener(onItemClickListerner)
    }

    private fun setAdapter() {
        if (favoriteList?.isNotEmpty() == true) {
            showPdfAdapter = ShowPdfAdapter(list!!, favoriteList!!, requireContext())
            rvFavorite.adapter = showPdfAdapter
            llEmptyFav.visibility = View.GONE
        } else {
            showPdfAdapter = ShowPdfAdapter(arrayListOf(), arrayListOf(), requireContext())
            rvFavorite.adapter = showPdfAdapter
            llEmptyFav.visibility = View.VISIBLE
        }
        showPdfAdapter.setOnItemClickListener(onItemClickListerner)
    }

    private fun setVisibilityOfEmptyView() {
        if (favoriteList?.isNotEmpty() == true) {
            llEmptyFav!!.visibility = View.GONE
        } else {
            llEmptyFav!!.visibility = View.VISIBLE
        }
    }

    private fun openMenuDialog(pdfList: ArrayList<PdfList>, position: Int) {
        val dialog = BottomSheetDialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_menu)
        dialog.tvPdfName.text = pdfList[position].pdfName
        dialog.tvPdfDate.text = pdfList[position].pdfDate?.let { formatter.format(it) }
        dialog.tvPdfSize.text = pdfList[position].pdfSize
        when (pdfList[position].pdfExt) {
            "pdf" -> {
                dialog.ivPdfIcon.setImageResource(R.drawable.ic_pdf)
                dialog.ivRename.setColorFilter(Color.parseColor("#E5252A"))
                dialog.ivShare.setColorFilter(Color.parseColor("#E5252A"))
                dialog.ivDelete.setColorFilter(Color.parseColor("#E5252A"))
                dialog.ivDetails.setColorFilter(Color.parseColor("#E5252A"))
            }

            "doc", "docx" -> {
                dialog.ivPdfIcon.setImageResource(R.drawable.ic_docs)
                dialog.ivRename.setColorFilter(Color.parseColor("#4C86F9"))
                dialog.ivShare.setColorFilter(Color.parseColor("#4C86F9"))
                dialog.ivDelete.setColorFilter(Color.parseColor("#4C86F9"))
                dialog.ivDetails.setColorFilter(Color.parseColor("#4C86F9"))
            }

            "xls", "xlsx" -> {
                dialog.ivPdfIcon.setImageResource(R.drawable.ic_xls)
                dialog.ivRename.setColorFilter(Color.parseColor("#49A84C"))
                dialog.ivShare.setColorFilter(Color.parseColor("#49A84C"))
                dialog.ivDelete.setColorFilter(Color.parseColor("#49A84C"))
                dialog.ivDetails.setColorFilter(Color.parseColor("#49A84C"))
            }

            "ppt", "pptx" -> {
                dialog.ivPdfIcon.setImageResource(R.drawable.ic_ppt)
                dialog.ivRename.setColorFilter(Color.parseColor("#F26925"))
                dialog.ivShare.setColorFilter(Color.parseColor("#F26925"))
                dialog.ivDelete.setColorFilter(Color.parseColor("#F26925"))
                dialog.ivDetails.setColorFilter(Color.parseColor("#F26925"))
            }

            "txt" -> {
                dialog.ivPdfIcon.setImageResource(R.drawable.ic_txt)
                dialog.ivRename.setColorFilter(Color.parseColor("#6F788E"))
                dialog.ivShare.setColorFilter(Color.parseColor("#6F788E"))
                dialog.ivDelete.setColorFilter(Color.parseColor("#6F788E"))
                dialog.ivDetails.setColorFilter(Color.parseColor("#6F788E"))
            }
        }

        if (pdfList[position].isFav) {
            dialog.ivPdfFav.setImageResource(R.drawable.ic_fill_star)
        } else {
            dialog.ivPdfFav.setImageResource(R.drawable.ic_unfill_star)
        }

        dialog.ivPdfFav.setOnClickListener {
            if (view != null) {
                if (!pdfList[position].isFav) {
                    pdfList[position].isFav = true
                    viewModel.checkFavPos("${"true"} + ${pdfList[position].pdfName.toString()}")
                    showPdfAdapter.addPreferenceList(true, pdfList[position].absPath.toString())
                } else {
                    pdfList[position].isFav = false
                    viewModel.checkFavPos("${"false"} + ${pdfList[position].pdfName.toString()}")
                    showPdfAdapter.addPreferenceList(false, pdfList[position].absPath.toString())
                    pdfList.removeAt(position)
                    showPdfAdapter.notifyItemRemoved(position)
                    if (pdfList.isEmpty()) {
                        setVisibilityOfEmptyView()
                    }
                }
            }
            dialog.dismiss()
        }

        dialog.llRename.setOnClickListener {
            openRenameDialog(pdfList[position], position)
            dialog.dismiss()
        }
        dialog.llShare.setOnClickListener {
            AppUtils.shareData(pdfList[position].absPath, requireActivity())
            dialog.dismiss()
        }
        dialog.llDelete.setOnClickListener {
            openConfirmationDialog(pdfList, position)
            dialog.dismiss()
        }
        dialog.llDetails.setOnClickListener {
            openDetailsDialog(pdfList[position])
            dialog.dismiss()
        }
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        dialog.show()
    }

    private fun openRenameDialog(renameList: PdfList, position: Int) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.rename_dialog)
        dialog.etName.text = renameList.pdfName?.substringBeforeLast(".")?.toEditable()
        dialog.btnRenameCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.btnSave.setOnClickListener {
            if (!AppUtils.isEmptyString(dialog.etName.text.toString())) {
                val path: String? = File(renameList.absPath.toString()).parentFile?.absolutePath
                val oldName = File(renameList.absPath.toString().trimStart())
                val newName = File(
                    path.plus("/").plus(
                        dialog.etName.text.toString().trimStart().plus(".").plus(renameList.pdfExt)
                    )
                )
                oldName.renameTo(newName)
                /*favoriteList!![position].pdfName = dialog.etName.text.toString()
                favoriteList!![position].absPath = newName.toString()
                showPdfAdapter.notifyDataSetChanged()
                //showPdfAdapter.notifyItemChanged(position)
                dialog.dismiss()*/

                favoriteList!![position].pdfName =
                    dialog.etName.text.toString().trimStart().plus(".").plus(renameList.pdfExt)
                favoriteList!![position].absPath = newName.toString()
                val intent = Intent("RENAME")
                intent.putExtra("changeRenameName", favoriteList!![position].pdfName)
                intent.putExtra("changeRenamePath", favoriteList!![position].absPath)
                intent.putExtra("changeRenamePos", position)
                showPdfAdapter.notifyItemChanged(position)
                dialog.dismiss()
                EventBus.getDefault().post(intent)
            } else {
                Toast.makeText(context, getString(R.string.enter_file_name), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        dialog.show()
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun openConfirmationDialog(dataList: ArrayList<PdfList>, position: Int) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.confirmation_dialog)
        dialog.btnDelete.setOnClickListener {
            val file = File(dataList[position].absPath.toString())
            file.delete()
            //dataList.removeAt(position)
            //showPdfAdapter.deletedData(dataList[position].pdfName, position)
            //showPdfAdapter.notifyItemRemoved(position)
            val intent = Intent("DELETE")
            intent.putExtra("path", dataList[position].absPath)
            favoriteList!!.removeAt(position)
            showPdfAdapter.notifyItemRemoved(position)
            dialog.dismiss()
            setVisibilityOfEmptyView()
            EventBus.getDefault().post(intent)
        }
        dialog.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        dialog.show()
    }

    private fun openDetailsDialog(pdfList: PdfList) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.details_dialog)
        dialog.tvName.text = pdfList.pdfName
        dialog.tvFilePath.text = pdfList.absPath
        dialog.tvDate.text = pdfList.pdfDate?.let { dateFormatter.format(it) }
        dialog.tvSize.text = pdfList.pdfSize
        dialog.btnDetailOk.setOnClickListener {
            dialog.dismiss()
        }
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        dialog.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onItemCountEvent(event: Intent) {
        if (event.action.equals("FAVORITE")) {
            val checkFav = event.getBooleanExtra("checkFav", false)
            val checkPos = event.getIntExtra("checkPos", 0)
            val checkFavPath = event.getStringExtra("checkFavPath")
            val checkFavName = event.getStringExtra("checkFavName")
            if (!checkFav) {
                favoriteList!![checkPos].isFav = false
                //viewModel.checkFavPos("${"false"} + ${checkFavName.toString()}")
                //showPdfAdapter.addPreferenceList(false, checkFavPath.toString())
            } else {
                favoriteList!![checkPos].isFav = true
                //viewModel.checkFavPos("${"true"} + ${checkFavName.toString()}")
                //showPdfAdapter.addPreferenceList(true, checkFavPath.toString())
                favoriteList!!.removeAt(checkPos)
                showPdfAdapter.notifyItemRemoved(checkPos)
                if (favoriteList!!.isEmpty()) {
                    setVisibilityOfEmptyView()
                }
            }
        }
        if (event.action.equals("DELETE_DOC")) {
            val path = event.getStringExtra("path_doc")
            //showPdfAdapter.deletedData(path, position)
            val pos = AppUtils.deleteListData(path, favoriteList!!)
            showPdfAdapter.notifyItemRemoved(pos)
            setVisibilityOfEmptyView()
        }
        if (event.action.equals("RENAME")) {
            val changeName = event.getStringExtra("changeRenameName")
            val changePath = event.getStringExtra("changeRenamePath")
            val changePos = event.getIntExtra("changeRenamePos", 0)
            val path: String? =
                File(favoriteList!![changePos].absPath.toString()).parentFile?.absolutePath
            val renamePath: String = File(path.plus("/").plus(changeName)).toString()
            showPdfAdapter.updateRename(renamePath, changeName.toString(), favoriteList!!)
        }
    }
}