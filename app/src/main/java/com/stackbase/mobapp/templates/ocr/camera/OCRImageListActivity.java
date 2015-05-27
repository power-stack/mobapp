package com.stackbase.mobapp.templates.ocr.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.stackbase.mobapp.R;
import com.stackbase.mobapp.activity.FinishListener;
import com.stackbase.mobapp.templates.InfoTemplate;
import com.stackbase.mobapp.templates.InfoTemplateManager;
import com.stackbase.mobapp.templates.ocr.util.ImageCutter;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.utils.LanguageCodeHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class OCRImageListActivity extends Activity {
    private static final String TAG = OCRImageListActivity.class.getSimpleName();

    static final String[] CUBE_SUPPORTED_LANGUAGES = {};
    private static final String[] CUBE_REQUIRED_LANGUAGES = {};
    public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "chi_sim";
    /** Resource to use for data file downloads. */
    static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";

    /** Download filename for orientation and script detection (OSD) data. */
    static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";

    /** Destination filename for orientation and script detection (OSD) data. */
    static final String OSD_FILENAME_BASE = "osd.traineddata";

    private String tempImageFile;
    InfoTemplate ocrTpl;

    private View progressView;
    static TessBaseAPI baseApi;
    private boolean isEngineReady;
    private int pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
    private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
    private String sourceLanguageCodeOcr; // ISO 639-3 language code
    private String sourceLanguageReadable; // Language name, for example,
    // "English"
    // private String
    // sourceLanguageCodeTranslation; //
    // ISO 639-1 language code

    private ProgressDialog dialog; // for initOcr - language download & unzip
    private ProgressDialog indeterminateDialog; // also for initOcr - init OCR
    // engine

    static File storageDirectory;

    final String getRectOCRLanguage(String name){
        if (ocrTpl == null){
            return null;
        }
        String lang = ocrTpl.getRectLanguage(name);
        if (null == lang)
            lang = DEFAULT_SOURCE_LANGUAGE_CODE;
        return lang;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list_view);
        InfoTemplateManager itManager = InfoTemplateManager.getInstance(getApplication().getResources());
        String tplName = getIntent().getStringExtra(Constant.OCR_TEMPLATE);
        this.ocrTpl = itManager.getTemplate(tplName);
        tempImageFile = getIntent().getStringExtra(Constant.INTENT_KEY_PIC_FULLNAME);
        Log.i("OCR_CUTTER", "template name is " + tplName);
        Log.i("OCR_CUTTER", "image file is " + tempImageFile);
        Log.i("OCR_CUTTER", "tempate is " + this.ocrTpl);
        HashMap<String, Bitmap> ciMap = loadcutImage();
        String[] names = new String[ciMap.size()];
        Bitmap[] bms = new Bitmap[ciMap.size()];
        Iterator<String> it = ciMap.keySet().iterator();
        int i = 0;
        while (it.hasNext()){
            String name = it.next();
            Bitmap bm = ciMap.get(name);
            names[i] = name;
            bms[i] = bm;
            i++;
        }

        OCRImageList adapter = new OCRImageList(OCRImageListActivity.this, names, bms);

        ListView list = (ListView)findViewById(R.id.image_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.i("OCR_IMAGE_LIST", "" + view.getClass().getName());
                //Toast.makeText(this, "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();
                TextView txtTitle = (TextView) view.findViewById(R.id.text);

                ImageView imageView = (ImageView) view.findViewById(R.id.icon);

                baseApi.init(storageDirectory.getAbsolutePath(), getRectOCRLanguage((String)txtTitle.getText()));
                baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
                BitmapDrawable drawable = (BitmapDrawable)imageView.getDrawable();
                baseApi.setImage(drawable.getBitmap());
                String outputText = baseApi.getUTF8Text();
                outputText = outputText.replaceAll("T", "1");
                outputText = outputText.replaceAll("o", "0");
                String txt = txtTitle.getText() + ": " + outputText;
                Log.i(TAG, txt);
                Toast.makeText(parent.getContext(), txt, Toast.LENGTH_SHORT).show();
            }
        });
        storageDirectory = getStorageDirectory();
        Log.i(TAG, storageDirectory.getAbsolutePath());
        /*
        if (storageDirectory != null) {
            //initialize OCR engine
            initOcrEngine(storageDirectory, sourceLanguageCodeOcr,
                    sourceLanguageReadable);
        }
        */
        baseApi = new TessBaseAPI();
    }

    private HashMap<String, Bitmap> loadcutImage(){
        byte[] data = Helper.loadFile(tempImageFile);
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);
        Log.i("OCR_CUTTER", "template name is " + this.ocrTpl.getName());
        HashMap<String, Bitmap> ciMap = ImageCutter.cutImages(bm, this.ocrTpl);
        return ciMap;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_list_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Sets the necessary language code values for the given OCR language. */
    private boolean setSourceLanguage(String languageCode) {
        sourceLanguageCodeOcr = languageCode;
        // sourceLanguageCodeTranslation = LanguageCodeHelper
        // .mapLanguageCode(languageCode);
        sourceLanguageReadable = LanguageCodeHelper.getOcrLanguageName(this,
                languageCode);
        return true;
    }

    /**
     * Requests initialization of the OCR engine with the given parameters.
     *
     * @param storageRoot
     *            Path to location of the tessdata directory to use
     * @param languageCode
     *            Three-letter ISO 639-3 language code for OCR
     * @param languageName
     *            Name of the language for OCR, for example, "English"
     */
    private void initOcrEngine(File storageRoot, String languageCode,
                               String languageName) {
        isEngineReady = false;

        // Set up the dialog box for the thermometer-style download progress
        // indicator
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new ProgressDialog(this);

        // If we have a language that only runs using Cube, then set the
        // ocrEngineMode to Cube
        if (ocrEngineMode != TessBaseAPI.OEM_CUBE_ONLY) {
            for (String s : CUBE_REQUIRED_LANGUAGES) {
                if (s.equals(languageCode)) {
                    ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(this);
                    prefs.edit()
                            .putString(Constant.KEY_OCR_ENGINE_MODE,
                                    getOcrEngineModeName()).apply();
                }
            }
        }

        // If our language doesn't support Cube, then set the ocrEngineMode to
        // Tesseract
        if (ocrEngineMode != TessBaseAPI.OEM_TESSERACT_ONLY) {
            boolean cubeOk = false;
            for (String s : CUBE_SUPPORTED_LANGUAGES) {
                if (s.equals(languageCode)) {
                    cubeOk = true;
                }
            }
            if (!cubeOk) {
                ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(this);
                prefs.edit()
                        .putString(Constant.KEY_OCR_ENGINE_MODE,
                                getOcrEngineModeName()).commit();
            }
        }

        // Display the name of the OCR engine we're initializing in the
        // indeterminate progress dialog box
        indeterminateDialog = new ProgressDialog(this);
        // indeterminateDialog.setTitle("Please wait");
        String ocrEngineModeName = getOcrEngineModeName();
        // if (ocrEngineModeName.equals("Both")) {
        // indeterminateDialog
        // .setMessage("Initializing Cube and Tesseract OCR engines for "
        // + languageName + "...");
        // } else {
        // indeterminateDialog.setMessage("Initializing " + ocrEngineModeName
        // + " OCR engine for " + languageName + "...");
        // }
        if (ocrEngineModeName.equals("Both")) {
            indeterminateDialog.setMessage("正在初始化识别引擎...");
        } else {
            indeterminateDialog.setMessage("正在初始化识别引擎...");
        }
        indeterminateDialog.setCancelable(false);
        indeterminateDialog.show();


        // Disable continuous mode if we're using Cube. This will prevent bad
        // states for devices
        // with low memory that crash when running OCR with Cube, and prevent
        // unwanted delays.
        if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY
                || ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            prefs.edit().putBoolean(Constant.KEY_CONTINUOUS_PREVIEW,
                    false);
        }

        // Start AsyncTask to install language data and init OCR
        baseApi = new TessBaseAPI();
        new OcrInitAsyncTask(this, baseApi, dialog, indeterminateDialog,
                languageCode, languageName, ocrEngineMode).execute(storageRoot
                .toString());
    }

    /**
     * Returns a string that represents which OCR engine(s) are currently set to
     * be run.
     *
     * @return OCR engine mode
     */
    String getOcrEngineModeName() {
        String ocrEngineModeName = "";
        String[] ocrEngineModes = getResources().getStringArray(
                R.array.ocrenginemodes);
        if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_ONLY) {
            ocrEngineModeName = ocrEngineModes[0];
        } else if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY) {
            ocrEngineModeName = ocrEngineModes[1];
        } else if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
            ocrEngineModeName = ocrEngineModes[2];
        }
        return ocrEngineModeName;
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
            showErrorMessage("Error",
                    "Required external storage (such as an SD card) is unavailable.");
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
                showErrorMessage("Error",
                        "Required external storage (such as an SD card) is full or unavailable.");
            }

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            Log.e(TAG, "External storage is read-only");
            showErrorMessage(
                    "Error",
                    "Required external storage (such as an SD card) is unavailable for data storage.");
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            Log.e(TAG, "External storage is unavailable");
            showErrorMessage("Error",
                    "Required external storage (such as an SD card) is unavailable or corrupted.");
        }
        return null;
    }
    /**
     * Displays an error message dialog box to the user on the UI thread.
     *
     * @param title
     *            The title for the dialog box
     * @param message
     *            The error message to be displayed
     */
    void showErrorMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setOnCancelListener(new FinishListener(this))
                .setPositiveButton("Done", new FinishListener(this)).show();
    }
}
