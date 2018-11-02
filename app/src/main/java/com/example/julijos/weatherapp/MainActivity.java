package com.example.julijos.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity implements AlertDialogChangeCity.AlertDialogListener{

    ImageView imageViewIcon;
    TextView textViewCity, textViewDesc, textViewTemp;
    ListView cityListView;


    private String apiKey = "5ffcfd078c6933d6a3e7eb281727fa75";
    private String apiChangeCityUrl = "https://api.openweathermap.org/data/2.5/weather?q=";//change city by name
    private String apiCityByCoord = "https://api.openweathermap.org/data/2.5/weather?lat=";//get city by coordinates

    String changeCityName ="";

    LocationManager locationManager;
    LocationListener locationListener;

    double longitude, latitude;
    String strLong, strLat;

    String icon = "";
    String desc = "";
    String cityName = "";
    double tempCelsius;

    ArrayList<String> prevCities = new ArrayList<String>();

    Map<String, String> prevCityToDB = new HashMap<String,String>();

    FirebaseFirestore firebaseFirestoreDB = FirebaseFirestore.getInstance();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locationListener);

    }

    @Override
    public void applyText(String cityName) {
        changeCityName = cityName;
        Log.i("ChangeCity:  ", "applyText: " + cityName);

        prevCities.add(changeCityName);
        //Replace non-acceptable charachters
        prevCityToDB.put("City name", changeCityName);
        changeCityName = changeCityName.replaceAll(" ", "+");
        changeCityName = changeCityName.replaceAll("ö", "o");
        changeCityName = changeCityName.replaceAll("ä","a");
        changeCity();
        uploadToDB();

    }

    public void formatCityName(String cityName){
        changeCityName = cityName;
        Log.i("ChangeCity:  ", "applyText: " + cityName);
        //Replace non-acceptable charachters
        changeCityName = changeCityName.replaceAll(" ", "+");
        changeCityName = changeCityName.replaceAll("ö", "o");
        changeCityName = changeCityName.replaceAll("ä","a");
        changeCity();
    }

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
            String desc = "";

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
                    Picasso.with(getBaseContext()).load("http://openweathermap.org/img/w/"+icon+".png").into(imageViewIcon);
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

        readFromDB();


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                //Toast.makeText(getApplicationContext(), String.valueOf(latitude)+ String.valueOf(longitude), Toast.LENGTH_LONG).show();
                Log.i("GPS-Coordinates", "Lat: " + String.valueOf(latitude) + ", Long: " + String.valueOf(longitude));
                strLat = String.valueOf(latitude);
                strLong = String.valueOf(longitude);
               // Log.i("Coordinates: ", strLat+", "+strLong);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, locationListener);
        }


        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute(apiCityByCoord+String.valueOf(latitude)+"&lon="+String.valueOf(longitude)+"&appid="+apiKey).get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Contents of URL ", result);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, prevCities);
        cityListView.setAdapter(arrayAdapter);

        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("City Tapped", prevCities.get(i));
                changeCityName = prevCities.get(i);
                Log.i("ChangeCityName: ", changeCityName);
                formatCityName(changeCityName);
                changeCity();
            }
        });


    }

    public void changeCityAlertDialog(View view){
        AlertDialogChangeCity alertDialogChangeCity = new AlertDialogChangeCity();
        alertDialogChangeCity.show(getSupportFragmentManager(), "Change city dialog");
    }

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

    public void readFromDB(){
        firebaseFirestoreDB.collection("Cities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                Log.d("Retrieving SUCCESS", "Data succefully retreived " + documentSnapshot.getId() + " => " + documentSnapshot.getData());

                                JSONObject dataFromDB = new JSONObject(documentSnapshot.getData());
                                JSONArray dataFromDBArray = new JSONArray();


                                for(String City : dataFromDBArray){

                                }


                                prevCities.add(String.valueOf(documentSnapshot.getData()));
                            }
                            Log.d("ARRAYLIST SUCCESS", "Previously added cities from DB" + prevCities.toString());
                        }else{
                            Log.w("Retrieving ERROR", "Error getting documents ", task.getException() );
                        }
                    }
                });
    }

    public void uploadToDB(){
        firebaseFirestoreDB.collection("Cities")
                .add(prevCityToDB)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FireStoreDB SUCCESS", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FAILURE DB", "Error occured when adding to DB" + e );
                    }
                });
    }
}
