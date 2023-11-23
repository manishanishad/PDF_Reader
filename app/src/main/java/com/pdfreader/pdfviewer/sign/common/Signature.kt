package com.example.pdfreader.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.pdfreader.pdfviewer.sign.R
import java.io.FileOutputStream

class Signature(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val paint = Paint()
    private val path: Path = Path()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val dirtyRect = RectF()
    private var bitmap: Bitmap? = null

    init {
        paint.isAntiAlias = true
        paint.color = context?.getColor(R.color.black)!!
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = STROKE_WIDTH
    }

    fun saveSign(v: View, storePath: String?) {
        Log.v("tag", "Width: " + v.width)
        Log.v("tag", "Height: " + v.height)
        Log.v("tag", "StorePath: " + storePath)
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(
                v.width,
                v.height,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = bitmap?.let { Canvas(it) }
        try {
            // Output the file
            val mFileOutStream = FileOutputStream(storePath)
            v.draw(canvas)
            // Convert the output file to Image such as .png
            bitmap?.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream)
            mFileOutStream.flush()
            mFileOutStream.close()
        } catch (e: Exception) {
            Log.v("log_tag", e.toString())
        }
    }

    fun setLayoutParams(i: Int, i2: Int) {
        bitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888)
        /*val canvas = Canvas(bitmap)
        setStrokeWidth(this.mStrokeWidthInDocSpace)
        layoutParams = RelativeLayout.LayoutParams(i, i2)
        this.mLayoutHeight = i2
        this.mLayoutWidth = i
        val arrayList: ArrayList<*> = this.mInkList
        val boundingBox: RectF = getBoundingBox()
        clear()
        initializeInkList(arrayList)
        scaleAndTranslatePath(arrayList, boundingBox, 1.0f, 1.0f, 0.0f, 0.0f)*/
    }

    fun clear() {
        path.reset()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        //btnSignDone.isEnabled = true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                lastTouchX = eventX
                lastTouchY = eventY
                return true
            }

            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                resetDirtyRect(eventX, eventY)
                val historySize = event.historySize
                var i = 0
                while (i < historySize) {
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)
                    expandDirtyRect(historicalX, historicalY)
                    path.lineTo(historicalX, historicalY)
                    i++
                }
                path.lineTo(eventX, eventY)
            }

            else -> {
                debug("Ignored touch event: $event")
                return false
            }
        }
        invalidate(
            (dirtyRect.left - HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt()
        )
        lastTouchX = eventX
        lastTouchY = eventY
        return true
    }

    private fun debug(string: String) {
        Log.v("log_tag", string)
    }

    private fun expandDirtyRect(historicalX: Float, historicalY: Float) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY
        }
    }

    private fun resetDirtyRect(eventX: Float, eventY: Float) {
        dirtyRect.left = lastTouchX.coerceAtMost(eventX)
        dirtyRect.right = lastTouchX.coerceAtLeast(eventX)
        dirtyRect.top = lastTouchY.coerceAtMost(eventY)
        dirtyRect.bottom = lastTouchY.coerceAtLeast(eventY)
    }

    fun setColor(color: Int) {
        paint.color = color
    }

    companion object {
        private const val STROKE_WIDTH = 5f
        private const val HALF_STROKE_WIDTH = STROKE_WIDTH / 2
    }
}