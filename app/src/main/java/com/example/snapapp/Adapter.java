package com.example.snapapp;

import android.content.Context;
import android.icu.text.UnicodeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Adapter extends BaseAdapter {

    private char c = '\u26A1';

    private List<Snap> items;

    private LayoutInflater layoutInflater;

    public Adapter(List<Snap> items, Context context) {
        this.items = items;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.itemrow, null);
        }

        LinearLayout linearLayout = (LinearLayout)convertView;

        TextView textView = convertView.findViewById(R.id.rowTextView);
        // Avoid null pointer
        if(textView != null){
            textView.setText("\u26A1\u26A1\u26A1 New Snap \u26A1\u26A1\u26A1");
        }

        return linearLayout;
    }
}
