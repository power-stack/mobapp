package com.stackbase.mobapp.templates.ocr.camera;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.utils.Helper;
/**
 * Created by bryan on 15/5/20.
 */
public class OCRCameraActivityHandler extends Handler implements Helper.ErrorCallback {
    private static final String TAG = OCRCameraActivityHandler.class.getSimpleName();
    //  private final DecodeThread decodeThread;
    private static State state;
    private final OCRCameraActivity activity;
    private final OCRCameraManager cameraManager;

    OCRCameraActivityHandler(OCRCameraActivity activity, OCRCameraManager cameraManager) {
        this.activity = activity;
        this.cameraManager = cameraManager;

        // Start ourselves capturing previews (and decoding if using continuous recognition mode).
        cameraManager.startPreview();

        state = State.SUCCESS;
    }

    @Override
    public void handleMessage(Message message) {

        switch (message.what) {
            case R.id.restart_preview:
                restartPreview();
                break;
        }
    }

    void stop() {
        // TODO See if this should be done by sending a quit message to decodeHandler as is done
        // below in quitSynchronously().

        Log.d(TAG, "Setting state to CONTINUOUS_PAUSED.");
        state = State.CONTINUOUS_PAUSED;

        // Freeze the view displayed to the user.
        cameraManager.stopPreview();
    }

    void resetState() {
        //Log.d(TAG, "in restart()");
        if (state == State.CONTINUOUS_PAUSED) {
            Log.d(TAG, "Setting state to CONTINUOUS");
            state = State.CONTINUOUS;
            restartOcrPreviewAndDecode();
        }
    }

    void quitSynchronously() {
        state = State.DONE;
        if (cameraManager != null) {
            cameraManager.stopPreview();
            cameraManager.closeDriver();
        }
    }

    /**
     * Start the preview, but don't try to OCR anything until the user presses the shutter button.
     */
    private void restartPreview() {
        // Display the shutter and torch buttons
        activity.resumeContinuousCapture();

        if (state == State.SUCCESS) {
            state = State.PREVIEW;

            // Draw the viewfinder
            // TODO: this is for OCR
            activity.drawViewfinder();
        }
    }

    /**
     * Send a decode request for realtime OCR mode
     */
    private void restartOcrPreviewAndDecode() {
        // Continue capturing camera frames
        cameraManager.startPreview();

    }

    @Override
    public void onErrorTaken(String title, String message) {
        Helper.showErrorMessage(activity, title, message, activity.getFinishListener(),
                activity.getFinishListener());
    }

    public void setCameraDisplayOrientation() {
        cameraManager.setDisplayOrientation(activity);
    }

    private enum State {
        PREVIEW,
        PREVIEW_PAUSED,
        CONTINUOUS,
        CONTINUOUS_PAUSED,
        SUCCESS,
        DONE
    }

}
