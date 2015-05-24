package com.stackbase.mobapp.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public final class FileUtils {
    public final static String TAG = FileUtils.class.getSimpleName();
    public static boolean deleteDirectory(File directory) {
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

}
