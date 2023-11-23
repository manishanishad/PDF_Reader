package com.reader.office.officereader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.reader.office.R;
import com.reader.office.common.IOfficeToPicture;
import com.reader.office.constant.EventConstant;
import com.reader.office.constant.MainConstant;
import com.reader.office.constant.wp.WPViewConstant;
import com.reader.office.mychanges.slidernativead.AppsLinks;
import com.reader.office.mychanges.slidernativead.SliderNativeAd;
import com.reader.office.mychanges.utils.ExtensionFunKt;
import com.reader.office.mychanges.utils.SharedPref;
import com.reader.office.officereader.beans.AImageButton;
import com.reader.office.officereader.beans.AImageCheckButton;
import com.reader.office.officereader.beans.AToolsbar;
import com.reader.office.officereader.beans.CalloutToolsbar;
import com.reader.office.officereader.beans.PDFToolsbar;
import com.reader.office.officereader.beans.PGToolsbar;
import com.reader.office.officereader.beans.SSToolsbar;
import com.reader.office.officereader.beans.WPToolsbar;
import com.reader.office.officereader.database.DBService;
import com.reader.office.res.ResKit;
import com.reader.office.ss.sheetbar.SheetBar;
import com.reader.office.system.FileKit;
import com.reader.office.system.IControl;
import com.reader.office.system.IMainFrame;
import com.reader.office.system.MainControl;
import com.reader.office.system.beans.pagelist.IPageListViewListener;
import com.reader.office.system.dialog.ColorPickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppActivity extends AppCompatActivity implements IMainFrame {

    private SliderNativeAd sliderNativeAd;
    String fileName = "";
    String fileDate = "";
    String fileExt = "";
    String fileSize = "";
    Boolean fileFav = false;
    int itemPos = 0;
    int fileType = 0;
    Boolean isBookmarked = false;
    ConstraintLayout clPdfView;
    LinearLayout layoutDetails;
    LinearLayout layoutDelete;
    LinearLayout llRename;
    Long mLastClickTime = 0L;
    Toolbar toolbar;


    @SuppressLint("UseCompatLoadingForDrawables")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        ExtensionFunKt.hideStatusBar(this);

        control = new MainControl(this);
        appFrame = new AppFrame(getApplicationContext());
        appFrame.post(this::init);
        control.setOffictToPicture(new IOfficeToPicture() {
            public Bitmap getBitmap(int componentWidth, int componentHeight) {
                if (componentWidth == 0 || componentHeight == 0) {
                    return null;
                }
                if (bitmap == null || bitmap.getWidth() != componentWidth || bitmap.getHeight() != componentHeight) {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    bitmap = Bitmap.createBitmap((int) (componentWidth), (int) (componentHeight), Config.ARGB_8888);
                }
                return bitmap;
            }

            public void callBack(Bitmap bitmap) {
                saveBitmapToFile(bitmap);
            }

            private Bitmap bitmap;

            @Override
            public void setModeType(byte modeType) {

            }

            @Override
            public byte getModeType() {
                return VIEW_CHANGE_END;
            }

            @Override
            public boolean isZoom() {
                return false;
            }

            @Override
            public void dispose() {
            }
        });
        setTheme(control.getSysKit().isVertical(this) ? R.style.title_background_vertical : R.style.title_background_horizontal);
        setContentView(appFrame);

        clPdfView = findViewById(R.id.clPdfView);
        layoutDetails = findViewById(R.id.layoutDetails);
        layoutDelete = findViewById(R.id.layoutDelete);
        llRename = findViewById(R.id.llRename);
        toolbar = new Toolbar(this);
        LayoutParams toolBarParams = new LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, 100);
        toolbar.setLayoutParams(toolBarParams);
        toolbar.setBackgroundColor(getResources().getColor(R.color.bgCommon));
        toolbar.setPopupTheme(R.style.Theme_AppCompat);
        toolbar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        fileType = intent.getIntExtra("fileType", 0);
        fileName = intent.getStringExtra("fileName");
        fileDate = intent.getStringExtra("fileDate");
        fileExt = intent.getStringExtra("fileExt");
        fileSize = intent.getStringExtra("fileSize");
        fileFav = intent.getBooleanExtra("fileFav", false);
        itemPos = intent.getIntExtra("itemPosition", 0);
        isBookmarked = Objects.requireNonNull(intent.getExtras()).getBoolean("IsBookmarked", false);
        SharedPref sharedPref = new SharedPref(this);
        ArrayList<AppsLinks> crossPromotionRemoteList = new ArrayList<>(sharedPref.getRemoteList("crossPromotionRemoteList"));

        setSupportActionBar(toolbar);

        toolbar.setTitle(fileName);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(getColor(R.color.black));
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            TextView titleTextView = (TextView) f.get(toolbar);
            //assert titleTextView != null;
            if (titleTextView != null) {
                titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                titleTextView.setFocusable(true);
                titleTextView.setFocusableInTouchMode(true);
                titleTextView.requestFocus();
                titleTextView.setSingleLine(true);
                titleTextView.setSelected(true);
                titleTextView.setMarqueeRepeatLimit(-1);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        appFrame.addView(toolbar);

        /*RecyclerView adLayout = new RecyclerView(this);
        adLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        adLayout.setBackgroundColor(getResources().getColor(R.color.white));*/
        //appFrame.addView(adLayout);

        /*sliderNativeAd = new SliderNativeAd(this, adLayout);
        sliderNativeAd.refreshAd(crossPromotionRemoteList);*/

        ExtensionFunKt.setRenameCompleteCallback((currentFile, newFile) -> toolbar.setTitle(newFile.getName()));
        ExtensionFunKt.setDeleteCompleteCallback((currentFile) -> finish());
        ExtensionFunKt.setBookmarkCompleteCallback((currentFile, isBookmark) -> isBookmarked = isBookmark);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sys_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sys_menu_settings) {
            try {
                /*ExtensionFunKt.showBottomSheet(
                        this,
                        appFrame.getRootView(),
                        ExtensionFunKt.getDataModelFromFile(
                                this,
                                new File(filePath),
                                false, isBookmarked
                        )
                );*/
                displayBottomSheet();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.sys_menu_share) {
            ExtensionFunKt.shareDocument(this, filePath);
        } else {
            return false;
        }
        return true;
    }


    private void displayBottomSheet() {
        final BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View layout = LayoutInflater.from(this).inflate(R.layout.dialog_menu, clPdfView);
        dialog.setContentView(layout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);

        TextView docName = layout.findViewById(R.id.tvPdfName);
        TextView docDate = layout.findViewById(R.id.tvPdfDate);
        TextView docSize = layout.findViewById(R.id.tvPdfSize);
        ImageView docImage = layout.findViewById(R.id.ivPdfIcon);
        ImageView docFav = layout.findViewById(R.id.ivPdfFav);
        LinearLayout layoutRename = layout.findViewById(R.id.llRename);
        LinearLayout layoutShare = layout.findViewById(R.id.llShare);
        LinearLayout layoutDelete = layout.findViewById(R.id.llDelete);
        LinearLayout layoutDetails = layout.findViewById(R.id.llDetails);

        docName.setText(fileName);
        docDate.setText(fileDate);
        docSize.setText(fileSize);
        if (Objects.equals(fileExt, "ppt")) {
            docImage.setImageResource(R.drawable.ic_ppt);
        } else if (Objects.equals(fileExt, "docx") || Objects.equals(fileExt, "doc")) {
            docImage.setImageResource(R.drawable.ic_docs);
        } else if (Objects.equals(fileExt, "xls") || Objects.equals(fileExt, "xlsx")) {
            docImage.setImageResource(R.drawable.ic_xls);
        } else if (Objects.equals(fileExt, "txt")) {
            docImage.setImageResource(R.drawable.ic_txt);
        }

        if (fileType == 1) {
            layoutRename.setVisibility(View.GONE);
            layoutDelete.setVisibility(View.GONE);
        } else {
            layoutRename.setVisibility(View.VISIBLE);
            layoutDelete.setVisibility(View.VISIBLE);
        }

        if (fileFav) {
            docFav.setImageResource(R.drawable.ic_fill_star);
        } else {
            docFav.setImageResource(R.drawable.ic_unfill_star);
        }

        docFav.setOnClickListener(v -> {
            if (!fileFav) {
                fileFav = true;
                docFav.setImageResource(R.drawable.ic_fill_star);
            } else {
                fileFav = false;
                docFav.setImageResource(R.drawable.ic_unfill_star);
            }
            Intent intent = new Intent("FAVORITE");
            intent.putExtra("checkFav", fileFav);
            intent.putExtra("checkFavPath", filePath);
            intent.putExtra("checkFavName", fileName);
            intent.putExtra("checkPos", itemPos);
            EventBus.getDefault().post(intent);
        });

        layoutRename.setOnClickListener(v -> {
            openRenameDialog(fileName, itemPos);
            dialog.dismiss();
        });

        layoutShare.setOnClickListener(v -> {
            ExtensionFunKt.shareDocument(this, filePath);
            dialog.dismiss();
        });

        layoutDelete.setOnClickListener(v -> {
            openDeleteDialog(itemPos);
            dialog.dismiss();
        });

        layoutDetails.setOnClickListener(v -> {
            openDetailsDialog();
            dialog.dismiss();
        });

        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        dialog.show();
    }

    private void openRenameDialog(String name, int position) {
        Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.setContentView(R.layout.rename_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        EditText etName = dialog.findViewById(R.id.etName);
        Button btnRenameCancel = dialog.findViewById(R.id.btnRenameCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        etName.setText(name.substring(0, name.lastIndexOf('.')));

        btnRenameCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            if (etName.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_file_name), Toast.LENGTH_SHORT).show();
            } else {
                String path = Objects.requireNonNull(new File(filePath).getParentFile()).getAbsolutePath();
                File oldName = new File(filePath.trim());
                File newName = new File(path + ("/") + etName.getText().toString().trim() + "." + fileExt);
                oldName.renameTo(newName);
                fileName = etName.getText().toString().trim() + "." + fileExt;
                toolbar.setTitle(etName.getText().toString().trim() + "." + fileExt);
                /*Intent intent = new Intent();
                intent.putExtra("TYPE", 2);
                intent.putExtra("renameDoc", fileName);
                intent.putExtra("renameDocPos", position);
                setResult(RESULT_OK, intent);*/

                Intent intent = new Intent("RENAME");
                intent.putExtra("changeRenameName", fileName);
                intent.putExtra("changeRenamePath", filePath);
                intent.putExtra("changeRenamePos", position);
                EventBus.getDefault().post(intent);
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            window.getDecorView().setRight(20);
            window.getDecorView().setLeft(20);
            window.setGravity(Gravity.CENTER);
        }
        dialog.show();
    }

    private void openDeleteDialog(int itemPos) {
        Dialog dialog = new Dialog(this, R.style.DialogTheme);

        dialog.setContentView(R.layout.confirmation_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            //File dir = getFilesDir();
            File file = new File(filePath);
            file.delete();
            /*Intent intent = new Intent();
            intent.putExtra("TYPE", 1);
            intent.putExtra("deletedItem", itemPos);
            setResult(RESULT_OK, intent);*/

            Intent intent = new Intent("DELETE_DOC");
            intent.putExtra("path_doc", filePath);
            EventBus.getDefault().post(intent);

            dialog.dismiss();
            finish();
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            window.getDecorView().setRight(20);
            window.getDecorView().setLeft(20);
            window.setGravity(Gravity.CENTER);
        }
        dialog.show();
    }

    private void openDetailsDialog() {
        Dialog dialog = new Dialog(this, R.style.DialogTheme);

        dialog.setContentView(R.layout.details_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        TextView tvName = dialog.findViewById(R.id.tvName);
        TextView tvFilePath = dialog.findViewById(R.id.tvFilePath);
        TextView tvSize = dialog.findViewById(R.id.tvSize);
        TextView tvPermission = dialog.findViewById(R.id.tvPermission);
        TextView tvDate = dialog.findViewById(R.id.tvDate);
        Button buttonOk = dialog.findViewById(R.id.btnDetailOk);

        tvName.setText(fileName);
        tvFilePath.setText(filePath);
        tvSize.setText(fileSize);
        tvPermission.setText(getString(R.string.read_write));
        tvDate.setText(fileDate);

        buttonOk.setOnClickListener(v -> dialog.dismiss());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            window.getDecorView().setRight(20);
            window.getDecorView().setLeft(20);
            window.setGravity(Gravity.CENTER);
        }
        dialog.show();
    }

    private void saveBitmapToFile(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (tempFilePath == null) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                tempFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            File file = new File(tempFilePath + File.separatorChar + "tempPic");
            if (!file.exists()) {
                file.mkdir();
            }
            tempFilePath = file.getAbsolutePath();
        }

        File file = new File(tempFilePath + File.separatorChar + "export_image.jpg");
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

        } catch (IOException e) {
            System.out.println("error converting bitmap" + e.getLocalizedMessage());
        }
    }

    public void setButtonEnabled(boolean enabled) {
        if (fullscreen) {
            pageUp.setEnabled(enabled);
            pageDown.setEnabled(enabled);
            penButton.setEnabled(enabled);
            eraserButton.setEnabled(enabled);
            settingsButton.setEnabled(enabled);
        }
    }

    protected void onPause() {
        super.onPause();

        Object obj = control.getActionValue(EventConstant.PG_SLIDESHOW, null);
        if (obj != null && (Boolean) obj) {
            wm.removeView(pageUp);
            wm.removeView(pageDown);
            wm.removeView(penButton);
            wm.removeView(eraserButton);
            wm.removeView(settingsButton);
        }

        //Objects.requireNonNull(sliderNativeAd.getTimerHandlerAd()).removeCallbacks(Objects.requireNonNull(sliderNativeAd.getTimerRunnableAd()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Objects.requireNonNull(sliderNativeAd.getTimerHandlerAd()).removeCallbacks(Objects.requireNonNull(sliderNativeAd.getTimerRunnableAd()));
    }

    protected void onResume() {
        super.onResume();
        Object obj = control.getActionValue(EventConstant.PG_SLIDESHOW, null);
        if (obj != null && (Boolean) obj) {
            wmParams.gravity = Gravity.END | Gravity.TOP;
            wmParams.x = MainConstant.GAP;
            wm.addView(penButton, wmParams);

            wmParams.gravity = Gravity.END | Gravity.TOP;
            wmParams.x = MainConstant.GAP;
            wmParams.y = wmParams.height;
            wm.addView(eraserButton, wmParams);

            wmParams.gravity = Gravity.END | Gravity.TOP;
            wmParams.x = MainConstant.GAP;
            wmParams.y = wmParams.height * 2;
            wm.addView(settingsButton, wmParams);

            wmParams.gravity = Gravity.START | Gravity.CENTER;
            wmParams.x = MainConstant.GAP;
            wmParams.y = 0;
            wm.addView(pageUp, wmParams);

            wmParams.gravity = Gravity.END | Gravity.CENTER;
            wm.addView(pageDown, wmParams);
        }
        //sliderNativeAd.setAutoScrollRecyclerView();


    }

    public void onBackPressed() {
        if (isSearchbarActive()) {
            showSearchBar(false);
            updateToolsbarStatus();
        } else {
            Object obj = control.getActionValue(EventConstant.PG_SLIDESHOW, null);
            if (obj != null && (Boolean) obj) {
                fullScreen(false);
                //
                this.control.actionEvent(EventConstant.PG_SLIDESHOW_END, null);
            } else {
                if (control.getReader() != null) {
                    control.getReader().abortReader();
                }
                if (marked != dbService.queryItem(MainConstant.TABLE_STAR, filePath)) {
                    if (!marked) {
                        dbService.deleteItem(MainConstant.TABLE_STAR, filePath);
                    } else {
                        dbService.insertStarFiles(MainConstant.TABLE_STAR, filePath);
                    }

                    Intent intent = new Intent();
                    intent.putExtra(MainConstant.INTENT_FILED_MARK_STATUS, marked);
                    setResult(RESULT_OK, intent);
                }
                if (control != null && control.isAutoTest()) {
                    System.exit(0);
                } else {
                    super.onBackPressed();
                }
            }
        }
    }

    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (isSearchbarActive()) {
            searchBar.onConfigurationChanged(newConfig);
        }
    }

    protected void onDestroy() {
        dispose();
        super.onDestroy();
    }

    public void showProgressBar(boolean visible) {
        setProgressBarIndeterminateVisibility(visible);
    }

    private void init() {
        //
//        toast = Toast.makeText(getApplicationContext(), "", 0);
        //
        Intent intent = getIntent();
        dbService = new DBService(getApplicationContext());

        filePath = intent.getStringExtra(MainConstant.INTENT_FILED_FILE_PATH);

        // 文件关联打开文件
        if (filePath == null) {
            this.filePath = intent.getDataString();
            int index = getFilePath().indexOf(":");
            if (index > 0) {
                filePath = filePath.substring(index + 3);
            }
            filePath = Uri.decode(filePath);
        }

        // 显示打开文件名称
        int index = filePath.lastIndexOf(File.separator);
        if (index > 0) {

            setTitle(filePath.substring(index + 1));
        } else {
            setTitle(filePath);
        }

        boolean isSupport = FileKit.instance().isSupport(filePath);
        //写入本地数据库
        if (isSupport) {
            dbService.insertRecentFiles(MainConstant.TABLE_RECENT, filePath);
        }
        // create view
        createView();
        // open file
        control.openFile(filePath);
        // initialization marked
        initMarked();
    }

    public boolean isShowZoomingMsg() {
        return true;
    }

    public boolean isPopUpErrorDlg() {
        return true;
    }

    private void createView() {
        // word
        String file = filePath.toLowerCase();
        if (file.endsWith(MainConstant.FILE_TYPE_DOC) || file.endsWith(MainConstant.FILE_TYPE_DOCX) || file.endsWith(MainConstant.FILE_TYPE_TXT) || file.endsWith(MainConstant.FILE_TYPE_DOT) || file.endsWith(MainConstant.FILE_TYPE_DOTX) || file.endsWith(MainConstant.FILE_TYPE_DOTM)) {
            applicationType = MainConstant.APPLICATION_TYPE_WP;
            toolsbar = new WPToolsbar(getApplicationContext(), control);
            toolsbar.setVisibility(View.GONE);
        }
        // excel
        else if (file.endsWith(MainConstant.FILE_TYPE_XLS) || file.endsWith(MainConstant.FILE_TYPE_XLSX) || file.endsWith(MainConstant.FILE_TYPE_XLT) || file.endsWith(MainConstant.FILE_TYPE_XLTX) || file.endsWith(MainConstant.FILE_TYPE_XLTM) || file.endsWith(MainConstant.FILE_TYPE_XLSM)) {
            applicationType = MainConstant.APPLICATION_TYPE_SS;
            toolsbar = new SSToolsbar(getApplicationContext(), control);
            toolsbar.setVisibility(View.GONE);

        }
        // PowerPoint
        else if (file.endsWith(MainConstant.FILE_TYPE_PPT) || file.endsWith(MainConstant.FILE_TYPE_PPTX) || file.endsWith(MainConstant.FILE_TYPE_POT) || file.endsWith(MainConstant.FILE_TYPE_PPTM) || file.endsWith(MainConstant.FILE_TYPE_POTX) || file.endsWith(MainConstant.FILE_TYPE_POTM)) {
            applicationType = MainConstant.APPLICATION_TYPE_PPT;
            toolsbar = new PGToolsbar(getApplicationContext(), control);
            toolsbar.setVisibility(View.GONE);

        }
        // PDF document
        else if (file.endsWith(MainConstant.FILE_TYPE_PDF)) {
            applicationType = MainConstant.APPLICATION_TYPE_PDF;
            toolsbar = new PDFToolsbar(getApplicationContext(), control);
            toolsbar.setVisibility(View.GONE);

        } else {
            applicationType = MainConstant.APPLICATION_TYPE_WP;
            toolsbar = new WPToolsbar(getApplicationContext(), control);
            toolsbar.setVisibility(View.GONE);

        }
        // 添加tool bar
//        appFrame.addView(toolsbar);
    }

    private boolean isSearchbarActive() {
        if (appFrame == null || isDispose) {
            return false;
        }
        int count = appFrame.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = appFrame.getChildAt(i);
            if (v instanceof FindToolBar) {
                return v.getVisibility() == View.VISIBLE;
            }
        }
        return false;
    }

    public void showSearchBar(boolean show) {
        //show search bar
        if (show) {
            if (searchBar == null) {
                searchBar = new FindToolBar(this, control);
                appFrame.addView(searchBar, 0);
            }
            searchBar.setVisibility(View.VISIBLE);
            toolsbar.setVisibility(View.GONE);
        }
        // hide search bar
        else {
            if (searchBar != null) {
                searchBar.setVisibility(View.GONE);
            }
            toolsbar.setVisibility(View.VISIBLE);
        }
    }

    public void showCalloutToolsBar(boolean show) {
        //show callout bar
        if (show) {
            if (calloutBar == null) {
                calloutBar = new CalloutToolsbar(getApplicationContext(), control);
                appFrame.addView(calloutBar, 0);
            }
            calloutBar.setCheckState(EventConstant.APP_PEN_ID, AImageCheckButton.CHECK);
            calloutBar.setCheckState(EventConstant.APP_ERASER_ID, AImageCheckButton.UNCHECK);
            calloutBar.setVisibility(View.VISIBLE);
            toolsbar.setVisibility(View.GONE);
        }
        // hide callout bar
        else {
            if (calloutBar != null) {
                calloutBar.setVisibility(View.GONE);
            }
            toolsbar.setVisibility(View.VISIBLE);
        }
    }

    public void setPenUnChecked() {
        if (fullscreen) {
            penButton.setState(AImageCheckButton.UNCHECK);
            penButton.postInvalidate();
        } else {
            calloutBar.setCheckState(EventConstant.APP_PEN_ID, AImageCheckButton.UNCHECK);
            calloutBar.postInvalidate();
        }
    }

    public void setEraserUnChecked() {
        if (fullscreen) {
            eraserButton.setState(AImageCheckButton.UNCHECK);
            eraserButton.postInvalidate();
        } else {
            calloutBar.setCheckState(EventConstant.APP_ERASER_ID, AImageCheckButton.UNCHECK);
            calloutBar.postInvalidate();
        }
    }

    public void setFindBackForwardState(boolean state) {
        if (isSearchbarActive()) {
            searchBar.setEnabled(EventConstant.APP_FIND_BACKWARD, state);
            searchBar.setEnabled(EventConstant.APP_FIND_FORWARD, state);
        }
    }

    public void fileShare() {
        ArrayList<Uri> list = new ArrayList<>();

        File file = new File(filePath);
        list.add(Uri.fromFile(file));

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_STREAM, list);
        intent.setType("application/octet-stream");
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.sys_share_title)));
    }

    public void initMarked() {
        marked = dbService.queryItem(MainConstant.TABLE_STAR, filePath);
        if (marked) {
            toolsbar.setCheckState(EventConstant.FILE_MARK_STAR_ID, AImageCheckButton.CHECK);
        } else {
            toolsbar.setCheckState(EventConstant.FILE_MARK_STAR_ID, AImageCheckButton.UNCHECK);
        }
    }

    private void markFile() {
        marked = !marked;
    }

    public Dialog onCreateDialog(int id) {
        return control.getDialog(this, id);
    }

    public void updateToolsbarStatus() {
        if (appFrame == null || isDispose) {
            return;
        }
        int count = appFrame.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = appFrame.getChildAt(i);
            if (v instanceof AToolsbar) {
                ((AToolsbar) v).updateStatus();
            }
        }
    }

    public IControl getControl() {
        return this.control;
    }

    public int getApplicationType() {
        return this.applicationType;
    }

    public String getFilePath() {
        return filePath;
    }

    public Activity getActivity() {
        return this;
    }

    public boolean doActionEvent(int actionID, Object obj) {
        try {
            switch (actionID) {
                case EventConstant.SYS_RESET_TITLE_ID:
                    setTitle((String) obj);
                    break;

                case EventConstant.SYS_ONBACK_ID:
                    onBackPressed();
                    break;

                case EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS: //update toolsbar state
                    updateToolsbarStatus();
                    break;

                case EventConstant.SYS_HELP_ID: //show help net
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.sys_url_wxiwei)));
                    startActivity(intent);
                    break;

                case EventConstant.APP_FIND_ID: //show search bar
                    showSearchBar(true);
                    break;

                case EventConstant.APP_SHARE_ID: //share file
                    fileShare();
                    break;

                case EventConstant.FILE_MARK_STAR_ID: //mark
                    markFile();
                    break;

                case EventConstant.APP_FINDING:
                    String content = ((String) obj).trim();
                    if (content.length() > 0 && Objects.requireNonNull(control.getFind()).find(content)) {
                        setFindBackForwardState(true);
                    } else {
                        setFindBackForwardState(false);
                        Toast.makeText(this, getLocalString("DIALOG_FIND_NOT_FOUND"), Toast.LENGTH_SHORT).show();
                    }
                    break;

                case EventConstant.APP_FIND_BACKWARD:
                    if (!Objects.requireNonNull(control.getFind()).findBackward()) {
                        searchBar.setEnabled(EventConstant.APP_FIND_BACKWARD, false);
                        Toast.makeText(this, getLocalString("DIALOG_FIND_TO_BEGIN"), Toast.LENGTH_SHORT).show();
                    } else {
                        searchBar.setEnabled(EventConstant.APP_FIND_FORWARD, true);
                    }
                    break;

                case EventConstant.APP_FIND_FORWARD:
                    if (!Objects.requireNonNull(control.getFind()).findForward()) {
                        searchBar.setEnabled(EventConstant.APP_FIND_FORWARD, false);
                        Toast.makeText(this, getLocalString("DIALOG_FIND_TO_END"), Toast.LENGTH_SHORT).show();
                    } else {
                        searchBar.setEnabled(EventConstant.APP_FIND_BACKWARD, true);
                    }
                    break;

                case EventConstant.SS_CHANGE_SHEET:
                    bottomBar.setFocusSheetButton((Integer) obj);
                    break;

                case EventConstant.APP_DRAW_ID:
                    showCalloutToolsBar(true);
                    control.getSysKit().getCalloutManager().setDrawingMode(MainConstant.DRAWMODE_CALLOUTDRAW);
                    appFrame.post(() -> control.actionEvent(EventConstant.APP_INIT_CALLOUTVIEW_ID, null));

                    break;

                case EventConstant.APP_BACK_ID:
                    showCalloutToolsBar(false);
                    control.getSysKit().getCalloutManager().setDrawingMode(MainConstant.DRAWMODE_NORMAL);
                    break;

                case EventConstant.APP_PEN_ID:
                    if ((Boolean) obj) {
                        control.getSysKit().getCalloutManager().setDrawingMode(MainConstant.DRAWMODE_CALLOUTDRAW);
                        setEraserUnChecked();
                        appFrame.post(() -> control.actionEvent(EventConstant.APP_INIT_CALLOUTVIEW_ID, null));
                    } else {
                        control.getSysKit().getCalloutManager().setDrawingMode(MainConstant.DRAWMODE_NORMAL);
                    }
                    break;

                case EventConstant.APP_ERASER_ID:
                    if ((Boolean) obj) {
                        control.getSysKit().getCalloutManager().setDrawingMode(MainConstant.DRAWMODE_CALLOUTERASE);
                        setPenUnChecked();
                    } else {
                        control.getSysKit().getCalloutManager().setDrawingMode(MainConstant.DRAWMODE_NORMAL);
                    }
                    break;

                case EventConstant.APP_COLOR_ID:
                    ColorPickerDialog dlg = new ColorPickerDialog(this, control);
                    dlg.show();
                    dlg.setOnDismissListener(dialog -> setButtonEnabled(true));
                    setButtonEnabled(false);
                    break;
                default:
                    return false;
            }
        } catch (Exception e) {
            Objects.requireNonNull(control.getSysKit().getErrorKit()).writerLog(e);
        }
        return true;
    }

    public void openFileFinish() {
        gapView = new View(getApplicationContext());
        gapView.setBackgroundColor(Color.GRAY);
        appFrame.addView(gapView, new LayoutParams(LayoutParams.MATCH_PARENT, 1));
        View app = control.getView();
        appFrame.addView(app, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public int getBottomBarHeight() {
        if (bottomBar != null) {
            return bottomBar.getSheetbarHeight();
        }
        return 0;
    }

    public int getTopBarHeight() {
        return 0;
    }

    public boolean onEventMethod(View v, MotionEvent e1, MotionEvent e2, float xValue, float yValue, byte eventMethodType) {
        return false;
    }

    public void changePage() {
    }

    public String getAppName() {
        return getString(R.string.sys_name);
    }

    public boolean isDrawPageNumber() {
        return true;
    }

    public boolean isTouchZoom() {
        return true;
    }

    public byte getWordDefaultView() {
        return WPViewConstant.PAGE_ROOT;
        //return WPViewConstant.NORMAL_ROOT;
    }

    public boolean isZoomAfterLayoutForWord() {
        return true;
    }

    private void initFloatButton() {
        //icon width and height
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.file_slideshow_left, opts);

        //load page up button
        Resources res = getResources();
        pageUp = new AImageButton(this, control, res.getString(R.string.pg_slideshow_pageup), -1, -1, EventConstant.APP_PAGE_UP_ID);
        pageUp.setNormalBgResID(R.drawable.file_slideshow_left);
        pageUp.setPushBgResID(R.drawable.file_slideshow_left_push);
        pageUp.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));

        //load page down button
        pageDown = new AImageButton(this, control, res.getString(R.string.pg_slideshow_pagedown), -1, -1, EventConstant.APP_PAGE_DOWN_ID);
        pageDown.setNormalBgResID(R.drawable.file_slideshow_right);
        pageDown.setPushBgResID(R.drawable.file_slideshow_right_push);
        pageDown.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));

        BitmapFactory.decodeResource(getResources(), R.drawable.file_slideshow_pen_normal, opts);
        // load pen button
        penButton = new AImageCheckButton(this, control, res.getString(R.string.app_toolsbar_pen_check), res.getString(R.string.app_toolsbar_pen), R.drawable.file_slideshow_pen_check, R.drawable.file_slideshow_pen_normal, R.drawable.file_slideshow_pen_normal, EventConstant.APP_PEN_ID);
        penButton.setNormalBgResID(R.drawable.file_slideshow_pen_normal);
        penButton.setPushBgResID(R.drawable.file_slideshow_pen_push);
        penButton.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));

        // load eraser button
        eraserButton = new AImageCheckButton(this, control, res.getString(R.string.app_toolsbar_eraser_check), res.getString(R.string.app_toolsbar_eraser), R.drawable.file_slideshow_eraser_check, R.drawable.file_slideshow_eraser_normal, R.drawable.file_slideshow_eraser_normal, EventConstant.APP_ERASER_ID);
        eraserButton.setNormalBgResID(R.drawable.file_slideshow_eraser_normal);
        eraserButton.setPushBgResID(R.drawable.file_slideshow_eraser_push);
        eraserButton.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));

        // load settings button
        settingsButton = new AImageButton(this, control, res.getString(R.string.app_toolsbar_color), -1, -1, EventConstant.APP_COLOR_ID);
        settingsButton.setNormalBgResID(R.drawable.file_slideshow_settings_normal);
        settingsButton.setPushBgResID(R.drawable.file_slideshow_settings_push);
        settingsButton.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));

        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();

        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.width = opts.outWidth;
        wmParams.height = opts.outHeight;
    }

    /**
     * full screen, not show top tool bar
     */
    public void fullScreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        if (fullscreen) {
            if (wm == null || wmParams == null) {
                initFloatButton();
            }

            wmParams.gravity = Gravity.END | Gravity.TOP;
            wmParams.x = MainConstant.GAP;
            wm.addView(penButton, wmParams);

            wmParams.gravity = Gravity.END | Gravity.TOP;
            wmParams.x = MainConstant.GAP;
            wmParams.y = wmParams.height;
            wm.addView(eraserButton, wmParams);

            wmParams.gravity = Gravity.END | Gravity.TOP;
            wmParams.x = MainConstant.GAP;
            wmParams.y = wmParams.height * 2;
            wm.addView(settingsButton, wmParams);

            wmParams.gravity = Gravity.START | Gravity.CENTER;
            wmParams.x = MainConstant.GAP;
            wmParams.y = 0;
            wm.addView(pageUp, wmParams);

            wmParams.gravity = Gravity.END | Gravity.CENTER;
            wm.addView(pageDown, wmParams);

            //hide title and tool bar
            ((View) getWindow().findViewById(android.R.id.title).getParent()).setVisibility(View.GONE);
            //hide status bar
            toolsbar.setVisibility(View.GONE);
            //
            gapView.setVisibility(View.GONE);

            penButton.setState(AImageCheckButton.UNCHECK);
            eraserButton.setState(AImageCheckButton.UNCHECK);

            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(params);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            //landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            wm.removeView(pageUp);
            wm.removeView(pageDown);
            wm.removeView(penButton);
            wm.removeView(eraserButton);
            wm.removeView(settingsButton);
            //show title and tool bar
            ((View) getWindow().findViewById(android.R.id.title).getParent()).setVisibility(View.VISIBLE);
            toolsbar.setVisibility(View.VISIBLE);
            gapView.setVisibility(View.VISIBLE);

            //show status bar
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(params);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

    }

    public void changeZoom() {
    }

    public void error(int errorCode) {
    }

    public String getLocalString(String resName) {
        return ResKit.instance().getLocalString(resName);
    }

    @Override
    public boolean isShowPasswordDlg() {
        return true;
    }

    @Override
    public boolean isShowProgressBar() {
        return true;
    }

    @Override
    public boolean isShowFindDlg() {
        return true;
    }

    @Override
    public boolean isShowTXTEncodeDlg() {
        return true;
    }

    public String getTXTDefaultEncode() {
        return "GBK";
    }

    @Override
    public void completeLayout() {
    }

    @Override
    public boolean isChangePage() {
        return true;
    }

    public void setWriteLog(boolean saveLog) {
        this.writeLog = saveLog;
    }

    public boolean isWriteLog() {
        return writeLog;
    }

    public void setThumbnail(boolean isThumbnail) {
        this.isThumbnail = isThumbnail;
    }

    public Object getViewBackground() {
        return bg;
    }

    public void setIgnoreOriginalSize(boolean ignoreOriginalSize) {
    }

    public boolean isIgnoreOriginalSize() {
        return false;
    }

    public byte getPageListViewMovingPosition() {
        return IPageListViewListener.Moving_Horizontal;
    }

    public boolean isThumbnail() {
        return isThumbnail;
    }

    public void updateViewImages(List<Integer> viewList) {
    }

    public File getTemporaryDirectory() {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.
        File file = getExternalFilesDir(null);
        if (file != null) {
            return file;
        } else {
            return getFilesDir();
        }
    }

    public void dispose() {
        isDispose = true;
        if (control != null) {
            control.dispose();
            control = null;
        }
        toolsbar = null;
        searchBar = null;
        bottomBar = null;
        if (dbService != null) {
            dbService.dispose();
            dbService = null;
        }
        if (appFrame != null) {
            int count = appFrame.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = appFrame.getChildAt(i);
                if (v instanceof AToolsbar) {
                    ((AToolsbar) v).dispose();
                }
            }
            appFrame = null;
        }

        if (wm != null) {
            wm = null;
            wmParams = null;
            pageUp.dispose();
            pageDown.dispose();
            penButton.dispose();
            eraserButton.dispose();
            settingsButton.dispose();
            pageUp = null;
            pageDown = null;
            penButton = null;
            eraserButton = null;
            settingsButton = null;
        }
    }

    private boolean isDispose;
    private boolean marked;
    private int applicationType = -1;
    private String filePath;
    private MainControl control;
    private AppFrame appFrame;
    private AToolsbar toolsbar;
    private FindToolBar searchBar;
    private DBService dbService;
    private SheetBar bottomBar;
    private View gapView;

    private WindowManager wm = null;
    private WindowManager.LayoutParams wmParams = null;
    private AImageButton pageUp;
    private AImageButton pageDown;
    private AImageCheckButton penButton;
    private AImageCheckButton eraserButton;
    private AImageButton settingsButton;
    private boolean writeLog = true;
    private boolean isThumbnail;
    private final Object bg = Color.GRAY;
    private CalloutToolsbar calloutBar;
    private boolean fullscreen;
    private String tempFilePath;
}
