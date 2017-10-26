package com.example.changoo.fishing.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.changoo.fishing.model.Fish;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by changoo on 2017-05-18.
 */

public class CatchedFishDBHelper extends SQLiteOpenHelper {
    public static final String TAG = "CatchedFishDBHelper";

    public CatchedFishDBHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table catchedfishs(";
        sql += "id string primary key,";
        sql += "maxPower double,";
        sql += "avgPower double,";
        sql += "date string,";
        sql += "time string,";
        sql += "timeing integer,";
        sql += "gps_lat double,";
        sql += "gps_log double";
        sql += ")";

        Log.i(TAG, "SQL " + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists catchedfishs");
        onCreate(db);
    }

    public ArrayList<Fish> getCatchedFishsFromDB( ) {
        Log.i(TAG,"get CatchedFishs From DB");
        ArrayList<Fish> catchedFishs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "select * from catchedfishs";
        Cursor cursor = db.rawQuery(sql, null);

        String id;
        Double maxPower;
        double avgPower;
        String date;
        String time;
        int timeing;
        double gps_lat;
        double gps_lot;

        while (cursor.moveToNext()) {
            id = cursor.getString(0);
            maxPower = cursor.getDouble(1);
            avgPower = cursor.getDouble(2);
            date = cursor.getString(3);
            time = cursor.getString(4);
            timeing = cursor.getInt(5);
            gps_lat = cursor.getDouble(6);
            gps_lot = cursor.getDouble(7);

            Fish catchedFish = new Fish();

            catchedFish.setId(id);
            catchedFish.setMaxFower(maxPower);
            catchedFish.setAvgFower(avgPower);
            catchedFish.setDate(date);
            catchedFish.setTime(time);
            catchedFish.setTimeing(timeing);
            catchedFish.setGPS_lat(gps_lat);
            catchedFish.setGPS_lot(gps_lot);
            catchedFishs.add(catchedFish);
        }
        return catchedFishs;
    }

    public void saveCatchedFishsToDB(ArrayList<Fish> catchedFishs){
        this.clearDB();

        Log.i(TAG,"save CatchedFishs To DB ");


        SQLiteDatabase db = getWritableDatabase();

        Iterator<Fish> iter=catchedFishs.iterator();

        String sql="";
        while(iter.hasNext()){
            Fish catchedFish=iter.next();
            sql="insert into catchedfishs values(";
            sql+="'"+catchedFish.getId()+"',";
            sql+="'"+catchedFish.getMaxFower()+"',";
            sql+="'"+catchedFish.getAvgFower()+"',";
            sql+="'"+catchedFish.getDate()+"',";
            sql+="'"+catchedFish.getTime()+"',";
            sql+="'"+catchedFish.getTimeing()+"',";
            sql+="'"+catchedFish.getGPS_lat()+"',";
            sql+="'"+catchedFish.getGPS_lot()+"'";
            sql+=");";
            try {
                db.execSQL(sql);
            }catch (SQLiteException e){
                Log.e(TAG,e.getMessage());
            }
        }

    }

    public void clearDB(){
        Log.i(TAG,"clear DB");
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from catchedfishs");
    }
}
