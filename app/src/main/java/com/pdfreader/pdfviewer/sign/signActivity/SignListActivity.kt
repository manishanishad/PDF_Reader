package com.pdfreader.pdfviewer.sign.signActivity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.example.pdfreader.common.Signature
import com.example.pdfreader.modalClas.SignList
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.pdf.SignatureUtils
import com.pdfreader.pdfviewer.sign.pdf.SignatureView
import kotlinx.android.synthetic.main.activity_pdf_view.toolBar
import kotlinx.android.synthetic.main.activity_sign_list.ivEditSign
import kotlinx.android.synthetic.main.activity_sign_list.llNoSign
import kotlinx.android.synthetic.main.activity_sign_list.llNoSignBottom
import kotlinx.android.synthetic.main.activity_sign_list.rvSign
import kotlinx.android.synthetic.main.confirmation_dialog.btnCancel
import kotlinx.android.synthetic.main.confirmation_dialog.btnDelete
import kotlinx.android.synthetic.main.dialog_add_sign.ivSignBlack
import kotlinx.android.synthetic.main.dialog_add_sign.ivSignBlue
import kotlinx.android.synthetic.main.dialog_add_sign.ivSignGreen
import kotlinx.android.synthetic.main.dialog_add_sign.ivSignOrange
import kotlinx.android.synthetic.main.dialog_add_sign.ivSignPink
import kotlinx.android.synthetic.main.dialog_add_sign.ivSignRed
import kotlinx.android.synthetic.main.dialog_add_sign.ivSignSkyBlue
import kotlinx.android.synthetic.main.dialog_add_sign.ivSignYellow
import kotlinx.android.synthetic.main.home_toolbar.ivDrawerMenu
import kotlinx.android.synthetic.main.home_toolbar.ivSearch
import kotlinx.android.synthetic.main.home_toolbar.tvPdfReader
import java.io.File
import java.util.Arrays

