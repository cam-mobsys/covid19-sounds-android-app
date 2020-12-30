package uk.ac.cam.cl.covid19sounds.activities;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionRadioButtons;

public class QuestionnaireQuestionRadioButtonsFragment extends Fragment {
//class to handle radio buttons based questions

    private static String ARG_QUESTION = "question";
    private QuestionRadioButtons question;
    private OnQuestionInteractionListener mListener;

    private RadioGroup radioButtons;


    public static QuestionnaireQuestionRadioButtonsFragment newInstance(Question question) {
        QuestionnaireQuestionRadioButtonsFragment fragment = new QuestionnaireQuestionRadioButtonsFragment();
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
        View v = inflater.inflate(R.layout.fragment_questionnaire_radio_buttons, container, false);

        // Define the page elements and events
        TextView text = v.findViewById(R.id.questionnaire_radio_buttons_text);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        radioButtons = v.findViewById(R.id.questionnaire_radio_buttons_buttons);
        String[] options = question.getOptions();
        int optionnumber = 0;
        for (String option : options) {
            RadioButton button = new RadioButton(getActivity());
            button.setText(option);
            button.setTextColor(Color.BLACK);
            button.setId(optionnumber);
            optionnumber = optionnumber + 1;


            radioButtons.addView(button);
        }

        // Set the value of the text field to the question text.
        text.setText(question.getText());
        text.setTextColor(Color.BLACK);


        // Initialise the submit button on the radioButtons page.
        Button button = v.findViewById(R.id.questionnaire_radio_buttons_submit);
        //button.setText(buttonText);
        button.setText(button.getText());

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
                int id = radioButtons.getCheckedRadioButtonId();
                RadioButton button = radioButtons.findViewById(id);
                if (button != null) { // Button may be null if no answer is selected

                    //get a value for this option and set it in the answer
                    String temp = getCommonValue(question.getID(), button.getId());
                    question.setAnswer(temp);
                    if (mListener != null) {
                        mListener.onRadioButtonsQuestionSubmission();
                    }
                }
            }
        });
    }

    public String getCommonValue(String qid, int buttonid) {
        //depending on the number of radio button and the question pick the answer value

        String answer_value = null;
        System.out.println(qid+","+buttonid);

        switch (qid) {
            case "User_sex":
                switch (buttonid) {
                    case 0:
                        answer_value = "Male";
                        break;
                    case 1:
                        answer_value = "Female";
                        break;
                    case 2:
                        answer_value = "Other";
                        break;
                    case 3:
                        answer_value = "Prefer not to say";
                        break;
                }
                break;
            case "User_age":
                switch (buttonid) {
                    case 0:
                        answer_value = "Under 20";
                        break;
                    case 1:
                        answer_value = "20-29";
                        break;
                    case 2:
                        answer_value = "30-39";
                        break;
                    case 3:
                        answer_value = "40-49";
                        break;

                    case 4:
                        answer_value = "50-59";
                        break;
                    case 5:
                        answer_value = "60-69";
                        break;

                    case 6:
                        answer_value = "70-79";
                        break;
                    case 7:
                        answer_value = "80-89";
                        break;
                    case 8:
                        answer_value = "90 or older";
                        break;
                    case 9:
                        answer_value = "Prefer not to say";
                        break;
                }


                break;
            case "Smoking":
                switch (buttonid) {
                    case 0:
                        answer_value = "Never smoked";
                        break;
                    case 1:
                        answer_value = "Prefer not to say";
                        break;
                    case 2:
                        answer_value = "Ex-smoker";
                        break;
                    case 3:
                        answer_value = "Current smoker (1-10 cigarettes per day)";
                        break;
                    case 4:
                        answer_value = "Current smoker (11 or more cigarettes per day)";
                        break;
                }
                break;
            case "covid":
                switch (buttonid) {
                    case 0:
                        answer_value = "Never Tested";
                        break;
                    case 1:
                        answer_value = "Positive";

                        break;
                    case 2:
                        answer_value = "Negative";
                        break;

                    case 3:
                        answer_value = "Prefer not to say";
                        break;
                }
                break;
            case "never_covid":
                switch (buttonid) {
                    case 0:
                        answer_value = "Yes, Now";
                        break;
                    case 1:
                        answer_value = "Yes, In last 14 days";
                        break;
                    case 2:
                        answer_value = "Yes, More than 14 days ago";
                        break;
                    case 3:
                        answer_value = "Never";
                        break;


                }
                break;
            case "positive_covid":
                switch (buttonid) {
                    case 0:
                        answer_value = "In the last 14 days";
                        break;
                    case 1:
                        answer_value = "More than 14 days ago";
                        break;

                }
                break;
            case "negative_covid":
                switch (buttonid) {
                    case 0:
                        answer_value = "Never";
                        break;
                    case 1:
                        answer_value = "In the last 14 days";
                        break;
                    case 2:
                        answer_value = "More than 14 days ago";
                        break;

                }
                break;
            case "hospital":
                switch (buttonid) {
                    case 0:
                        answer_value = "Yes";
                        break;
                    case 1:
                        answer_value = "No";

                        break;
                    case 2:
                        answer_value = "Prefer not to say";
                        break;
                }
                break;
        }
        //System.out.println("ans value"+answer_value);

        return answer_value;
    }

    public interface OnQuestionInteractionListener {
        void onRadioButtonsQuestionSubmission();
    }
}

