package com.stackbase.mobapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.stackbase.mobapp.activity.FinishListener;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Android key = 000000
 */
public class HomePage extends Activity implements Helper.ErrorCallback {

    private static final String TAG = HomePage.class.getSimpleName();
    private static boolean isFirstLaunch; // True if this is the first time the app is being run
    private ImageButton camera = null;
    private ImageButton manage = null;
    private ImageButton settings = null;
    private boolean isExit = false;
    private SharedPreferences prefs;
    private FinishListener finishListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        long m = 1432085916155l;
        Date resultdate = new Date(m);
        System.out.println(sdf.format(resultdate));

        super.onCreate(savedInstanceState);
        checkFirstLaunch();

        if (isFirstLaunch) {
            setDefaultPreferences();
        }

        if (!Helper.checkSDCard()) {
            DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };
            Helper.showErrorMessage(this, getString(R.string.err_title), getString(R.string.err_nosd),
                    null, alertListener);

        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);

        camera = (ImageButton) findViewById(R.id.cameraBtn);
        manage = (ImageButton) findViewById(R.id.manageBtn);
        settings = (ImageButton) findViewById(R.id.settingsBtn);

        camera.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(HomePage.this, CollectActivity.class);
                startActivity(intent);
                // HomePage.this.finish();
            }


        });

        manage.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(HomePage.this, ManageActivity.class);
                startActivity(intent);
                // HomePage.this.finish();
            }
        });

        settings.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(HomePage.this, SettingsActivity.class);
                startActivity(intent);
                // HomePage.this.finish();
            }
        });
    }

    protected void toast() {
        // TODO Auto-generated method stub

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByDoubleClick();
        }
        return false;
    }

    private void exitByDoubleClick() {
        Timer timer = null;
        if (isExit == false) {
            isExit = true; // Prepare to exit
            Toast.makeText(this, R.string.pressAgain, Toast.LENGTH_LONG).show();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    /**
     * We want the help screen to be shown automatically the first time a new version of the app is
     * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
     * it to a value stored as a preference.
     */
    private boolean checkFirstLaunch() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            int currentVersion = info.versionCode;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int lastVersion = prefs.getInt(Constant.KEY_HELP_VERSION_SHOWN, 0);
            if (lastVersion == 0) {
                isFirstLaunch = true;
            } else {
                isFirstLaunch = false;
            }
            if (currentVersion > lastVersion) {

                // Record the last version for which we last displayed the What's New (Help) page
                prefs.edit().putInt(Constant.KEY_HELP_VERSION_SHOWN, currentVersion).commit();
//                Intent intent = new Intent(this, HelpActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//
//                // Show the default page on a clean install, and the what's new page on an upgrade.
//                String page = lastVersion == 0 ? HelpActivity.DEFAULT_PAGE : HelpActivity.WHATS_NEW_PAGE;
//                intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
//                startActivity(intent);
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, e);
        }
        return false;
    }

    /**
     * Sets default values for preferences. To be called the first time this app is run.
     */
    private void setDefaultPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        File storage_dir_root_file = Helper.getStorageDirectory(this, this);
        String default_storage = Constant.DEFAULT_STORAGE_DIR;
        if (storage_dir_root_file != null) {
            default_storage = storage_dir_root_file.getAbsolutePath() + File.separator
                    + default_storage;
            File file = new File(default_storage);
            file.mkdirs();
        }
        // Set storage dir
        prefs.edit().putString(Constant.KEY_STORAGE_DIR, default_storage).apply();
        // Autofocus
        prefs.edit().putBoolean(Constant.KEY_AUTO_FOCUS, Constant.DEFAULT_TOGGLE_AUTO_FOCUS).apply();
        // Beep
        prefs.edit().putBoolean(Constant.KEY_PLAY_BEEP, Constant.DEFAULT_TOGGLE_BEEP).apply();
        // Light
        prefs.edit().putString(Constant.KEY_TOGGLE_LIGHT, Constant.DEFAULT_TOGGLE_LIGHT).apply();
        // OCR engine
        prefs.edit().putString(Constant.KEY_OCR_ENGINE_MODE, Constant.DEFAULT_OCR_ENGINE_MODE).apply();
        // Message notify
        prefs.edit().putBoolean(Constant.KEY_MESSAGE_NOTIFY, Constant.DEFAULT_MESSAGE_NOTIFY).apply();
        // Message vibrate
        prefs.edit().putBoolean(Constant.KEY_MESSAGE_VIBRATE, Constant.DEFAULT_MESSAGE_VIBRATE).apply();
    }

    @Override
    public void onErrorTaken(String title, String message) {
        Helper.showErrorMessage(this, title, message, finishListener,
                finishListener);

    }
}
