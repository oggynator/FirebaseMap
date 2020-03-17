package com.example.mapfirebase;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.example.mapfirebase.model.MyLocation;
import com.example.mapfirebase.repo.FirebaseRepo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationListener listener;  // listens for location updates
    LocationManager manager;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        builder = new AlertDialog.Builder(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE); // gets from device
        createListener();
        // handle permissions
        // check first, if we have already the permission
        handlePermissionUpdate();
    }

    private void handlePermissionUpdate() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //ask for p.
        }else {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener); // start listening to updates
        }
    }

    private void createListener() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //  Log.i("all", "new location " + location);
                // move (map) camera to this location
                addMarker(location.getLatitude(), location.getLongitude());
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            handlePermissionUpdate();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // wait to start Firebase listener, until we have a map!
        FirebaseRepo.setMapsActivity(this);  // this will start the FB listener
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {

                builder.setTitle("New marker");
                final EditText input = new EditText(MapsActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double lat = latLng.latitude;
                        double lon = latLng.longitude;
                        String title = input.getText().toString();
                        FirebaseRepo.addMarker(title, lat, lon);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }


        });

    }

    private void addMarker(double lat, double lon) {
        LatLng marker = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(marker).title("Marker"));
    }

    public void updateMarkers(){  // call this method from FirebaseRepo, when the listener gets data.
        mMap.clear();
        for(MyLocation location : FirebaseRepo.locations){
            addMarker(location.getLat(), location.getLon());
        }
    }
}