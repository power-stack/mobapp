package com.stackbase.mobapp.view.adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.activity.ThumbnailsActivity;
import com.stackbase.mobapp.objects.Borrower;
import com.stackbase.mobapp.objects.BorrowerData;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataTypeGridViewAdapter extends BaseAdapter {
    private Activity activity;
    private List<BorrowerData> dataList = new ArrayList<>();
    private Borrower borrower;

    public DataTypeGridViewAdapter(Activity a, Borrower borrower) {
        activity = a;
        if (borrower.getDatalist() != null) {
            this.dataList = borrower.getDatalist();
        }
        this.borrower = borrower;
    }

    public int getCount() {
        return dataList.size();
    }

    public Object getItem(int position) {
        return dataList.get(position);
    }

    public long getItemId(int position) {
        return dataList.get(position).getDatumId();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        BorrowerData data = dataList.get(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.borrower_data_type, null);
            holder = new ViewHolder();
            holder.btn = (ImageButton) convertView.findViewById(R.id.data_type_btn);
            holder.title = (TextView) convertView.findViewById(R.id.data_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.getTitle().setText(data.getDatumName());
        ImageButton.OnClickListener clickListener = new ButtonListener(data);
        holder.getBtn().setOnClickListener(clickListener);
        return convertView;
    }

    class ButtonListener implements ImageButton.OnClickListener {
        private BorrowerData data;
        public ButtonListener(BorrowerData d) {
            data = d;
        }

        @Override
        public void onClick(View v) {
            String parentFolder = Helper.getBorrowerSubFolder(borrower);
            String subFolder = Helper.getBorrowDataSubFolder(borrower, data);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            String rootDir = prefs.getString(Constant.KEY_STORAGE_DIR,
                    Constant.DEFAULT_STORAGE_DIR);
            File imageFolder = new File(rootDir + File.separator + parentFolder
                    + File.separator + subFolder);
            if (!imageFolder.exists()) {
                imageFolder.mkdirs();
            }
            Intent intent = new Intent();
            intent.putExtra(Constant.INTENT_KEY_PIC_FOLDER, imageFolder.getAbsolutePath());
            intent.setClass(activity, ThumbnailsActivity.class);
            activity.startActivity(intent);
        }

    }
    public static class ViewHolder {
        ImageButton btn;
        TextView title;

        public ImageButton getBtn() {
            return btn;
        }

        public TextView getTitle() {
            return title;
        }
    }

}