package com.stackbase.mobapp.templates.ocr.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by bryan on 15/5/20.
 */
public final class OCRPreviewCallback implements Camera.PreviewCallback {
    private static final String TAG = OCRPreviewCallback.class.getSimpleName();

    private final OCRCameraConfigurationManager configManager;
    private Handler previewHandler;
    private int previewMessage;

    public OCRPreviewCallback(OCRCameraConfigurationManager configManager) {
        this.configManager = configManager;
    }

    public void setHandler(Handler previewHandler, int previewMessage) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
    }

    // Since we're not calling setPreviewFormat(int), the data arrives here in the YCbCr_420_SP
    // (NV21) format.
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = configManager.getCameraResolution();
        Handler thePreviewHandler = previewHandler;
        if (cameraResolution != null && thePreviewHandler != null) {
            Message message = thePreviewHandler.obtainMessage(previewMessage, cameraResolution.x,
                    cameraResolution.y, data);
            message.sendToTarget();
            previewHandler = null;
        } else {
            Log.d(TAG, "Got preview callback, but no handler or resolution available");
        }
    }

}
