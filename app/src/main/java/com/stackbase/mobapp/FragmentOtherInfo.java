package com.stackbase.mobapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.stackbase.mobapp.objects.Borrower;
import com.stackbase.mobapp.objects.BorrowerData;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.view.adapters.DataTypeGridViewAdapter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentOtherInfo extends Fragment {
    Activity active;
    View content;
    private static final String TAG = FragmentOtherInfo.class.getSimpleName();

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag, Class<?> viewType) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag, viewType));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag) && viewType.isInstance(child)) {
                views.add(child);
            }
        }
        return views;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        content = inflater.inflate(R.layout.fragment_otherinfo, container, false);
        return content;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            active = this.getActivity();
            GridView gridview = (GridView) content.findViewById(R.id.fragment_otherinfo_gridview);

            String jsonFile = active.getIntent().getStringExtra(Constant.INTENT_KEY_ID_JSON_FILENAME);
            if (jsonFile != null && !jsonFile.equals("")) {
                Borrower borrower = new Borrower(jsonFile);
                if (borrower.getDatalist() == null || borrower.getDatalist().size() == 0) {
                    // Load borrow type from template
                    Log.d(TAG, "Can not find the borrow type from borrower json file, will load it from template.");
                    List<BorrowerData> templateDats = new ArrayList<>();
                    JSONObject json = Helper.loadJsonFromRaw(getResources(), R.raw.borrow_data_template);
                    Object obj = json.get("datalist");
                    if (obj != null && obj instanceof JSONArray) {
                        for (Object jobj: ((JSONArray) obj)) {
                            BorrowerData data = new BorrowerData();
                            Helper.covertJson(data, ((JSONObject) jobj));
                            templateDats.add(data);
                        }
                    }
                    borrower.setDatalist(templateDats);
                    ((CollectActivity) active).saveBorrowerInfo(borrower);
                }
                gridview.setAdapter(new DataTypeGridViewAdapter(active, borrower));
            }
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
