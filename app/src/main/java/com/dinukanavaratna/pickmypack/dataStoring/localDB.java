package com.dinukanavaratna.pickmypack.dataStoring;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dinukanavaratna.pickmypack.MainActivity;
import com.dinukanavaratna.pickmypack.appBase;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dinukanavaratna.pickmypack.login;
import com.dinukanavaratna.pickmypack.splash;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class localDB {

    private Context context = appBase.getAppContext();
    private SQLiteDatabase mydatabase;

    public void getAllData(final String[] tables) {
        try {
            mydatabase = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS shopping_group(id INTEGER PRIMARY KEY, user_id INTEGER(5), topic TEXT(250), description TEXT(500), date TEXT(20), status TEXT(20));");
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS shopping_pins(id INTEGER PRIMARY KEY, group_id INTEGER(5), description TEXT(500), time TEXT(20), status TEXT(20), payment DOUBLE, lat REAL, lng REAL);");
        } catch (Exception x){
        }

        for (final String table : tables) {
            Log.i("Table", table);
            final String URL_PAYMENTS = "https://infotechdesigners.com/PICK%20my%20PACK/readAllData.php?dbName=infotech_PICK_my_PACK&code="+table;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PAYMENTS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray array = new JSONArray(response);

                                if(table.equals("shopping_group")) {
                                    mydatabase.execSQL("DELETE FROM shopping_group;");
                                } else if(table.equals("shopping_pins")) {
                                    mydatabase.execSQL("DELETE FROM shopping_pins;");
                                }

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject value = array.getJSONObject(i);

                                    if(table.equals("shopping_group")) {
                                        mydatabase.execSQL("INSERT INTO shopping_group VALUES(" +
                                                "'"+value.getInt("id")+"'," +
                                                ""+value.getInt("user_id")+"," +
                                                "'"+value.getString("topic")+"'," +
                                                "'"+value.getString("description")+"'," +
                                                "'"+value.getString("date")+"'," +
                                                "'"+value.getString("status")+"'"+
                                                ");");
                                        Log.i("LocalDB Groups", value.getString("id"));

                                    } else if(table.equals("shopping_pins")) {
                                        mydatabase.execSQL("INSERT INTO shopping_pins VALUES(" +
                                                ""+value.getInt("id")+"," +
                                                ""+value.getInt("group_id")+"," +
                                                "'"+value.getString("description")+"'," +
                                                "'"+value.getString("time")+"'," +
                                                "'"+value.getString("status")+"'," +
                                                ""+value.getDouble("payment")+"," +
                                                ""+value.getDouble("lat")+"," +
                                                ""+value.getDouble("lng")+""+
                                                ");");
                                        Log.i("LocalDB Pins", value.getString("id"));
                                    } else {
                                        try {
                                            SharedPreferences user = PreferenceManager.getDefaultSharedPreferences(appBase.getAppContext());
                                            String psw = user.getString("password", "");
                                            if (!(value.getString("id")).equals("0")) {
                                                if ((psw).equals(value.getString("password"))) {
                                                    SharedPreferences.Editor editor = user.edit();
                                                    editor.putString("id", value.getString("id"));
                                                    editor.putString("name", value.getString("name"));
                                                    editor.putString("email", value.getString("email"));
                                                    editor.putString("country", value.getString("country"));
                                                    editor.putString("password", "");
                                                    editor.commit();
                                                    Toast.makeText(appBase.getAppContext(), "Login Successful...", Toast.LENGTH_SHORT).show();
                                                    Intent startActivity = new Intent();
                                                    startActivity.setClass(context, MainActivity.class);
                                                    startActivity.setAction(MainActivity.class.getName());
                                                    startActivity.setFlags(
                                                            Intent.FLAG_ACTIVITY_NEW_TASK
                                                                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                    context.startActivity(startActivity);
                                                } else {
                                                    Toast.makeText(appBase.getAppContext(), "Incorrect Password...", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(appBase.getAppContext(), "User not found...", Toast.LENGTH_SHORT).show();
                                            }
                                            Log.i("LocalDB User", value.getString("name"));
                                        }catch (Exception x){
                                            Log.i("Login Exception", x.toString());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i("DBException", e.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

            //adding our stringrequest to queue
            try {
                Volley.newRequestQueue(context).add(stringRequest);
            } catch (Exception x){}
        }

    }
}
