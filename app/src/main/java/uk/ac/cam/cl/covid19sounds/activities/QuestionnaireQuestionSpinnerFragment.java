package uk.ac.cam.cl.covid19sounds.activities;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionSpinner;

public class QuestionnaireQuestionSpinnerFragment extends Fragment {
//class to handle spinner based  questions possibly in future.

    private static String ARG_QUESTION = "question";
    private QuestionSpinner question;
    private OnQuestionInteractionListener mListener;

    private Spinner spinner;

    public static QuestionnaireQuestionSpinnerFragment newInstance(Question question) {
        QuestionnaireQuestionSpinnerFragment fragment = new QuestionnaireQuestionSpinnerFragment();
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
        View v = inflater.inflate(R.layout.fragment_questionnaire_spinner, container, false);


        // Define the page elements and events
        TextView text = v.findViewById(R.id.questionnaire_spinner_text);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        spinner = v.findViewById(R.id.questionnaire_spinner_spinner);

        // Set the value of the text field to the question text.
        text.setText(question.getText());

        // Initialise the submit button on the spinner page.
        Button button = v.findViewById(R.id.questionnaire_spinner_submit);
        String buttonText = "Next";
        button.setText(buttonText);
        setupSubmitButton(button);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, question.getOptions());
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

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
                question.setAnswer(spinner.getSelectedItem().toString());
                if (mListener != null) {
                    mListener.onSpinnerQuestionSubmission();
                }
            }
        });
    }

    public interface OnQuestionInteractionListener {
        void onSpinnerQuestionSubmission();
    }
}
