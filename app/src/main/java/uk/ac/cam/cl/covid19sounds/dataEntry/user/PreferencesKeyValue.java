package uk.ac.cam.cl.covid19sounds.dataEntry.user;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferencesKeyValue {
    private final SharedPreferences sharedPreferences;

    public PreferencesKeyValue(String filename, Context context) {
        sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
    }

    public boolean keyExists(String key) {
        return !sharedPreferences.contains(key);
    }

    public String getStringValue(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void setStringValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean getBooleanValue(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void setBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public long getLongValue(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public void setLongValue(String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public int getIntValue(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public void setIntValue(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

}
