package com.stackbase.mobapp.templates.activities;

import android.os.Bundle;
import android.app.Application;

import com.stackbase.mobapp.ocr.OCRActivity;
import com.stackbase.mobapp.ocr.camera.CameraManager;
import com.stackbase.mobapp.templates.InfoTemplate;
import com.stackbase.mobapp.templates.InfoTemplateManager;
import com.stackbase.mobapp.utils.Constant;

/**
 * Created by bryan on 15/3/4.
 */
public class IDCardOCRActivity extends OCRActivity {
    private static final String TAG = OCRActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        InfoTemplateManager itManager = InfoTemplateManager.getInstance(getApplication().getResources());
        String tplName = getIntent().getStringExtra(Constant.OCR_TEMPLATE);
        InfoTemplate icFrontTpl = itManager.getTemplate(tplName);
        CameraManager cm = this.getCameraManager();
        cm.setOcrInfo(icFrontTpl);
    }
}
