package com.iride.ayride;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class VehicleRegistrationDialogFragment extends DialogFragment {

    private final static String loggerTag = VehicleRegistrationDialogFragment.class.getSimpleName();
    private EditText vehicleModel;
    private EditText vehicleYear;
    private EditText vehicleColor;
    private EditText vehicleLicensePlate;

    public interface VehicleRegistrationDialogListener {
        public void onDialogPositiveClick(VehicleRegistrationDialogFragment dialog);
        public void onDialogNegativeClick(VehicleRegistrationDialogFragment dialog);
    }

    VehicleRegistrationDialogListener dialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.vehicle_registration, null);
            builder.setView(view)
                    .setPositiveButton(R.string.addVehicleInfo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialogListener.onDialogPositiveClick(VehicleRegistrationDialogFragment.this);
                        }
                    })
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialogListener.onDialogNegativeClick(VehicleRegistrationDialogFragment.this);
                        }
                    });

            view.findViewById(R.id.vehicle_loading_panel).setVisibility(View.GONE);
            vehicleModel = (EditText) view.findViewById(R.id.vehicle_model_text);
            vehicleColor = (EditText) view.findViewById(R.id.vehicle_color_text);
            vehicleYear = (EditText) view.findViewById(R.id.vehicle_year_text);
            vehicleLicensePlate = (EditText) view.findViewById(R.id.vehicle_license_plate_text);
        } catch (Exception exc){
            Log.e(loggerTag, exc.getMessage());
        }
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            dialogListener = (VehicleRegistrationDialogListener) activity;
            this.setCancelable(false);
        } catch (ClassCastException e) {
            Log.e(loggerTag, e.getMessage());
        }
    }

    @Override
    public void onDetach(){
        dialogListener = null;
        super.onDetach();
    }

    protected Vehicle getVehicleInformations(){
        if (vehicleModel.getText() == null){
            Log.d(loggerTag, "Vehicle Model is NULL!");
            return new Vehicle();
        }

        if (vehicleColor.getText() == null){
            Log.d(loggerTag, "Vehicle Color is NULL!");
            return new Vehicle();
        }

        if (vehicleYear.getText() == null){
            Log.d(loggerTag, "Vehicle Year is NULL!");
            return new Vehicle();
        }

        if (vehicleLicensePlate.getText() == null){
            Log.d(loggerTag, "Vehicle License Plate is NULL!");
            return new Vehicle();
        }

        return new Vehicle(vehicleModel.getText().toString(), vehicleColor.getText().toString(),
                vehicleLicensePlate.getText().toString(), vehicleYear.getText().toString());
    }
}
