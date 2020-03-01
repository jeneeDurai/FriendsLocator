package com.jenee.friendslocator.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jenee.friendslocator.R;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jne on 3/21/2018.
 */

public class FriendsAdapter extends BaseAdapter{
    private Context context;
    private int layout;
    private ArrayList<Friend> friendsList;
    @Override
    public int getCount() {
        return friendsList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder{
        TextView Name;
        TextView KnownNameAddress;
        TextView City;
        TextView Phone;
        ImageView Image;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View row = view;
        Holder holder = new Holder();

        if(row==null){
            LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflator.inflate(layout,null);

            holder.Name = (TextView) row.findViewById(R.id.tv_locality);
            holder.KnownNameAddress = (TextView) row.findViewById(R.id.tv_address);
            holder.Phone = (TextView) row.findViewById(R.id.tv_phone);
            holder.Image = (ImageView) row.findViewById(R.id.imageView1);

            row.setTag(holder);

        }
        else{
            holder = (Holder) row.getTag();

        }

        Friend friend = friendsList.get(position);

        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(friend.getLatitude(), friend.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();


        holder.Name.setText(friend.getFirstName()+" "+friend.getLastName());

        holder.KnownNameAddress.setText(address);
        holder.Phone.setText(friend.getPhone());


        byte[] imageByte = friend.getImage();
        Bitmap bm = BitmapFactory.decodeByteArray(imageByte,0,imageByte.length);
        holder.Image.setImageBitmap(bm);

        return row;
    }

    public FriendsAdapter(Context context, int layout, ArrayList<Friend> friendsList) {
        this.context = context;
        this.layout = layout;
        this.friendsList = friendsList;
    }
}
