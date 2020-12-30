package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire;

import android.os.Parcel;

import java.util.ArrayList;

public class QuestionSpinner extends Question<String> {

    public static final Creator<QuestionSpinner> CREATOR = new Creator<QuestionSpinner>() {
        @Override
        public QuestionSpinner createFromParcel(Parcel source) {
            return new QuestionSpinner(source);
        }

        @Override
        public QuestionSpinner[] newArray(int size) {
            return new QuestionSpinner[size];
        }
    };
    private String[] options;

    public QuestionSpinner(String id, String type, String text, ArrayList<String> options) {
        super(id, type, text);
        // this.options = options.toArray(new String[options.size()]);

        // this should be faster.
        this.options = options.toArray(new String[0]);
    }

    private QuestionSpinner(Parcel in) {
        this.options = in.createStringArray();
        this.id = in.readString();
        this.type = in.readString();
        this.text = in.readString();
    }

    public String[] getOptions() {
        return options;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(this.options);
        dest.writeString(this.id);
        dest.writeString(this.type);
        dest.writeString(this.text);
    }
}
