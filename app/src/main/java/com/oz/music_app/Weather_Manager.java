package com.oz.music_app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by LG on 2016-03-21.
 */
public class Weather_Manager {
    GPS_Manager gps;
    private final String DEBUG_TAG="DEBUG";
    private Context context;
    private Lamc_parameter map;

    private class mAsyncTask extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... arg0) {
            try {
                String weather_info = (String)downloadUrl((String)arg0[0]);
               /* InputSource is = new InputSource(new StringReader(weather_info));
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
*/
                Document document=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(arg0[0]);
                //XPath xPath= XPathFactory.newInstance().newXPath();

                //String expression ="//*/item";
                //NodeList cols = (NodeList)xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
                NodeList nodes=document.getElementsByTagName("item");
                for(int i=0;i<nodes.getLength();i++){
                    NodeList item_nodelist=nodes.item(i).getChildNodes();
                    //이렇게 사용하는건 DOM방식 인것 같다.
                    String category=item_nodelist.item(2).getTextContent();
                    String value;
                    //강수:PTY, 기온:T1H, 하늘:SKY에 한해서만 확인
                    if(category.equals("PTY")
                            ||category.equals("T1H")
                            ||category.equals("SKY")){

                        value=item_nodelist.item(5).getTextContent();
                        Log.d("OZ",category);
                        Log.d("OZ",value);
                    }

                }
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "The msg is : " + e.getMessage());
                return "download failed";
            } catch (SAXException e) {//.newInstance().newDocumentBuilder().parse(is) 여기서 걸림
                e.printStackTrace();
                return "parse failed";
            } catch (ParserConfigurationException e) {//DocumentBuilderFactory.newInstance().newDocumentBuilder() 에서 걸림
                e.printStackTrace();
                return "document build failed";
            } /*catch (XPathExpressionException e) {//xPath.compile 에서 걸림
                e.printStackTrace();
                return "XPath compile failed";
            }*/
            return "download & parse success";
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
            xo    = 210f/grid;   // 기준점 X좌표
            yo    = 675f/grid;   // 기준점 Y좌표

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

    public Weather_Manager(Context context){
        this.context=context;
        gps=new GPS_Manager(context);
        map=new Lamc_parameter();
    }

    public void get_Weather_info(){

        if (!gps.is_location_info_received_successfully()) return;

        //날짜정보 입력
        GregorianCalendar today=new GregorianCalendar();

        int minute = today.get(Calendar.MINUTE);

        if(minute<=40){
            today.add(Calendar.HOUR, -1);

        }
        today.set(Calendar.MINUTE,0);

        String DATE = new SimpleDateFormat("yyyyMMdd").format(today.getTime());
        String HOUR = new SimpleDateFormat("HH").format(today.getTime());
        String MINUTE = new SimpleDateFormat("mm").format(today.getTime());

        //
        Coordinate_XY coord=map_conv(gps.get_longitude(),gps.get_latitude());

        String strUrl="http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastGrib?" +
                "ServiceKey=dU6gHB3IpqNP4G2linHrhdoxy22nmeoDDMHgbfQBiD8XFk6yKXsKlYXF1QpVGlmnAbPUUttMhY6vZsyTshJh6A%3D%3D"+
                "&base_date="+ DATE +
                "&base_time="+HOUR+MINUTE+
                "&nx="+(int)coord.X+
                "&ny="+(int)coord.Y;
        Log.d("OZ",HOUR+MINUTE);
        Log.d("OZ",strUrl);

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
