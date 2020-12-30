package uk.ac.cam.cl.covid19sounds.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.DataConstants;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups.DailySurveyQuestionGroup;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups.OnboardingQuestionGroup;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups.QuestionGroup;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups.QuestionGroupLoadException;
import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;
import uk.ac.cam.cl.covid19sounds.notifications.MorningMessager;

import static uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups.QuestionGroup.QUESTION_TYPES.DAILY_SURVEY;
import static uk.ac.cam.cl.covid19sounds.utils.sharedConfig.COVID_LOG_TAG;

//class to display questions as an activity  asked in the surveys
public class QuestionnaireActivity extends AppCompatActivity implements
        QuestionnaireQuestionSliderFragment.OnQuestionInteractionListener,
        QuestionnaireQuestionTextFragment.OnQuestionInteractionListener,
        QuestionnaireQuestionInfoFragment.OnQuestionInteractionListener,
        QuestionnaireQuestionSpinnerFragment.OnQuestionInteractionListener,
        QuestionnaireQuestionCheckboxFragment.OnQuestionInteractionListener,
        QuestionnaireQuestionRadioButtonsFragment.OnQuestionInteractionListener,
        QuestionnaireQuestionAudioRecordFragment.OnQuestionInteractionListener
{

    public static final String QUESTIONNAIRE_KEY = "QuestionnaireKey";
    private int currentQuestion = 0;
    private QuestionGroup questions;
    //private TextView questionnaireTitle;

    private void displayQuestion(Question question) {
        FragmentManager questionFragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = questionFragmentManager.beginTransaction();

        // Select the correct fragment to use, depending on the question type.
        switch (question.getType()) {
            case Question.TYPE.INFO: {
                QuestionnaireQuestionInfoFragment q = QuestionnaireQuestionInfoFragment.newInstance(question);
                fragmentTransaction.replace(R.id.questionnaire_fragment_container, q, "");
                break;
            }
            case Question.TYPE.TEXT: {
                QuestionnaireQuestionTextFragment q = QuestionnaireQuestionTextFragment.newInstance(question);
                fragmentTransaction.replace(R.id.questionnaire_fragment_container, q, "");
                break;
            }
            case Question.TYPE.SPINNER: {
                QuestionnaireQuestionSpinnerFragment q = QuestionnaireQuestionSpinnerFragment.newInstance(question);
                fragmentTransaction.replace(R.id.questionnaire_fragment_container, q, "");
                break;
            }
            case Question.TYPE.SLIDER: {
                QuestionnaireQuestionSliderFragment q = QuestionnaireQuestionSliderFragment.newInstance(question);
                fragmentTransaction.replace(R.id.questionnaire_fragment_container, q, "");
                break;
            }
            case Question.TYPE.CHECKBOX: {
                QuestionnaireQuestionCheckboxFragment q = QuestionnaireQuestionCheckboxFragment.newInstance(question);
                fragmentTransaction.replace(R.id.questionnaire_fragment_container, q, "");
                break;
            }
            case Question.TYPE.RADIO_BUTTONS: {
                QuestionnaireQuestionRadioButtonsFragment q = QuestionnaireQuestionRadioButtonsFragment.newInstance(question);
                fragmentTransaction.replace(R.id.questionnaire_fragment_container, q, "");
                break;
            }
            case Question.TYPE.AUDIO_RECORDER: {
                QuestionnaireQuestionAudioRecordFragment q = QuestionnaireQuestionAudioRecordFragment.newInstance(question);
                fragmentTransaction.replace(R.id.questionnaire_fragment_container, q, "");
                break;
            }
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            currentQuestion -= 1;
            getFragmentManager().popBackStack();
        } else {
            if (UserOnboardingPrefs.getInstance(this).getCompletedOnboarding())
                this.finish();
        }
    }

    private void displayNextQuestion() {
        // Display the next question if there is a question.
        if (currentQuestion < questions.size()) {

            Question nextQuestion = questions.get(currentQuestion);
            //System.out.println("The next question is: "+nextQuestion.getID()+","+nextQuestion.hasDependency());

            if (nextQuestion == null) {
                return;
            }
            currentQuestion += 1;

            if (nextQuestion.hasDependency()) {
                //display the question depending on the current answer and if this question has dependency
                //System.out.println("The next question has dependency so check the answer");

                if (questions.checkAnswer(nextQuestion.getDependencyID(), nextQuestion.getDependencyAnswer())) {
                    //System.out.println("The next question is displayQuestion ");

                    displayQuestion(nextQuestion);
                } else {
                    //System.out.println("The next question is displayNextQuestion ");

                    displayNextQuestion();
                }
            } else {
                //this question has no dependency on the answer
                //System.out.println("The next question has no dependency so check the answer");

                displayQuestion(nextQuestion);
            }
        } else {
            //all the questions have been answered. it is time to handle the results.
            questions.handleResult();
            questions.setResult(this);
            if (Objects.requireNonNull(getIntent().getStringExtra(QUESTIONNAIRE_KEY)).contains(DAILY_SURVEY)) {
                // we need to start a new activity here to get location only once
                Intent intent = new Intent(this, LocationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                this.startActivity(intent);
            }
            MorningMessager messenger = new MorningMessager();
            // Set a reminder to take the survey next time at 8 o clock
            messenger.setMorningMessager(this, 8, 0);
            finish();
         }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(false);      // Disable the button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Remove the left caret
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(
                        Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).cancelAll(); //cancel all the app notifications as user is taking the survey

        try {
            Intent intent = getIntent();
            String questionnaire = intent.getStringExtra(QUESTIONNAIRE_KEY);
            //String questionType;
            if (questionnaire == null) {
                finish();
            }
            else if ((questionnaire.equals(QuestionGroup.QUESTIONNAIRES.DAILY_SURVEY_US)))
            {
                //start a daily survey
                this.questions = new DailySurveyQuestionGroup(this);
            }
            else if ((questionnaire.equals(QuestionGroup.QUESTIONNAIRES.ONBOARDING_US))) {
                //start the initial survey
                this.questions = new OnboardingQuestionGroup(this);
            } else {
                finish();
            }
            //display questions for the survey
            displayNextQuestion();
        } catch (QuestionGroupLoadException e) {
            e.printStackTrace();
        }
    }

    // React to the submission button events on the fragments, when the questions have been answered and display the next question.
    @Override
    public void onSpinnerQuestionSubmission() {
        displayNextQuestion();
    }

    @Override
    public void onTextQuestionSubmission() {
        displayNextQuestion();
    }

    @Override
    public void onInfoQuestionSubmission() {
        displayNextQuestion();
    }

    @Override
    public void onSliderQuestionSubmission() {
        displayNextQuestion();
    }

    @Override
    public void onRadioButtonsQuestionSubmission() {
        displayNextQuestion();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onAudioRecordQuestionSubmission() {
        displayNextQuestion();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //trying to handle permission request for audio recording asked in audio record fragment.
         //System.out.println("*********"+"requestCode in onRequestPermissionsResult " + requestCode);
        Log.d(COVID_LOG_TAG, "permissions in onRequestPermissionsResult " + permissions.length);
        Log.d(COVID_LOG_TAG, "grantResults in onRequestPermissionsResult " + grantResults.length);
        if (requestCode == DataConstants.AUDIO_PERMISSION_REQUEST_CODE) { //local sd storage required for storing logs
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission granted for audio");
                //permission granted for accessing the microphone. so we can record the audio now.

            } else {
                System.out.println("Permission Denied");

                //permission denied for accessing the microphone. so we cannot record the audio.
                //just try to ask for location sample now.

                Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                UserOnboardingPrefs.getInstance(getApplicationContext())
                        .setQuestionnaireLastCompleted(System.currentTimeMillis());
                this.finish();


            }
        }

    }
}