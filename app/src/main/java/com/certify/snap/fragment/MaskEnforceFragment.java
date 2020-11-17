package com.certify.snap.fragment;

import android.app.Activity;
import android.gesture.Gesture;
import android.graphics.Typeface;
import android.os.Bundle;

import android.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.common.AppSettings;
import com.certify.snap.controller.GestureController;


public class MaskEnforceFragment extends Fragment implements GestureController.GestureMECallbackListener {

    private Activity mActivity;
    private TextView maskEnforceDescription;
    private View view;
    private Typeface rubiklight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mask_enforce, container, false);
        GestureController.getInstance().setGestureMECallbackListener(this);
        this.view = view;

        initView();
        return view;
    }

    private void initView() {
        maskEnforceDescription = view.findViewById(R.id.mask_enforce_description);

        rubiklight = Typeface.createFromAsset(mActivity.getAssets(),
                "rubiklight.ttf");
        maskEnforceDescription.setTypeface(rubiklight);
        maskEnforceDescription.setText(AppSettings.getMaskEnforceMessage());
    }

    @Override
    public void onGestureMEDetected() {
        mActivity.runOnUiThread(() -> {
            GestureController.getInstance().setGestureMECallbackListener(null);
            IrCameraActivity activity = (IrCameraActivity) mActivity;
            if (activity != null) {
                activity.resumeFromMaskEnforcement();
            }
        });
    }

    @Override
    public void onMaskEnforceYes() {
        if (mActivity != null) {
            mActivity.runOnUiThread(() -> {
                mActivity.getFragmentManager().beginTransaction().remove(MaskEnforceFragment.this).commitAllowingStateLoss();
                IrCameraActivity activity = (IrCameraActivity) mActivity;
                if (activity != null) {
                    activity.resetMaskEnforcementGesture();
                }
            });
        }
    }
}