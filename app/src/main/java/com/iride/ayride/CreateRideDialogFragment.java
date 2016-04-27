package com.iride.ayride;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.Calendar;

public class CreateRideDialogFragment extends DialogFragment implements PlaceSelectionListener{

    private final static String loggerTag = CreateRideDialogFragment.class.getSimpleName();
    private PlaceAutocompleteFragment fromAutocompleteFragment;
    private PlaceAutocompleteFragment toAutocompleteFragment;
    private EditText timeText;
    private EditText availableSeatText;
    private TimePickerDialog appointmentTimePickerDialog;
    private Activity inActivity;

    public interface CreateRideDialogListener {
        public void onDialogPositiveClick(CreateRideDialogFragment dialog);
        public void onDialogNegativeClick(CreateRideDialogFragment dialog);
    }

    CreateRideDialogListener dialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.create_ride_layout, null);
            builder.setView(view)
                    .setPositiveButton(R.string.driverCreateRide, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialogListener.onDialogPositiveClick(CreateRideDialogFragment.this);
                        }
                    })
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialogListener.onDialogNegativeClick(CreateRideDialogFragment.this);
                        }
                    });
            fromAutocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.from_autocomplete_fragment);
            fromAutocompleteFragment.setOnPlaceSelectedListener(this);
            toAutocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.to_autocomplete_fragment);
            toAutocompleteFragment.setOnPlaceSelectedListener(this);
            availableSeatText = (EditText) view.findViewById(R.id.available_seat);
            timeText = (EditText) view.findViewById(R.id.appointment_time_text);
            timeText.setOnTouchListener(timeTextTouchListener);
            timeText.setOnFocusChangeListener(timeTextFocusChangeListener);
        } catch (Exception exc){
            Log.e(loggerTag, exc.getMessage());
        }
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.inActivity = activity;
            dialogListener = (CreateRideDialogListener) activity;
            initializeTimePickerDialog(this.inActivity);
        } catch (ClassCastException e) {
            Log.e(loggerTag, e.getMessage());
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.i(loggerTag, "Place Selected: " + place.getName());
    }

    @Override
    public void onError(Status status) {
        Log.e(loggerTag, "onError: Status = " + status.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getFragmentManager().beginTransaction().remove(fromAutocompleteFragment).commit();
        getFragmentManager().beginTransaction().remove(toAutocompleteFragment).commit();
    }

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(loggerTag, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    private void initializeTimePickerDialog(Activity activity){

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        appointmentTimePickerDialog = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeText.setText( selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);
        appointmentTimePickerDialog.setTitle("Select Time");

    }

    private void hideSoftKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private View.OnFocusChangeListener timeTextFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                hideSoftKeyboard(v);
                appointmentTimePickerDialog.show();
            }
        }
    };

    private View.OnTouchListener timeTextTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hideSoftKeyboard(v);
            appointmentTimePickerDialog.show();
            return true;
        }
    };
}
