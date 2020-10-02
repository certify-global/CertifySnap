package com.certify.snap.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.ProgressDialog;
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
    private View view, q2view1, q3view1, q3view2, q4view1, q4view2, q4view3, q5view1, q5view2, q5view3, q5view4,
            q6view1, q6view2, q6view3, q6view4, q6view5, q7view1, q7view2, q7view3, q7view4, q7view5, q7view6;
    private ImageView q2image1, q2image2, q3image1, q3image2, q3image3, q4image1, q4image2, q4image3, q4image4,
            q5image1, q5image2, q5image3, q5image4, q5image5, q6image1, q6image2, q6image3, q6image4, q6image5, q6image6,
            q7image1, q7image2, q7image3, q7image4, q7image5, q7image6, q7image7;
    private LinearLayout voiceLayout, q2Layout, q3Layout, q4Layout, q5Layout, q6Layout, q7Layout;
    private RelativeLayout handGestureLayout;
    private Typeface rubiklight;
    private TimerAnimationView mTimerView;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gesture, container, false);
        this.view = view;

        initView();
        GestureController.getInstance().init(this.getContext());
        GestureController.getInstance().setCallbackListener(this);

        if (AppSettings.isEnableVoice()) {
            handleQuestionnaireByVoice();
        } else {
            handleQuestionnaireByGesture();
        }

        progressDialog = ProgressDialog.show(this.getContext(), "", "Fetching Questions, Please wait...");
        return view;
    }

    void initView() {
        covidQuestionsText = view.findViewById(R.id.covid_questions_text);
        titleView = view.findViewById(R.id.title_text_view);
        voiceLayout = view.findViewById(R.id.voice_layout);
        //handGestureLayout = view.findViewById(R.id.hand_gesture_layout);
        mTimerView = view.findViewById(R.id.timer_view);

        //q2 Layout
        q2Layout = view.findViewById(R.id.two_questions_layout);
        q2view1 = view.findViewById(R.id.twoQ_view1);
        q2image1 = view.findViewById(R.id.twoQ_image1);
        q2image2 = view.findViewById(R.id.twoQ_image2);


        //q3 Layout
        q3Layout = view.findViewById(R.id.three_questions_layout);
        q3view1 = view.findViewById(R.id.threeQ_view1);
        q3view2 = view.findViewById(R.id.threeQ_view2);
        q3image1 = view.findViewById(R.id.threeQ_image1);
        q3image2 = view.findViewById(R.id.threeQ_image2);
        q3image3 = view.findViewById(R.id.threeQ_image3);

        //q4 Layout
        q4Layout = view.findViewById(R.id.four_questions_layout);
        q4view1 = view.findViewById(R.id.fourQ_view1);
        q4view2 = view.findViewById(R.id.fourQ_view2);
        q4view3 = view.findViewById(R.id.fourQ_view3);
        q4image1 = view.findViewById(R.id.fourQ_image1);
        q4image2 = view.findViewById(R.id.fourQ_image2);
        q4image3 = view.findViewById(R.id.fourQ_image3);
        q4image4 = view.findViewById(R.id.fourQ_image4);

        //q5 Layout
        q5Layout = view.findViewById(R.id.five_questions_layout);
        q5view1 = view.findViewById(R.id.fiveQ_view1);
        q5view2 = view.findViewById(R.id.fiveQ_view2);
        q5view3 = view.findViewById(R.id.fiveQ_view3);
        q5view4 = view.findViewById(R.id.fiveQ_view4);
        q5image1 = view.findViewById(R.id.fiveQ_image1);
        q5image2 = view.findViewById(R.id.fiveQ_image2);
        q5image3 = view.findViewById(R.id.fiveQ_image3);
        q5image4 = view.findViewById(R.id.fiveQ_image4);
        q5image5 = view.findViewById(R.id.fiveQ_image5);

        //q6 Layout
        q6Layout = view.findViewById(R.id.six_questions_layout);
        q6view1 = view.findViewById(R.id.sixQ_view1);
        q6view2 = view.findViewById(R.id.sixQ_view2);
        q6view3 = view.findViewById(R.id.sixQ_view3);
        q6view4 = view.findViewById(R.id.sixQ_view4);
        q6view5 = view.findViewById(R.id.sixQ_view5);
        q6image1 = view.findViewById(R.id.sixQ_image1);
        q6image2 = view.findViewById(R.id.sixQ_image2);
        q6image3 = view.findViewById(R.id.sixQ_image3);
        q6image4 = view.findViewById(R.id.sixQ_image4);
        q6image5 = view.findViewById(R.id.sixQ_image5);
        q6image6 = view.findViewById(R.id.sixQ_image6);

        //q7 Layout
        q7Layout = view.findViewById(R.id.seven_questions_layout);
        q7view1 = view.findViewById(R.id.sevenQ_view1);
        q7view2 = view.findViewById(R.id.sevenQ_view2);
        q7view3 = view.findViewById(R.id.sevenQ_view3);
        q7view4 = view.findViewById(R.id.sevenQ_view4);
        q7view5 = view.findViewById(R.id.sevenQ_view5);
        q7view6 = view.findViewById(R.id.sevenQ_view6);
        q7image1 = view.findViewById(R.id.sevenQ_image1);
        q7image2 = view.findViewById(R.id.sevenQ_image2);
        q7image3 = view.findViewById(R.id.sevenQ_image3);
        q7image4 = view.findViewById(R.id.sevenQ_image4);
        q7image5 = view.findViewById(R.id.sevenQ_image5);
        q7image6 = view.findViewById(R.id.sevenQ_image6);
        q7image7 = view.findViewById(R.id.sevenQ_image7);


        rubiklight = Typeface.createFromAsset(getActivity().getAssets(),
                "rubiklight.ttf");
        covidQuestionsText.setTypeface(rubiklight);
        titleView.setTypeface(rubiklight);

        if (AppSettings.isEnableVoice()) {
            //  titleView.setText("Please answer the questions by saying Yes or No");
            // voiceLayout.setVisibility(View.VISIBLE);
            //handGestureLayout.setVisibility(View.VISIBLE);
        } else {
            // voiceLayout.setVisibility(View.GONE);
            //handGestureLayout.setVisibility(View.VISIBLE);

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
           // handGestureLayout.setVisibility(View.VISIBLE);
        }
        if (AppSettings.isEnableVoice()) {
            //voiceLayout.setVisibility(View.VISIBLE);
            //handGestureLayout.setVisibility(View.VISIBLE);
        }
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
           int questionsCount = GestureController.getInstance().getQuestionAnswerMap().size();
            if (questionsCount == 2) {
                twoQuestions(index);
            } else if (questionsCount == 3) {
                threeQuestions(index);
            } else if (questionsCount == 4) {
                fourQuestions(index);
            } else if (questionsCount == 5) {
                fiveQuestions(index);
            } else if (questionsCount == 6) {
                sixQuestions(index);
            } else if (questionsCount == 7) {
                sevenQuestions(index);
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

    @Override
    public void onQuestionsReceived() {
        getActivity().runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            covidQuestionsText.setText(GestureController.getInstance().getQuestion());
            int questionsCount = GestureController.getInstance().getQuestionAnswerMap().size();
            if (questionsCount == 2) {
                q2Layout.setVisibility(View.VISIBLE);
            } else if (questionsCount == 3) {
                q3Layout.setVisibility(View.VISIBLE);
            } else if (questionsCount == 4) {
                q4Layout.setVisibility(View.VISIBLE);
            } else if (questionsCount == 5) {
                q5Layout.setVisibility(View.VISIBLE);
            } else if (questionsCount == 6) {
                q6Layout.setVisibility(View.VISIBLE);
            } else if (questionsCount == 7) {
                q7Layout.setVisibility(View.VISIBLE);
            }
            resetQuestionProgressView();
        });
    }

    @Override
    public void onQuestionsNotReceived() {
        getActivity().runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(this.getContext(), "Failed to get the Questions, Closing Gesture screen", Toast.LENGTH_LONG).show();
            if (getActivity() != null) {
                getActivity().getFragmentManager().beginTransaction().remove(GestureFragment.this).commitAllowingStateLoss();
                IrCameraActivity activity = (IrCameraActivity) getActivity();
                activity.resetGesture();
            }
        });
    }

    private void closeFragment() {
        if (getActivity() != null) {
            getActivity().getFragmentManager().beginTransaction().remove(GestureFragment.this).commitAllowingStateLoss();
            IrCameraActivity activity = (IrCameraActivity) getActivity();
            activity.resumeFromGesture();
        }
    }

    private void twoQuestions(int index) {
        if (index == 1) {
            q2image1.setImageResource(R.drawable.tick);
            q2view1.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 2) {
            q2image2.setImageResource(R.drawable.tick);
        }
    }

    private void threeQuestions(int index) {
        if (index == 1) {
            q3image1.setImageResource(R.drawable.tick);
            q3view1.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 2) {
            q3image2.setImageResource(R.drawable.tick);
            q3view2.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 3) {
            q3image3.setImageResource(R.drawable.tick);
        }
    }

    private void fourQuestions(int index) {

        if (index == 1) {
            q4image1.setImageResource(R.drawable.tick);
            q4view1.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 2) {
            q4image2.setImageResource(R.drawable.tick);
            q4view2.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 3) {
            q4image3.setImageResource(R.drawable.tick);
            q4view3.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 4) {
            q4image4.setImageResource(R.drawable.tick);
        }

    }

    private void fiveQuestions(int index) {
        if (index == 1) {
            q5image1.setImageResource(R.drawable.tick);
            q5view1.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 2) {
            q5image2.setImageResource(R.drawable.tick);
            q5view2.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 3) {
            q5image3.setImageResource(R.drawable.tick);
            q5view3.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 4) {
            q5image4.setImageResource(R.drawable.tick);
            q5view4.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 5) {
            q5image5.setImageResource(R.drawable.tick);
        }
    }

    private void sixQuestions(int index) {
        if (index == 1) {
            q6image1.setImageResource(R.drawable.tick);
            q6view1.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 2) {
            q6image2.setImageResource(R.drawable.tick);
            q6view2.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 3) {
            q6image3.setImageResource(R.drawable.tick);
            q6view3.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 4) {
            q6image4.setImageResource(R.drawable.tick);
            q6view4.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 5) {
            q6image5.setImageResource(R.drawable.tick);
            q6view5.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 6) {
            q6image6.setImageResource(R.drawable.tick);
        }
    }

    private void sevenQuestions(int index) {
        if (index == 1) {
            q7image1.setImageResource(R.drawable.tick);
            q7view1.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 2) {
            q7image2.setImageResource(R.drawable.tick);
            q7view2.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 3) {
            q7image3.setImageResource(R.drawable.tick);
            q7view3.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 4) {
            q7image4.setImageResource(R.drawable.tick);
            q7view4.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 5) {
            q7image5.setImageResource(R.drawable.tick);
            q7view5.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 6) {
            q7image6.setImageResource(R.drawable.tick);
            q7view6.setBackgroundColor(getResources().getColor(R.color.parrot_green));
        } else if (index == 7) {
            q7image7.setImageResource(R.drawable.tick);
        }
    }
    
    private void resetQuestionProgressView(){
        // reset all the views
       q2image1.setImageResource(R.drawable.no_tick);
       q2image2.setImageResource(R.drawable.no_tick);

       q3image1.setImageResource(R.drawable.no_tick);
       q3image2.setImageResource(R.drawable.no_tick);
       q3image3.setImageResource(R.drawable.no_tick);

        q4image1.setImageResource(R.drawable.no_tick);
        q4image2.setImageResource(R.drawable.no_tick);
        q4image3.setImageResource(R.drawable.no_tick);
        q4image4.setImageResource(R.drawable.no_tick);

        q5image1.setImageResource(R.drawable.no_tick);
        q5image2.setImageResource(R.drawable.no_tick);
        q5image3.setImageResource(R.drawable.no_tick);
        q5image4.setImageResource(R.drawable.no_tick);
        q5image5.setImageResource(R.drawable.no_tick);

        q6image1.setImageResource(R.drawable.no_tick);
        q6image2.setImageResource(R.drawable.no_tick);
        q6image3.setImageResource(R.drawable.no_tick);
        q6image4.setImageResource(R.drawable.no_tick);
        q6image5.setImageResource(R.drawable.no_tick);
        q6image6.setImageResource(R.drawable.no_tick);

        q7image1.setImageResource(R.drawable.no_tick);
        q7image2.setImageResource(R.drawable.no_tick);
        q7image3.setImageResource(R.drawable.no_tick);
        q7image4.setImageResource(R.drawable.no_tick);
        q7image5.setImageResource(R.drawable.no_tick);
        q7image6.setImageResource(R.drawable.no_tick);
        q7image7.setImageResource(R.drawable.no_tick);


        q2view1.setBackgroundColor(getResources().getColor(R.color.very_light_gray));

        q3view1.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q3view2.setBackgroundColor(getResources().getColor(R.color.very_light_gray));

        q4view1.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q4view2.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q4view3.setBackgroundColor(getResources().getColor(R.color.very_light_gray));

        q5view1.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q5view2.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q5view3.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q5view4.setBackgroundColor(getResources().getColor(R.color.very_light_gray));

        q6view1.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q6view2.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q6view3.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q6view4.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q6view5.setBackgroundColor(getResources().getColor(R.color.very_light_gray));

        q7view1.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q7view2.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q7view3.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q7view4.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q7view5.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
        q7view6.setBackgroundColor(getResources().getColor(R.color.very_light_gray));
    }

}
