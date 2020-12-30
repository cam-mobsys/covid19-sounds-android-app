package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire;

import android.os.Parcel;

public class QuestionRadioButtons extends Question<String> {

    public static final Creator<QuestionRadioButtons> CREATOR = new Creator<QuestionRadioButtons>() {
        @Override
        public QuestionRadioButtons createFromParcel(Parcel source) {
            return new QuestionRadioButtons(source);
        }

        @Override
        public QuestionRadioButtons[] newArray(int size) {
            return new QuestionRadioButtons[size];
        }
    };
    private String[] options;

    public QuestionRadioButtons(String id, String type, String text, String[] options) {
        super(id, type, text);
        this.options = options;
    }

    protected QuestionRadioButtons(Parcel in) {
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
