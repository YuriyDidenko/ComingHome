package example.com.cominghome.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import example.com.cominghome.R;


/**
 * Created by Loner on 24.05.2015.
 */
public class OptionsFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {
    public OptionsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_options, container, false);

        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radio_group);
        RadioButton rbTrack, rbNotTrack, rbAskTrack;
        rbTrack = (RadioButton) rootView.findViewById(R.id.rb_track);
        rbNotTrack = (RadioButton) rootView.findViewById(R.id.rb_not_track);
        rbAskTrack = (RadioButton) rootView.findViewById(R.id.rb_ask_track);

//        rbTrack.setOnCheckedChangeListener(this);
//        rbNotTrack.setOnCheckedChangeListener(this);
//        rbAskTrack.setOnCheckedChangeListener(this);

        return rootView;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }
}
