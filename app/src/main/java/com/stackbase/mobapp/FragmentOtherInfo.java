package com.stackbase.mobapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
