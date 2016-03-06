package com.oz.music_app;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LG on 2016-03-06.
 */
public class MusicPlayer {

    public static final String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath()+"/Music_App/";

    MusicManager mMusicManager;
    private MediaPlayer m=new MediaPlayer();

    private ListView listView;
    private SeekBar seekBar;
    private ImageView jacket_ImageView;
    private Button play_Button, back_Button, forward_Button;
    private Context context;

    //음악 재생기의 생성자. 뷰처리도 여기서 다하게끔 만듬
    public MusicPlayer(Context context, ListView listView, SeekBar seekBar,ImageView imageView,
                       Button play_Button, Button back_Button, Button forward_Button){

        mMusicManager=new MusicManager(context,listView);

        this.context=context;
        this.listView=listView;
        this.seekBar=seekBar;
        this.jacket_ImageView=imageView;
        this.play_Button=play_Button;
        this.back_Button=back_Button;
        this.forward_Button=forward_Button;

        this.play_Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                play();
            }
        });
        this.back_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        this.forward_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMusicManager.currentPosition = position;
                set_music(mMusicManager.get_CurrentSong(),true);
            }
        });

        set_music(mMusicManager.get_CurrentSong(),false);
    }

    //어떤 노래를 재생할지 세팅하는 함수, just_play가 true 일 경우 세팅후 즉시 재생한다.
    public void set_music(String song_path,boolean just_play) {
        try {
            m.reset();
            m.setDataSource(song_path);
            m.prepare();
            if(just_play) play();
            //곡이 끝까지 재생 되고 나서 작동하는 리스너
            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //다음곡을 불러와서 재생시킨다.
                    set_music(mMusicManager.get_NextSong(),true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //음악을 재생 시키는 함수
    public void play(){
        if(!m.isPlaying()) {
            m.start();
            play_Button.setText(R.string.pause);
        }
        else {
            m.pause();
            play_Button.setText(R.string.play);
        }
    }

    public void forward(){
        set_music(mMusicManager.get_NextSong(),true);
    }
    public void back(){
        set_music(mMusicManager.get_PreviousSong(),true);
    }

    public void stop(){
        m.stop();
    }

    //앱 종료시 호출할 것들을 모아 주세요
    public void close_Player(){
        m.release();
    }


    /**********************************************************/
    /**********************************************************/

    private class MusicManager {

        Context context;
        public int currentPosition = 0;
        private List<String> songs = new ArrayList<String>();
        ListView listView;


        public MusicManager(Context context,ListView v){
            this.context=context;
            listView=v;
            update_SongList();
        }
    /*
    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getAction();
        if (name.equals("com.oz.music_app.MusicManager.sendreciver.next")){

        }
    }
    */

        public void update_SongList(){
            File home=new File(MEDIA_PATH);
            if(home.listFiles(new Mp3Filter()).length>0){
                for(File file:home.listFiles(new Mp3Filter())){
                    songs.add(file.getName());
                }
            }
            ArrayAdapter<String> songList = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,songs);
            listView.setAdapter(songList);
        }

        public String get_NextSong(){
            if(++currentPosition>=songs.size()){
                currentPosition=0;
            }
            return MEDIA_PATH+songs.get(currentPosition);
        }
        public String get_PreviousSong(){
            if(--currentPosition<0){
                currentPosition=songs.size();
            }
            return MEDIA_PATH+songs.get(currentPosition);
        }
        public String get_CurrentSong(){
            return MEDIA_PATH+songs.get(currentPosition);
        }

        private class Mp3Filter implements FilenameFilter {
            public boolean accept(File dir, String name){
                return (name.endsWith(".mp3"));
            }
        }

    }
}