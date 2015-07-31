package paxspace.org.reflextimer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends Activity {

    private Button mainButton;
    private TextView mainText;

    private enum State { idle, wait, counting, finished }

    private State currentState;

    private Handler timer = new Handler();
    private Random rand = new Random();

    private static final int minWait_ms = 200, maxWait_ms = 3000;

    private int timerResolution_ms = 50, iterationNum;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainButton = (Button) findViewById(R.id.MainButton);
        mainText = (TextView) findViewById(R.id.textView);

        mainButton.setOnClickListener(new OnMainButtonClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainButton.setText(getString(R.string.start_button));
        mainText.setText(getString(R.string.start_text));
        currentState = State.idle;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        timerResolution_ms = Integer.valueOf(sharedPref.getString(SettingsActivity.KEY_PREF_RESOLUTION, "50"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class OnMainButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (currentState) {
                case idle:
                    start();
                    break;
                case wait:
                    foul();
                    break;
                case counting:
                    stop();
                    break;
                case finished:
                    reset();
                    break;
            }
        }
    }

    private void start() {
        timer.removeCallbacksAndMessages(null); //remove all callbacks
        mainButton.setText(getString(R.string.wait_button));
        mainText.setText(getString(R.string.wait_text));
        currentState = State.wait;
        iterationNum = 0;

        timer.postDelayed(new Runnable() {
            @Override
            public void run() {
                initiateTimer();
            }
        }, randInt(minWait_ms, maxWait_ms));
    }

    private void foul() {
        timer.removeCallbacksAndMessages(null); //remove all callbacks
        mainText.setText(getString(R.string.too_soon_text));
        mainButton.setText(getString(R.string.reset_button));
        currentState = State.finished;
    }

    private void initiateTimer() {
        timer.removeCallbacksAndMessages(null); //remove all callbacks
        mainButton.setText(getString(R.string.stop_button));
        mainText.setText("0 ms");
        iterationNum = 0;
        currentState = State.counting;
        startTime = SystemClock.uptimeMillis();
        timer.postAtTime(new Updater(), startTime + timerResolution_ms);
    }

    private class Updater implements Runnable {
        @Override
        public void run() {
            updateTime();
        }
    }

    private void updateTime() {
        long curTime = SystemClock.uptimeMillis() - startTime;
        mainText.setText(String.valueOf(curTime) + " ms");
        iterationNum++;
        timer.postAtTime(new Updater(), startTime + iterationNum* timerResolution_ms);
    }

    private void stop() {
        timer.removeCallbacksAndMessages(null); //remove all callbacks
        //leave last displayed time on screen
        mainButton.setText(getString(R.string.reset_button));
        currentState = State.finished;
    }

    private void reset() {
        timer.removeCallbacksAndMessages(null); //remove all callbacks
        mainText.setText(getString(R.string.start_text));
        mainButton.setText(getString(R.string.start_button));
        currentState = State.idle;
    }

    private int randInt(int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }
}
