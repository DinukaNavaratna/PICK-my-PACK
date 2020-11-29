package com.dinukanavaratna.pickmypack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dinukanavaratna.pickmypack.dataStoring.localDB;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ligl.android.widget.iosdialog.IOSDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class shopping_list extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    
    private Integer group_id;
    private List<shopping_list_items> shoppingList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar loading;
    private Button add_location, back_btn, submit, add_list_btn, add_back;
    private Double lat = null, lng = null;
    private GoogleMap mMap;
    private RelativeLayout mapFrame;
    private FrameLayout frameLayout;
    private AutoCompleteTextView group_list;
    public List<String> productList = new ArrayList<>();
    public List<Integer> productListId = new ArrayList<>();
    private View loadView;
    private TextView textView4;
    private SharedPreferences user;
    private FrameLayout loadingFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_lists);

        frameLayout = findViewById(R.id.frameLayout);
        loadView = LayoutInflater.from(shopping_list.this).inflate(R.layout.add_new_pin, frameLayout, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);

        back_btn = findViewById(R.id.list_back_btn);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loading = findViewById(R.id.loading);
        add_list_btn = findViewById(R.id.add_list_btn);
        group_list = findViewById(R.id.group_list);
        textView4 = findViewById(R.id.textView4);
        loadingFrame = findViewById(R.id.loadingFrame);
        user = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Context context = appBase.getAppContext();
        SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
        String uid = user.getString("id", "");
        Cursor resultSet1 = database.rawQuery("Select id, topic from shopping_group WHERE user_id="+uid+" ORDER BY id ASC;", null);
        resultSet1.moveToFirst();
        productList.clear();
        productListId.clear();
        if(resultSet1.getCount() > 0) {
            productList.clear();
            productListId.clear();
            do {
                productList.add(resultSet1.getString(1));
                productListId.add(resultSet1.getInt(0));
            } while (resultSet1.moveToNext());
        }

        new IOSDialog.Builder(shopping_list.this)
            .setMessage("Enter the list name to show the items...")
            .setNegativeButton("Okay...", null)
            .show();

        ArrayAdapter<String> productsAdapter = new ArrayAdapter<String>(shopping_list.this, android.R.layout.simple_dropdown_item_1line, productList);
        productsAdapter.setNotifyOnChange(true);
        group_list.setThreshold(1);
        group_list.setAdapter(productsAdapter);

        group_list.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                String search = s.toString();
                int count = 0;
                for (String string : productList) {
                    if (string.equals(search)) {
                        group_id = productListId.get(count);
                        loadData();
                        break;
                    }
                        count = count + 1;
                }
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent myIntent = new Intent(shopping_list.this, MainActivity.class);
                shopping_list.this.startActivity(myIntent);
            }
        });

        add_list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
                add_list_btn.setVisibility(View.GONE);
                textView4.setVisibility(View.GONE);
                back_btn.setVisibility(View.GONE);
                group_list.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);

                frameLayout.setVisibility(View.VISIBLE);
                frameLayout.removeAllViews();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    TransitionManager.beginDelayedTransition(frameLayout, new Slide(Gravity.TOP));
                }
                frameLayout.addView(loadView);

                add_back = findViewById(R.id.add_back);
                submit = findViewById(R.id.submit_btn);
                add_location = findViewById(R.id.add_location);
                mapFrame = findViewById(R.id.mapFrame);

                final AutoCompleteTextView group_list_add = findViewById(R.id.group_list_add);

                ArrayAdapter<String> productsAdapter = new ArrayAdapter<String>(shopping_list.this, android.R.layout.simple_dropdown_item_1line, productList);
                productsAdapter.setNotifyOnChange(true);
                group_list_add.setThreshold(1);
                group_list_add.setAdapter(productsAdapter);

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText description_add = findViewById(R.id.description_add);
                        EditText payments_add = findViewById(R.id.payments_add);

                        final String group_list_add1 = group_list_add.getText().toString();
                        final String description_add1 = description_add.getText().toString();
                        String payments_add1 = payments_add.getText().toString();

                        if(lat == null || lng == null || lat == 0 || lng ==0){
                            new IOSDialog.Builder(shopping_list.this)
                                    .setTitle("Set Location")
                                    .setMessage("Provide the location of your package...")
                                    .setNegativeButton("Ok", null)
                                    .show();
                        } else {
                            if (!group_list_add1.equals("") || !description_add1.equals("")) {
                                if(payments_add1.equals("")){
                                    payments_add1 = "0";
                                }

                                Context context = appBase.getAppContext();
                                SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
                                final Cursor resultSet = database.rawQuery("Select id from shopping_group WHERE topic='"+group_list_add1+"';", null); //Select * from requests WHERE donation_count<3 ORDER BY id;
                                if(resultSet.getCount() == 1) {
                                    resultSet.moveToFirst();
                                    String qry = "INSERT INTO shopping_pins (group_id, description, payment, lat, lng) VALUES ("+resultSet.getInt(0)+", '" + description_add1 + "', '" + payments_add1 + "', " + lat + ", " + lng + ");";
                                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                    nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                    insertData data = new insertData(nameValuePairs);

                                    refreshPage();
                                } else {
                                    IOSDialog.Builder builder = new IOSDialog.Builder(shopping_list.this);
                                    builder.setTitle(group_list_add1);
                                    // Set up the input
                                    final EditText input = new EditText(shopping_list.this);
                                    input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    input.setCursorVisible(true);
                                    builder.setContentView(input);
                                    final String finalPayments_add = payments_add1;
                                    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String uid = user.getString("id", "");

                                            String qry1 = "INSERT INTO shopping_group (user_id, topic, description) VALUES ("+uid+", '" + group_list_add1 + "', '" + input.getText().toString() +"');";
                                            List<NameValuePair> nameValuePairs1 = new ArrayList<NameValuePair>();
                                            nameValuePairs1.add(new BasicNameValuePair("querytoDB", qry1));
                                            insertData data1 = new insertData(nameValuePairs1);

                                            String qry = "INSERT INTO shopping_pins (group_id, description, payment, lat, lng) VALUES ((SELECT id FROM shopping_group ORDER BY id DESC LIMIT 1), '" + description_add1 + "', '" + finalPayments_add + "', " + lat + ", " + lng + ");";
                                            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                            nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                            insertData data = new insertData(nameValuePairs);

                                            refreshPage();
                                        }
                                    });
                                    builder.setNegativeButton("Cancel", null);
                                    builder.show();
                                }
                            } else {
                                new IOSDialog.Builder(shopping_list.this)
                                        .setTitle("Please fill the details")
                                        .setMessage("Providing your name helps the donators to find you easily while the passcode will be helping you to confirm the donation...")
                                        .setNegativeButton("Ok", null)
                                        .show();
                            }
                        }
                    }
                });

                add_location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getLocation();
                        add_back.setVisibility(View.GONE);
                        try {
                            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {
                        }
                        if (lat == null || lng == null) {
                            lat = 6.927079;
                            lng = 79.861244;
                        }
                        mapMarker();
                        mapFrame.setVisibility(View.VISIBLE);
                        mapFrame.bringToFront();
                        back_btn.setVisibility(View.GONE);

                        final TextView mapSearchTxt = ((TextView) findViewById(R.id.mapSearchTxt));
                        Button mapSearch = findViewById(R.id.mapSearch);
                        Button mapClose = findViewById(R.id.mapClose);

                        mapSearch.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                                } catch (Exception e) {
                                }
                                if (Geocoder.isPresent()) {
                                    try {
                                        String city = ((EditText)findViewById(R.id.mapSearchTxt)).getText().toString();
                                        if(!city.equals("")) {
                                            Geocoder gc = new Geocoder(shopping_list.this);
                                            List<Address> addresses = gc.getFromLocationName(city, 5); // get the found Address Objects

                                            List<LatLng> ll = new ArrayList<LatLng>(addresses.size()); // A list to save the coordinates if they are available
                                            for (Address a : addresses) {
                                                if (a.hasLatitude() && a.hasLongitude()) {
                                                    lat = a.getLatitude();
                                                    lng = a.getLongitude();
                                                    LatLng loc;
                                                    loc = new LatLng(lat, lng);
                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                                                    Toast.makeText(shopping_list.this, a.getLatitude() + " || " + a.getLongitude(), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    lat = 6.927079;
                                                    lng = 79.861244;
                                                }
                                            }
                                        } else {
                                            Toast.makeText(shopping_list.this, "Please enter a valid keyword!", Toast.LENGTH_SHORT).show();
                                            lat = 6.927079;
                                            lng = 79.861244;
                                        }
                                        mapMarker();
                                    } catch (IOException e) {
                                        lat = 6.927079;
                                        lng = 79.861244;
                                    }
                                }
                            }
                        });

                        mapClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                                } catch (Exception e) {
                                }
                                mapFrame.setVisibility(View.GONE);
                                add_back.setVisibility(View.VISIBLE);
                                mapSearchTxt.setText("");
                                LatLng mapCenter = mMap.getCameraPosition().target;
                                lat = mapCenter.latitude;
                                lng = mapCenter.longitude;
                                if (lat == null && lng == null) {
                                    Toast.makeText(shopping_list.this, "Location not set. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                add_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        frameLayout.setVisibility(View.GONE);
                        add_list_btn.setVisibility(View.VISIBLE);
                        textView4.setVisibility(View.VISIBLE);
                        back_btn.setVisibility(View.VISIBLE);
                        group_list.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

    }

    // ------------------------- Location ----------------------------------------------------------
    private Criteria criteria;
    public String bestProvider;
    public double latitude;
    public double longitude;
    public LocationManager locationManager;

    private void getLocation() {

        if (isLocationEnabled(shopping_list.this)) {
            locationManager = (LocationManager) shopping_list.this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isLocationEnabled(this);
                return;
            } else{
                locationManager.requestLocationUpdates(bestProvider, 1000, 1, this);
            }

            //You can still do this if you like, you might get lucky:
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                this.lat = latitude;
                this.lng = longitude;
            } else {
                //This is what you need:
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, (LocationListener) this);
            }
        } else {
            isLocationEnabled(shopping_list.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLocationEnabled(shopping_list.this)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isLocationEnabled(this);
                return;
            } else {
                try {
                    locationManager.requestLocationUpdates(bestProvider, 1000, 1, this);
                } catch (Exception e){
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates((LocationListener) this);

    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates((LocationListener) this);
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(this, "Please turn on GPS...", Toast.LENGTH_SHORT).show();
    }

    public boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new IOSDialog.Builder(shopping_list.this)
                    .setTitle("Enable Location")
                    .setMessage("Providing your location helps the donators to find you easily...")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return false;
        } else {
            return true;
        }
    }
    // ------------------------- End Location ----------------------------------------------------------

    // -------------------------- Map ---------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    private void mapMarker() {
        if(lat == null || lng == null){
            lat = mMap.getMyLocation().getLatitude();
            lng = mMap.getMyLocation().getLongitude();
        }
        LatLng loc;
        loc = new LatLng(lat, lng);
        try {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(loc).draggable(false));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

            // Enable the zoom controls for the map
            mMap.getUiSettings().setZoomControlsEnabled(true);
        } catch(Exception x){}
    }

    private void buildGoogleApiClient() {
    }
    // ------------------------------- Map -------------------------------------------------------


    @Override
    public void onBackPressed() {
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

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void loadData(){
        //---------------- Local DB ----------------------------------------------------------------
        try {
            Context context = appBase.getAppContext();
            SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
            Cursor resultSet = database.rawQuery("Select * from shopping_pins WHERE group_id=" + group_id + " ORDER BY id ASC;", null);
            resultSet.moveToFirst();
            shoppingList.clear();
            if (resultSet.getCount() != 0) {
                do {
                    shoppingList.add(new shopping_list_items(
                            resultSet.getInt(0),
                            resultSet.getString(5),
                            resultSet.getString(2),
                            resultSet.getString(4),
                            resultSet.getInt(1),
                            resultSet.getDouble(6),
                            resultSet.getDouble(7)
                    ));
                } while (resultSet.moveToNext());
            } else {
                new IOSDialog.Builder(shopping_list.this)
                        .setMessage("No list item found!")
                        .setNegativeButton("Yey...", null)
                        .show();
            }
        } catch (Exception x){}
        //---------------- End Local DB ------------------------------------------------------------
        recyclerView.removeAllViews();
        shopping_list_adapter adapter = new shopping_list_adapter(shopping_list.this, shoppingList, "");
        recyclerView.setAdapter(adapter);
        loading.setVisibility(View.INVISIBLE);

    }
}