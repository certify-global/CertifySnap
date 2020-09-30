package com.certify.snap.activity;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.certify.snap.R;
import com.certify.snap.common.AppSettings;
import com.certify.snap.controller.GestureController;
import com.certify.snap.view.TimerAnimationView;

public class GestureFragment extends Fragment implements GestureController.GestureCallbackListener {

    private final String TAG = GestureFragment.class.getSimpleName();

    private TextView covidQuestionsText, titleView;
    private View view, view1, view2, view3;
    private ImageView image1, image2, image3, image4;
    private LinearLayout voiceLayout,  progressLayout;
    private RelativeLayout handGestureLayout;
    private Typeface rubiklight;
    private TimerAnimationView mTimerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gesture, container, false);
        this.view = view;

        initView();
        GestureController.getInstance().init(this.getContext());
        GestureController.getInstance().setCallbackListener(this);

        if (AppSettings.isEnableVoice()) {
           // handleQuestionnaireByVoice();
        } else {
            handleQuestionnaireByGesture();
            //handleQuestionnaireByVoice();
        }

        return view;
    }

    void initView() {
        covidQuestionsText = view.findViewById(R.id.covid_questions_text);
        titleView = view.findViewById(R.id.title_text_view);
        voiceLayout = view.findViewById(R.id.voice_layout);
        handGestureLayout = view.findViewById(R.id.hand_gesture_layout);
        progressLayout = view.findViewById(R.id.three_questions_layout);
        mTimerView = view.findViewById(R.id.timer_view);
        view1 = view.findViewById(R.id.threeQ_view1);
        view2 = view.findViewById(R.id.threeQ_view2);
        view3 = view.findViewById(R.id.threeQ_view3);
        image1 = view.findViewById(R.id.threeQ_image1);
        image2 = view.findViewById(R.id.threeQ_image2);
        image3 = view.findViewById(R.id.threeQ_image3);
        image4 = view.findViewById(R.id.threeQ_image4);

        rubiklight = Typeface.createFromAsset(getActivity().getAssets(),
                "rubiklight.ttf");
        covidQuestionsText.setTypeface(rubiklight);
        titleView.setTypeface(rubiklight);

        if (AppSettings.isEnableVoice()) {
          //  titleView.setText("Please answer the questions by saying Yes or No");
           // voiceLayout.setVisibility(View.VISIBLE);
            handGestureLayout.setVisibility(View.VISIBLE);
        } else {
           // voiceLayout.setVisibility(View.GONE);
            handGestureLayout.setVisibility(View.VISIBLE);

        }
    }

    private void handleQuestionnaireByVoice() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }
        GestureController.getInstance().initVoice(getContext());
        GestureController.getInstance().setSpeechRecognitionListener();
        GestureController.getInstance().startListening();
    }

    private void handleQuestionnaireByGesture() {
        GestureController.getInstance().initHandGesture();
    }

    private void setQuestion() {
        covidQuestionsText.setText(GestureController.getInstance().getQuestion());
    }

    private void uiUpdate() {
        titleView.setVisibility(View.VISIBLE);
        if (AppSettings.isEnableHandGesture()) {
            handGestureLayout.setVisibility(View.VISIBLE);
        }
        if (AppSettings.isEnableVoice()) {
            //voiceLayout.setVisibility(View.VISIBLE);
            handGestureLayout.setVisibility(View.VISIBLE);
        }
        progressLayout.setVisibility(View.GONE);
    }

    //-----> Voice code
    private void checkPermission() {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        //}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQuestionAnswered(String answeredQ) {
        getActivity().runOnUiThread(() -> {
            int index = GestureController.getInstance().getIndex();
         if (index== 0) {
                image1.setImageResource(R.drawable.tick);
            } else if (index== 1) {
                view1.setBackgroundColor(getResources().getColor(R.color.parrot_green));
                image2.setImageResource(R.drawable.tick);
            } else if (index== 2) {
                view2.setBackgroundColor(getResources().getColor(R.color.parrot_green));
                image3.setImageResource(R.drawable.tick);
            } else if (index== 3) {
                view3.setBackgroundColor(getResources().getColor(R.color.parrot_green));
                image4.setImageResource(R.drawable.tick);
            }

            uiUpdate();
            setQuestion();
        });
        GestureController.getInstance().setTimer();
    }

    @Override
    public void onAllQuestionsAnswered() {
        GestureController.getInstance().clearData();
        getActivity().runOnUiThread(this::closeFragment);
    }

    @Override
    public void onVoiceListeningStart() {
        getActivity().runOnUiThread(() -> mTimerView.start(8));
    }

    private void closeFragment() {
        if (getActivity() != null) {
            getActivity().getFragmentManager().beginTransaction().remove(GestureFragment.this).commitAllowingStateLoss();
            IrCameraActivity activity = (IrCameraActivity) getActivity();
            activity.resumeFromGesture();
        }
    }

}
