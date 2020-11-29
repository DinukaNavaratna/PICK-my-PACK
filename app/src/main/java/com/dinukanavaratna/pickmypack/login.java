package com.dinukanavaratna.pickmypack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dinukanavaratna.pickmypack.dataStoring.localDB;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class login extends AppCompatActivity {

    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        final EditText email = (EditText) findViewById(R.id.email);
        final EditText psw = (EditText) findViewById(R.id.psw);
        final Button login = (Button) findViewById(R.id.login);
        loading = (ProgressBar) findViewById(R.id.loading);
        TextView textView5 = findViewById(R.id.textView5);

        textView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://pickmypack.co/"));
                startActivity(browserIntent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
                String emailValue = email.getText().toString().toLowerCase();
                String passwordValue = psw.getText().toString();
                if(emailValue != "" && passwordValue != ""){

                    String encPsw2 = md5(passwordValue);
                    String encPsw1 = md5(encPsw2);
                    passwordValue = md5(encPsw1);

                    SharedPreferences user = PreferenceManager.getDefaultSharedPreferences(appBase.getAppContext());
                    SharedPreferences.Editor editor = user.edit();
                    editor.putString("password", passwordValue);
                    editor.commit();

                    final String[] tables = {emailValue};
                    com.dinukanavaratna.pickmypack.dataStoring.localDB localdb = new localDB();
                    localdb.getAllData(tables);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.INVISIBLE);
                        }
                    }, 3000);
                } else {
                    Toast.makeText(login.this, "Username & Password cannot be blank!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ---------------------- Encryption --------------------------
    public String md5(String s) {
        try {
            String result = s;
            if(s != null) {
                MessageDigest md = MessageDigest.getInstance("MD5"); //or "SHA-1"
                md.update(s.getBytes());
                BigInteger hash = new BigInteger(1, md.digest());
                result = hash.toString(16);
                while(result.length() < 32) { //40 for SHA-1
                    result = "0" + result;
                }
            }
            Log.i("Pass", result);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    // ---------------------- End Encryption --------------------------

    @Override
    public void onBackPressed() {
    }

    public void register(String email){
        Log.i("Send email", "");

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PICK my PACK - Register");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Use the following link to activate your registration....\n\nURL: www.");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Email sent...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(login.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}