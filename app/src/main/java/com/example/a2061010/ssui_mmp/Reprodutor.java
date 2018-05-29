package com.example.a2061010.ssui_mmp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
    Sensor proxSensor, gyroscopeSensor, accelerometerSensor, geoMagneticSensor;
    SensorEventListener SensorListener;
    private static final String MODULE = "Reprodutor";
    boolean ban=false;
    int posicaoAtual = 0;
    int duracao = 0;
    private final float[] accelerometer = new float[3];
    private final float[] magnetic = new float[3];
    private float[] dadosAcelerometro = new float[20];
    int ia = 0;
    Thread faceUpDown = new Thread(){
        @Override
        public void run(){ //verifica se num certo intervalo de tempo o telemovel esteve sempre virado para baixo
          while(true)
          {
             try {
                 final float[] rotationMatrix = new float[9];
                 sensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, magnetic);
                 int inclination = (int) Math.round(Math.toDegrees(Math.acos(rotationMatrix[8])));
                 //Log.i(MODULE, "inclinacao:   " + inclination);
                 if (inclination < 160) {
                    // if (!mp.isPlaying()) mp.start();
                    // Log.i(MODULE, "PARA CIMA");
                 } else {
                     //if(mp.isPlaying())mp.pause();
                     //Log.i(MODULE, "PARA BAIXO");
                 }
             }catch(Exception e)
             {
                 e.printStackTrace();
             }
          }

            /*  boolean t = true;
            int iteracoes = 0;
            int[] amostras = new int[20];
            boolean has = false;
            try {
                while (t) {
                    sleep(25);
                    if (iteracoes >= 19) {
                        for (int i=0;i<iteracoes;i++) {
                               if(amostras[i] <= 150)
                                   has = true;
                        }
                        if(!has)
                        {
                            mp.pause();
                        }
                        else
                        {
                            if(!mp.isPlaying())mp.start();
                        }
                        has = false;
                        iteracoes = 0;
                    } else {
                        final float[] rotationMatrix = new float[9];
                        sensorManager.getRotationMatrix(rotationMatrix, null, accelerometer, magnetic);
                        int inclination = (int) Math.round(Math.toDegrees(Math.acos(rotationMatrix[8])));
                        amostras[iteracoes] = inclination;
                        Log.i(MODULE,"inclinacao:   "+amostras[iteracoes]);
                        iteracoes++;

                        /*if (inclination < 90) {
                            if (!mp.isPlaying()) mp.start();
                            Log.i(MODULE, "PARA CIMA");
                        } else {
                            mp.pause();
                            Log.i(MODULE, "PARA BAIXO");
                        }
                        final float[] orientationAngles = new float[3];
                        sensorManager.getOrientation(rotationMatrix, orientationAngles);
                        Log.i(MODULE, "----------------------------");
                        Log.i(MODULE, "valor0  " + orientationAngles[0]);
                        Log.i(MODULE, "valor1  " + orientationAngles[1]);
                        Log.i(MODULE, "valor2  " + orientationAngles[2]);
                        Log.i(MODULE, "----------------------------");

                    }
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }*/
        }

    };

    Thread atualizarSeekBar = new Thread(){
        @Override
        public void run() {
            int duracaoThread = duracao;
            posicaoAtual = 0;
            ban = false;
            while (posicaoAtual < (duracaoThread - 60) && !ban) {
                try {
                    sleep(100);
                    posicaoAtual = mp.getCurrentPosition();
                    sb.setProgress(posicaoAtual);
                    Message msg2 = new Message();
                    msg2.arg1 = 2;
                    _handler2.sendMessage(msg2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (posicaoAtual > (duracaoThread-100)) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg1 = new Message();//next cancao
                msg1.arg1 = 1;
                _handler.sendMessage(msg1);

            }
            Log.i(MODULE, "ACABOU a thread: -------->>>>>>>>>>" + atualizarSeekBar.getId());
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
        faceUpDown.start();
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        geoMagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

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
        ban = true;
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
                ban = true;
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
        duracao = mp.getDuration();
        sb.setMax(duracao);
        atualizarSeekBar.start();
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
                /* if(sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    if(sensorEvent.values[0] <= 2)
                    {
                        if(mp.isPlaying()) {
                            btnPlay.setImageResource(R.drawable.play);
                            if(mp.isPlaying()) mp.pause();
                        }
                    }
                    if(sensorEvent.values[0] > 2)
                    {
                        if(mp != null) {
                            btnPlay.setImageResource(R.drawable.pause);
                           if(!mp.isPlaying()) mp.start();
                        }
                    }
                    Log.i(MODULE,"prox: "+sensorEvent.values[0]);
                }*/
                if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE)
                {
                    //Log.i(MODULE,"X: "+sensorEvent.values[0]);
                    //Log.i(MODULE,"Y: "+sensorEvent.values[1]);
                    //Log.i(MODULE,"Z: "+sensorEvent.values[2]);
                }
                if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                {
                    System.arraycopy(sensorEvent.values,0,accelerometer,0,accelerometer.length);
                    Log.i(MODULE, "acelerometro: "+sensorEvent.values[2]);
                    dadosAcelerometro[ia] = sensorEvent.values[2];
                    ia++;
                    if(ia >= 7)
                    {
                        boolean checkFaceDown = true;
                        for (int i=0;i<7;i++)
                        {
                            if(dadosAcelerometro[i] < -10.3 || dadosAcelerometro[i] > -9)
                                checkFaceDown = false;
                        }
                            ia = 0;
                        if(checkFaceDown)
                        {
                            if(mp.isPlaying()) mp.pause();
                        }
                        else
                        {
                            if(!mp.isPlaying()) mp.start();
                        }
                    }

                }
                if(sensorEvent.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
                {
                    System.arraycopy(sensorEvent.values,0,magnetic,0,magnetic.length);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

// Register it, specifying the polling interval in
// microseconds
        sensorManager.registerListener(SensorListener,
                proxSensor, 500);
        sensorManager.registerListener(SensorListener,gyroscopeSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(SensorListener,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(SensorListener,geoMagneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
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
        ban = true;
        mp.stop();
            }

}
