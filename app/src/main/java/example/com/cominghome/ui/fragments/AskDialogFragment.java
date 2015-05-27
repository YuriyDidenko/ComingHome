package example.com.cominghome.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import example.com.cominghome.app.App;
import example.com.cominghome.background.LocationService;

import static example.com.cominghome.utils.Utils.TRACK_MODE_KEY;
import static example.com.cominghome.utils.Utils.TRACK_MODE_OFF;
import static example.com.cominghome.utils.Utils.TRACK_MODE_ON;


public class AskDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle("title")
                .setPositiveButton("Yes", this)
                .setNegativeButton("No", this)
                .setNeutralButton("Cancel", this)
                .setMessage("Do you still want to track your location?");

        return adb.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:

                Toast.makeText(getActivity(), "yes", Toast.LENGTH_SHORT).show();
                // saving radio button state
                OptionsFragment.saveOption(getActivity(), TRACK_MODE_KEY, TRACK_MODE_ON);
                // running, if the service is sleeping
                if (!App.isServiceRunning(getActivity(), LocationService.class))
                    getActivity().startService(new Intent(LocationService.ACTION_START_RECORD));
                getActivity().finish();

                break;
            case Dialog.BUTTON_NEGATIVE:

                Toast.makeText(getActivity(), "no", Toast.LENGTH_SHORT).show();
                getActivity().startService(new Intent(LocationService.ACTION_STOP_RECORD));
                OptionsFragment.saveOption(getActivity(), TRACK_MODE_KEY, TRACK_MODE_OFF);
                getActivity().finish();

                break;
            case Dialog.BUTTON_NEUTRAL:
                Toast.makeText(getActivity(), "cancel", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
