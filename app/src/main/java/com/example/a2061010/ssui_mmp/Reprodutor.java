package com.example.a2061010.ssui_mmp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.util.ArrayList;

public class Reprodutor extends AppCompatActivity  implements View.OnClickListener{
    /*SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    public static final String Duracao = "duracaoAtual";
    public static final String Musica = "musica";*/
    static MediaPlayer mp;
    ArrayList<File> cancoes;
    int posicao,posicaoAtualPref;
    Uri uri;
    String aux = "";
    private final UIHandler _handler = new UIHandler();
    private final UIHandler _handler2 = new UIHandler();
    ImageButton btnff,btnPv, btnfb, btnNext, btnPlay;
    ImageButton btnPlaylist;
    TextView nome,duracaoCancao, continua;
    SeekBar sb,sk_volume;
    AudioManager audioManager;
    SensorManager sensorManager;
    Sensor proxSensor, gyroscopeSensor;
    SensorEventListener SensorListener;
    private static final String MODULE = "Reprodutor";
    boolean ban=false;
    int posicaoAtual = 0;
    int duracao = 0;
    Thread atualizarSeekBar = new Thread(){
        @Override
        public void run() {
                //Log.i(MODULE,"--------------------------duracao----------------------------: "+duracao);
                posicaoAtual = 0;
                //int execucao = 0;
                ban = false;
                while (posicaoAtual < (duracao - 60) && ban != true){
                    try {
                        sleep(100);
                        posicaoAtual = mp.getCurrentPosition();
                        //      Log.i(MODULE,"????????????????posicaoAtual: "+posicaoAtual);
                        sb.setProgress(posicaoAtual);
                        //execucao = sb.getProgress();
                        //aux = getHRM(execucao);
                        Message msg2 = new Message();
                        msg2.arg1 = 2;
                        _handler2.sendMessage(msg2);
                        //continua.setText(aux.toString().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                        //posicaoAtual = duracao;
                    }

                }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
                Log.i(MODULE,"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<atual: "+posicaoAtual);
                Message msg1 = new Message();//next cancao
                msg1.arg1 = 1;
                _handler.sendMessage(msg1);
            }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reprodutor);
        /*sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(Duracao)) {
            posicaoAtualPref = sharedpreferences.getInt(Duracao, 0);
        }*/

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        btnPlay= (ImageButton) findViewById(R.id.btnPlay);
        btnfb= (ImageButton) findViewById(R.id.btnfb);
        btnff = (ImageButton) findViewById(R.id.btnff);
        btnPv = (ImageButton) findViewById(R.id.btnPv);
        btnNext= (ImageButton) findViewById(R.id.btnNext);
        btnPlaylist= (ImageButton) findViewById(R.id.btn_playlist);

        nome = (TextView) findViewById(R.id.nome);
        duracaoCancao = (TextView) findViewById(R.id.tempo2);
        continua = (TextView) findViewById(R.id.tempo);

        btnPlay.setOnClickListener(this);
        btnfb.setOnClickListener(this);
        btnff.setOnClickListener(this);
        btnPv.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPlaylist.setOnClickListener(this);

        sb = (SeekBar) findViewById(R.id.sb);

        /*atualizarSeekBar = new Thread(){
            @Override
            public void run(){
                boolean ban=false;


                while(ban == false) {
                    while(mp == null)
                    {}
                    int duracao = 0;
                            duracao = mp.getDuration();
                        sb.setMax(duracao);

                    duracao -= 60;
                    Log.i(MODULE,"--------------------------duracao----------------------------: "+duracao);
                    int posicaoAtual = 0;
                    //int execucao = 0;
                    while (posicaoAtual < (duracao-60)) {

                        try {
                            sleep(100);
                            posicaoAtual = mp.getCurrentPosition();
                      //      Log.i(MODULE,"????????????????posicaoAtual: "+posicaoAtual);
                            sb.setProgress(posicaoAtual);
                            //execucao = sb.getProgress();
                            //aux = getHRM(execucao);
                            Message msg2 = new Message();
                            msg2.arg1 = 2;
                            _handler2.sendMessage(msg2);
                            //continua.setText(aux.toString().trim());
                        } catch (Exception e) {
                            e.printStackTrace();
                            //posicaoAtual = duracao;
                        }

                    }
                    Log.i(MODULE,"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<atual: "+posicaoAtual);
                    Message msg1 = new Message();//next cancao
                    msg1.arg1 = 1;
                    _handler.sendMessage(msg1);
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };*/

