package com.mapalarm.android_911.mps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    TextView latitudeText;
    TextView longitudeText;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Double myLatitude;
    private Double myLongitude;
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 102;
    private boolean permissionIsGranted = false;
    Button settingButton;


    float[] distance = new float[2];


    private MediaPlayer mediaPlayer;
    private Place place;
    private CircleOptions[] circleOptions;
    private LatLng point;
    private CircleOptions[] circleOptions1;
    private CircleOptions circleOptions2;


    private void drawCircle(LatLng point){

        // Instantiating CircleOptions to draw a circle around the marker
        circleOptions2 = new CircleOptions();


        // Specifying the center of the circle
        circleOptions2.center(point);

        // Radius of the circle
        circleOptions2.radius(1000.0);

        // Border color of the circle
        circleOptions2.strokeColor(0xffff0000);
        // Fill color of the circle
        circleOptions2.fillColor(0x44ff0000);

        // Border width of the circle
        circleOptions2.strokeWidth(2);

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions2);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);


        latitudeText = (TextView) findViewById(R.id.tvLatitude);
        longitudeText = (TextView) findViewById(R.id.tvLongitude);
        settingButton = (Button) findViewById(R.id.sbutton);


        mediaPlayer = MediaPlayer.create(this, RingtoneManager.getValidRingtoneUri(this));
        mediaPlayer.setLooping(true);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(15 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        settingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
                } else {
                    permissionIsGranted = true;

                }

                latitudeText.setText("Latitude:");
                longitudeText.setText("Longitude:");
            }
        });


        final PlaceAutocompleteFragment places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                MapsActivity.this.place = place;
                Toast.makeText(getApplicationContext(), place.getName(), Toast.LENGTH_SHORT).show();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12));
                mMap.clear();               //Clears the map before adding another Marker
                MarkerOptions marker = new MarkerOptions().position(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude)).title("Destination");
                mMap.addMarker(marker);

                drawCircle(marker.getPosition());
            }


            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else
            {
                permissionIsGranted = true;

            }
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener((GoogleMap.OnMapClickListener) this);
    }




    @Override
    public void onMapClick (LatLng point) {

        this.point = point;

        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(point.latitude, point.longitude)).title("Destination");
        mMap.clear();
        drawCircle(marker.getPosition());
        mMap.addMarker(marker);

    }



        @Override
        public void onLocationChanged (Location location){
            myLatitude = location.getLatitude();
            myLongitude = location.getLongitude();
            latitudeText.setText("Latitude : " + String.valueOf(myLatitude));
            longitudeText.setText("Longitude : " + String.valueOf(myLongitude));

           if (place != null){
                Location.distanceBetween(myLatitude, myLongitude, place.getLatLng().latitude, place.getLatLng().longitude, distance);

                if (distance[0] >= circleOptions2.getRadius()) {
                   // Toast.makeText(getBaseContext(), " PLACE You are not in the circle", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "You are going to reach your destination.", Toast.LENGTH_LONG).show();


                    if (mediaPlayer.isPlaying()) {
                        return;
                    } else {
                        mediaPlayer.start();
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);


                        alert.setCancelable(false).setMessage("You Are Going To Reach Your Destination").setPositiveButton("STOP ALARM                           ", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mediaPlayer.stop();
                                mediaPlayer.prepareAsync();
                                mediaPlayer.seekTo(0);
                                dialog.cancel();
                                mMap.clear();
                                place = null;
                            }
                        });

                        AlertDialog dialog = alert.create();
                        dialog.show();
                    }
                }
            }



            if(point != null) {
                //place = null;
                Location.distanceBetween(myLatitude, myLongitude,
                        point.latitude, point.longitude, distance);

                if (distance[0] >= circleOptions2.getRadius()) {
                  // Toast.makeText(getBaseContext(), "POINTYou are not in the circle", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "You are going to reach your destination.", Toast.LENGTH_LONG).show();


                    if (mediaPlayer.isPlaying()) {
                        return;
                    } else {
                        mediaPlayer.start();
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);


                        alert.setCancelable(false).setMessage("You Are Going To Reach Your Destination").setPositiveButton("STOP ALARM                           ", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mediaPlayer.stop();
                                mediaPlayer.prepareAsync();
                                mediaPlayer.seekTo(0);
                                dialog.cancel();
                                mMap.clear();
                                point = null;
                            }
                        });

                        AlertDialog dialog = alert.create();
                        dialog.show();
                    }
                }
            }
        }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();

    }

    private void requestLocationUpdates() {
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
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onStart()
    {
        super.onStart();
        googleApiClient.connect();
    }

    protected void onResume()
    {
        super.onResume();
        if(permissionIsGranted) {
            if (googleApiClient.isConnected()) {
                requestLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {


        super.onPause();
        if (permissionIsGranted) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (permissionIsGranted) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSION_REQUEST_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permissions granted
                    permissionIsGranted = true;
                }  else {
                    //permission denied
                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(),"This app requires location permission to be granted", Toast.LENGTH_SHORT).show();
                    latitudeText.setText("Latitude : Permission is not granted");
                    longitudeText.setText("Longitude : Permission is not granted");
                }
                break;
            case MY_PERMISSION_REQUEST_COARSE_LOCATION:
                break;
        }
    }
}
