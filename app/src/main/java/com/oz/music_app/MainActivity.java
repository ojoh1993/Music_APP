package com.oz.music_app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by LG on 2016-03-05.
 */
public class MainActivity extends Activity {

    private MusicPlayer mMusicPlayer;
    private GPS_Manager mGPS_Manager;
    private Weather_Manager mWeather_Manager;
    private DbAdapter mDbAdapter;

    public Context this_context;
    public ListView listView;
    public SeekBar seekBar;
    public ImageView jacket_ImageView;
    public Button play_Button, back_Button, forward_Button, gps_Button, test_Button;
    public TextView time_View, song_Title_View;

    @Override
    public void onCreate(Bundle SavedInstance){
        super.onCreate(SavedInstance);
        setContentView(R.layout.layout_main);

        this_context=this;
        listView=(ListView)findViewById(R.id.main_list);
        seekBar=(SeekBar)findViewById(R.id.main_seekBar);
        jacket_ImageView=(ImageView)findViewById(R.id.main_jacket_image_view);
        play_Button=(Button)findViewById(R.id.main_play_button);
        back_Button=(Button)findViewById(R.id.main_back_button);
        forward_Button=(Button)findViewById(R.id.main_forward_button);
        gps_Button=(Button)findViewById(R.id.main_etc_button1);
        test_Button=(Button)findViewById(R.id.main_etc_button2);

        mMusicPlayer=new MusicPlayer(this,listView,seekBar,jacket_ImageView,play_Button,back_Button,forward_Button);
        mGPS_Manager=new GPS_Manager(this);
        mWeather_Manager = new Weather_Manager(this);
        mDbAdapter = new DbAdapter(this).open();

        gps_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGPS_Manager.show_location_info();
            }
        });
        test_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                play_recommended_song();
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


    private void play_recommended_song(){

        String weather=null;
        weather = mWeather_Manager.get_weather_info_in_String();
        if(weather==null) return;
        //날씨 매니저에서 날씨 정보 문자열을 get

        double lat=mGPS_Manager.get_latitude(),
                lon=mGPS_Manager.get_longitude();
        if(lat==-1 || lon==-1) return;
        //GPS 매니저에서 위도 경도 정보를 얻음

        GregorianCalendar today=new GregorianCalendar();
        int hour = today.get(Calendar.HOUR_OF_DAY);
        String time;
        if(hour>=21 || hour<=4) time="밤";
        else if(05<=hour && hour<=9) time="아침";
        else if(10<hour && hour<=15) time="낮";
        else if(hour>=16 && hour<=20) time="저녁";
        else time="밤";
        //현재 시간도 받아오고

        double speed=mGPS_Manager.get_speed();
        String moving_state;
        if(speed==-1) return;
        else if(speed>1.5) moving_state="교통";
        else if(speed>0.5) moving_state="걸음";
        else moving_state="정지";
        //현재 속도도 받아오고

        String place = mDbAdapter.get_PlaceName(lon,lat);
        //DB상에 현재 장소가 있는지 확인
        Log.d("OZ",place+", "+weather+", "+time+", "+moving_state+", ");
        String song_title=mDbAdapter.get_Recommended_SongTitle_FromDatabase(place, time, weather, moving_state);
        Log.d("OZ",song_title+"");
        //데이터베이스에 rawQuery를 던져봅니다.

        if(song_title!=null) mMusicPlayer.set_music(MusicPlayer.MEDIA_PATH+song_title+".mp3",true);
        else Toast.makeText(this,"조건에 맞는 곡이 없습니다.",Toast.LENGTH_SHORT).show();


    }

}
