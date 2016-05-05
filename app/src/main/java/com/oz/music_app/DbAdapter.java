package com.oz.music_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by LG on 2016-04-01.
 */
public class DbAdapter {


    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "";
    private static final String TAG="DbAdapter";
    private final Context mContext;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    public DbAdapter(Context context){

        this.mContext=context;

    }

    public DbAdapter open() throws SQLException{
        mDbHelper=new DatabaseHelper(mContext);
        mDb=mDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        mDbHelper.close();
    }

























    private class DatabaseHelper extends SQLiteOpenHelper{

        private static final java.lang.String DATABASE_CREATE = "";

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
