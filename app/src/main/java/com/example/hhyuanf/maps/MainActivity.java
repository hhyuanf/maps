package com.example.hhyuanf.maps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.location.Geocoder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.Intent;
import java.util.List;

import java.io.IOException;

public class MainActivity extends Activity {


    private static final String LATITUDE = "LATITUDE";
    private static final String LONGITUDE = "LONGITUDE";

//    private boolean alarm;
//    private boolean set = false;
    private LocationManager locManager;
    private EditText latEditText;
    private EditText longEditText;
    private EditText address;

    private Button startService;
    private Button go;
    private Button search;

    public int selectionPosition;
    public List<Address> addresses;
//    public Context context;

//    MyLocListener myLocListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Constants.prefs = this.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        Constants.alarm = false;
        startService(new Intent(this, LocationService.class));
//        context = this;
        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        myLocListener = new MyLocListener();
//        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, myLocListener);

        latEditText = (EditText) findViewById(R.id.latitude);
        longEditText = (EditText) findViewById(R.id.longitude);
        address = (EditText) findViewById(R.id.address);

        startService = (Button) findViewById(R.id.start_service);
        go = (Button) findViewById(R.id.go);
        search = (Button) findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int add = search();
                if (add == 1) {
                    address.setText("No address found", TextView.BufferType.EDITABLE);
                }
                else if (add == 2) {
                    address.setText("error", TextView.BufferType.EDITABLE);
                }
//                address.setText(add, TextView.BufferType.EDITABLE);
            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.set = true;
                hideSoftKeyboard();
                go();
            }
        });


        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.set = true;
                hideSoftKeyboard();
                proximityAlert();
            }
        });
    }

    public void go() {
        saveCoordinates((float) addresses.get(selectionPosition).getLatitude(), (float) addresses.get(selectionPosition).getLongitude());
//        Location location = retrieveLoc();
        Constants.alarm = false;
    }

    public int search() {
        if (address.getText().toString().matches("")) {
            Toast.makeText(this, "Address?", Toast.LENGTH_LONG).show();
            return -1;
        }
        Geocoder geocoder = new Geocoder(getApplicationContext());
        try {
            addresses = geocoder.getFromLocationName(address.getText().toString(), 5);
            if (addresses != null && addresses.size() > 0) {
                final String[] popUpContents = new String[addresses.size()];
                for (int i = 0; i < addresses.size(); i++) {
                    popUpContents[i] = String.format("%s, %s, %s", addresses.get(i).getMaxAddressLineIndex() > 0 ? addresses.get(i).getAddressLine(0) : "", addresses.get(i).getLocality(), addresses.get(i).getCountryName());
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select one address").setSingleChoiceItems(popUpContents, 0, null).setPositiveButton("select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        selectionPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        address.setText(popUpContents[selectionPosition], TextView.BufferType.EDITABLE);
//                        Constants.latitude = addresses.get(selectionPosition).getLatitude();
//                        Constants.longitude = addresses.get(selectionPosition).getLongitude();
//                        saveCoordinates((float) addresses.get(selectionPosition).getLatitude(), (float) addresses.get(selectionPosition).getLongitude());
                    }
                }).show();
                return 0;
            }
            return 1;
        }catch (IOException io) {
            io.printStackTrace();
            return 2;
        }

    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void proximityAlert() {
        Constants.alarm = false;
        double latitude;
        double longitude;
        if (latEditText.getText().toString().matches("") || longEditText.getText().toString().matches("")) {
            Toast.makeText(this, "No input", Toast.LENGTH_LONG).show();
            return;
        }
        latitude =  Double.parseDouble(latEditText.getText().toString());
        longitude = Double.parseDouble(longEditText.getText().toString());



        Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null) {
            Toast.makeText(this, "Can't get your location.", Toast.LENGTH_LONG).show();
            return;
        }
        System.out.println(location.getLatitude());
        System.out.println(location.getLongitude());

        if (latitude == retrieveLoc().getLatitude() && longitude == retrieveLoc().getLongitude()) {
            Toast.makeText(this, "Same location", Toast.LENGTH_LONG).show();
            return;
        }
        saveCoordinates((float)latitude, (float)longitude);
    }


    public void saveCoordinates(float latitude, float longitude) {
//        prefs = this.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor =Constants.prefs.edit();
        prefEditor.putFloat(LATITUDE, latitude);
        prefEditor.putFloat(LONGITUDE, longitude);
        prefEditor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Location retrieveLoc() {
//        prefs = this.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        Location location = new Location("Des");
        location.setLatitude(Constants.prefs.getFloat(LATITUDE, 0));
        location.setLongitude(Constants.prefs.getFloat(LONGITUDE, 0));
        return location;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(proIntentReceiver);
//        locManager.removeProximityAlert(proxIntent);
    }
}
