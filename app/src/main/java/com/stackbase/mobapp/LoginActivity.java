package com.stackbase.mobapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Handler;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.SIMCardInfo;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
   // Handler handler = new Handler();

    private EditText eUsr;
    private EditText ePWD;
    private Button btn;

    private long number;
    //private EditText phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eUsr = (EditText) findViewById(R.id.usrName);
        ePWD = (EditText) findViewById(R.id.pwd);
        btn = (Button) findViewById(R.id.loginBtn);

       // EditText ePhone = (EditText) findViewById(R.id.phone);

        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usrInfo = eUsr.getText().toString();
                String pwdInfo = ePWD.getText().toString();

                if (v == btn) {
                    SIMCardInfo siminfo = new SIMCardInfo(LoginActivity.this);
                    try {
                        number = siminfo.getNativePhoneNumber();
                    } catch (Exception e ) {
                        e.printStackTrace();
                    }
                    //privoid.setText(siminfo.getProvidersName());
                }

                if (!usrInfo.trim().equals("") && !pwdInfo.trim().equals("")) {
                    // Get phone serial strings
                    Build bd = new Build();
                    String model = bd.SERIAL;
                    if (number != 0) {
                        // Invoke API

                        Toast.makeText(getApplicationContext(), model + "       " + number, Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "请输入您的用户名和密码!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }



}


