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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    String ipDevice;
    TextView tv;
    ImageView iv;
    EditText edt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textView);
        iv = findViewById(R.id.imageView);
        edt = findViewById(R.id.edtCEP);
        NotificacaoConexao nc=new NotificacaoConexao();
    }

    public void onclickListar(View v){
        listConnections(); //lista todas as conexões
        if (estaWifiConectado()){
            // Se o Wifi estiver conectado tentarei ligar o servidor
            tv.setText("Wifi conectado em: "+ipDevice);
            //baixarImagem(); //Erro pq usa rede na Thread Main
            //baixarImagemThreads(); // Baixa a Imagem com Threads
           // baixarImagemEColocarNaView();//Erro ao acessar a View por outra Thread
          //  baixarImagemEColocarNaViewII();//Finalmente

            Thread t = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                           // executarACalculadora();
                            transformarCEP();
                        }
                    }
            );
            t.start();

        }
        else{
            String texto = "Macho, liga o Wifi";
            int duracao = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, texto,duracao);
            toast.show();
        }

    }

    private void transformarCEP() {

        //https://viacep.com.br/ws/60010020/json/


        String CEP= edt.getText().toString();
                //"6001002000";

        Log.v("PMD","CEP:"+CEP);
        try {
            URL url = new URL ("https://viacep.com.br/ws/"+CEP+"/json/");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true); //Vou ler dados?
            conn.connect();


            String result[] = new String[1];
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                result[0] = response.toString();
              //  Log.v ("PDM","Resultado:"+result[0]);

                JSONObject respostaJSON = new JSONObject(result[0]);

                final String loc = respostaJSON.getString("logradouro");
                String cidade = respostaJSON.getString("localidade");

                Log.v ("PDM","Esse é o CEP da rua "+loc+" da cidade "+cidade);

                tv.post(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(loc);
                    }
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }





    private void executarACalculadora() {

        //https://double-nirvana-273602.appspot.com/?hl=pt-BR



        int oper1,oper2,operacao;
        oper1 = 14;
        oper2 = 22;
        operacao= 1;

        try {
            URL url = new URL ("https://double-nirvana-273602.appspot.com/?hl=pt-BR");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true); //Vou ler dados?
            conn.setDoOutput(true); //Vou Enviar dados?

            //Criar objetos de comunicação em Java

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
            writer.write("oper1="+oper1+"&oper2="+oper2+"&operacao="+operacao);
            writer.flush();
            writer.close();
            os.close();

            String result[] = new String[1];
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                result[0] = response.toString();
                Log.v ("PDM","Resultado:"+result[0]);

            }







        } catch (Exception e) {
            e.printStackTrace();
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