package uk.ac.cam.cl.covid19sounds.activities;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionText;

public class QuestionnaireQuestionTextFragment extends Fragment {
//class to handle text entry based  questions possibly in future.

    private static String ARG_QUESTION = "question";
    private QuestionText question;
    private OnQuestionInteractionListener mListener;

    private EditText textEntry;

    public static QuestionnaireQuestionTextFragment newInstance(Question question) {
        QuestionnaireQuestionTextFragment fragment = new QuestionnaireQuestionTextFragment();
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_questionnaire_text, container, false);

        // Initialise the submit button on the spinner page.
        final Button button = v.findViewById(R.id.questionnaire_text_submit);
        String buttonText = "Next";
        button.setText(buttonText);
        setupSubmitButton(button);

        TextView text = v.findViewById(R.id.questionnaire_text_title);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        textEntry = v.findViewById(R.id.questionnaire_text_entry);
        textEntry.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                button.setVisibility(View.INVISIBLE);
                return false;
            }

        });
        textEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                button.setVisibility(View.VISIBLE);
                return false;
            }
        });

        text.setText(question.getText());


        return v;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof QuestionnaireQuestionTextFragment.OnQuestionInteractionListener) {
            mListener = (QuestionnaireQuestionTextFragment.OnQuestionInteractionListener) context;
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

        final QuestionnaireQuestionTextFragment self = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String answer = textEntry.getText().toString();

                // Check if the textarea is empty and if so, show an error.
                if (TextUtils.isEmpty(answer)) {
                    textEntry.setError("Enter a value");
                } else {

                    // Hide the soft keyboard
                    View activityView = self.getActivity().getCurrentFocus();
                    if (activityView != null) {
                        InputMethodManager imm = (InputMethodManager) activityView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    question.setAnswer(textEntry.getText().toString());
                    if (mListener != null) {
                        mListener.onTextQuestionSubmission();
                    }
                }
            }
        });
    }

    public interface OnQuestionInteractionListener {
        void onTextQuestionSubmission();
    }
}

