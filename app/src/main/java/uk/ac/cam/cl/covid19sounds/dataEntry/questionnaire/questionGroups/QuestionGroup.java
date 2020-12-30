package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups;

import java.util.ArrayList;

import uk.ac.cam.cl.covid19sounds.activities.QuestionnaireActivity;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;

public class QuestionGroup {

    protected ArrayList<Question> questions = new ArrayList<>();
    private String title;

    public QuestionGroup(String title) {
        this.title = title;
    }

    public void setTitle(String s) {
        title = s;
    }


    /**
     * Checks whether the answer with the given ID, also has the given answer.
     */
    public boolean checkAnswer(String id, String answer) {

        for (Question q : questions) {
            // this is equivalent

            if (q.getID().equals(id) && ((String) q.getAnswer()).contains(answer)) {
                return true;
            }

        }

        return false;
    }

    public void handleResult() {
    }

    public void setResult(QuestionnaireActivity questionnaireActivity) {
    }

    public int size() {
        return questions.size();
    }

    public Question get(int index) {
        return questions.get(index);
    }

    public static class QUESTIONNAIRES {

        public static final String ONBOARDING_US = "questionnaires/signup_survey.json";
        public static final String DAILY_SURVEY_US = "questionnaires/daily_survey.json";

        public static final String ONBOARDING_IT = "questionnaires/signup_survey_it.json";
        public static final String DAILY_SURVEY_IT = "questionnaires/daily_survey_it.json";

        public static final String ONBOARDING_DE = "questionnaires/signup_survey_de.json";
        public static final String DAILY_SURVEY_DE = "questionnaires/daily_survey_de.json";

        public static final String ONBOARDING_ES = "questionnaires/signup_survey_es.json";
        public static final String DAILY_SURVEY_ES = "questionnaires/daily_survey_es.json";

        public static final String ONBOARDING_FR = "questionnaires/signup_survey_fr.json";
        public static final String DAILY_SURVEY_FR = "questionnaires/daily_survey_fr.json";

        public static final String ONBOARDING_HI = "questionnaires/signup_survey_hi.json";
        public static final String DAILY_SURVEY_HI = "questionnaires/daily_survey_hi.json";

        public static final String ONBOARDING_PT = "questionnaires/signup_survey_pt.json";
        public static final String DAILY_SURVEY_PT = "questionnaires/daily_survey_pt.json";

        public static final String ONBOARDING_RU = "questionnaires/signup_survey_ru.json";
        public static final String DAILY_SURVEY_RU = "questionnaires/daily_survey_ru.json";

        public static final String ONBOARDING_ZH = "questionnaires/signup_survey_zh.json";
        public static final String DAILY_SURVEY_ZH = "questionnaires/daily_survey_zh.json";

        public static final String ONBOARDING_EL = "questionnaires/signup_survey_el.json";
        public static final String DAILY_SURVEY_EL = "questionnaires/daily_survey_el.json";

        public static final String ONBOARDING_RO = "questionnaires/signup_survey_ro.json";
        public static final String DAILY_SURVEY_RO = "questionnaires/daily_survey_ro.json";
    }

    public static class QUESTION_TYPES {
        public static final String DAILY_SURVEY = "daily_survey";
    }

}
