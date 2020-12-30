package uk.ac.cam.cl.covid19sounds.transfer;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import uk.ac.cam.cl.covid19sounds.dataEntry.user.UserOnboardingPrefs;

import static uk.ac.cam.cl.covid19sounds.MainApplication.refreshCredentials;
import static uk.ac.cam.cl.covid19sounds.utils.sharedConfig.COVID_LOG_TAG;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String SERVER_URL_FILE = "server url file path here";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }



    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient
            contentProviderClient, SyncResult syncResult) {
        Context context = this.getContext();
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(connMgr).getActiveNetworkInfo();
        if ((networkInfo != null) && (networkInfo.isConnected())) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                if(refreshCredentials(context, UserOnboardingPrefs.getInstance(context).getUserID())) {

                    try {
                        //System.out.println("push files");
                        pushFiles(context);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }


    private void pushFiles(Context context) throws IOException {
        String token = UserOnboardingPrefs.getInstance(context).getAccessToken();
        HttpClient client = new DefaultHttpClient();

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);


        // first, get the file directory
        File directory = context.getFilesDir();

        // get the names of the files
        File[] files = directory.listFiles();

        // check if files instance is null
        if (files == null) {
            Log.e(COVID_LOG_TAG, "Encountered null files variable, returning");
            return;
        }

        // loop to pack the files into the body
        for (File f : files) {

            if (f.getName().endsWith(".wav") || f.getName().endsWith(".json")) {
                // add the file pair - (key, satellite value)

                entityBuilder.addBinaryBody(f.getName(), f);
            }
        }

        // build the entity
        HttpEntity entity = entityBuilder.build();

        // create a put request
        HttpPut put = new HttpPut(SERVER_URL_FILE);
        put.setEntity(entity);
        put.setHeader("Authorization", "Bearer " + token);

        // try to execute this
        HttpResponse response = client.execute(put);

        // check if the request completed successfully.
        if (response.getStatusLine().getStatusCode() == HttpsURLConnection.HTTP_OK) {
            // delete the files, only if we uploaded correctly.
            for (File f : files) {
                if (f.delete()) {
                    Log.d(COVID_LOG_TAG, "Temporary file deleted successfully");

                } else {
                    Log.e(COVID_LOG_TAG, "Failed to delete file");
                }
            }
            // knock for finish
            Log.d(COVID_LOG_TAG, "File uploaded correctly.");
        } else {
            int code = response.getStatusLine().getStatusCode();
            String reason = response.getStatusLine().getReasonPhrase();
            Log.d(COVID_LOG_TAG, "Upload failed with code " + code + ", reason: " + reason);
        }


    }
}