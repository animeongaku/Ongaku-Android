package com.example.dipansh.ongaku_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
    private FloatingActionButton next;
    private FloatingActionButton previous;
    private MediaPlayer media;
    private SeekBar seekBar;

    private int songLength;
    private int songPosition;
    private String urltext;
    public ProgressBar progressBar;
    private ProgressBar progressBar2;
    private Handler handler;
    private Boolean prefOpen, prefEnd, prefOsts;
    private ArrayList<Song> list;
    private SongAdapter adapter;
    private ListView listView;

    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        progressBar = findViewById(R.id.progressBar);
        media = new MediaPlayer();
        media.setAudioStreamType(AudioManager.STREAM_MUSIC);
        seekBar = findViewById(R.id.seekBar);
        progressBar2 = findViewById(R.id.progressBar2);

        urltext = null;
        songPosition = 0;

        sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);

        prefOpen = sharedPref.getBoolean(getString(R.string.pref_openings), true);
        prefEnd = sharedPref.getBoolean(getString(R.string.pref_endings), true);
        prefOsts = sharedPref.getBoolean(getString(R.string.pref_osts), true);

        list = extractData(ReadFromfile("data.json", MainActivity.this), prefOpen, prefEnd, prefOsts);

        Toast.makeText(this, "Welcome to Ongaku !!", Toast.LENGTH_SHORT).show();

        progressBar.setVisibility(View.INVISIBLE);
        listView = (ListView) findViewById(R.id.list);
        adapter = null;
        adapter = new SongAdapter(MainActivity.this ,R.layout.list_item ,list);
        listView.setAdapter(adapter);

        handler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(media != null){
                    if(media.isPlaying()){
                        progressBar2.setProgress((int)((float)media.getCurrentPosition()*100/songLength));
                    }
                }
                handler.postDelayed(this, 30);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!media.isPlaying()){
                    if(urltext != null){
                        media.start();
                        play.setImageResource(android.R.drawable.ic_media_pause);
                        Toast.makeText(MainActivity.this, "playing", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Select a song to play !!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    media.pause();
                    play.setImageResource(android.R.drawable.ic_media_play);
                    Toast.makeText(MainActivity.this, "paused", Toast.LENGTH_SHORT).show();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list.size()==songPosition+1){
                    urltext = list.get(0).getLink();
                    songPosition = 0;
                    Toast.makeText(MainActivity.this, list.get(songPosition).getName(), Toast.LENGTH_SHORT).show();
                }else{
                    urltext = list.get(songPosition+1).getLink();
                    songPosition++;
                    Toast.makeText(MainActivity.this, list.get(songPosition).getName(), Toast.LENGTH_SHORT).show();
                }
                try {
                    media.reset();
                    media.setDataSource(urltext);
                    play.setVisibility(View.INVISIBLE);
                    new PrepareMusicPlayer().execute(urltext);
                    progressBar.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songPosition-1 < 0){
                    urltext = list.get(list.size()-1).getLink();
                    songPosition = list.size()-1;
                    Toast.makeText(MainActivity.this, list.get(songPosition).getName(), Toast.LENGTH_SHORT).show();
                }else{
                    urltext = list.get(songPosition-1).getLink();
                    songPosition--;
                    Toast.makeText(MainActivity.this, list.get(songPosition).getName(), Toast.LENGTH_SHORT).show();
                }
                try {
                    media.reset();
                    media.setDataSource(urltext);
                    play.setVisibility(View.INVISIBLE);
                    new PrepareMusicPlayer().execute(urltext);
                    progressBar.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Song song = (Song) parent.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, song.getName(), Toast.LENGTH_SHORT).show();

                try {
                    urltext = song.getLink();
                    songPosition = position;
                    media.reset();
                    media.setDataSource(urltext);
                    play.setVisibility(View.INVISIBLE);
                    new PrepareMusicPlayer().execute(urltext);
                    progressBar.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(media.isPlaying()){
                    int playPosition = (songLength/100)*seekBar.getProgress();
                    media.seekTo(playPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public ArrayList<Song> extractData(String s, Boolean open, Boolean end, Boolean osts){

        ArrayList<Song> mylist = new ArrayList<>();

        try {
            JSONObject main = new JSONObject(s);
            JSONObject contacts = main.getJSONObject("songs");

            JSONArray list = contacts.getJSONArray("openings");
            if(open){
                for(int i=0;i<list.length();i++){
                    JSONObject obj = list.getJSONObject(i);

                    String link = obj.getString("link");
                    String name = obj.getString("name");
                    String image = obj.getString("img");

                    Song song = new Song(link , name , image);
                    mylist.add(song);
                }
            }

            list = contacts.getJSONArray("endings");
            if(end){
                for(int i=0;i<list.length();i++){
                    JSONObject obj = list.getJSONObject(i);

                    String link = obj.getString("link");
                    String name = obj.getString("name");
                    String image = obj.getString("img");

                    Song song = new Song(link , name , image);
                    mylist.add(song);
                }
            }

            list = contacts.getJSONArray("osts");
            if(osts){
                for(int i=0;i<list.length();i++){
                    JSONObject obj = list.getJSONObject(i);

                    String link = obj.getString("link");
                    String name = obj.getString("name");
                    String image = obj.getString("img");

                    Song song = new Song(link , name , image);
                    mylist.add(song);
                }
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
            songLength = media.getDuration();
            media.start();
            progressBar.setVisibility(View.INVISIBLE);
            play.setVisibility(View.VISIBLE);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        prefOpen = sharedPref.getBoolean(getString(R.string.pref_openings), true);
        prefEnd = sharedPref.getBoolean(getString(R.string.pref_endings), true);
        prefOsts = sharedPref.getBoolean(getString(R.string.pref_osts), true);
        menu.getItem(1).getSubMenu().getItem(0).setChecked(prefOpen);
        menu.getItem(1).getSubMenu().getItem(1).setChecked(prefEnd);
        menu.getItem(1).getSubMenu().getItem(2).setChecked(prefOsts);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.github) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DipanshKhandelwal/Ongaku-Android"));
            startActivity(browserIntent);
            return true;
        }else{
            if(id == R.id.openings) {
                prefEditor = sharedPref.edit();
                Toast.makeText(this, "openings", Toast.LENGTH_SHORT).show();
                if(item.isChecked()){
                    item.setChecked(false);
                    prefEditor.putBoolean(getString(R.string.pref_openings), false);
                }else{
                    item.setChecked(true);
                    prefEditor.putBoolean(getString(R.string.pref_openings), true);
                }
                prefEditor.commit();
            }else {
                if (id == R.id.endings) {
                    prefEditor = sharedPref.edit();
                    Toast.makeText(this, "endings", Toast.LENGTH_SHORT).show();
                    if (item.isChecked()) {
                        item.setChecked(false);
                        prefEditor.putBoolean(getString(R.string.pref_endings), false);
                    } else {
                        item.setChecked(true);
                        prefEditor.putBoolean(getString(R.string.pref_endings), true);
                    }
                    prefEditor.commit();
                } else if (id == R.id.osts) {
                    prefEditor = sharedPref.edit();
                    Toast.makeText(this, "osts", Toast.LENGTH_SHORT).show();
                    if (item.isChecked()) {
                        item.setChecked(false);
                        prefEditor.putBoolean(getString(R.string.pref_osts), false);
                    } else {
                        item.setChecked(true);
                        prefEditor.putBoolean(getString(R.string.pref_osts), true);
                    }
                    prefEditor.commit();
                }

                sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);

                prefOpen = sharedPref.getBoolean(getString(R.string.pref_openings), true);
                prefEnd = sharedPref.getBoolean(getString(R.string.pref_endings), true);
                prefOsts = sharedPref.getBoolean(getString(R.string.pref_osts), true);

                list = null;
                list = extractData(ReadFromfile("data.json", MainActivity.this), prefOpen, prefEnd, prefOsts);
                adapter = null;
                adapter = new SongAdapter(MainActivity.this ,R.layout.list_item ,list);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                urltext = null;
                songPosition = 0;
            }
                return super.onOptionsItemSelected(item);
            }

        }

    @Override
    protected void onPause() {
        media.pause();
        play.setImageResource(android.R.drawable.ic_media_play);
        super.onPause();
    }

    @Override
    protected void onStop() {
        media.release();
        super.onStop();
    }
}
