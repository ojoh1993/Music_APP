package com.oz.music_app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * Created by LG on 2016-03-21.
 */
public class Weather_Manager {

    private class mAsyncTask extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... arg0) {
            try {
                return (String)downloadUrl((String)arg0[0]);
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "The msg is : " + e.getMessage());
                return "download failed";
            }
        }
    }
    /*
    * 좌표 변환 식 자바 버전
    */
    private class Lamc_parameter {
        float  Re;          /* 사용할 지구반경 [ km ]      */
        float  grid;        /* 격자간격        [ km ]      */
        float  slat1;       /* 표준위도        [degree]    */
        float  slat2;       /* 표준위도        [degree]    */
        float  _olon;        /* 기준점의 경도   [degree]    */
        float  _olat;        /* 기준점의 위도   [degree]    */
        float  xo;          /* 기준점의 X좌표  [격자거리]  */
        float  yo;          /* 기준점의 Y좌표  [격자거리]  */

        double  re, olon, olat, sn, sf, ro;

        public Lamc_parameter(){

            Re    = 6371.00877f;     // 지도반경
            grid  = 5.0f;            // 격자간격 (km)
            slat1 = 30.0f;           // 표준위도 1
            slat2 = 60.0f;           // 표준위도 2
            _olon  = 126.0f;          // 기준점 경도
            _olat  = 38.0f;           // 기준점 위도
            xo    = 210f/map.grid;   // 기준점 X좌표
            yo    = 675f/map.grid;   // 기준점 Y좌표

            re = Re/grid;
            slat1 =(float)Math.toRadians(slat1);
            slat2 = (float)Math.toRadians(slat2);
            olon = Math.toRadians(_olon);
            olat = Math.toRadians(_olat);

            sn = Math.tan(Math.PI*0.25 + slat2*0.5)/Math.tan(Math.PI*0.25 + slat1*0.5);
            sn = Math.log(Math.cos(slat1)/Math.cos(slat2))/Math.log(sn);
            sf = Math.tan(Math.PI*0.25 + slat1*0.5);
            sf = Math.pow(sf,sn)*Math.cos(slat1)/sn;
            ro = Math.tan(Math.PI*0.25 + olat*0.5);
            ro = re*sf/Math.pow(ro,sn);
        }
    }

    private class Coordinate_XY{
        float X,Y;
    }

    String strUrl="http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastGrib?ServiceKey=dU6gHB3IpqNP4G2linHrhdoxy22nmeoDDMHgbfQBiD8XFk6yKXsKlYXF1QpVGlmnAbPUUttMhY6vZsyTshJh6A%3D%3D"+"&base_date=20160322&base_time=0600&nx=55&ny=127";

    private final String DEBUG_TAG="DEBUG";
    private Context context;
    private Lamc_parameter map;

    public Weather_Manager(Context context){
        this.context=context;

        map=new Lamc_parameter();

    }




    public void get_Weather_info(){
        try {
            if (strUrl != null && strUrl.length() > 0) {
                ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    new mAsyncTask().execute(strUrl);        // html 다운로드 쓰레드 기동
                } else {
                    throw new Exception("network error");
                }
            } else {
                throw new Exception("bad url");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        InputStream is = null;
        Reader reader = null;
        int len = 5000;
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int resp = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " +resp);
            is = conn.getInputStream();
            reader = new InputStreamReader(is, "UTF-8");
            char[] buff = new char[len];
            reader.read(buff);
            reader.close();
            is.close();
            Log.d("OZ",new String(buff));
            return new String(buff);
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
    }


    /*위,경도를 입력으로 받고 날씨정보용 좌표를 출력 하는 함수*/
    private Coordinate_XY map_conv (float lon,float lat)
    {
        Coordinate_XY coord=new Coordinate_XY();

        double  ra, theta;

        ra = Math.tan(Math.PI*0.25+(lat)*Math.toRadians(0.5));
        ra = map.re*map.sf/Math.pow(ra,map.sn);
        theta =Math.toRadians (lon)- map.olon;
        if (theta >  Math.PI) theta -= 2.0*Math.PI;
        if (theta < -Math.PI) theta += 2.0*Math.PI;
        theta *= map.sn;
        coord.X = (float)(ra*Math.sin(theta)) + (map).xo;
        coord.Y = (float)(map.ro - ra*Math.cos(theta)) + (map).yo;


        coord.X = (int)(coord.X + 1.5);
        coord.Y = (int)(coord.Y + 1.5);

        return coord;
    }

}
