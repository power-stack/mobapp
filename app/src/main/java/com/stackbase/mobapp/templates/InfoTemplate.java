
package com.stackbase.mobapp.templates;

import android.graphics.Rect;

import com.stackbase.mobapp.ocr.OCRActivity;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class InfoTemplate{

    private static final String TAG = InfoTemplate.class.getSimpleName();

    private HashMap<String, Rect> _ocrRectMap = null;

    private String _name = null;

    private String _id = null;

    private String _desc = null;

    private String _data_folder = null;

    private int _width = 0;

    private int _height = 0;

    private Class _ocr_activity_class = OCRActivity.class;

    private HashMap<String, String> _langMap = new HashMap<>();

    private HashMap<String, String[]> _valueMap = new HashMap<>();

    private String[] _values = null;

    public InfoTemplate(){
        _ocrRectMap = new HashMap<String, Rect>();
    }

    public void load(JSONObject jObj){
        if (jObj.containsKey("name")){
            _name = (String)jObj.get("name");
        }
        if (jObj.containsKey("description")){
            _desc = (String)jObj.get("description");
        }

        if (jObj.containsKey("data_folder")){
            _data_folder = (String)jObj.get("data_folder");
        }
        if (jObj.containsKey("width")){
            _width = ((Long)jObj.get("width")).intValue();
        }
        if (jObj.containsKey("height")){
            _height = ((Long)jObj.get("height")).intValue();
        }
        if (jObj.containsKey("ocr-activity") && null != jObj.get("ocr-activity")){
            String classname = (String)jObj.get("ocr-activity");
            try {
                _ocr_activity_class = Class.forName(classname);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (jObj.containsKey("ocr-rects")){
            JSONObject ocrRects = (JSONObject)jObj.get("ocr-rects");
            if (ocrRects != null){
                Iterator it = ocrRects.keySet().iterator();
                while(it.hasNext()) {
                    String rectName = (String) it.next();
                    JSONObject rectJson = (JSONObject)ocrRects.get(rectName);
                    /*
                     * left   The X coordinate of the left side of the rectangle
                     * top    The Y coordinate of the top of the rectangle
                     * right  The X coordinate of the right side of the rectangle
                     * bottom The Y coordinate of the bottom of the rectangle
                     */
                    if (rectJson.containsKey("language")) {
                        _langMap.put(rectName, (String)rectJson.get("language"));
                    }
                    if (rectJson.containsKey("values")){
                       // String[] values = (String[]) rectJson.get("values");
                        //_valueMap.put(rectName, values);
                    }
                    Long left = (Long)rectJson.get("left");
                    Long right = (Long)rectJson.get("right");
                    Long top = (Long)rectJson.get("top");
                    Long bottom = (Long)rectJson.get("bottom");
                    if (null == left) left = new Long(0);
                    if (null == right) right = new Long(0);
                    if (null == top) top = new Long(0);
                    if (null == bottom) bottom = new Long(0);
                    Rect rect = new Rect(left.intValue(), top.intValue(),
                            right.intValue(), bottom.intValue());
                    _ocrRectMap.put(rectName, rect);
                }
            }
        }
    }

    public static InfoTemplate fromJSONObject(JSONObject jObj){
        InfoTemplate itpl = new InfoTemplate();
        itpl.load(jObj);
        return itpl;
    }

    public Class getOcrActivityClass(){
        return _ocr_activity_class;
    }

    public HashMap<String, Rect> getRectsMap(){
        return _ocrRectMap;
    }

    public Rect getRect(String rectName){
        return _ocrRectMap.get(rectName);
    }

    public String getName(){ return _name; }

    public String getDescription() { return _desc; }

    public int getWidth() { return _width; }

    public int getHeight() { return _height; }

    public String getID() {return _id;}

    public void setID(String id) { this._id = id; }

    public String getRectLanguage(String rectName) {
        return _langMap.get(rectName);
    }

    public String[] getRectValues(String rectName) {
        return _valueMap.get(rectName);
    }

    public String getDataFolder(){
        return _data_folder;
    }
}