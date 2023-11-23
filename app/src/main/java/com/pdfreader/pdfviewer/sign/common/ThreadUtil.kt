package com.example.pdfreader.common

import android.os.Handler
import android.util.Log
import timber.log.Timber

class ThreadUtil {
    companion object{
        private var handler: Handler? = null
        private var runnable: Runnable? = null

        fun startTask(iThreadTask: () -> Unit, delayTime: Long) {
            stopTask()
            handler = Handler()
            runnable = Runnable { iThreadTask.invoke() }
            if (handler == null || runnable == null) {
                return
            }
            handler!!.postDelayed(runnable!!, delayTime)
        }

        fun stopTask() {
            try {
                handler!!.removeCallbacks(runnable!!)
                handler!!.removeCallbacksAndMessages(null)
                handler = null
                runnable = null
            } catch (e: Exception) {
                Log.e("ThreadUtil:", "Error:$e")
            }
        }


        interface IThreadTask {
            fun doTask()
        }
        fun <T> tryOrNull(logOnError: Boolean = true, body: () -> T?): T? {
            return try {
                body()
            } catch (e: Exception) {
                if (logOnError) {
                    Timber.w(e)
                }

                null
            }
        }

    }
}