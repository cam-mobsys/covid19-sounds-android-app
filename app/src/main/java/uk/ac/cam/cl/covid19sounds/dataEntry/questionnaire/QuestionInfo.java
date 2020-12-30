package uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire;

import android.os.Parcel;

public class QuestionInfo extends Question<Integer> {

    public static final Creator<QuestionInfo> CREATOR = new Creator<QuestionInfo>() {
        @Override
        public QuestionInfo createFromParcel(Parcel source) {
            return new QuestionInfo(source);
        }

        @Override
        public QuestionInfo[] newArray(int size) {
            return new QuestionInfo[size];
        }
    };

    public QuestionInfo(String id, String type, String text) {
        super(id, type, text);

    }

    protected QuestionInfo(Parcel in) {
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
