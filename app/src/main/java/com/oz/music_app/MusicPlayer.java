package com.oz.music_app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MusicPlayer {

    public static final String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath()+"/Music_App/";

    MusicManager mMusicManager;
    private MediaPlayer m=new MediaPlayer();
    private MediaMetadataRetriever mmr=new MediaMetadataRetriever();

    ListView listView;
    private SeekBar seekBar;
    private ImageView jacket_ImageView;
    private ImageButton play_Button;
    ImageButton back_Button, forward_Button;
    private Context context;
    private TextView title, artist, time_View1, time_View2;
    private int cur;
    int song_duration;
    private android.os.Handler mHandler;
    private Drawable drawable_play_button, drawable_pause_button;
    //현재 음악 추천 상태인지 아닌지 판단.
    public boolean recommending=false;

    //음악 재생기의 생성자. 뷰처리도 여기서 다하게끔 만듬
    public MusicPlayer(Context context, ListView listView, final SeekBar seekBar, TextView timeView, TextView timeView2, ImageView imageView,
                       TextView title, TextView artist,
                       ImageButton play_Button, ImageButton back_Button, ImageButton forward_Button){

        mMusicManager=new MusicManager(context,listView);

        this.context=context;
        this.listView=listView;
        this.seekBar=seekBar;
        this.jacket_ImageView=imageView;
        this.play_Button=play_Button;
        this.back_Button=back_Button;
        this.forward_Button=forward_Button;
        this.title=title;
        this.artist=artist;
        this.time_View1=timeView;
        this.time_View2=timeView2;
        this.jacket_ImageView=imageView;

        this.mHandler=new android.os.Handler();
        this.play_Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                play();
            }
        });
        this.back_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        this.forward_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forward();
            }
        });

        listView.setSelected(true);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id) {
                mMusicManager.currentPosition = position;
                set_music(mMusicManager.currentPosition, true);

            }
        });

        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (m != null && fromUser) {
                    m.seekTo(progress);
                    time_View1.setText(new SimpleDateFormat("mm:ss").format(progress));
                    cur = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Activity activity=(Activity)context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (m != null) {
                    cur = m.getCurrentPosition();
                    seekBar.setProgress(cur);
                    time_View1.setText(new SimpleDateFormat("mm:ss").format(cur));
                }
                mHandler.postDelayed(this, 1000);
            }
        });

        //일단 켜자마자 첫번째 노래를 로드상태로 만들어 놓음
        set_music(0, false);
        cur=0;
    }

    public int get_music_position(String title, boolean extension_included){
        if (extension_included){
            return mMusicManager.find_position_of_Song(title);
        }else{
            return mMusicManager.find_position_of_Song(title+".mp3");
        }
    }
    public void set_music_position(int position){
        mMusicManager.set_position_of_Song(position);
    }
    //어떤 노래를 재생할지 세팅하는 함수, just_play가 true 일 경우 세팅후 즉시 재생한다.
    public void set_music(int position,boolean just_play) {
        try {
            String song_path=mMusicManager.set_Song(position);
            m.reset();
            m.setDataSource(song_path);
            m.prepare();

            //곡 정보를 받아와서 textview 에 표시
            mmr.setDataSource(song_path);
            String temp=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (temp==null || temp.equals("")){
                artist.setText("Various Artist");
            }
            else{
                artist.setText(temp);
            }
            temp=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if(temp==null || temp.equals("")){
                title.setText(mMusicManager.get_song_title());
            }
            else{
                title.setText(temp);
            }
            byte[] image=mmr.getEmbeddedPicture();
            if(image!=null){
                jacket_ImageView.setImageBitmap(BitmapFactory.decodeByteArray(image,0,image.length));
            }
            else{
                jacket_ImageView.setImageDrawable(null);
            }

            //곡 길이 정보를 받아와서 textview에 뿌려준다
            cur=0;
            song_duration=Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            time_View1.setText(new SimpleDateFormat("mm:ss").format(cur));
            time_View2.setText(new SimpleDateFormat("mm:ss").format(song_duration));
            seekBar.setMax(song_duration);
            seekBar.setProgress(0);

            if(just_play) play();
            //곡이 끝까지 재생 되고 나서 작동하는 리스너
            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //만약 곡 추천중이라면,, 한곡 끝나고 곡추천 루틴 재가동
                    if(recommending){
                        ((MainActivity)context).play_recommended_song();
                    }
                    //아니라면 그냥 다음곡을 불러와서 재생시킨다.
                    else {
                        set_music(mMusicManager.set_NextSong(),true);
                    }
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
            if(m.isPlaying()) play_Button.setImageResource(R.drawable.button_pause);
        }
        else {
            m.pause();
            play_Button.setImageResource(R.drawable.button_play);
        }
    }

    //추천음악 전용 리스트로 변경 할경우 인자를 true로
    public void set_songlist(boolean for_recommending, Cursor c)
    {
        if(for_recommending)
            mMusicManager.update_songList_from_query_result(c);
        else
            mMusicManager.update_SongList_from_storage();
    }

    public String get_song_title(){
        return mMusicManager.get_song_title();
    }

    public void forward(){
        set_music(mMusicManager.set_NextSong(),true);
    }
    public void back() {
        set_music(mMusicManager.set_PreviousSong(),true);
    }
    public void stop(){
        m.stop();
    }
    //앱 종료시 호출할 것들을 모아 주세요
    public void close_Player(){
        m.release();
    }


    /**********************************************************

    **********************************************************/
    private class MusicManager {

        Context context;
        public int currentPosition = 0;
        private ArrayList<String> songs = new ArrayList<String>();
        private ListView listView;
        private View.OnLayoutChangeListener m_OnLayoutChangeListener;

        public MusicManager(Context context,ListView v){
            this.context=context;
            listView=v;
            update_SongList_from_storage();
        }

        /*내부 저장소에 담긴 곡들을 어댑터에 업데이트 하는 함수*/
        public void update_SongList_from_storage() {
            File home = new File(MEDIA_PATH);
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                if (!home.exists()) {
                    home.mkdirs();
                }
                if (home.listFiles(new Mp3Filter()).length > 0) {
                    songs.clear();
                    for (File file : home.listFiles(new Mp3Filter())) {
                        songs.add(file.getName());
                    }
                    listView.setAdapter(new CustomAdapter(songs));
                }
            }
        }

        /*쿼리 결과 커서에 담긴 악곡 이름을 어댑터에 업데이트 하는 함수*/
        public void update_songList_from_query_result(Cursor c){
            int number_of_songs=c.getCount();
            if(number_of_songs==0) return;
            else{
                songs.clear();
                do{
                    songs.add(c.getString(0)+".mp3");
                }while(c.moveToNext());
                listView.setAdapter(new CustomAdapter(songs));
            }
        }

        public int find_position_of_Song(String title){
            return songs.indexOf(title);
        }
        public void set_position_of_Song(int position){
            currentPosition=position;
        }

        public int set_NextSong(){
            if(++currentPosition>=songs.size()){
                currentPosition=0;
            }
            return currentPosition;
        }

        public int set_PreviousSong(){
            if(--currentPosition<0){
                currentPosition=songs.size()-1;
            }
            return currentPosition;
        }

        public String set_Song(int position){
            currentPosition=position;

            final TextView tv = (TextView) listView.getChildAt(position-listView.getFirstVisiblePosition());
            if(tv!=null) {
                tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                tv.setSelected(true);
               // tv.requestFocus();
                //Log.e("OZOZ", "setSong");
                m_OnLayoutChangeListener= new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        //Log.e("OZOZ", "onLayoutChange : " + v.toString() + ", " + v.getId() + ", " + left + ", " + right + ", " + top + ", " + bottom + ", " + oldLeft + ", " + oldRight + ", " + oldTop + ", " + oldBottom);
                        for(int i=0;i<listView.getLastVisiblePosition()-listView.getFirstVisiblePosition();i++){
                            TextView _v=(TextView)listView.getChildAt(i);
                            if(_v!=null){
                                _v.setEllipsize(TextUtils.TruncateAt.END);
                                _v.setSelected(false);
                            }
                        }
                        tv.setSelected(true);
                        tv.requestFocus();
                    }
                };
                tv.removeOnLayoutChangeListener(m_OnLayoutChangeListener);
                tv.addOnLayoutChangeListener(m_OnLayoutChangeListener);
            }
            return MEDIA_PATH+songs.get(currentPosition);
        }
        /* 현재 재생중인 곡의 이름을 불러오는 함수 */
        public String get_song_title(){
            return songs.get(currentPosition);
        }

        /*
        *
        */
        private class Mp3Filter implements FilenameFilter {
            public boolean accept(File dir, String name){
                return (name.endsWith(".mp3"));
            }
        }
        /*
        *
        */
        private class CustomAdapter extends BaseAdapter{
            private ArrayList<String> m_List;
            private class CustomHolder{
                TextView m_TextView;
            }
            public CustomAdapter(ArrayList<String> ar){
                m_List=ar;
            }

            @Override
            public int getCount() {
                return m_List.size();
            }

            @Override
            public String getItem(int position) {
                return m_List.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final int pos = position;
                final Context context = parent.getContext();

                TextView text=null;
                CustomHolder holder=null;

                if ( convertView == null ) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.main_list_textview, parent, false);

                    text = (TextView) convertView.findViewById(R.id.main_list_textview);
                    holder=new CustomHolder();
                    holder.m_TextView=text;
                    convertView.setTag(holder);
                }
                else{
                    holder= (CustomHolder) convertView.getTag();
                    text=holder.m_TextView;
                }
                text.setText(m_List.get(pos));
                return convertView;
            }
            public void add(String text){
                m_List.add(text);
            }

            public void remove(int pos){
                m_List.remove(pos);
            }
        }
    }

}
