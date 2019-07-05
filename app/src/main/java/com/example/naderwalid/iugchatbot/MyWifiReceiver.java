package com.example.naderwalid.iugchatbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyWifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        if(status.isEmpty()) {
            status="لا يوجد إتصال بالإنترنت";
        }
        Toast.makeText(context, status, Toast.LENGTH_LONG).show();
    }
}
