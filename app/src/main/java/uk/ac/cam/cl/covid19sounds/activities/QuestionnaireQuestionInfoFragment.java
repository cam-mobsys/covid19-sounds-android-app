package uk.ac.cam.cl.covid19sounds.activities;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionInfo;

import static uk.ac.cam.cl.covid19sounds.utils.sharedConfig.COVID_LOG_TAG;

public class QuestionnaireQuestionInfoFragment extends Fragment {
//class to show initial survey screen information.

    private static String ARG_QUESTION = "question";
    private QuestionInfo question;
    private OnQuestionInteractionListener mListener;

    public static QuestionnaireQuestionInfoFragment newInstance(Question question) {
        QuestionnaireQuestionInfoFragment fragment = new QuestionnaireQuestionInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_QUESTION, question);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = getArguments().getParcelable(ARG_QUESTION);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_questionnaire_info, container, false);
        TextView text = v.findViewById(R.id.questionnaire_info_title);
        // Set the value of the text field to the question text.
        text.setText(question.getText());
        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        text.setMovementMethod(LinkMovementMethod.getInstance());
        text.setTextColor(Color.BLACK);

        // check if we need to show the extra user bits
        if (question.getType().equals(Question.TYPE.INFO)) {
            Log.d(COVID_LOG_TAG, "Info screen detected, showing user bits"+ question.getText());

        }

        Button button = v.findViewById(R.id.questionnaire_info_submit);
        button.setText(button.getText());

        //if this the first quesiton in the initial surveyn then user is asked to give consent
        if(question.getID().contains("Initial_question"))  button.setText(R.string.consent);
        setupSubmitButton(button);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnQuestionInteractionListener) {
            mListener = (OnQuestionInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setupSubmitButton(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onInfoQuestionSubmission();
                }
            }
        });
    }

    public interface OnQuestionInteractionListener {
        void onInfoQuestionSubmission();
    }
}
