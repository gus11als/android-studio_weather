package com.example.weathergps3timeloc;


import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "46d2ef6c9b25d4e43117533a1343af98";
    private static final String API_BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private TextView weatherTextView;
    private TextView forecastTextView;
    private TextView locationTextView;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        weatherTextView = findViewById(R.id.weatherTextView);
        forecastTextView = findViewById(R.id.forecastTextView);
        locationTextView = findViewById(R.id.locationTextView);
        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWeather();
            }
        });



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String apiUrl = API_BASE_URL + "weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
                FetchWeatherTask weatherTask = new FetchWeatherTask();
                weatherTask.execute(apiUrl);

                String forecastApiUrl = API_BASE_URL + "forecast?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
                FetchForecastTask forecastTask = new FetchForecastTask();
                forecastTask.execute(forecastApiUrl);

                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String locality = address.getLocality();
                        String adminArea = address.getAdminArea();
                        String countryName = address.getCountryName();

                        String locationText = String.format(Locale.getDefault(), "내 위치: %s, %s, %s", locality, adminArea, countryName);
                        locationTextView.setText(locationText);
                    } else {
                        // 위치 정보를 가져오지 못했을 때의 처리
                        locationTextView.setText("위치 정보를 가져오지 못했습니다.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // 위치 권한 요청 코드 추가
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void refreshWeather() {
        // 현재 위치를 다시 가져옴
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();

                String apiUrl = API_BASE_URL + "weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
                FetchWeatherTask weatherTask = new FetchWeatherTask();
                weatherTask.execute(apiUrl);

                String forecastApiUrl = API_BASE_URL + "forecast?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
                FetchForecastTask forecastTask = new FetchForecastTask();
                forecastTask.execute(forecastApiUrl);

                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String locality = address.getLocality();
                        String adminArea = address.getAdminArea();
                        String countryName = address.getCountryName();

                        String locationText = String.format(Locale.getDefault(), "지역: %s, %s, %s", locality, adminArea, countryName);
                        locationTextView.setText(locationText);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String apiUrl = urls[0];
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject main = jsonObject.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    double humidity = main.getDouble("humidity");
                    JSONArray weatherArray = jsonObject.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    String weatherDescription = weatherObject.getString("description");

                    double celsius = temperature - 273.15;
                    String weatherText = String.format(Locale.getDefault(), "현재온도: %.1f°C\n현재습도: %.1f%%\n현재날씨: %s", celsius, humidity, weatherDescription);
                    weatherTextView.setText(weatherText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                weatherTextView.setText("Failed to fetch weather");
            }
        }

    }

    private class FetchForecastTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            String apiUrl = urls[0];
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray list = jsonObject.getJSONArray("list");

                    StringBuilder forecastText = new StringBuilder();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    for (int i = 0; i < list.length(); i++) {
                        JSONObject item = list.getJSONObject(i);
                        long timestamp = item.getLong("dt");
                        double temperature = item.getJSONObject("main").getDouble("temp");
                        double humidity = item.getJSONObject("main").getDouble("humidity");
                        JSONObject weather = item.getJSONArray("weather").getJSONObject(0);
                        String weatherDescription = weather.getString("description");

                        calendar.setTimeInMillis(timestamp * 1000);
                        String date = sdf.format(new Date(timestamp * 1000));
                        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":00";

                        double celsius = temperature - 273.15;
                        String forecastInfo = String.format(Locale.getDefault(), "날짜: %s\n시간: %s\n온도: %.1f°C\n습도: %.1f%%\n날씨: %s\n\n",
                                date, time, celsius, humidity, weatherDescription);
                        forecastText.append(forecastInfo);
                    }

                    forecastTextView.setText(forecastText.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //forecastTextView.setText("Failed to fetch forecast");
            }
        }
    }
}
