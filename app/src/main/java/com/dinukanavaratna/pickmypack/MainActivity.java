package com.dinukanavaratna.pickmypack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dinukanavaratna.pickmypack.dataStoring.localDB;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button group = findViewById(R.id.group);
        Button list = findViewById(R.id.list);
        Button history = findViewById(R.id.history);
        ImageView imageView3 = findViewById(R.id.imageView3);

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] tables = {"shopping_group", "shopping_pins"};
                com.dinukanavaratna.pickmypack.dataStoring.localDB localdb = new localDB();
                localdb.getAllData(tables);
            }
        });

        imageView3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Logging Out...", Toast.LENGTH_SHORT).show();
                SharedPreferences user = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                user.edit().clear().commit();
                Intent myIntent = new Intent(MainActivity.this, splash.class);
                MainActivity.this.startActivity(myIntent);
                return false;
            }
        });

        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, shopping_groups.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, shopping_list.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, history.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
    }
}