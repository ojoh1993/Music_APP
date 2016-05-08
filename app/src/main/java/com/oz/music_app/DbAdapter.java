package com.oz.music_app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by LG on 2016-04-01.
 */
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
        c.moveToFirst();
        return c.getString(0);
    }

    public String get_Recommended_SongTitle_FromDatabase(@Nullable String placename, String time, String weather,@Nullable String moving_state){

        Cursor c;
        if(placename==null) {//내가 지금 위치한 장소가 DB상에 없다면 이동상태만 본다.
            c = mDb.rawQuery("SELECT DISTINCT TABLE_PLACE.NAME, TABLE_PLACE.PLACE, TABLE_TIME.TIME, TABLE_WEATHER.WEATHER, TABLE_PLACE.MOVING_STATE" +
                    " FROM  TABLE_PLACE, TABLE_TIME, TABLE_WEATHER" +
                    " WHERE TABLE_PLACE.MOVING_STATE='" + moving_state + "'" +
                    " AND TABLE_TIME.TIME='" + time + "'" +
                    " AND TABLE_WEATHER.WEATHER='" + weather + "'" +
                    " AND TABLE_PLACE.NAME=TABLE_TIME.NAME AND TABLE_PLACE.NAME=TABLE_WEATHER.NAME", null);
        }else{//있다면 이동상태 신경 꺼야하나??
            c = mDb.rawQuery("SELECT DISTINCT TABLE_PLACE.NAME, TABLE_PLACE.PLACE, TABLE_TIME.TIME, TABLE_WEATHER.WEATHER, TABLE_PLACE.MOVING_STATE" +
                    " FROM  TABLE_PLACE, TABLE_TIME, TABLE_WEATHER" +
                    " WHERE TABLE_PLACE.PLACE='" + placename +"'"+
                    " AND TABLE_PLACE.MOVING_STATE='" + moving_state + "'" +
                    " AND TABLE_TIME.TIME='" + time + "'" +
                    " AND TABLE_WEATHER.WEATHER='" + weather + "'" +
                    " AND TABLE_PLACE.NAME=TABLE_TIME.NAME AND TABLE_PLACE.NAME=TABLE_WEATHER.NAME", null);
        }
        if(c.getCount()==0) return null;
        c.moveToFirst();
        return c.getString(0);
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
