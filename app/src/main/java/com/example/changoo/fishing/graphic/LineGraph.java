package com.example.changoo.fishing.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.changoo.fishing.R;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by changoo on 2017-02-27.
 */

public class LineGraph extends View {

    private static final String TAG = "LINE_GRAPH";

    private static final int MAX_POINT = 10; //그래프의 최대 점 개수
    private static final int VISIBLE_POINT = 7;  // 보여질 점 개수
    private static final int POINT_RADIUS = 10; // 점의 반지름
    private static final int LINE_WIDTH = 5; // 선의 굵기
    private static final int POINT_TEXTSIZE = 40; //점의 글자 사이즈
    private static final int DATA_RATE_VIEW = 50;  // Data와 Y좌표의 비율.


    private float WIDTH = 0; //View 가로 사이즈
    private float HEIGHT = 0; //View 세로 사이즈
    private float xGap = 0; // 점 간 가로 간격
    private float yZero = 0; // Y좌표 0 // 위쪽이 0, 아래쪽이 MAX 반전해야함



    private double maxData=0; // 가장 큰 값

    private Paint mPointPaint = null;
    private Paint mLinePaint = null;
    private Paint mTextPaint = null;

    private DataQueue mDataQueue = new DataQueue();

    public LineGraph(Context context) {
        super(context, null);
    }

    public LineGraph(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public LineGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    /**
     * Private Method;
     */

    private float returnYbyData(Double data){
        return data.floatValue()*DATA_RATE_VIEW;
    }

    private void setPainter() {
        mPointPaint = new Paint();
        mLinePaint = new Paint();
        mTextPaint = new Paint();

        mPointPaint.setColor(Color.GRAY);
        mPointPaint.setAntiAlias(true);

        mLinePaint.setColor(getResources().getColor(R.color.colorBase));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(LINE_WIDTH);

        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(POINT_TEXTSIZE);
    }

    private void drawBackgroundLine(Canvas canvas) {
        // 가로줄 2kg 간격 , 0, 2,4,6,8,10;

        Paint mBackTextPaint = new Paint();
        mBackTextPaint.setAntiAlias(true);
        mBackTextPaint.setColor(Color.BLACK);
        mBackTextPaint.setTextSize(30);

        Paint mBackLinePaint = new Paint();
        mBackLinePaint.setAntiAlias(true);
        mBackLinePaint.setColor(getResources().getColor(R.color.colorTransParentBase30));
        mBackLinePaint.setStrokeWidth(3);

        float x = 0;
        float viewY = yZero;

        Double linePoint = 0.0;

        while (viewY > 0) {
            canvas.drawText(linePoint.toString(), x, viewY, mBackTextPaint);
            canvas.drawLine(50, viewY, WIDTH, viewY, mBackLinePaint);

            linePoint = linePoint + 2;
            viewY = yZero - returnYbyData(linePoint);
        }
    }

    private void drawMaxData(Canvas canvas){
        if(maxData!=0){
            Paint mMaxPaint = new Paint();
            mMaxPaint.setAntiAlias(true);
            mMaxPaint.setTextSize(50);
            mMaxPaint.setColor(Color.BLUE);
            mMaxPaint.setStrokeWidth(5);

            float viewY = returnYbyData(maxData);
            canvas.drawLine(50, yZero - viewY, WIDTH, yZero - viewY, mMaxPaint);
            canvas.drawText("MAX", WIDTH - 100, yZero - viewY, mMaxPaint);
        }
    }
    /**
     * Overide Method
     */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "ON DRAW");

        float x = 50;

        float beforeX = 50;
        float beforeY = yZero;

        drawBackgroundLine(canvas);
        drawMaxData(canvas);

        setPainter();
        Iterator<Double> iter = mDataQueue.getIterater();
        if (iter != null) {
            while (iter.hasNext()) {
                Double data = iter.next();
                float y = yZero - returnYbyData(data);

                canvas.drawCircle(x, y, POINT_RADIUS, mPointPaint);
                if(x>50)
                    canvas.drawLine(beforeX, beforeY, x, y, mLinePaint);
                canvas.drawText(data.toString(), x, y, mTextPaint);

                beforeX = x;
                beforeY = y;


                x += xGap;
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // height 진짜 크기 구하기
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED://mode 가 셋팅되지 않은 크기가 넘어올때
                HEIGHT = heightMeasureSpec;
                break;
            case MeasureSpec.AT_MOST://wrap_content (뷰 내부의 크기에 따라 크기가 달라짐)
                //HEIGHT = 20;
                break;
            case MeasureSpec.EXACTLY://fill_parent, match_parent (외부에서 이미 크기가 지정되었음)
                HEIGHT = MeasureSpec.getSize(heightMeasureSpec);
                break;
        }

        // width 진짜 크기 구하기
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED://mode 가 셋팅되지 않은 크기가 넘어올때
                WIDTH = widthMeasureSpec;
                break;
            case MeasureSpec.AT_MOST://wrap_content (뷰 내부의 크기에 따라 크기가 달라짐)
                //WIDTH = 100;
                break;
            case MeasureSpec.EXACTLY://fill_parent, match_parent (외부에서 이미 크기가 지정되었음)
                WIDTH = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }

        Log.w(TAG, "onMeasure(" + widthMeasureSpec + "," + heightMeasureSpec + ")");

        setMeasuredDimension((int) WIDTH, (int) HEIGHT);

        xGap = WIDTH / MAX_POINT;
        yZero = HEIGHT;
    }


    /**
     * Public Method
     */
    public void add(Double data) {
        mDataQueue.add(data);
        invalidate();
    }

    public void setMaxData(double maxData) {
        this.maxData = maxData;
    }

    /**
     * Customer Classs  // DATA_QUEUE
     */
    class DataQueue {

        Queue<Double> datas = null;

        public DataQueue() {
            datas = new LinkedList<>();
        }

        public void add(Double data) {
            if (datas.size() >= VISIBLE_POINT) {
                //overflow
                datas.poll();
            }

            datas.offer(data);
        }

        public Iterator<Double> getIterater() {
            return datas.iterator();
        }
    }
}