        if(mp!= null)
        {
            if(mp.isPlaying())
            mp.stop();
        }
        try
        {
            Intent i = getIntent();
            Bundle b = i.getExtras();
            cancoes = (ArrayList) b.getParcelableArrayList("cancoes");
            //if(!sharedpreferences.contains(Musica))
                posicao = (int) b.getInt("pos",0);
           /* else
                posicao = sharedpreferences.getInt(Musica,0);*/
            uri = Uri.parse(cancoes.get(posicao).toString());
            nome.setText(cancoes.get(posicao).getName().toString());
            mp = MediaPlayer.create(getApplication(),uri);
            duracao = mp.getDuration();
            atualizarSeekBar.start();
            sb.setMax(duracao);
            mp.start();
            mp.seekTo(posicaoAtualPref);
            Volume();
            duracaoCancao.setText(getHRM(mp.getDuration()));
        }catch(Exception e)
        {

        }

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }

        });
    }

    public class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            if(msg.arg1 == 1)
            {
                NextCancao();
                //atualizarSeekBar.start();
            }
            if(msg.arg1 == 2)
            {
                int execucao;
                execucao = sb.getProgress();
                aux = getHRM(execucao);
                continua.setText(aux.toString().trim());
            }
        }
    }
    private String getHRM(int miliseconds){
        int seconds = (int) (miliseconds/1000) % 60;
        int minutes = (int) ((miliseconds/ (1000*60)) % 60);
        int hours = (int) ((miliseconds/(1000*60*60)) %24);
        String aux="";
        aux = ((hours<10)?"0"+hours:hours)+ ":" + ((minutes<10)?"0"+minutes:minutes)+":"+((seconds<10)?"0"+seconds:seconds);
        return aux;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btnPlay:
                if(mp.isPlaying()){
                    btnPlay.setImageResource(R.drawable.play);
                    mp.pause();
                }else if(mp != null){
                    btnPlay.setImageResource(R.drawable.pause);
                    mp.start();
                }
                break;
            case R.id.btnff:
                mp.seekTo(mp.getCurrentPosition()+5000);
                break;
            case R.id.btnfb:
                mp.seekTo(mp.getCurrentPosition() - 5000);
                break;
            case R.id.btnNext:
                ban = true;
                NextCancao();
                break;
            case R.id.btnPv:
                PrevCancao();
                break;
            case R.id.btn_playlist:
                startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("pos",posicao).putExtra("cancaos",cancoes));
                break;
        }
    }

    public void NextCancao(){
        if(mp.isPlaying())
        mp.stop();

        mp.release();
        mp = null;
        posicao = (posicao +1) % cancoes.size();
        nome.setText(cancoes.get(posicao).getName().toString());

        uri = Uri.parse(cancoes.get(posicao).toString());
        mp = MediaPlayer.create(getApplicationContext(),uri);
        mp.start();


        sb.setMax(0);
        duracaoCancao.setText(getHRM(mp.getDuration()));
        try{
            sb.setMax(mp.getDuration());
        }catch (Exception e){

        }
        duracao = mp.getDuration();
        sb.setMax(duracao);
        atualizarSeekBar.start();
    }



    public void PrevCancao(){
        if(mp.isPlaying())
        mp.stop();
        mp.release();
        mp = null;
        if(posicao-1<0){
            posicao = cancoes.size()-1;
        } else{
            posicao = posicao-1;
        }
        nome.setText(cancoes.get(posicao).getName().toString());
        uri = Uri.parse(cancoes.get(posicao).toString());
        mp = MediaPlayer.create(getApplicationContext(),uri);


            mp.start();

        sb.setMax(0);
        duracaoCancao.setText(getHRM(mp.getDuration()));
        try{
            sb.setMax(mp.getDuration());
        }catch (Exception e){

        }
    }

    public void Volume(){
        try{
            sk_volume = (SeekBar) findViewById(R.id.sbAudio);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            sk_volume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            sk_volume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

            sk_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress,0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        // Create listener
            SensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                /*if(sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    if(sensorEvent.values[0] <= 2)
                    {
                        if(mp.isPlaying()) {
                            btnPlay.setImageResource(R.drawable.play);
                            mp.pause();
                        }
                    }
                    if(sensorEvent.values[0] > 2)
                    {
                        if(mp != null) {
                            btnPlay.setImageResource(R.drawable.pause);
                            mp.start();
                        }
                    }
                }*/
                if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE)
                {

                    //Log.i(MODULE,"X: "+sensorEvent.values[0]);
                    //Log.i(MODULE,"Y: "+sensorEvent.values[1]);
                    //Log.i(MODULE,"Z: "+sensorEvent.values[2]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

// Register it, specifying the polling interval in
// microseconds
        /*sensorManager.registerListener(SensorListener,
                proxSensor, 500);*/
        sensorManager.registerListener(SensorListener,gyroscopeSensor,SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onPause(){
        super.onPause();
        //sensorManager.unregisterListener(proximitySensorListener);
    }

    @Override
    public void onStop(){
        super.onStop();

    }


    @Override
    public void onDestroy(){
       /* SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(Duracao,mp.getCurrentPosition());
        editor.putInt(Musica,posicao);
        editor.apply();*/
        super.onDestroy();
    }

}
