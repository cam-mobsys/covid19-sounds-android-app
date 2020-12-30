package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire;
import android.os.Parcel;

public class QuestionAudioRecord extends Question<String> {

    public static final Creator<QuestionCheckbox> CREATOR = new Creator<QuestionCheckbox>() {
        @Override
        public QuestionCheckbox createFromParcel(Parcel source) {
            return new QuestionCheckbox(source);
        }

        @Override
        public QuestionCheckbox[] newArray(int size) {
            return new QuestionCheckbox[size];
        }
    };
    private String[] options;

    public QuestionAudioRecord(String id, String type, String text, String[] options) {
        super(id, type, text);
        this.options = options;
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
