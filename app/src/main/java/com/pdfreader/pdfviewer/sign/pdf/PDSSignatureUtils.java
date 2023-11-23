package com.pdfreader.pdfviewer.sign.pdf;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;

import com.pdfreader.pdfviewer.sign.R;

import java.io.File;

public class PDSSignatureUtils {

    private static PopupWindow sSignaturePopUpMenu;
    private static View mSignatureLayout;

    public static SignatureView showFreeHandView(Context mCtx, File file) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        //SignatureView createFreeHandView = SignatureUtils.createFreeHandView((((int) mCtx.getResources().getDimension(R.dimen.sign_menu_width)) - ((int) mCtx.getResources().getDimension(R.dimen.sign_left_offset))) - (((int) mCtx.getResources().getDimension(R.dimen.sign_right_offset)) * 3), ((int) mCtx.getResources().getDimension(R.dimen.sign_button_height)) - ((int) mCtx.getResources().getDimension(R.dimen.sign_top_offset)), file, mCtx);
        SignatureView createFreeHandView = SignatureUtils.createFreeHandView((width - ((int) mCtx.getResources().getDimension(R.dimen.sign_left_offset))) - (((int) mCtx.getResources().getDimension(R.dimen.sign_right_offset)) * 3), ((int) mCtx.getResources().getDimension(R.dimen.sign_button_height)) - ((int) mCtx.getResources().getDimension(R.dimen.sign_top_offset)), file, mCtx);
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.addRule(9);
        layoutParams.setMargins((int) mCtx.getResources().getDimension(R.dimen.sign_left_offset), (int) mCtx.getResources().getDimension(R.dimen.sign_top_offset), 0, 0);
        createFreeHandView.setLayoutParams(layoutParams);
        return createFreeHandView;

       /* createFreeHandView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FASSignatureUtils.addSignElement(z);
            }
        });*/
    }

    public static boolean isSignatureMenuOpen() {
        return sSignaturePopUpMenu != null && sSignaturePopUpMenu.isShowing();
    }

    public static void dismissSignatureMenu() {
        if (sSignaturePopUpMenu != null && sSignaturePopUpMenu.isShowing()) {
            sSignaturePopUpMenu.dismiss();
            mSignatureLayout = null;
        }
    }
}
