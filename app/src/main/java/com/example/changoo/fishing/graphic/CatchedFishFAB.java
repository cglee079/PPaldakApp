package com.example.changoo.fishing.graphic;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.activity.CatchActivity;
import com.example.changoo.fishing.activity.MainActivity;
import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.util.Formatter;
import com.github.clans.fab.FloatingActionButton;


/**
 * Created by changoo on 2017-05-10.
 */

public class CatchedFishFAB extends FloatingActionButton {
    private AppCompatActivity mContext;
    private Fish mCathcedFish = null;

    public CatchedFishFAB(AppCompatActivity context, Fish mCathcedFish) {
        super(context);
        mContext = context;
        this.mCathcedFish = mCathcedFish;
        setImageResource(R.drawable.icon_fish);

        setColorNormalResId(R.color.colorBase);
        setColorPressedResId(R.color.colorBase);
        setShowAnimation(AnimationUtils.loadAnimation(mContext,
                R.anim.jump_from_down));
        setShowAnimation(AnimationUtils.loadAnimation(mContext,
                R.anim.jump_to_down));

        setLabelText(mCathcedFish.getDate() + " " + mCathcedFish.getTime() + " 잡은 물고기");
        setButtonSize(SIZE_NORMAL);

        this.setOnClickListener(onClickListener);

    }

    public CatchedFishFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CatchedFishFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, CatchActivity.class);
            intent.putExtra("fish", mCathcedFish);
            mContext.startActivityForResult(intent, MainActivity.FISH_CATCH);
        }
    };

    public Fish getmCathcedFish() {
        return mCathcedFish;
    }

    public void setmCathcedFish(Fish mCathcedFish) {
        this.mCathcedFish = mCathcedFish;
    }
}
