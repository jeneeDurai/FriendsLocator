package com.jenee.friendslocator.ui;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.jenee.friendslocator.R;
import com.jenee.friendslocator.data.DatabaseHelper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jenee.friendslocator.dialogWindow.MyDialogWindow;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class UpdateFriendActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Marker marker;
    DatabaseHelper databaseHelper;

    final int REQUEST_CODE_GALLERY = 999;

    EditText updateEditFirstName, updateEditLastName, updateEditPhone, updateEditLocation;
    double updateLatitude,updateLongitude;
    Button updateButton;
    int id;
    ImageView updateImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_friend);
        initMap();

        databaseHelper = new DatabaseHelper(this);

        updateEditFirstName = (EditText) findViewById(R.id.updateEditTextFirstName);
        updateEditLastName = (EditText) findViewById(R.id.updateEditTextLastName);
        updateEditPhone = (EditText) findViewById(R.id.updateEditTextPhoneNumber);
        updateEditLocation = (EditText) findViewById(R.id.updateEditTextLocation);
        updateButton = (Button) findViewById(R.id.updateButton);

        updateImageView = (ImageView) findViewById(R.id.updateImageView5);

        Cursor res = databaseHelper.getFriend(getIntent().getIntExtra("FriendId", 0));

        res.moveToNext();

        id=res.getInt(0);
        updateEditFirstName.setText(res.getString(1));
        updateEditLastName.setText(res.getString(2));
        updateEditPhone.setText(res.getString(3));
        updateEditLocation.setText(res.getString(4));
        updateLatitude = res.getDouble(5);
        updateLongitude = res.getDouble(6);

        byte [] imageByte = res.getBlob(7);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        updateImageView.setImageBitmap(bitmap);
        uploadImage();

        updateData();

    }

    private void initMap() {
        MapFragment mapFragmanet = (MapFragment) getFragmentManager().findFragmentById(R.id.updateMapFragment);
        mapFragmanet.getMapAsync(this);
    }

    public void uploadImage(){
        updateImageView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(UpdateFriendActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_GALLERY);


                    }
                }
        );
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY)
        {
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

        if(requestCode==REQUEST_CODE_GALLERY && resultCode==RESULT_OK && data !=null)
        {
            Uri uri = data.getData();

            try {

                Bitmap bm = decodeBitmap(uri,getApplicationContext());

                updateImageView.setImageBitmap(bm);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        goToLocationZoom(updateLatitude, updateLongitude,13);

        int height = 150;
        int width = 150;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.user_grey);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        marker = mGoogleMap.addMarker(new MarkerOptions()
                        .title(updateEditLocation.getText().toString())
                        .draggable(true)
                        .position(new LatLng(updateLatitude,updateLongitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));


        if(mGoogleMap!=null)
        {
            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder gc = new Geocoder(UpdateFriendActivity.this);
                    LatLng ll = marker.getPosition();
                    List<Address> list = null;
                    try {
                        list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address add = list.get(0);
                    marker.setTitle(add.getLocality());
                    marker.showInfoWindow();
                    updateEditLocation.setText(list.get(0).getAddressLine(0));
                    updateLatitude = ll.latitude;
                    updateLongitude =ll.longitude;
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
    }
    private void goToLocationZoom(double lat, double lon, float zoom) {
        LatLng ll = new LatLng(lat, lon);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }
    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.updateEditTextLocation);
        String location = et.getText().toString();

        if(location.isEmpty())
        {
            MyDialogWindow.showMessage(this,"Error","Enter a location to Search");
            return;
        }

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);

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

        updateLatitude = lat;
        updateLongitude =lon;
    }
    public void updateData(){
        updateButton.setOnClickListener(
                new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {


                        if(TextUtils.isEmpty(updateEditFirstName.getText().toString()))
                        {
                            MyDialogWindow.showMessage(UpdateFriendActivity.this,"ERROR","Enter First Name");
                            return;
                        }
                        if(!isValidName(updateEditFirstName.getText().toString())){
                            MyDialogWindow.showMessage(UpdateFriendActivity.this,"ERROR","First Name not valid");
                            return;

                        }
                        if(TextUtils.isEmpty(updateEditLastName.getText().toString()))
                        {
                            MyDialogWindow.showMessage(UpdateFriendActivity.this,"ERROR","Enter Last Name");
                            return;
                        }

                        if (!isValidName(updateEditLastName.getText().toString()))
                        {
                            MyDialogWindow.showMessage(UpdateFriendActivity.this,"ERROR","Last Name is not valid");
                            return;

                        }
                        if(TextUtils.isEmpty(updateEditPhone.getText().toString()))
                        {
                            MyDialogWindow.showMessage(UpdateFriendActivity.this,"ERROR","Enter the Phone number");
                            return;
                        }
                        if(!isValidPhone(updateEditPhone.getText().toString()))
                        {
                            //Toast.makeText(getApplicationContext(),"Phone number is not valid",Toast.LENGTH_SHORT).show();
                            MyDialogWindow. showMessage(UpdateFriendActivity.this,"ERROR","Phone number is not valid");
                            return ;
                        }

                        if(TextUtils.isEmpty(updateEditLocation.getText().toString())){
                            MyDialogWindow.showMessage(UpdateFriendActivity.this,"ERROR","Enter and Search your friends location");
                            return;

                        }


                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),R.style.MyDialogTheme);

                        builder.setTitle("Confirm").setMessage("Do you want to update?").setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Bitmap bitmap = ((BitmapDrawable) updateImageView.getDrawable()).getBitmap();
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] imageInByte = baos.toByteArray();

                                        boolean isUpdate = databaseHelper.updateData(String.valueOf(id),
                                                updateEditFirstName.getText().toString(),
                                                updateEditLastName.getText().toString(),
                                                updateEditPhone.getText().toString(),
                                                updateEditLocation.getText().toString(),
                                                updateLatitude,updateLongitude,imageInByte);
                                        if(isUpdate == true)
                                        {
                                            Toast.makeText(getApplicationContext(),"Friend Detail Updated",Toast.LENGTH_LONG).show();
                                            Intent newIntent = new Intent(UpdateFriendActivity.this,MainActivity.class);
                                            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(newIntent);
                                        }
                                        else{
                                            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                                        }
                                        dialogInterface.cancel();

                                    }
                                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
        );
    }
    public boolean isValidPhone(CharSequence phone) {
        if(phone.length()!=10)
        {
            return false;
        } else {

                if(Pattern.matches("0[0-9]+", phone) || Pattern.matches("\\+[1-9][0-9]+", phone))
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
}
