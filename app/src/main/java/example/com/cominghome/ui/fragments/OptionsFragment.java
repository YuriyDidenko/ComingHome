package example.com.cominghome.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import example.com.cominghome.R;


/**
 * Created by Loner on 24.05.2015.
 */
public class OptionsFragment extends Fragment {
    public OptionsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_options, container, false);

        return rootView;
    }
}
