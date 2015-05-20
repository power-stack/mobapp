package com.stackbase.mobapp.templates;

/**
 * Created by bryan on 15/2/28.
 */

import android.content.res.Resources;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.JSONObj;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class InfoTemplateManager {

    private static final String TAG = InfoTemplateManager.class.getSimpleName();

    static InfoTemplateManager _instance = null;

    private HashMap<String, InfoTemplate> _itMap = null;

    private InfoTemplateManager(Resources res){
        _itMap = new HashMap<String, InfoTemplate>();
        try {
            _load(res);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static InfoTemplateManager getInstance(Resources res){
        if (_instance == null){
            _instance = new InfoTemplateManager(res);
        }
        return _instance;
    }

    private void _load(Resources res) throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        BufferedReader bfReader = null;
        InputStream is = res.openRawResource(R.raw.templates);
        try {
            bfReader = new BufferedReader(new InputStreamReader(is));
            Object obj = parser.parse(bfReader);
            JSONObject jsonObject = (JSONObject) obj;
            Iterator it = jsonObject.keySet().iterator();
            while(it.hasNext()){
                String tid = (String)it.next();
                JSONObject jobj = (JSONObject)jsonObject.get(tid);
                InfoTemplate itpl = InfoTemplate.fromJSONObject(jobj);
                _itMap.put(tid, itpl);
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (ParseException e){
            e.printStackTrace();
        }
        try {
            is.close();
        }catch (IOException e){

        }
    }

    public InfoTemplate getTemplate(String templateId){
        return _itMap.get(templateId);
    }

    public Collection<InfoTemplate> getAll(){
        return _itMap.values();
    }
}
