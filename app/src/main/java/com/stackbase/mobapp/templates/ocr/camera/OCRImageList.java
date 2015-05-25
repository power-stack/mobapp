package com.stackbase.mobapp.templates.ocr.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stackbase.mobapp.R;

/**
 * Created by bryan on 15/5/24.
 */
public class OCRImageList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] web;
    private final Bitmap[] images;

    public OCRImageList(Activity context,
                      String[] web, Bitmap[] images) {
        super(context, R.layout.imagelist, web);
        this.context = context;
        this.web = web;
        this.images = images;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.imagelist, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.text);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        txtTitle.setText(web[position]);
        imageView.setImageBitmap(images[position]);
        return rowView;
    }
}
