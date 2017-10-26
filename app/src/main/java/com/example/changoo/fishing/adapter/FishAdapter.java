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

public class FishAdapter extends android.widget.BaseAdapter {
    public static final int SORTED_NAME=0;
    public static final int SORTED_SPECIES=1;
    public static final int SORTED_MAXPOWER =2;
    public static final int SORTED_AVGPOWER=3;
    public static final int SORTED_DATETIME=4;

    private int sorted=0;
    private Context mContext = null;
    private ArrayList<Fish> mData = null;
    private LayoutInflater mLayoutInflater = null;


    public FishAdapter(Context context, ArrayList<Fish> data) {
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
        if(view!=null)
            item=view;
        else
            item = mLayoutInflater.inflate(R.layout.listview_item_fish, null);

        ImageView mFishPictureImgv = (ImageView) item.findViewById(R.id.imgv_item_fish_image);
        TextView mFishnameTv = (TextView) item.findViewById(R.id.tv_item_fish_name);
        TextView mFishSpeciesTv = (TextView) item.findViewById(R.id.tv_item_fish_species);
        TextView mFishMaxLabelTv = (TextView) item.findViewById(R.id.tv_item_fish_max_label);
        TextView mFishMaxTv = (TextView) item.findViewById(R.id.tv_item_fish_max);
        TextView mFishAvgLabelTv = (TextView) item.findViewById(R.id.tv_item_fish_avg_label);
        TextView mFishAvgTv = (TextView) item.findViewById(R.id.tv_item_fish_avg);
        TextView mFishDateTv = (TextView) item.findViewById(R.id.tv_item_fish_date);

        mFishnameTv.setTextColor(Color.BLACK);
        mFishSpeciesTv.setTextColor(Color.BLACK);
        mFishMaxLabelTv.setTextColor(mContext.getResources().getColor(R.color.colorGray));
        mFishMaxTv.setTextColor(mContext.getResources().getColor(R.color.colorBase));
        mFishAvgLabelTv.setTextColor(mContext.getResources().getColor(R.color.colorGray));
        mFishAvgTv.setTextColor(mContext.getResources().getColor(R.color.colorBase));
        mFishDateTv.setTextColor(mContext.getResources().getColor(R.color.colorGray));

        mFishnameTv.setTypeface(null, Typeface.NORMAL);
        mFishSpeciesTv.setTypeface(null, Typeface.NORMAL);
        mFishMaxLabelTv.setTypeface(null, Typeface.NORMAL);
        mFishMaxTv.setTypeface(null, Typeface.NORMAL);
        mFishAvgLabelTv.setTypeface(null, Typeface.NORMAL);
        mFishAvgTv.setTypeface(null, Typeface.NORMAL);
        mFishDateTv.setTypeface(null, Typeface.NORMAL);


        switch(sorted){
            case SORTED_NAME:
                mFishnameTv.setTextColor(mContext.getResources().getColor(R.color.colorBlue));
                mFishnameTv.setTypeface(null, Typeface.BOLD);
                break;
            case SORTED_SPECIES:
                mFishSpeciesTv.setTextColor(mContext.getResources().getColor(R.color.colorBlue));
                mFishSpeciesTv.setTypeface(null, Typeface.BOLD);
                break;
            case SORTED_MAXPOWER:
                mFishMaxLabelTv.setTextColor(Color.BLACK);
                mFishMaxTv.setTextColor(mContext.getResources().getColor(R.color.colorBlue));
                mFishMaxLabelTv.setTypeface(null, Typeface.BOLD);
                mFishMaxTv.setTypeface(null, Typeface.BOLD);
                break;
            case SORTED_AVGPOWER:
                mFishAvgLabelTv.setTextColor(Color.BLACK);
                mFishAvgTv.setTextColor(mContext.getResources().getColor(R.color.colorBlue));
                mFishAvgLabelTv.setTypeface(null, Typeface.BOLD);
                mFishAvgTv.setTypeface(null, Typeface.BOLD);
                break;
            case SORTED_DATETIME:
                mFishDateTv.setTextColor(mContext.getResources().getColor(R.color.colorBlue));
                mFishDateTv.setTypeface(null, Typeface.BOLD);
                break;
        }

        String fishName = mData.get(i).getName();
        String fishSpecies = mData.get(i).getSpecies();
        Double fishMax = Formatter.setFormat(mData.get(i).getMaxFower());
        Double fishAvg = Formatter.setFormat(mData.get(i).getAvgFower());
        String fishImageFile = mData.get(i).getImageFile();
        String fishDate = mData.get(i).getDate();

        mFishnameTv.setText(fishName);
        mFishSpeciesTv.setText("(" + fishSpecies + ")");
        mFishMaxTv.setText(fishMax.toString() + " F");
        mFishAvgTv.setText(fishAvg.toString() + " F");
        mFishDateTv.setText(fishDate);

        if (fishImageFile.equals("null"))
            Picasso.with(mContext).load(R.drawable.image_default_fish).transform(new CircleTransform()).into(mFishPictureImgv);
        else
            Picasso.with(mContext).load(HttpManager.getFishImageURL() + fishImageFile).transform(new CircleTransform()).into(mFishPictureImgv);


        return item;
    }

    public void setSorted(int sorted) {
        this.sorted = sorted;

        switch (sorted){
            case SORTED_NAME:
                Collections.sort(mData, Sort.nameAsc);
                break;
            case SORTED_SPECIES:
                Collections.sort(mData, Sort.speciesAsc);
                break;
            case SORTED_MAXPOWER:
                Collections.sort(mData, Sort.maxDesc);
                break;
            case SORTED_AVGPOWER:
                Collections.sort(mData, Sort.avgDesc);
                break;
            case SORTED_DATETIME:
                Collections.sort(mData, Sort.datetimeAsc);
                break;
        }

        notifyDataSetChanged();
    }

    public void refreshData(ArrayList<Fish> mDataFromServer){
        mData.clear();
        mData.addAll(mDataFromServer);
        notifyDataSetChanged();
    }
}



