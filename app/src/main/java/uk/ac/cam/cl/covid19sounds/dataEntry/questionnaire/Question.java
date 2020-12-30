package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire;

import android.os.Parcelable;

// The abstract type is used to hold the return answer type.
abstract public class Question<T> implements Parcelable {


    // Fields associated with the question
    protected String id;
    protected String type;
    protected String text;
    protected boolean hasDependency;
    protected String dependencyID;
    protected String dependencyAnswer;
    protected T answer;
    public Question() {
    }

    public Question(String id, String type, String text) {
        this.id = id;
        this.type = type;
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public String getID() {
        return id;
    }

    public String getText() {
        return text;
    }

    /**
     * Sets a dependency on displaying this question, depending on whether
     * another question has a particular answer.
     */
    public void setDependency(String id, String answer) {
        this.dependencyID = id;
        this.dependencyAnswer = answer;
        this.hasDependency = true;
    }

    public boolean hasDependency() {
        return hasDependency;
    }

    public String getDependencyID() {
        return dependencyID;
    }

    public String getDependencyAnswer() {
        return dependencyAnswer;
    }

    public T getAnswer() {
        return answer;
    }

    public void setAnswer(T answer) {
        this.answer = answer;
    }

    // Defines the types of the questions, used for displaying different formats.
    public static class TYPE {
        public static final String INFO = "Info";
        public static final String TEXT = "Text";
        public static final String SPINNER = "Spinner";
        public static final String SLIDER = "Slider";
        public static final String AUDIO_RECORDER = "AudioRecord";
        public static final String CHECKBOX = "Checkbox";
        public static final String RADIO_BUTTONS = "RadioButtons";
    }
}
