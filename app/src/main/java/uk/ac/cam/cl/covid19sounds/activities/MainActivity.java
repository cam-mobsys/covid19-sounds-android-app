package uk.ac.cam.cl.covid19sounds.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups.QuestionGroup;
import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //String current = getResources().getConfiguration().locale.getDisplayLanguage();
        //System.out.println("locale:"+current);

        setContentView(R.layout.activity_main);
        UserOnboardingPrefs prefs = UserOnboardingPrefs.getInstance(this);
        if (!prefs.getCompletedOnboarding()) {
            //if the user has not completed initial survey about demograhics
            showQuestionnaire(QuestionGroup.QUESTIONNAIRES.ONBOARDING_US);
        }
        Button b = findViewById(R.id.startButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //otherwise show daily survey
                showQuestionnaire(QuestionGroup.QUESTIONNAIRES.DAILY_SURVEY_US);
            }
        });
        TextView textView = findViewById(R.id.textView);

        textView.setMovementMethod(new ScrollingMovementMethod());
        //textView.setMovementMethod(new LinkMovementMethod().getInstance());
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        // check if we can answer the questionnaire/survey today

        canAnswerQuestionnaire();

    }

    private String generatePostQuestionnaireUserBits() {
        String userID = UserOnboardingPrefs.getInstance(this).getUserID();
        //show the user id at the completion of the survey
        return getString(R.string.post_questionnaire_user_bits, userID);
    }

    @Override
    public void onResume() {
        super.onResume();
        //System.out.println("onresume");
        // check if we can answer the questionnaire today
        canAnswerQuestionnaire();

    }

    /**
     * Checks if the user is able to answer the questionnaire today.
     */
    private void canAnswerQuestionnaire() {
        if (questionnaireCompletedToday()) {
            // set the post questionnaire once we launch. if the survey is already completed for today.
            ((TextView) findViewById(R.id.textView)).setText(R.string.post_questionnaire);
            // generate the right to delete/download data at the bottom of the activity
            ((TextView) findViewById(R.id.textViewForUserData)).setText(generatePostQuestionnaireUserBits());
            // set its visibility
            findViewById(R.id.textViewForUserData).setVisibility(View.VISIBLE);
            // hide the button by setting its visibility.
            findViewById(R.id.startButton).setVisibility(View.INVISIBLE);
            // invalidate the elements
            findViewById(R.id.textView).postInvalidate();
            findViewById(R.id.startButton).postInvalidate();
            findViewById(R.id.textViewForUserData).postInvalidate();
        } else {
            // only change its visibility if the button is hidden
            if (findViewById(R.id.startButton).getVisibility() == View.INVISIBLE) {
                // set its visibility
                findViewById(R.id.textViewForUserData).setVisibility(View.VISIBLE);
                // hide it, for now
                findViewById(R.id.textViewForUserData).postInvalidate();
            }
        }
    }

    private boolean questionnaireCompletedToday() {
        long time = UserOnboardingPrefs.getInstance(this).getQuestionnaireLastCompleted();
        return DateUtils.isSameDay(new Date(), new Date(time));
        //return false; //in case u want to test same day
    }

    private void showQuestionnaire(String questionnaire) {
        Intent intent = new Intent(this, QuestionnaireActivity.class);
        intent.putExtra(QuestionnaireActivity.QUESTIONNAIRE_KEY, questionnaire);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //show user the surveys
        startActivity(intent);
    }
}
