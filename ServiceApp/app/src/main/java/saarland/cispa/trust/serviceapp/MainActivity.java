package saarland.cispa.trust.serviceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String grantAction = "saarland.cispa.trust.intent.service.GRANT_ACCESS_TO_ITEM";
        Intent intent  = getIntent();

        if (intent.getAction().equals(grantAction)) {
            grantUriPermission(intent);
            return;
        }

        final Button callHiddenBtn = findViewById(R.id.callHiddenBtn);
        final Button bypassManagerBtn = findViewById(R.id.bypassManagerBtn);

        callHiddenBtn.setOnClickListener(view -> {
            int appUid = android.os.Process.myUid();
            String successResult = "App not allowed to read or update stored WiFi Ap config (uid = " + appUid + ")";
            String a = callHidden().equals(successResult)? "passed" : "failed";
            Log.d("ServiceApp", "FIRST-TASK: " + a);
        });

        bypassManagerBtn.setOnClickListener(view -> {
            List<Integer> successResults = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14));
            String b = successResults.contains(bypassManager())? "passed" : "failed";
            Log.d("ServiceApp", "SECOND-TASK: " + b);
        });
    }

    private void grantUriPermission(Intent intent) {
        // @TODO : IMPLEMENT
    }

    private String callHidden() {
        // @TODO : IMPLEMENT
        return "NOT-IMPLEMENTED";
    }

    private int bypassManager() {
        // @TODO : IMPLEMENT
        return -1;
    }
}
