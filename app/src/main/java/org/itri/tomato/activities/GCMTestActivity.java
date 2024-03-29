package org.itri.tomato.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.itri.tomato.R;

public class GCMTestActivity extends AppCompatActivity {
    private static final String TAG = "GCMTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcmtest);
        Intent intent = getIntent();
        if (intent.hasExtra("from") && intent.hasExtra("message")) {
            String from = intent.getStringExtra("from");
            String message = intent.getStringExtra("message");
            String collapse_key;
            Log.d(TAG, "from " + from + ": " + message);
        }
        TextView gcmMsg = (TextView) findViewById(R.id.gcmMsg);
        gcmMsg.setText(gcmMsg.getText().toString() + "\n: " + intent.getStringExtra("message"));
    }

}
