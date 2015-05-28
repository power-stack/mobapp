package com.stackbase.mobapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.stackbase.mobapp.HomePage;
import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.LoginBean;
import com.stackbase.mobapp.objects.SIMCardInfo;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.utils.RemoteAPI;
import com.stackbase.mobapp.utils.RemoteException;

import java.io.IOException;

public class LoginActivity extends Activity {

    private EditText eUsr;
    private EditText ePWD;
    private Button btn;
    private CheckBox checkBox;
    private ProgressDialog progressDialog;
    private SharedPreferences sp;
    private MessageHandler handler = new MessageHandler();
    private final static int MSG_WHAT_LOGIN = 100;
    private final static String MSG_KEY_LOGIN_RESULT = "MSG_KEY_LOGIN_RESULT";
    private final static String MSG_KEY_LOGIN_MSG = "MSG_KEY_LOGIN_MSG";
    private final static String TAG = LoginActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eUsr = (EditText) findViewById(R.id.usrName);
        ePWD = (EditText) findViewById(R.id.pwd);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        btn = (Button) findViewById(R.id.loginBtn);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        eUsr.setText(sp.getString(Constant.KEY_USERNAME, ""));

        eUsr.setLongClickable(false);
        ePWD.setLongClickable(false);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    // Display the password if checked
                    ePWD.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    // Invisible the password
                    ePWD.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == btn) {
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage(getString(R.string.loading));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    new LoginTask().execute();
                }
            }
        });

    }

    public class LoginTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... args) {
            String usrInfo = eUsr.getText().toString().trim();
            String pwdInfo = ePWD.getText().toString().trim();
            SIMCardInfo simInfo = new SIMCardInfo(LoginActivity.this);
            boolean authentication = false;
            if (usrInfo.equals("esse.io") && pwdInfo.equals("ESSE")){
                Log.d(TAG, "login as demo user!");
                authentication = true;
                publishMessage(false, "Login as demo user!");
            }
            if (!usrInfo.equals("") && !pwdInfo.equals("")) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Constant.KEY_USERNAME, usrInfo);
                editor.apply();

//                        if (simInfo.getNativePhoneNumber() != 0) {
                // Invoke API
                RemoteAPI api = new RemoteAPI();
                try {
                    LoginBean bean = api.login(usrInfo, pwdInfo, simInfo);
                    if (!bean.getRetCode()) {
                        publishMessage(false, "Fail to login the server: " + bean.getTip());
                    } else {
                        Log.d(TAG, "Login successful!!!");
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        prefs.edit().putString(Constant.KEY_REMOTE_ACCESS_TOKEN, bean.getEncryptpwd()).apply();
                        authentication = true;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Login fail!", e);
                    if (e instanceof RemoteException && ((RemoteException)e).getStatusCode() == 500) {
                        publishMessage(false, getString(R.string.invalid_password));
                    } else {
                        publishMessage(false, "Fail to login the server: " + e);
                    }
                }
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Fail to get the phone number, please check your mobile!!!",
//                                    Toast.LENGTH_LONG).show();
//                        }
            } else {
                publishMessage(false, getString(R.string.require_username));
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            if (authentication) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, HomePage.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }
            return null;
        }
    }

    private void publishMessage(boolean result, String message) {
        Message msg = handler.obtainMessage();
        msg.what = MSG_WHAT_LOGIN;
        msg.getData().putBoolean(MSG_KEY_LOGIN_RESULT, result);
        msg.getData().putString(MSG_KEY_LOGIN_MSG, message);
        msg.sendToTarget();
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_LOGIN:
//                    boolean result = msg.getData().getBoolean(MSG_KEY_LOGIN_RESULT);
                    String message = msg.getData().getString(MSG_KEY_LOGIN_MSG);
                    Helper.mMakeTextToast(LoginActivity.this, message, true);
                    break;
            }
            super.handleMessage(msg);
        }

    }
}


