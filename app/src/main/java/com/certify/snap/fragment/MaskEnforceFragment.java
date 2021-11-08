package com.certify.snap.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.common.AppSettings;
import com.certify.snap.controller.GestureController;
import com.google.android.material.snackbar.Snackbar;


public class MaskEnforceFragment extends Fragment implements GestureController.GestureMECallbackListener {

    private Activity mActivity;
    private TextView maskEnforceDescription;
    private Button btStartMask;
    private View view;
    private Typeface rubiklight;
    private Snackbar snackbar;

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
        GestureController.getInstance().startWaveHandTimer();
        if (GestureController.getInstance().getAnswerType() == GestureController.AnswerType.Touch) {
            btStartMask.setVisibility(View.VISIBLE);
            GestureController.getInstance().cancelWaveHandTimer();
        } else btStartMask.setVisibility(View.GONE);
        btStartMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // GestureController.getInstance().cancelWaveHandTimer();
                mActivity.runOnUiThread(() -> {
                    GestureController.getInstance().setGestureMECallbackListener(null);
                    IrCameraActivity activity = (IrCameraActivity) mActivity;
                    if (activity != null) {
                        activity.resumeFromMaskEnforcement();
                    }
                });
            }
        });
        return view;
    }

    private void initView() {
        maskEnforceDescription = view.findViewById(R.id.mask_enforce_description);
        btStartMask = view.findViewById(R.id.bt_wave_start_mask);
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
    public void onLeftHandWave() {
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

    @Override
    public void onWaveHandTimeout() {
        if (mActivity != null) {
            mActivity.runOnUiThread(() -> {
                if (mActivity.isDestroyed() || mActivity.isFinishing()) return;
                snackbar = Snackbar.make(view, getString(R.string.gesture_timeout_msg), Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            });
        }
    }

    @Override
    public void onWaveHandReset() {
        if (mActivity != null) {
            mActivity.runOnUiThread(this::dismissSnackbar);
        }
    }

    private void dismissSnackbar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }
}