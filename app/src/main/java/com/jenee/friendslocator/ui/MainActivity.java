package com.jenee.friendslocator.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jenee.friendslocator.R;
import com.jenee.friendslocator.data.DatabaseHelper;
import com.jenee.friendslocator.dialogWindow.MyDialogWindow;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);

        WelcomeActivity.WelcomeStart = true;

        //check that device has google play service
        if (googleServiceAvailable()) {
            //Toast.makeText(this, "Welcome", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_main);
            initMap();
        }
    }

    private void initMap() {
        //use map fragment to handle map lifecycle automatically
        MapFragment mapFragmanet = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
       //Automatically initialize the map system and view
        mapFragmanet.getMapAsync(this);

    }

    public boolean googleServiceAvailable() {

        //helper calss to verifying that the Google Play services APK is available and up-to-date on this device
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to play service", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if(mGoogleMap!=null)
        {
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    // class to handling geocoding and reverse geocoding
                    Geocoder geocoder;
                    //Address list to store list of Addresses
                    List<Address> addresses = null;
                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    //get data from the database by the id to set data to the InfoWindow
                    Cursor cur  = databaseHelper.getFriendById(Integer.valueOf(marker.getSnippet()));
                    //move to the first raw data
                    cur.moveToNext();

                    //set layout elements
                    View v = getLayoutInflater().inflate(R.layout.listview,null);
                    TextView tvName = (TextView)v.findViewById(R.id.tv_locality);
                    TextView tvAddress = (TextView)v.findViewById(R.id.tv_address);
                    TextView tvPhone = (TextView)v.findViewById(R.id.tv_phone);
                    ImageView img = (ImageView) v.findViewById(R.id.imageView1);

                    //get the marker's location and use LatLng class to get Latitude and Longitude
                    LatLng ll = marker.getPosition();
                    //get firstname and lastname from the raw data and set that to textview
                    tvName.setText(cur.getString(1)+" "+cur.getString(2));

                    //assign  the image blob to byte array
                    byte[] byteArray = cur.getBlob(7);
                    //decode the image byte array to the mitmap
                    Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0 ,byteArray.length);
                    //set the bitmap to the image view
                    img.setImageBitmap(bm);

                    try {
                        //get the address using latitude and longitude and assign that to address list
                        addresses = geocoder.getFromLocation(ll.latitude, ll.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //get full adress from the addresses list
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                    //set textviews with values
                    tvAddress.setText(address);
                    tvPhone.setText(String.valueOf(cur.getString(3)));

                    return v;
                }
            });
        }

        //hardcoded latitude and logitude for initial map view
        goToLocationZoom(6.947589, 79.863359,7);

        //set the google api client with Location service API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //set hiegh and the width for the marker
        int height = 150;
        int width = 150;
        //get stored image and set that to Bitmap object
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.user_grey);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        MarkerOptions options;
        // get all friends data
        Cursor res = databaseHelper.getAllData();

        double lat,lon;

        //go through all data and add marker to the map
        while(res.moveToNext())
        {
            String title = res.getString(1)+" "+res.getString(2);
            lat = res.getDouble(5);
            lon = res.getDouble(6);
            options = new MarkerOptions()
                    .title(title)
                    .position(new LatLng(lat, lon))
                    //set Id as snippet to identify each friend loacation uniquely
                    .snippet(String.valueOf(res.getInt(0)))
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

            mGoogleMap.addMarker(options);
        }
    }

    //view map location with a zoom
    private void goToLocationZoom(double lat, double lon, float zoom) {
        LatLng ll = new LatLng(lat, lon);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }

    Marker marker;

    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.searchEditText);
        String location = et.getText().toString();


        //show error dialog when search by typing nothing
        if(location.isEmpty())
        {
            MyDialogWindow.showMessage(this,"Error","Enter a Location to Search");
            return;
        }

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);

        //if the result address list is null that means there is no address or location with the serach word
        // prompt user to search by valid address
        if(list.isEmpty())
        {
            MyDialogWindow.showMessage(this,"Error","Enter a valid Location");
            return ;
        }


        Address address = list.get(0);
        String locality = address.getLocality();

        //Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();
        double lat = address.getLatitude();
        double lon = address.getLongitude();
        goToLocationZoom(lat, lon, 13);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //handle menu item selection
        switch (item.getItemId()) {
            case R.id.mapTypeNormal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    LocationRequest mLocationRequest;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location==null)
        {
            Toast.makeText(this,"Can't get current location",Toast.LENGTH_LONG);
        }
        else{
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll,15);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }

    // Redirect to next activity
    public void navigateToFriendsDetails(View view)
    {
        Intent i = new Intent(this,FriendsDetailActivity.class);
        startActivity(i);
    }

    //ask user to sure that he/she want to exit the app
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this,R.style.MyDialogTheme)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                        WelcomeActivity.WelcomeStart = false;
                        finish();
                    }
                }).create().show();
    }




}
