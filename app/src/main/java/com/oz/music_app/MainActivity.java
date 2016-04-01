package com.oz.music_app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by LG on 2016-03-05.
 */
public class MainActivity extends Activity {

    private MusicPlayer mMusicPlayer;
    private GPS_Manager mGPS_Manager;
    private Weather_Manager mWeather_Manager;

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

        gps_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGPS_Manager.show_location_info();
            }
        });
        test_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeather_Manager.get_Weather_info();
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mMusicPlayer.close_Player();
        mGPS_Manager.close_GPS_Manager();
    }

}
