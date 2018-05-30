package com.example.a2061010.ssui_mmp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.MonthDisplayHelper;
import android.view.Gravity;
import android.view.MotionEvent;
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
    Uri uri;                //uri do ficheiro
    String aux = "";        //para textviews
    private final UIHandler _handler = new UIHandler();     //handlers para mandar msgs
    ImageButton btnff,btnPv, btnfb, btnNext, btnPlay,btnPlaylist;
    TextView nome,duracaoCancao, continua;
    SeekBar sb,sk_volume;       //seekbars de volume e music
    AudioManager audioManager;  //para o volume
    SensorManager sensorManager;    //para os sensores
    Sensor accelerometerSensor;
    SensorEventListener SensorListener;     //para captar eventos dos sensores
    ImageView imgvinil;
    float vinilScaleY,vinilScaleX,angle = 0;
    private boolean houveActionUp = false;  //para verificar se estamos a carregar na imagem
    private boolean detectSensors = false;  //para ativar ou desativar os sensores
    private long timeOnActionDown;      //para efetuar as contas de contar quanto tempo estivemos a pressionar a imagem que serve para ativar ou desativar os sensores
    private final int duracaoLongClick = 1500;     //angulo do vinil
    private static final String MODULE = "Reprodutor";  //para mandar logs
    boolean ban=false;      //para definir se a thread acabou
    int posicaoAtual = 0,duracao = 0, ia = 0,timeverifica = 1,tempoDesativado = 0;  // posicaoAtual = posicao atual da musica ---- duracao = duracao total da musica ---- ia = iterações para verificar se o telefone esta virado para baixo ----- timeverifica = para verificar movimentos de next e previous --- tempoDesativado = para contar o numero de iteraçoes que fica sem obter dados do acelerometro;
    private float[] dadosAcelerometro = new float[7]; //para guardar dados do acelerometro no eixo Z, de forma a depois verificar se está faceup ou facedown
    float[] teste1 = new float[5];      //dados do acelerometro
    double[] teste2 = new double[5];    //angulo
    boolean trueVerificacaoSeguinte = true,trueVerificacaoAnterior = true;  //para ver se devemos passar para a musica seguinte ou anterior

    Thread atualizarSeekBar = new Thread(){
        @Override
        public void run() {
            int duracaoThread = duracao; //duracao da musica atual
            posicaoAtual = 0;       //inicio na posicao 0
            ban = false;            //ban serve para sair do ciclo while caso a musica acabe antes de chegar ao fim (devido a haver haver a acao de nextCancao ou prevCancao)
            int div = 0;            //div serve para rodar o vinil conforme o tamanho da musica
            try{div = 360/(duracaoThread/1000);}catch(Exception e){e.printStackTrace();}
            if(div == 0) div = 1;
            Message msg3 = new Message();
            msg3.arg1 = 3;
            _handler.sendMessage(msg3);
            while (posicaoAtual < (duracaoThread - 60) && !ban) {
                try {
                    sleep(100);
                    posicaoAtual = mp.getCurrentPosition();     //busca a posicao da musica. é retornado um valor em milisegundos
                    sb.setProgress(posicaoAtual);//atualizar posicao da seekbar
                    Message msg2 = new Message();
                    msg2.arg1 = 2;                  //update sb
                    msg2.arg2 = div;        //em div vai o valor que devemos adicionar à rotacao da imagem
                    _handler.sendMessage(msg2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (posicaoAtual > (duracaoThread-100)) { //caso a musica tenha chegado ao fim sera mandada uma mensagem à thread principal para mudar de musica
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
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        imgvinil = (ImageView)findViewById(R.id.imgvinil);
        vinilScaleY = imgvinil.getScaleY();     //escala base da imagem em Y
        vinilScaleX = imgvinil.getScaleX();     //escala base da imagem em X
        imgvinil.setOnTouchListener(new View.OnTouchListener() {//detecao de acoes na imagem
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){   //quando tocamos pela primeira vez na imagem guardamos o momento
                    timeOnActionDown = (long) System.currentTimeMillis();
                    houveActionUp = false;

                }
                if(event.getAction() == MotionEvent.ACTION_UP){ //quando tiramos o dedo da imagem, repomos o seu tamanho original
                    imgvinil.setScaleX(vinilScaleX);
                    imgvinil.setScaleY(vinilScaleY);
                }
                if(!houveActionUp) {    //nao entra neste if enquanto que nao começarmos a pressionar o botao
                    imgvinil.setScaleX(imgvinil.getScaleX()+(float)0.01);       //vai aumentando o tamanho da imagem para fornecer algum tipo de feedback
                    imgvinil.setScaleY(imgvinil.getScaleY()+(float)0.01);
                    if ((System.currentTimeMillis() - timeOnActionDown) > duracaoLongClick) {   //verifica se carregamos o tempo suficiente que está definido em duracaoLongClick
                        if (!detectSensors) {
                            Toast toast = Toast.makeText(Reprodutor.this, "Interface não tradicional ativada", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,50);
                            toast.show();       //envia uma mensagem temporaria para o ecra a informar que ativamos a interface nao tradicional
                            detectSensors = true;
                            sensorManager.registerListener(SensorListener,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);//sensores ativados
                            btnPlay.setVisibility(View.INVISIBLE);//retiramos do ecra alguns botoes convencionais
                            btnNext.setVisibility(View.INVISIBLE);
                            btnPv.setVisibility(View.INVISIBLE);
                        } else {
                            Toast toast = Toast.makeText(Reprodutor.this, "Interface nao tradicional desativada", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,50);
                            toast.show();   //envia uma mensagem temporaria para o ecra a informar que desativamos a interface nao tradicional
                            detectSensors = false;
                            sensorManager.unregisterListener(SensorListener);
                            btnPlay.setVisibility(View.VISIBLE);//volta a por os botoes convencionais no layout
                            btnNext.setVisibility(View.VISIBLE);
                            btnPv.setVisibility(View.VISIBLE);
                        }
                        imgvinil.setScaleX(vinilScaleX);//quando o estado dos sensores muda a imagem volta ao tamanho original
                        imgvinil.setScaleY(vinilScaleY);
                        houveActionUp = true;
                        return false;
                    }
                }
                return true;
            }
        });

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
            cancoes = (ArrayList) b.getParcelableArrayList("cancoes"); //obtem a lista de cancoes fornecida pela outra atividade
            posicao = (int) b.getInt("pos",0);      //obtem o numero da musica a tocar
            uri = Uri.parse(cancoes.get(posicao).toString());       //uri da musica escolhida
            nome.setText(posicao+" "+cancoes.get(posicao).getName().toString());    //poe o nome da musica numa textview
            mp = MediaPlayer.create(getApplication(),uri);      //inicializamos o mediaplayer com a cancao escolhida
            duracao = mp.getDuration(); //duracao da musica
            atualizarSeekBar.start();   //inicalizamos o servico de atualizar a seekbar à media que a musica vai tocando
            sb.setMax(duracao); //definimos o tamanho da seekbar para que fique sempre relativa ao tempo máximo de cada musica
            mp.start(); //play
            mp.seekTo(posicaoAtualPref);//posicao neste momento = 0
            Volume();   //pomos no ecra o valor atual do volume
            duracaoCancao.setText(getHRM(mp.getDuration()));    //escrevemos numa textview a duracao da musica
        }catch(Exception e){e.printStackTrace();}

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());               //atualizar tempo da musica de acordo com o toque efetuado na seekbar
            }
        });
    }

    public class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg){     //handler de mensagens criado, visto que apenas a thread principal é que pode mexer nas views que criou
            if(msg.arg1 == 1) //proxima cancao
            {
                NextCancao();
            }
            if(msg.arg1 == 2)//atualizar textview referente ao tempo atual da musica e imagem do vinil
            {
                int execucao;
                execucao = sb.getProgress();
                aux = getHRM(execucao);
                continua.setText(aux.toString().trim());    //tempo atual da musica
                angle += msg.arg2;  //calcular novo angulo
                imgvinil.setRotation((float) angle); //definir novo angulo na imagem
            }
            if(msg.arg1 == 3)//posicao inicial da imagem
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
                playOuPause(); //se estava em pause, faz play, caso contrário,.....
                break;
            case R.id.btnff:
                mp.seekTo(mp.getCurrentPosition()+5000);    //fastforward 5 segunos
                break;
            case R.id.btnfb:
                mp.seekTo(mp.getCurrentPosition() - 5000);      //fastbackward 5 segundos
                break;
            case R.id.btnNext:
                acabaServicoSeekBar();      //termina a thread atual
                NextCancao();           //inicia a proxima cancao e inicia uma nova thread
                break;
            case R.id.btnPv:
                acabaServicoSeekBar();
                PrevCancao();
                break;
            case R.id.btn_playlist:
                startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("pos",posicao).putExtra("cancaos",cancoes)); //muda para a atividade das lisa de musicas
                break;
        }
    }

    public void NextCancao(){ //cancao seguinte
        if(mp.isPlaying())
        mp.stop();//para a musica
        mp.release();//release de recursos
        mp = null;
        posicao = (posicao +1) % cancoes.size();//busca a proxima posicao e mesmo que chegue ao fim da lista, volta ao inicio
        nome.setText(posicao+" "+cancoes.get(posicao).getName().toString());    //nome da nova cancao
        uri = Uri.parse(cancoes.get(posicao).toString());   //uri da nova cancao
        mp = MediaPlayer.create(getApplicationContext(),uri);
        playMusica();//play
        sb.setMax(0);
        duracaoCancao.setText(getHRM(mp.getDuration()));
        try{sb.setMax(mp.getDuration());}catch (Exception e){}
        duracao = mp.getDuration();
        sb.setMax(duracao);//?
        atualizarSeekBar.start();
    }

    public void PrevCancao(){ //cancao anterior
        if(mp.isPlaying())
        mp.stop();
        mp.release();
        mp = null;
        if(posicao-1<0){ //caso esteja na primeira posicao vai para a ultima musica
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

    public void Volume(){//funcao que busca o volume do dispositivo e que muda o volume conforme selecionado na seekbar de volume
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

    public void acabaServicoSeekBar(){
        ban = true;
        atualizarSeekBar.interrupt();
    }

    public void playOuPause(){
        if(mp.isPlaying()){
            btnPlay.setImageResource(R.drawable.play);
            mp.pause();
        }else if(mp != null){
            btnPlay.setImageResource(R.drawable.pause);
            mp.start();
        }
    }

    public void pauseMusica(){
        if(mp.isPlaying()){ mp.pause();
        btnPlay.setImageResource(R.drawable.play);}
    }

    public void playMusica(){
        if(!mp.isPlaying()){ mp.start();
        btnPlay.setImageResource(R.drawable.pause);}
    }

    @Override
    public void onResume(){
        super.onResume();
        // Create listener
            SensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) { //quando à um evento num sensor registado ...
                if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) //caso o evento tenha sido detetado no acelerometro
                {
                    dadosAcelerometro[ia] = sensorEvent.values[2];//guarda valores do eixo Z obtidos a partir do acelerometro
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
                        if(checkFaceDown) //caso todos os valores em dadosAcelerometro[] nao estejam no intervalo acima definido, isto significa que o telefone está facedown e suficientemente parado para que façamos pause na musica
                        {
                            pauseMusica();
                        }
                        else
                        {
                           playMusica();
                        }
                    }
                    if(tempoDesativado > 10) {//depois de deterar um movimento fica 10 iteracoes sem obter dados
                                        if(timeverifica == 0)
                                        {
                                            teste1[0] = sensorEvent.values[0];      //teste1 são os valores da aceleração a cada iteracao
                                            teste2[0] = Math.atan2(teste1[0], sensorEvent.values[1]) / (Math.PI / 180); //teste2 são os angulos a cada iteracao
                                            if(teste1[0] < -1 || teste2[0] < 5) trueVerificacaoAnterior = false; //se os valores de teste1 e teste2 estiverem dentro destes intervalos, isto significa que nao estamos a movimentar o telefone para a esquerda(prevCancao)
                                            else
                                            {
                                                Log.i(MODULE,""+timeverifica+"angulo anterior:  "+teste2[timeverifica]);
                                                Log.i(MODULE,""+timeverifica+"aceleracao anterior:  "+teste1[timeverifica]);
                                            }
                                            if(teste1[0] > 1 || teste2[0] > -5) trueVerificacaoSeguinte = false; //se os valores de teste1 e teste2 estiverem dentro destes intervalos, isto significa que nao estamos a movimentar o telefone para a direita(nextCancao)
                                            else
                                            {
                                                Log.i(MODULE,""+timeverifica+" angulo seguinte:  "+teste2[timeverifica]);
                                                Log.i(MODULE,""+timeverifica+"aceleracao seguinte:  "+teste1[timeverifica]);
                                            }
                                        }
                                        else{
                                            teste1[timeverifica] = sensorEvent.values[0];
                                            teste2[timeverifica] = Math.atan2(teste1[timeverifica], sensorEvent.values[1]) / (Math.PI / 180);
                                            double valorZ = sensorEvent.values[2];
                                            if (valorZ < -1 || valorZ > 8) { //os valores de z têm de estar fora deste intervalo para considerarmos os movimentos como válidos
                                                trueVerificacaoSeguinte = false;
                                                trueVerificacaoAnterior = false;
                                                Log.i(MODULE, "z " + valorZ);
                                            }
                                            //verificacao dos angulos
                                            if (teste2[timeverifica] > teste2[timeverifica - 1] || teste2[timeverifica] + 15 > teste2[timeverifica - 1] || teste2[timeverifica] > -1) //angulo
                                            { //o angulo tem de aumentar gradualmente no sentido do relogio para percebermos que queremos mudar para a musica seguinte
                                                trueVerificacaoSeguinte = false;
                                            }
                                            else
                                            {
                                                Log.i(MODULE,""+timeverifica+"angulo seguinte:  "+teste2[timeverifica]);
                                            }
                                            if (teste2[timeverifica] < teste2[timeverifica - 1] || teste2[timeverifica] - 15 < teste2[timeverifica - 1] || teste2[timeverifica] < 1) {
                                                trueVerificacaoAnterior = false; //o angulo tem de aumentar gradualmente no sentido contrario ao relogio para percebermos que queremos mudar para a musica anterior
                                            }
                                            else
                                            {
                                                Log.i(MODULE,""+timeverifica+"angulo anterior:  "+teste2[timeverifica]);
                                            }
                                            //verificacao da aceleracao
                                            if (teste1[timeverifica] < teste1[timeverifica - 1] || teste1[timeverifica] < -1 || teste1[timeverifica] - 2 < teste1[timeverifica - 1])
                                                trueVerificacaoAnterior = false; //a aceleração tem de diminuir gradualmente no eixo X para que possamos mudar para a musica anterior e ser sempre um valor menor que -1
                                            else
                                            {
                                                Log.i(MODULE,""+timeverifica+"aceleracao anterior:  "+teste1[timeverifica]);
                                            }
                                            if (teste1[timeverifica] > teste1[timeverifica - 1] || teste1[timeverifica] > 1 || teste1[timeverifica] + 2 > teste1[timeverifica - 1])
                                                trueVerificacaoSeguinte = false; //a aceleração tem de aumentar gradualmente no eixo X para que possamos mudar para a musica seguinte e ser sempre um valor maior que 1
                                            else
                                            {
                                                Log.i(MODULE,""+timeverifica+"aceleracao seguinte:  "+teste1[timeverifica]);
                                            }
                                        }
                        timeverifica++;
                        if (timeverifica == 2) { //podemos mudar este numero para obtermos dados mais exatos
                            if (trueVerificacaoSeguinte) {//quando ja temos n resultados e todos estiverem de acordo com a logica de mudar para a musica seguinte entao mudamos e damos feedback
                                ban = true;
                                NextCancao();
                                Toast.makeText(Reprodutor.this, "SEGUINTE", Toast.LENGTH_SHORT).show();
                                tempoDesativado = 0;
                                trueVerificacaoAnterior = false;    //para ter a certeza que apenas mudamos para a musica seguinte
                            }
                            if (trueVerificacaoAnterior) {//quando ja temos n resultados e todos estiverem de acordo com a logica de mudar para a musica anterior entao mudamos e damos feedback
                                ban = true; //para acabar com a thread
                                PrevCancao();
                                Toast.makeText(Reprodutor.this, "ANTERIOR", Toast.LENGTH_SHORT).show();
                                tempoDesativado = 0; //repomos tempoDesativado para nao verificar um certo numero de iteracoes no inicio da proxima musica
                                trueVerificacaoSeguinte = false;    //para ter a certeza que nao mudamos tambem para a musica seguinte
                            }
                            timeverifica = 0;   //variavel que é usada para guardar valores em teste1 e teste2
                            trueVerificacaoSeguinte = true;
                            trueVerificacaoAnterior = true;
                        }
                    }
                    tempoDesativado++;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
    }

    @Override
    public void onPause() {
        acabaServicoSeekBar();
        if(mp.isPlaying())mp.pause();
        super.onPause();
    }

    @Override
    public void onStop(){
        acabaServicoSeekBar();
        sensorManager.unregisterListener(SensorListener);
        super.onStop();
    }

    @Override
    public void onDestroy(){
        acabaServicoSeekBar();
        sensorManager.unregisterListener(SensorListener);
        mp.stop();
        super.onDestroy();
    }
}
