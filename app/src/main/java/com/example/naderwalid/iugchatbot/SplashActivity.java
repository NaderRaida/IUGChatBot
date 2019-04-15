package com.example.naderwalid.iugchatbot;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_INTERNET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork != null){
            Toast.makeText(this, "Internet Connection", Toast.LENGTH_SHORT).show();
            checkPermissions();
        }else{
            Toast.makeText(this, "check internet connection,try again!", Toast.LENGTH_SHORT).show();
        }

//            startActivity(new Intent(SplashActivity.this,ChatBotActivity.class));
//            finish();




    }
    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    private boolean checkPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        final List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(SplashActivity.this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), PERMISSIONS_REQUEST_INTERNET);
            requestInternetPermission();
            return false;
        }

        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
                } else {
                    checkPermissions();
                    Toast.makeText(this, "You need to Allow Write Storage Permission!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    private void requestInternetPermission() {
        // TODO - Check if showing permission rationale needed
        // If yes, call showExplanationDialog() to explain why you need this permission
        // If not, request the permission from the Android system
        if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, "android.permission.INTERNET")) {
            showExplanationDialog();

        } else {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{"android.permission.INTERNET"}, PERMISSIONS_REQUEST_INTERNET);

        }

    }

    private void showExplanationDialog() {
        // TODO: show alert dialog that show the following message to the user
        // "Read Contacts permission needed to be able to bring contacts here"
        // Dialog should contain two buttons, "Ok" and "Cancel"
        // If the user click Ok, request permission again
        // If the user click Cancel, only dismiss the dialog
        new AlertDialog.Builder(this)
                .setMessage("Read Contacts permission needed to be able to bring contacts here")
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(SplashActivity.this, new String[]{"android.permission.INTERNET"}, PERMISSIONS_REQUEST_INTERNET);
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


}
