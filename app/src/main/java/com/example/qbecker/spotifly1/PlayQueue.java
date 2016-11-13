package com.example.qbecker.spotifly1;

import android.app.Activity;
import android.os.StrictMode;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class PlayQueue extends Activity implements ConnectionStateCallback, Player.NotificationCallback {

    LocalConfig conf = new LocalConfig();
    String host = conf.HOST_NAME;

    TextView txt;

    ListView mSongList;
    ArrayList<SongWrapper> songArrList = new ArrayList<SongWrapper>();




    private JsonArray sendGet(String toSend) throws Exception {
        URL obj = new URL(toSend);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JsonArray result = new JsonParser().parse(response.toString()).getAsJsonArray();
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_queue);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mSongList = (ListView) findViewById(R.id.PlayList);
        txt = (TextView) findViewById(R.id.QueueNameText);

        mSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFromList =(String) (mSongList.getItemAtPosition(i));
                MainActivity.mPlayer.playUri(null, "spotify:track:" + selectedFromList, 0, 0);

            }

        });

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String queueName = extras.getString("QueueName");
            txt.setText((CharSequence) queueName);

            try {

                JsonArray songs = sendGet(host + "get/"+queueName);

                for(JsonElement sg : songs) {
                    SongWrapper wrap = new SongWrapper();
                    JsonObject songData = sg.getAsJsonObject();
                    wrap.song = songData.get("Song").getAsString();
                    songArrList.add(wrap);
                }
                String[] songList = new String[songArrList.size()];
                for(int i = 0; i < songArrList.size(); i++){
                    SongWrapper songWrapper = songArrList.get(i);
                    songList[i] = songWrapper.song;
                }

                ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, songList);
                mSongList.setAdapter(adapter);

            } catch (Exception e) {
                e.printStackTrace();
            }

            //The key argument here must match that used in the other activity

        }
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(int i) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

    }

    @Override
    public void onPlaybackError(Error error) {

    }
}
