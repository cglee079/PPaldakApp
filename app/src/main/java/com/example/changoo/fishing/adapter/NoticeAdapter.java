package com.example.changoo.fishing.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.model.Notice;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NoticeAdapter extends android.widget.BaseAdapter {

	private Context mContext = null;
	private ArrayList<Notice> mData = null;
	private LayoutInflater mLayoutInflater = null;

    public NoticeAdapter(Context context, ArrayList<Notice> data) {
        mContext 	= context;
        mData 		= data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View item;
        if(view == null){
            item = mLayoutInflater.inflate(R.layout.listview_item_notice, null);
        } else {
            item = view;
        }

        Notice notice=mData.get(i);

        TextView mNoticeTimeTv=(TextView)item.findViewById(R.id.item_tv_notice_time);
        TextView mNoticeMsgTv=(TextView)item.findViewById(R.id.item_tv_notice_msg);

        mNoticeMsgTv.setText(notice.getMsg());
        mNoticeTimeTv.setText(notice.getTimestr());

        return item;
    }

    public void add(Notice notice){
        mData.add(0,notice);
        notifyDataSetChanged();
    }


}
