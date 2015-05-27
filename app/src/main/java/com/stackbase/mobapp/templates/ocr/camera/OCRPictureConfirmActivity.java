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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.stackbase.mobapp.CollectActivity;
import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.Borrower;
import com.stackbase.mobapp.objects.GPSLocation;
import com.stackbase.mobapp.templates.InfoTemplate;
import com.stackbase.mobapp.templates.InfoTemplateManager;
import com.stackbase.mobapp.templates.ocr.util.ImageCutter;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class OCRPictureConfirmActivity extends Activity implements View.OnClickListener {

    private static final String TAG = OCRPictureConfirmActivity.class.getSimpleName();
    public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "chi_sim";
    private TextView savePictureTextView;
    private TextView recaptureTextView;
    private ImageView pictureConfirmImageView;
    private String tempImageFile;
    InfoTemplate ocrTpl;
    private String borrowerJsonPath;
    Activity active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_ocr_picture_confirm);
        savePictureTextView = (TextView) findViewById(R.id.savePictureTextView);
        recaptureTextView = (TextView) findViewById(R.id.recaptureTextView);
        pictureConfirmImageView = (ImageView) findViewById(R.id.pictureConfirmImageView);
        savePictureTextView.setOnClickListener(this);
        recaptureTextView.setOnClickListener(this);

        InfoTemplateManager itManager = InfoTemplateManager.getInstance(getApplication().getResources());
        String tplName = getIntent().getStringExtra(Constant.OCR_TEMPLATE);
        ocrTpl = itManager.getTemplate(tplName);
        borrowerJsonPath = this.getIntent().getStringExtra(Constant.INTENT_KEY_ID_JSON_FILENAME);
        Log.i("JSON path :::::::::: ", borrowerJsonPath);
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
            Bitmap bm1 = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);
            Log.i("ocr_bm", "" + bm1.getWidth() + "," + bm1.getHeight());

            if (bm1.getWidth() < this.ocrTpl.getWidth() && bm1.getHeight() < this.ocrTpl.getHeight()) {
                Bitmap scaled = Bitmap.createScaledBitmap(bm1, screenHeight, screenWidth, true);
                bm1.recycle();
                bm1 = scaled;
            }
            int topOffset = 0;
            int leftOffset = 0;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                topOffset = (screenWidth - this.ocrTpl.getHeight()) / 2;
                leftOffset = (screenHeight - this.ocrTpl.getWidth()) / 2;
            } else {
                topOffset = (screenHeight - this.ocrTpl.getHeight()) / 2;
                leftOffset = (screenWidth - this.ocrTpl.getWidth()) / 2;
            }
            Bitmap bm;
            Bitmap bm2 = Bitmap.createBitmap(bm1, leftOffset, topOffset, ocrTpl.getWidth(), ocrTpl.getHeight());
            bm1.recycle();
            bm1 = null;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Notice that width and height are reversed
                Bitmap scaled = Bitmap.createScaledBitmap(bm2, screenHeight, screenWidth, true);
                int w = scaled.getWidth();
                int h = scaled.getHeight();
                // Setting post rotate to 90
                Matrix mtx = new Matrix();
                mtx.postRotate(90);
                // Rotating Bitmap
                bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
            } else {// LANDSCAPE MODE
                //No need to reverse width and height
                Bitmap scaled = Bitmap.createScaledBitmap(bm2, screenWidth, screenHeight, true);
                bm = scaled;
            }
            bm2.recycle();
            bm2 = null;
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
                String fileName1 = savePictureFromView();
                Intent intent1 = new Intent();
                intent1.putExtra(Constant.OCR_TEMPLATE, ocrTpl.getID());
                intent1.setClass(this, OCRImageListActivity.class);
                intent1.putExtra(Constant.INTENT_KEY_PIC_FULLNAME, fileName1);
                startActivity(intent1);
                break;
            case R.id.savePictureTextView:
                new OCRTextTask().execute();

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

    public class OCRTextTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... args) {
            HashMap<String, Bitmap> ciMap = loadcutImage();
            String[] names = new String[ciMap.size()];
            Bitmap[] bms = new Bitmap[ciMap.size()];
            Iterator<String> it = ciMap.keySet().iterator();
            File storageDirectory = getStorageDirectory();
            Borrower borrower = new Borrower(borrowerJsonPath);
            while (it.hasNext()) {
                String name = it.next();
                Bitmap bm = ciMap.get(name);
                TessBaseAPI baseApi = new TessBaseAPI();
                baseApi.init(storageDirectory.getAbsolutePath(), getRectOCRLanguage(name));
                baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
                baseApi.setImage(bm);
                String outputText = baseApi.getUTF8Text();
                outputText = outputText.replaceAll("T", "1");
                outputText = outputText.replaceAll("o", "0");

                setBorrowersName(name, borrower, outputText);

            }
            ((CollectActivity) active).saveBorrowerInfo(borrower);
            return null;
        }

        private void setBorrowersName(String name, Borrower borrower, String outputText) {

            if (name.equals("name"))
                borrower.setName(outputText);
            else if (name.equals("gender"))
                borrower.setGender(outputText);
            else if (name.equals("nationality"))
                borrower.setNation(outputText);
            else if (name.equals("birthday")) {
                Date date = str2Date(outputText);
                borrower.setBirthday(date);
            } else if (name.equals("address1") && name.equals("address2") && name.equals("address3")) {
                outputText += outputText;
                borrower.setAddress(outputText);
            } else if (name.equals("id_number"))
                borrower.setId(outputText);
            else if (name.equals("issued"))
                borrower.setLocation(outputText);
            else if (name.equals("period")) {
                String[] strs = outputText.split("-");
                Date date1 = str2DateDot(strs[0]);
                Date date2 = str2DateDot(strs[1]);
                borrower.setExpiryFrom(date1);
                borrower.setExpiryTo(date2);
            }


        }

        private Date str2Date(String str) {
            Date date = null;
            String[] b = {"", "", ""};
            int j = 0;
            for (int i = 0; i < str.length(); i++) {
                char a = str.charAt(i);
                if (a >= '0' && a <= '9') {
                    b[j] += a;
                } else {
                    j++;
                }
            }
            String dateStr = b[0] + "-" + b[1] + "-" + b[2];
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
                date = sdf.parse(dateStr);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            return date;
        }

        private Date str2DateDot(String str) {
            Date date = null;

            String dateStr = str.replaceAll(".", "-");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
                date = sdf.parse(dateStr);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            return date;
        }
    }

    private HashMap<String, Bitmap> loadcutImage() {
        byte[] data = Helper.loadFile(tempImageFile);
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);
        Log.i("OCR_CUTTER", "template name is " + ocrTpl.getName());
        HashMap<String, Bitmap> ciMap = ImageCutter.cutImages(bm, ocrTpl);
        return ciMap;
    }

    final String getRectOCRLanguage(String name) {
        if (ocrTpl == null) {
            return null;
        }
        String lang = ocrTpl.getRectLanguage(name);
        if (null == lang)
            lang = DEFAULT_SOURCE_LANGUAGE_CODE;
        return lang;
    }

        /** Finds the proper location on the SD card where we can save files. */

    private File getStorageDirectory() {
        // Log.d(TAG, "getStorageDirectory(): API level is " +
        // Integer.valueOf(android.os.Build.VERSION.SDK_INT));

        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (RuntimeException e) {
            Log.e(TAG, "Is the SD card visible?", e);
        }

        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {

            // We can read and write the media
            // if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
            // For Android 2.2 and above

            try {
                return getExternalFilesDir(Environment.MEDIA_MOUNTED);
            } catch (NullPointerException e) {
                // We get an error here if the SD card is visible, but full
                Log.e(TAG, "External storage is unavailable");
            }

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            Log.e(TAG, "External storage is read-only");
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            Log.e(TAG, "External storage is unavailable");
        }
        return null;
    }

}
