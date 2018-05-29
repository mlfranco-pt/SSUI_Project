package com.example.a2061010.ssui_mmp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.util.ArrayList;

public class Reprodutor extends AppCompatActivity  implements View.OnClickListener{
    static MediaPlayer mp;      //mediaplayer
    ArrayList<File> cancoes;
    int posicao,posicaoAtualPref;
    Uri uri;
    String aux = "";
    private final UIHandler _handler = new UIHandler();     //handlers para mandar msgs
    private final UIHandler _handler2 = new UIHandler();
    ImageButton btnff,btnPv, btnfb, btnNext, btnPlay;
    ImageButton btnPlaylist;
    TextView nome,duracaoCancao, continua;
    SeekBar sb,sk_volume;
    AudioManager audioManager;
    SensorManager sensorManager;
    Sensor proxSensor, gyroscopeSensor, accelerometerSensor, geoMagneticSensor;
    SensorEventListener SensorListener;
    ImageView imgvinil;

    float angle = 0;                                    //angulo do vinil
    private static final String MODULE = "Reprodutor";  //para mandar logs
    boolean ban=false;      //para definir se a thread acabou
    int posicaoAtual = 0;   //
    int duracao = 0;
    private final float[] accelerometer = new float[3];
    private final float[] magnetic = new float[3];
    private float[] dadosAcelerometro = new float[20];
    int ia = 0;
    int timeAcelerometro1 = 0;
    int timeAcelerometro2 = 0;
    float historiaAcelerometro;
    double[] anguloAcelerometro = new double[2];
    int timeverifica = 1;
    float[] teste1 = new float[5];
    double[] teste2 = new double[5];
    boolean trueVerificacaoSeguinte = true;
    boolean trueVerificacaoAnterior = true;
    int tempoDesativado = 0;
    Thread atualizarSeekBar = new Thread(){
        @Override
        public void run() {
            imgvinil = (ImageView)findViewById(R.id.imgvinil);
            int duracaoThread = duracao;
            posicaoAtual = 0;
            ban = false;
            int div = 0;
            try{div = 360/(duracaoThread/1000);}catch(Exception e){e.printStackTrace();}
            if(div == 0) div = 1;
            Message msg3 = new Message();
            msg3.arg1 = 3;
            _handler.sendMessage(msg3);
            while (posicaoAtual < (duracaoThread - 60) && !ban) {
                try {
                    sleep(100);
                    posicaoAtual = mp.getCurrentPosition();
                    sb.setProgress(posicaoAtual);
                    Message msg2 = new Message();
                    msg2.arg1 = 2;
                    msg2.arg2 = div;
                    _handler2.sendMessage(msg2);
                   // imgvinil.setRotation((float) ++angle);
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
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //geoMagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

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
            posicao = (int) b.getInt("pos",0);
            uri = Uri.parse(cancoes.get(posicao).toString());
            nome.setText(posicao+" "+cancoes.get(posicao).getName().toString());
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
            }
            if(msg.arg1 == 2)
            {
                int execucao;
                execucao = sb.getProgress();
                aux = getHRM(execucao);
                continua.setText(aux.toString().trim());
                angle += msg.arg2;
                imgvinil.setRotation((float) angle);
            }
            if(msg.arg1 == 3)
            {
                imgvinil.setRotation((float) 45.0);
            }
        }
    }
    private String getHRM(int miliseconds){ //retorna uma entrada de milissegundos num texto dividido em minutos,segundos,...
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
                atualizarSeekBar.interrupt();
                NextCancao();
                break;
            case R.id.btnPv:
                ban = true;
                atualizarSeekBar.interrupt();
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
        nome.setText(posicao+" "+cancoes.get(posicao).getName().toString());
        uri = Uri.parse(cancoes.get(posicao).toString());
        mp = MediaPlayer.create(getApplicationContext(),uri);
        mp.start();
        sb.setMax(0);
        duracaoCancao.setText(getHRM(mp.getDuration()));
        try{sb.setMax(mp.getDuration());}catch (Exception e){}
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
        nome.setText(posicao+" "+cancoes.get(posicao).getName().toString());
        uri = Uri.parse(cancoes.get(posicao).toString());
        mp = MediaPlayer.create(getApplicationContext(),uri);
        mp.start();
        sb.setMax(0);
        duracaoCancao.setText(getHRM(mp.getDuration()));
        try{sb.setMax(mp.getDuration());}catch (Exception e){e.printStackTrace();}
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
                if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE)
                {
                    //Log.i(MODULE,"X: "+sensorEvent.values[0]);
                    //Log.i(MODULE,"Y: "+sensorEvent.values[1]);
                    //Log.i(MODULE,"Z: "+sensorEvent.values[2]);
                }
                if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                {
                    //System.arraycopy(sensorEvent.values,0,accelerometer,0,accelerometer.length);
                    //Log.i(MODULE,"ZZZZZZZZZ: "+sensorEvent.values[2]);

                    dadosAcelerometro[ia] = sensorEvent.values[2];
                    ia++;
                    if(ia >= 7)
                    {
                        boolean checkFaceDown = true;
                        for (int i=0;i<7;i++)
                        {
                            if(dadosAcelerometro[i] < -10.4 || dadosAcelerometro[i] > -8.7)
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
                    //if(sensorEvent.values[0] < -25 ||sensorEvent.values[0] > 25)
                    //Log.i(MODULE,"acele: "+sensorEvent.values[0]);
                    //Log.i(MODULE,"angulo:   "+ Math.atan2(sensorEvent.values[0], sensorEvent.values[1])/(Math.PI/180));
                    if(tempoDesativado > 10) {
                        teste1[timeverifica] = sensorEvent.values[0];
                        teste2[timeverifica] = Math.atan2(teste1[timeverifica], sensorEvent.values[1]) / (Math.PI / 180);
                        Log.i(MODULE, "aceleracao     " + teste1[timeverifica]);
                        //verificacao dos angulos
                        if (teste2[timeverifica] > teste2[timeverifica - 1] || teste2[timeverifica] + 10 > teste2[timeverifica - 1] || teste2[timeverifica] > 0) //angulo
                        {
                            trueVerificacaoSeguinte = false;
                        }
                        if(teste2[timeverifica] < teste2[timeverifica - 1] || teste2[timeverifica] - 10 < teste2[timeverifica - 1] || teste2[timeverifica] < 0)
                        {
                            trueVerificacaoAnterior = false;
                        }
                        //verificacao da aceleracao
                        if(teste1[timeverifica] < teste1[timeverifica-1] || teste1[timeverifica]<0 || teste1[timeverifica]-1<teste1[timeverifica-1])
                            trueVerificacaoAnterior = false;
                        if(teste1[timeverifica] > teste1[timeverifica-1] || teste1[timeverifica]>0 || teste1[timeverifica]+1>teste1[timeverifica-1])
                            trueVerificacaoSeguinte = false;
                        timeverifica++;
                        if (timeverifica == 3) {
                            if (trueVerificacaoSeguinte) {
                                ban = true;
                                NextCancao();
                                Toast.makeText(Reprodutor.this, "SEGUINTE", Toast.LENGTH_SHORT).show();
                                tempoDesativado = 0;
                            }
                            if (trueVerificacaoAnterior) {
                                ban = true;
                                PrevCancao();
                                Toast.makeText(Reprodutor.this, "ANTERIOR", Toast.LENGTH_SHORT).show();
                                tempoDesativado = 0;
                            }
                            timeverifica = 1;
                            trueVerificacaoSeguinte = true;
                            trueVerificacaoAnterior = true;
                            teste1[0] = sensorEvent.values[0];
                            teste2[0] = Math.atan2(teste1[0], sensorEvent.values[1]) / (Math.PI / 180);
                        /*double anteriorAngulo = teste2[0];
                        float anteriorAce = teste1[0];
                        boolean trueVerificacaoSeguinte = true;
                     //   Log.i(MODULE,"angulo[0]: "+teste2[0]);
                       // Log.i(MODULE,"forca[0]: "+teste1[0]);
                      /*  for (int i = 1; i<4;i++)
                        {
                            if(teste1[i] > teste1[i-1]) //aceleracao
                            {
                                trueVerificacaoSeguinte = false;
                            }
                            if(teste2[i]>teste2[i-1]) //angulo
                            {
                                trueVerificacaoSeguinte = false;
                            }
                            //Log.i(MODULE,"angulo["+i+"]: "+teste2[i]);
                            Log.i(MODULE,"forca["+i+"]: "+teste1[i]);
                        }*/
                       /* if(!trueVerificacaoSeguinte)
                        {
                            ban = true;
                            NextCancao();
                            Toast.makeText(Reprodutor.this, "SEGUINTE", Toast.LENGTH_SHORT).show();
                        }
                        timeverifica = 0;*/
                        }
                    }
                    tempoDesativado++;
                            /*
                    if(timeverifica > 20) {
                        if (timeAcelerometro1 == 1) {
                            historiaAcelerometro = sensorEvent.values[0];
                            anguloAcelerometro[0] = Math.atan2(historiaAcelerometro, sensorEvent.values[1])/(Math.PI/180);
                        }
                        if (timeAcelerometro2 >= 2) {
                            float agora = sensorEvent.values[0];
                            float dif = historiaAcelerometro - agora;
                            anguloAcelerometro[1] = Math.atan2(agora, sensorEvent.values[1])/(Math.PI/180);
                            if(anguloAcelerometro[1] < 0 && anguloAcelerometro[0] < 15) //&& anguloAcelerometro[1] < anguloAcelerometro[0])
                                Log.i(MODULE, "SEGUINTE angulo0:  "+anguloAcelerometro[0]+"   angulo1:  "+anguloAcelerometro[1]+"       dif: " + dif);
                           // if(anguloAcelerometro[1] > 15 && anguloAcelerometro[0] > 15 && anguloAcelerometro[1] > anguloAcelerometro[0])
                             //   Log.i(MODULE, "ANTERIOR angulo0:  "+anguloAcelerometro[0]+"   angulo1:  "+anguloAcelerometro[1]+"       dif: " + dif);
                            if (dif > 10 && anguloAcelerometro[1] < 0 && anguloAcelerometro[0] < 15){ //&& anguloAcelerometro[1] < anguloAcelerometro[0]) {
                                ban = true;
                                NextCancao();
                                timeverifica = 0;
                                Log.i(MODULE, "SEGUINTE angulo0:  "+anguloAcelerometro[0]+"   angulo1:  "+anguloAcelerometro[1]+"       dif: " + dif);
                                Toast.makeText(Reprodutor.this, "SEGUINTE", Toast.LENGTH_SHORT).show();
                            }
                            if (dif < -10 && anguloAcelerometro[1] > 0 && anguloAcelerometro[0] > -15){// && anguloAcelerometro[1] > anguloAcelerometro[0]) {
                                ban = true;
                                PrevCancao();
                                timeverifica = 0;
                                Log.i(MODULE, "ANTERIOR angulo0:  "+anguloAcelerometro[0]+"   angulo1:  "+anguloAcelerometro[1]+"       dif: " + dif);
                                Toast.makeText(Reprodutor.this, "ANTERIOR", Toast.LENGTH_SHORT).show();

                               // Log.i(MODULE, "antes: " + historiaAcelerometro + "                 depois: " + sensorEvent.values[0] + "       dif: " + dif);
                            }
                            timeAcelerometro1 = 0;
                            timeAcelerometro2 = 0;
                        }
                        timeAcelerometro1++;
                        timeAcelerometro2++;
                    }
                    timeverifica++;*/
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

// Register it, specifying the polling interval in microseconds
        sensorManager.registerListener(SensorListener,
                proxSensor, 500);
        //sensorManager.registerListener(SensorListener,gyroscopeSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(SensorListener,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        atualizarSeekBar.interrupt();
        if(mp.isPlaying())mp.pause();
        super.onPause();
        sensorManager.unregisterListener(SensorListener); //desativa os sensores quando a atividade entre em onPause()
    }

    @Override
    public void onStop(){
        ban = true;
        atualizarSeekBar.interrupt();
        if(mp.isPlaying())mp.pause();
        super.onStop();
    }

    @Override
    public void onDestroy(){
        ban = true;
        atualizarSeekBar.interrupt();
        mp.stop();
        super.onDestroy();
    }
}
