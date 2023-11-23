package com.pdfreader.pdfviewer.sign.pdf;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class ViewUtils {

    public static void constrainRectXY(RectF rectF, RectF rectF2) {
        if (rectF.left < rectF2.left) {
            rectF.left = rectF2.left;
        } else if (rectF.right > rectF2.right) {
            rectF.left = rectF2.right - rectF.width();
        }
        if (rectF.top < rectF2.top) {
            rectF.top = rectF2.top;
        } else if (rectF.bottom > rectF2.bottom) {
            rectF.top = rectF2.bottom - rectF.height();
        }
    }

    public static SignatureView createSignatureView(Context context, PDSElement fASElement, Matrix matrix) {
        SignatureView createFreeHandView;
        RectF rectF = new RectF(fASElement.getRect());
        float strokeWidth = fASElement.getStrokeWidth();
        if (matrix != null) {
            matrix.mapRect(rectF);
            strokeWidth = matrix.mapRadius(strokeWidth);
        }

        createFreeHandView = SignatureUtils.createFreeHandView((int) rectF.height(), fASElement.getFile(), context);
        if (createFreeHandView != null) {
            createFreeHandView.setX(rectF.left);
            createFreeHandView.setY(rectF.top);
        }
        return createFreeHandView;
    }

    public static ImageView createImageView(Context context, PDSElement fASElement, Matrix matrix) {
        ImageView createFreeHandView;
        RectF rectF = new RectF(fASElement.getRect());
        if (matrix != null) {
            matrix.mapRect(rectF);
        }

        createFreeHandView = SignatureUtils.createImageView((int) rectF.height(), fASElement.getBitmap(), context);
        if (createFreeHandView != null) {
            createFreeHandView.setX(rectF.left);
            createFreeHandView.setY(rectF.top);
        }
        return createFreeHandView;
    }

    public static TextView createEditTextView(Context context, PDSElement fASElement, Matrix matrix) {
        TextView createFreeHandView;
        RectF rectF = new RectF(fASElement.getRect());
        float strokeWidth = fASElement.getStrokeWidth();

        if (matrix != null) {
            matrix.mapRect(rectF);
            strokeWidth = matrix.mapRadius(strokeWidth);
        }

        createFreeHandView = SignatureUtils.createEditTextView((int) rectF.height(), fASElement.getText(), context);
        if (createFreeHandView != null) {
            createFreeHandView.setX(rectF.left);
            createFreeHandView.setY(rectF.top);
        }
        return createFreeHandView;
    }
}
