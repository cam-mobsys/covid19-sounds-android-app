package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups;

import android.accounts.Account;
import android.content.Context;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.ac.cam.cl.covid19sounds.MainApplication;
import uk.ac.cam.cl.covid19sounds.dataEntry.DataConstants;
import uk.ac.cam.cl.covid19sounds.dataEntry.DataFileWriter;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;
import uk.ac.cam.cl.covid19sounds.localisation.Localisation;


public class OnboardingQuestionGroup extends QuestionGroup {
    private static final String TITLE = "Welcome!";
    private Context context;
    private Account account = null;

    public OnboardingQuestionGroup(Context context) throws QuestionGroupLoadException {
        super(TITLE);
        this.context = context;
        loadQuestions(context);
    }

    private void loadQuestions(Context context) throws QuestionGroupLoadException {
        QuestionGroupLoader loader;
        //depending on the local language settings on the phone display initial survey questions in the local language
        switch (context.getResources().getConfiguration().locale.getDisplayLanguage()) {
            case (Localisation.LANGUAGES_IT):
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_IT);
                questions = loader.parse();
                break;
            case (Localisation.LANGUAGES_DE):
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_DE);
                questions = loader.parse();
                break;
            case (Localisation.LANGUAGES_ES):
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_ES);
                questions = loader.parse();
                break;
            case (Localisation.LANGUAGES_FR):
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_FR);
                questions = loader.parse();
                break;
            case (Localisation.LANGUAGES_HI):
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_HI);
                questions = loader.parse();
                break;
            case (Localisation.LANGUAGES_PT):
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_PT);
                questions = loader.parse();
                break;
            case Localisation.LANGUAGES_RU:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_RU);
                questions = loader.parse();
                break;

            case Localisation.LANGUAGES_EL:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_EL);
                questions = loader.parse();
                break;

            case Localisation.LANGUAGES_RO:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_RO);
                questions = loader.parse();
                break;
            case (Localisation.LANGUAGES_ZH_TRAD):
            case (Localisation.LANGUAGES_ZH_SIMP):
            case (Localisation.LANGUAGES_ZH):
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_ZH);
                questions = loader.parse();
                break;
            default:
                loader = new QuestionGroupLoader(context, QUESTIONNAIRES.ONBOARDING_US);
                questions = loader.parse();
        }
    }


    @Override
    public void handleResult() {
        super.handleResult();
        UserOnboardingPrefs onboarding = new UserOnboardingPrefs(context);
        //depending on the question  put the responses in a file. some of the quesitons here are not asked but can be used
        // in the future such as alcohol.
        for (Question q : questions) {
            switch (q.getID()) {
                case "User_sex":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.USER_SEX, (String) q.getAnswer());
                    break;
                case "User_age":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.USER_AGE, (String) q.getAnswer());
                    break;
                case "User_height":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.USER_HEIGHT, (String) q.getAnswer());
                    break;
                case "User_weight":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.USER_WEIGHT, (String) q.getAnswer());
                    break;
                case "Medical_history":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.USER_PMH, (String) q.getAnswer());
                    break;
                case "Allergies":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.ALLERGIES, (String) q.getAnswer());
                    break;
                case "Medications":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.MEDICATION, (String) q.getAnswer());
                    break;
                case "PhysicalActivity":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.ACTIVITY, (String) q.getAnswer());
                    break;
                case "Diet":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.DIET, (String) q.getAnswer());
                    break;
                case "Smoking":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.SMOKING, (String) q.getAnswer());
                    break;
                case "Alcohol":
                    onboarding.setStringValue(UserOnboardingPrefs.Values.ALCOHOL, (String) q.getAnswer());
                    break;
            }
        }
        try {
            //record the user responses in a json format.
            recordSignUpSurveyLog(questions, onboarding, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //initial survey is complete. so register this user. assign them an id.
        registerUser();

        onboarding.setCompletedOnboarding(true);
    }

    public void registerUser() {
        final String[] password = {null};
        final String[] userID = {null};
        //final UserOnboardingPrefs onboardingPrefs = UserOnboardingPrefs.getInstance(context);
        Thread t = new Thread() {
            public void run() {
                userID[0] = UserOnboardingPrefs.getInstance(context).getUserID();
                password[0] = MainApplication.getPassword(context, userID[0]);
                if (password[0] != null) {
                    account = MainApplication.createSyncAccount(context, userID[0], password[0]);
                } else {
                    Looper.prepare();
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void recordSignUpSurveyLog(ArrayList<Question> questions, UserOnboardingPrefs uop, Context context) throws JSONException {
        JSONObject json = new JSONObject();
        //log the user survey in the form of a json format

        json.put(DataConstants.PARTICIPANT_ID, uop.getUserID());
        json.put(DataConstants.FINISH_DATETIME, System.currentTimeMillis());

        for (Question q : questions) {

            json.put(q.getID(), q.getAnswer());
        }
        //put locale in json to make it easier to detect the origin
        json.put("User_Locale", context.getResources().getConfiguration().locale.getLanguage());
        //write json to a log file which will be sent to a server.

        DataFileWriter.log(json.toString().concat("\n"), DataFileWriter.FileNames.SIGN_UP_SURVEYS, context);
    }


}