package example.com.cominghome.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import example.com.cominghome.R;

import static example.com.cominghome.utils.Utils.SHARED_PREFERENCES_NAME;
import static example.com.cominghome.utils.Utils.TRACK_MODE_ASK;
import static example.com.cominghome.utils.Utils.TRACK_MODE_KEY;
import static example.com.cominghome.utils.Utils.TRACK_MODE_OFF;
import static example.com.cominghome.utils.Utils.TRACK_MODE_ON;
import static example.com.cominghome.utils.Utils.TRACK_MODE_RADIO_BUTTON_ID_KEY;
import static example.com.cominghome.utils.Utils.TURNING_MODE_KEY;

public class OptionsFragment extends Fragment {

    public OptionsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_options, container, false);

        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radio_group);

        RadioButton rbTrack = (RadioButton) rootView.findViewById(R.id.rb_track);
        RadioButton rbNotTrack = (RadioButton) rootView.findViewById(R.id.rb_not_track);
        RadioButton rbAskTrack = (RadioButton) rootView.findViewById(R.id.rb_ask_track);

        CheckBox chbTurningMode = (CheckBox) rootView.findViewById(R.id.check_box_turning_mode);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedItem = group.getCheckedRadioButtonId();
                switch (selectedItem) {
                    case R.id.rb_track:
                        saveOption(getActivity(), TRACK_MODE_KEY, TRACK_MODE_ON);
                        saveOption(getActivity(), TRACK_MODE_RADIO_BUTTON_ID_KEY, R.id.rb_track);
                        break;
                    case R.id.rb_not_track:
                        saveOption(getActivity(), TRACK_MODE_KEY, TRACK_MODE_OFF);
                        saveOption(getActivity(), TRACK_MODE_RADIO_BUTTON_ID_KEY, R.id.rb_not_track);
                        break;
                    case R.id.rb_ask_track:
                        saveOption(getActivity(), TRACK_MODE_KEY, TRACK_MODE_ASK);
                        saveOption(getActivity(), TRACK_MODE_RADIO_BUTTON_ID_KEY, R.id.rb_ask_track);
                        break;
                    default:
                        saveOption(getActivity(), TRACK_MODE_KEY, -1);
                        break;
                }
            }
        });
        radioGroup.check(getOldSelectedId());
        chbTurningMode.setChecked(getOldTurningMode());

        chbTurningMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveOption(getActivity(), TURNING_MODE_KEY, isChecked);
            }
        });

        return rootView;
    }

    static void saveOption(Context context, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(key, value);
        editor.apply();
    }

    static void saveOption(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(key, value);
        editor.apply();
    }

    private int getOldSelectedId() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        return prefs.getInt(TRACK_MODE_RADIO_BUTTON_ID_KEY, R.id.rb_track);
    }

    private boolean getOldTurningMode() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        return prefs.getBoolean(TURNING_MODE_KEY, false);
    }
}
