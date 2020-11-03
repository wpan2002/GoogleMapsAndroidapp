package com.example.william.googlemapreference;

import com.example.william.googlemapreference.R;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements LoaderCallbacks<Cursor>, OnMapReadyCallback, OnMapClickListener {

    GoogleMap googleMap;
    final Context context = this;
    boolean markerClicked;
    SQLiteDatabase mydb;
    public String lastId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();


        } else { // Google Play Services are available

            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            // Getting GoogleMap object from the fragment
            fm.getMapAsync(this);

            // Enabling MyLocation Layer of Google Map
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(true);

            // Invoke LoaderCallbacks to retrieve and draw already saved locations in map
            getSupportLoaderManager().initLoader(0, null, this);


        }





//deletes all markers

        googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {

                // Removing all markers from the Google Map
                googleMap.clear();

                // Creating an instance of LocationDeleteTask
                LocationDeleteTask deleteTask = new LocationDeleteTask();

                // Deleting all the rows from SQLite database table
                deleteTask.execute();

                Toast.makeText(getBaseContext(), "All markers are removed", Toast.LENGTH_LONG).show();

            }
        });


    }


    private void drawMarker(LatLng point) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title("view notes");
        double lati = point.latitude;
        double lngi = point.longitude;

        String latiString = String.valueOf(lati);
        String lngiString = String.valueOf(lngi);
        //allows full string of decimals
        googleMap.addMarker(markerOptions);
        //wanted you to see what's really happening:
        mydb = openOrCreateDatabase("locationmarkersqlite", Context.MODE_PRIVATE, null);
        //we use rawQuery because we are selecting data. We need the id for this geo point
        Cursor cursor = mydb.rawQuery("select _id from locations where lat = '" + latiString + "' and lng = '" + lngiString + "'", null);
        if (cursor.moveToFirst()) {
            do {
                lastId = cursor.getString(0);//can be lastId = cursor.getString("_id");

            }
            while (cursor.moveToNext());//no need to continue loop..the points are never truly identical
        }

        cursor.close();


        //add a listener for every marker with the setOnInforWindowClickListener
        googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker marker) {
                //an intent to pass the last id to the ViewNotes sect
                Intent intentViewNotes = new Intent(MapsActivity.this, ViewNotes.class);
                Bundle d = new Bundle();
                d.putString("markerid", lastId);
                intentViewNotes.putExtras(d);
                startActivity(intentViewNotes);


            }
        });


    }

    @Override
    public void onMapReady(GoogleMap gMap) {

        googleMap = gMap;
        googleMap.setOnMapClickListener(this);

    }


    @Override
    public void onMapClick(final LatLng point) {


            // Drawing marker on the map
            drawMarker(point);

            // Creating an instance of ContentValues
            ContentValues contentValues = new ContentValues();

            // Setting latitude in ContentValues
            contentValues.put(LocationsDB.FIELD_LAT, point.latitude);
            // Setting longitude in ContentValues
            contentValues.put(LocationsDB.FIELD_LNG, point.longitude);

            // Setting zoom in ContentValues
            contentValues.put(LocationsDB.FIELD_ZOOM, googleMap.getCameraPosition().zoom);


            // Creating an instance of LocationInsertTask
            LocationInsertTask insertTask = new LocationInsertTask();

            // Storing the latitude, longitude and zoom level to SQLite database
            insertTask.execute(contentValues);

            //get last id here final says dialog can't be altered
            final Dialog dialog = new Dialog(context);
            //go for the right xml
            dialog.setContentView(R.layout.custom_dialogue);
            //self explanatory
            dialog.setTitle("Your Location has been saved");

            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.text);
            //talk to them?
            text.setText("Would you like to save a note or a photo?");
            ImageView image = (ImageView) dialog.findViewById(R.id.image);
            image.setImageResource(R.drawable.ic_launcher);
            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            Button dialogButtonNote = (Button) dialog.findViewById(R.id.dialogButtonNote);
            Button dialogButtonPhoto = (Button) dialog.findViewById(R.id.dialogButtonPhoto);


            //if the dismiss button is clicked
            dialogButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            //if the note button is clicked no Bundle because we are working with the last id
            dialogButtonNote.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentNote = new Intent(MapsActivity.this, LocationNotes.class);
                    startActivity(intentNote);
                }

            });

            //if the photo button is clicked no Bundle because we are working with the last id
            dialogButtonPhoto.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentPhoto = new Intent(MapsActivity.this, LocationPhoto.class);
                    startActivity(intentPhoto);
                }
            });

            dialog.show();

        }





    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void>{
        @Override
        protected Void doInBackground(ContentValues... contentValues) {


            getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]);
            return null;
        }
    }

    private class LocationDeleteTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {

            /** Deleting all the locations stored in SQLite database */
            getContentResolver().delete(LocationsContentProvider.CONTENT_URI, null, null);
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0,
                                         Bundle arg1) {

        // Uri to the content provider LocationsContentProvider
        Uri uri = LocationsContentProvider.CONTENT_URI;

        // Fetches all the rows from locations table
        return new CursorLoader(this, uri, null, null, null, null);

    }




    @Override
    public void onLoadFinished(Loader<Cursor> arg0,
                               Cursor arg1) {
        int locationCount = 0;
        double lat=0;
        double lng=0;
        float zoom=0;

        // Number of locations available in the SQLite database table
        locationCount = arg1.getCount();

        // Move the current record pointer to the first row of the table
        arg1.moveToFirst();

        for(int i=0;i<locationCount;i++){

            // Get the latitude

            lat = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LAT));

            // Get the longitude
            lng = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LNG));

            // Get the zoom level
            zoom = arg1.getFloat(arg1.getColumnIndex(LocationsDB.FIELD_ZOOM));

            // Creating an instance of LatLng to plot the location in Google Maps
            LatLng location = new LatLng(lat, lng);

            // Drawing the marker in the Google Maps
            drawMarker(location);

            // Traverse the pointer to the next row
            arg1.moveToNext();

        }

        if(locationCount>0){
            // Moving CameraPosition to last clicked position
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));

            // Setting the zoom level in the map on last position  is clicked
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));


        }





    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
    }
}
