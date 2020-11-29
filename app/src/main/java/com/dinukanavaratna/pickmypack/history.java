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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dinukanavaratna.pickmypack.dataStoring.localDB;
import com.ligl.android.widget.iosdialog.IOSDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class history extends AppCompatActivity {

    private List<shopping_groups_lists> groupList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar loading;
    private Button back_btn;
    private SharedPreferences user;
    private FrameLayout loadingFrame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        back_btn = findViewById(R.id.history_back_btn);
        recyclerView = findViewById(R.id.history_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loading = findViewById(R.id.history_loading);
        loadingFrame = findViewById(R.id.history_loadingFrame);
        user = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadData();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent myIntent = new Intent(history.this, MainActivity.class);
                history.this.startActivity(myIntent);
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
            Cursor resultSet = database.rawQuery("Select * from shopping_group WHERE user_id=" + uid + " AND status='Completed' ORDER BY id ASC;", null);
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
                history_adapter adapter = new history_adapter(history.this, groupList, "");
                recyclerView.setAdapter(adapter);
                loading.setVisibility(View.INVISIBLE);
            } else {
                new IOSDialog.Builder(history.this)
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