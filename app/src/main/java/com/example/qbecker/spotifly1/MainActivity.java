package com.example.qbecker.spotifly1;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//This class will hold the spotify authentication and queue creation/ joining an already made queue.

public class MainActivity extends AppCompatActivity {

    LocalConfig conf = new LocalConfig();
    String host = conf.HOST_NAME;
    int REQUEST_CODE = conf.REQUEST_CODE;
    String REDIRECT_URI = conf.REDIRECT_URI;
    String CLIENT_ID = conf.CLIENT_ID;

    Intent testIntent = new Intent(MainActivity.this, PlayQueue.class);

    private JsonObject sendGet(String toSend) throws Exception {
        String url = toSend;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JsonObject result = new JsonParser().parse(response.toString()).getAsJsonObject();
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);


        //Text field
        final EditText queueNameTxt = (EditText) findViewById(R.id.queueNameTextBox);
        //create Button
        Button button = (Button) findViewById(R.id.loggedIn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "";
                s = queueNameTxt.getText().toString();
                s = s.trim().replace(" ", "");
                String tosend = host+"create/" + s;
                try {
                    JsonObject response = sendGet(tosend);

                    if(response.get("response").getAsString().equals("N")){
                        //Toast.makeText(MainActivity.this, "Sorry that queue already exists, please try another", Toast.LENGTH_SHORT).show();

                        Toast.makeText(MainActivity.this, "Queue created!", Toast.LENGTH_SHORT).show();

                        testIntent.putExtra("QueueName", s);
                        MainActivity.this.startActivity(testIntent);
                    }else if(response.get("response").getAsString().equals("Y")){
                        Toast.makeText(MainActivity.this, "Queue created!", Toast.LENGTH_SHORT).show();
                        Intent testIntent = new Intent(MainActivity.this, PlayQueue.class);
                        testIntent.putExtra("QueueName", s);
                        MainActivity.this.startActivity(testIntent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    testIntent.putExtra("Auth Token", response);
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

}