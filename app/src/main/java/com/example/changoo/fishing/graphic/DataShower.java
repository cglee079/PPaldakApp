package com.example.changoo.fishing.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.changoo.fishing.R;

/**
 * Created by changoo on 2017-05-13.
 */

public class DataShower extends View {
	private static final String TAG = "DATA SHOWER";

	private float WIDTH = 0; // View 가로 사이즈
	private float HEIGHT = 0; // View 세로 사이즈
	private float radius = 0; // View 세로 사이즈
	private float centerX = 0;
	private float centerY = 0;

	private double maxData;
	private double data;

	public DataShower(Context context) {
		super(context);
	}

	public DataShower(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DataShower(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// height 진짜 크기 구하기
		int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

		switch (heightMode) {
		case View.MeasureSpec.UNSPECIFIED:// mode 가 셋팅되지 않은 크기가 넘어올때
			HEIGHT = heightMeasureSpec;
			break;
		case View.MeasureSpec.AT_MOST:// wrap_content (뷰 내부의 크기에 따라 크기가 달라짐)
			// HEIGHT = 20;
			break;
		case View.MeasureSpec.EXACTLY:// fill_parent, match_parent (외부에서 이미 크기가
										// 지정되었음)
			HEIGHT = View.MeasureSpec.getSize(heightMeasureSpec);
			break;
		}

		// width 진짜 크기 구하기
		int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);

		switch (widthMode) {
		case View.MeasureSpec.UNSPECIFIED:// mode 가 셋팅되지 않은 크기가 넘어올때
			WIDTH = widthMeasureSpec;
			break;
		case View.MeasureSpec.AT_MOST:// wrap_content (뷰 내부의 크기에 따라 크기가 달라짐)
			// WIDTH = 100;
			break;
		case View.MeasureSpec.EXACTLY:// fill_parent, match_parent (외부에서 이미 크기가  지정되었음)
			WIDTH = View.MeasureSpec.getSize(widthMeasureSpec);
			break;
		}

		Log.w(TAG, "onMeasure(" + WIDTH + "," + HEIGHT + ")");

		setMeasuredDimension((int) WIDTH, (int) HEIGHT);

		centerX = WIDTH / 2;
		centerY = HEIGHT / 2;

		radius = 0;
		if (WIDTH < HEIGHT) {
			radius = WIDTH;
		} else {
			radius = HEIGHT;
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		drawArc(canvas, 180, 360, getResources().getColor(R.color.colorTransParentBase10));
		drawArc(canvas, 180, dataToAngle(maxData), getResources().getColor(R.color.colorRed));
		drawArc(canvas, 180, dataToAngle(data), getResources().getColor(R.color.colorTransParentBase70));

		drawData(canvas, data);
	}

	private void drawData(Canvas canvas, Double data) {
		float textSize = 100.0f;
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(getResources().getColor(R.color.colorBase));
		paint.setTextSize(textSize);
		paint.setTextAlign(Paint.Align.CENTER);

		canvas.drawText(data + "F", centerX, centerY + (HEIGHT / 20), paint);
	}

	private void drawArc(Canvas canvas, int start_angle, int angleMount, int color) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);

		// HEIGHT MIN
		float gapForSquare 	= (WIDTH - HEIGHT) / 2;
		float gapInAndOut 	= 150;
		float forCenter 	= 0;
		float sizeUp 		= 0;
		canvas.drawArc(gapForSquare - sizeUp, 0.0f + forCenter - sizeUp, WIDTH - gapForSquare + sizeUp,
				HEIGHT + forCenter + sizeUp, start_angle, angleMount, true, paint);
		
		paint.setColor(Color.WHITE);
		canvas.drawArc(gapForSquare + gapInAndOut - sizeUp, 0.0f + gapInAndOut + forCenter - sizeUp, 
				WIDTH - gapForSquare - gapInAndOut + sizeUp, HEIGHT - gapInAndOut + forCenter + sizeUp, start_angle,
				360, true, paint);

	}

	private int dataToAngle(double data) {
		double limitdata = 10.0;
		double radio = data / limitdata;
		double angle = 360 * radio;

		return (int) angle;
	}

	public void add(Double data) {
		this.data = data;
		invalidate();
	}

	public void setMaxData(double maxData) {
		this.maxData = maxData;
	}
}
