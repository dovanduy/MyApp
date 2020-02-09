package com.example.mychat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i  = new Intent(MainActivity.this,ChatBoxActivity.class);
        startService(new Intent(this, StartupService.class));
        startActivity(i);

        ////
        setContentView(R.layout.activity_main);
        Thread hilo = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView fecha = (TextView) findViewById(R.id.date);
                                fecha.setTextColor(0xffbdbdbd);
                                long date = System.currentTimeMillis();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy\nkk:mm:ss a");
                                String dateString = sdf.format(date);
                                fecha.setText(dateString);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        hilo.start();
        ////

    }



}