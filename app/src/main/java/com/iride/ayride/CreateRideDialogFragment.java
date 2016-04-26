package com.iride.ayride;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class CreateRideDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private final static String loggerTag = CreateRideDialogFragment.class.getSimpleName();
    private EditText fromText;
    private EditText toText;
    private EditText timeText;
    private EditText availableSeatText;
    private Button createRideButton;
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

            fromText = (EditText) view.findViewById(R.id.from_text);
            toText = (EditText) view.findViewById(R.id.to_text);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
