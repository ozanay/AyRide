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

/**
 * Created by user on 6.05.2016.
 */
public class HashTagDialogFragment extends DialogFragment {

    private final static String loggerTag = HashTagDialogFragment.class.getSimpleName();
    private EditText hashTagText;
    public interface HashTagDialogListener {
        void onDialogPositiveClick(HashTagDialogFragment hashTagDialogFragment);
        void onDialogNegativeClick(HashTagDialogFragment hashTagDialogFragment);
    }

    HashTagDialogListener dialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.hashtag_layout, null);
            builder.setView(view)
                    .setPositiveButton(R.string.addHashTag, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialogListener.onDialogPositiveClick(HashTagDialogFragment.this);
                        }
                    })
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialogListener.onDialogNegativeClick(HashTagDialogFragment.this);
                        }
                    });

            hashTagText = (EditText) view.findViewById(R.id.hash_tag_text);
        } catch (Exception exc){
            Log.e(loggerTag, exc.getMessage());
        }
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            dialogListener = (HashTagDialogListener) activity;
        } catch (ClassCastException e) {
            Log.e(loggerTag, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public String getHashTag(){
        return hashTagText.getText().toString();
    }
}
