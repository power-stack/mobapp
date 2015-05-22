package com.stackbase.mobapp.templates.ocr.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.GPSLocation;
import com.stackbase.mobapp.templates.InfoTemplate;
import com.stackbase.mobapp.templates.InfoTemplateManager;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OCRPictureConfirmActivity extends Activity implements View.OnClickListener {

    private static final String TAG = OCRPictureConfirmActivity.class.getSimpleName();
    private TextView savePictureTextView;
    private TextView recaptureTextView;
    private ImageView pictureConfirmImageView;
    private String tempImageFile;
    InfoTemplate ocrTpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_picture_confirm);
        savePictureTextView = (TextView) findViewById(R.id.savePictureTextView);
        recaptureTextView = (TextView) findViewById(R.id.recaptureTextView);
        pictureConfirmImageView = (ImageView) findViewById(R.id.pictureConfirmImageView);
        savePictureTextView.setOnClickListener(this);
        recaptureTextView.setOnClickListener(this);

        InfoTemplateManager itManager = InfoTemplateManager.getInstance(getApplication().getResources());
        String tplName = getIntent().getStringExtra(Constant.OCR_TEMPLATE);
        ocrTpl = itManager.getTemplate(tplName);
        initImageView();
    }

    private void initImageView() {
        tempImageFile = getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT);
        if (tempImageFile != null) {
            byte[] data = Helper.loadFile(tempImageFile);
            WindowManager manager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();
            //int screenWidth = getResources().getDisplayMetrics().widthPixels;
            //int screenHeight = getResources().getDisplayMetrics().heightPixels;
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);
            Log.i("ocr_bm", "" + bm.getWidth() + "," + bm.getHeight());
            int topOffset = 0;
            int leftOffset = 0;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                topOffset = (screenWidth - this.ocrTpl.getHeight()) / 2;
                leftOffset = (screenHeight - this.ocrTpl.getWidth()) / 2;
            }else{
                topOffset = (screenHeight - this.ocrTpl.getHeight()) / 2;
                leftOffset = (screenWidth - this.ocrTpl.getWidth()) / 2;
            }
            bm = Bitmap.createBitmap(bm, leftOffset, topOffset, ocrTpl.getWidth(), ocrTpl.getHeight());
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Notice that width and height are reversed
                Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);
                int w = scaled.getWidth();
                int h = scaled.getHeight();
                // Setting post rotate to 90
                Matrix mtx = new Matrix();
                mtx.postRotate(90);
                // Rotating Bitmap
                bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
            } else {// LANDSCAPE MODE
                //No need to reverse width and height
                Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth, screenHeight, true);
                bm = scaled;
            }

            pictureConfirmImageView.setImageBitmap(bm);

        }

    }

    private String savePictureFromView() {
        String fileName = "";
        if (pictureConfirmImageView == null) {
            pictureConfirmImageView = (ImageView) findViewById(R.id.pictureConfirmImageView);
        }
        BitmapDrawable drawable = (BitmapDrawable) pictureConfirmImageView.getDrawable();
        if (drawable != null && drawable.getBitmap() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            drawable.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions!!");
            } else {
                Helper.saveFile(pictureFile.getAbsolutePath(), byteArray);
                fileName = pictureFile.getAbsolutePath();
            }
            releaseBitmap();
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "Fail to close stream.", e);
            }

            Location location = OCRCameraActivity.getLocationTracker().getLocation();
            Log.d(TAG, "location: " + location);
            if (location == null) {
                //TODO: show this message in the message center.
                Helper.mMakeTextToast(this, getString(R.string.err_gps_location), true);
            } else {
                GPSLocation gpsObj = new GPSLocation(location);
                String gpsFileName = Helper.getGPSFileName(fileName);
                try {
                    Helper.saveFile(gpsFileName, gpsObj.toJson().toString().getBytes("UTF-8"));
                } catch (UnsupportedEncodingException ue) {
                    Log.e(TAG, "Fail to save GPS location", ue);
                }
            }
        }
        return fileName;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recaptureTextView:
                break;
            case R.id.savePictureTextView:
                String fileName = savePictureFromView();
                Intent intent = new Intent();
                intent.putExtra(Constant.INTENT_KEY_PIC_FULLNAME, fileName);
                this.setResult(Activity.RESULT_OK, intent);
                break;
        }
        releaseBitmap();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (tempImageFile != null && !tempImageFile.equals("")) {
            File file = new File(tempImageFile);
            file.delete();
        }
        super.onDestroy();
    }

    private File getOutputMediaFile() {
        //get the mobile Pictures directory
        String storage_dir = getIntent().getStringExtra(Constant.INTENT_KEY_PIC_FOLDER);
        if (storage_dir == null || storage_dir.equals("")) {
            storage_dir = PreferenceManager.getDefaultSharedPreferences(this).getString(Constant.KEY_STORAGE_DIR, "");
        }

        File picDir = new File(storage_dir);
        //get the current time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(picDir.getAbsolutePath() + File.separator + "IMAGE_" + timeStamp + ".jpg");
    }


    private void releaseBitmap() {
        if (pictureConfirmImageView != null) {
            // release the memory
            BitmapDrawable drawable = (BitmapDrawable) pictureConfirmImageView.getDrawable();
            if (drawable != null && drawable.getBitmap() != null) {
                drawable.getBitmap().recycle();
                pictureConfirmImageView = null;
            }
        }
    }

}
