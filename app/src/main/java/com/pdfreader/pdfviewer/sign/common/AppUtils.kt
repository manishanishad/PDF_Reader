package com.pdfreader.pdfviewer.sign.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pdfreader.modalClas.PdfList
import com.pdfreader.pdfviewer.sign.BuildConfig
import com.pdfreader.pdfviewer.sign.R
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppUtils {
    companion object {
        var filePdf: ArrayList<File> = ArrayList()
        var filePdflist: ArrayList<PdfList> = ArrayList()
        private var filePdfName: ArrayList<File> = ArrayList()
        private var fileNameList: ArrayList<String> = ArrayList()

        fun Activity.changeStatusBarColor(color: Int, isLight: Boolean) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                isLight
        }

        fun changeStatusBarColor(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (isDarkModeOn(activity)) {
                    activity.window.decorView.windowInsetsController!!
                        .setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        )
                } else {
                    activity.window.decorView.windowInsetsController!!
                        .setSystemBarsAppearance(0, 0)
                    activity.window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                //activity.window.statusBarColor = getColor(R.color.bgCommon)
            } else {
                val window: Window = activity.window
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                //activity.window.statusBarColor = getColor(R.color.bgCommon)
            }
        }

        private fun isDarkModeOn(context: Context): Boolean {
            val nightModeFlags =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }

        fun isEmptyString(text: String?): Boolean {
            return text === null || text.trim { it <= ' ' } == "null" || text.trim { it <= ' ' } == "" || text.trim { it <= ' ' }
                .isEmpty()
        }

        fun isPermissionGranted(activity: Activity?): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                if ((ActivityCompat.checkSelfPermission(
                        activity!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED)
                ) {
                    //permissionTime = Calendar.getInstance().timeInMillis
                    ActivityCompat.requestPermissions(
                        activity, arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), 1
                    )
                    return false
                }
                return true
            }
        }

        fun shareData(path: String?, activity: Activity) {
            try {
                val file = path?.let { File(it) }
                if (file != null) {
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            activity,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.type = "*/*"
                        intent.putExtra(Intent.EXTRA_STREAM, uri)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        activity.startActivity(intent)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(activity, activity.getString(R.string.error), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        fun getFile(dir: File): ArrayList<File> {
            val listFile = dir.listFiles()
            val fileList = java.util.ArrayList<File>()
            if (listFile != null && listFile.isNotEmpty()) {
                for (i in listFile.indices) {
                    if (listFile[i].isDirectory) {
                        //fileList.add(listFile[i])
                        getFile(listFile[i])
                    } else {
                        if (listFile[i].name.endsWith(".pdf")) {
                            fileList.add(listFile[i])
                            filePdf.add(listFile[i])
                            filePdflist.add(
                                PdfList(
                                    listFile[i].name,
                                    Date(listFile[i].lastModified()),
                                    0.0.toLong(),
                                    getFileSize(listFile[i].length()),
                                    listFile[i].length(),
                                    "pdf",
                                    listFile[i].absolutePath,
                                    false
                                )
                            )
                        }
                        if (listFile[i].name.endsWith(".txt")) {
                            fileList.add(listFile[i])
                            filePdf.add(listFile[i])
                            filePdflist.add(
                                PdfList(
                                    listFile[i].name,
                                    Date(listFile[i].lastModified()),
                                    0.0.toLong(),
                                    getFileSize(listFile[i].length()),
                                    listFile[i].length(),
                                    "txt",
                                    listFile[i].absolutePath,
                                    false
                                )
                            )
                        }
                        if (listFile[i].name.endsWith(".doc")) {
                            fileList.add(listFile[i])
                            filePdf.add(listFile[i])
                            filePdflist.add(
                                PdfList(
                                    listFile[i].name,
                                    Date(listFile[i].lastModified()),
                                    0.0.toLong(),
                                    getFileSize(listFile[i].length()),
                                    listFile[i].length(),
                                    "doc",
                                    listFile[i].absolutePath,
                                    false
                                )
                            )
                        }
                        if (listFile[i].name.endsWith(".docx")) {
                            fileList.add(listFile[i])
                            filePdf.add(listFile[i])
                            filePdflist.add(
                                PdfList(
                                    listFile[i].name,
                                    Date(listFile[i].lastModified()),
                                    0.0.toLong(),
                                    getFileSize(listFile[i].length()),
                                    listFile[i].length(),
                                    "docx",
                                    listFile[i].absolutePath,
                                    false
                                )
                            )
                        }
                        if (listFile[i].name.endsWith(".xlsx")) {
                            fileList.add(listFile[i])
                            filePdf.add(listFile[i])
                            filePdflist.add(
                                PdfList(
                                    listFile[i].name,
                                    Date(listFile[i].lastModified()),
                                    0.0.toLong(),
                                    getFileSize(listFile[i].length()),
                                    listFile[i].length(),
                                    "xlsx",
                                    listFile[i].absolutePath,
                                    false
                                )
                            )
                        }
                        if (listFile[i].name.endsWith(".xls")) {
                            fileList.add(listFile[i])
                            filePdf.add(listFile[i])
                            filePdflist.add(
                                PdfList(
                                    listFile[i].name,
                                    Date(listFile[i].lastModified()),
                                    0.0.toLong(),
                                    getFileSize(listFile[i].length()),
                                    listFile[i].length(),
                                    "xls",
                                    listFile[i].absolutePath,
                                    false
                                )
                            )
                        }
                        if (listFile[i].name.endsWith(".ppt")) {
                            fileList.add(listFile[i])
                            filePdf.add(listFile[i])
                            filePdflist.add(
                                PdfList(
                                    listFile[i].name,
                                    Date(listFile[i].lastModified()),
                                    0.0.toLong(),
                                    getFileSize(listFile[i].length()),
                                    listFile[i].length(),
                                    "ppt",
                                    listFile[i].absolutePath,
                                    false
                                )
                            )
                        }
                        if (listFile[i].name.endsWith(".pptx")) {
                            fileList.add(listFile[i])
                            filePdf.add(listFile[i])
                            filePdflist.add(
                                PdfList(
                                    listFile[i].name,
                                    Date(listFile[i].lastModified()),
                                    0.0.toLong(),
                                    getFileSize(listFile[i].length()),
                                    listFile[i].length(),
                                    "pptx",
                                    listFile[i].absolutePath,
                                    false
                                )
                            )
                        }
                    }
                }
            }
            return filePdf
        }

        fun openPlayStore(activity: Activity) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_VIEW
            val packageName = "com.example.pdfreader"
            sendIntent.data = Uri.parse("market://details?id=$packageName")
            if (sendIntent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(sendIntent)
            }
        }

        fun moveToEmail(activity: Activity) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("neonappsapple@gmail.com"))
            intent.putExtra(
                Intent.EXTRA_SUBJECT,
                activity.getString(R.string.app_name) + " Support"
            )
            intent.putExtra(
                Intent.EXTRA_TEXT, StringBuilder("\n\n")
                    .append("\n\n--- Tell Us Your Thoughts above this line ---\n\n")
                    .append("Package: ${activity.packageName}\n")
                    .append("Version: ${BuildConfig.VERSION_NAME}\n")
                    .append("Version Code: ${BuildConfig.VERSION_CODE}\n")
                    .append("Device: ${Build.BRAND} ${Build.MODEL}\n")
                    .append("SDK: ${Build.VERSION.SDK_INT}\n")
                    .toString()
            )
            activity.startActivity(intent)
        }

        /*private fun getFileSize(length: Long): String {
            val size = length / 1000.0 // Get size and convert bytes into KB.
            return if (size >= 1024) {
                (size / 1024).toString() + " MB"
            } else {
                "${String.format("%.0f")} KB"
            }
        }*/

        fun changeLanguage(activity: Activity) {
            var langCode: String =
                PreferencesManager.getString(activity, PreferencesManager.PREF_LANGUAGE)
            if (!isEmptyString(langCode)) {
                PreferencesManager.getString(activity, PreferencesManager.PREF_LANGUAGE)
            } else {
                langCode = "en"
            }
            Log.d(TAG, "Languagee:::: $langCode")
            val locale = Locale(langCode)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        }

        fun getFileSize(length: Long): String {
            var hrSize: String? = null
            val b = length.toDouble()
            val k = length / 1024.0
            val m = length / 1024.0 / 1024.0
            val g = length / 1024.0 / 1024.0 / 1024.0
            val t = length / 1024.0 / 1024.0 / 1024.0 / 1024.0
            //val dec = DecimalFormat("0.00")
            val dec = DecimalFormat("0")
            hrSize = if (t > 1) {
                dec.format(t).plus(" TB")
            } else if (g > 1) {
                dec.format(g).plus(" GB")
            } else if (m > 1) {
                dec.format(m).plus(" MB")
            } else if (k > 1) {
                dec.format(k).plus(" KB")
            } else {
                dec.format(b).plus(" Bytes")
            }
            return hrSize
        }

        fun Context.rateUs() {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }

        fun View.hideKeyboard() {
            val imm =
                context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }

        fun View.openKeyboard() {
            val imm =
                context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        fun Activity.sendEmail() {
            val addresses = arrayOf("itechsolution.feedback@gmail.com")
            val subject = "Feed back " + applicationContext.getString(R.string.app_name) + ""
            val body =
                "Tell us which issues you are facing using " + applicationContext.getString(R.string.app_name) + " App?"
            try {
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                emailIntent.type = "plain/text"
                emailIntent.setClassName(
                    "com.google.android.gm",
                    "com.google.android.gm.ComposeActivityGmail"
                )
//        emailIntent.setPackage("com.google.android.gm");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses)
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                emailIntent.putExtra(Intent.EXTRA_TEXT, body)
                startActivity(emailIntent)
            } catch (e: Exception) {
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse("mailto:" + addresses[0])
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                emailIntent.putExtra(Intent.EXTRA_TEXT, body)
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email using..."))
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun deleteListData(path: String?, list: java.util.ArrayList<PdfList>): Int {
            Log.d(TAG, "Pathhhh:::: $path")
            var i = 0
            var j = 0
            val l = list.size
            while (i < l) {
                if (list[i].absPath == path) {
                    j = i
                }
                i++
            }
            if (list.size > 0) {
                list.removeAt(j)
            }
            return j
        }

        fun renameListData(path: String?, list: java.util.ArrayList<PdfList>): Int {
            Log.d(TAG, "Pathhhh:::: $path")
            var i = 0
            var j = 0
            val l = list.size
            while (i < l) {
                if (list[i].absPath == path) {
                    j = i
                }
                i++
            }
            /*if (list.size > 0) {
                list.removeAt(j)
            }*/
            return j
        }

        @SuppressLint("SimpleDateFormat")
        fun Date.toSimpleString(): String {
            val format = SimpleDateFormat("dd-MM-yyyy")
            return format.format(this)
        }

        fun getRealPathFromURI(uri: Uri, context: Context): String {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            Log.d(TAG, "fileListtttt:::: $name")
            val dir = File(Environment.getExternalStorageDirectory().absolutePath)
            filePdfName.clear()
            fileNameList.clear()
            val fileList = getFileName(dir, name)
            Log.d(TAG, "fileListtttt:::: $fileList")
            Log.d(TAG, "fileListtttt:::: ${fileList[0].absolutePath}")
            return if (fileNameList.size > 0) {
                fileNameList[0]
            } else {
                ""
            }
        }

        private fun getFileName(dir: File, name: String): ArrayList<File> {
            val listFile = dir.listFiles()
            val fileList = java.util.ArrayList<File>()
            if (listFile != null && listFile.isNotEmpty()) {
                for (i in listFile.indices) {
                    if (listFile[i].isDirectory) {
                        //fileList.add(listFile[i])
                        getFileName(listFile[i], name)
                    } else {
                        filePdfName.add(listFile[i])
                        fileList.add(listFile[i])
                        if (listFile[i].name.equals(name)) {
                            fileNameList.add(listFile[i].absolutePath)
                        }
                    }
                }
            }
            return filePdfName
        }
    }
}