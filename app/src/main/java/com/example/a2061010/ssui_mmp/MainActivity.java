package com.example.a2061010.ssui_mmp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    ListView lista_musicas;
    String[] itens;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int perm=0;
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},perm);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lista_musicas=(ListView)findViewById(R.id.lista_musica);
        TextView teste=(TextView)findViewById(R.id.textView2);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            teste.setText("negou ");
        }
        else {
            final ArrayList<File> cancaos = BuscaMusica(Environment.getExternalStorageDirectory().getAbsoluteFile());
            teste.setText("aceitou ");
            itens = new String[cancaos.size()];
            for (int i = 0; i < cancaos.size(); i++) {
                itens[i] = cancaos.get(i).getName().toString().replace("mp3", "").toLowerCase();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.cancao, R.id.textView, itens);
            lista_musicas.setAdapter(adapter);
            lista_musicas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
