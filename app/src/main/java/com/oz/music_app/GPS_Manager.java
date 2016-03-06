package com.oz.music_app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by LG on 2016-03-06.
 */
public class GPS_Manager {
    Context context;
    myLocationListener mLocationListener;
    LocationManager mLocationManager;

    public GPS_Manager(Context context) {

        this.context = context;
        prepare_GPS();

    }

    //Toast형태로 위치정보를 받아 볼수 있도록 만든 함수.(보기용)
    public void show_location_info() {
        if(mLocationListener==null) prepare_GPS();
        Toast.makeText(context, mLocationListener.location_info(), Toast.LENGTH_SHORT).show();
    }

    //앱 종료시 같이 종료되어야 할 것들을 모아 둔 함수
    public void close_GPS_Manager() {
    }

    //혠ghkftjdghk
    private void prepare_GPS(){
        //LocationManager를 활성화 한다.
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "GPS가 작동하지 않습니다.", Toast.LENGTH_SHORT).show();
        } else {
            //밑에 있는 getLaskKnownLocation 함수를 쓰기위해 자동으로 추가된 구문들. 권한이 있는지 체크한다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
            }
            Location ll = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mLocationListener = new myLocationListener();
            //리스너에게 GPS및 네트워크를 이용해 위치정보를 수신하게 한다. (1초마다 또는 1m이상 움직일 경우)
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, mLocationListener);
        }

    }

    //위치정보를 수신하는 리스너, 위에서 설정한 requestLocationUpdates 함수에 의해 위치 변경이 있을 경우 받아온다.
    private class myLocationListener implements LocationListener{

        double location_latitude,location_longitude,location_altitude;
        float location_accuracy,location_speed,location_bearing;

        @Override
        public void onLocationChanged(Location location) {

            location_accuracy=location.getAccuracy();
            location_speed=location.getSpeed();
            location_bearing=location.getBearing();

            location_latitude=location.getLatitude();
            location_longitude=location.getLongitude();
            location_altitude=location.getAltitude();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch(status) {
                case GpsStatus.GPS_EVENT_STARTED:
                    //Toast.makeText(context, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    //Toast.makeText(context, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    //Toast.makeText(context, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    //Toast.makeText(context, "GPS_EVENT_SATELLITE_STATUS", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(context, provider + " Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(context, provider + " Disabled", Toast.LENGTH_SHORT).show();
        }
        //보기용 함수. 현재 위치정보를 String형태로 뿌려준다.
        public String location_info(){
            return "Longitude : " + Double.toString(location_longitude) + "\n"+
                    "Latitude : " + Double.toString(location_latitude) + "\n"+
                    "Altitude : " + Double.toString(location_altitude) + "\n"+
                    "Speed : " + Float.toString(location_speed) + "\n"+
                    "Accuracy : " + Float.toString(location_accuracy) + "\n"+
                    "Bearing : " + Float.toString(location_bearing) + "\n";
        }
    };

}
