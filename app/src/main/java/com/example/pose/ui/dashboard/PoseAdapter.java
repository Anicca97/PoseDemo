package com.example.pose.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pose.R;

import java.util.LinkedList;

public class PoseAdapter extends BaseAdapter {
    private LinkedList<Pose> mData;
    private Context mContext;
    private String desc;

    public PoseAdapter(LinkedList<Pose> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
        this.desc = "30s / 组 . 2组";
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        ImageView img = (ImageView) convertView.findViewById(R.id.image_view_avatar);
        TextView txtName = (TextView) convertView.findViewById(R.id.text_view_name);
        TextView txtDesc = (TextView) convertView.findViewById(R.id.text_view_description);
        img.setBackgroundResource(mData.get(position).getPic());
        txtName.setText(mData.get(position).getName());
        txtDesc.setText(desc);
        return convertView;
    }
}
