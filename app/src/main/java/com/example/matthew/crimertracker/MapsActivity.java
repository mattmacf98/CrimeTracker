package com.example.matthew.crimertracker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private Marker myMarker;
    private ArrayList<LatLng> crimeDataLocations;
    private ArrayList<Marker> crimePins;
    private boolean pinsVisible = false;
    private static final int unique_id = 457126;
    NotificationCompat.Builder notification;
    Location myLoc;
    FloatingActionButton fab;
    SharedPreferences sp;

    LocationManager locationManager;
    LocationListener locationListener;
    boolean callingEnabled = false;
    final OkHttpClient client = new OkHttpClient();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10,5,locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10,5,locationListener);
                myLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                setUpLocations();
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

        //set up notification
        notification = new NotificationCompat.Builder(this, "MY_CHANNEL_ID");

        //initialize crime pins
        crimePins = new ArrayList<>();

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
        String baseURL = "http://data.baltimorecity.gov/resource/4ih5-d5d5.json";
        // String baltimoreURL = baltimoreURL + "?$where=crimedate between " + timeStamp + " and " +
        String baltimoreURL = baseURL + "?$limit=2500&$where=crimedate between '2018' and '2019'";
        Log.d("url", baltimoreURL);
        final Request request2018 = new Request.Builder()
                .url(baltimoreURL)
                .build();

        // AsyncTask allows us to fetch data while maintaing main thread processes
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    // Get request using okhttp client
                    Response response2018 = client.newCall(request2018).execute();
                    if (!response2018.isSuccessful()) {
                        return null;
                    }
                    String crimes2018 = response2018.body().string();
                    return crimes2018;
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
                        crimeDataLocations = parseLatLngfromCrimeJSON(crimeDataJSON);
                        // Build a heatmap provider using LatLng objects and naive clustering
                        mProvider = new HeatmapTileProvider.Builder().data(crimeDataLocations).build();
                        // Add heatmap as overlay to map
                        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                        setUpZoomListener();

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
        Log.d("Num crimes", Integer.toString(crimeDataJSON.length()));
        ArrayList<LatLng> crimesList = new ArrayList<>();
        for (int i = 0; i < crimeDataJSON.length(); i++) {
            JSONObject crimeJSON = crimeDataJSON.getJSONObject(i);
            if (crimeJSON.has("latitude") && crimeJSON.has("longitude")) {
                LatLng crimeLoc = new LatLng(crimeJSON.getDouble("latitude"),
                        crimeJSON.getDouble("longitude"));
                crimesList.add(crimeLoc);
                String snippetText = crimeJSON.get("crimedate").toString().split("T")[0];
                if (crimeJSON.has("neighborhood")) {
                    snippetText = snippetText + "\n" +
                            "Neighborhood: " + crimeJSON.get("neighborhood").toString();
                }
                if (crimeJSON.has("weapon")) {
                    snippetText = snippetText + "\n" +
                            "Weapon: " + crimeJSON.get("weapon").toString();
                }
                Marker tempPin = mMap.addMarker(new MarkerOptions()
                        .position(crimeLoc)
                        .title(crimeJSON.get("description").toString())
                        .snippet(snippetText)
                        .visible(false));
                crimePins.add(tempPin);
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
                //updateMap(myLoc);
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10,5,locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10,5,locationListener);
            myLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            if (myLoc != null) {
                LatLng me = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me,13));
            }
        }
    }

    public void callCops(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel","911",null));
        call(intent);
    }

    public void callICE(View view) {
        sp = getSharedPreferences("settings", 0);
        String ICE = sp.getString("ICE_Number","");
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",ICE,null));
        call(intent);
    }

    public void goToSettings(View view) {
        //put in here until we have the intensities from the data
        notification.setSmallIcon(R.drawable.gear).setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notification.setContentTitle("DANGEROUS NEIGHBORHOOD").setContentText("You have entered a dangerous neighborhood");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MY_CHANNEL_ID","crimeTracker", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notification for crimeTracker: entered a bad neighborhood");

            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManager nm =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(unique_id,notification.build());

        Intent intent = new Intent(this,SettingsPageActivity.class);
        startActivity(intent);
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

    private void setupInfoWindowAdapter() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
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
        mMap.setMinZoomPreference(11);
        setUpLocations();
        addHeatMapOverlay();
        setupInfoWindowAdapter();
    }

    private void updateMap(Location myLocation) {
        //add marker where you are and center
        Log.i("note", "Asked for location");
        if (myLocation != null) {
            Log.i("location:", "doesn't  equal null");
            LatLng me = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //remove old marker
            if (myMarker != null) {
                myMarker.remove();
            } else {
                //only center if no pin to begin ie first time getting location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me,13));
            }
            myMarker = mMap.addMarker(new MarkerOptions().position(me).title("You are here."));
        } else {
            Log.i("location", "equals null");
        }
    }

    private void setUpZoomListener() {
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition position = mMap.getCameraPosition();
                if(pinsVisible && position.zoom <= 16) {
                    Log.i("zoom is:",Float.toString(position.zoom)+ " pins invisible");
                    adjustPinVisibility(false);
                    pinsVisible = false;
                } else if (!pinsVisible && position.zoom > 16) {
                    Log.i("zoom is:",Float.toString(position.zoom)+ " pins visible");
                    adjustPinVisibility(true);
                    pinsVisible = true;
                }
            }
        });
    }

    private void adjustPinVisibility(boolean visibility) {
        for (int i = 0; i < crimePins.size(); i++) {
            crimePins.get(i).setVisible(visibility);
        }
    }

}
