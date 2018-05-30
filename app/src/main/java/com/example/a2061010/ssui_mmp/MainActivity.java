package com.example.a2061010.ssui_mmp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
   /* SharedPreferences sharedPreferences;
    public static final String mypreference = "mypref";
    public static final String Duracao = "duracaoAtual";
    public static final String Musica = "musica";*/
    ListView lista_musicas;
    String[] itens;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int perm=0;
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},perm);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lista_musicas=(ListView)findViewById(R.id.lista_musica);


    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) //
    {
        TextView teste=(TextView)findViewById(R.id.textView2);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            teste.setText("Necessita de dar permissoes para aceder aos dados do cartão SD para visualizar o ficheiros existentes. ");
        }
        else {
            teste.setText("Lista de músicas:");
            final ArrayList<File> cancaos = BuscaMusica(Environment.getExternalStorageDirectory().getAbsoluteFile());
            itens = new String[cancaos.size()];
            for (int i = 0; i < cancaos.size(); i++) {
                itens[i] = i+" "+cancaos.get(i).getName().toString().replace("mp3", "").toLowerCase();
                Uri uri = Uri.parse(cancaos.get(i).getAbsolutePath());
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(getApplicationContext(),uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int millSecond = Integer.parseInt(durationStr);
                Number seconds = millSecond/1000;
                itens[i] = itens[i]+" "+seconds.floatValue()+" s";
            }
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.cancao, R.id.textView, itens);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_main,R.id.textView2,itens); //adaptar o array de musicas para depois utilizar na listview. É necessário dar uma textview para poder instanciar cada item na listview
            lista_musicas.setAdapter(adapter);
            lista_musicas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 /*   sharedPreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.commit();*/
                    startActivity(new Intent(getApplicationContext(),Reprodutor.class).putExtra("pos",position).putExtra("cancoes",cancaos));
                }
            });
        }
    }

    public ArrayList<File> BuscaMusica(File raiz){// objetivo com base nesta rota vai buscar todos ficheiros de musica
        File[] arquivos=raiz.listFiles();         // recebe lista de arquivos que pertecem a essa rota
        ArrayList<File> ficheiro_musicas= new ArrayList<File>();// basicamente vai ser o array que contem todos ficheiros de musicas
        for(File lista : arquivos){//vai correndo os arquivos da lista
            if(lista.isDirectory() && !lista.isHidden()){// entra aqui depois de correr toda a carpeta e passa para carpeta seguinte
                ficheiro_musicas.addAll(BuscaMusica(lista));
            }
            else{
                if(lista.getName().endsWith(".mp3")){// basicamente buscar os ficheiros que acabam em mp3
                    ficheiro_musicas.add(lista);
                }
            }
        }
        return ficheiro_musicas;
    }
}
