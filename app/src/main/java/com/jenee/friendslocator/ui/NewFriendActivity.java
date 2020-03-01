package com.jenee.friendslocator.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class NewFriendActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Marker marker;
    DatabaseHelper databaseHelper;

    final int REQUEST_CODE_GALLERY = 999;


    EditText editFirstName,editLastName,editPhone,editLocation;
    Button addBtn;
    ImageView imageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        initMap();
        databaseHelper = new DatabaseHelper(this);

        editFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editLastName = (EditText) findViewById(R.id.editTextLastName);
        editPhone = (EditText) findViewById(R.id.editTextPhoneNumber);
        editLocation = (EditText) findViewById(R.id.editTextLocation);

        addBtn = (Button) findViewById(R.id.AddNewButton);
        imageView = (ImageView) findViewById(R.id.imageView5);

        uploadImage();
    }

    public void uploadImage(){
        imageView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //request to read storage
                        ActivityCompat.requestPermissions(NewFriendActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_GALLERY);


                    }
                }
        );
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY)
        {
            //if permission granted then open the gallery to pick image
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else{
                Toast.makeText(getApplicationContext(),"You don't have permission to access files",Toast.LENGTH_LONG).show();
            }

            return ;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //after selecting the image , convert that to bitmap and load that in imageview
        if(requestCode==REQUEST_CODE_GALLERY && resultCode==RESULT_OK && data !=null)
        {
            Uri uri = data.getData();
            try {
                Bitmap bm = decodeBitmap(uri,getApplicationContext());
                imageView.setImageBitmap(bm);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //method to decode the bitmap that fit in the imageview
    public static Bitmap decodeBitmap(Uri selectedImage, Context context)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(selectedImage), null, o2);
    }

    public void addData(View view) {

        //fields validation
        if(TextUtils.isEmpty(editFirstName.getText().toString()))
        {
            MyDialogWindow.showMessage(this,"ERROR","Enter First Name");
            return;
        }
        if(!isValidName(editFirstName.getText().toString())){
            MyDialogWindow.showMessage(this,"ERROR","First Name not valid");
            return;

        }
        if(TextUtils.isEmpty(editLastName.getText().toString()))
        {
            MyDialogWindow.showMessage(this,"ERROR","Enter Last Name");
            return;
        }

        if (!isValidName(editLastName.getText().toString()))
        {
            MyDialogWindow.showMessage(this,"ERROR","Last Name is not valid");
            return;

        }

        if(TextUtils.isEmpty(editLocation.getText().toString())){
            MyDialogWindow.showMessage(this,"ERROR","Enter and Search your friends location");
            return;

        }

        if (editPhone.getText().toString().isEmpty())
        {
            MyDialogWindow.showMessage(this,"ERROR","Phone Number is empty");
            return;

        }
        if(!isValidPhone(editPhone.getText().toString()))
        {
            MyDialogWindow.showMessage(this,"ERROR","Phone number is not valid");
            return ;
        }

        double lat,lon;

        LatLng ll = marker.getPosition();

        lat = ll.latitude;
        lon = ll.longitude;

        ImageView imageView = (ImageView) findViewById(R.id.imageView5);
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInByte = baos.toByteArray();


        //insert the data in to the db
        boolean isInserted =   databaseHelper.insertData(editFirstName.getText().toString(),editLastName.getText().toString(),editPhone.getText().toString(),editLocation.getText().toString(),lat,lon,imageInByte);

        if(isInserted==true)
        {
            Toast.makeText(getApplicationContext(),"Friend Added",Toast.LENGTH_LONG).show();
            //Intent i = new Intent(this,MainActivity.class);
            Intent newIntent = new Intent(NewFriendActivity.this,MainActivity.class);
            //remove all top flags to avoid activity repeatation and redirect to friends detail activity
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(newIntent);
        }
        else{
            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
        }
    }

    private void initMap() {
        MapFragment mapFragmanet = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment2);
        mapFragmanet.getMapAsync(this);
    }

    private void goToLocationZoom(double lat, double lon, float zoom) {
        LatLng ll = new LatLng(lat, lon);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }

    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.editTextLocation);
        String location = et.getText().toString();

        //show error dialog when search by typing nothing
        if(location.isEmpty())
        {
            MyDialogWindow.showMessage(this,"Error","Enter a location to Search");
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
        double lat = address.getLatitude();
        double lon = address.getLongitude();
        goToLocationZoom(lat, lon, 13);

        if(marker !=null)
        {
            marker.remove();
        }

        //add a marker to the searched location
        int height = 150;
        int width = 150;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.user_grey);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);


        marker = mGoogleMap.addMarker(new MarkerOptions()
                .title(locality)
                .draggable(true)
                .position(new LatLng(lat,lon))
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
        );
        Toast.makeText(this,location,Toast.LENGTH_LONG).show();
    }
    public boolean isValidPhone(CharSequence phone) {

        if(phone.length()!=10)
        {
            return false;
        }
        else{
            if(Pattern.matches("0[0-9]{9}", phone) || Pattern.matches("\\+94[1-9][0-9]{8}", phone))
            {
                return true;
            }
            else{
                return false;
            }
        }

    }

    public boolean isValidName(CharSequence firstName)
    {
        if (TextUtils.isEmpty(firstName)) {
            return false;
        } else {

                if(Pattern.matches("[a-zA-z]+", firstName))
                {
                    return true;
                }
                else{
                    return false;
                }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if(mGoogleMap!=null)
        {
            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                //add info window when user draq the marker and show current place of the marker
                @Override
                public void onMarkerDragEnd(Marker marker) {

                    Geocoder gc = new Geocoder(NewFriendActivity.this);
                    LatLng ll = marker.getPosition();
                    List<Address> list = null;
                    try {
                        list = gc.getFromLocation(ll.latitude,ll.longitude,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address add = list.get(0);
                    marker.setTitle(add.getAddressLine(0));
                    marker.showInfoWindow();
                    editLocation.setText(list.get(0).getAddressLine(0));


                }
            });
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    View v = getLayoutInflater().inflate(R.layout.info_window,null);
                    TextView tvLocality = (TextView)v.findViewById(R.id.tv_locality);
                    tvLocality.setText(marker.getTitle());
                    return v;
                }
            });
        }

        goToLocationZoom(6.947589, 79.863359,13);
    }

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


}
