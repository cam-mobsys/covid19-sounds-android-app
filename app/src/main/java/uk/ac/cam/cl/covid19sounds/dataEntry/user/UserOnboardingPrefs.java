package uk.ac.cam.cl.covid19sounds.dataEntry.user;

import android.content.Context;

import org.apache.commons.lang3.RandomStringUtils;

public class UserOnboardingPrefs extends PreferencesKeyValue {

    private static final String COMPLETED_ONBOARDING = "completed_onboarding";
    private static String filename = "user_pref";
    private static UserOnboardingPrefs instance;

    public UserOnboardingPrefs(Context context) {
        super(filename, context);
    }

    public static UserOnboardingPrefs getInstance(Context context) {
        if (instance == null) {
            instance = new UserOnboardingPrefs(context);
        }
        return instance;
    }
    public char[] getPassword() {
        return super.getStringValue(Values.PASSWD).toCharArray();
    }

    public void setPassword(String password) {
        super.setStringValue(Values.PASSWD, password);
    }

    public boolean getCompletedOnboarding() {
        if (super.keyExists(COMPLETED_ONBOARDING)) {
            return false;
        } return super.getBooleanValue(COMPLETED_ONBOARDING);
    }

    public void setCompletedOnboarding(boolean b) {
        super.setBooleanValue(COMPLETED_ONBOARDING, b);
    }

    public String getUserID() {
        if (super.keyExists(Values.USER_UUID)) {
            super.setStringValue(Values.USER_UUID, generateUserName());
        }
        return super.getStringValue(Values.USER_UUID);
    }


    public void setRefreshToken(String refresh_token) {
        super.setStringValue(Values.REFRESH_TOKEN, refresh_token);
    }

    public String getAccessToken() {
        return super.getStringValue(Values.ACCESS_TOKEN);
    }

    public void setAccessToken(String access_token) {
        super.setStringValue(Values.ACCESS_TOKEN, access_token);
    }

    private String generateUserName() {
        return RandomStringUtils.random(10, true, true);
    }

    public int getMorningNotificationHour() {
        if (super.keyExists(Values.MORNING_NOTIFICATION_HOUR)) {
            super.setIntValue(Values.MORNING_NOTIFICATION_HOUR, 8);
        }
        return super.getIntValue(Values.MORNING_NOTIFICATION_HOUR);
    }

    public int getMorningNotificationMinute() {
        if (super.keyExists(Values.MORNING_NOTIFICATION_MINUTE)) {
            super.setIntValue(Values.MORNING_NOTIFICATION_MINUTE, 0);
        }
        return super.getIntValue(Values.MORNING_NOTIFICATION_MINUTE);
    }

    public long getQuestionnaireLastCompleted() {
        if (super.keyExists(Values.LAST_QUESTIONNAIRE)) {
            super.setLongValue(Values.LAST_QUESTIONNAIRE, 0);
        }
        return super.getLongValue(Values.LAST_QUESTIONNAIRE);
    }

    public void setQuestionnaireLastCompleted(long l) {
        super.setLongValue(Values.LAST_QUESTIONNAIRE, l);
    }


    public static class Values {
        public static final String USER_UUID = "user_id";
        public static final String PASSWD = "password";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String MORNING_NOTIFICATION_HOUR = "morning_notification_hour";
        public static final String MORNING_NOTIFICATION_MINUTE = "morning_notification_minute";
        public static final String USER_AGE = "User_age";
        public static final String USER_SEX = "sex";
        public static final String USER_HEIGHT = "height";
        public static final String USER_WEIGHT = "weight";
        public static final String USER_PMH = "medical_history";
        public static final String ALLERGIES = "allergies";
        public static final String MEDICATION = "medication";
        public static final String ACTIVITY = "physical_activity";
        public static final String DIET = "5_a_day";
        public static final String SMOKING = "smoking";
        public static final String ALCOHOL = "alcohol";
        public static final String LAST_QUESTIONNAIRE = "last_questionnaire";

    }
}
