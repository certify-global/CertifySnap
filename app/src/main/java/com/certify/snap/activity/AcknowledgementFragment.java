package com.certify.snap.activity;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;


public class AcknowledgementFragment extends Fragment {
    private View view;
    private TextView acknowledge_text;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acknowledge, container, false);
        this.view = view;
        sharedPreferences = Util.getSharedPreferences(getContext());

        initView();
        return view;
    }

    void initView() {
        acknowledge_text = view.findViewById(R.id.acknowledge_text);
        acknowledge_text.setTextColor(getResources().getColor(R.color.black));
        acknowledge_text.setText(sharedPreferences.getString(GlobalParameters.ACKNOWLEDGEMENT_TEXT,"All the acknowledge"));

    }

}
