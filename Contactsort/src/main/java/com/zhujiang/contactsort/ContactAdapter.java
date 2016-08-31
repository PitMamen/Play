package com.zhujiang.contactsort;

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
 * Created by zhujiang on 16-8-29.
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


    public ContactAdapter(Context context, int resource, List<Contact> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = getItem(position);
        LinearLayout layout = null;
        if (convertView == null) {
            layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(resource, parent, false);
        } else {
            layout = (LinearLayout) convertView;
        }

        TextView name = (TextView) layout.findViewById(R.id.name);
        LinearLayout sortKeyLayout = (LinearLayout) layout.findViewById(R.id.sort_key_layout);
        TextView sortKey = (TextView) layout.findViewById(R.id.sort_key);

        name.setText(contact.getName());

        int section = mIndexer.getSectionForPosition(position);
        if (position == mIndexer.getPositionForSection(section)) {
            sortKeyLayout.setVisibility(View.VISIBLE);
            sortKey.setText(contact.getSortKey());
        } else {
            sortKeyLayout.setVisibility(View.GONE);
        }

        return layout;
    }

    /**
     * 为当前适配器设置一个排序工具
     *
     * @param indexer
     */
    public void setIndexer(SectionIndexer indexer) {
        this.mIndexer = indexer;
    }


}
