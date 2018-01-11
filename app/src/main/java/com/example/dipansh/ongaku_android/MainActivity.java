package com.example.dipansh.ongaku_android;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton play;
    private MediaPlayer media;
    private SeekBar seekBar;
    private int songLength;
    private String urltext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        play = findViewById(R.id.play);
        media = new MediaPlayer();
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(99);

        ArrayList<Song> list = new ArrayList<>();
        list = extractData(ReadFromfile("data.json", MainActivity.this));

        Toast.makeText(this, list.get(1).getImage(), Toast.LENGTH_SHORT).show();


        ListView listView = (ListView) findViewById(R.id.list);
        SongAdapter adapter = null;
        adapter = new SongAdapter(MainActivity.this ,R.layout.list_item ,list);
        listView.setAdapter(adapter);


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://lyricmp3skull.org/s363640c/file/boku-no-hero-academia-op-1/261606779.mp3"; // your URL here
                media.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    media.setDataSource(url);
                    Toast.makeText(MainActivity.this, "starting", Toast.LENGTH_SHORT).show();
                    media.prepare(); // might take long! (for buffering, etc)
                } catch (IOException e) {
                    e.printStackTrace();
                }
                media.start();

                Toast.makeText(MainActivity.this, "started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ArrayList<Song> extractData(String s){

        ArrayList<Song> mylist = new ArrayList<>();

        try {
            JSONObject main = new JSONObject(s);
            JSONObject contacts = main.getJSONObject("songs");

            JSONArray list = contacts.getJSONArray("openings");
            for(int i=0;i<list.length();i++){
                JSONObject obj = list.getJSONObject(i);

                String link = obj.getString("link");
                String name = obj.getString("name");
                String image = obj.getString("img");

                Song song = new Song(link , name , image);
                mylist.add(song);
            }

            list = contacts.getJSONArray("endings");
            for(int i=0;i<list.length();i++){
                JSONObject obj = list.getJSONObject(i);

                String link = obj.getString("link");
                String name = obj.getString("name");
                String image = obj.getString("img");

                Song song = new Song(link , name , image);
                mylist.add(song);
            }

            list = contacts.getJSONArray("osts");
            for(int i=0;i<list.length();i++){
                JSONObject obj = list.getJSONObject(i);

                String link = obj.getString("link");
                String name = obj.getString("name");
                String image = obj.getString("img");

                Song song = new Song(link , name , image);
                mylist.add(song);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mylist;
    }


    public String ReadFromfile(String fileName, Context context) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets()
                    .open(fileName, Context.MODE_WORLD_READABLE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }




    private class PrepareMusicPlayer extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... strings) {
            try {
                media.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String string) {
            media.start();
            play.setImageResource(android.R.drawable.ic_media_pause);
            Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_SHORT).show();
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
