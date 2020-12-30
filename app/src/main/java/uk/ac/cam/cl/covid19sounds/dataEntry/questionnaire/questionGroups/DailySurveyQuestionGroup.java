package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import uk.ac.cam.cl.covid19sounds.activities.QuestionnaireActivity;
import uk.ac.cam.cl.covid19sounds.dataEntry.DataConstants;
import uk.ac.cam.cl.covid19sounds.dataEntry.DataFileWriter;
import uk.ac.cam.cl.covid19sounds.dataEntry.WavFileHelper;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;
import uk.ac.cam.cl.covid19sounds.localisation.Localisation;

import static uk.ac.cam.cl.covid19sounds.utils.sharedConfig.COVID_LOG_TAG;

public class DailySurveyQuestionGroup extends QuestionGroup {
    private static final String title = "Daily Questionnaire";
    private static final String VOICE = "voice";
    private static final String COUGH = "cough";
    private static final String BREATH = "breaths";
    private static final String HOLD = "hold";

    private Context context;

    public DailySurveyQuestionGroup(Context context) throws
            QuestionGroupLoadException {
        super(title);
        super.setTitle(title);
        this.context = context;
        //load daily survey questions on screen
        loadQuestions(context);
    }

    public void loadQuestions(Context context) throws QuestionGroupLoadException {
        QuestionGroupLoader loader;
        switch(context.getResources().getConfiguration().locale.getDisplayLanguage()) {
            //depending on the local language settings on the phone display daily survey questions in the local language
            case Localisation.LANGUAGES_IT:
                 loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_IT);
                questions = loader.parse();
                break;
            case Localisation.LANGUAGES_DE:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_DE);
                questions = loader.parse();
                break;
            case Localisation.LANGUAGES_ES:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_ES);
                questions = loader.parse();
                break;
            case Localisation.LANGUAGES_FR:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_FR);
                questions = loader.parse();
                break;
            case Localisation.LANGUAGES_HI:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_HI);
                questions = loader.parse();
                break;
            case Localisation.LANGUAGES_PT:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_PT);
                questions = loader.parse();
                break;
            case Localisation.LANGUAGES_RU:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_RU);
                questions = loader.parse();
                break;

            case Localisation.LANGUAGES_EL:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_EL);
                questions = loader.parse();
                break;

            case Localisation.LANGUAGES_RO:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_RO);
                questions = loader.parse();
                break;
                
            case Localisation.LANGUAGES_ZH:
            case Localisation.LANGUAGES_ZH_SIMP:
            case Localisation.LANGUAGES_ZH_TRAD:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_ZH);
                questions = loader.parse();
                break;
            default:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.DAILY_SURVEY_US);
                questions = loader.parse();
        }
    }

    @Override
    public void handleResult() {
        //handle answers coming from the users here after completion of the question
        super.handleResult();
        UserOnboardingPrefs.getInstance(context)
                .setQuestionnaireLastCompleted(System.currentTimeMillis());
        String userID = UserOnboardingPrefs.getInstance(context).getUserID();
        ArrayList<Question> questions = super.questions;
        for (Question<String> q : questions) {

            // we need this switch statement as we are  changing to wav file
            switch (q.getID()) {
                //depending on the type of audio recording convert the raw audio file into .wav format
                case "hold":
                    String wavfilename = WavFileHelper.copyWaveFile(q.getAnswer(), getWavFileName(HOLD, userID));
                    (new File(q.getAnswer())).delete();
                    q.setAnswer(wavfilename);

                    break;
                case "voice":
                    System.out.println("voice");
                    wavfilename = WavFileHelper.copyWaveFile(q.getAnswer(), getWavFileName(VOICE, userID));
                    (new File(q.getAnswer())).delete();
                    q.setAnswer(wavfilename);

                    break;
                case "breathe":
                    System.out.println("breathe");

                    wavfilename = WavFileHelper.copyWaveFile(q.getAnswer(), getWavFileName(BREATH, userID));
                    (new File(q.getAnswer())).delete();
                    q.setAnswer(wavfilename);

                    break;
                case "cough":
                    if (q.getAnswer() != null) {
                        System.out.println("cough");

                        wavfilename = WavFileHelper.copyWaveFile(q.getAnswer(), getWavFileName(COUGH, userID));
                        (new File(q.getAnswer())).delete();
                        q.setAnswer(wavfilename);

                    }
                    break;
            }
        }

        try {
            //log the user answers in a json file to be sent to the server
            logSurvey(context);
        } catch (JSONException e) {
            Log.e(COVID_LOG_TAG, "Could not log survey to file due to JSON error.");
            e.printStackTrace();
        }
    }

    private String getWavFileName(String type, String userID) {
        //name for the .wav file to which raw recording is to be converted
        return context.getFilesDir() + "/" + type + "_" + userID + "_" + System.currentTimeMillis() + ".wav";

    }

    private void logSurvey(Context context) throws JSONException {
        //log the user survey in the form of a json format
        JSONObject json = new JSONObject();
        json.put(DataConstants.PARTICIPANT_ID, UserOnboardingPrefs.getInstance(context).getUserID());

        json.put(DataConstants.FINISH_DATETIME, System.currentTimeMillis());

        for (Question q : questions) {
            json.put(q.getID(), q.getAnswer());
            //System.out.println(q.getID());

            //System.out.println(q.getAnswer());
        }
        String versionName = uk.ac.cam.cl.covid19sounds.BuildConfig.VERSION_NAME;
        json.put(DataConstants.APP_VERSION, versionName);
        //write json to a log file
        DataFileWriter.log(json.toString().concat("\n"), DataFileWriter.FileNames.DAILY_SURVEYS, context);
    }

    @Override
    public void setResult(QuestionnaireActivity questionnaireActivity) {
        super.setResult(questionnaireActivity);
    }
}

