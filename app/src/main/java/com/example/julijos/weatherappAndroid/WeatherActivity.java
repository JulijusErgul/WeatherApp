package com.example.julijos.weatherappAndroid;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;


public class WeatherActivity extends AppCompatActivity implements AlertDialogChangeCity.AlertDialogListener{

    private static final String TAG = "WeatherActivity";
    private static final String LONGITUDE = "Longitude";
    private static final String LATITUDE = "Latitude";
    private static final String EMAIL = "Email";
    private static final String WEATHER = "weather";
    private static final String ICON = "icon";
    private static final String DESCRIPTION = "description";
    private static final String TEMPERATURE = "temp";
    private boolean favorite = false;

    private ImageView imageViewIcon, imageViewAddToFavorites;
    private TextView textViewCity, textViewDesc, textViewTemp;

    private City city = new City();
    private String changeCityName ="";
    private String desc = "";
    private String cityName = "";
    int tempCelsius;

    private FavoriteCityRecyclerViewAdapter adapter;

    private String firebaseAuthentication;

    private DatabaseReference databaseReference;
    private String icon;
    private ArrayList<String> cityNames;
    private ArrayList<String> cityTemps;
    private ArrayList<String> weatherIcons;
    private ArrayList<City> cityArrayList;
    String strLongitude, strLatitude;

