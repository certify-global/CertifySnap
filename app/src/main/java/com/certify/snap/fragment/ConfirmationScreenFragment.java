package com.certify.snap.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.CameraController;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.AccessControlModel;
import com.certify.snap.model.RegisteredMembers;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class ConfirmationScreenFragment extends Fragment {
    private static final String TAG = ConfirmationScreenFragment.class.getSimpleName();
    private Typeface rubiklight;
    private TextView tv_title, tv_subtitle, user_name, face_score;
    private SharedPreferences sp;
    private String value = "";
    private long delayMilli = 0;
    private String longVal = "";
    private ImageView user_img;
    private CompareResult compareResultValues;
    private String confirm_title = "";
    private String confirm_subtitle = "";
    private LinearLayout confirmation_screen_layout;
    private int count = 1;
    private static final int VACCINATED = 1;
    private static final int NON_VACCINATED = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation_screen, container, false);
        rubiklight = Typeface.createFromAsset(getActivity().getAssets(), "rubiklight.ttf");
        sp = Util.getSharedPreferences(getContext());
        tv_title = view.findViewById(R.id.tv_title);
        tv_subtitle = view.findViewById(R.id.tv_subtitle);

        user_img = view.findViewById(R.id.iv_item_head_img);
        user_name = view.findViewById(R.id.tv_item_name);
        face_score = view.findViewById(R.id.facial_score);
        confirmation_screen_layout = view.findViewById(R.id.confirmation_screen_layout);

        Bundle bundle = getArguments();
        if (bundle != null) {
            value = bundle.getString("tempVal");
        }
        compareResultValues = CameraController.getInstance().getCompareResult();
        if (compareResultValues != null && sp.getBoolean(GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, false)) {
            user_img.setVisibility(View.VISIBLE);
            compareResult();
        } else if (CameraController.getInstance().isFaceNotMatchedOnRetry()) {
            showSnackBarMessage("Potential face match didn't happen. Please retry");
        } else {
            onAccessCardMatch();
        }

        if (AppSettings.getShowVaccinationIndicator()) {
            RegisteredMembers firstScanMember = CameraController.getInstance().getFirstScanMember();
            RegisteredMembers secondScanMember = CameraController.getInstance().getSecondScanMember();
            if (secondScanMember != null) {
                showVaccinationIndicator(secondScanMember, 1);
            } else if (firstScanMember != null) {
                showVaccinationIndicator(firstScanMember, 1);
            }
        } else if (AppSettings.getShowNonVaccinationIndicator()) {
            RegisteredMembers firstScanMember = CameraController.getInstance().getFirstScanMember();
            RegisteredMembers secondScanMember = CameraController.getInstance().getSecondScanMember();
            if (secondScanMember != null) {
                showVaccinationIndicator(secondScanMember, 2);
            } else if (firstScanMember != null) {
                showVaccinationIndicator(firstScanMember, 2);
            }
        }

        if (value != null) {
            if (value.equals("high")) {
                confirm_title = sp.getString(GlobalParameters.Confirm_title_above, getResources().getString(R.string.confirmation_text_above));
                confirm_subtitle = sp.getString(GlobalParameters.Confirm_subtitle_above, "");
                longVal = sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_ABOVE, "1");
                tv_title.setText(confirm_title);
                tv_subtitle.setText(confirm_subtitle);
            } else if (value.equals("gestureExit")) {
                confirm_title = AppSettings.getGestureExitConfirmText();
                longVal = sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, "1");
                tv_title.setText(confirm_title);
            } else {
                confirm_title = sp.getString(GlobalParameters.Confirm_title_below, getResources().getString(R.string.confirm_title_below));
                confirm_subtitle = sp.getString(GlobalParameters.Confirm_subtitle_below, "");
                longVal = sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, "1");
                tv_title.setText(confirm_title);
                tv_subtitle.setText(confirm_subtitle);
            }
        }

        tv_title.setTypeface(rubiklight);
        tv_subtitle.setTypeface(rubiklight);
        tv_title.setTextSize(titleSize(confirm_title.length()));
        tv_subtitle.setTextSize(titleSizeSub(confirm_subtitle.length()));
        if (longVal.equals("")) {
            delayMilli = 1;
        } else {
            delayMilli = Long.parseLong(longVal);
        }
        setHomeScreenTimer();

        Log.d("delay milli seconds", "" + delayMilli);

        return view;
    }

    private void compareResult() {
        if (getActivity() != null) {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResultValues.getUserName() + FaceServer.IMG_SUFFIX);
                        if (imgFile.exists()) {
                            Glide.with(ConfirmationScreenFragment.this)
                                    .load(imgFile)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .into(user_img);
                        }
                        user_name.setText(compareResultValues.getMessage());
                        face_score.setText(compareResultValues.getFacialScore());
                    }
                });
                CameraController.getInstance().setCompareResult(null);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                CameraController.getInstance().setCompareResult(null);
            }
        }
    }

    private void onAccessCardMatch() {
        RegisteredMembers matchedMember = AccessControlModel.getInstance().getRfidScanMatchedMember();
        if (matchedMember != null) {
            if (!matchedMember.getImage().isEmpty()) {
                File file = new File(matchedMember.getImage());
                if (file.exists()) {
                    Glide.with(ConfirmationScreenFragment.this)
                            .load(matchedMember.getImage())
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(user_img);
                }
            }
            user_name.setText(matchedMember.getFirstname());
        }else {
            user_img.setVisibility(View.GONE);
            user_name.setVisibility(View.GONE);
            face_score.setVisibility(View.GONE);
        }
    }

    private float titleSize(int titleLength) {
        if (titleLength > 500)
            return 28;
        else if (titleLength > 150)
            return 34;
        else return 40;
    }
    private float titleSizeSub(int titleLength) {
        if (titleLength > 500)
            return 22;
        else if (titleLength > 150)
            return 28;
        else return 32;
    }

    private void showSnackBarMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setHomeScreenTimer() {
        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().getFragmentManager().beginTransaction().remove(ConfirmationScreenFragment.this).commitAllowingStateLoss();
                    IrCameraActivity activity = (IrCameraActivity) getActivity();
                    activity.resumeScan();
                }
            }
        }, delayMilli * 1000);
    }

    private void showVaccinationIndicator(RegisteredMembers member, int vaccineValue) {
        if (vaccineValue == 1) {
            if (member.isDocument) {
                confirmation_screen_layout.setBackgroundResource(R.drawable.green_border);
            } else {
                confirmation_screen_layout.setBackgroundResource(R.drawable.red_border);
            }
        } else if (vaccineValue == 2) {
            confirmation_screen_layout.setBackgroundResource(R.drawable.red_border);
        }
    }

}