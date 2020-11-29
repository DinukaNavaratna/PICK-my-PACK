package com.dinukanavaratna.pickmypack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.dinukanavaratna.pickmypack.dataStoring.localDB;

public class splash extends AppCompatActivity {

    private Boolean permission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        final String[] tables = {"shopping_group", "shopping_pins"};
        com.dinukanavaratna.pickmypack.dataStoring.localDB localdb = new localDB();
        localdb.getAllData(tables);
        permissionCheck();
    }


    private boolean permissionCheck() {
        Boolean check = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please provide access location permissions for this app...", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            check = false;
            return false;
        } else {
            permission = true;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please provide make access location permissions for this app...", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            check = false;
            return false;
        } else {
            permission = true;
        }
        if(check){
            action();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permission = true;
            permissionCheck();
        }
        else {
            permission = false;
            permissionCheck();
        }
    }

    private void action(){
        if (isOnline() != true) {
            Toast.makeText(splash.this, "Internet Not Connected!", Toast.LENGTH_SHORT).show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    action();
                }
            }, 3000);
        } else {
            if (permission) {
                try {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences user = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String uname = user.getString("id", "");
                            if (uname.equals("")) {
                                Toast.makeText(splash.this, "Please login...", Toast.LENGTH_SHORT).show();
                                Intent myIntent = new Intent(splash.this, login.class);
                                splash.this.startActivity(myIntent);
                                finish();
                            } else {
                                Intent myIntent = new Intent(splash.this, MainActivity.class);
                                splash.this.startActivity(myIntent);
                                finish();
                            }
                        }
                    }, 3000);
                } catch (Exception x) {
                }
            } else {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        splash.this.finish();
                        splash.this.startActivity(splash.this.getIntent());
                    }
                }, 3000);
            }
        }
    }

    //-------------- Internet -----------------
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) appBase.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    //-------------------------------------------

}