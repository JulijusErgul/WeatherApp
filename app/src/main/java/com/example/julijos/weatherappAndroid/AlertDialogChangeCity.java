package com.example.julijos.weatherappAndroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AlertDialogChangeCity extends AppCompatDialogFragment {

    private EditText editTextChangeCity;
    private AlertDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_changecity, null);

        builder.setView(view)
                .setTitle(R.string.alert_dialog_change_city)
                .setMessage(R.string.alert_dialog_change_to)
                .setNegativeButton(R.string.alert_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String cityName = editTextChangeCity.getText().toString();
                        listener.removeSpecialCharacterFromCityName(cityName);
                    }
                });
        editTextChangeCity = view.findViewById(R.id.editText_changeCity);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AlertDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement AlertDialogListener");
        }

    }

    public interface AlertDialogListener{
        void removeSpecialCharacterFromCityName(String cityName);
    }
}
