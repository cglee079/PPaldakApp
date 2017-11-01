package com.example.changoo.fishing.graphic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.changoo.fishing.activity.CropImageActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class CropImage extends ImageView {
    private static final String TAG = "CROP_IMAGE";

    private CropImageActivity mContext;

    private String mImagePath;
    private static final int DEP = 50;  // Crop 경계선의 유효폭(선근처)

    private float sx, ex, sy, ey;
    private Bitmap mPicture;
    private float mDisplayWidth;
    private float mDisplayHeight;
    private float mReScaleWidth;
    private float mReScaleHeight;
    private float scaleRatio;
    private float centerX;
    private float centerY;
    private boolean orientation = false;
    private int mPictureAngle=0;

    private Paint mPaint;

    public CropImage(Context context, AttributeSet attrs) {
        super(context, attrs);

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mDisplayWidth = display.getWidth();  // 화면 크기 설정
        mDisplayHeight = display.getHeight();

        mImagePath = BitmapManager.getImagePath();
        Log.e(TAG, mImagePath);
        mContext = (CropImageActivity) context;

        // 비트맵 크기 조절(메모리 문제로 인하여 1/2 크기로)
        BitmapFactory.Options resizeOpts = new BitmapFactory.Options();
        resizeOpts.inSampleSize = 2;
        try {
            mPicture = BitmapFactory.decodeStream(new FileInputStream(mImagePath), null, resizeOpts);
           
            // 이미지를 상황에 맞게 회전시킨다
            ExifInterface exif 	= new ExifInterface(mImagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree 		= exifOrientationToDegrees(exifOrientation);
            mPictureAngle 		= exifDegree;
            mPicture	 		= rotate(mPicture,mPictureAngle);

        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.i(TAG, "PICTURE SIZE " + mPicture.getWidth() + " " + "*" + "  " + mPicture.getHeight());
        Log.i(TAG, "VIEW SIZE " + mDisplayWidth + " " + "*" + "  " + mDisplayHeight);


        mReScaleWidth 	= mDisplayWidth;
        scaleRatio 		= mDisplayWidth / mPicture.getWidth();
        mReScaleHeight 	= mPicture.getHeight() * scaleRatio;

        mPicture = Bitmap.createScaledBitmap(mPicture, (int) mDisplayWidth, (int) mReScaleHeight, false);

        Log.i(TAG, "PICTURE RESCALE SIZE :  " + mPicture.getWidth() + " " + "*" + "  " + mPicture.getHeight());

        if (mPicture.getWidth() < mPicture.getHeight()){
            orientation = true;
        } else{
            orientation = false;
        }

        centerX = mPicture.getWidth() / 2;
        centerY = mPicture.getHeight() / 2;

        if (orientation) {
            sx = 0;  // 초기 Crop선의 위치 설정
            ex = mPicture.getWidth();
            sy = centerY - centerX;
            ey = centerY + centerX;
        } else {
            sx = centerX - centerY;  // 초기 Crop선의 위치 설정
            ex = centerX + centerY;
            sy = 0;
            ey = mPicture.getHeight();
        }


        // 페인트 설정
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
    }

    public void onDraw(Canvas canvas) {
        // 사각형 라인 그리기
        canvas.drawBitmap(mPicture, 0, 0, null);

        canvas.drawCircle(sx, sy, 20, mPaint);
        canvas.drawCircle(sx, ey, 20, mPaint);
        canvas.drawCircle(ex, sy, 20, mPaint);
        canvas.drawCircle(ex, ey, 20, mPaint);

        canvas.drawLine(sx, sy, ex, sy, mPaint);
        canvas.drawLine(ex, sy, ex, ey, mPaint);
        canvas.drawLine(sx, sy, sx, ey, mPaint);
        canvas.drawLine(sx, ey, ex, ey, mPaint);

        canvas.drawLine(sx, (sy + ey) / 2, ex, (sy + ey) / 2, mPaint);
        canvas.drawLine((sx + ex) / 2, sy, (sx + ex) / 2, ey, mPaint);
    }

    // 이벤트 처리, 현재의 그리기 모드에 따른 점의 위치를 조정
    float dx = 0;
    float dy = 0;
    float oldx;
    float oldy;
    boolean bsx, bsy, bex, bey;
    boolean bMove = false;

    public boolean onTouchEvent(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            oldx = x;
            oldy = y;

            // 눌려진곳이 선 근처인가 확인
            if ((x > sx - DEP) && (x < sx + DEP)){
                bsx = true;
            } else if ((x > ex - DEP) && (x < ex + DEP)){
                bex = true;
            } 
            
            if ((y > sy - DEP) && (y < sy + DEP)){
                bsy = true;
            } else if ((y > ey - DEP) && (y < ey + DEP)) {
                bey = true;
            }

            // 어느 하나라도 선택이 되었다면 move에서 값 변경
            if ((bsx || bex || bsy || bey)) {
                bMove = false;
            } else if (((x > sx + DEP) && (x < ex - DEP)) && ((y > sy + DEP) && (y < ey - DEP))){
                bMove = true;
            }
            
            invalidate(); // 움직일때 다시 그려줌
            return true;
        }

        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            if (bsx) { sx = x; }
            if (bex) { ex = x; }
            if (bsy) { sy = y; }
            if (bey) { ey = y; }

            // 사각형의 시작 라인보다 끝라인이 크지않게 처리
            if (ex <= sx + DEP) {
                ex = sx + DEP;
                return true;
            }
            
            if (ey <= sy + DEP) {
                ey = sy + DEP;
                return true;
            }

            // 움직인 거리 구해서 적용
            if (bMove) {
                dx = oldx - x;
                dy = oldy - y;

                sx -= dx;
                ex -= dx;

                sy -= dy;
                ey -= dy;

                if (sx <= 1) sx = 1;
                if (ex >= mDisplayWidth - 1) ex = mDisplayWidth - 1;
                if (sy <= 1) sy = 1;
                if (ey >= mReScaleHeight - 1) ey = mReScaleHeight - 1;

            }

            if (bsx && sx <= 1) {
                sx = 1;
            }
            if (bex && ex >= mDisplayWidth - 1) {
                ex = mDisplayWidth - 1;
            }
            if (bsy && sy <= 1) {
                sy = 1;
            }
            if (bey && ey >= mReScaleHeight - 1) {
                ey = mReScaleHeight - 1;
            }


            invalidate(); // 움직일때 다시 그려줌
            oldx = x;
            oldy = y;
            return true;
        }

        // ACTION_UP 이면 그리기 종료
        if (e.getAction() == MotionEvent.ACTION_UP) {
            bsx = bex = bsy = bey = bMove = false;
            return true;
        }

        return false;
    }

    // 선택된 사각형의 이미지를 저장
    public void save() {
        Bitmap tmp = Bitmap.createBitmap(mPicture, (int) sx, (int) sy, (int) (ex - sx), (int) (ey - sy));
        byte[] byteArray = bitmapToByteArray(tmp);
        File file = new File(mImagePath);

        Log.e(TAG, file.getAbsolutePath());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Toast.makeText(mContext, "파일 저장 중 에러 발생 : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // 이미지를 전송하기위한 테스트 코드
    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public void rotatePicture(){
        mImagePath = BitmapManager.getImagePath();
        Log.e(TAG, mImagePath);

        // 비트맵 크기 조절(메모리 문제로 인하여 1/2 크기로)
        BitmapFactory.Options resizeOpts = new BitmapFactory.Options();
        resizeOpts.inSampleSize = 2;
        try {
            mPicture = BitmapFactory.decodeStream(new FileInputStream(mImagePath), null, resizeOpts);
            mPictureAngle += 90;
            mPicture = rotate(mPicture,mPictureAngle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "PICTURE SIZE " + mPicture.getWidth() + " " + "*" + "  " + mPicture.getHeight());
        Log.i(TAG, "VIEW SIZE " + mDisplayWidth + " " + "*" + "  " + mDisplayHeight);

        mReScaleWidth 	= mDisplayWidth;
        scaleRatio		= mDisplayWidth / mPicture.getWidth();
        mReScaleHeight 	= mPicture.getHeight() * scaleRatio;

        mPicture = Bitmap.createScaledBitmap(mPicture, (int) mDisplayWidth, (int) mReScaleHeight, false);

        Log.i(TAG, "PICTURE RESCALE SIZE :  " + mPicture.getWidth() + " " + "*" + "  " + mPicture.getHeight());

        if (mPicture.getWidth() < mPicture.getHeight()){
            orientation = true;
        } else {
            orientation = false;
        }

        centerX = mPicture.getWidth() / 2;
        centerY = mPicture.getHeight() / 2;

        if (orientation) {
            sx = 0;  // 초기 Crop선의 위치 설정
            ex = mPicture.getWidth();
            sy = centerY - centerX;
            ey = centerY + centerX;
        } else {
            sx = centerX - centerY;  // 초기 Crop선의 위치 설정
            ex = centerX + centerY;
            sy = 0;
            ey = mPicture.getHeight();

        }

        invalidate();
    }

    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if(degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }
}