package uk.ac.cam.cl.covid19sounds;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;
import uk.ac.cam.cl.covid19sounds.notifications.MorningMessager;
import uk.ac.cam.cl.covid19sounds.transfer.ApplicationConstants;

import static uk.ac.cam.cl.covid19sounds.utils.sharedConfig.COVID_LOG_TAG;

public class MainApplication extends Application {

    // The authority for the sync adapter's (dummy) content provider
    public static final String AUTHORITY = "provider name here";
    public static final String ACCOUNT_TYPE = "app name here";
    public static final String AUTHORIZATION_URL = "authorization url here";
    public static final String GRANT_TYPE = "type of grant";

    /* CONSTANTS FOR THE AUTHORIZATION PROCESS */
    public static final String APP_PASSWORD = "app password";
    public static final String APP_EMAIL = "app email id";
    private static final String PASSWD_KEY = "password key";


    public static Account getUserAccount(Context context) {
        // Get the user account (required for sync, doesn't do anything else)
        Account[] accounts = AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0)
            return accounts[0]; // Should only be one account.
        else return null;
    }

    /**
     * Creates a sync account.
     *
     * @param context  the current context.
     * @param userId   the user id.
     * @param password sync account password.
     * @return returns a valid Account instance or null.
     */
    public static Account createSyncAccount(Context context, String userId, String password) {
        Account newAccount = new Account(userId, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        if (accountManager == null) {
            Log.e(COVID_LOG_TAG, "Account manager is null");
            return null;
        } else {
            // Create user in backend and get initial access token
            boolean success = accountManager.addAccountExplicitly(newAccount, password, null);
            if (success) {
                Log.d(COVID_LOG_TAG, "Sync account added successfully");
                return newAccount;
            } else {
                Log.e(COVID_LOG_TAG, "Failed to add sync account");
                return null;
            }
        }
    }

    public static String getPassword(Context context, String userID) {
        // username, email, password
        try {
            HttpsURLConnection urlConnection;
            URL url;
            DataOutputStream dos;

            // open a URL connection to the Servlet
            url = new URL(AUTHORIZATION_URL);
            // Open a HTTP  connection to  the URL
            urlConnection = (HttpsURLConnection) url.openConnection();

            try {
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false); // don't use a cache copy
                urlConnection.setDoInput(true); // Allow Inputs
                urlConnection.setDoOutput(true); // Allow Outputs
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Build body to write (form)
                JSONObject obj = new JSONObject();
                obj.put("username", userID);
                obj.put("password", APP_PASSWORD);
                obj.put("email", APP_EMAIL);
                String objStr = obj.toString();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    urlConnection.setRequestProperty("Content-Length", objStr.getBytes(StandardCharsets.UTF_8).length + "");
                }
                else urlConnection.setRequestProperty("Content-Length", objStr.getBytes().length + "");

                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(objStr);

                // Responses from the server (message)
                if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    Log.d(COVID_LOG_TAG, "User registered in the backend correctly.");
                    // Parse received json
                    BufferedReader br = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;

                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    JSONObject serverResponseMessage = new JSONObject(sb.toString());
                    String password = serverResponseMessage.getString(PASSWD_KEY);
                    UserOnboardingPrefs uop = UserOnboardingPrefs.getInstance(context);
                    uop.setPassword(password);
                    return password;
                } else {
                    int responseCode = urlConnection.getResponseCode();
                    Log.d(COVID_LOG_TAG, "response code received in createAppUserTask: " + responseCode);
                }
                // close the streams
                dos.flush();
                dos.close();

            } catch (Exception e) {
                Log.e(COVID_LOG_TAG, "multipart post error " + e);
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            //FileLogger.log("IOException " + e, context);
        }
        return null;
    }

    private static void setUpSync(final Context context) {
        Thread t = new Thread() {
            public void run() {
                // User account should already exist, as already registered
                Account account = getUserAccount(context);
                if (account == null) {
                    String userID = UserOnboardingPrefs.getInstance(context).getUserID();
                    String password = getPassword(context, userID);
                    if (password != null) {
                        account = createSyncAccount(context, userID, password);
                    } else {
                        return;
                    }
                }
                // Inform the system that this account supports sync
                ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                // Inform the system that this account is eligible for auto sync
                ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                // Recommend a schedule for auto sync
                Bundle bundle = new Bundle();
                ContentResolver.addPeriodicSync(
                        account,
                        AUTHORITY,
                        bundle,
                        3600);
                requestSync(context);
            }
        };
        t.start();
        try {
            t.join();
            new Thread() {
                public void run() {
                    if (refreshCredentials(context, UserOnboardingPrefs.getInstance(context).getUserID())) {
                        Log.d(COVID_LOG_TAG, "Refreshed credentials successfully.");
                    } else {
                        Log.e(COVID_LOG_TAG, "Error refreshing credentials or no files to upload");
                    }
                }
            }.start();
        } catch (InterruptedException e) {
            Log.e(COVID_LOG_TAG, "Error getting account.");
        }
    }

    public static void requestSync(Context context) {
        // request sync
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        Account account = MainApplication.getUserAccount(context);
        if (account == null) {
            System.out.println("requestsync acct is null");
            String userID = UserOnboardingPrefs.getInstance(context).getUserID();

            String password = getPassword(context, userID);
            if (password != null) {

                account = createSyncAccount(context, userID, password);
            } else {
                return;
            }
        }

        ContentResolver.requestSync(account, MainApplication.AUTHORITY, bundle);
    }

    public static boolean refreshCredentials(Context context, String userId) {
        HttpsURLConnection urlConnection;
        URL url;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        //first thing is to check if there is data to send otherwise no need to refresh credentials
        if(!areThereFiles(context))
        {
            return false;


        }
        try {

            // open a URL connection to the Servlet
            url = new URL(ApplicationConstants.ACCESS_TOKEN_URL);
            // Open a HTTP  connection to  the URL
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false); // don't use a cache copy
            urlConnection.setDoInput(true); // Allow Inputs
            urlConnection.setDoOutput(true); // Allow Outputs
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary + "; charset=utf-8"); //, "application/json");

            // Build body to write (form)
            StringBuilder sb = new StringBuilder();

            // do it without leaks and warnings
            sb.append(twoHyphens).append(boundary).append(lineEnd);
            sb.append("Content-Disposition: form-data; name=\"username\"").append(lineEnd);
            sb.append(lineEnd);
            sb.append(userId);
            sb.append(lineEnd);
            sb.append(twoHyphens).append(boundary).append(lineEnd);
            sb.append("Content-Disposition: form-data; name=\"password\"").append(lineEnd);
            sb.append(lineEnd);
            sb.append(UserOnboardingPrefs.getInstance(context).getPassword());
            sb.append(lineEnd);
            sb.append(twoHyphens).append(boundary).append(lineEnd);
            sb.append("Content-Disposition: form-data; name=\"client_id\"").append(lineEnd);
            sb.append(lineEnd);
            sb.append(ApplicationConstants.CLIENT_ID);
            sb.append(lineEnd);
            sb.append(twoHyphens).append(boundary).append(lineEnd);
            sb.append("Content-Disposition: form-data; name=\"client_secret\"").append(lineEnd);
            sb.append(lineEnd);
            sb.append(ApplicationConstants.CLIENT_SECRET);
            sb.append(lineEnd);
            sb.append(twoHyphens).append(boundary).append(lineEnd);
            sb.append("Content-Disposition: form-data; name=\"grant_type\"").append(lineEnd);
            sb.append(lineEnd);
            sb.append(GRANT_TYPE);
            sb.append(lineEnd);
            sb.append(twoHyphens).append(boundary).append(lineEnd);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                urlConnection.setRequestProperty("Content-Length", sb.toString().getBytes(StandardCharsets.UTF_8).length + "");
            } else {
                urlConnection.setRequestProperty("Content-Length", sb.toString().getBytes().length + "");
            }

            dos = new DataOutputStream(urlConnection.getOutputStream());
            dos.writeBytes(sb.toString());

            // Responses from the server (message)
            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                // Parse received json
                BufferedReader br = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));
                sb = new StringBuilder();
                String output;

                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                JSONObject serverResponseMessage = new JSONObject(sb.toString());
                String access_token = serverResponseMessage.getString(ApplicationConstants.ACCESS_TOKEN);
                String refresh_token = serverResponseMessage.getString(ApplicationConstants.REFRESH_TOKEN);
                // Replace access token and refresh token
                UserOnboardingPrefs uop = UserOnboardingPrefs.getInstance(context);
                uop.setAccessToken(access_token);
                uop.setRefreshToken(refresh_token);
                Log.d(COVID_LOG_TAG, "Oauth token received from Backend correctly in Main");
                Log.d(COVID_LOG_TAG, "Access_token: " + access_token);
                Log.d(COVID_LOG_TAG, "Refresh_token: " + refresh_token);
                // close the streams
                dos.flush();
                dos.close();
                return true;

            } else {
                Log.d(COVID_LOG_TAG, "response code received in reqAuthToken: " + urlConnection.getResponseCode());

                dos.flush();
                dos.close();
                return false;
            }


        } catch (Exception e) {
            return false;
        }
    }



    private static boolean areThereFiles(Context context) {

        // first, get the file directory
        File directory = context.getFilesDir();

        // get the names of the files
        File[] files = directory.listFiles();

        // check if files instance is null
        if (files != null) {

            if (files.length == 0) {

                Log.e(COVID_LOG_TAG, "No files to upload");
                return false;
            }
        }
        return true;

    }
    @Override
    public void onCreate() {
         // check the stage of the user
        UserOnboardingPrefs prefs = UserOnboardingPrefs.getInstance(this);
        super.onCreate();
        if (prefs.getCompletedOnboarding()) { // onboarding completed
            setUpMorningMessage(prefs);

            setUpSync(getApplicationContext());
        }
    }

    private void setUpMorningMessage(UserOnboardingPrefs prefs) {
        MorningMessager morningMessager = new MorningMessager(getApplicationContext());//MorningMessager.getInstance(this);
        morningMessager.setMorningMessager(this, prefs.getMorningNotificationHour(), prefs.getMorningNotificationMinute());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
