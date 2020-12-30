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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionCheckbox;
//class to handle checkbox based questions

public class QuestionnaireQuestionCheckboxFragment extends Fragment {

    private static String ARG_QUESTION = "question";
    private QuestionCheckbox question;
    private OnQuestionInteractionListener mListener;

    private ArrayList<CheckBox> checkBoxArrayList = new ArrayList<>();

    public static QuestionnaireQuestionCheckboxFragment newInstance(Question question) {
        QuestionnaireQuestionCheckboxFragment fragment = new QuestionnaireQuestionCheckboxFragment();
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
        View v = inflater.inflate(R.layout.fragment_questionnaire_checkbox, container, false);

        TextView text = v.findViewById(R.id.questionnaire_checkbox_text);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        LinearLayout checkBoxView = v.findViewById(R.id.questionnaire_checkbox_list);
        text.setText(question.getText());
        text.setTextColor(Color.BLACK);

        Button button = v.findViewById(R.id.questionnaire_checkbox_submit);
        button.setText(button.getText());
        setupSubmitButton(button);

        for (String option : question.getOptions()) {

            // Set up checkbox
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setChecked(false);
            checkBox.setText(option);
            checkBox.setTextSize(15);
            checkBox.setTextColor(Color.BLACK);

            checkBoxArrayList.add(checkBox);
            checkBoxView.addView(checkBox);
        }

        for (CheckBox checkBox : checkBoxArrayList) {
            if (excludesOtherOptions(checkBox)) {
                //try to disable other options when user selects nobody, none, no urges or prefer not to say.

                disableOtherOptionsWhenChecked(checkBox, checkBoxArrayList);

            }
        }

        checkBoxView.setScrollContainer(true);

        return v;
    }

    private void disableOtherOptionsWhenChecked(final CheckBox checkBox, final ArrayList<CheckBox> checkBoxArrayList) {
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CheckBox c : checkBoxArrayList) {
                    if (c != checkBox) {
                        c.setChecked(false);
                        c.setEnabled(!checkBox.isChecked());
                    }
                }
            }
        });
    }

    private boolean excludesOtherOptions(CheckBox checkBox) {
        String text = checkBox.getText().toString();
        //try to exclude other options when user selects nobody, none, no urges or prefer not to say.
        return (text.contains("Nobody") || text.contains("None") || text.contains("No urges") || text.contains("Prefer not to say"));
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
                if (numChecked() > 0) {
                    //if user has picked many answers then set them in the final response

                    question.setAnswer(getChecked(question.getID()));
                    if (mListener != null) {
                        mListener.onSpinnerQuestionSubmission();
                    }
                }
            }
        });
    }

    private int numChecked() {
        //check how many responses user has picked for this question.
        int i = 0;
        for (CheckBox cb : checkBoxArrayList) {
            if (cb.isChecked()) {
                i += 1;
            }
        }
        return i;
    }



    /**
     * Parse checkboxes and concatenate them into a string
     *
     * @return the parsed option names formatted as a csv string.
     */
    private String getChecked(String qid) {
        // use a string builder to do the string generation
        StringBuilder sb_result = new StringBuilder();
        String prefix = "";

        // wizardy that allows us to not use the check
        //
        // Note: might be worth using StringJoiner if our target JDK is > 8
        int index = 0;
        String temp;
        for (CheckBox checkBox : checkBoxArrayList) {

            if (checkBox.isChecked()) {
                sb_result.append(prefix);
                prefix = ",";
                //call a commoner method
                temp = getCommonValue(qid,index);

                sb_result.append(temp);


            }
            index = index + 1;
        }

        // build and return the parsed string
        return sb_result.toString();
    }



    public String getCommonValue(String qid, int checkboxid) {
        //depending on the number of checked box and the question pick the answer value

        String answer_value = null;
        //System.out.println(qid+","+checkboxid);

        if (qid.equals("Medical_history")) {
            switch (checkboxid) {
                case 0:
                    answer_value = "None";
                    break;
                case 1:
                    answer_value = "Prefer not to say";
                    break;
                case 2:
                    answer_value = "Asthma";
                    break;
                case 3:
                    answer_value = "Cystic fibrosis";
                    break;
                case 4:
                    answer_value = "COPD/emphysema";
                    break;
                case 5:
                    answer_value = "Pulmonary fibrosis";
                    break;
                case 6:
                    answer_value = "Other lung disease";
                    break;
                case 7:
                    answer_value = "High blood pressure";
                    break;
                case 8:
                    answer_value = "Angina";
                    break;

                case 9:
                    answer_value = "Previous stroke or Transient Ischaemic Attack";
                    break;

                case 10:
                    answer_value = "Previous heart attack";
                    break;
                case 11:
                    answer_value = "Valvular heart disease";
                    break;
                case 12:
                    answer_value = "Other heart disease";
                    break;
                case 13:
                    answer_value = "Diabetes";
                    break;
                case 14:
                    answer_value = "Cancer";
                    break;
                case 15:
                    answer_value = "Previous organ transplant";
                    break;
                case 16:
                    answer_value = "HIV or an impaired immune system";
                    break;
                case 17:
                    answer_value = "Other long-term condition";
                    break;
            }
        }


        if (qid.equals("symptoms")) {
            switch (checkboxid) {
                case 0:
                    answer_value = "None";
                    break;
                case 1:
                    answer_value = "Prefer not to say";
                    break;
                case 2:
                    answer_value = "Fever (feeling feverish or warmer than usual)";
                    break;
                case 3:
                    answer_value = "Chills";
                    break;
                case 4:
                    answer_value = "Dry cough";
                    break;
                case 5:
                    answer_value = "Wet cough";
                    break;
                case 6:
                    answer_value = "Difficulty breathing or feeling short of breath";
                    break;
                case 7:
                    answer_value = "Tightness in your chest";
                    break;
                case 8:
                    answer_value = "Loss of taste and smell";
                    break;

                case 9:
                    answer_value = "Dizziness, confusion or vertigo";
                    break;

                case 10:
                    answer_value = "Headache";
                    break;
                case 11:
                    answer_value = "Muscle aches";
                    break;
                case 12:
                    answer_value = "Sore throat";
                    break;

                case 13:
                    answer_value = "Runny or blocked nose";
                    break;

            }
        }

        return answer_value;
    }


    public interface OnQuestionInteractionListener {
        void onSpinnerQuestionSubmission();
    }
}
