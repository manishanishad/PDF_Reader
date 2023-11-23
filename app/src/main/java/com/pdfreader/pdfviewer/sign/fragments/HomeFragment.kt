package com.pdfreader.pdfviewer.sign.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.example.pdfreader.modalClas.PdfList
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.pdfAdapterClass.ShowPdfAdapter
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
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
import kotlinx.android.synthetic.main.dialog_sort.btnDone
import kotlinx.android.synthetic.main.dialog_sort.btnSortCancel
import kotlinx.android.synthetic.main.dialog_sort.radioGroup1
import kotlinx.android.synthetic.main.dialog_sort.radioGroup2
import kotlinx.android.synthetic.main.dialog_sort.rbAscending
import kotlinx.android.synthetic.main.dialog_sort.rbDate
import kotlinx.android.synthetic.main.dialog_sort.rbDescending
import kotlinx.android.synthetic.main.dialog_sort.rbSize
import kotlinx.android.synthetic.main.dialog_sort.rbTitle
import kotlinx.android.synthetic.main.dialog_sort.rbType
import kotlinx.android.synthetic.main.fragment_home.ivSort
import kotlinx.android.synthetic.main.fragment_home.tvDocument
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
class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var showPdfAdapter: ShowPdfAdapter
    private var fileList = ArrayList<PdfList>()
    private var mainList: ArrayList<PdfList>? = null
    private var filterList: ArrayList<PdfList>? = null
    private lateinit var viewModel: ItemViewModel
    private var type: String? = null
    private var linearEmptyView: LinearLayout? = null
    private var llTopView: LinearLayout? = null
    private var rvHome: RecyclerView? = null
    private var permissionTime: Long = 0
    private var tvGivePermission: TextView? = null
    private var sortType1: String? = null
    private var sortType2: String? = null
    private var mLastClickTime: Long = 0
    private var formatter: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
    private var dateFormatter: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy")
    private val onItemClickListerner: ShowPdfAdapter.OnItemClickListener =
        object : ShowPdfAdapter.OnItemClickListener {
            override fun onFavClick(view: View?, obj: ArrayList<PdfList>, pos: Int) {
                if (view != null) {
                    if (!obj[pos].isFav) {
                        obj[pos].isFav = true
                        showPdfAdapter.addPreferenceList(true, obj[pos].absPath.toString())
                        //view.ivPdfFav.setImageResource(R.drawable.ic_fill_star)
                    } else {
                        obj[pos].isFav = false
                        showPdfAdapter.addPreferenceList(false, obj[pos].absPath.toString())
                        //view.ivPdfFav.setImageResource(R.drawable.ic_unfill_star)
                    }
                    showPdfAdapter.notifyItemChanged(pos)
                }
            }

            override fun onMenuDialog(view: View?, obj: ArrayList<PdfList>, pos: Int) {
                openMenuDialog(obj, pos)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().run {
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        clickListener()
        isPermissionGiven()
        viewModel.selectedItemHome.observe(requireActivity()) {
            Log.d(TAG, "TextttttHH:::: $it")
            if (!AppUtils.isEmptyString(it)) {
                llTopView!!.visibility = View.GONE
                searchListData(it.toString().trimStart())
            } else {
                llTopView!!.visibility = View.VISIBLE
                setEmptyData()
            }
        }
        viewModel.selectedItemFav.observe(requireActivity()) {
            if (!AppUtils.isEmptyString(it)) {
                val mainString = it.split("+")
                val isFav = mainString[0]
                val getName = mainString[1]
                if (type.equals("ALL")) {
                    checkFavData(isFav, getName)
                }
            }
        }
    }

    private fun checkFavData(status: String, getName: String) {
        var pos = 0
        for (item in mainList!!) {
            if (item.pdfName == getName.trim()) {
                pos = mainList!!.indexOf(item)
            }
        }

        mainList!![pos].isFav = status.trim() == "true"

        setAdapter()
    }

    private fun initView() {
        /*val dir = File(Environment.getExternalStorageDirectory().absolutePath)
        fileList = AppUtils.getFile(dir)
        pdfList = AppUtils.filePdflist*/
        linearEmptyView = view?.findViewById(R.id.llEmptyView)
        rvHome = view?.findViewById(R.id.rvHome)
        llTopView = view?.findViewById(R.id.llTopView)
        tvGivePermission = view?.findViewById(R.id.tvGivePermission)

        val bundle =
            arguments //  mainList = bundle?.getSerializable("PdfList") as? ArrayList<PdfList>
        //mainList = bundle?.getSerializable("PdfList") as? ArrayList<PdfList>
        mainList = AppUtils.filePdflist
        fileList = mainList!!
        type = bundle?.getString("TYPE")
        filterTabData()
        setAdapter()

        if (showPdfAdapter.itemCount == 0) {
            ivSort.visibility = View.INVISIBLE
        } else {
            ivSort.visibility = View.VISIBLE
        }
        tvDocument.text =
            showPdfAdapter.itemCount.toString().plus(" ").plus(getString(R.string.documents))

        sortType1 =
            context?.let {
                PreferencesManager.getString(it, PreferencesManager.PREF_SORT)
                    .ifEmpty { getString(R.string.title) }
            }
        sortType2 =
            context?.let {
                PreferencesManager.getString(it, PreferencesManager.PREF_SORT_A_D)
                    .ifEmpty { getString(R.string.ascending) }
            }

        sortingData()
    }

    private fun filterTabData() {
        if ((mainList?.size ?: 0) > 0) {
            mainList = if (type == "PDF") {
                mainList?.filter { pdfList: PdfList -> pdfList.pdfExt == "pdf" } as ArrayList<PdfList>?
            } else {
                mainList
            }

            mainList = if (type == "WORD") {
                mainList?.filter { pdfList: PdfList -> pdfList.pdfExt == "doc" || pdfList.pdfExt == "docx" } as ArrayList<PdfList>?
            } else {
                mainList
            }

            mainList = if (type == "EXCEL") {
                mainList?.filter { pdfList: PdfList -> pdfList.pdfExt == "xlsx" || pdfList.pdfExt == "xls" } as ArrayList<PdfList>?
            } else {
                mainList
            }

            mainList = if (type == "PPT") {
                mainList?.filter { pdfList: PdfList -> pdfList.pdfExt == "ppt" || pdfList.pdfExt == "pptx" } as ArrayList<PdfList>?
            } else {
                mainList
            }

            mainList = if (type == "TXT") {
                mainList?.filter { pdfList: PdfList -> pdfList.pdfExt == "txt" } as ArrayList<PdfList>?
            } else {
                mainList
            }
        }
    }

    private fun sortingData() {
        Log.d(TAG, "Sorttttt")
        when (sortType1) {
            getString(R.string.title) -> {
                if (sortType2 == getString(R.string.ascending)) {
                    mainList?.sortWith { s1, s2 ->
                        s1?.pdfName.toString().lowercase(Locale.ROOT).trim().compareTo(
                            s2?.pdfName.toString().lowercase(Locale.ROOT).trim()
                        )
                    }

                } else {
                    mainList?.sortWith { s1, s2 ->
                        s2.pdfName.toString().lowercase(Locale.ROOT).trim().compareTo(
                            s1.pdfName.toString().lowercase(Locale.ROOT).trim()
                        )
                    }
                }
                showPdfAdapter.notifyDataSetChanged()
            }

            getString(R.string.type) -> {
                if (sortType2 == getString(R.string.ascending)) {
                    mainList?.sortWith { s1, s2 ->
                        s1?.pdfExt.toString().lowercase(Locale.ROOT).trim().compareTo(
                            s2?.pdfExt.toString().lowercase(Locale.ROOT).trim()
                        )
                    }
                } else {
                    mainList?.sortWith { s1, s2 ->
                        s2.pdfExt.toString().lowercase(Locale.ROOT).trim().compareTo(
                            s1.pdfExt.toString().lowercase(Locale.ROOT).trim()
                        )
                    }
                }
                showPdfAdapter.notifyDataSetChanged()
            }

            getString(R.string.date) -> {
                if (sortType2 == getString(R.string.ascending)) {
                    mainList?.sortWith { s1, s2 ->
                        //s1?.pdfDate.toString().compareTo(s2?.pdfDate.toString())
                        val time2 = s2?.pdfDate?.time
                        val time1 = s1.pdfDate?.time
                        time2?.let { it1 -> time1?.compareTo(it1) } ?: 0
                    }
                } else {
                    mainList!!.sortByDescending { it.pdfDate }
                }
                showPdfAdapter.notifyDataSetChanged()
            }

            getString(R.string.size) -> {
                if (sortType2 == getString(R.string.ascending)) {
                    mainList?.sortWith { s1, s2 ->
                        s2.pdfRealSize?.let { it1 -> s1.pdfRealSize?.compareTo(it1) } ?: 0
                    }
                } else {
                    mainList?.sortWith { s1, s2 ->
                        s1.pdfRealSize?.let { it1 -> s2.pdfRealSize?.compareTo(it1) } ?: 0
                    }
                }
                showPdfAdapter.notifyDataSetChanged()
            }
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
                    //viewModel.checkFavPos("${"true"} + ${pdfList[position].pdfName.toString()}")
                    showPdfAdapter.addPreferenceList(true, pdfList[position].absPath.toString())
                    dialog.ivPdfFav.setImageResource(R.drawable.ic_fill_star)
                } else {
                    pdfList[position].isFav = false
                    //viewModel.checkFavPos("${"false"} + ${pdfList[position].pdfName.toString()}")
                    showPdfAdapter.addPreferenceList(false, pdfList[position].absPath.toString())
                    dialog.ivPdfFav.setImageResource(R.drawable.ic_unfill_star)
                }
                showPdfAdapter.notifyItemChanged(position)
            }
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
                /*val oldName = File(renameList.absPath.toString().trimStart())
                val newName = File(path.plus("/").plus(dialog.etName.text.toString().trimStart()))*/
                val oldName = File(renameList.absPath.toString().trimStart())
                val newName = File(
                    path.plus("/").plus(
                        dialog.etName.text.toString().trimStart().plus(".").plus(renameList.pdfExt)
                    )
                )
                oldName.renameTo(newName)
                renameList.pdfName =
                    dialog.etName.text.toString().trimStart().plus(".").plus(renameList.pdfExt)
                renameList.absPath = newName.toString()
                showPdfAdapter.notifyDataSetChanged()
                sortingData()
                //showPdfAdapter.notifyItemChanged(position)
                dialog.dismiss()
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
            /*//dataList.removeAt(position)
            showPdfAdapter.deletedData(dataList[position].pdfName, position)
            tvDocument.text =
                showPdfAdapter.itemCount.toString().plus(" ").plus(getString(R.string.documents))
            setVisibilityOfEmptyView()
            //showPdfAdapter.notifyItemRemoved(position)
            dialog.dismiss()*/

            /*if (type.equals("PDF")) {
                deletedData(fileList, position, mainList!!)
                dialog.dismiss()
            } else*/
            if (type.equals("WORD")) {
                deletedData(fileList, position, mainList!!)
                dialog.dismiss()
            } else if (type.equals("EXCEL")) {
                deletedData(fileList, position, mainList!!)
                dialog.dismiss()
            } else if (type.equals("PPT")) {
                deletedData(fileList, position, mainList!!)
                dialog.dismiss()
            } else if (type.equals("TXT")) {
                deletedData(fileList, position, mainList!!)
                dialog.dismiss()
            } else {
                Log.d(TAG, "Visibleeeeee:::: ${llTopView!!.visibility == View.VISIBLE}")
                if (llTopView!!.visibility == View.VISIBLE) {
                    val intent = Intent("DELETE")
                    intent.putExtra("path", dataList[position].absPath)
                    //list!!.removeAt(position)
                    //showPdfAdapter.notifyItemRemoved(position)
                    setVisibilityOfEmptyView()
                    dialog.dismiss()
                    EventBus.getDefault().post(intent)
                } else {
                    val intent = Intent("DELETE")
                    intent.putExtra("path", dataList[position].absPath)
                    dataList.removeAt(position)
                    showPdfAdapter.notifyItemRemoved(position)
                    setVisibilityOfEmptyView()
                    dialog.dismiss()
                    EventBus.getDefault().post(intent)
                }
            }
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

    private fun deletedData(
        dataList: ArrayList<PdfList>,
        position: Int,
        deleteList: ArrayList<PdfList>
    ) {
        val path = deleteList[position].absPath
        deleteList.removeAt(position)
        showPdfAdapter.notifyItemRemoved(position)
        if (deleteList.size == 0) {
            ivSort.visibility = View.INVISIBLE
        } else {
            ivSort.visibility = View.VISIBLE
        }
        tvDocument.text =
            deleteList.size.toString().plus(" ").plus(getString(R.string.documents))
        setVisibilityOfEmptyView()
        AppUtils.deleteListData(path, dataList)
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

    private fun searchListData(text: String) {
        val filteredList: ArrayList<PdfList> = ArrayList()
        if (text.isEmpty()) {
            mainList?.let { showPdfAdapter.setNewData(it) }
        } else {
            for (item in mainList!!) {
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
                linearEmptyView!!.visibility = View.GONE
                //setEmptyData()
            } else {
                context?.let {
                    showPdfAdapter = ShowPdfAdapter(arrayListOf(), arrayListOf(), it)
                    rvHome!!.adapter = showPdfAdapter
                    linearEmptyView!!.visibility = View.VISIBLE
                }
            }
            showPdfAdapter.setOnItemClickListener(onItemClickListerner)
        }
    }

    private fun clickListener() {
        ivSort.setOnClickListener(this)
        tvGivePermission!!.setOnClickListener(this)
    }

    private fun setEmptyData() {
        context?.let {
            showPdfAdapter = ShowPdfAdapter(arrayListOf(), mainList!!, it)
            rvHome!!.adapter = showPdfAdapter
            linearEmptyView!!.visibility = View.GONE
        }
        showPdfAdapter.setOnItemClickListener(onItemClickListerner)
    }

    private fun isPermissionGiven() {
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                tvGivePermission?.visibility = View.GONE
            } else {
                if ((ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED)
                ) {
                    tvGivePermission?.visibility = View.VISIBLE
                } else {
                    tvGivePermission?.visibility = View.GONE
                }
            }
        }
    }

    private fun checkPermission() {
        context?.let {
            if (AppUtils.isPermissionGranted(requireActivity())) {
                tvGivePermission?.visibility = View.GONE
                //Toast.makeText(this, "Granted", Toast.LENGTH_LONG).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", it.packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, 2453)
                }
            }
        }
    }

    private fun setAdapter() {
        context?.let {
            if (mainList?.isNotEmpty() == true) {
                showPdfAdapter = ShowPdfAdapter(arrayListOf(), mainList!!, it)
                rvHome!!.adapter = showPdfAdapter
                linearEmptyView!!.visibility = View.GONE
            } else {
                showPdfAdapter = ShowPdfAdapter(arrayListOf(), arrayListOf(), it)
                rvHome!!.adapter = showPdfAdapter
                linearEmptyView!!.visibility = View.VISIBLE
            }
        }
        showPdfAdapter.setOnItemClickListener(onItemClickListerner)
    }

    private fun setVisibilityOfEmptyView() {
        if (mainList?.isNotEmpty() == true) {
            linearEmptyView!!.visibility = View.GONE
        } else {
            linearEmptyView!!.visibility = View.VISIBLE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onItemCountEvent(event: Intent) {
        Log.d(TAG, "Eventttt:::: ${event.action}")
        if (event.action.equals("DELETE")) {
            val path = event.getStringExtra("path")
            //showPdfAdapter.deletedData(path, position)
            Log.i(TAG, "DeleteeeSize::::  ${mainList!!.size}")
            Log.i(TAG, "DeleteeePath::::  $path")
            val pos = AppUtils.deleteListData(path, mainList!!)
            Log.i(TAG, "DeleteeePos::::  $pos")
            showPdfAdapter.notifyItemRemoved(pos)
            if (mainList?.size == 0) {
                ivSort.visibility = View.INVISIBLE
            } else {
                ivSort.visibility = View.VISIBLE
            }
            tvDocument.text =
                mainList?.size.toString().plus(" ").plus(getString(R.string.documents))
            setVisibilityOfEmptyView()
        }
        if (event.action.equals("DELETE_DOC")) {
            val path = event.getStringExtra("path_doc")
            //showPdfAdapter.deletedData(path, position)
            val pos = AppUtils.deleteListData(path, mainList!!)
            showPdfAdapter.notifyItemRemoved(pos)
            if (mainList?.size == 0) {
                ivSort.visibility = View.INVISIBLE
            } else {
                ivSort.visibility = View.VISIBLE
            }
            tvDocument.text =
                mainList?.size.toString().plus(" ").plus(getString(R.string.documents))
            setVisibilityOfEmptyView()
        }
        if (event.action.equals("RENAME")) {
            if (type == "ALL") {
                val changeName = event.getStringExtra("changeRenameName")
                val changePath = event.getStringExtra("changeRenamePath")
                val changePos = event.getIntExtra("changeRenamePos", 0)
                val pos = AppUtils.renameListData(changePath, mainList!!)
                val path: String? =
                    File(mainList!![pos].absPath.toString()).parentFile?.absolutePath
                Log.d(TAG, "ChangeeeRename--->>> $changeName")
                Log.d(TAG, "ChangeeeRRPath--->>> $changePath")
                Log.d(TAG, "ChangeeeRRPos--->>> $changePos")
                Log.d(TAG, "ChangeeePosss--->>> $pos")
                Log.d(TAG, "ChangeeePathhh--->>> $path")
                mainList!![pos].pdfName = changeName
                mainList!![pos].absPath = File(path.plus("/").plus(changeName)).toString()
                showPdfAdapter.notifyItemChanged(pos)
                sortingData()
            }
        }
        if (event.action.equals("FAVORITE")) {
            var checkFav = event.getBooleanExtra("checkFav", false)
            val checkPos = event.getIntExtra("checkPos", 0)
            val checkFavPath = event.getStringExtra("checkFavPath")
            val checkFavName = event.getStringExtra("checkFavName")
            checkFav = checkFav != true

            if (type.equals("PDF")) {
                checkFavStatus(checkFav, checkPos, checkFavName, checkFavPath, fileList)
            } else if (type.equals("WORD")) {
                checkFavStatus(checkFav, checkPos, checkFavName, checkFavPath, fileList)
            } else if (type.equals("EXCEL")) {
                checkFavStatus(checkFav, checkPos, checkFavName, checkFavPath, fileList)
            } else if (type.equals("PPT")) {
                checkFavStatus(checkFav, checkPos, checkFavName, checkFavPath, fileList)
            } else if (type.equals("TXT")) {
                checkFavStatus(checkFav, checkPos, checkFavName, checkFavPath, fileList)
            } else {
                mainList!![checkPos].isFav = checkFav
                showPdfAdapter.addPreferenceList(checkFav, checkFavPath.toString())
                /*if (!checkFav) {
                    mainList!![checkPos].isFav = true
                    viewModel.checkFavPos("${"true"} + ${checkFavName.toString()}")
                    showPdfAdapter.addPreferenceList(true, checkFavPath.toString())
                } else {
                    mainList!![checkPos].isFav = false
                    viewModel.checkFavPos("${"false"} + ${checkFavName.toString()}")
                    showPdfAdapter.addPreferenceList(false, checkFavPath.toString())
                }*/
            }
        }
        if (event.action != null && event.action.equals("PERMISSION_RESULT")) {
            isPermissionGiven()
        }
        if (event.action.equals("SORTED_TYPE")) {
            sortingData()
            tvDocument.text =
                mainList?.size.toString().plus(" ").plus(getString(R.string.documents))
        }
    }

    private fun checkFavStatus(
        fav: Boolean,
        pos: Int,
        name: String?,
        path: String?,
        favList: ArrayList<PdfList>
    ) {
        if (!fav) {
            favList[pos].isFav = true
            viewModel.checkFavPos("${"true"} + ${name.toString()}")
            showPdfAdapter.addPreferenceList(true, path.toString())
        } else {
            favList[pos].isFav = false
            viewModel.checkFavPos("${"false"} + ${name.toString()}")
            showPdfAdapter.addPreferenceList(false, path.toString())
        }
    }

    /*override fun onResume() {
        super.onResume()
        mainList?.let { showPdfAdapter.setNewData(it) }
        if (mainList?.size == 0) {
            ivSort.visibility = View.INVISIBLE
        } else {
            ivSort.visibility = View.VISIBLE
        }
        tvDocument.text = mainList?.size.toString().plus(" ").plus(getString(R.string.documents))
        setVisibilityOfEmptyView()
    }*/

    @SuppressLint("ResourceType")
    private fun openSortDialog() {
        val dialog = Dialog(requireContext())
        var rb1: RadioButton? = null
        var rb2: RadioButton? = null

        dialog.run {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_sort)

            val getValue1 =
                PreferencesManager.getString(context, PreferencesManager.PREF_SORT)
                    .ifEmpty { getString(R.string.title) }
            val getValue2 =
                PreferencesManager.getString(context, PreferencesManager.PREF_SORT_A_D)
                    .ifEmpty { getString(R.string.ascending) }

            when (getValue1) {
                getString(R.string.title) -> rbTitle.isChecked = true
                getString(R.string.type) -> rbType.isChecked = true
                getString(R.string.date) -> rbDate.isChecked = true
                getString(R.string.size) -> rbSize.isChecked = true
                else -> rbTitle.isChecked = true
            }

            when (getValue2) {
                getString(R.string.ascending) -> rbAscending.isChecked = true
                getString(R.string.descending) -> rbDescending.isChecked = true
                else -> rbAscending.isChecked = true
            }

            radioGroup1.setOnCheckedChangeListener { _, checkedId ->
                rb1 = findViewById(checkedId)
                PreferencesManager.setString(
                    context, PreferencesManager.PREF_SORT,
                    rb1!!.text as String
                )
            }

            radioGroup2.setOnCheckedChangeListener { _, checkedId ->
                rb2 = findViewById(checkedId)
                PreferencesManager.setString(
                    context, PreferencesManager.PREF_SORT_A_D,
                    rb2!!.text as String
                )
            }

            btnSortCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnDone.setOnClickListener {
                val selectValue1 =
                    PreferencesManager.getString(context, PreferencesManager.PREF_SORT)
                        .ifEmpty { getString(R.string.title) }
                val selectValue2 =
                    PreferencesManager.getString(context, PreferencesManager.PREF_SORT_A_D)
                        .ifEmpty { getString(R.string.ascending) }

                when (selectValue1) {
                    getString(R.string.title) -> {
                        if (selectValue2 == getString(R.string.ascending)) {
                            mainList?.sortWith { s1, s2 ->
                                s1?.pdfName.toString().lowercase(Locale.ROOT).trim().compareTo(
                                    s2?.pdfName.toString().lowercase(Locale.ROOT).trim()
                                )
                            }

                        } else {
                            mainList?.sortWith { s1, s2 ->
                                s2.pdfName.toString().lowercase(Locale.ROOT).trim().compareTo(
                                    s1.pdfName.toString().lowercase(Locale.ROOT).trim()
                                )
                            }
                        }
                        showPdfAdapter.notifyDataSetChanged()
                    }

                    getString(R.string.type) -> {
                        if (selectValue2 == getString(R.string.ascending)) {
                            mainList?.sortWith { s1, s2 ->
                                s1?.pdfExt.toString().lowercase(Locale.ROOT).trim().compareTo(
                                    s2?.pdfExt.toString().lowercase(Locale.ROOT).trim()
                                )
                            }
                        } else {
                            mainList?.sortWith { s1, s2 ->
                                s2.pdfExt.toString().lowercase(Locale.ROOT).trim().compareTo(
                                    s1.pdfExt.toString().lowercase(Locale.ROOT).trim()
                                )
                            }
                        }
                        showPdfAdapter.notifyDataSetChanged()
                    }

                    getString(R.string.date) -> {
                        if (selectValue2 == getString(R.string.ascending)) {
                            mainList?.sortWith { s1, s2 ->
                                //s1?.pdfDate.toString().compareTo(s2?.pdfDate.toString())
                                val time2 = s2?.pdfDate?.time
                                val time1 = s1.pdfDate?.time
                                time2?.let { it1 -> time1?.compareTo(it1) } ?: 0
                            }
                        } else {
                            mainList!!.sortByDescending { it.pdfDate }
                        }
                        showPdfAdapter.notifyDataSetChanged()
                    }

                    getString(R.string.size) -> {
                        if (selectValue2 == getString(R.string.ascending)) {
                            mainList?.sortWith { s1, s2 ->
                                s2.pdfRealSize?.let { it1 -> s1.pdfRealSize?.compareTo(it1) } ?: 0
                            }
                        } else {
                            mainList?.sortWith { s1, s2 ->
                                s1.pdfRealSize?.let { it1 -> s2.pdfRealSize?.compareTo(it1) } ?: 0
                            }
                        }
                        showPdfAdapter.notifyDataSetChanged()
                    }
                }
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
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSort -> {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)
                openSortDialog()
            }

            R.id.tvGivePermission -> {
                if (activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            it,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    } == false) {
                    context?.let {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", it.packageName, null)
                        intent.data = uri
                        //startActivity(intent)
                        activity?.startActivityForResult(intent, 1212)
                        //requireActivity().finish()
                        return
                    }
                } else {
                    checkPermission()
                }
            }
        }
    }
}