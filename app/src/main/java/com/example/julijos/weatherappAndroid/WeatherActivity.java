package com.example.julijos.weatherappAndroid;


import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private String latitude="", longitude="", email="";
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
    private LinearLayoutManager linearLayoutManager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
      /*  if(FirebaseAuth.getInstance().getCurrentUser() == null){
            MenuItem menuItem = (MenuItem) findViewById(R.id.logout_item);
            menuItem.setTitle("Login");
        }*/
        switch (item.getItemId()){
            case R.id.logout_item:
                userLogout();
                break;
            case R.id.changeCity_item:
                changeCityAlertDialog();
                break;
            case R.id.add_to_favorites_item:
                addCityToFavorites(firebaseAuthentication, cityName);
                break;
            default:
                return false;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        cityNames = new ArrayList<String>();
        cityTemps = new ArrayList<String>();
        weatherIcons = new ArrayList<String>();

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
            firebaseAuthentication = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
            readDataFromDatabase();
        }
        textViewCity = (TextView) findViewById(R.id.textViewCity);
        textViewDesc = (TextView) findViewById(R.id.textViewDesc);
        textViewTemp = (TextView) findViewById(R.id.textViewTemp);

        //Loads the coordinaties from the loginActivity or SignupActivity
        latitude = getIntent().getStringExtra(LATITUDE);
        longitude = getIntent().getStringExtra(LONGITUDE);
        email = getIntent().getStringExtra(EMAIL);

        DownloadCityInformationTask task = new DownloadCityInformationTask();
        String result = null;
        try {
            result = task.execute("https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid="+"5ffcfd078c6933d6a3e7eb281727fa75").get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"Contents of URL " +  result);

    }

    //Replaces special-charachters (like åäö, and space) in the cityname
    public void removeSpecialCharacterFromCityName(String cityName) {
        changeCityName = cityName;
        changeCityName = changeCityName.replaceAll(" ", "+");
        changeCityName = changeCityName.replaceAll("ö", "o");
        changeCityName = changeCityName.replaceAll("ä","a");
        changeCityName = changeCityName.replaceAll("å","a");
        changeCity();
    }

    // Background thread to douwnload the weather information
    public class DownloadCityInformationTask extends AsyncTask<String, Void, String>{

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
                JSONObject jsonObject = new JSONObject(result);
                String weatherInfo = jsonObject.getString(WEATHER);
                JSONObject jsonPart = null;
                JSONArray array = new JSONArray(weatherInfo);

                for(int i = 0 ; i < array.length(); i++){
                    jsonPart = array.getJSONObject(i);
                    icon = jsonPart.getString(ICON);

                    if(favorite) {
                        weatherIcons.add(icon);
                        city.setIconId(icon);
                    }
                    Log.i("iconID", "onPostExecute: " + icon);
                    desc = jsonPart.getString(DESCRIPTION);
                    city.setDescription(desc);
                }
                JSONObject main = jsonObject.getJSONObject("main");
                int tempKelvin = main.getInt(TEMPERATURE);
                tempCelsius = tempKelvin-273;
                city.setTemperature(String.valueOf(tempCelsius)+ " °C");
                cityName = jsonObject.getString("name");
                city.setName(cityName);
                if(favorite){
                    cityTemps.add(String.valueOf(tempCelsius)+ " °C");
                    city.setTemperature(String.valueOf(tempCelsius)+ " °C");
                    Log.d(TAG, "doInBackground: citytemps:" + tempCelsius);
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
            try {
                if (cityName != "" && String.valueOf(tempCelsius) != "" && desc != "") {
                    imageViewIcon = findViewById(R.id.imageViewWeateherIcon);
                    textViewCity.setText(city.getName());
                    textViewDesc.setText(city.getDescription());
                    textViewTemp.setText(city.getTemperature());
                    Log.i("Icon URL", "onPostExecute: " + icon);
                    if(favorite){
                        cityTemps.add(String.valueOf(tempCelsius) + "°C");
                        weatherIcons.add(icon);
                    }
                    showWeatherIcon(icon);
                }
            }catch (Exception e){

            }
            Log.i("Website content ", result);
        }
    }

    // Background thread to douwnload the weather information to the recyclerview
    public class DownloadCityListInformationTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;
            City newCity = new City();
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

            try {

                if (cityName != "" && String.valueOf(tempCelsius) != "" && desc != "") {
                    Log.i("Icon URL", "onPostExecute: " + icon);
                    if(favorite){
                        cityTemps.add(String.valueOf(tempCelsius) + "°C");
                        weatherIcons.add(icon);
                    }

                }
            }catch (Exception e){

            }
            Log.i("Website content ", result);
        }
    }

    //Function to set the weather icon
    private void showWeatherIcon(String icon) {
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
    }

    // Adds city to list of favorite cities
    public void addCityToFavorites(String userID, String cityName) {
        cityNames.add(cityName);
        databaseReference.child("users").child(userID).child("cities").child(cityName).setValue(cityName);
        //notifyAll();
        readDataFromDatabase();
        DownloadCityInformationTask task = new DownloadCityInformationTask();
        String result = null;
        try {
            result = task.execute("https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid="+"5ffcfd078c6933d6a3e7eb281727fa75").get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"Contents of URL " +  result);
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
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //Opens an Alert Dialog to change to another city
    public void changeCityAlertDialog(){
        AlertDialogChangeCity alertDialogChangeCity = new AlertDialogChangeCity();
        alertDialogChangeCity.show(getSupportFragmentManager(), "Change city dialog");
    }

    //Makes a new API call through the background thread
    public void changeCity(){
        DownloadCityInformationTask task = new DownloadCityInformationTask();
        String result = null;
        try {
            // cannot get the weather information when api_key and url is in resources
            result = task.execute("https://api.openweathermap.org/data/2.5/weather?q="+changeCityName+"&appid="+"5ffcfd078c6933d6a3e7eb281727fa75").get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Contents of URL ", result);
        favorite = false;
    }

    // Initilizes the favorite cities list
    private void initializeFavoriteCityRecyclerView(){
        Log.d(TAG, "initializeFavoriteCityRecyclerView: starts");
        RecyclerView recyclerView = findViewById(R.id.favorite_cities_recycler_view);
        adapter = new FavoriteCityRecyclerViewAdapter( cityNames, cityTemps, weatherIcons, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "initializeFavoriteCityRecyclerView: finish");
    }
}
