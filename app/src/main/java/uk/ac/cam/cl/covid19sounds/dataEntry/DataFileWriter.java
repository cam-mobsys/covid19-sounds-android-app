package uk.ac.cam.cl.covid19sounds.dataEntry;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;

import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;

public class DataFileWriter {
    //write data from a json to log file
    public static void log(String s, String filename, Context context) {
        FileOutputStream fos = null;
        String user = UserOnboardingPrefs.getInstance(context).getUserID();
        String file = user + "_" + System.currentTimeMillis()+"_" + filename + ".json";
        try {
            fos = context.openFileOutput(file, Context.MODE_APPEND);
            fos.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class FileNames {
        public static final String SIGN_UP_SURVEYS = "signUpSurveys";
        public static final String DAILY_SURVEYS = "dailySurveys";
        public static final String LOCATION = "locations";
    }

}
