package uk.ac.cam.cl.covid19sounds.dataEntry;

public class DataConstants {
    public static final String PARTICIPANT_ID = "participant_id";
    public static final String FINISH_DATETIME = "datetime";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final int SAMPLING_RATE = 16000;
    public static final int NUM_CHANNELS = 2;
    public static final int ENCODING_BITS = 16;
    public static final String APP_VERSION = "app_version";
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;
    public static final int LOC_REQUEST_CODE = 10001; //Application specific request code to
    // match with a result reported to startResolutionForResult for location acccess setttings. this is for location settings enabling
    //gps on or off.
    public static final int LOC_PERMISSION_REQUEST_CODE = 1; //Application specific request code to
    // match with a result reported to OnRequestPermissionsResultCallback. this is asking location permission.
    public static final int LOC_UPDATE_INTERVAL = 1000;
    public static final int LOC_UPDATE_FASTEST_INTERVAL = 500;
    public static final int LOC_NUM_UPDATES = 1;

    public static final int AUDIO_PERMISSION_REQUEST_CODE = 1234; //Application specific request code to
    // match with a result reported to OnRequestPermissionsResultCallback. this is asking audio recording permission.


}
