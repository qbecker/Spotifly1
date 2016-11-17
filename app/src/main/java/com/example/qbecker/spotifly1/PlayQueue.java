package com.example.qbecker.spotifly1;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;

public class PlayQueue extends Activity implements Player.NotificationCallback {

    LocalConfig conf = new LocalConfig();
    String host = conf.HOST_NAME;

    Timer t;
    boolean isPaused = false;
    boolean nextSong = false;
    private TextView txt;
    public String tosend;
    public String queueName;
    private  ListView  mSongList;
    private String[] songList;
    private ArrayList<SongWrapper> songArrList = new ArrayList<SongWrapper>();
    private ArrayAdapter adapter;
    private Player mPlayer = MainActivity.mPlayer;
    private  PlaybackState mCurrentPlaybackState;
    private final Player.OperationCallback mOperationCallBack = new Player.OperationCallback(){

        @Override
        public void onSuccess() {}

        @Override
        public void onError(Error error) {
            nextSong();
        }
    };

    public void nextSong(){
        SongWrapper temp = songArrList.get(0);
        songArrList.remove(0);
        songArrList.add(songArrList.size(), temp);
        nextSong = true;
        updateListView();
    }

    public void prevSong(){
        SongWrapper temp = songArrList.get(songArrList.size()-1);
        songArrList.remove(songArrList.size()-1);
        songArrList.add(0, temp);
        nextSong = true;
        updateListView();
    }

    public void updateListView(){
        songList = new String[songArrList.size()];
        for(int i = 0; i < songArrList.size(); i++){
            SongWrapper songWrapper = songArrList.get(i);
            songList[i] = songWrapper.song;
        }
        adapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_1, songList);
        mSongList.setAdapter(adapter);
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        if(!mCurrentPlaybackState.isPlaying && !isPaused || nextSong){
            String selectedFromList =(String) (mSongList.getItemAtPosition(0));
            mPlayer.playUri(null, "spotify:track:" + selectedFromList, 0, 0);
            nextSong = false;
        }

    }

    public void populateSongArr(JsonArray songs){
        for(int k = 0; k < songs.size(); k++) {
            SongWrapper wrap = new SongWrapper();
            JsonObject songData = songs.get(k).getAsJsonObject();
            wrap.setSong(songData.get("Song").getAsString());
                if(!songArrList.isEmpty()){
                    int count = 0;
                    for(int i = 0; i < songArrList.size(); i++){
                        SongWrapper tester = songArrList.get(i);
                        String temp1 = tester.getSong();
                        String temp2 = wrap.getSong();
                        if(temp1.equalsIgnoreCase(temp2)){
                            count ++;
                        }
                    }
                    if(count < 1){
                        songArrList.add(wrap);
                    }
                }else{
                    songArrList.add(wrap);
                }
        }
        updateListView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
              //Called each time when 1000 milliseconds (1 second) (the period parameter)
              if(queueName != null){
                  JSONBackgroud back = new JSONBackgroud();
                  tosend = host + "get/"+queueName;
                  back.execute(tosend);
              }
          }
        }, 2000, 25000);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_queue);
        mPlayer.addNotificationCallback(PlayQueue.this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mSongList = (ListView) findViewById(R.id.PlayList);
        txt = (TextView) findViewById(R.id.QueueNameText);
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
                mCurrentPlaybackState = mPlayer.getPlaybackState();
                if (mCurrentPlaybackState != null && mCurrentPlaybackState.isPlaying) {
                    mPlayer.pause(mOperationCallBack);
                    isPaused = true;
                } else {
                    mPlayer.resume(mOperationCallBack);
                    isPaused = false;
                }
            }
        });

        mSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongWrapper temp = songArrList.get(i);
                songArrList.remove(i);
                songArrList.add(0, temp);
                if(i>0){
                    nextSong = true;
                    updateListView();
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            queueName = extras.getString("QueueName");
            txt.setText((CharSequence) queueName);
        }
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("Player Event ", playerEvent.name());
        switch(playerEvent){
            case kSpPlaybackNotifyTrackDelivered:
                nextSong();
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        nextSong();
    }

    private class JSONBackgroud extends AsyncTask<String, String, JsonArray>{
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
                    JSONBackgroud jsn = new JSONBackgroud();
                    jsn.execute(tosend);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                populateSongArr(result);
            }
        }
    }
}
