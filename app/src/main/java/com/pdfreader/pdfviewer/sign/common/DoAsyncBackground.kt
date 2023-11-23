package com.pdfreader.pdfviewer.sign.common

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import com.pdfreader.pdfviewer.sign.R

class DoAsyncBackground(var activity: Activity, val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
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
            activity, "",
            activity.getString(R.string.please_wait)
        )
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        Log.i("ASYNC", "onPostExecute")
        progressDialog!!.dismiss()
    }
}