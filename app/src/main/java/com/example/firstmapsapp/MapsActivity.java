package com.example.firstmapsapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private Marker ubicationMarker;
    private Marker marker;
    private AlertDialog dialog;
    private TextView infoTxt;
    private ImageButton addMarkerBTN;
    private ArrayList<CustomMarker> savedMarkers;
    private boolean primeraVez;

    Geocoder geocoder;
    List<Address> addresses;


    private Location myLocation;
    private Location markerLocation;
    private double distance;

    double lat = 0.0;
    double lng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addMarkerBTN = findViewById(R.id.addMarkerBTN);
        savedMarkers = new ArrayList<>();
        infoTxt = findViewById(R.id.infoTxt);
        primeraVez = true;

        addMarkerBTN.setOnClickListener(
                (view)->{
                    if(marker!=null){

                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
                        View mView = getLayoutInflater().inflate(R.layout.dialog_place,null);


                        mBuilder.setView(mView);
                        dialog = mBuilder.create();
                        dialog.show();

                        Button markerDialogBtn = mView.findViewById(R.id.markerDialogBtn);
                        EditText newMarkerTxt = mView.findViewById(R.id.newMarkerTxt);

                        markerDialogBtn.setOnClickListener(
                                (view2)->{


                                    CircleOptions circleArea = new CircleOptions();
                                    circleArea.center(new LatLng(marker.getPosition().latitude,marker.getPosition().longitude));
                                    circleArea.radius(5);

                                    savedMarkers.add(new CustomMarker(newMarkerTxt.getText().toString(),marker, circleArea));
                                    Toast.makeText(this,newMarkerTxt.getText().toString()+ "- Total markers: "+savedMarkers.size(),Toast.LENGTH_SHORT).show();

                                    LatLng delta = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                                    mMap.addMarker(new MarkerOptions().position(delta).draggable(true).title(newMarkerTxt.getText().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                                    dialog.hide();
                                    newMarkerTxt.setText("");
                                    findCloserMarker();
                                    marker.remove();

                                }
                        );






                    }

                }
        );


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
        myLocation = new Location("my position");
        markerLocation = new Location("marker location");
        geocoder = new Geocoder(this,Locale.getDefault());


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GetMyPosition();

        mMap.setOnMarkerDragListener(this);


        mMap.setOnMapClickListener(
                (point)->{

                    if(marker!=null){
                        marker.remove();
                    }


                    LatLng delta = new LatLng(point.latitude, point.longitude);


                    marker = mMap.addMarker(new MarkerOptions().position(delta).draggable(true).title("Marker new delta"));
                    markerLocation.setLatitude(marker.getPosition().latitude);
                    markerLocation.setLongitude(marker.getPosition().longitude);
                    marker.setSnippet("Distancia: "+ myLocation.distanceTo(markerLocation));
                    LatLng latLng = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                    marker.showInfoWindow();



                }
        );

    }

    private void addMarkerActual(double lat, double lng) {
        LatLng coords = new LatLng(lat, lng);

        CameraUpdate myPosition;

        if(primeraVez){
            myPosition= CameraUpdateFactory.newLatLngZoom(coords,15);
            primeraVez = false;
        }else{
            myPosition= CameraUpdateFactory.newLatLng(coords);
        }



        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()


        if (ubicationMarker != null) ubicationMarker.remove();

        ubicationMarker = mMap.addMarker(new MarkerOptions()
                .position(coords)
                .title("Posición Actual")
                .snippet("Dirección: "+ address)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.imhere)));
        mMap.animateCamera(myPosition);


    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void updatePosition(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            myLocation.setLatitude(lat);
            myLocation.setLongitude(lng);
            myLocation.setLongitude(lng);
            addMarkerActual(lat, lng);

            Log.e("updatePosition: ","Supuestamente creando el hilo");
            findCloserMarker();

        }
    }

    LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            updatePosition(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    private void GetMyPosition() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updatePosition(location);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,1,locListener);


    }




    private CustomMarker findCloserMarker(){

        CustomMarker closerMarker = null;

        if(savedMarkers != null && savedMarkers.size()!=0){

            float closerDistance = 1000000000;
            Marker temp;
            Location markerLocation = new Location("marker");
            Location positionLocation = new Location("position");



            for(CustomMarker markerTemp : savedMarkers){

                temp = markerTemp.getMarker();

                markerLocation.setLatitude(temp.getPosition().latitude);
                markerLocation.setLongitude(temp.getPosition().longitude);

                positionLocation.setLatitude(ubicationMarker.getPosition().latitude);
                positionLocation.setLongitude(ubicationMarker.getPosition().longitude);


                float distance = markerLocation.distanceTo(positionLocation);

                if(distance <=closerDistance){
                    closerDistance = distance;
                    closerMarker = markerTemp;
                }



            }

            if(closerDistance<=50){
                infoTxt.setText("Usted se encuentra en "+ closerMarker.getName());
            }else{
                infoTxt.setText("El lugar más cercano es: " +closerMarker.getName());
            }
        }



        return closerMarker;
    }


    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

        markerLocation.setLatitude(marker.getPosition().latitude);
        markerLocation.setLongitude(marker.getPosition().longitude);
        marker.setSnippet("Distancia: "+ myLocation.distanceTo(markerLocation));
        LatLng latLng = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
        marker.setPosition(latLng);
        marker.showInfoWindow();

        for(CustomMarker temp : savedMarkers){

            if(temp.getName().equals(marker.getTitle())){
                temp.setMarker(marker);
            }
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        findCloserMarker();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updatePosition(location);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,1,locListener);
            }
        }



    }
}
