package com.pdfreader.pdfviewer.sign.pdfViewer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.pdfreader.interfaces.OnSavePdfToImg
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pdfreader.pdfviewer.sign.BuildConfig
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.addText.TextEditorDialogFragment
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.pdfreader.pdfviewer.sign.common.AppUtils.Companion.hideKeyboard
import com.pdfreader.pdfviewer.sign.common.AppUtils.Companion.openKeyboard
import com.pdfreader.pdfviewer.sign.pdf.PDSElement
import com.pdfreader.pdfviewer.sign.pdf.PDSPDFDocument
import com.pdfreader.pdfviewer.sign.pdf.PDSPageAdapter
import com.pdfreader.pdfviewer.sign.pdf.PDSPageViewer
import com.pdfreader.pdfviewer.sign.pdf.PDSSaveAsPDFAsyncTask
import com.pdfreader.pdfviewer.sign.pdf.PDSViewPager
import com.pdfreader.pdfviewer.sign.pdf.SignatureUtils
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
import com.pdfreader.pdfviewer.sign.signActivity.SignListActivity
import com.reader.office.constant.MainConstant
import kotlinx.android.synthetic.main.activity_pdf_view.clPdfBottom
import kotlinx.android.synthetic.main.activity_pdf_view.clSignBottom
import kotlinx.android.synthetic.main.activity_pdf_view.editPdfToolbar
import kotlinx.android.synthetic.main.activity_pdf_view.ivEditPdf
import kotlinx.android.synthetic.main.activity_pdf_view.ivRedo
import kotlinx.android.synthetic.main.activity_pdf_view.ivReplace
import kotlinx.android.synthetic.main.activity_pdf_view.llGoToPage
import kotlinx.android.synthetic.main.activity_pdf_view.llMore
import kotlinx.android.synthetic.main.activity_pdf_view.llRedo
import kotlinx.android.synthetic.main.activity_pdf_view.llReplace
import kotlinx.android.synthetic.main.activity_pdf_view.llSearchText
import kotlinx.android.synthetic.main.activity_pdf_view.llUndo
import kotlinx.android.synthetic.main.activity_pdf_view.searchPdfToolbar
import kotlinx.android.synthetic.main.activity_pdf_view.toolBar
import kotlinx.android.synthetic.main.activity_pdf_view.tvPageNo
import kotlinx.android.synthetic.main.activity_pdf_view.tvRedo
import kotlinx.android.synthetic.main.activity_pdf_view.tvReplace
import kotlinx.android.synthetic.main.confirmation_dialog.btnCancel
import kotlinx.android.synthetic.main.confirmation_dialog.btnDelete
import kotlinx.android.synthetic.main.confirmation_dialog.tvConfirmText
import kotlinx.android.synthetic.main.details_dialog.btnDetailOk
import kotlinx.android.synthetic.main.details_dialog.tvDate
import kotlinx.android.synthetic.main.details_dialog.tvFilePath
import kotlinx.android.synthetic.main.details_dialog.tvName
import kotlinx.android.synthetic.main.details_dialog.tvSize
import kotlinx.android.synthetic.main.dialog_doc_menu.ivPdfFav
import kotlinx.android.synthetic.main.dialog_doc_menu.ivPdfIcon
import kotlinx.android.synthetic.main.dialog_doc_menu.llDelete
import kotlinx.android.synthetic.main.dialog_doc_menu.llDetails
import kotlinx.android.synthetic.main.dialog_doc_menu.llPrint
import kotlinx.android.synthetic.main.dialog_doc_menu.llRename
import kotlinx.android.synthetic.main.dialog_doc_menu.llSaveAs
import kotlinx.android.synthetic.main.dialog_doc_menu.llShare
import kotlinx.android.synthetic.main.dialog_doc_menu.tvPdfDate
import kotlinx.android.synthetic.main.dialog_doc_menu.tvPdfName
import kotlinx.android.synthetic.main.dialog_doc_menu.tvPdfSize
import kotlinx.android.synthetic.main.dialog_edit_doc.llAddSignature
import kotlinx.android.synthetic.main.dialog_edit_doc.llAddText
import kotlinx.android.synthetic.main.edit_pdf_toolbar.btnSignSave
import kotlinx.android.synthetic.main.edit_pdf_toolbar.ivSignCancel
import kotlinx.android.synthetic.main.edit_pdf_toolbar.tvSign
import kotlinx.android.synthetic.main.home_toolbar.ivDrawerMenu
import kotlinx.android.synthetic.main.home_toolbar.ivSearch
import kotlinx.android.synthetic.main.home_toolbar.tvPdfReader
import kotlinx.android.synthetic.main.rename_dialog.btnRenameCancel
import kotlinx.android.synthetic.main.rename_dialog.btnSave
import kotlinx.android.synthetic.main.rename_dialog.etName
import kotlinx.android.synthetic.main.rename_dialog.tvTitle
import kotlinx.android.synthetic.main.search_toolbar.etSearchText
import kotlinx.android.synthetic.main.search_toolbar.ivCancel
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.security.KeyStore
import java.util.Random


