package com.example.megamonsterbankapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    float zoom = 8.0F;
    private LatLng currentLocation;
    private LatLng destination;
    private Polyline mPolyline;
    LocationManager locationManager;
    ArrayList<LatLng> mMarkerPoints;
    SQLiteDatabase myDB;
    Cursor cursor;
    AlertDialog.Builder alertBox;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mMarkerPoints = new ArrayList<>();

        alertBox = new AlertDialog.Builder(this);
        alertBox.setTitle("ATM selector");
        alertBox.setMessage("You haven't selected ATM destination");
        alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        final Button b1 = (Button)findViewById(R.id.button1);

        b1.setOnClickListener(new View.OnClickListener()
                              {
                                  public void onClick(View view)
                                  {
                                      zoom --;
                                      mMap.moveCamera(CameraUpdateFactory.newLatLng(mMap.getCameraPosition().target));
                                      mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 500, null);
                                  }
                              }
        );


        final Button b2 = (Button)findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener()
                              {
                                  public void onClick(View view)
                                  {
                                      zoom ++;
                                      mMap.moveCamera(CameraUpdateFactory.newLatLng(mMap.getCameraPosition().target));
                                      mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 500, null);
                                  }
                              }
        );

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria ();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            String lp = locationManager.getBestProvider(criteria, true);

            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            }

            if (lp != null)
            {
                locationManager.requestLocationUpdates(lp, 5000, 0.0f, this);
                Log.i("GPS", "GOT GPS");
            }
            else
            {
                Log.i("GPS", "NO GPS");
            }

        }
        catch(SecurityException e) {
            e.printStackTrace();
        }

        final Button b = (Button)findViewById(R.id.getDirectionBtn);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(destination==null){
                    alertBox.show();
                }else {
                    drawRoute();
                }
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

        File dbFile = new File(getFilesDir()+ "/myDB.db");
        myDB = SQLiteDatabase.openOrCreateDatabase(getFilesDir()+ "/myDB.db", null);

        if(!dbFile.exists()){
            Log.i("SQLiteExample", "file doesnt exist");

            myDB.execSQL("create table atmlocations(id INTEGER PRIMARY KEY AUTOINCREMENT, title text, latitude float, longitude float)");
            myDB.execSQL("create table loginDetails (id INTEGER PRIMARY KEY AUTOINCREMENT, email text, password text)");


            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM1','52.0431','-0.7571')");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM2','52.7381','-0.9071')");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM3','52.9498','-0.8365')");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM4','52.3409','-1.2541')");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM5','52.0431','-0.9120')");

        }else{
            Log.i("SqlLiteExample", "File exist");

        }


        cursor = myDB.rawQuery("select * from atmlocations", null);

        int atmTitle = cursor.getColumnIndex("title");
        int lat = cursor.getColumnIndex("latitude");
        int longi = cursor.getColumnIndex("longitude");

        if(cursor.moveToFirst()){
            do{
                LatLng atm = new LatLng(cursor.getFloat(lat), cursor.getFloat(longi));
                mMap.addMarker(new MarkerOptions().position(atm).title(cursor.getString(atmTitle)));
            }while(cursor.moveToNext());
        }

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(atm1));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(52.3, -0.9)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 500, null);

        Log.i("Map Ready", "Marker Added");
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        destination = marker.getPosition();

        Log.i("MarkerClick", destination.toString());
        return false;
    }


    public void onLocationChanged(Location location) {
        double altitude = location.getAltitude();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String s = "Alt:" + altitude + " Lat:" + latitude + " Long:" + longitude;
        Log.i("GPS", s);
        currentLocation = new LatLng(latitude, longitude);
    }

    private void drawRoute(){

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(currentLocation, destination);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception on download", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask","DownloadTask : " + data);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if(mPolyline != null){
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);

            }else
                Toast.makeText(getApplicationContext(),"No route is found", Toast.LENGTH_LONG).show();
        }
    }
}