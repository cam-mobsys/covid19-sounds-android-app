package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.questionGroups;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionAudioRecord;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionCheckbox;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionInfo;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionRadioButtons;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionSlider;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionSpinner;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionText;


//prepare the questions
public class QuestionGroupLoader {
    private String filename;
    private Context context;

    public QuestionGroupLoader(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    public String readFileAsString(String filename) throws IOException {
        InputStream is = context.getAssets().open(filename);

        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }

        return total.toString();
    }

    private ArrayList<Question> readQuestions(JSONObject object) throws JSONException {
        ArrayList<Question> questions = new ArrayList<>();

        JSONArray arr = object.getJSONArray("questions");

        for (int i = 0; i < arr.length(); i++) {
            JSONObject questionJSON = arr.getJSONObject(i);
            questions.add(readQuestion(questionJSON));
        }
        return questions;
    }

    private Question readQuestion(JSONObject questionJSON) throws JSONException {

        Question result = null;

        if (!questionJSON.has("id") || !questionJSON.has("type") || !questionJSON.has("text")) {
            throw new JSONException("Question error");
        }

        String id = questionJSON.getString("id");
        String type = questionJSON.getString("type");
        String text = questionJSON.getString("text");

        switch (type) {
            case Question.TYPE.SPINNER: {
                JSONArray optionsJSON = questionJSON.getJSONArray("options");
                ArrayList<String> options = new ArrayList<>();

                for (int j = 0; j < optionsJSON.length(); j++) {
                    options.add(optionsJSON.getString(j));
                }

                result = new QuestionSpinner(id, type, text, options);
                break;
            }
            case Question.TYPE.INFO:
                result = new QuestionInfo(id, type, text);
                break;
            case Question.TYPE.TEXT:
                result = new QuestionText(id, type, text);
                break;
            case Question.TYPE.SLIDER: {
                JSONArray optionsJSON = questionJSON.getJSONArray("options");
                String[] options = new String[optionsJSON.length()];
                for (int j = 0; j < optionsJSON.length(); j++) {
                    options[j] = optionsJSON.getString(j);
                }
                result = new QuestionSlider(id, type, text, options);
                break;
            }
            case Question.TYPE.CHECKBOX: {
                JSONArray optionsJSON = questionJSON.getJSONArray("options");
                String[] options = new String[optionsJSON.length()];
                for (int j = 0; j < optionsJSON.length(); j++) {
                    options[j] = optionsJSON.getString(j);
                }
                result = new QuestionCheckbox(id, type, text, options);
                break;
            }
            case Question.TYPE.RADIO_BUTTONS: {
                JSONArray optionsJSON = questionJSON.getJSONArray("options");
                String[] options = new String[optionsJSON.length()];
                for (int j = 0; j < optionsJSON.length(); j++) {
                    options[j] = optionsJSON.getString(j);
                }
                result = new QuestionRadioButtons(id, type, text, options);
                break;
            }
            case Question.TYPE.AUDIO_RECORDER: {
                JSONArray optionsJSON = questionJSON.getJSONArray("options");
                String[] options = new String[optionsJSON.length()];
                for (int j = 0; j < optionsJSON.length(); j++) {
                    options[j] = optionsJSON.getString(j);
                }
                result = new QuestionAudioRecord(id, type, text, options);
                break;
            }
        }

        // Check if the question has a dependency and if so, add it to the
        // Question object.
        if (questionJSON.has("dependency")) {
            JSONObject dependency = questionJSON.getJSONObject("dependency");
            String dependencyID = dependency.getString("id");
            String dependencyAnswer = dependency.getString("answer");

            assert result != null;
            result.setDependency(dependencyID, dependencyAnswer);
        }

        return result;
    }

    public ArrayList<Question> parse() throws QuestionGroupLoadException {

        try {
            String JSONstring = readFileAsString(filename);
            JSONObject ob = new JSONObject(JSONstring);

            return readQuestions(ob);
        } catch (IOException | JSONException e) {
            throw new QuestionGroupLoadException();
        }
    }
}