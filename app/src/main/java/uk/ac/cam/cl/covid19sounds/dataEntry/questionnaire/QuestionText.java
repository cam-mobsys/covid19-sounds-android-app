package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire;

import android.os.Parcel;

public class QuestionText extends Question<String> {

    public static final Creator<QuestionText> CREATOR = new Creator<QuestionText>() {
        @Override
        public QuestionText createFromParcel(Parcel source) {
            return new QuestionText(source);
        }

        @Override
        public QuestionText[] newArray(int size) {
            return new QuestionText[size];
        }
    };

    public QuestionText(String id, String type, String text) {
        super(id, type, text);
    }

    private QuestionText(Parcel in) {
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
        dest.writeString(this.id);
        dest.writeString(this.type);
        dest.writeString(this.text);
    }
}
