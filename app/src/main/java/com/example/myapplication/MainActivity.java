package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    String ipDevice;
    TextView tv;
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textView);
        iv = findViewById(R.id.imageView);
        NotificacaoConexao nc=new NotificacaoConexao();
    }

    public void onclickListar(View v){
        listConnections(); //lista todas as conexões
        if (estaWifiConectado()){
            // Se o Wifi estiver conectado tentarei ligar o servidor
            tv.setText("Wifi conectado em: "+ipDevice);
            baixarImagem(); //Erro pq usa rede na Thread Main
            baixarImagemThreads(); // Baixa a Imagem com Threads
            baixarImagemEColocarNaView();//Erro ao acessar a View por outra Thread
            baixarImagemEColocarNaViewII();//Finalmente
        }
        else{
            String texto = "Macho, liga o Wifi";
            int duracao = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, texto,duracao);
            toast.show();
        }

    }

    private void baixarImagemEColocarNaView() {
        Log.v("PDM", "BAIXANDO IMAGEM E COLANDO NA VIEW");
        Bitmap b;
        Thread t= new Thread (new Runnable(){
            @Override
            public void run() {
                Bitmap b= loadImageFromNetwork("http://pudim.com.br/pudim.jpg");
                Log.v("PDM", "Imagem baixada com "+b.getByteCount()+" bytes");
                try {
                    iv.setImageBitmap(b);
                }catch (Exception e){
                    Log.v("PDM","Não é possível acessar a Thread UI");
                    e.printStackTrace();
                }
            }}
        );
        t.start();

    }


    private void baixarImagemEColocarNaViewII() {
        Log.v("PDM", "BAIXANDO IMAGEM E COLANDO NA VIEW");

        Thread t= new Thread (new Runnable(){
            @Override
            public void run() {
                final Bitmap b= loadImageFromNetwork("http://pudim.com.br/pudim.jpg");
                Log.v("PDM", "Imagem baixada com "+ b.getByteCount()+" bytes");
                try {
                    iv.post(new Runnable(){
                        @Override
                        public void run() {
                            iv.setImageBitmap(b);
                        }
                    });

                }catch (Exception e){
                    Log.v("PDM","Não é possível acessar a Thread UI");
                    e.printStackTrace();
                }
            }}
        );
        t.start();

    }
    public void onClickTravar(View v){
        Log.v("PDM","Vou tentar travar");
        for (long i=0;i<10000000;i++){
            for (long j=0; j<1000000000;j++){

            }
        }
        Log.v("PDM","Terminou o loop?");
    }

    public void baixarImagem(){
        Log.v("PDM", "BAIXANDO IMAGEM SEM THREAD");
        Bitmap b = loadImageFromNetwork("http://pudim.com.br/pudim.jpg");
        //mImageView.setImageBitmap(b);

    }



    public void baixarImagemThreads(){
        Log.v("PDM", "BAIXANDO IMAGEM COM THREAD");
        Bitmap b;
        Thread t= new Thread (new Runnable(){
                @Override
                public void run() {
                Bitmap b= loadImageFromNetwork("http://pudim.com.br/pudim.jpg");
                Log.v("PDM", "Imagem baixada com "+b.getByteCount()+" bytes");
        }}
        );
        t.start();


    }

    private Bitmap loadImageFromNetwork(String url) {

        try {
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;}




    public boolean estaWifiConectado(){
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks =  connManager.getAllNetworks();
        NetworkInfo networkInfo;

        boolean temConexao= false;

        for (Network mNetwork : networks) {
            networkInfo = connManager.getNetworkInfo(mNetwork);
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {

                NetworkCapabilities networkFeatures;
                networkFeatures = connManager.getNetworkCapabilities(mNetwork);

                if (networkFeatures.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    temConexao=true;

                    Log.v("PDM", "Tem acesso a WIFI");
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    String macAddress = wifiManager.getConnectionInfo().getMacAddress();
                    int ip = wifiManager.getConnectionInfo().getIpAddress();
                    String ipAddress = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
                    Log.v("PDM", "IP:"+ipAddress);
                    Log.v("PDM", "MAC:"+macAddress);

                    ipDevice=ipAddress;

                    //Vamos ouvir mudanças nessa conexão
                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                    NotificacaoConexao nfc = new NotificacaoConexao();
                    connManager.registerNetworkCallback(builder.build(),nfc);

                    }

            }
        }
        return temConexao;

    }

    public void listConnections(){
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks =  connManager.getAllNetworks();
        NetworkInfo networkInfo;


        for (Network mNetwork : networks) {
            networkInfo = connManager.getNetworkInfo(mNetwork);
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {

                Log.v ("PDM","tipo de conexão conectada:"+ networkInfo.getTypeName());

            }
        }

    }



}

class NotificacaoConexao extends ConnectivityManager.NetworkCallback{

    //Um Call Back para saber se houve ou não mudanças no estado da conexão
    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        Log.v("PDM", "Wifi está conectado");
    }
    @Override
    public void onLost(Network network) {
        super.onLost(network);
        Log.i("PDM", "Desligaram o Wifi");
    }
}