package com.pdfreader.pdfviewer.sign.pdf;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pdfreader.pdfviewer.sign.R;
import com.pdfreader.pdfviewer.sign.pdfViewer.PdfViewActivity;
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class PDSSaveAsPDFAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private String mfileName;
    PdfViewActivity mCtx;

    ProgressDialog progressDialog;


    public PDSSaveAsPDFAsyncTask(PdfViewActivity context, String str) {
        this.mCtx = context;
        this.mfileName = str;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(this.mCtx, "", this.mCtx.getString(R.string.please_wait));
        //mCtx.savingProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public Boolean doInBackground(Void... voidArr) {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        PDSPDFDocument document = mCtx.getDocument();
        /*File root = mCtx.getFilesDir();

        File myDir = new File(root + "/DigitalSignature");
        Log.d(TAG, "Directoryy::: " + myDir);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }*/

        //File file = new File(myDir.getAbsolutePath(), mfileName);

        File file;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + "pdf_reader" + "/" + mfileName);
        } else {
            file = new File(Environment.getExternalStorageDirectory() + "/" + "pdf_reader" + "/" + mfileName);
        }
        Log.d(TAG, "Directoryy---->>> " + file);

        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }

        try {
            InputStream stream = document.stream;
            FileOutputStream os = new FileOutputStream(file);
            Log.d(TAG, "Directoryy:::::>>> " + os);
            PdfReader reader = new PdfReader(stream);
            PdfStamper signer = null;
            Bitmap createBitmap = null;

            for (int i = 0; i < document.getNumPages(); i++) {
                Rectangle mediabox = reader.getPageSize(i + 1);
                for (int j = 0; j < document.getPage(i).getNumElements(); j++) {
                    PDSPDFPage page = document.getPage(i);
                    PDSElement element = page.getElement(j);
                    RectF bounds = element.getRect();
                    Log.d(TAG, "fASElementTypeeeee:::::" + element.getType());
                    if (element.getType() == PDSElement.PDSElementType.PDSElementTypeSignature) {
                        PDSElementViewer viewer = element.mElementViewer;
                        View dummy = viewer.getElementView();
                        View view = ViewUtils.createSignatureView(mCtx, element, viewer.mPageViewer.getToViewCoordinatesMatrix());
                        createBitmap = Bitmap.createBitmap(dummy.getWidth(), dummy.getHeight(), Bitmap.Config.ARGB_8888);
                        view.draw(new Canvas(createBitmap));
                    } else if (element.getType() == PDSElement.PDSElementType.PDSElementTypeEditText) {
                        PDSElementViewer viewer = element.mElementViewer;
                        View dummy = viewer.getElementView();
                        View view = ViewUtils.createEditTextView(mCtx, element, viewer.mPageViewer.getToViewCoordinatesMatrix());
                        TextView mTextView = ViewUtils.createEditTextView(mCtx, element, viewer.mPageViewer.getToViewCoordinatesMatrix());
                        mTextView.setCursorVisible(false);
                        mTextView.buildDrawingCache();
                        createBitmap = Bitmap.createBitmap(dummy.getWidth(), dummy.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(createBitmap);

                        Paint paint = new Paint();
                        paint.setColor(Color.TRANSPARENT);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawPaint(paint);
                        paint.setColor(Integer.parseInt(PreferencesManager.Companion.getString(mCtx, PreferencesManager.TEXT_COLOR)));
                        paint.setTextSize(20);

                        String valueX = PreferencesManager.Companion.getString(mCtx, PreferencesManager.SET_POSITION_X);
                        String valueY = PreferencesManager.Companion.getString(mCtx, PreferencesManager.SET_POSITION_Y);

                        if (valueX.isEmpty()) {
                            valueX = "0";
                        } else {
                            valueX = PreferencesManager.Companion.getString(mCtx, PreferencesManager.SET_POSITION_X);
                        }

                        if (valueY.isEmpty()) {
                            valueY = "0";
                        } else {
                            valueY = PreferencesManager.Companion.getString(mCtx, PreferencesManager.SET_POSITION_Y);
                        }
                        Log.d(TAG, "PrefValueee:::>>>> " + valueX);
                        Log.d(TAG, "PrefValueee::-->>> " + valueY);
                        int x = 95;
                        int y = 90;
                        if (valueX.equals("0") && valueY.equals("0")) {
                            Log.d(TAG, "Ifff");
                            canvas.drawText(PreferencesManager.Companion.getString(mCtx, PreferencesManager.SET_TEXT), 109.68103F, 80.23279F, paint);
                        } else {
                            Log.d(TAG, "Elsssss");
                            canvas.drawText(PreferencesManager.Companion.getString(mCtx, PreferencesManager.SET_TEXT), Float.parseFloat(valueX), Float.parseFloat(valueY), paint);
                        }
                        view.draw(canvas);
                    } else {
                        createBitmap = element.getBitmap();
                    }
                    ByteArrayOutputStream saveBitmap = new ByteArrayOutputStream();
                    createBitmap.compress(Bitmap.CompressFormat.PNG, 100, saveBitmap);
                    byte[] byteArray = saveBitmap.toByteArray();
                    createBitmap.recycle();

                    Image sigimage = Image.getInstance(byteArray);
                    if (mCtx.getAliases() != null && mCtx.getKeyStore() != null && mCtx.getDigitalIDPassword() != null) {
                        KeyStore ks = mCtx.getKeyStore();
                        String alias = mCtx.getAliases();
                        PrivateKey pk = (PrivateKey) ks.getKey(alias, mCtx.getDigitalIDPassword().toCharArray());
                        Certificate[] chain = ks.getCertificateChain(alias);
                        if (signer == null)
                            signer = PdfStamper.createSignature(reader, os, '\0');

                        PdfSignatureAppearance appearance = signer.getSignatureAppearance();

                        float top = mediabox.getHeight() - (bounds.top + bounds.height());
                        appearance.setVisibleSignature(new Rectangle(bounds.left, top, bounds.left + bounds.width(), top + bounds.height()), i + 1, "sig" + j);
                        appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
                        appearance.setSignatureGraphic(sigimage);
                        ExternalDigest digest = new BouncyCastleDigest();
                        ExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, null);
                        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CADES);
                    } else {
                        if (signer == null)
                            signer = new PdfStamper(reader, os, '\0');
                        PdfContentByte contentByte = signer.getOverContent(i + 1);
                        sigimage.setAlignment(Image.ALIGN_UNDEFINED);
                        sigimage.scaleToFit(bounds.width(), bounds.height());
                        sigimage.setAbsolutePosition(bounds.left - (sigimage.getScaledWidth() - bounds.width()) / 2, mediabox.getHeight() - (bounds.top + bounds.height()));
                        contentByte.addImage(sigimage);
                    }
                }
            }
            PreferencesManager.Companion.setString(mCtx, PreferencesManager.SET_POSITION_X, "0");
            PreferencesManager.Companion.setString(mCtx, PreferencesManager.SET_POSITION_Y, "0");
            if (signer != null)
                signer.close();
            if (reader != null)
                reader.close();
            if (os != null)
                os.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (file.exists()) {
                file.delete();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onPostExecute(Boolean result) {
        mCtx.runPostExecution();
        if (!result) {
            Toast.makeText(mCtx,
                    R.string.something_wrong_in_sign, Toast.LENGTH_LONG).show();
        }
        progressDialog.dismiss();
        /*else {
            //Toast.makeText(mCtx, "PDF document saved successfully", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }*/
    }
}

