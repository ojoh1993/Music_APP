package com.oz.music_app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

public class DbAdapter {

    private final Context mContext;
    //private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final String stPathToDB = android.os.Environment.getExternalStorageDirectory().toString()+"/Music_APP/data1.db";

    public DbAdapter(Context context){

        this.mContext=context;

    }

    public DbAdapter open(){
        //mDbHelper=new DatabaseHelper(mContext);
        mDb=SQLiteDatabase.openDatabase(stPathToDB,null,0);
        //mDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        //mDbHelper.close();
    }

    public String get_PlaceName(double longitude, double latitude){
        Cursor c=mDb.rawQuery("SELECT  TABLE_PLACE_SOURCE.NAME " +
                "FROM  TABLE_PLACE_SOURCE " +
                "WHERE TABLE_PLACE_SOURCE.UP>="+latitude+
                " AND TABLE_PLACE_SOURCE.DOWN<="+latitude+
                " AND TABLE_PLACE_SOURCE.LEFT<="+longitude+
                " AND TABLE_PLACE_SOURCE.RIGHT>="+longitude, null);

        if (c.getCount()==0) return null;
        //결과값은 한개인것으로 봄
        c.moveToFirst();
        return c.getString(0);
    }
    public Cursor get_Recommended_Songs_FromDatabase(String placename, String time, String weather,String moving_state){
        Cursor c;
        if(placename!=null) {//첫 검색 조건 : 시간, 날씨, 장소
            Log.d("OZ", "시간/날씨/장소");
            c = mDb.rawQuery("SELECT DISTINCT TABLE_PLACE.NAME, TABLE_PLACE.PLACE, TABLE_TIME.TIME, TABLE_WEATHER.WEATHER" +
                    " FROM  TABLE_PLACE, TABLE_TIME, TABLE_WEATHER" +
                    " WHERE TABLE_PLACE.PLACE='" + placename + "'" +
                    " AND TABLE_TIME.TIME='" + time + "'" +
                    " AND TABLE_WEATHER.WEATHER='" + weather + "'" +
                    " AND TABLE_PLACE.NAME=TABLE_TIME.NAME AND TABLE_PLACE.NAME=TABLE_WEATHER.NAME", null);
            //검색 결과가 있다면 바로 리턴
            if(c.getCount()>0) {
                Toast.makeText(mContext,"시간/날씨/장소",Toast.LENGTH_SHORT).show();
                c.moveToFirst();
                return c;
            }
        }
        Log.d("OZ","시간/날씨/이동상태");
        c = mDb.rawQuery("SELECT DISTINCT TABLE_PLACE.NAME, TABLE_TIME.TIME, TABLE_WEATHER.WEATHER, TABLE_PLACE.MOVING_STATE" +
                " FROM TABLE_PLACE, TABLE_TIME, TABLE_WEATHER" +
                " WHERE TABLE_PLACE.MOVING_STATE='" + moving_state + "'" +
                " AND TABLE_TIME.TIME='" + time + "'" +
                " AND TABLE_WEATHER.WEATHER='" + weather + "'" +
                " AND TABLE_PLACE.NAME=TABLE_TIME.NAME AND TABLE_PLACE.NAME=TABLE_WEATHER.NAME", null);
        //검색 결과가 있다면 바로 리턴
        if(c.getCount()>0) {
            Toast.makeText(mContext,"시간/날씨/이동상태",Toast.LENGTH_SHORT).show();
            c.moveToFirst();
            return c;
        }
        //이렇게 하고도 검색 결과가 없을 경우, 검색 조건 : 시간, 날씨
        Log.d("OZ","시간/날씨");
         c = mDb.rawQuery("SELECT DISTINCT TABLE_TIME.NAME, TABLE_TIME.TIME, TABLE_WEATHER.WEATHER"+
                    " FROM TABLE_TIME, TABLE_WEATHER" +
                    " WHERE TABLE_TIME.TIME='" + time + "'" +
                    " AND TABLE_WEATHER.WEATHER='" + weather + "'" +
                    " AND TABLE_TIME.NAME=TABLE_WEATHER.NAME", null);
        //검색 결과가 있다면 바로 리턴
        if(c.getCount()>0) {
            Toast.makeText(mContext,"시간/날씨",Toast.LENGTH_SHORT).show();
            c.moveToFirst();
            return c;
        }
        //이래도 검색 결과가 없다면 검색 조건 : 시간
        Log.d("OZ","시간");
        c = mDb.rawQuery("SELECT TABLE_TIME.NAME, TABLE_TIME.TIME"+
                " FROM TABLE_TIME"+
                " WHERE TABLE_TIME.TIME='" + time + "'", null);
        if(c.getCount()>0) {
            Toast.makeText(mContext,"시간",Toast.LENGTH_SHORT).show();
            c.moveToFirst();
            return c;
        }
        //이래도 없다면 진짜 없는거. 근데 시간은 왠만하면 아침 오후 저녁 밤 다 임력 되어 있을거라..
        else return null;

    }

    private class DatabaseHelper extends SQLiteOpenHelper{

        private static final java.lang.String DATABASE_CREATE = "";
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "data1.db";
        private static final String TAG="DbAdapter";

        public DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading db from version" + oldVersion + " to" +
                    newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS data");
            onCreate(db);
        }
    }

}
