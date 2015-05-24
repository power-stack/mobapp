package com.stackbase.mobapp.templates.ocr.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.activity.FinishListener;
import com.stackbase.mobapp.camera.BeepManager;
import com.stackbase.mobapp.ocr.OCRActivity;
import com.stackbase.mobapp.templates.InfoTemplate;
import com.stackbase.mobapp.templates.InfoTemplateManager;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.FallbackLocationTracker;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.utils.LocationTracker;
import com.stackbase.mobapp.view.ShutterButton;

import java.io.File;
import java.io.IOException;

/**
 * Created by bryan on 15/3/4.
 */
public class OCRCameraActivity extends Activity implements
        SurfaceHolder.Callback, ShutterButton.OnShutterButtonListener, Camera.PictureCallback {

    private static final String TAG = OCRActivity.class.getSimpleName();

    private OCRCameraManager cameraManager;
    private OCRCameraActivityHandler handler;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private OCRCameraPreviewView cpView;
    private View cameraButtonView;
    private boolean hasSurface;
    private BeepManager beepManager;
    private ShutterButton shutterButton;
    private SharedPreferences prefs;
    private boolean isPaused;
    private FinishListener finishListener;
    private static LocationTracker locationTracker;

    public FinishListener getFinishListener() {
        return finishListener;
    }

    public SharedPreferences getSharedPreferences() {
        return prefs;
    }

    public static LocationTracker getLocationTracker() {
        return locationTracker;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ocr_camera_preview);

        cpView = (OCRCameraPreviewView) findViewById(R.id.ocr_camera_preview_view);
        cameraButtonView = findViewById(R.id.camera_button_view);

        handler = null;
        hasSurface = false;
        beepManager = new BeepManager(this);

        // Camera shutter button
        shutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        shutterButton.setOnShutterButtonListener(this);
        finishListener = new FinishListener(this);

        InfoTemplateManager itManager = InfoTemplateManager.getInstance(getApplication().getResources());
        String tplName = getIntent().getStringExtra(Constant.OCR_TEMPLATE);
        InfoTemplate ocrTpl = itManager.getTemplate(tplName);
        cameraManager = new OCRCameraManager(getApplication(), ocrTpl);
        cpView.setCameraManager(cameraManager);
        cpView.setInfoTemplate(ocrTpl);
        locationTracker = new FallbackLocationTracker(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetStatusView();
        retrievePreferences();

        // Set up the camera preview surface.
        surfaceView = (SurfaceView) findViewById(R.id.tpl_ocr_capture_preview_view);
        surfaceHolder = surfaceView.getHolder();
        if (!hasSurface) {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        }
        locationTracker.start();

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        locationTracker.stop();
        super.onDestroy();
    }

    /**
     * Gets values from shared preferences and sets the corresponding data members in this activity.
     */
    private void retrievePreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.camera_preferences, false);

        beepManager.updatePrefs();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");

        if (holder == null) {
            Log.e(TAG, "surfaceCreated gave us a null surface");
        }

        // Only initialize the camera if the OCR engine is ready to go.
        if (!hasSurface) { // && isEngineReady
            Log.d(TAG, "surfaceCreated(): calling initCamera()...");
            initCamera(holder);
        }
        hasSurface = true;
    }

    /**
     * Initializes the camera and starts the handler to begin previewing.
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "initCamera()");
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        try {

            // Open and initialize the camera
            cameraManager.openDriver(surfaceHolder);

            // Creating the handler starts the preview, which can also throw a RuntimeException.
            handler = new OCRCameraActivityHandler(this, cameraManager);

        } catch (IOException ioe) {
            Log.e(TAG, "Fail to open camera driver", ioe);
            Helper.showErrorMessage(this, "错误", "不能打开照相机设备, 请重启您的手机或检查权限设置.",
                    null, finishListener);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fail to open camera driver", e);
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Helper.showErrorMessage(this, "错误", "不能打开照相机设备, 请重启您的手机或检查权限设置.",
                    null, finishListener);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (handler != null) {
            handler.setCameraDisplayOrientation();
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
        }

        // Stop using the camera, to avoid conflicting with other camera-based
        // apps
        cameraManager.closeDriver();

        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // First check if we're paused in continuous mode, and if so, just unpause.
            if (isPaused) {
                Log.d(TAG, "only resuming continuous recognition, not quitting...");
                resumeContinuousCapture();
                return true;
            }

            // Exit the app if we're not viewing an OCR result.
            setResult(RESULT_CANCELED);
//            releaseBitmap();
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            onShutterButtonPress();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            // Only perform autofocus if user is not holding down the button.
            if (event.getRepeatCount() == 0) {
                cameraManager.requestAutoFocus(500L);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void resumeContinuousCapture() {
        isPaused = false;
        resetStatusView();
        if (handler != null) {
            handler.resetState();
        }
    }

    /**
     * Resets view elements.
     */
    private void resetStatusView() {
        cameraButtonView.setVisibility(View.VISIBLE);
        shutterButton.setVisibility(View.VISIBLE);
    }

    /**
     * Request the viewfinder to be invalidated.
     */
    void drawViewfinder() {
//     viewfinderView.drawViewfinder();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_capture, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings: {
                intent = new Intent().setClass(this, OCRPreferencesActivity.class);
                intent.putExtra(Constant.INTENT_KEY_PREFERENCES_TYPE, this.getClass().getSimpleName());
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onShutterButtonClick(ShutterButton b) {
        onShutterButtonPress();
    }

    @Override
    public void onShutterButtonFocus(ShutterButton b, boolean pressed) {
        requestDelayedAutoFocus();
    }

    /**
     * Requests autofocus after a 350 ms delay. This delay prevents requesting focus when the user
     * just wants to click the shutter button without focusing. Quick button press/release will
     * trigger onShutterButtonClick() before the focus kicks in.
     */
    private void requestDelayedAutoFocus() {
        // Wait 350 ms before focusing to avoid interfering with quick button presses when
        // the user just wants to take a picture without focusing.
        cameraManager.requestAutoFocus(350L);
    }

    /**
     * Called when the shutter button is pressed in continuous mode.
     */
    void onShutterButtonPress() {
        Log.d(TAG, "onShutterButtonPress");
        String torch = prefs.getString(Constant.KEY_TOGGLE_LIGHT, Constant.DEFAULT_TOGGLE_LIGHT);
        cameraManager.setTorch(torch);

//        handler.stop();
        isPaused = true;
        cameraManager.takePicture(this);
        beepManager.playBeepSoundAndVibrate();

        shutterButton.setVisibility(View.GONE);

        Log.d(TAG, "onShutterButtonPress finished");
    }

    private String saveTempImage(byte[] data) {
        String image = null;
        String tempFile = getExternalCacheDir().getAbsolutePath() + File.separator + "stackbase.jpg";
        if (Helper.saveFile(tempFile, data)) {
            image = tempFile;
        }
        return image;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "In onPictureTaken");
        String tplName = this.getIntent().getStringExtra(Constant.OCR_TEMPLATE);
        if (data != null) {
            Intent intent = new Intent();
            intent.setClass(OCRCameraActivity.this, OCRPictureConfirmActivity.class);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, saveTempImage(data));
            intent.putExtra(Constant.INTENT_KEY_PIC_FOLDER,
                    getIntent().getStringExtra(Constant.INTENT_KEY_PIC_FOLDER));
            intent.putExtra(Constant.OCR_TEMPLATE, tplName);
            startActivityForResult(intent, 0);
            handler.stop();
        } else {
            Log.d(TAG, "Did not get the data when take picture!");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                String fileName = data.getStringExtra(Constant.INTENT_KEY_PIC_FULLNAME);
                Intent intent = new Intent();
                intent.putExtra(Constant.INTENT_KEY_PIC_FULLNAME, fileName);
                this.setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                resumeContinuousCapture();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
