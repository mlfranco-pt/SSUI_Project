package com.example.a2061010.ssui_mmp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    Thread atualizarSeekBar;
    Button btnff, btnfb, btnPv, btnNext, btnPlay, btnPlaylist;
    TextView nome,duracaoCancao, continua;
    SeekBar sb,sk_volume;
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reprodutor);
        /*sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(Duracao)) {
            posicaoAtualPref = sharedpreferences.getInt(Duracao, 0);
        }*/

        btnPlay= (Button) findViewById(R.id.btnPlay);
        btnfb= (Button) findViewById(R.id.btnfb);
        btnff = (Button) findViewById(R.id.btnff);
        btnPv = (Button) findViewById(R.id.btnPv);
        btnNext= (Button) findViewById(R.id.btnNext);
        btnPlaylist= (Button) findViewById(R.id.btn_playlist);

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

        atualizarSeekBar = new Thread(){
            @Override
            public void run(){
                boolean ban=false;


                while(ban == false) {
                    while(mp == null)
                    {}
                    int duracao = mp.getDuration();
                    sb.setMax(duracao);

                    int posicaoAtual = 0;
                    //int execucao = 0;
                    while (posicaoAtual < duracao-125) {

                        try {
                            sleep(500);
                            posicaoAtual = mp.getCurrentPosition();
                            sb.setProgress(posicaoAtual);
                            //execucao = sb.getProgress();
                            //aux = getHRM(execucao);
                            Message msg2 = new Message();
                            msg2.arg1 = 2;
                            _handler2.sendMessage(msg2);
                            //continua.setText(aux.toString().trim());
                        } catch (Exception e) {
                            e.printStackTrace();
                            posicaoAtual = duracao;
                        }

                    }
                    Message msg1 = new Message();
                    msg1.arg1 = 1;
                    _handler.sendMessage(msg1);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

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
            atualizarSeekBar.start();
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
                    btnPlay.setText("play");
                    mp.pause();
                }else{
                    btnPlay.setText("pause");
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
    public void onDestroy(){
       /* SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(Duracao,mp.getCurrentPosition());
        editor.putInt(Musica,posicao);
        editor.apply();*/
        super.onDestroy();

    }

}
