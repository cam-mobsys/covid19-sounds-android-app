package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire;

import android.os.Parcel;

public class QuestionSlider extends Question<String> {

    public static final Creator<QuestionSlider> CREATOR = new Creator<QuestionSlider>() {
        @Override
        public QuestionSlider createFromParcel(Parcel source) {
            return new QuestionSlider(source);
        }

        @Override
        public QuestionSlider[] newArray(int size) {
            return new QuestionSlider[size];
        }
    };
    private String[] options;

    public QuestionSlider(String id, String type, String text, String[] options) {
        super(id, type, text);

        this.options = options;
    }

    protected QuestionSlider(Parcel in) {
        this.options = in.createStringArray();
        this.id = in.readString();
        this.type = in.readString();
        this.text = in.readString();
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

    public String[] getOptions() {
        return options;
    }
}
