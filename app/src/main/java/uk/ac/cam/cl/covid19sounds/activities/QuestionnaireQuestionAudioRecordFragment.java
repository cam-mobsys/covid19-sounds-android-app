package uk.ac.cam.cl.covid19sounds.activities;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.ac.cam.cl.covid19sounds.R;
import uk.ac.cam.cl.covid19sounds.dataEntry.DataConstants;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.Question;
import uk.ac.cam.cl.covid19sounds.dataEntry.questionnaire.QuestionAudioRecord;

//class to handle audio recordings

public class QuestionnaireQuestionAudioRecordFragment extends Fragment {

    private static String ARG_QUESTION = "question";
    private QuestionAudioRecord question;
    private QuestionnaireQuestionAudioRecordFragment.OnQuestionInteractionListener mListener;
    private AudioRecord recorder = null;
    private boolean isRecording = false;
    private int recordTime;
    private Handler handler;
    private int bufferSize = 0;

    private Thread recordingThread = null;

    private View mView;
    private SeekBar seekBar;

    // Handler for click events on the record buttons
    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            //check if the user has pressed record or stop button and act accordingly
            if (id == R.id.recordButton) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            } else if (id == R.id.deleteButton) {
                deleteRecording(); // deletes file if exists and disables Submit button
            }
        }
    };



    public static QuestionnaireQuestionAudioRecordFragment newInstance(Question question) {
        QuestionnaireQuestionAudioRecordFragment fragment = new QuestionnaireQuestionAudioRecordFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_QUESTION, question);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = getArguments().getParcelable(ARG_QUESTION);
        }
        handler = new Handler();
        // we will record .wav files with 16k sampling rate, pcm 16 bit encoding and stereo channels
        bufferSize = AudioRecord.getMinBufferSize(DataConstants.SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_questionnaire_audio_record, container, false);
        // Define the page elements and events
        TextView text = mView.findViewById(R.id.questionnaire_audio_record_text);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        text.setText(question.getText());
        text.setTextColor(Color.BLACK);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            //ask permission to record audio if not given already by the user.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    DataConstants.AUDIO_PERMISSION_REQUEST_CODE);
        }

        // Set listeners on buttons
        setButtonHandlers();

        // Initialise the submit button
        Button button = mView.findViewById(R.id.questionnaire_audio_record_submit);
        button.setText(button.getText());

        setupSubmitButton(button);

        seekBar = mView.findViewById(R.id.seekBar);
        seekBar.setMax(Integer.parseInt(question.getOptions()[0]));
        return mView;
    }

    private String getFileName(String recordingType) {
        //return raw audio recording file name.
        return getActivity().getFilesDir() + "/" + recordingType + ".raw";
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof QuestionnaireQuestionAudioRecordFragment.OnQuestionInteractionListener) {
            mListener = (QuestionnaireQuestionAudioRecordFragment.OnQuestionInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setupSubmitButton(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when question submitted, set filename as answer to then be copied
                question.setAnswer(getFileName(question.getID()));
                if (mListener != null) {
                    mListener.onAudioRecordQuestionSubmission();
                }
            }
        });
        button.setEnabled(false); // don't enable until the recording is made
    }

    private void deleteRecording() {
        // Delete recording
        File file = new File(getFileName(question.getID()));
        if (file.exists()) {
            file.delete();
        } // Disable submission until audio is recorded
        enableButton(R.id.questionnaire_audio_record_submit, false);
        enableButton(R.id.recordButton, true);
        enableButton(R.id.deleteButton, false);
    }

    private void startRecording() {
        //TODO: Disable back button
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            //ask to record audio permissions.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    DataConstants.AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            // Record the audio
            ((Button) mView.findViewById(R.id.recordButton)).setText(getString(R.string.stop));
            enableButton(R.id.deleteButton, false); // can't delete

            recordTime = 0;


            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    DataConstants.SAMPLING_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            recorder.startRecording();
            isRecording = true;
            recordingThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    writeAudioDataToFile();
                }
            }, "AudioRecorder Thread");

            recordingThread.start();
            handler.post(UpdateRecordTime);

        }
    }


    private void writeAudioDataToFile() {
        //write recording audio to a file
        byte[] data = new byte[bufferSize];
        String filename = getFileName(question.getID());
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }

        int read;

        if (null != os) {
            while (isRecording) {
                read = recorder.read(data, 0, bufferSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void enableButton(int id, boolean isEnable) {
        mView.findViewById(id).setEnabled(isEnable);
    }

    private void setButtonHandlers() {
        mView.findViewById(R.id.recordButton).setOnClickListener(btnClick);

        mView.findViewById(R.id.deleteButton).setOnClickListener(btnClick);

        enableButton(R.id.deleteButton, false);
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {

            isRecording = false;
            recorder.stop();
            mView.findViewById(R.id.recordButton).invalidate();

            recorder.release();
            recorder = null;
            recordingThread = null;

            ((Button) mView.findViewById(R.id.recordButton)).setText(getString(R.string.Record));
            // enableButton(R.id.playButton, true);
            enableButton(R.id.deleteButton, true);
            enableButton(R.id.recordButton, false);
            enableButton(R.id.questionnaire_audio_record_submit, true);
            recordTime = 0;
            seekBar.setProgress(0);
        }
    }

    public interface OnQuestionInteractionListener {
        void onAudioRecordQuestionSubmission();
    }

    Runnable UpdateRecordTime = new Runnable(){
        public void run(){
            //show how much recording has been done
            if(isRecording){
                recordTime++;
                seekBar.setProgress(recordTime);

                if (recordTime > Integer.parseInt(question.getOptions()[0])) {
                    stopRecording();
                }
                handler.postDelayed(this, 1000);
            }
        }
    };


}
