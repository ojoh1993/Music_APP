package com.oz.music_app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

public class MainActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 4;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 5;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 6;

    private MusicPlayer mMusicPlayer;
    private GPS_Manager mGPS_Manager;
    private Weather_Manager mWeather_Manager;
    private DbAdapter mDbAdapter;

    public ListView listView;
    public SeekBar seekBar;
    public ImageView jacket_ImageView;
    public ImageButton play_Button, back_Button, forward_Button;
    public Button recommend_button;
    public TextView time_View, time_View2, song_Title_View, song_Artist_View;

    @Override
    public void onCreate(Bundle SavedInstance){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) check_permissions();
        super.onCreate(SavedInstance);
        setContentView(R.layout.layout_main);

        listView=(ListView)findViewById(R.id.main_list);
        seekBar=(SeekBar)findViewById(R.id.main_seekBar);
        jacket_ImageView=(ImageView)findViewById(R.id.main_jacket_image_view);
        play_Button=(ImageButton)findViewById(R.id.main_play_button);
        back_Button=(ImageButton)findViewById(R.id.main_back_button);
        forward_Button=(ImageButton)findViewById(R.id.main_forward_button);
        //gps_Button=(Button)findViewById(R.id.main_etc_button1);
        recommend_button=(Button)findViewById(R.id.main_etc_button2);
        song_Title_View=(TextView)findViewById(R.id.main_song_title);
        song_Artist_View=(TextView)findViewById(R.id.main_song_artist);
        time_View=(TextView)findViewById(R.id.main_timeTextView);
        time_View2=(TextView)findViewById(R.id.main_timeTextView2);

        song_Artist_View.setSelected(true);
        song_Title_View.setSelected(true);

        mMusicPlayer=new MusicPlayer(this,listView,seekBar,time_View,time_View2,jacket_ImageView,song_Title_View,song_Artist_View,play_Button,back_Button,forward_Button);
        mGPS_Manager=new GPS_Manager(this);
        mWeather_Manager = new Weather_Manager(this);
        mDbAdapter = new DbAdapter(this).open();

        /*gps_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGPS_Manager.show_location_info();
            }
        });*/
        recommend_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                seton_recommend_song();
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mMusicPlayer.close_Player();
        mGPS_Manager.close_GPS_Manager();
        //앱을 완전히 종료하는데 필요하다는데...?
        ActivityCompat.finishAffinity(this);
        System.runFinalization();
        System.exit(0);
    }

    private void seton_recommend_song(){
        mMusicPlayer.recommending=(!mMusicPlayer.recommending);
        if(mMusicPlayer.recommending) {
           int a=play_recommended_song();
           if(a==0) {
               recommend_button.setText(R.string.recommendation_button2);
           }else{
               String temp=mMusicPlayer.get_song_title();
               mMusicPlayer.set_songlist(false, null);
               int d=mMusicPlayer.get_music_position(temp,true);
               mMusicPlayer.set_music_position(d);
               recommend_button.setText(R.string.recommendation_button);
           }
        }
        else if (!mMusicPlayer.recommending){
            String temp=mMusicPlayer.get_song_title();
            mMusicPlayer.set_songlist(false, null);
            int d=mMusicPlayer.get_music_position(temp,true);
            mMusicPlayer.set_music_position(d);
            recommend_button.setText(R.string.recommendation_button);
        }
    }

    public int play_recommended_song(){

        String weather=null;
        weather = mWeather_Manager.get_weather_info_in_String();
        if(weather==null) return -1;
        //날씨 매니저에서 날씨 정보 문자열을 get

        double lat=mGPS_Manager.get_latitude(),
                lon=mGPS_Manager.get_longitude();
        if(lat==-1 || lon==-1) return -2;
        //GPS 매니저에서 위도 경도 정보를 얻음

        GregorianCalendar today=new GregorianCalendar();
        int hour = today.get(Calendar.HOUR_OF_DAY);
        String time;
        if(hour>=21 || hour<=4) time="밤";
        else if(5<=hour && hour<=9) time="아침";
        else if(10<=hour && hour<=17) time="낮";
        else if(18<=hour && hour<=20) time="저녁";
        else time="밤";
        //현재 시간도 받아오고

        double speed=mGPS_Manager.get_speed();
        String moving_state;
        if(speed==-1) return -3;
        else if(speed>1.5) moving_state="교통";
        else if(speed>0.5) moving_state="걸음";
        else moving_state="정지";
        //현재 속도도 받아오고

        String place = mDbAdapter.get_PlaceName(lon,lat);

        Toast.makeText(this, place+","+weather+","+time+","+moving_state,Toast.LENGTH_SHORT).show();

        //DB상에 현재 장소가 있는지 확인
        Log.d("OZ",place+", "+weather+", "+hour+", "+time+", "+moving_state+", ");
        Cursor songs=mDbAdapter.get_Recommended_Songs_FromDatabase(place, time, weather, moving_state);
        //데이터베이스에 rawQuery를 던져봅니다.

        if(songs!=null && songs.getCount()>0){
            mMusicPlayer.set_songlist(true,songs);
            if(songs.moveToPosition(new Random().nextInt(songs.getCount()))){
                //Log.d("OZOZ",mMusicPlayer.get_music_position(songs.getString(0))+", "+songs.getString(0));
                mMusicPlayer.set_music(mMusicPlayer.get_music_position(songs.getString(0),false), true);
                return 0;
            }
            else{
             return -4;
            }
        }
        else{
            Toast.makeText(this,"조건에 맞는 곡이 없습니다.",Toast.LENGTH_SHORT).show();
            return 1;
        }
    }

    private void check_permissions(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE
                    },
                    MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET
                    },
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }

    }
}
