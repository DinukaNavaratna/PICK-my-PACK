package com.dinukanavaratna.pickmypack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dinukanavaratna.pickmypack.dataStoring.localDB;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.ligl.android.widget.iosdialog.IOSDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class shopping_groups extends AppCompatActivity {

    private List<shopping_groups_lists> groupList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar loading;
    private Button back_btn, add_list_btn;
    private SharedPreferences user;
    private FrameLayout loadingFrame;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_groups);

        back_btn = findViewById(R.id.group_back_btn);
        recyclerView = findViewById(R.id.group_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loading = findViewById(R.id.group_loading);
        add_list_btn = findViewById(R.id.add_group_btn);
        loadingFrame = findViewById(R.id.group_loadingFrame);
        user = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadData();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent myIntent = new Intent(shopping_groups.this, MainActivity.class);
                shopping_groups.this.startActivity(myIntent);
            }
        });

        add_list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IOSDialog.Builder builder = new IOSDialog.Builder(shopping_groups.this);
                builder.setTitle("Add New List");

                final LinearLayout layout = new LinearLayout(shopping_groups.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final AutoCompleteTextView topic = new AutoCompleteTextView(shopping_groups.this);
                topic.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                topic.setCursorVisible(true);
                topic.setHint("Topic");
                layout.addView(topic);

                final EditText description = new EditText(shopping_groups.this);
                description.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                description.setCursorVisible(true);
                description.setHint("Description");
                layout.addView(description);

                builder.setContentView(layout);
                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!(topic.getText().toString()).equals("") && !(description.getText().toString()).equals("")) {
                            String uid = user.getString("id", "");
                            String qry = "INSERT INTO shopping_group (user_id, topic, description) VALUES (" + uid + ", '" + topic.getText().toString() + "' , '" + description.getText().toString() + "');";
                            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                            nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                            insertData data = new insertData(nameValuePairs);
                            refreshPage();
                        } else {
                            Toast.makeText(shopping_groups.this, "All the fields must be filled...", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
            }
        });
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void loadData(){
        //---------------- Local DB ----------------------------------------------------------------
        try {
            String uid = user.getString("id", "");
            Context context = appBase.getAppContext();
            SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
            Cursor resultSet = database.rawQuery("Select * from shopping_group WHERE user_id=" + uid + " AND status='Pending' ORDER BY id ASC;", null);
            resultSet.moveToFirst();
            if (resultSet.getCount() > 0) {
                groupList.clear();
                do {
                    groupList.add(new shopping_groups_lists(
                            resultSet.getInt(0),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getString(5)
                    ));
                } while (resultSet.moveToNext());

                recyclerView.removeAllViews();
                shopping_groups_adapter adapter = new shopping_groups_adapter(shopping_groups.this, groupList, "");
                recyclerView.setAdapter(adapter);
                loading.setVisibility(View.INVISIBLE);
            } else {
                new IOSDialog.Builder(shopping_groups.this)
                        .setMessage("No list item found!")
                        .setNegativeButton("Yey...", null)
                        .show();
            }
        } catch (Exception x){}
        //---------------- End Local DB ------------------------------------------------------------
    }

    public void refreshPage(){
        try {
            loadingFrame.setVisibility(View.VISIBLE);
            final String[] tables = {"shopping_group", "shopping_pins"};
            com.dinukanavaratna.pickmypack.dataStoring.localDB localdb = new localDB();
            localdb.getAllData(tables);
            if (isOnline()) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        startActivity(getIntent());
                        loadingFrame.setVisibility(View.GONE);
                    }
                }, 3000);
            } else {
                Toast.makeText(this, "Refresh failed!\nNot connected to internet.", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Refresh failed!\nNot connected to internet.", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception x){}
    }
}