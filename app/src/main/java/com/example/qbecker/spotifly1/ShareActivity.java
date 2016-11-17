package com.example.qbecker.spotifly1;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.gson.JsonArray;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Button submitbttn = (Button) findViewById(R.id.submitButton);
        submitbttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Intent intent = getIntent();
        Uri data = intent.getData();
       // String test = data.toString();
        // Figure out what to do based on the intent type
        if (intent.getType().indexOf("image/") != -1) {
            Log.d("What they shared", "picture");
        } else if (intent.getType().equals("text/plain")) {
            Log.d("What they shared", "Something");
        }
    }
}
