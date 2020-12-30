package uk.ac.cam.cl.covid19sounds.activities;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionSlider;

import static uk.ac.cam.cl.covid19sounds.utils.sharedConfig.COVID_LOG_TAG;

public class QuestionnaireQuestionSliderFragment extends Fragment {
//class to handle slider based questions possibly in future.

    private static String ARG_QUESTION = "question";
    private QuestionSlider question;
    private OnQuestionInteractionListener mListener;

    private SeekBar slider;
    private TextView sBarText;
    private String[] options;

    /**
     * Initiate the fragment to include the parcelable question.
     *
     * @param question the type of question we have
     * @return returns the QuestionnaireQuestionSliderFragment
     */
    public static QuestionnaireQuestionSliderFragment newInstance(Question question) {
        QuestionnaireQuestionSliderFragment fragment = new QuestionnaireQuestionSliderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_QUESTION, question);

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when creating QuestionnaireQuestionSliderFragment instance
     *
     * @param savedInstanceState returns an instance of the view
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchQuestionOptions();
    }

    /**
     * Fetches the question options which is used upon creation.
     */
    private void fetchQuestionOptions() {
        if (getArguments() != null) {
            question = getArguments().getParcelable(ARG_QUESTION);
            // check if the question object is null.
            if (question == null) {
                Log.e(COVID_LOG_TAG, "Null pointer exception occurred while getting options");
            } else {
                // get question options, since we are not null.
                options = question.getOptions();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_questionnaire_slider, container, false);

        // Question text
        TextView text = v.findViewById(R.id.questionnaire_slider_text);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());

        // Slider
        slider = v.findViewById(R.id.questionnaire_slider_bar);
        slider.setMax(options.length - 1);
        sBarText = v.findViewById(R.id.seekBarText);
        sBarText.setText(getLabel(slider.getProgress()));
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sBarText.setText(getLabel(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Initialise the submit button on the spinner page.
        Button button = v.findViewById(R.id.questionnaire_slider_submit);
        String buttonText = "Next";
        button.setText(buttonText);
        setupSubmitButton(button);

        return v;
    }

    private String getLabel(int progress) {
        return options[progress];
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
                question.setAnswer(options[slider.getProgress()]);
                if (mListener != null) {
                    mListener.onSliderQuestionSubmission();
                }
            }
        });
    }

    public interface OnQuestionInteractionListener {
        void onSliderQuestionSubmission();
    }
}

