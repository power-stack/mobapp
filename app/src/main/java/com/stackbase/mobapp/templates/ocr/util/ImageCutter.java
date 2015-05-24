package com.stackbase.mobapp.templates.ocr.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import com.stackbase.mobapp.templates.InfoTemplate;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by bryan on 15/5/22.
 */
public class ImageCutter {

    public static HashMap<String, Bitmap> cutImages(Bitmap orgbm, InfoTemplate ocrInfo){
        Bitmap bm = orgbm;
        Log.i("IMG_CUTTER", "ocr template name is " + ocrInfo.getName());
        int tplwidth = ocrInfo.getWidth();
        int tplheight = ocrInfo.getHeight();
        if ((tplwidth > tplheight) && bm.getWidth() < bm.getHeight()) {
            Bitmap scaled = bm;
            Matrix mtx = new Matrix();
            mtx.postRotate(270);
            // Rotating Bitmap
            bm = Bitmap.createBitmap(scaled, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);

        }else if ((tplwidth < tplheight) && bm.getWidth() > bm.getHeight()) {
            Bitmap scaled = bm;
            Matrix mtx = new Matrix();
            mtx.postRotate(90);
            // Rotating Bitmap
            bm = Bitmap.createBitmap(scaled, 0, 0, bm.getWidth(), bm.getHeight(), mtx, true);
        }
        if ((orgbm.getWidth() != tplwidth) && (orgbm.getHeight() != tplheight)){
            bm = Bitmap.createScaledBitmap(bm, tplwidth, tplheight, true);
        }
        HashMap<String, Bitmap> res = new HashMap<String, Bitmap>();
        HashMap<String, Rect> rmap = ocrInfo.getRectsMap();
        Iterator<String> it = rmap.keySet().iterator();
        while (it.hasNext()){
            String name = it.next();
            Rect r = rmap.get(name);
            Bitmap rb = Bitmap.createBitmap(bm, r.left, r.top, r.right - r.left, r.bottom - r.top);
            res.put(name, rb);
        }
        return res;
    }
}
