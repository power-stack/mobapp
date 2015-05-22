package com.stackbase.mobapp.objects;

import android.util.Log;

import com.stackbase.mobapp.utils.Helper;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract public class JSONObj implements Serializable {
    private static final String TAG = JSONObj.class.getSimpleName();
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                String name = getFieldName(method, "get");
                if (name != null) {
                    try {
                        jsonObject.put(name, method.invoke(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            } else if (method.getName().startsWith("is") && method.getReturnType() == Boolean.class) {
                String name = getFieldName(method, "is");
                if (name != null) {
                    try {
                        jsonObject.put(name, method.invoke(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return jsonObject;
    }

    private String getFieldName(Method method, String methodPrefix) {
        try {
            int length = methodPrefix.length();
            String fieldName = method.getName().substring(length, length + 1).toLowerCase()
                    + method.getName().substring(length + 1);
            Field field = this.getClass().getDeclaredField(fieldName);
            return field.getName();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }

    }
    public void fromJSON(String jsonFile) {
        JSONParser parser = new JSONParser();
        BufferedReader bfReader = null;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(Helper.loadFile(jsonFile));
            bfReader = new BufferedReader(new InputStreamReader(is));

            Object obj = parser.parse(bfReader);
            JSONObject jsonObject = (JSONObject) obj;
            Method[] methods = this.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("set")) {
                    try {
                        String fieldName = method.getName().substring(3, 4).toLowerCase()
                                + method.getName().substring(4);
                        Object value = jsonObject.get(fieldName);
                        Field field = this.getClass().getDeclaredField(fieldName);
                        if (value != null && field.getType() == value.getClass()) {
                            Log.d(TAG, "Find field in json file: " + fieldName + "--" + value);
                            method.invoke(this, value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void fromJSONStr(String jsonStr) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonStr);
            JSONObject jsonObject = (JSONObject) obj;
            Method[] methods = this.getClass().getDeclaredMethods();
            for (Method method: methods) {
                if (method.getName().startsWith("set")) {
                    try {
                        String fieldName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
                        Object value = jsonObject.get(fieldName);
                        Field field = this.getClass().getDeclaredField(fieldName);
                        if (value != null && field.getType() == value.getClass()) {
                            method.invoke(this, value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();;
        }
    }
    
    @Override
    public String toString() {
        return this.toJson().toJSONString();
    }

}
