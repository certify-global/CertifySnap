package com.certify.snap.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.certify.snap.R;

public class SecondaryIdentificationFragment extends Fragment {

    private View view;
    private TextView textMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_secondary_identification, container, false);
        this.view = view;

        initView();
        return view;
    }

    void initView() {
        textMsg = view.findViewById(R.id.text_msg);
        textMsg.setText(getString(R.string.rfid_msg));
    }

}
