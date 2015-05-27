package com.stackbase.mobapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.stackbase.mobapp.HomePage;
import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.LoginBean;
import com.stackbase.mobapp.objects.SIMCardInfo;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.RemoteAPI;

import java.io.IOException;

public class LoginActivity extends Activity {

    private EditText eUsr;
    private EditText ePWD;
    private Button btn;
    private CheckBox checkBox;
    private final static String TAG = LoginActivity.class.getName();
    private ProgressDialog progressDialog;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eUsr = (EditText) findViewById(R.id.usrName);
        ePWD = (EditText) findViewById(R.id.pwd);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        btn = (Button) findViewById(R.id.loginBtn);

        sp = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        eUsr.setText(sp.getString("userName", ""));
        ePWD.setText(sp.getString("pwd", ""));

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
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, HomePage.class);
                    startActivity(intent);
                    LoginActivity.this.finish();

                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage(getString(R.string.loading));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    String usrInfo = eUsr.getText().toString().trim();
                    String pwdInfo = ePWD.getText().toString().trim();
                    SIMCardInfo simInfo = new SIMCardInfo(LoginActivity.this);
                    if (!usrInfo.equals("") && !pwdInfo.equals("")) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("usrName", usrInfo);
                        editor.putString("pwd", pwdInfo);
                        editor.commit();

                        if (simInfo.getNativePhoneNumber() != 0) {
                        // Invoke API
                        RemoteAPI api = new RemoteAPI();
                        String tip = "";
                        try {
                            LoginBean bean = api.login(usrInfo, pwdInfo, simInfo);
                            tip = bean.getTip();
                            if (!bean.getRetCode()) {
                                Toast.makeText(getApplicationContext(), "Fail to login the server: " + tip,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Log.d(TAG, "Login successful!!!");
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                prefs.edit().putString(Constant.KEY_REMOTE_ACCESS_TOKEN, bean.getEncryptpwd()).apply();
//                                prefs.edit().putString(Constant.DEFAULT_MESSAGE_DIR, usrInfo).apply();
//                                prefs.edit().putString(Constant.DEFAULT_MESSAGE_DIR, pwdInfo).apply();

//                                Intent intent = new Intent();
//                                intent.setClass(LoginActivity.this, HomePage.class);
//                                startActivity(intent);
//                                LoginActivity.this.finish();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Login fail!", e);
                            Toast.makeText(getApplicationContext(), "Fail to login the server!!!",
                                    Toast.LENGTH_LONG).show();
                        }}
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Fail to get the phone number, please check your mobile!!!",
//                                    Toast.LENGTH_LONG).show();
//                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.require_username),
                                Toast.LENGTH_LONG).show();
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
            }
        });

    }

    public class LoginTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... args) {
            String usrInfo = eUsr.getText().toString();
            String pwdInfo = ePWD.getText().toString();
            SIMCardInfo siminfo = new SIMCardInfo(LoginActivity.this);
            if (!usrInfo.trim().equals("") && !pwdInfo.trim().equals("")) {
//                        if (siminfo.getNativePhoneNumber() != 0) {
                // Invoke API
                RemoteAPI api = new RemoteAPI();
                String tip = "";
                try {
                    LoginBean bean = api.login(usrInfo, pwdInfo, siminfo);
                    tip = bean.getTip();
                    if (!bean.getRetCode()) {
                        Toast.makeText(getApplicationContext(), "Fail to login the server: " + tip,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "Login successful!!!");
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        prefs.edit().putString(Constant.KEY_REMOTE_ACCESS_TOKEN, bean.getEncryptpwd()).apply();
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, HomePage.class);
                        startActivity(intent);
                        LoginActivity.this.finish();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Login fail!", e);
                    Toast.makeText(getApplicationContext(), "Fail to login the server!!!",
                            Toast.LENGTH_LONG).show();
                }
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Fail to get the phone number, please check your mobile!!!",
//                                    Toast.LENGTH_LONG).show();
//                        }
            }
            else {
                Toast.makeText(getApplicationContext(), getString(R.string.require_username),
                        Toast.LENGTH_LONG).show();
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            return null;
        }
    }

}


