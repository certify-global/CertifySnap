package com.certify.snap.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.certify.snap.R;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.api.response.LanguageData;
import com.certify.snap.common.AppSettings;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.controller.GestureController;

import java.util.List;


public class TouchModeFragment extends Fragment {

    private final String TAG = TouchModeFragment.class.getSimpleName();
    private Activity mActivity;
    private View view;
    private TextView tvLang, tvMassageTouch;
    private Button btTouchStart;
    private Typeface rubikLight;
    private RadioButton radio_english, radio_spanish, radio_french;
    private String languageCode = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_touch_mode, container, false);
        this.view = view;
        initView();
        //sharedPreferences = Util.getSharedPreferences(this.getContext());
        languageSetting();
        btTouchStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity != null) {
                    mActivity.runOnUiThread(() -> {
                        DeviceSettingsController.getInstance().setSelectedLang(AppSettings.getLanguageType());
                        IrCameraActivity activity = (IrCameraActivity) mActivity;
                        if (activity != null) {
                            activity.resetTouchModeGesture();
                            // with out Handler it showing IrCamera Home Screen
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mActivity.getFragmentManager().beginTransaction().remove(TouchModeFragment.this).commitAllowingStateLoss();
                                }
                            }, 1000);
                        }
                    });
                }
            }
        });

        return view;
    }

    private void initView() {
        tvLang = view.findViewById(R.id.locale_settings_tach);
        tvMassageTouch = view.findViewById(R.id.tv_message_touch);
        btTouchStart = view.findViewById(R.id.bt_wave_start_touch);
        radio_english = view.findViewById(R.id.radio_english);
        radio_spanish = view.findViewById(R.id.radio_spanish);
        radio_french = view.findViewById(R.id.radio_french);
        // spinnerLanguageSelector = view.findViewById(R.id.spinner_language_selector_touch);
        rubikLight = Typeface.createFromAsset(mActivity.getAssets(),
                "rubiklight.ttf");
        tvLang.setTypeface(rubikLight);
        tvMassageTouch.setTypeface(rubikLight);
        btTouchStart.setTypeface(rubikLight);
        radio_english.setTypeface(rubikLight);
        radio_spanish.setTypeface(rubikLight);
        radio_french.setTypeface(rubikLight);

        radio_english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!DeviceSettingsController.getInstance().getSelectedLang().equals("en"))
                    updateLag("en");
            }
        });
        radio_spanish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!DeviceSettingsController.getInstance().getSelectedLang().equals("es"))
                    updateLag("es");
            }
        });
        radio_french.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!DeviceSettingsController.getInstance().getSelectedLang().equals("fr"))
                    updateLag("fr");
            }
        });
    }

    private void languageSetting() {
        languageCode = AppSettings.getLanguageType();
        languageCode = DeviceSettingsController.getInstance().getLanguageToUpdate();
        List<LanguageData> languageDataList = DeviceSettingsController.getInstance().getLanguageDataList();
        for (LanguageData languageData : languageDataList) {
            SelectedLag(languageData.languageCode, languageData);
        }

    }

    private void SelectedLag(String lagType, LanguageData languageData) {
        switch (lagType) {
            case "en":
                radio_english.setVisibility(View.VISIBLE);
                radio_english.setChecked(languageCode.equals("en"));
                radio_english.setText(languageData.name);
                break;
            case "es":
                radio_spanish.setVisibility(View.VISIBLE);
                radio_spanish.setChecked(languageCode.equals("es"));
                radio_spanish.setText(languageData.name);
                break;
            case "fr":
                radio_french.setVisibility(View.VISIBLE);
                radio_french.setChecked(languageCode.equals("fr"));
                radio_french.setText(languageData.name);
                break;
        }
    }

    private void updateLag(String code) {
        DeviceSettingsController.getInstance().setSelectedLang(code);
        mActivity.runOnUiThread(() -> {
            DeviceSettingsController.getInstance().setLanguageToUpdate(code);
            String msg = String.format(getString(R.string.update_language_msg),
                    DeviceSettingsController.getInstance().getLanguageNameOnCode(code));
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            DeviceSettingsController.getInstance().getSettingsFromDb(DeviceSettingsController.getInstance().
                    getLanguageIdOnCode(DeviceSettingsController.getInstance().getLanguageToUpdate()));
            GestureController.getInstance().getQuestionsFromDb(DeviceSettingsController.getInstance().getLanguageToUpdate());
            new Handler().postDelayed(mActivity::recreate, 500);
        });
    }


}