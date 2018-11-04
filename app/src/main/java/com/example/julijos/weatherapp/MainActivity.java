package com.example.julijos.weatherapp;


import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements AlertDialogChangeCity.AlertDialogListener{

    ImageView imageViewIcon;
    TextView textViewCity, textViewDesc, textViewTemp;
    private ListView cityListView;



    //API-URLs and API-key to Openweathermap API
    private String apiKey = "5ffcfd078c6933d6a3e7eb281727fa75";
    private String apiChangeCityUrl = "https://api.openweathermap.org/data/2.5/weather?q=";//change city by name
    private String apiCityByCoord = "https://api.openweathermap.org/data/2.5/weather?lat=";//get city by coordinates

    String changeCityName ="";

    String latitude="", longitude="";

    String icon = "";
    String desc = "";
    String cityName = "";
    double tempCelsius;



    //Replaces special-charachters (like åäö, and space) in the cityname
    public void applyText(String cityName) {
        changeCityName = cityName;
        Log.i("ChangeCity:  ", "applyText: " + cityName);
        Log.i("UPLOAD TO DATABASE", "databaseReference  " + changeCityName);
        changeCityName = changeCityName.replaceAll("Göteborg", "Gothenburg");
        changeCityName = changeCityName.replaceAll("göteborg", "Gothenburg");
        changeCityName = changeCityName.replaceAll(" ", "+");
        changeCityName = changeCityName.replaceAll("ö", "o");
        changeCityName = changeCityName.replaceAll("ä","a");
        changeCity();

    }

/*    public void formatCityName(String cityName){
        changeCityName = cityName;
        Log.i("ChangeCity:  ", "applyText: " + cityName);
        //Replace non-acceptable charachters
        changeCityName = changeCityName.replaceAll(" ", "+");
        changeCityName = changeCityName.replaceAll("ö", "o");
        changeCityName = changeCityName.replaceAll("ä","a");
        changeCity();
    }*/


    //Downloading data from the API Using a Backround Thread
    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try{
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream in = httpURLConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1){
                    char current = (char) data;

                    result += current;

                    data = reader.read();
                }
                return result;
            }
            catch (Exception e){
                e.printStackTrace();
                return "Failed";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            String icon = "";
           // String desc = "";

            try {
                JSONObject jsonObject = new JSONObject(result);
                String weatherInfo = jsonObject.getString("weather");
                JSONObject jsonPart = null;
                JSONArray array = new JSONArray(weatherInfo);

                for(int i = 0 ; i < array.length(); i++){
                    jsonPart = array.getJSONObject(i);
                    icon = jsonPart.getString("icon");
                    desc = jsonPart.getString("description");
                }

                JSONObject main = jsonObject.getJSONObject("main");
                double tempKelvin = main.getInt("temp");
                tempCelsius = tempKelvin-273;
                cityName = jsonObject.getString("name");

                if(cityName != "" && String.valueOf(tempCelsius)!= "" && desc !=""){
                    Log.i("if-Sats", cityName +String.valueOf(tempCelsius) + desc);
                    textViewCity.setText(cityName);
                    textViewDesc.setText(desc);
                    textViewTemp.setText(String.valueOf(tempCelsius)+ "°C");
                    Picasso.get().load("http://openweathermap.org/img/w/"+icon+".png").into(imageViewIcon);
                }
            }

            catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("Website content ", result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewIcon = (ImageView) findViewById(R.id.imageViewWeateherIcon);
        textViewCity = (TextView) findViewById(R.id.textViewCity);
        textViewDesc = (TextView) findViewById(R.id.textViewDesc);
        textViewTemp = (TextView) findViewById(R.id.textViewTemp);
        cityListView = (ListView) findViewById(R.id.cityListView);

        //Loads the coordinaties from the loginActivity & SignupActivity
        latitude = getIntent().getStringExtra("Latitude");
        //Log.i("Inflate from Login", "Latitude : " + latitude);
        longitude = getIntent().getStringExtra("Longitude");
        //Log.i("Inflate from Login", "Longitude : " + longitude);


        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute(apiCityByCoord+latitude+"&lon="+longitude+"&appid="+apiKey).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Contents of URL ", result);

    }

    //Opens an Alert Dialog to change to another city
    public void changeCityAlertDialog(View view){
        AlertDialogChangeCity alertDialogChangeCity = new AlertDialogChangeCity();
        alertDialogChangeCity.show(getSupportFragmentManager(), "Change city dialog");
    }

    //Makes a new API call through the background thread
    public void changeCity(){
        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute(apiChangeCityUrl+changeCityName+"&appid="+apiKey).get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Contents of URL ", result);
    }

}