    // Initializes the action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: start");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar_menu, menu);
        MenuItem loginItem = menu.findItem(R.id.login_item);
        MenuItem logoutItem = menu.findItem(R.id.logout_item);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            loginItem.setVisible(false);
            logoutItem.setVisible(true);
        }
        else{
            logoutItem.setVisible(false);
            loginItem.setVisible(true);
        }
        Log.d(TAG, "onCreateOptionsMenu: lämnar");
        return super.onCreateOptionsMenu(menu);
    }

    //creates menu option items for the actionbar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: start");
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.logout_item:
                userLogout();
                break;
            case R.id.login_item:
                Intent intent = new Intent(this, LogInActivity.class);
                startActivity(intent);
                break;
            case R.id.changeCity_item:
                changeCityAlertDialog();
                break;
            case R.id.add_to_favorites_item:
                if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                    addCityToFavoritesDB(firebaseAuthentication, cityName);
                }else{
                    addCityToFavoritesSharedPrefs();
                }
                break;
            default:
                return false;
        }
        Log.d(TAG, "onOptionsItemSelected: lämnar");
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        if(getIntent().hasExtra("Longitude")  ){
            strLongitude = getIntent().getStringExtra("Longitude");
            strLatitude = getIntent().getStringExtra("Latitude");
        }

        cityNames = new ArrayList<String>();
        cityTemps = new ArrayList<String>();
        weatherIcons = new ArrayList<String>();

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
            firebaseAuthentication = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
            readDataFromDatabase();
        }
        else{
            cityNames.clear();
            cityTemps.clear();
            weatherIcons.clear();
            cityNames = getArrayList("cityNames");
            if(cityTemps != null) {
                downloadDataFromCityNames();
            }else{
                cityNames = new ArrayList<String>();
                cityTemps = new ArrayList<String>();
                weatherIcons = new ArrayList<String>();
            }
        }
        textViewCity = (TextView) findViewById(R.id.textViewCity);
        textViewDesc = (TextView) findViewById(R.id.textViewDesc);
        textViewTemp = (TextView) findViewById(R.id.textViewTemp);

        //downloads the content for the main city that is shown in the activity
        downloadCityInformation();
        Log.d(TAG, "onCreate: lämnar");
    }

    // downloading data for the the users current location
    private void downloadCityInformation() {
        DownloadCityInformationTask task = new DownloadCityInformationTask();
        String result = null;
        try {
            result = task.execute("https://api.openweathermap.org/data/2.5/weather?lat="+strLatitude+"&lon="+strLongitude+"&appid="+"5ffcfd078c6933d6a3e7eb281727fa75").get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"Contents of URL " +  result);
    }

    // downloading data for the cityNames Arraylist
    private void downloadDataFromCityNames() {
        if(cityNames != null){
            for(int i = 0; i < cityNames.size(); i++){
                Log.i(TAG, "onCreate: citynames: " + cityNames.get(i));
                favorite = true;
                DownloadCityListInformationTask taskList = new DownloadCityListInformationTask();
                String resultList = null;
                try {
                    resultList = taskList.execute("https://api.openweathermap.org/data/2.5/weather?q="+cityNames.get(i)+"&appid="+"5ffcfd078c6933d6a3e7eb281727fa75").get();
                } catch (ExecutionException e) {
                    Log.e(TAG, "onDataChange: error",e );
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.e(TAG, "onDataChange: error",e );
                    e.printStackTrace();
                }
                Log.i("Contents of URL ", resultList);
            }
            initializeFavoriteCityRecyclerView();
        }
    }

    //Replaces special-charachters (like åäö, and space) in the cityname
    public void removeSpecialCharacterFromCityName(String cityName) {
        Log.d(TAG, "removeSpecialCharacterFromCityName: start");
        changeCityName = cityName;
        changeCityName = changeCityName.replaceAll(" ", "+");
        changeCityName = changeCityName.replaceAll("ö", "o");
        changeCityName = changeCityName.replaceAll("ä","a");
        changeCityName = changeCityName.replaceAll("å","a");
        changeCity();
        Log.d(TAG, "removeSpecialCharacterFromCityName: lämnar");
    }

    // Background thread to download the weather information
    public class DownloadCityInformationTask extends AsyncTask<String, Void, String>{
        private static final String TAG = "DownloadCityInformation";
        @Override
        protected String doInBackground(String... urls) {
            Log.d(TAG, "doInBackground: start");
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try{
                Log.i(TAG, "doInBackground: try: start");
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
                JSONObject jsonObject = new JSONObject(result);
                String weatherInfo = jsonObject.getString(WEATHER);
                JSONObject jsonPart = null;
                JSONArray array = new JSONArray(weatherInfo);

                for(int i = 0 ; i < array.length(); i++){
                    jsonPart = array.getJSONObject(i);
                    icon = jsonPart.getString(ICON);
                    Log.i("iconID", "onPostExecute: " + icon);
                    desc = jsonPart.getString(DESCRIPTION);

                }
                JSONObject main = jsonObject.getJSONObject("main");
                int tempKelvin = main.getInt(TEMPERATURE);
                tempCelsius = tempKelvin-273;
                cityName = jsonObject.getString("name");
                textViewCity.setText(cityName);
                textViewDesc.setText(desc);
                textViewTemp.setText(String.valueOf(tempCelsius)+ " °C");
                showWeatherIcon(icon);
                Log.i(TAG, "doInBackground: try: lämnar");
                return result;
            }
            catch (Exception e){
                Log.i(TAG, "doInBackground: catch: start");
                e.printStackTrace();
                Log.i(TAG, "doInBackground: catch: lämnar");
                return "Failed";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: start");
            super.onPostExecute(result);
            try {
                Log.d(TAG, "onPostExecute: try: start");
                /*cityNames.add(cityName);
                cityTemps.add(String.valueOf(tempCelsius)+ " °C");
                weatherIcons.add(icon);
                */
                textViewCity.setText(cityName);
                textViewDesc.setText(desc);
                textViewTemp.setText(String.valueOf(tempCelsius)+ " °C");
                showWeatherIcon(icon);
                Log.i(TAG, "onPostExecute: try: lämnar");

            }catch (Exception e){
                Log.i(TAG, "onPostExecute: catch: start");
                Log.i(TAG, "onPostExecute: catch: lämnar");
            }
            Log.i("Website content ", result);
            Log.d(TAG, "onPostExecute: lämnar");
        }
    }

    // Background thread to douwnload the weather information to the recyclerview
    public class DownloadCityListInformationTask extends AsyncTask<String, Void, String>{
        private static final String TAG = "DownloadCityListInforma";

        @Override
        protected String doInBackground(String... urls) {
            Log.d(TAG, "doInBackground: start");
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;
            City newCity = new City();
            try{
                Log.i(TAG, "doInBackground: try: start");
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

                JSONObject jsonObject = new JSONObject(result);
                String weatherInfo = jsonObject.getString(WEATHER);
                JSONObject jsonPart = null;
                JSONArray array = new JSONArray(weatherInfo);

                for(int i = 0 ; i < array.length(); i++){
                    jsonPart = array.getJSONObject(i);
                    icon = jsonPart.getString(ICON);
                    if(favorite) {
                        weatherIcons.add(icon);
                    }
                    Log.i("iconID", "onPostExecute: " + icon);
                    desc = jsonPart.getString(DESCRIPTION);
                }
                JSONObject main = jsonObject.getJSONObject("main");
                int tempKelvin = main.getInt(TEMPERATURE);
                tempCelsius = tempKelvin-273;
                cityName = jsonObject.getString("name");
                if(favorite){
                    cityTemps.add(String.valueOf(tempCelsius)+ " °C");
                    Log.d(TAG, "doInBackground: citytemps:" + tempCelsius);
                }
                Log.i(TAG, "doInBackground: try: lämnar");
                return result;
            }
            catch (Exception e){
                Log.i(TAG, "doInBackground: catch: start");
                e.printStackTrace();
                Log.i(TAG, "doInBackground: catch: lämnar");
                return "Failed";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

            }catch (Exception e){

            }
            Log.i("Website content ", result);
        }
    }

    //Function to set the weather icon
    private void showWeatherIcon(String icon) {
        Log.d(TAG, "showWeatherIcon: start");
        ArrayList<String> icons = new ArrayList<String>();
        icons.addAll(Arrays.asList("i01d","i01n","i02d","i02n","i03d","i03n",
                "i04d","i04n", "i09d", "i09n", "i10d", "i10n","i11d", "i11n",
                "i13d", "i13n","i50d", "i50n"));
        String iconId = "i"+icon;

        if(iconId == null)
            Picasso.get().load(R.drawable.noweather).into(imageViewIcon);
        else{
            if(icons.contains(iconId)){
                String uri = "drawable/"+iconId;
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                ImageView imageView = (ImageView) findViewById(R.id.imageViewWeateherIcon);
                Drawable image = getResources().getDrawable(imageResource);
                imageView.setImageDrawable(image);
            }
            else{
                Picasso.get().load(R.drawable.noweather).into(imageViewIcon);
            }
        }
        Log.d(TAG, "showWeatherIcon: lämnar");
    }

    // Adds city to list of favorite cities
    public void addCityToFavoritesDB(String userID, String cityName) {
        Log.d(TAG, "addCityToFavoritesDB: start");
        cityNames.add(cityName);
        databaseReference.child("users").child(userID).child("cities").child(cityName).setValue(cityName);
        readDataFromDatabase();
        DownloadCityInformationTask task = new DownloadCityInformationTask();
        String result = null;
        try {
            Log.i(TAG, "addCityToFavoritesDB: try: start");
            result = task.execute("https://api.openweathermap.org/data/2.5/weather?lat="+strLatitude+"&lon="+strLongitude+"&appid="+"5ffcfd078c6933d6a3e7eb281727fa75").get();
            Log.i(TAG, "addCityToFavoritesDB: try: lämnar");
        } catch (ExecutionException e) {
            Log.i(TAG, "addCityToFavoritesDB: catch: start");
            e.printStackTrace();
            Log.i(TAG, "addCityToFavoritesDB: catch: lämnar");
        } catch (InterruptedException e) {
            Log.i(TAG, "addCityToFavoritesDB: catch: start");
            e.printStackTrace();
            Log.i(TAG, "addCityToFavoritesDB: catch: lämnar");
        }
        Log.i(TAG,"Contents of URL " +  result);
        Log.d(TAG, "addCityToFavoritesDB: lämnar");
    }

    // adding new cities to sharedpreferences
    public void addCityToFavoritesSharedPrefs(){
        if(cityNames == null || cityTemps == null || weatherIcons == null){
            cityNames = new ArrayList<String>();
            cityTemps = new ArrayList<String>();
            weatherIcons = new ArrayList<String>();
        }
        cityNames.add(cityName);
        cityTemps.add(String.valueOf(tempCelsius)+ " °C");
        weatherIcons.add(icon);
        saveArrayList(cityNames, "cityNames");
        saveArrayList(cityTemps, "cityTemps");
        saveArrayList(weatherIcons, "weatherIcons");
        initializeFavoriteCityRecyclerView();
    }

    /*
    * Save City information arrays to Sharedpreferences
    * */
    public void saveArrayList(ArrayList<String> arrayList, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(key,json);
        editor.apply();
    }
    /*
     * Get City information arrays from Sharedpreferences
     * */
    public ArrayList<String> getArrayList(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /*
     * Save City information arrays to Sharedpreferences
     * */
    public void saveUser(User user){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString("userLoggedIn",json);
        editor.apply();
    }
    /*
     * Get City information arrays from Sharedpreferences
     * */
    public User getUser(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString("userLoggedIn", null);
        Type type = new TypeToken<User>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Load data from the database and sets it to the list.
    public void readDataFromDatabase(){

        Log.d(TAG, "readDataFromDatabase: starts");
        // Read from the database
        new Thread(new Runnable() {
            private static final String TAG = "ReadDataFromDatabase";
            @Override
            public void run() {
                Log.d(TAG, "run: start");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: start");
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        cityNames.clear();
                        cityTemps.clear();
                        weatherIcons.clear();
                        Log.d(TAG, "onDataChange: starts");
                        for(DataSnapshot ds : dataSnapshot.child("users").child(firebaseAuthentication).child("cities").getChildren()){
                            cityNames.add(ds.getValue(String.class));
                        }
                        if(!cityNames.isEmpty()){
                            for(int i = 0; i < cityNames.size(); i++){
                                Log.i(TAG, "onCreate: citynames: " + cityNames.get(i));
                                favorite = true;
                                DownloadCityListInformationTask task = new DownloadCityListInformationTask();
                                String result = null;
                                try {
                                    result = task.execute("https://api.openweathermap.org/data/2.5/weather?q="+cityNames.get(i)+"&appid="+"5ffcfd078c6933d6a3e7eb281727fa75").get();
                                } catch (ExecutionException e) {
                                    Log.e(TAG, "onDataChange: error",e );
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "onDataChange: error",e );
                                    e.printStackTrace();
                                }
                                Log.i("Contents of URL ", result);
                            }
                        }
                        initializeFavoriteCityRecyclerView();
                        Log.d(TAG, "onDataChange: finish");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.e(TAG, "Failed to read value.", error.toException());
                    }
                });
                favorite = false;
                Log.d(TAG, "run: finish");
            }
        }).start();

        Log.d(TAG, "readDataFromDatabase: finish");
    }

    //logout
    public void userLogout(){
        Log.d(TAG, "userLogout: start");
        FirebaseAuth.getInstance().signOut();
        User user = null;
        saveUser(user);
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Log.d(TAG, "userLogout: lämnar");
    }

    //Opens an Alert Dialog to change to another city
    public void changeCityAlertDialog(){
        Log.d(TAG, "changeCityAlertDialog: start");
        AlertDialogChangeCity alertDialogChangeCity = new AlertDialogChangeCity();
        alertDialogChangeCity.show(getSupportFragmentManager(), "Change city dialog");
        Log.d(TAG, "changeCityAlertDialog: lämnar");
    }

    //Makes a new API call through the background thread
    public void changeCity(){
        Log.d(TAG, "changeCity: start");
        DownloadCityInformationTask task = new DownloadCityInformationTask();
        String result = null;
        try {
            result = task.execute("https://api.openweathermap.org/data/2.5/weather?q="+changeCityName+"&appid="+"5ffcfd078c6933d6a3e7eb281727fa75").get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Contents of URL ", result);
        favorite = false;
        Log.d(TAG, "changeCity: lämnar");
    }

    // Initilizes the favorite cities list
    private void initializeFavoriteCityRecyclerView(){
        Log.d(TAG, "initializeFavoriteCityRecyclerView: starts");
        RecyclerView recyclerView = findViewById(R.id.favorite_cities_recycler_view);
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            adapter = new FavoriteCityRecyclerViewAdapter(this);
        }else{
            adapter = new FavoriteCityRecyclerViewAdapter(this, cityNames, cityTemps, weatherIcons);
        }
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "initializeFavoriteCityRecyclerView: finish");
    }
}
