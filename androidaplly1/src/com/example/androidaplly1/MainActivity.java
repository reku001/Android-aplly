package com.example.androidaplly1;

import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LocationManager locationManager = null;	
	private	boolean	button1Clicked = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(mButton1Listener);
		Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(mButton1Listener);
	}

	private OnClickListener mButton1Listener = new OnClickListener() {
		public void onClick(View v) {
			//　ボタンのIDを取得
			int id = v.getId();
			if(id == R.id.button1){
				button1Clicked = true;
			} else {
				button1Clicked = false;
			}
			if (locationManager != null) {
	        	locationManager.removeUpdates(mLocationListener);
	        }
        	locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0,mLocationListener);
        }
	};
	
	private  LocationListener mLocationListener  = new LocationListener() {
        public void onStatusChanged(String provider, int status,
                Bundle extras) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onProviderDisabled(String provider) {
        }
        public void onLocationChanged(Location location) {
        	String latitude = Double.toString(location.getLatitude());
        	String longitude = Double.toString(location.getLongitude());
            // 1回しか呼ばない
            locationManager.removeUpdates(mLocationListener);
            if(button1Clicked){
            	yahooMap(latitude,longitude);
            } else {
            	getReuest(latitude,longitude);
            }
        }
    };

	private  void  yahooMap(String latitude,String longitude) {
		String urlStrng = "http://map.yahoo.co.jp/maps?type=scroll&pointer=on&sc=2&lat=" + latitude;
		urlStrng += "&lon=" + longitude;

		Intent intent = null;
        intent = new Intent(Intent.ACTION_VIEW,Uri.parse(urlStrng));
    	startActivity(intent); 
	}

	private	void	getReuest(String latitude,String longitude)
	{
		String requestURL = "http://api.openweathermap.org/data/2.5/forecast/daily?lat=" + latitude + "&lon=" + longitude
			 +"&xmode=json&cnt=1";
		Task task = new Task();
        task.execute(requestURL);
	}

	protected class Task extends AsyncTask<String, String, String>
	{
        @Override
        protected String doInBackground(String... params)
        {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(params[0]);
            byte[] result = null;
            String rtn = "";
            try{
                HttpResponse response = client.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpURLConnection.HTTP_OK){
                    result = EntityUtils.toByteArray(response.getEntity());
                    rtn = new String(result, "UTF-8");
                }
            }
            catch (Exception e) {
            }
            return rtn;
        }
        
        @Override
        protected void onPostExecute(String result)
        {
			try {
				JSONObject json = new JSONObject(result);
				JSONObject obj = json.getJSONObject("city");
				String cityName = obj.getString("name");
				JSONArray listArray = json.getJSONArray("list");
				JSONObject obj2 = listArray.getJSONObject(0);
				JSONObject mainObj = obj2.getJSONObject("temp");
				float currentTemp = (float) (mainObj.getDouble("day") - 273.15f);
				String ct = currentTemp + "度";
				JSONArray weatherArray = obj2.getJSONArray("weather");
				String weather = weatherArray.getJSONObject(0)
						.getString("main");
				String temp = "場所(" + cityName + ") / 現在温度(" + ct + "度)";
				temp += " / 天気（" + weather + ")";
				Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_LONG)
						.show();
            }
            catch (JSONException e) {
            	Toast.makeText(getApplicationContext(), "error!!!", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
