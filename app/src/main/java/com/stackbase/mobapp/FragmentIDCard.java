package com.stackbase.mobapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.stackbase.mobapp.objects.Borrower;
import com.stackbase.mobapp.ocr.OCRActivity;
import com.stackbase.mobapp.utils.Constant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentIDCard extends Fragment {
    private static final String TAG = FragmentIDCard.class.getSimpleName();
    View content;
    ImageView frantIDPic;
    Activity active;
    private EditText nameEdit;
    private EditText idEdit;
    private RadioButton maleButton;
    private RadioButton femaleButton;
    private EditText minzuEdit;
    private EditText dobEdit;
    private EditText addrEdit;
    private EditText locationEdit;
    private EditText expiryFromEdit;
    private EditText expiryToEdit;
    private SharedPreferences prefs;
    private boolean isFromManage = false;
    private Borrower borrower;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        content = inflater.inflate(R.layout.fragment_idcard, container, false);
        idEdit = (EditText) content.findViewById(R.id.idEdit);
        nameEdit = (EditText) content.findViewById(R.id.nameEdit);
        maleButton = (RadioButton) content.findViewById(R.id.maleButton);
        femaleButton = (RadioButton) content.findViewById(R.id.femaleButton);
        minzuEdit = (EditText) content.findViewById(R.id.minzuEdit);
        dobEdit = (EditText) content.findViewById(R.id.dobEdit);
        addrEdit = (EditText) content.findViewById(R.id.addrEdit);
        locationEdit = (EditText) content.findViewById(R.id.locationEdit);
        expiryFromEdit = (EditText) content.findViewById(R.id.expiryFromEdit);
        expiryToEdit = (EditText) content.findViewById(R.id.expiryToEdit);

        initView();

        return content;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public boolean isFromManage() {
        return isFromManage;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    private void initView() {
        Log.d(TAG, "initView");

        active = this.getActivity();
        frantIDPic = (ImageView) content.findViewById(R.id.frontView);

        ImageButton.OnClickListener clickListener = new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(active, OCRActivity.class);
                startActivity(intent);
            }

        };

        frantIDPic.setOnClickListener(clickListener);

        idEdit.setEnabled(true);
        nameEdit.setEnabled(true);
        String jsonFile = active.getIntent().getStringExtra(Constant.INTENT_KEY_ID_JSON_FILENAME);
        if (jsonFile != null && !jsonFile.equals("")) {
            isFromManage = true;
            // This is from borrower manage list
            borrower = new Borrower(jsonFile);
            idEdit.setText(borrower.getId());
            idEdit.setEnabled(false);
            nameEdit.setText(borrower.getName());
            nameEdit.setEnabled(false);
            if ("男".equals(borrower.getGender())) {
                maleButton.setChecked(true);
                femaleButton.setChecked(false);
            } else {
                maleButton.setChecked(false);
                femaleButton.setChecked(true);
            }
            minzuEdit.setText(borrower.getNation());
            if (borrower.getBirthday() != null) {
                dobEdit.setText(String.valueOf(borrower.getBirthday()));
            }
            addrEdit.setText(borrower.getAddress());
            locationEdit.setText(borrower.getLocation());
            if (borrower.getExpiryFrom() != null) {
                expiryFromEdit.setText(String.valueOf(borrower.getExpiryFrom()));
            }
            if (borrower.getExpiryTo() != null) {
                expiryToEdit.setText(String.valueOf(borrower.getExpiryTo()));
            }
        }
    }

    public boolean validateIdCardInputs() {
        String id = "";
        String name = "";
        String gender = "";
        String nation = "";
        Date dob = null;
        String address = "";
        String location = "";
        Date expiryFrom = null;
        Date expiryTo = null;

        if (idEdit != null) {
            id = idEdit.getText().toString();
        }
        if (nameEdit != null) {
            name = nameEdit.getText().toString();
        }
        if (maleButton != null && maleButton.isChecked()) {
            gender = "男";
        } else {
            gender = "女";
        }
        if (minzuEdit != null) {
            nation = minzuEdit.getText().toString();
        }
        if (dobEdit != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                dob = dateFormat.parse(dobEdit.getText().toString());
            } catch (ParseException pe) {
                Log.d(TAG, "unexpected date format: " + dobEdit.getText().toString()
                        + " , should be 'yyyy/MM/dd'");
                //TODO: need should error message here
            }
        }
        if (addrEdit != null) {
            address = addrEdit.getText().toString();
        }
        if (locationEdit != null) {
            location = locationEdit.getText().toString();
        }
        if (expiryFromEdit != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                expiryFrom = dateFormat.parse(expiryFromEdit.getText().toString());
            } catch (ParseException pe) {
                Log.d(TAG, "unexpected date format: " + expiryFromEdit.getText().toString()
                        + " , should be 'yyyy/MM/dd'");
                //TODO: need should error message here
            }
        }
        if (expiryToEdit != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                expiryTo = dateFormat.parse(expiryToEdit.getText().toString());
            } catch (ParseException pe) {
                Log.d(TAG, "unexpected date format: " + expiryToEdit.getText().toString()
                        + " , should be 'yyyy/MM/dd'");
                //TODO: need should error message here
            }
        }


        if (id.equals("") || name.equals("")) {
            if (nameEdit != null) {
                nameEdit.requestFocus();
            }
            return false;
        } else {
            if (borrower == null) {
                borrower = new Borrower();
            }
            borrower.setId(id);
            borrower.setName(name);
            borrower.setGender(gender);
            borrower.setNation(nation);
            borrower.setBirthday(dob);
            borrower.setAddress(address);
            borrower.setLocation(location);
            borrower.setExpiryFrom(expiryFrom);
            borrower.setExpiryTo(expiryTo);
            active.getIntent().putExtra(Constant.INTENT_KEY_ID, id);
            active.getIntent().putExtra(Constant.INTENT_KEY_NAME, name);

//            ArrayList<BorrowerData> list = new ArrayList<BorrowerData>();
//            BorrowerData data = new BorrowerData();
//            data.setDatumId(1);
//            data.setDatumName("test");
//            list.add(data);
//            data = new BorrowerData();
//            data.setDatumId(2);
//            data.setDatumName("test2");
//            list.add(data);
//            data = new BorrowerData();
//            data.setDatumId(3);
//            data.setDatumName("test3");
//            list.add(data);
//            data = new BorrowerData();
//            data.setDatumId(4);
//            data.setDatumName("test4");
//            list.add(data);
//            borrower.setDatalist(list);
            ((CollectActivity) active).saveBorrowerInfo(borrower);
            return true;
        }
    }
}
