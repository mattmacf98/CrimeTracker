package com.example.matthew.crimertracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    Location myLoc;
    FloatingActionButton fab;

    LocationManager locationManager;
    LocationListener locationListener;
    boolean callingEnabled = false;
    final OkHttpClient client = new OkHttpClient();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,locationListener);
                myLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateMap(myLoc);
            } else if (ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                callingEnabled = true;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Crime Tracker");
        setContentView(R.layout.activity_maps);

        //set up action bar
        //ActionBar actionBar = getSupportActionBar();
        ActionBar actionBar = getSupportActionBar();
        //inflate menu
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        View view = layoutInflater.inflate(R.layout.custom_menu, null);
        actionBar.setCustomView(view);

        android.support.v7.widget.Toolbar parent = (android.support.v7.widget.Toolbar) view.getParent();
        parent.setPadding(0,0,0,0);
        parent.setContentInsetsAbsolute(0,0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Method runs async task to fetch data and onPostExcecute of fetch
     * creates heatmap of data to overlay on global mMap for activity
     */
    public void addHeatMapOverlay() {
        // Create request for baltimore city data api endpoint
        String baltimoreURL = "http://data.baltimorecity.gov/resource/4ih5-d5d5.json";
        final Request request = new Request.Builder()
                .url(baltimoreURL)
                .build();

        // AsyncTask allows us to fetch data while maintaing main thread processes
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    // Get request using okhttp client
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        return null;
                    }
                    return response.body().string();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error fetching crime data", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    return null;
                }
            }

            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    try {
                        // Build collection of LatLng from crime data locations
                        JSONArray crimeDataJSON = new JSONArray(s);
                        ArrayList<LatLng> crimeDataLocations = parseLatLngfromCrimeJSON(crimeDataJSON);
                        // Build a heatmap provider using LatLng objects and naive clustering
                        mProvider = new HeatmapTileProvider.Builder().data(crimeDataLocations).build();
                        // Add heatmap as overlay to map
                        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

                    } catch (JSONException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error loading crime data", Toast.LENGTH_LONG).show();
                    }
                }
            }

        };

        asyncTask.execute();
    }

    /**
     *
     * @param crimeDataJSON - json array of crime data fetched from API
     * @return ArrayList<LatLng> containing LatLng objects of each crime fetched
     * @throws JSONException in cases when errors parsing json object (bad fetch)
     */
    private ArrayList<LatLng> parseLatLngfromCrimeJSON(JSONArray crimeDataJSON) throws JSONException {
        ArrayList<LatLng> crimesList = new ArrayList<>();
        for (int i = 0; i < crimeDataJSON.length(); i++) {
            JSONObject crimeJSON = crimeDataJSON.getJSONObject(i);
            if (crimeJSON.has("latitude") && crimeJSON.has("longitude")) {
                crimesList.add(new LatLng(crimeJSON.getDouble("latitude"),
                        crimeJSON.getDouble("longitude")));
            }
        }
        return crimesList;
    }

    private void setUpLocations() {
        //create location manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
                myLoc = location;
                Log.i("updateOnLocationChanged", "yes");
                updateMap(myLoc);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        //permission checks location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,locationListener);
            myLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateMap(myLoc);
        }
    }

    public void callCops(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel","911",null));
        call(intent);
    }

    public void callICE(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel","1234567",null));
        call(intent);
    }

    private void call(Intent intent) {
        //calling permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //asks for call permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},1);
        } else {
            callingEnabled = true;
        }
        if (callingEnabled) {
            startActivity(intent);
        }
    }

    public void launchCrimeList(View view) {
        Intent intent = new Intent(this, CrimeList.class);
        startActivity(intent);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpLocations();
        addHeatMapOverlay();
    }

    private void updateMap(Location myLocation) {
        //add marker where you are and center
        Log.i("note", "Asked for location");
        if (myLocation != null) {
            Log.i("location:", "doesn't  equal null");
            LatLng me = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(me).title("You are here."));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me,15));
        } else {
            Log.i("location", "equals null");
        }
    }

}
