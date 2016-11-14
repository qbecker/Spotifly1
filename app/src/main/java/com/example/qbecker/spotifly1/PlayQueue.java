package com.example.qbecker.spotifly1;

import android.app.Activity;
import android.os.Message;
import android.os.StrictMode;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
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
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class PlayQueue extends Activity implements Player.NotificationCallback {

    LocalConfig conf = new LocalConfig();
    String host = conf.HOST_NAME;

    TextView txt;

    public static ListView  mSongList;
    static String[] songList;
    static ArrayList<SongWrapper> songArrList = new ArrayList<SongWrapper>();
    static ArrayAdapter adapter;
    public static Player mPlayer = MainActivity.mPlayer;
    public static PlaybackState mCurrentPlaybackState;
    private final Player.OperationCallback mOperationCallBack = new Player.OperationCallback(){

        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(Error error) {

        }
    };

    public  void nextSong(){
        SongWrapper temp = songArrList.get(0);
        songArrList.remove(0);
        songArrList.add(songArrList.size(), temp);
        updateListViewNew();

    }

    public void prevSong(){
        SongWrapper temp = songArrList.get(songArrList.size()-1);
        songArrList.remove(songArrList.size()-1);
        songArrList.add(0, temp);
        updateListViewNew();

    }

    public void updateListViewNew(){
        songList = new String[songArrList.size()];
        for(int i = 0; i < songArrList.size(); i++){
            SongWrapper songWrapper = songArrList.get(i);
            songList[i] = songWrapper.song;
        }

        adapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_1, songList);
        mSongList.setAdapter(adapter);
        String selectedFromList =(String) (mSongList.getItemAtPosition(0));
        mPlayer.playUri(null, "spotify:track:" + selectedFromList, 0, 0);

    }

    public void updateListView(JsonArray songs){
        for(JsonElement sg : songs) {
            SongWrapper wrap = new SongWrapper();
            JsonObject songData = sg.getAsJsonObject();
            wrap.song = songData.get("Song").getAsString();
            songArrList.add(wrap);
        }
        songList = new String[songArrList.size()];
        for(int i = 0; i < songArrList.size(); i++){
            SongWrapper songWrapper = songArrList.get(i);
            songList[i] = songWrapper.song;
        }

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, songList);
        mSongList.setAdapter(adapter);

    }

    public static void chooseWhatToDo(PlayerEvent playerEvent){
        PlayQueue play = new PlayQueue();
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        Log.d("Player Event ", playerEvent.name());
        switch(playerEvent){
            case kSpPlaybackNotifyTrackDelivered:
                //nextSong();
                break;

        }
    }

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
        mPlayer.addNotificationCallback(PlayQueue.this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mSongList = (ListView) findViewById(R.id.PlayList);

        Button prevSongButtn = (Button) findViewById(R.id.prev_button);
        prevSongButtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevSong();
            }
        });

        Button nextSongButtn = (Button) findViewById(R.id.next_button);
        nextSongButtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                nextSong();
            }
        });
        Button playPauseButtn = (Button) findViewById(R.id.Play_Pause_button);
        playPauseButtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentPlaybackState != null && mCurrentPlaybackState.isPlaying) {
                    mPlayer.pause(mOperationCallBack);
                } else {
                    mPlayer.resume(mOperationCallBack);
                }
            }
        });

        mSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFromList =(String) (mSongList.getItemAtPosition(i));
                mPlayer.playUri(null, "spotify:track:" + selectedFromList, 0, 0);

            }

        });


        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String queueName = extras.getString("QueueName");
           // txt.setText((CharSequence) queueName);
            try {
                JsonArray songs = sendGet(host + "get/"+queueName);
                updateListView(songs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("Player Event ", playerEvent.name());
        switch(playerEvent){
            case kSpPlaybackNotifyTrackDelivered:
                Log.d("Its calling", "This");
                nextSong();
                break;

        }
    }

    @Override
    public void onPlaybackError(Error error) {

    }
}
