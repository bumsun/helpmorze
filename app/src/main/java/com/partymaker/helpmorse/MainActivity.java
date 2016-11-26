package com.partymaker.helpmorse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 31;


    private ClipboardManager clipboardManager;
    private boolean keepRecording;
    private boolean lineState;
    private String messageText;
    private TextView messageTextView;
    private HashMap<String, String> morseCode;
    private PowerManager powerManager;
    private Thread recordingThread;
    private AutoScrollView scrollView;
    private SignalGraph signalGraph;
    private TextView statusTextView;
    private PowerManager.WakeLock wakeLock;

    private static final int PERMISSION_REQUEST = 1;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK
    };
    private Button button_start;


    class UiRunnable implements Runnable {
        private final /* synthetic */ String val$letter;

        UiRunnable(String str) {
            this.val$letter = str;
        }

        public void run() {
            MainActivity.this.setMessageText(new StringBuilder(String.valueOf(MainActivity.this.messageText)).append(this.val$letter).toString());
        }
    }

    class UiRunnable2 implements Runnable {
        private final /* synthetic */ boolean val$currentState;
        private final /* synthetic */ String val$s;

        UiRunnable2(boolean z, String str) {
            this.val$currentState = z;
            this.val$s = str;
        }

        public void run() {
            MainActivity.this.setLineState(this.val$currentState);
            MainActivity.this.setStatusText(this.val$s);
        }
    }

    class UiRunnable3 implements Runnable {
        private final /* synthetic */ short val$avg2;
        private final /* synthetic */ short val$t;

        UiRunnable3(short s, short s2) {
            this.val$avg2 = s;
            this.val$t = s2;
        }

        public void run() {
            MainActivity.this.emitSignalLevel(this.val$avg2, this.val$t);
        }
    }

    class RecordRunnable implements Runnable {
        RecordRunnable() {
        }

        public void run() {
            MainActivity.this.doRecording();
        }
    }






    class ClearListener implements View.OnClickListener {
        ClearListener() {
        }

        public void onClick(View v) {
            MainActivity.this.setMessageText("");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(INITIAL_PERMS, PERMISSION_REQUEST);
        } else {

        }
    }

    private void startApp() {
        this.messageText = "";
        this.lineState = false;

        this.powerManager = (PowerManager) getSystemService("power");
        this.wakeLock = this.powerManager.newWakeLock(536870918, "Morse Code Reader");
        this.clipboardManager = (ClipboardManager) getSystemService("clipboard");
        this.morseCode = new HashMap();
        this.morseCode.put(".-", "A");
        this.morseCode.put("-...", "B");
        this.morseCode.put("-.-.", "C");
        this.morseCode.put("-..", "D");
        this.morseCode.put(".", "E");
        this.morseCode.put("..-.", "F");
        this.morseCode.put("--.", "G");
        this.morseCode.put("....", "H");
        this.morseCode.put("..", "I");
        this.morseCode.put(".---", "J");
        this.morseCode.put("-.-", "K");
        this.morseCode.put(".-..", "L");
        this.morseCode.put("--", "M");
        this.morseCode.put("-.", "N");
        this.morseCode.put("---", "O");
        this.morseCode.put(".--.", "P");
        this.morseCode.put("--.-", "Q");
        this.morseCode.put(".-.", "R");
        this.morseCode.put("...", "S");
        this.morseCode.put("-", "T");
        this.morseCode.put("..-", "U");
        this.morseCode.put("...-", "V");
        this.morseCode.put(".--", "W");
        this.morseCode.put("-..-", "X");
        this.morseCode.put("-.--", "Y");
        this.morseCode.put("--..", "Z");
        this.morseCode.put(".----", "1");
        this.morseCode.put("..---", "2");
        this.morseCode.put("...--", "3");
        this.morseCode.put("....-", "4");
        this.morseCode.put(".....", "5");
        this.morseCode.put("-....", "6");
        this.morseCode.put("--...", "7");
        this.morseCode.put("---..", "8");
        this.morseCode.put("----.", "9");
        this.morseCode.put("-----", "0");
        this.morseCode.put(".-.-.-", ".");
        this.morseCode.put("--..--", ",");
        this.morseCode.put("---...", ":");
        this.morseCode.put("..--..", "?");
        this.morseCode.put(".----.", "'");
        this.morseCode.put("-....-", "-");
        this.morseCode.put("-..-.", "/");
        this.morseCode.put("-.--.", "(");
        this.morseCode.put("-.--.-", ")");
        this.morseCode.put(".-..-.", "\"");
        this.morseCode.put("-...-", "=");
        this.morseCode.put(".-.-.", "+");
        this.morseCode.put(".--.-.", "@");
        this.morseCode.put("space", " ");
        onResume();
    }

    private void emitLetter(String dits) {
        if (!dits.equals("")) {
            String letter = (String) this.morseCode.get(dits);
            if (letter != null) {
                runOnUiThread(new UiRunnable(letter));
            }
        }
    }

    private void doRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(11025, 2, 2);
        AudioRecord audioRecord = new AudioRecord(1, 11025, 2, 2, bufferSize);
        audioRecord.startRecording();
        short[] buffer = new short[bufferSize];
        double avg = 0.0d;
        double longavg = 0.0d;
        double peak = 0.0d;
        boolean state = false;
        long counter = 0;
        long peakCounter = 0;
        String dits = "";
        long ditLength = (long) (0.06666666666666668d * ((double) 11025));
        while (this.keepRecording) {
            int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
            for (int i = 0; i < bufferReadResult; i++) {
                peakCounter++;
                if (peakCounter > ((long) (11025 * 2))) {
                    peak *= 0.9999d;
                }
                avg = ((99.0d * avg) + ((double) Math.abs(buffer[i]))) / 100.0d;
                if (avg > peak) {
                    peak = avg;
                    peakCounter = 0;
                }
                longavg = ((9999.0d * longavg) + ((double) Math.abs(buffer[i]))) / 10000.0d;
                counter++;
                double threshold = 0.43d * peak;
                if (threshold < 300.0d) {
                    threshold = 300.0d;
                }

                if ((((avg > threshold) ? true : false) ^ state) != false) {
                    if (state) {
                        if (((double) counter) > 0.02d * ((double) 11025) && counter < ditLength) {
                            ditLength = counter;
                        } else if (counter > 3 * ditLength) {
                            ditLength = counter / 3;
                        }
                    }
                    if (((double) counter) > 0.02d * ((double) 11025)) {
                        if (state) {
                            dits = counter < 2 * ditLength ? new StringBuilder(String.valueOf(dits)).append(".").toString() : new StringBuilder(String.valueOf(dits)).append("-").toString();
                        } else if (counter >= 2 * ditLength) {
                            emitLetter(dits);
                            dits = "";
                            if (counter >= 4 * ditLength) {
                                emitLetter("space");
                            }
                        }
                    }
                    if (state) {
                        state = false;
                    } else {
                        state = true;
                    }
                    runOnUiThread(new UiRunnable2(state, "peak: " + peak + " ditLength: " + ditLength));
                    counter = 0;
                } else if (counter > 10 * ditLength) {
                    counter = 0;
                    emitLetter(dits);
                    dits = "";
                }
                if (i % 128 == 0) {
                    runOnUiThread(new UiRunnable3((short) ((int) avg), (short) ((int) threshold)));
                }
            }
        }
        audioRecord.stop();
    }

    protected void onPause() {
        super.onPause();
        try {
            this.keepRecording = false;
            try {
                this.recordingThread.join();
            } catch (InterruptedException e) {
            }
            if (this.wakeLock.isHeld()) {
                this.wakeLock.release();
            }
        }catch (Exception e){

        }

    }

    protected void onResume() {
        super.onResume();
        try{
            if (!this.wakeLock.isHeld()) {
                this.wakeLock.acquire();
            }
            this.keepRecording = true;
            this.recordingThread = new Thread(new RecordRunnable());
            this.recordingThread.start();
        }catch (Exception e){

        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
        statusTextView.setVisibility(View.INVISIBLE);
    }

    private void setMessageText(String text) {
        this.messageText = text;
        this.messageTextView.setText(text);
        this.scrollView.scrollDownMaybe();
    }

    private void setupUI() {
        this.messageTextView = (TextView) findViewById(R.id.message_text);
        this.messageTextView.setText(this.messageText);
        this.statusTextView = (TextView) findViewById(R.id.status_text);
        this.signalGraph = (SignalGraph) findViewById(R.id.signal_graph);
        setLineState(this.lineState);
        this.scrollView = (AutoScrollView) findViewById(R.id.scrollView_messagetext);
        this.button_start = (Button) findViewById(R.id.button_start);

        this.button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startApp();
            }
        });


        ((Button) findViewById(R.id.button_clear)).setOnClickListener(new ClearListener());
    }

    private void setLineState(boolean state) {
        this.lineState = state;
    }

    private void setStatusText(String s) {
        this.statusTextView.setText(s);
    }

    private void emitSignalLevel(short l, short t) {
        this.signalGraph.emit(l, t);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(INITIAL_PERMS, PERMISSION_REQUEST);
                }
            }
            return;

        }
    }
}
