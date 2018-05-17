package com.example.a2061010.ssui_mmp;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class Reprodutor extends AppCompatActivity  implements View.OnClickListener{

    static MediaPlayer mp;
    ArrayList<File> cancoes;
    int posicao;
    Uri uri;
    String aux = "";

    Thread atualizarSeekBar;

    Button btnff, btnfb, btnPv, btnNext, btnPlay, btnPlaylist;
    TextView nome,duracaoCancao, continua;
    SeekBar sb,sk_volume;
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reprodutor);

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

                int duracao = mp.getDuration();
                sb.setMax(duracao);

                int posicaoAtual = 0;
                int execucao = 0;

                boolean ban=false;

                while (posicaoAtual < duracao)
                {
                    try{
                        sleep(500);
                        posicaoAtual = mp.getCurrentPosition();
                        sb.setProgress(posicaoAtual);
                        execucao = sb.getProgress();
                        aux = getHRM(execucao);
                        continua.setText(aux.toString().trim());

                    }catch (Exception e)
                    {
                        continua.setText(aux);
                    }

                }
            }
        };

        if(mp!= null)
        {
            mp.stop();
        }
        try
        {
            Intent i = getIntent();
            Bundle b = i.getExtras();
            cancoes = (ArrayList) b.getParcelableArrayList("cancoes");
            posicao = (int) b.getInt("pos",0);
            uri = Uri.parse(cancoes.get(posicao).toString());
            nome.setText(cancoes.get(posicao).getName().toString());
            mp = MediaPlayer.create(getApplication(),uri);
            atualizarSeekBar.start();
            mp.start();
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
        mp.stop();

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
        mp.stop();
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
        sb.setMax(mp.getDuration());
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

}
