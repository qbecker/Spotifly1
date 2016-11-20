package com.example.qbecker.spotifly1;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonArray;

public class ShareActivity extends AppCompatActivity {

    String songLink;
    String tosend;
    LocalConfig config = new LocalConfig();
    String host = config.HOST_NAME;
    public void doSomething(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        final EditText shareQueue = (EditText) findViewById(R.id.ShareQueue);
        Button submitbttn = (Button) findViewById(R.id.submitButton);
        submitbttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //songLink = "https://open.spotify.com/track/3LRJbFT9rKoKv4aW7PuBJC";


                String parseOut[] = songLink.split("/");

                if(parseOut != null && parseOut.length > 0) {
                    songLink = parseOut[parseOut.length - 1];
                    Log.d("Testing", songLink);
                }
                String s = "";
                s = shareQueue.getText().toString();
                s = s.trim().replace(" ", "");
                tosend = host+"add/" + s+ "/" + songLink;
                ShareActivity.JSONBackgroud back = new ShareActivity.JSONBackgroud();
                back.execute(tosend);


            }
        });

        Intent intent = getIntent();
        songLink = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (intent.getType().indexOf("image/") != -1) {
            Log.d("What they shared", "picture");
        } else if (intent.getType().equals("text/plain")) {
            Log.d("What they shared", songLink);
        }
    }
    private class JSONBackgroud extends AsyncTask<String, String, JsonArray> {
        @Override
        protected JsonArray doInBackground(String... params) {
            JSONGetter jsn = new JSONGetter();
            try {
                JsonArray result = jsn.sendGet(params[0]);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public void onPostExecute(JsonArray result){
            if (result.size() <=0){
                try {
                    Thread.sleep(3000);
                    ShareActivity.JSONBackgroud jsn = new ShareActivity.JSONBackgroud();
                    jsn.execute(tosend);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                // lets close the activity
                finish();
            }
        }
    }
}
