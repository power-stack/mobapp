package com.stackbase.mobapp.templates.ocr.camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.ocr.OcrResultText;
import com.stackbase.mobapp.templates.InfoTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the result text.
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
public final class OCRCameraPreviewView extends View {
    //private static final long ANIMATION_DELAY = 80L;

    /**
     * Flag to draw boxes representing the results from TessBaseAPI::GetRegions().
     */
    public static final boolean DRAW_REGION_BOXES = false;

    /**
     * Flag to draw boxes representing the results from TessBaseAPI::GetTextlines().
     */
    public static final boolean DRAW_TEXTLINE_BOXES = true;

    /**
     * Flag to draw boxes representing the results from TessBaseAPI::GetStrips().
     */
    public static final boolean DRAW_STRIP_BOXES = false;

    /**
     * Flag to draw boxes representing the results from TessBaseAPI::GetWords().
     */
    static final boolean DRAW_WORD_BOXES = true;

    /**
     * Flag to draw word text with a background varying from transparent to opaque.
     */
    static final boolean DRAW_TRANSPARENT_WORD_BACKGROUNDS = false;

    /**
     * Flag to draw boxes representing the results from TessBaseAPI::GetCharacters().
     */
    static final boolean DRAW_CHARACTER_BOXES = false;

    /**
     * Flag to draw the text of words within their respective boxes from TessBaseAPI::GetWords().
     */
    static final boolean DRAW_WORD_TEXT = false;

    /**
     * Flag to draw each character in its respective box from TessBaseAPI::GetCharacters().
     */
    static final boolean DRAW_CHARACTER_TEXT = false;

    private OCRCameraManager cameraManager;
    private final Paint paint;
    private final int maskColor;
    private final int frameColor;
    private final int cornerColor;
    private OcrResultText resultText;
    private String[] words;
    private List<Rect> regionBoundingBoxes;
    private List<Rect> textlineBoundingBoxes;
    private List<Rect> stripBoundingBoxes;
    private List<Rect> wordBoundingBoxes;
    private List<Rect> characterBoundingBoxes;
    private InfoTemplate ocrTpl;

    //  Rect bounds;
    private Rect previewFrame;
    private Rect rect;

    // This constructor is used when the class is built from an XML resource.
    public OCRCameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        frameColor = resources.getColor(R.color.viewfinder_frame);
        cornerColor = resources.getColor(R.color.viewfinder_corners);

        //    bounds = new Rect();
        previewFrame = new Rect();
        rect = new Rect();
    }

    public void setCameraManager(OCRCameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    public void setInfoTemplate(InfoTemplate infoTpl){
        this.ocrTpl = infoTpl;
    }

    @SuppressWarnings("unused")
    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = cameraManager.getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        // Draw a two pixel solid border inside the framing rect
        paint.setAlpha(0);
        paint.setStyle(Style.FILL);
        paint.setColor(frameColor);
        canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
        canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
        canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
        canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);

        // Draw the framing rect corner UI elements
        paint.setColor(cornerColor);
        canvas.drawRect(frame.left - 15, frame.top - 15, frame.left + 15, frame.top, paint);
        canvas.drawRect(frame.left - 15, frame.top, frame.left, frame.top + 15, paint);
        canvas.drawRect(frame.right - 15, frame.top - 15, frame.right + 15, frame.top, paint);
        canvas.drawRect(frame.right, frame.top - 15, frame.right + 15, frame.top + 15, paint);
        canvas.drawRect(frame.left - 15, frame.bottom, frame.left + 15, frame.bottom + 15, paint);
        canvas.drawRect(frame.left - 15, frame.bottom - 15, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right - 15, frame.bottom, frame.right + 15, frame.bottom + 15, paint);
        canvas.drawRect(frame.right, frame.bottom - 15, frame.right + 15, frame.bottom + 15, paint);

        // Draw the rects defined in the templates
        Log.i("OCR", "" + frame.left + "," + frame.top + "," + frame.right + "," + frame.bottom + "," + frame.width() + "," + frame.height());
        paint.setColor(maskColor);
        HashMap<String, Rect> rectMap = this.ocrTpl.getRectsMap();
        Iterator<String> iterator = rectMap.keySet().iterator();
        while(iterator.hasNext()){
            String rectName = iterator.next();
            Rect r = rectMap.get(rectName);
            int[] ltrb = {frame.left + r.left, frame.top + r.top,
                    frame.left + r.right,
                    frame.top + r.bottom};
            Log.i("OCR", "Drawing rect " + rectName + " for template " + ocrTpl.getName() + "\n" +
                    ltrb[0] + "," + ltrb[1] + "," + ltrb[2] + "," + ltrb[3]);
            canvas.drawRect(ltrb[0],ltrb[1],ltrb[2],ltrb[3], paint);
        }

    }

    public void drawViewfinder() {
        invalidate();
    }

    /**
     * Adds the given OCR results for drawing to the view.
     *
     * @param text Object containing OCR-derived text and corresponding data.
     */
    public void addResultText(OcrResultText text) {
        resultText = text;
    }

    /**
     * Nullifies OCR text to remove it at the next onDraw() drawing.
     */
    public void removeResultText() {
        resultText = null;
    }
}