@SuppressLint("UseCompatLoadingForDrawables")
class PdfViewActivity : AppCompatActivity(), View.OnClickListener {

    var digitalIDPassword: String? = null
    var aliases: String? = null
    var keyStore: KeyStore? = null
    private var filePath: String? = null
    private var fileName: String? = null
    private var fileDate: String? = null
    private var fileExt: String? = null
    private var fileSize: String? = null
    private var itemPos: Int = 0
    private var fileType: Int = 0
    private var fileFav: Boolean? = null
    private var mViewPager: PDSViewPager? = null
    private var imageAdapter: PDSPageAdapter? = null
    private var mDocument: PDSPDFDocument? = null
    private var mFirstTap = true
    private var mVisibleWindowHt = 0
    private val mUIElemsHandler = UIElementsHandler(this)
    private var currentPage = 1
    private lateinit var pdsPageViewer: PDSPageViewer
    private lateinit var fASPageViewer: PDSPageViewer
    private lateinit var pdsElementUpdate: PDSElement
    private var fASElementType: PDSElement.PDSElementType =
        PDSElement.PDSElementType.PDSElementTypeSignature
    private val f: Float = 0.0f
    private val f2: Float = 0.0f
    private var signPath: String = ""
    private var signUriPath: File? = null
    private var typeDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        initView()
        clickListener()
    }

    @SuppressLint("CutPasteId")
    private fun initView() {
        AppUtils.changeStatusBarColor(this)
        window.statusBarColor = getColor(R.color.bgCommon)

        mViewPager = findViewById(R.id.viewpager)
        toolBar.visibility = View.VISIBLE
        clPdfBottom.visibility = View.VISIBLE
        ivEditPdf.visibility = View.VISIBLE
        searchPdfToolbar.visibility = View.GONE
        editPdfToolbar.visibility = View.GONE
        clSignBottom.visibility = View.GONE
        toolBar.setBackgroundColor(getColor(R.color.bgCommon))
        ivDrawerMenu.setImageDrawable(getDrawable(R.drawable.ic_back))
        ivSearch.visibility = View.GONE
        fileType = intent.getIntExtra("fileType", 0)
        fileName = intent.getStringExtra("fileName")
        fileDate = intent.getStringExtra("fileDate")
        fileExt = intent.getStringExtra("fileExt")
        fileSize = intent.getStringExtra("fileSize")
        fileFav = intent.getBooleanExtra("fileFav", false)
        filePath = intent.getStringExtra(MainConstant.INTENT_FILED_FILE_PATH)
        itemPos = intent.getIntExtra("itemPosition", 0)
        tvPdfReader.text = fileName
        //filePath?.let { showPDF(it) }
        openPDFViewer(filePath)

        etSearchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*try {
                    var parsedText = s
                    Log.d(TAG, "Texttttt:::: $parsedText")
                    val reader = PdfReader(filePath)
                    val n: Int = reader.numberOfPages
                    for (i in 0 until n) {
                        parsedText = """ $parsedText${
                            PdfTextExtractor.getTextFromPage(reader, i + 1).trim { it <= ' ' }
                        }""".trimIndent()
                    }
                    println(parsedText)
                    reader.close()
                } catch (e: Exception) {
                    println(e)
                }*/
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openPDFViewer(pdfData: String?) {
        val view: View? = this.currentFocus
        if (view != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        val pdfUri = Uri.fromFile(pdfData?.let { File(it) })
        try {
            val document = PDSPDFDocument(this, pdfUri)
            document.open()
            this.mDocument = document
            imageAdapter = PDSPageAdapter(supportFragmentManager, document)
            updatePageNumber(1)
            mViewPager?.adapter = imageAdapter

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@PdfViewActivity,
                getString(R.string.cannot_open_pdf),
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    fun getDocument(): PDSPDFDocument? {
        return mDocument
    }

    fun isFirstTap(): Boolean {
        return this.mFirstTap
    }

    fun setFirstTap(z: Boolean) {
        this.mFirstTap = z
    }

    fun getVisibleWindowHeight(): Int {
        if (this.mVisibleWindowHt == 0) {
            this.mVisibleWindowHt = computeVisibleWindowHtForNonFullScreenMode()
        }
        return this.mVisibleWindowHt
    }

    private fun computeVisibleWindowHtForNonFullScreenMode(): Int {
        return findViewById<View>(R.id.clLayout).height
    }

    fun invokeMenuButton(disableButtonFlag: Boolean) {
        if (disableButtonFlag) {
            typeDone = true
            searchPdfToolbar.visibility = View.GONE
            toolBar.visibility = View.GONE
            clPdfBottom.visibility = View.GONE
            ivEditPdf.visibility = View.GONE
            editPdfToolbar.visibility = View.VISIBLE
            clSignBottom.visibility = View.VISIBLE
        } else {
            typeDone = false
            toolBar.visibility = View.VISIBLE
            clPdfBottom.visibility = View.VISIBLE
            ivEditPdf.visibility = View.VISIBLE
            editPdfToolbar.visibility = View.GONE
            clSignBottom.visibility = View.GONE
            this.currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    fun updatePageNumber(i: Int) {
        val textView = findViewById<View>(R.id.tvPageNo) as TextView
        //findViewById<View>(R.id.pageNumberOverlay).visibility = View.VISIBLE
        val stringBuilder = StringBuilder()
        stringBuilder.append(i)
        stringBuilder.append("/")
        stringBuilder.append(mDocument!!.numPages)
        textView.text = stringBuilder.toString()
        resetTimerHandlerForPageNumber(1000)
    }

    private fun resetTimerHandlerForPageNumber(i: Int) {
        this.mUIElemsHandler.removeMessages(1)
        val message = Message()
        message.what = 1
        this.mUIElemsHandler.sendMessageDelayed(message, i.toLong())
    }

    private class UIElementsHandler(fASDocumentViewer: PdfViewActivity?) :
        Handler() {
        private val mActivity: WeakReference<PdfViewActivity?>

        init {
            mActivity = WeakReference<PdfViewActivity?>(fASDocumentViewer)
        }

        override fun handleMessage(message: Message) {
            val fASDocumentViewer = mActivity.get()
            if (fASDocumentViewer != null && message.what == 1) {
                //fASDocumentViewer.fadePageNumberOverlay()
            }
            super.handleMessage(message)
        }
    }

    private fun fadePageNumberOverlay() {
        val loadAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        //val findViewById = findViewById<View>(R.id.pageNumberOverlay)
        /*if (findViewById.visibility == View.VISIBLE) {
            findViewById.startAnimation(loadAnimation)
            findViewById.visibility = View.INVISIBLE
        }*/
    }

    fun runPostExecution() {
        //savingProgress.visibility = View.INVISIBLE
        val intent = Intent()
        intent.putExtra("TYPE", 3)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun clickListener() {
        ivDrawerMenu.setOnClickListener(this)
        ivEditPdf.setOnClickListener(this)
        llSearchText.setOnClickListener(this)
        ivCancel.setOnClickListener(this)
        llGoToPage.setOnClickListener(this)
        llMore.setOnClickListener(this)
        llReplace.setOnClickListener(this)
        llUndo.setOnClickListener(this)
        llRedo.setOnClickListener(this)
        ivSignCancel.setOnClickListener(this)
        btnSignSave.setOnClickListener(this)
    }

    private fun openMenuDialog(onSavePdfToImg: OnSavePdfToImg) {
        val dialog = BottomSheetDialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_doc_menu)
        dialog.tvPdfName.text = fileName
        dialog.tvPdfDate.text = fileDate
        dialog.tvPdfSize.text = fileSize
        when (fileExt) {
            "pdf" -> dialog.ivPdfIcon.setImageResource(R.drawable.ic_pdf)
            "docx", "doc" -> dialog.ivPdfIcon.setImageResource(R.drawable.ic_docs)
            "xlsx", "xls" -> dialog.ivPdfIcon.setImageResource(R.drawable.ic_xls)
            "ppt" -> dialog.ivPdfIcon.setImageResource(R.drawable.ic_ppt)
            "txt" -> dialog.ivPdfIcon.setImageResource(R.drawable.ic_txt)
        }

        if (fileType == 1) {
            dialog.llRename.visibility = View.GONE
            dialog.llDelete.visibility = View.GONE
        } else {
            dialog.llRename.visibility = View.VISIBLE
            dialog.llDelete.visibility = View.VISIBLE
        }

        if (fileFav == true) {
            dialog.ivPdfFav.setImageResource(R.drawable.ic_fill_star)
        } else {
            dialog.ivPdfFav.setImageResource(R.drawable.ic_unfill_star)
        }

        dialog.ivPdfFav.setOnClickListener {
            if (fileFav == false) {
                fileFav = true
                dialog.ivPdfFav.setImageResource(R.drawable.ic_fill_star)
            } else {
                fileFav = false
                dialog.ivPdfFav.setImageResource(R.drawable.ic_unfill_star)
            }

            val intent = Intent("FAVORITE")
            intent.putExtra("checkFav", fileFav)
            intent.putExtra("checkFavPath", filePath)
            intent.putExtra("checkFavName", fileName)
            intent.putExtra("checkPos", itemPos)
            EventBus.getDefault().post(intent)
        }

        dialog.llSaveAs.setOnClickListener {
            dialog.dismiss()
            onSavePdfToImg.onClickSave(filePath)
        }

        dialog.llPrint.setOnClickListener {
            printPdf()
            dialog.dismiss()
        }

        dialog.llRename.setOnClickListener {
            fileName?.let { it1 -> openRenameDialog(it1, itemPos) }
            dialog.dismiss()
        }
        dialog.llShare.setOnClickListener {
            shareData(filePath)
            dialog.dismiss()
        }
        dialog.llDelete.setOnClickListener {
            openConfirmationDialog(itemPos)
            dialog.dismiss()
        }
        dialog.llDetails.setOnClickListener {
            openDetailsDialog()
            dialog.dismiss()
        }
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun convertPdfToImage(pdfFile: File): ArrayList<Bitmap> {
        //pBar.visibility = View.VISIBLE
        val bitmaps: ArrayList<Bitmap> = ArrayList()
        try {
            val renderer =
                PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))

            var bitmap: Bitmap
            val pageCount = renderer.pageCount
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val width = resources.displayMetrics.densityDpi / 72 * page.width
                val height = resources.displayMetrics.densityDpi / 72 * page.height
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                val root: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                            .toString() + "/" + "PDF Reader"
                    )
                } else {
                    File(Environment.getExternalStorageDirectory().toString() + "/" + "PDF Reader")
                }

                root.mkdirs()
                val generator = Random()
                var n = 10000
                n = generator.nextInt(n)
                val name = "Image-$n.jpg"
                val file = File(root, name)
                if (file.exists()) file.delete()
                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    out.flush()
                    out.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                bitmaps.add(bitmap)
                page.close()
            }
            renderer.close()
            runOnUiThread {
                run {
                    Toast.makeText(
                        this@PdfViewActivity,
                        getString(R.string.save_pdf_images),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            //pBar.visibility = View.GONE
            //Toast.makeText(this, getString(R.string.save_pdf_images), Toast.LENGTH_SHORT).show()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }

        return bitmaps
    }

    private fun printPdf() {
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        try {
            val printDocumentAdapter: PrintDocumentAdapter = PdfDocumentAdapter(
                this, filePath!!
            )
            printManager.print(
                getString(R.string.document),
                printDocumentAdapter,
                PrintAttributes.Builder().build()
            )
        } catch (ex: java.lang.Exception) {
            Toast.makeText(this, getString(R.string.cant_read_pdf), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openRenameDialog(name: String, position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.rename_dialog)
        dialog.etName.text = name.substringBeforeLast(".").toEditable()
        dialog.btnRenameCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.btnSave.setOnClickListener {
            if (!AppUtils.isEmptyString(dialog.etName.text.toString())) {
                val path: String? = File(filePath.toString()).parentFile?.absolutePath
                val oldName = File(filePath.toString().trimStart())
                val newName = File(
                    path.plus("/")
                        .plus(dialog.etName.text.toString().trimStart().plus(".").plus(fileExt))
                )
                oldName.renameTo(newName)
                fileName = dialog.etName.text.toString().trimStart().plus(".").plus(fileExt)
                tvPdfReader.text = dialog.etName.text.toString().trimStart().plus(".").plus(fileExt)

                val intent = Intent("RENAME")
                intent.putExtra("changeRenameName", fileName)
                intent.putExtra("changeRenamePath", filePath)
                intent.putExtra("changeRenamePos", position)
                EventBus.getDefault().post(intent)
                /*val intent = Intent()
                intent.putExtra("TYPE", 2)
                intent.putExtra("renameDoc", fileName)
                intent.putExtra("renameDocPos", position)
                setResult(RESULT_OK, intent)*/
                dialog.dismiss()
            } else {
                Toast.makeText(this, getString(R.string.enter_file_name), Toast.LENGTH_SHORT)
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

    private fun shareData(path: String?) {
        try {
            val file = path?.let { File(it) }
            if (file != null) {
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.type = "*/*"
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openConfirmationDialog(position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.confirmation_dialog)
        dialog.btnDelete.setOnClickListener {
            //val dir = filesDir
            val file = filePath?.let { it1 -> File(it1) }
            file?.delete()

            val intent = Intent("DELETE_DOC")
            intent.putExtra("path_doc", filePath)
            EventBus.getDefault().post(intent)

            /*val intent = Intent()
            intent.putExtra("TYPE", 1)
            intent.putExtra("deletedItem", position)
            setResult(RESULT_OK, intent)*/

            dialog.dismiss()
            finish()
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

    private fun openDetailsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.details_dialog)
        dialog.tvName.text = fileName
        dialog.tvFilePath.text = filePath
        dialog.tvDate.text = fileDate
        dialog.tvSize.text = fileSize
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

    private fun openEditDialog(path: String) {
        val filePath = Uri.fromFile(File(path))
        val dialog = BottomSheetDialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_edit_doc)
        dialog.llAddText.setOnClickListener {
            val textEditorDialogFragment = TextEditorDialogFragment.show(this)
            textEditorDialogFragment.setOnTextEditorListener(object :
                TextEditorDialogFragment.TextEditorListener {
                override fun onDone(inputText: String, colorCode: Int) {
                    tvSign.text = getString(R.string.add_text)
                    searchPdfToolbar.visibility = View.GONE
                    toolBar.visibility = View.GONE
                    clPdfBottom.visibility = View.GONE
                    ivEditPdf.visibility = View.GONE
                    editPdfToolbar.visibility = View.VISIBLE
                    clSignBottom.visibility = View.GONE

                    PreferencesManager.setString(
                        this@PdfViewActivity,
                        PreferencesManager.SET_TEXT,
                        inputText
                    )
                    PreferencesManager.setString(
                        this@PdfViewActivity,
                        PreferencesManager.TEXT_COLOR,
                        colorCode.toString()
                    )
                    addTextElement(
                        PDSElement.PDSElementType.PDSElementTypeEditText,
                        inputText,
                        SignatureUtils.getSignatureWidth(
                            resources.getDimension(R.dimen.sign_field_default_height).toInt(),
                            File(inputText),
                            applicationContext
                        ).toFloat(),
                        resources.getDimension(R.dimen.sign_field_default_height),
                    )
                    invokeMenuButton(true)
                    clSignBottom.visibility = View.GONE
                }
            })
            dialog.dismiss()
        }
        dialog.llAddSignature.setOnClickListener {
            val intent = Intent(this, SignListActivity::class.java)
            intent.putExtra("SIGN_TYPE", true)
            startActivityForResult(intent, 144)
            dialog.dismiss()
        }
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun addTextElement(
        fASElementType: PDSElement.PDSElementType,
        text: String?,
        f: Float,
        f2: Float
    ) {
        val focusedChild: View? = this.mViewPager?.focusedChild
        if (focusedChild != null) {
            val fASPageViewer: PDSPageViewer =
                (focusedChild as ViewGroup).getChildAt(0) as PDSPageViewer
            if (fASPageViewer != null) {
                pdsPageViewer = fASPageViewer
                val visibleRect: RectF = fASPageViewer.visibleRect
                val width = visibleRect.left + visibleRect.width() / 2.0f - f / 2.0f
                val height = visibleRect.top + visibleRect.height() / 2.0f - f2 / 2.0f
                val fASElementType2: PDSElement.PDSElementType = fASElementType
                val element: PDSElement =
                    fASPageViewer.createTextElement(fASElementType2, text, width, height, f, f2)
                pdsElementUpdate = element
            }
        }
    }

    private fun goToPageDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.rename_dialog)
        dialog.tvTitle.text = getString(R.string.go_to_page)
        dialog.etName.hint = getString(R.string.enter_a_page_number)
        dialog.etName.inputType = InputType.TYPE_CLASS_NUMBER
        dialog.btnSave.text = getString(R.string.go)
        dialog.btnRenameCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.btnSave.setOnClickListener {
            if (dialog.etName.text.toString().isNotEmpty()) {
                //updatePageNumber(dialog.etName.text.toString().toInt())
                //openPDFViewer(filePath, dialog.etName.text.toString().toInt())
                val number = dialog.etName.text.toString().toIntOrNull() ?: 0
                Log.d(TAG, "Numberrrr::: $number")
                Log.d(TAG, "Numberrrr::: ${mDocument!!.numPages}")
                Log.d(TAG, "Numberrrr::: ${number <= mDocument!!.numPages && number != 0}")
                /*try {
                    if (number in 1..mDocument!!.numPages) {
                        mViewPager!!.setPageNumber(number - 1)
                        mViewPager!![number - 1]
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.invalid_page_number),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    //Toast.makeText(this, getString(R.string.invalid_page_number), Toast.LENGTH_SHORT).show()
                }*/

                if (number <= mDocument!!.numPages && number != 0) {
                    mViewPager?.setPageNumber(number - 1)
                    //mViewPager!![number - 1]
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.invalid_page_number),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            } else {
                dialog.etName.error = getString(R.string.enter_a_page_number)
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

    private fun openSaveAsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.rename_dialog)
        dialog.tvTitle.text = getString(R.string.save_pdf)
        dialog.etName.hint = getString(R.string.enter_file_name)
        dialog.btnRenameCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.btnSave.setOnClickListener {
            if (!AppUtils.isEmptyString(dialog.etName.text.toString())) {
                val task = PDSSaveAsPDFAsyncTask(this@PdfViewActivity, "${dialog.etName.text}.pdf")
                task.execute(*arrayOfNulls<Void>(0))
                dialog.etName.hideKeyboard()
                dialog.dismiss()
            } else {
                dialog.etName.error = getString(R.string.enter_file_name)
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

    private fun openChangesConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.confirmation_dialog)
        dialog.tvConfirmText.text = getString(R.string.want_to_save_your_changes)
        dialog.btnDelete.text = getString(R.string.save)
        dialog.btnDelete.setOnClickListener {
            openSaveAsDialog()
            dialog.dismiss()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 144) {
            if (resultCode == RESULT_OK) {
                toolBar.visibility = View.GONE
                clPdfBottom.visibility = View.GONE
                ivEditPdf.visibility = View.GONE
                editPdfToolbar.visibility = View.VISIBLE
                clSignBottom.visibility = View.VISIBLE

                signPath = data?.getStringExtra("FilePath").toString()
                signUriPath = File(signPath)

                addElement(
                    PDSElement.PDSElementType.PDSElementTypeSignature,
                    signUriPath,
                    SignatureUtils.getSignatureWidth(
                        resources.getDimension(R.dimen.sign_field_default_height).toInt(),
                        signUriPath,
                        applicationContext
                    )
                        .toFloat(),
                    resources.getDimension(R.dimen.sign_field_default_height),
                    data!!.getBooleanExtra("TYPE_SIGN", false)
                )
            }
        }
    }

    private fun addElement(
        fASElementType: PDSElement.PDSElementType,
        file: File?,
        f: Float,
        f2: Float,
        type: Boolean
    ) {
        if (type) {
            val focusedChild: View? = this.mViewPager?.focusedChild
            if (focusedChild != null) {
                val fASPageViewer: PDSPageViewer =
                    (focusedChild as ViewGroup).getChildAt(0) as PDSPageViewer
                if (fASPageViewer != null) {
                    pdsPageViewer = fASPageViewer
                    val visibleRect: RectF = fASPageViewer.visibleRect
                    val width = visibleRect.left + visibleRect.width() / 2.0f - f / 2.0f
                    val height = visibleRect.top + visibleRect.height() / 2.0f - f2 / 2.0f
                    //val lastFocusedElementViewer: PDSElementViewer = fASPageViewer.lastFocusedElementViewer
                    val fASElementType2: PDSElement.PDSElementType = fASElementType
                    val element: PDSElement =
                        fASPageViewer.createElement(fASElementType2, file, width, height, f, f2)
                    pdsElementUpdate = element
                    invokeMenuButton(true)
                }
            }
        } else {
            val visibleRect: RectF = pdsPageViewer.visibleRect
            val width = visibleRect.left + visibleRect.width() / 2.0f - f / 2.0f
            val height = visibleRect.top + visibleRect.height() / 2.0f - f2 / 2.0f
            val fASElementType2: PDSElement.PDSElementType = fASElementType

            pdsPageViewer.removePerEle(
                width, height, f, f2,
                pdsElementUpdate, pdsPageViewer
            )

            val element: PDSElement =
                pdsPageViewer.createElement(fASElementType2, file, width, height, f, f2)
            pdsElementUpdate = element

            invokeMenuButton(true)
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "typeeeeee--->>> $typeDone")
        if (typeDone) {
            clearElementView()
            invokeMenuButton(false)
        } else {
            super.onBackPressed()
            finish()
        }
    }

    private fun clearElementView() {
        val focusedChild: View? = mViewPager?.focusedChild
        if (focusedChild != null) {
            fASPageViewer = (focusedChild as ViewGroup).getChildAt(0) as PDSPageViewer
            if (fASPageViewer != null) {
                pdsPageViewer = fASPageViewer
                val visibleRect: RectF = fASPageViewer.visibleRect
                val width = visibleRect.left + visibleRect.width() / 2.0f - f / 2.0f
                val height = visibleRect.top + visibleRect.height() / 2.0f - f2 / 2.0f
                pdsPageViewer.removePerEle(
                    width, height, f, f2,
                    pdsElementUpdate, pdsPageViewer
                )
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivDrawerMenu -> finish()
            R.id.ivEditPdf -> filePath?.let { openEditDialog(it) }
            R.id.llSearchText -> {
                toolBar.visibility = View.GONE
                clPdfBottom.visibility = View.GONE
                ivEditPdf.visibility = View.GONE
                tvPageNo.visibility = View.GONE
                etSearchText.openKeyboard()
                etSearchText.requestFocus()
                searchPdfToolbar.visibility = View.VISIBLE
            }

            R.id.ivCancel -> {
                toolBar.visibility = View.VISIBLE
                clPdfBottom.visibility = View.VISIBLE
                ivEditPdf.visibility = View.VISIBLE
                tvPageNo.visibility = View.VISIBLE
                etSearchText.hideKeyboard()
                etSearchText.text.clear()
                searchPdfToolbar.visibility = View.GONE
            }

            R.id.llGoToPage -> goToPageDialog()
            R.id.llMore -> {
                openMenuDialog(object : OnSavePdfToImg {
                    override fun onClickSave(filePath: String?) {
                        val file = File(filePath!!)
                        DoAsync(this@PdfViewActivity) {
                            Log.i("ASYNC", "START")
                            convertPdfToImage(file)
                        }.execute()
                    }
                })
            }

            R.id.ivSignCancel -> {
                Log.d(TAG, "typeeeeee>>> $typeDone")
                if (!typeDone) {
                    invokeMenuButton(false)
                } else {
                    clearElementView()
                    invokeMenuButton(false)
                }
            }

            R.id.btnSignSave -> {
                this.currentFocus?.let { view ->
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(view.windowToken, 0)
                }
                openSaveAsDialog()
                //openChangesConfirmationDialog()
            }

            R.id.llReplace -> {
                val intent = Intent(this, SignListActivity::class.java)
                intent.putExtra("SIGN_TYPE", false)
                startActivityForResult(intent, 144)
            }

            R.id.llUndo -> {
                ivRedo.imageTintList = getColorStateList(R.color.bottom_gray)
                tvRedo.setTextColor(getColor(R.color.bottom_gray))
                ivReplace.imageTintList = getColorStateList(R.color.img_gray)
                tvReplace.setTextColor(getColor(R.color.img_gray))
                llReplace.isEnabled = false
                llRedo.isEnabled = true

                val focusedChild: View? = mViewPager?.focusedChild
                if (focusedChild != null) {
                    fASPageViewer = (focusedChild as ViewGroup).getChildAt(0) as PDSPageViewer
                    pdsPageViewer = fASPageViewer
                    val visibleRect: RectF = fASPageViewer.visibleRect
                    val width = visibleRect.left + visibleRect.width() / 2.0f - f / 2.0f
                    val height = visibleRect.top + visibleRect.height() / 2.0f - f2 / 2.0f
                    pdsPageViewer.removePerEle(
                        width, height, f, f2,
                        pdsElementUpdate, pdsPageViewer
                    )
                    invokeMenuButton(true)
                }
            }

            R.id.llRedo -> {
                ivRedo.imageTintList = getColorStateList(R.color.img_gray)
                tvRedo.setTextColor(getColor(R.color.img_gray))
                ivReplace.imageTintList = getColorStateList(R.color.bottom_gray)
                tvReplace.setTextColor(getColor(R.color.bottom_gray))
                llReplace.isEnabled = true
                llRedo.isEnabled = false

                val visibleRect: RectF = pdsPageViewer.visibleRect
                val width = visibleRect.left + visibleRect.width() / 2.0f - f / 2.0f
                val height = visibleRect.top + visibleRect.height() / 2.0f - f2 / 2.0f
                val fASElementType2: PDSElement.PDSElementType = fASElementType
                val element: PDSElement =
                    pdsPageViewer.createElement(
                        fASElementType2,
                        signUriPath,
                        width,
                        height,
                        resources.getDimension(R.dimen.sign_field_default_height),
                        resources.getDimension(R.dimen.sign_field_default_height)
                    )
                pdsElementUpdate = element
                invokeMenuButton(true)
            }
        }
    }
}

class DoAsync(var activity: Activity, val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    private var progressDialog: ProgressDialog? = null

    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        Log.i("ASYNC", "doInBackground")
        return null
    }

    override fun onPreExecute() {
        super.onPreExecute()
        Log.i("ASYNC", "onPreExecute")
        progressDialog = ProgressDialog.show(
            activity, activity.getString(R.string.please_wait),
            activity.getString(R.string.convert_pdf_to_image)
        )
        //progressDialog = ProgressDialog(activity)
        //progressDialog!!.setTitle(R.string.please_wait)
        //progressDialog!!.setMessage(R.string.convert_pdf_to_image.toString())
        //progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        //progressDialog!!.isIndeterminate = true
        //progressDialog!!.setMax(100);
        //progressDialog!!.setCancelable(false)
        //progressDialog!!.show()
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        Log.i("ASYNC", "onPostExecute")
        progressDialog!!.dismiss()
    }
}