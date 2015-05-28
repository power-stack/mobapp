package com.stackbase.mobapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.stackbase.mobapp.objects.LoginBean;
import com.stackbase.mobapp.objects.SIMCardInfo;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.utils.RemoteAPI;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SettingsActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private SharedPreferences prefs;
    private CheckBox messageNotify;
    private CheckBox messageVibrate;
    private TextView spaceUsage;
    private TextView account;
    private TextView testConnect;
    private ImageView testConnectView;
    private ProgressDialog progressDialog;

    private static final int MSG_WHAT_CALCULATE_USAGE = 1;
    private static final String MSG_KEY_TOTAL_SPACE = "MSG_KEY_TOTAL_SPACE";
    private static final String TAG = SettingsActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings);

        initView();
	}

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CalculateUsage task = new CalculateUsage(new MessageHandler());
        new Thread(task).start();
    }

    private void initView() {
        retrievePreferences();

        account = (TextView) findViewById(R.id.account_id);
        account.setText(prefs.getString(Constant.KEY_USERNAME, "default"));
        testConnectView = (ImageView) findViewById(R.id.connection_testing_view);
        testConnect = (TextView) findViewById(R.id.connection_testing);
        messageNotify = (CheckBox) findViewById(R.id.notify_me);
        messageNotify.setChecked(prefs.getBoolean(Constant.KEY_MESSAGE_NOTIFY,
                Constant.DEFAULT_MESSAGE_NOTIFY));
        messageVibrate = (CheckBox) findViewById(R.id.vibrate);
        messageVibrate.setChecked(prefs.getBoolean(Constant.KEY_MESSAGE_VIBRATE,
                Constant.DEFAULT_MESSAGE_VIBRATE));

        messageNotify.setOnCheckedChangeListener(this);
        messageVibrate.setOnCheckedChangeListener(this);

        testConnect.setOnClickListener(this);
        testConnectView.setOnClickListener(this);
        spaceUsage = (TextView) findViewById(R.id.spaceUsage);
    }

    private void retrievePreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.camera_preferences, false);
    }

    private void savePreferences() {
        prefs.edit().putBoolean(Constant.KEY_MESSAGE_NOTIFY, messageNotify.isChecked()).apply();
        prefs.edit().putBoolean(Constant.KEY_MESSAGE_VIBRATE, messageVibrate.isChecked()).apply();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (messageNotify == buttonView) {
            if (!isChecked) {
                messageVibrate.setChecked(false);
            }
        } else if (messageVibrate == buttonView) {
            if (isChecked) {
                messageNotify.setChecked(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connection_testing || v.getId() == R.id.connection_testing_view) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.login_view);
            dialog.setTitle(getString(R.string.settings));

            final EditText urlText = (EditText) dialog.findViewById(R.id.server_url);
            urlText.setSingleLine(true);

            final EditText userText = (EditText) dialog.findViewById(R.id.user_name);
            userText.setSingleLine(true);
            userText.setLongClickable(false);

            final EditText passText = (EditText) dialog.findViewById(R.id.password);
            passText.setSingleLine(true);
            passText.setLongClickable(false);
            passText.setTransformationMethod(PasswordTransformationMethod.getInstance());

            CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.maskCheckBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        // Display the password if checked
                        passText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    else
                        // Invisible the password
                        passText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            });

            final String originUrl = prefs.getString(Constant.KEY_REMOTE_API_ENDPOINT, Constant.DEFAULT_API_ENDPOINT);
            urlText.setText(originUrl);
            final String originUser = prefs.getString(Constant.KEY_USERNAME, "");
            userText.setText(originUser);
            final Button cancelTestBtn = (Button) dialog.findViewById(R.id.cancelTestBtn);
            cancelTestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Canceled.
                    dialog.dismiss();
                }
            });

            Button testConnectBtn = (Button) dialog.findViewById(R.id.testConnectBtn);
            testConnectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelTestBtn.setEnabled(false);
                    String url = urlText.getText().toString().trim();
                    String user = userText.getText().toString().trim();
                    String password = passText.getText().toString().trim();
                    if (!url.equals("") && !user.equals("") && !user.equals("")) {
                        progressDialog = new ProgressDialog(SettingsActivity.this);
                        progressDialog.setMessage(getString(R.string.loading));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        ValidTask task = new ValidTask();
                        task.execute(url, user, password);
                        boolean result = false;
                        try {
                            result = task.get();
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(TAG, "Fail to get result.", e);
                        }
                        if (result) {
                            Helper.mMakeTextToast(SettingsActivity.this, getString(R.string.joinable), true);
                            String newUrl = urlText.getText().toString().trim();
                            if (!originUrl.equals(newUrl)) {
                                prefs.edit().putString(Constant.KEY_REMOTE_API_ENDPOINT, newUrl).apply();
                            }
                            String newUser = userText.getText().toString().trim();
                            if (!originUser.equals(newUser)) {
                                prefs.edit().putString(Constant.KEY_USERNAME, newUser).apply();
                            }
                        } else {
                            Helper.mMakeTextToast(SettingsActivity.this, getString(R.string.unjoinable), true);
                        }
                    } else {
                        Helper.mMakeTextToast(SettingsActivity.this, getString(R.string.require_username), true);
                    }
                    cancelTestBtn.setEnabled(true);
                }
            });

            dialog.show();
        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_CALCULATE_USAGE:
                    float space = msg.getData().getFloat(MSG_KEY_TOTAL_SPACE);
                    if (space == -1) {
                        spaceUsage.setText(getString(R.string.calculate_usage));
                    } else {
                        Log.d(TAG, "space in handler: " + space);
                        spaceUsage.setText(space + " MB");
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }


    private class CalculateUsage implements Runnable {
        MessageHandler handler;

        public CalculateUsage(MessageHandler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            publishProgress(-1);
            String storageDir = prefs.getString(Constant.KEY_STORAGE_DIR, Constant.DEFAULT_STORAGE_DIR);
            Log.d(TAG, "storageDir: " + storageDir);
            File dir = new File(storageDir);
            long total = totalSize(dir);
            publishProgress(total/1024/1024);
        }

        private void publishProgress(float space) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_WHAT_CALCULATE_USAGE;
            msg.getData().putFloat(MSG_KEY_TOTAL_SPACE, (Math.round(space*100))/100);
            msg.sendToTarget();
        }

        private long totalSize(File directory) {
            long length = 0;
            for (File file : directory.listFiles()) {
                if (file.isFile())
                    length += file.length();
                else
                    length += totalSize(file);
            }
            return length;
        }
    }
    public class ValidTask extends AsyncTask<String, Integer, Boolean> {
        protected Boolean doInBackground(String... params) {
            try {
                SIMCardInfo simInfo = new SIMCardInfo(SettingsActivity.this);
                RemoteAPI api = new RemoteAPI();
                LoginBean bean = api.login(params[0], params[1], params[2], simInfo);
                if (!bean.getRetCode()) {
                    Log.d(TAG, "Login fail!!!" + bean.getTip());
                    return false;
                } else {
                    Log.d(TAG, "Login successful!!!");
                    prefs.edit().putString(Constant.KEY_REMOTE_ACCESS_TOKEN, bean.getEncryptpwd()).apply();
                    return true;
                }
            } catch (IOException e) {
                Log.e(TAG, "Fail to connect to " + params[0], e);
                return false;
            } finally {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        }
    }
}
