package com.stackbase.mobapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.Borrower;
import com.stackbase.mobapp.objects.BorrowerData;
import com.stackbase.mobapp.objects.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

abstract public class Helper {

    private static final String TAG = Helper.class.getSimpleName();

    private static final String BORROWER_FILE_NAME = "id.json";
    private static final String MESSAGE_FILE_EXTENSION = ".msg";

    /**
     * Displays an error message dialog box to the user on the UI thread.
     *
     * @param title   The title for the dialog box
     * @param message The error message to be displayed
     */
    public static void showErrorMessage(Context context, String title, String message,
                                        DialogInterface.OnClickListener cancelListener,
                                        DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context).setTitle(title)
                .setMessage(message);
        if (cancelListener != null) {
            dialog.setNegativeButton(context.getString(R.string.cancel), cancelListener);
        }
        if (positiveListener != null) {
            dialog.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        }
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Finds the proper location on the SD card where we can save files.
     */
    public static File getStorageDirectory(Context context, ErrorCallback callback) {
        //Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));

        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (RuntimeException e) {
            Log.e(TAG, "Is the SD card visible?", e);
            if (callback != null) {
                callback.onErrorTaken(context.getString(R.string.err_title), context.getString(R.string.sd_unavailable));
            }
        }

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            // We can read and write the media
            //    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
            // For Android 2.2 and above

            try {
                return context.getExternalFilesDir(Environment.MEDIA_MOUNTED);
            } catch (NullPointerException e) {
                // We get an error here if the SD card is visible, but full
                Log.e(TAG, "External storage is unavailable");
                if (callback != null) {
                    callback.onErrorTaken(context.getString(R.string.err_title),
                            context.getString(R.string.sd_no_space));
                }
            }

            //        } else {
            //          // For Android 2.1 and below, explicitly give the path as, for example,
            //          // "/mnt/sdcard/Android/data/edu.sfsu.cs.orange.ocr/files/"
            //          return new File(Environment.getExternalStorageDirectory().toString() + File.separator +
            //                  "Android" + File.separator + "data" + File.separator + getPackageName() +
            //                  File.separator + "files" + File.separator);
            //        }

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            Log.e(TAG, "External storage is read-only");
            if (callback != null) {
                callback.onErrorTaken(context.getString(R.string.err_title),
                        context.getString(R.string.sd_readonly));
            }
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // to know is we can neither read nor write
            Log.e(TAG, "External storage is unavailable");
            if (callback != null) {
                callback.onErrorTaken(context.getString(R.string.err_title),
                        context.getString(R.string.sd_damage));
            }
        }
        return null;
    }

    public static String getMD5String(String source) {
        String result = source;
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source.getBytes());
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            result = new String(str);

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Fail to generate md5 string", e);
        }
        return result;
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static boolean isValidMD5(String s) {
        return s.matches("[a-fA-F0-9]{32}");
    }

    public static String findMd5fromPath(File file) {
        String parent = file.getParent();
        String strs[] = parent.split(File.separator);
        String result = "com.stackbase.mobapp"; // default password
        for (int i = strs.length - 1; i >= 0; i--) {
            if (Helper.isValidMD5(strs[i])) {
                result = strs[i];
                break;
            }
        }
        return result;
    }

    public static byte[] generateKey(String password) throws Exception {
        byte[] keyStart = password.getBytes("UTF-8");

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(keyStart);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    public static byte[] encodeFile(byte[] key, byte[] fileData, boolean isPlainText) throws Exception {
        byte[] encrypted;
        if (!isPlainText) {
            encrypted = new byte[key.length + fileData.length + key.length];
            System.arraycopy(key, 0, encrypted, 0, key.length);
            System.arraycopy(fileData, 0, encrypted, key.length, fileData.length);
            System.arraycopy(key, 0, encrypted, key.length + fileData.length, key.length);
        } else {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            encrypted = cipher.doFinal(fileData);
        }
        return encrypted;
    }

    public static byte[] decodeFile(byte[] key, byte[] fileData, boolean isPlainText) throws Exception {
        byte[] decrypted;
        if (!isPlainText) {
            decrypted = new byte[fileData.length - key.length * 2];
            System.arraycopy(fileData, key.length, decrypted, 0, decrypted.length);
        } else {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            decrypted = cipher.doFinal(fileData);
        }
        return decrypted;
    }

    /**
     * save file and encode the content
     *
     * @param fileFullName file name include the path info
     * @param data         content for the file
     * @return
     */
    public static boolean saveFile(String fileFullName, byte[] data) {
        File file = new File(fileFullName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        boolean result = false;
        try {
            fos = new FileOutputStream(file);
            String key = Helper.findMd5fromPath(file);
            try {
//                byte[] encodedData = Helper.encodeFile(Helper.generateKey(key), data, isPlainText(fileFullName));
                byte[] encodedData = data;
                fos.write(encodedData);
            } catch (Exception ex) {
                Log.d(TAG, "Fail to encode data!!");
                fos.write(data);
            }
            result = true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Load file and decrypt the content
     *
     * @param fileFullName file name include the path info
     * @return
     */
    public static byte[] loadFile(String fileFullName) {
        File file = new File(fileFullName);
        String key = Helper.findMd5fromPath(file);
        byte[] result = null;
        try {
            byte[] data = Helper.readFile(file);
//            result = Helper.decodeFile(Helper.generateKey(key), data, isPlainText(fileFullName));
            result = data;
        } catch (Exception ex) {
            Log.d(TAG, "Fail to load File: " + ex.getMessage());
        }
        return result;
    }

    private static boolean isPlainText(String fileFullName) {
        boolean isPlainText = false;
        if (fileFullName.endsWith(".json") || fileFullName.endsWith(".gs")) {
            isPlainText = true;
        }
        return isPlainText;
    }

    public interface ErrorCallback {
        /**
         * Called when hint error
         *
         * @param title,   error title
         * @param message, error message
         */
        void onErrorTaken(String title, String message);
    }

    // The folder name is generated by idnumber + borrower name
    public static String getBorrowerSubFolder(Borrower borrower) {
        String subFolder = Helper.getMD5String(borrower.getName() + borrower.getId());
        return subFolder;
    }

    // The folder name is generated by idnumber + borrower name + the id of picture type
    public static String getBorrowDataSubFolder(Borrower borrower, BorrowerData data) {
        String subFolder = Helper.getMD5String(borrower.getName() + borrower.getId() + data.getDatumId());
        return subFolder;
    }

    public static boolean saveBorrower(Borrower borrower, String rootDir) {
        String idFile = rootDir + File.separator + getBorrowerSubFolder(borrower)
                + File.separator + BORROWER_FILE_NAME;
        borrower.setJsonFile(idFile);
        boolean result = false;
        try {
            result = Helper.saveFile(idFile, borrower.toJson().toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Log.d(TAG, "Fail to save id file " + ex.getMessage());
        }
        return result;
    }

    public static String getGPSFileName(String pictureFileName) {
        String gpsFileName = pictureFileName.substring(0, pictureFileName.length() - ".jpg".length()) + ".gs";
        return gpsFileName;
    }

    public static ArrayList<Borrower> loadBorrowersInfo(String rootDir) {
        ArrayList<Borrower> borrowers = new ArrayList<>();
        File brDir = new File(rootDir);
        if (brDir.isDirectory()) {
            for (File file : brDir.listFiles()) {
                if (isValidMD5(file.getName())) {
                    borrowers.add(new Borrower(file.getAbsolutePath() + File.separator
                            + BORROWER_FILE_NAME));
                }
            }
        }
        return borrowers;
    }

    // Delete all the borrower's files
    public static void deleteBorrower(String idJsonFile) {
        File file = new File(idJsonFile);
        FileUtils.deleteDirectory(file.getParentFile());
    }

    public static boolean checkSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static void mMakeTextToast(Activity activity, String str, boolean isLong) {
        if (isLong == true) {
            Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static boolean saveMessage(Message message, String rootDir) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = rootDir + File.separator + Constant.DEFAULT_MESSAGE_DIR + File.separator + timeStamp + MESSAGE_FILE_EXTENSION;
        message.setJsonFile(fileName);
        boolean result = false;
        try {
            result = Helper.saveFile(fileName, message.toJson().toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Log.e(TAG, "Fail to save message file", ex);
        }
        return result;
    }

    public static boolean hasNewMessages(String rootDir) {
        String messageDir = rootDir + File.separator + Constant.DEFAULT_MESSAGE_DIR;
        pullMessagesFromServer();
        File file = new File(messageDir);
        if (file.exists()) {
            File[] msgs = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.endsWith(MESSAGE_FILE_EXTENSION)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            for (File msgFile : msgs) {
                Message message = new Message(msgFile.getAbsolutePath());
                if (!message.isRead()) return true;
            }
        }
        return false;
    }

    public static List<Message> pullMessagesFromServer() {
        // TODO: pull messages from remote server and save to local
        return new ArrayList<>();
    }

    public static List<Message> getMessages(String rootDir) {
        String messageDir = rootDir + File.separator + Constant.DEFAULT_MESSAGE_DIR;
        pullMessagesFromServer();
        List<Message> messages = new ArrayList<>();
        File file = new File(messageDir);
        if (file.exists()) {
            for (File msgFile : file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.endsWith(MESSAGE_FILE_EXTENSION)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            })) {
                Message message = new Message(msgFile.getAbsolutePath());
                messages.add(message);
            }
        } else {
            file.mkdirs();
        }
        return messages;
    }

    public static int getOcrEngineMode(Activity activity) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(activity);
        String[] ocrEngineModes = activity.getResources().getStringArray(
                R.array.ocrenginemodes);
        int engineMode = 0;
        String ocrEngineModeName = prefs.getString(
                Constant.KEY_OCR_ENGINE_MODE, ocrEngineModes[0]);
        if (ocrEngineModeName.equals(ocrEngineModes[0])) {
            engineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
        } else if (ocrEngineModeName.equals(ocrEngineModes[1])) {
            engineMode = TessBaseAPI.OEM_CUBE_ONLY;
        } else if (ocrEngineModeName.equals(ocrEngineModes[2])) {
            engineMode = TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED;
        }
        return engineMode;
    }

}
