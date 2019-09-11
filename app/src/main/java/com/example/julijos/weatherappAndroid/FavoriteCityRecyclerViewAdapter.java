package com.example.julijos.weatherappAndroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

public class FavoriteCityRecyclerViewAdapter extends  RecyclerView.Adapter<FavoriteCityRecyclerViewAdapter.ViewHolder>  {

    private static final String TAG = "FavoriteCityRecView";

    private FragmentActivity context;
    private ArrayList<String>cityNames;
    private ArrayList<String>cityTemps;
    private ArrayList<String>weatherIcons;
    private ImageView weatherIcon;

    private DatabaseReference databaseReference;
    private String firebaseAuthentication;


    public FavoriteCityRecyclerViewAdapter(ArrayList<String> cityNames, ArrayList<String> cityTemps, ArrayList<String> weatherIcons,FragmentActivity context) {
        this.cityNames = cityNames;
        this.cityTemps = cityTemps;
        this.weatherIcons = weatherIcons;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_list_favorite_cities, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        Log.d(TAG, "onBindViewHolder: called");
        viewHolder.cityName.setText(cityNames.get(i));
        viewHolder.temperature.setText(cityTemps.get(i));

        //Function to set the weather icon
        if(!weatherIcons.isEmpty()) {
            ArrayList<String> icons = new ArrayList<String>();
            icons.addAll(Arrays.asList("i01d", "i01n", "i02d", "i02n", "i03d", "i03n",
                    "i04d", "i04n", "i09d", "i09n", "i10d", "i10n", "i11d", "i11n",
                    "i13d", "i13n", "i50d", "i50n"));
            String iconId = "i" + weatherIcons.get(i);

            if (iconId == null)
                Picasso.get().load(R.drawable.noweather).into(weatherIcon);
            else {
                if (icons.contains(iconId)) {
                    String uri = "drawable/" + iconId;
                    int imageResource = context.getResources()
                            .getIdentifier(uri, null, "com.example.julijos.weatherappAndroid");
                    Drawable image = context.getResources().getDrawable(imageResource);
                    weatherIcon.setImageDrawable(image);
                } else {
                    Picasso.get().load(R.drawable.noweather).into(weatherIcon);
                }
            }
        }

        viewHolder.favoriteListLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "onLongClick: clicked on" + cityNames.get(i));
                Toast.makeText(context, "onLongClick", Toast.LENGTH_SHORT).show();
                showDeleteAlertDialog(i);
                return true;
            }
        });

    }

    // onLongclick should display an alert dialog, asking the user if he wants to
    // delete the current object.
    private void showDeleteAlertDialog(final int i){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Delete " + cityNames.get(i) + " from favorites")
                .setTitle("Delete");
        builder.setPositiveButton(R.string.alert_dialog_remove_favorite_positive_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Toast.makeText(context, "itemDeleted " + cityNames.get(i), Toast.LENGTH_SHORT).show();
                deleteFavorite(i);
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_remove_favorite_negative_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Removes favorite city from list and from firebase database
    private void deleteFavorite(int i) {

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseAuthentication)
                .child("cities")
                .child(cityNames.get(i).toString());
        databaseReference.removeValue();
        cityNames.remove(i);
        cityTemps.remove(i);
        weatherIcons.remove(i);
        Intent intent = new Intent(context, WeatherActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return cityNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout favoriteListLayout;
        TextView cityName, temperature;
        ImageView weatherIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firebaseAuthentication = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
            favoriteListLayout = itemView.findViewById(R.id.recycler_view_layout);
            cityName = itemView.findViewById(R.id.rec_view_fav_city_name_text_view);
            temperature = itemView.findViewById(R.id.rec_view_fav_city_temp_text_view);
            FavoriteCityRecyclerViewAdapter.this.weatherIcon = itemView.findViewById(R.id.weather_icon_from_favorites_image);

        }
    }

}
