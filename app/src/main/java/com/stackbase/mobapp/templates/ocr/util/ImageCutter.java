package com.stackbase.mobapp.templates.ocr.util;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.stackbase.mobapp.templates.InfoTemplate;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by bryan on 15/5/22.
 */
public class ImageCutter {

    public static HashMap<String, Bitmap> cutImages(Bitmap orgbm, InfoTemplate ocrInfo){
        Bitmap bm = orgbm;
        int tplwidth = ocrInfo.getWidth();
        int tplheight = ocrInfo.getHeight();
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
