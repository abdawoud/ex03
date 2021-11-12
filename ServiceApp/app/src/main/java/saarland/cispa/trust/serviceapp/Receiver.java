package saarland.cispa.trust.serviceapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String key = "_id";
        Bundle b = intent.getExtras();
        if (b == null || !b.containsKey(key)) {
            return;
        }
        Log.d("ServiceApp", "CONGRATULATIONS_YOU_GOT_THIS_WORKING");
    }
}