@SuppressLint("UseCompatLoadingForDrawables")
class SignListActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var signAdapter: SignAdapter
    private val signList = ArrayList<SignList>()
    private var items: List<File> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_list)
        AppUtils.changeStatusBarColor(this)
        window.statusBarColor = getColor(R.color.bgCommon)
        initView()
        clickListener()
    }

    private fun initView() {
        toolBar.setBackgroundColor(getColor(R.color.bgCommon))
        ivDrawerMenu.setImageDrawable(getDrawable(R.drawable.ic_back))
        ivSearch.visibility = View.GONE
        tvPdfReader.text = getString(R.string.sign_list)
        //db = DBHelper(this, null)
        //signAdapter = SignAdapter(signList, this@SignListActivity)
        //signAdapter = SignAdapter(items)
        //signList.addAll(db.getSign())
        //signList.addAll(items)
        //rvSign.adapter = signAdapter
        setAdapter()
        //Log.d(TAG, "GetSign::: ${db.getSign()}")
        //Log.d(TAG, "GetSign--->>> ${signList.size}")
    }

    private fun setAdapter() {
        val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + "sign_check"
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + "sign_check")
        }

        if (!file.exists()) {
            file.mkdir()
        }

        items = java.util.ArrayList<File>();
        val myDir = File("$file")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val files = myDir.listFiles()

        if (files != null) {
            Arrays.sort(files) { file1, file2 ->
                val result = file2?.lastModified()!! - file1?.lastModified()!!
                if (result < 0) {
                    -1
                } else if (result > 0) {
                    1
                } else {
                    0
                }
            }
        }

        if (files != null) {
            (items as java.util.ArrayList<File>).clear()
            for (i in files) {
                //signList.add(i, SignList(files[i].absolutePath))
                (items as java.util.ArrayList<File>).add(i)
                Log.d(TAG, "GetSign>>>>> $items")
            }
        }

        signAdapter = SignAdapter(items, this)
        rvSign.adapter = signAdapter
        signAdapter.notifyDataSetChanged()

        if (items.isNotEmpty()) {
            llNoSign.visibility = View.GONE
            llNoSignBottom.visibility = View.GONE
        } else {
            rvSign.adapter = signAdapter
            llNoSign.visibility = View.VISIBLE
            llNoSignBottom.visibility = View.VISIBLE
        }

        signAdapter.setOnItemClickListener(object : SignAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: File?, pos: Int) {
                val filePath = obj?.path
                Log.d(TAG, "Pathhh:::: $filePath")
                val signType = intent.getBooleanExtra("SIGN_TYPE", false)
                Log.d(TAG, "Typeee--->>> $signType")
                intent.putExtra("TYPE_SIGN", signType)
                intent.putExtra("FilePath", filePath)
                setResult(RESULT_OK, intent)
                finish()
            }

            override fun onDeleteItemClick(view: View?, obj: File?, pos: Int) {
                openConfirmationDialog(obj, pos)
            }
        })

    }

    private fun clickListener() {
        ivDrawerMenu.setOnClickListener(this)
        ivEditSign.setOnClickListener(this)
    }

    private fun openConfirmationDialog(signatureList: File?, position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.confirmation_dialog)
        dialog.btnDelete.setOnClickListener {
            /*val file = File(signList[position].signImage)
            file.delete()
            db.deleteSign(signList[position].signImage)
            signatureList.removeAt(position)
            setAdapter()
            dialog.dismiss()
            signAdapter.notifyDataSetChanged()*/

            if (signatureList != null) {
                if (signatureList.exists()) {
                    signatureList.delete()
                }
            }
            setAdapter()
            signAdapter.notifyItemInserted(items.size - 1)
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

    @SuppressLint("ObsoleteSdkInt")
    private fun openAddSignDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_add_sign)
        //val mContent = dialog.findViewById<View>(R.id.etAddSign) as LinearLayout
        val mSignature = Signature(applicationContext, null)
        val localSignatureView: SignatureView = dialog.findViewById(R.id.signatureView)

        val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + "sign_check"
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + "sign_check")
        }

        if (!file.exists()) {
            file.mkdir()
        }

        //mContent.addView(mSignature,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)

        val mGetSign = dialog.findViewById<View>(R.id.btnSignDone) as Button
        val mCancelSign = dialog.findViewById<View>(R.id.btnSignCancel) as Button
        mCancelSign.setOnClickListener {
            dialog.dismiss()
        }
        mGetSign.setOnClickListener {
            val localArrayList: ArrayList<*>? = localSignatureView.mInkList
            if (localArrayList != null && localArrayList.size > 0) {
                //isFreeHandCreated = true
            }
            SignatureUtils.saveSSignature(applicationContext, localSignatureView)
            /*val folderName = "sign_check"
            val folderPath = System.currentTimeMillis()
            val signalSignPath = commonDocumentDirPath(folderName, folderPath.toString()).absolutePath
            val sign = mSignature.saveSign(mContent, signalSignPath)
            db.addSign(signalSignPath)
            signList.add(SignList(signalSignPath))*/
            setAdapter()
            dialog.dismiss()
            //recreate()
        }

        dialog.ivSignBlack.setOnClickListener {
            localSignatureView.setStrokeColor(ContextCompat.getColor(this, R.color.black))
            //mSignature.setColor(ContextCompat.getColor(this, R.color.black))
            dialog.ivSignBlack.setBackgroundResource(R.drawable.ic_bg_color)
            dialog.ivSignYellow.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignRed.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignPink.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignOrange.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignSkyBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignGreen.setBackgroundColor(getColor(R.color.white_black))
        }
        dialog.ivSignYellow.setOnClickListener {
            localSignatureView.setStrokeColor(ContextCompat.getColor(this, R.color.yellow))
            dialog.ivSignYellow.setBackgroundResource(R.drawable.ic_bg_color)
            dialog.ivSignBlack.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignRed.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignPink.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignOrange.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignSkyBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignGreen.setBackgroundColor(getColor(R.color.white_black))
        }
        dialog.ivSignRed.setOnClickListener {
            localSignatureView.setStrokeColor(ContextCompat.getColor(this, R.color.red))
            dialog.ivSignRed.setBackgroundResource(R.drawable.ic_bg_color)
            dialog.ivSignBlack.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignYellow.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignPink.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignOrange.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignSkyBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignGreen.setBackgroundColor(getColor(R.color.white_black))
        }
        dialog.ivSignBlue.setOnClickListener {
            localSignatureView.setStrokeColor(ContextCompat.getColor(this, R.color.blue))
            dialog.ivSignBlue.setBackgroundResource(R.drawable.ic_bg_color)
            dialog.ivSignBlack.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignYellow.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignRed.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignPink.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignOrange.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignSkyBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignGreen.setBackgroundColor(getColor(R.color.white_black))
        }
        dialog.ivSignPink.setOnClickListener {
            localSignatureView.setStrokeColor(ContextCompat.getColor(this, R.color.pink))
            dialog.ivSignPink.setBackgroundResource(R.drawable.ic_bg_color)
            dialog.ivSignBlack.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignYellow.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignRed.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignOrange.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignSkyBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignGreen.setBackgroundColor(getColor(R.color.white_black))
        }
        dialog.ivSignOrange.setOnClickListener {
            localSignatureView.setStrokeColor(ContextCompat.getColor(this, R.color.orange))
            dialog.ivSignOrange.setBackgroundResource(R.drawable.ic_bg_color)
            dialog.ivSignBlack.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignYellow.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignRed.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignPink.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignSkyBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignGreen.setBackgroundColor(getColor(R.color.white_black))
        }
        dialog.ivSignSkyBlue.setOnClickListener {
            localSignatureView.setStrokeColor(ContextCompat.getColor(this, R.color.blue_sky))
            dialog.ivSignSkyBlue.setBackgroundResource(R.drawable.ic_bg_color)
            dialog.ivSignBlack.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignYellow.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignRed.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignPink.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignOrange.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignGreen.setBackgroundColor(getColor(R.color.white_black))
        }
        dialog.ivSignGreen.setOnClickListener {
            localSignatureView.setStrokeColor(ContextCompat.getColor(this, R.color.green))
            dialog.ivSignGreen.setBackgroundResource(R.drawable.ic_bg_color)
            dialog.ivSignBlack.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignYellow.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignRed.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignBlue.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignPink.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignOrange.setBackgroundColor(getColor(R.color.white_black))
            dialog.ivSignSkyBlue.setBackgroundColor(getColor(R.color.white_black))
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

    private fun commonDocumentDirPath(folderName: String, folderPath: String): File {
        val dir: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + File.separator + folderName + File.separator + folderPath
            )
        } else {
            File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + folderName + File.separator + folderPath
            )
        }
        return dir
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivDrawerMenu -> finish()
            R.id.ivEditSign -> openAddSignDialog()
        }
    }
}