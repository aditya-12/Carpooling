package com.example.carpooling;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;



import com.example.carpooling.adapter.PlaceAutoSuggestAdapter;

import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

public class Home<Private> extends AppCompatActivity implements OnMapReadyCallback {


    //Google map Variables
    private GoogleMap mGoogleMap;
    private GeoApiContext mGeoApicontext=null;


    Location mlocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    //FireBase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;


    //Widgets
    private AutoCompleteTextView destinationTextview, locationTextView;
    private Button mSearchBtn, mSwitchTextBtn;
    private RadioButton findButton, offerButton;
    private RadioGroup mRideSelectionRadioGroup;
    private BottomNavigationView bottomNavigationView;
    private ImageView mLocationBtn;
    private Fragment frag_map;
    private EditText editDescription;

    //Variables
    private static final int Request_Code = 1001;
    String TAG="HomeActivity";
    String url;
    double Origin_latittude,Origin_longitude,Destination_lattitude,Destination_logitude;

    //Google Map functions
    LatLng latLngaddress, des_latlng;

    LatLngBounds.Builder builder;
    CameraUpdate cameraUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        destinationTextview=findViewById(R.id.destinationTextview);
        locationTextView=findViewById(R.id.locationTextview);
        mSearchBtn=findViewById(R.id.searchBtn);
        mSwitchTextBtn=findViewById(R.id.switchTextBtn);
        mRideSelectionRadioGroup=findViewById(R.id.toggle);
        editDescription=findViewById(R.id.edittext_Enter_Description);
        bottomNavigationView = findViewById(R.id.bottomNavViewBar);



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        GetlastLocation();

        locationTextView.setAdapter(new PlaceAutoSuggestAdapter(Home.this,android.R.layout.simple_list_item_1));
        destinationTextview.setAdapter(new PlaceAutoSuggestAdapter(Home.this,android.R.layout.simple_list_item_1));






        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_rides:
                        startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
                        return true;
                    case R.id.menu_booked:
                        startActivity(new Intent(getApplicationContext(), BookingActivity.class));
                        return true;
                    case R.id.menu_account:
                        startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });
    }


    private LatLng getLatLngFromAddress(String address) {
        Geocoder geocoder=new Geocoder(Home.this);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if(addressList!=null){
                Address singleaddress=addressList.get(0);
                LatLng latLng=new LatLng(singleaddress.getLatitude(),singleaddress.getLongitude());
                return latLng;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    private Address getAddressFromLatLng(LatLng latLng){
        Geocoder geocoder=new Geocoder(Home.this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if(addresses!=null){
                Address address=addresses.get(0);
                return address;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



    private void GetlastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.ACCESS_FINE_LOCATION}, Request_Code);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mlocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(Home.this);
                }
            }

        });
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {


        final List<MarkerOptions> markerList=new ArrayList<MarkerOptions>();
        final LatLng latLng = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
        final MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("your location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(markerOptions);


        //FROM LOCATION AUTOCOMPLETETEXTVIEW
        destinationTextview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                googleMap.clear();
                Log.d("FROM Address : ",destinationTextview.getText().toString());
                latLngaddress=getLatLngFromAddress(destinationTextview.getText().toString());
                if(latLngaddress!=null) {
                    Log.d("Lat Lng : ", " " + latLngaddress.latitude + " " + latLngaddress.longitude);
                    Origin_latittude=latLngaddress.latitude;
                    Origin_longitude=latLngaddress.longitude;
                    Address address=getAddressFromLatLng(latLngaddress);
                    if(address!=null) {
                        Log.d("FROM Address : ", "" + address.toString());
                        Log.d("FROM Address Line : ",""+address.getAddressLine(0));
                        Log.d("FROM ADDRESS Phone : ",""+address.getPhone());
                        Log.d("FROM addr Pin Code : ",""+address.getPostalCode());
                        Log.d("FROM ADDRESS Feature : ",""+address.getFeatureName());
                        Log.d("FROM ADDRESS More : ",""+address.getLocality());
                        MarkerOptions markerOptionsaddress=new MarkerOptions().position(latLngaddress).title("YOUR LOCATION");
                        markerOptionsaddress.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLngaddress));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngaddress,15));
                        googleMap.addMarker(markerOptionsaddress);
                        markerList.add(markerOptionsaddress);

                    }
                    else {
                        Log.d("Adddress","Address Not Found");
                    }
                }
                else {
                    Log.d("Lat Lng","Lat Lng Not Found");
                }
            }
        });

        //Destination AUTOCOMPLETETEXTVIEW
        locationTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("DESTINATION Address : ",locationTextView.getText().toString());
                des_latlng=getLatLngFromAddress(locationTextView.getText().toString());
                if(des_latlng!=null) {
                    Log.d("Lat Lng : ", " " + des_latlng.latitude + " " + des_latlng.longitude);
                    Address address=getAddressFromLatLng(des_latlng);
                    Destination_lattitude=des_latlng.latitude;
                    Destination_logitude=des_latlng.longitude;
                    if(address!=null) {
                        Log.d("DESTINATION Address : ", "" + address.toString());
                        Log.d("DESTINATION Add Line:",""+address.getAddressLine(0));
                        Log.d("DESTINATION Phone : ",""+address.getPhone());
                        Log.d("DESTINATION Pin Code : ",""+address.getPostalCode());
                        Log.d("DESTINATION Feature : ",""+address.getFeatureName());
                        Log.d("DESTINATION More : ",""+address.getLocality());
                        MarkerOptions destinationmarker = new MarkerOptions().position(des_latlng).title("Destination");
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(des_latlng));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(des_latlng, 15));
                        googleMap.addMarker(destinationmarker);
                        markerList.add(destinationmarker);
                        builder = new LatLngBounds.Builder();
                        for(MarkerOptions m:markerList)
                        {
                            builder.include(m.getPosition());
                        }
                        /**initialize the padding for map boundary*/
                        int padding = 50;
                        /**create the bounds from latlngBuilder to set into map camera*/
                        LatLngBounds bounds = builder.build();
                        /**create the camera with bounds and padding to set into map*/
                        cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                googleMap.animateCamera(cameraUpdate);

                            }

                        });




                    }
                    else {
                        Log.d("Adddress","Address Not Found");
                    }
                }
                else {
                    Log.d("Lat Lng","Lat Lng Not Found");
                }
            }


        });
        /*url="https://maps.googleapis.com/maps/api/directions/json?origin="+Origin_latittude+","+Origin_longitude+"&destination="+Destination_lattitude+","
                +Destination_logitude+"&mode driving"
                +"&key="+getString(R.string.google_maps_api_key);*/

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Request_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GetlastLocation();
                }
                break;
            default:
                Log.d(String.valueOf(Home.class), "onRequestPermissionsResult: failed");
        }
    }


}