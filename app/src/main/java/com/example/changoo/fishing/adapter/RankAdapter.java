package com.example.changoo.fishing.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.graphic.CircleTransform;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.util.Formatter;
import com.example.changoo.fishing.util.Sort;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class RankAdapter extends android.widget.BaseAdapter {

    public static final int CRITERIA_MAXPOWER = 0;
    public static final int CRITERIA_AVGPOWER = 1;

    private int criteria = 0;
    Context mContext = null;
    ArrayList<Fish> mData = null;
    LayoutInflater mLayoutInflater = null;

    public RankAdapter(Context context, ArrayList<Fish> data) {
        mContext = context;
        mData = data;
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

        View item = null;
        if (i == 0) {
            item = mLayoutInflater.inflate(R.layout.listview_item_null, null);
        } else {
            item = mLayoutInflater.inflate(R.layout.listview_item_rank, null);

            ImageView mFishPictureImgv = (ImageView) item.findViewById(R.id.imgv_item_rank_image);
            TextView mFishnameTv = (TextView) item.findViewById(R.id.tv_item_rank_name);
            TextView mFishSpciesTv = (TextView) item.findViewById(R.id.tv_item_rank_species);
            TextView mUserIdTv = (TextView) item.findViewById(R.id.tv_item_rank_user_id);
            TextView mFishRankTv = (TextView) item.findViewById(R.id.tv_item_rank_rank);
            TextView mFishMaxLabelTv = (TextView) item.findViewById(R.id.tv_item_rank_max_label);
            TextView mFishMaxTv = (TextView) item.findViewById(R.id.tv_item_rank_max);
            TextView mFishAvgLabelTv = (TextView) item.findViewById(R.id.tv_item_rank_avg_label);
            TextView mFishAvgTv = (TextView) item.findViewById(R.id.tv_item_rank_avg);
            TextView mFishDateTv = (TextView) item.findViewById(R.id.tv_item_rank_date);


            mFishMaxLabelTv.setTextColor(mContext.getResources().getColor(R.color.colorGray));
            mFishMaxTv.setTextColor(mContext.getResources().getColor(R.color.colorBase));
            mFishAvgLabelTv.setTextColor(mContext.getResources().getColor(R.color.colorGray));
            mFishAvgTv.setTextColor(mContext.getResources().getColor(R.color.colorBase));

            mFishMaxLabelTv.setTypeface(null, Typeface.NORMAL);
            mFishMaxTv.setTypeface(null, Typeface.NORMAL);
            mFishAvgLabelTv.setTypeface(null, Typeface.NORMAL);
            mFishAvgTv.setTypeface(null, Typeface.NORMAL);

            switch (criteria) {
                case CRITERIA_MAXPOWER:
                    mFishMaxLabelTv.setTextColor(Color.BLACK);
                    mFishMaxTv.setTextColor(mContext.getResources().getColor(R.color.colorBlue));
                    mFishMaxLabelTv.setTypeface(null, Typeface.BOLD);
                    mFishMaxTv.setTypeface(null, Typeface.BOLD);
                    break;
                case CRITERIA_AVGPOWER:
                    mFishAvgLabelTv.setTextColor(Color.BLACK);
                    mFishAvgTv.setTextColor(mContext.getResources().getColor(R.color.colorBlue));
                    mFishAvgLabelTv.setTypeface(null, Typeface.BOLD);
                    mFishAvgTv.setTypeface(null, Typeface.BOLD);
                    break;
            }
            String fishName = mData.get(i).getName();
            String userId = mData.get(i).getUser_id();
            String fishSpecies = mData.get(i).getSpecies();
            Double fishMax = Formatter.setFormat(mData.get(i).getMaxFower());
            Double fishAvg = Formatter.setFormat(mData.get(i).getAvgFower());
            String fishImageFile = mData.get(i).getImageFile();
            String fishDate = mData.get(i).getDate();

            mFishnameTv.setText(fishName);
            mFishSpciesTv.setText("(" + fishSpecies + ")");
            mUserIdTv.setText(userId);
            mFishRankTv.setText((i + 1) + "위");
            mFishMaxTv.setText(fishMax.toString() + " F");
            mFishAvgTv.setText(fishAvg.toString() + " F");
            mFishDateTv.setText(fishDate);

            if (fishImageFile.equals("null"))
                Picasso.with(mContext).load(R.drawable.image_default_fish).transform(new CircleTransform()).into(mFishPictureImgv);
            else
                Picasso.with(mContext).load(HttpManager.getFishImageURL() + fishImageFile).transform(new CircleTransform()).into(mFishPictureImgv);
        }

        return item;
    }

    public void setCriteria(int criteria) {
        this.criteria = criteria;

        switch (criteria) {
            case CRITERIA_MAXPOWER:
                Collections.sort(mData, Sort.maxDesc);
                break;
            case CRITERIA_AVGPOWER:
                Collections.sort(mData, Sort.avgDesc);
                break;
        }

        notifyDataSetChanged();
    }

    public void refreshData(ArrayList<Fish> mDataFromServer) {
        mData.clear();
        mData.addAll(mDataFromServer);
        this.notifyDataSetChanged();
    }
}

