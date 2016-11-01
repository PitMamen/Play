package com.example.contactdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pengxinkai001 on 2016/11/1.
 */
public class ContactAdapter extends ArrayAdapter<Contact> {

    /**
     * 需要设置的item布局
     */
    private int resource;
    /**
     * 字母表排序
     */
    private SectionIndexer mIndexer;


    public ContactAdapter(Context context, int resource, List<Contact> object) {
        super(context, resource, object);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = getItem(position);
        LinearLayout linearLayout = null;
        if (convertView == null) {

            linearLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(resource, parent, false);
        } else {
            linearLayout = (LinearLayout) convertView;
        }
        TextView name = (TextView) linearLayout.findViewById(R.id.name);
        LinearLayout sortkeylayout = (LinearLayout) linearLayout.findViewById(R.id.sort_key_layout);
        TextView sorket = (TextView) linearLayout.findViewById(R.id.sort_key);

        name.setText(contact.getName());

        int secation = mIndexer.getSectionForPosition(position);
        if (position == mIndexer.getPositionForSection(secation)) {
            sortkeylayout.setVisibility(View.VISIBLE);
            sorket.setText(contact.getSortkey());
        } else {
            sortkeylayout.setVisibility(View.GONE);
        }
        return linearLayout;

    }

    /**
     * 为当前适配器设置一个排序工具
     *
     */
    public void setIndexer(SectionIndexer sectionIndexer) {

        this.mIndexer = sectionIndexer;

    }


}
