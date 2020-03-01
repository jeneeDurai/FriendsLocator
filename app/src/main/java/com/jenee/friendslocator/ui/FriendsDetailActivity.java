package com.jenee.friendslocator.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jenee.friendslocator.R;
import com.jenee.friendslocator.data.DatabaseHelper;
import com.jenee.friendslocator.data.Friend;
import com.jenee.friendslocator.data.FriendsAdapter;
import com.jenee.friendslocator.dialogWindow.MyDialogWindow;

import java.util.ArrayList;

public class FriendsDetailActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;

    EditText searchTxt;
    Button btnsearch;
    ListView listView;
    //list to handle all friends object
    ArrayList<Friend> friendList;
    //adapter to show friends details in a list view
    FriendsAdapter friendsAdapter;
    Dialog myDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_detail);
        searchTxt= (EditText) findViewById(R.id.editTextSearch);
        btnsearch = (Button) findViewById(R.id.searchBtn);
        databaseHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.listView);
        friendList = new ArrayList<>();
        //set the adapter with listview and friends list
        friendsAdapter = new FriendsAdapter(this,R.layout.listview,friendList);
        myDialog = new Dialog(this);
        //search button onclick listener method called
        search();

        //get all friends data from db
        Cursor res = databaseHelper.getAllData();
        friendList.clear();

        //iterate through all the friends detail add each friend object to the friend list
        while(res.moveToNext()) {
            int id = res.getInt(0);
            String firstName = res.getString(1);
            String lastName = res.getString(2);
            String phone = res.getString(3);
            String location = res.getString(4);
            double latitude = res.getDouble(5);
            double longitude = res.getDouble(6);
            byte[] imageByte = res.getBlob(7);

            friendList.add(new Friend(id, firstName, lastName, phone, location, latitude, longitude, imageByte));

        }
        //set adapter to the list view
        listView.setAdapter(friendsAdapter);
        friendsAdapter.notifyDataSetChanged();

        //set onclick listener to listview to show popup
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Friend f = friendList.get(i);
                ShowPopup(view,f);
            }
        });

    }

    public void ShowPopup(View v, final Friend friend) {
        myDialog.setContentView(R.layout.popup);

        //get popup window view elements
        TextView nameTextView = (TextView) myDialog.findViewById(R.id.NameText) ;
        final Button callButton = (Button) myDialog.findViewById(R.id.callButton);
        Button editButton = (Button) myDialog.findViewById(R.id.editButton);
        Button deleteButton = (Button) myDialog.findViewById(R.id.deleteButton);
        ImageView ProfileImage = (ImageView) myDialog.findViewById(R.id.profileImage);

        //decode the byte array image to bitmap
        byte[] imageByte = friend.getImage();
        Bitmap bm = BitmapFactory.decodeByteArray(imageByte,0,imageByte.length);
       ProfileImage.setImageBitmap(bm);

        String name = friend.getFirstName()+" "+friend.getLastName();
        nameTextView.setText(name);

        //onclick listener to the call button
        callButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                //dial the friend number in dialpad and let the user to press call button
                Uri number = Uri.parse("tel:"+friend.getPhone());
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            }
        });

        //onclick listener to
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pass selected friend object to the update activty and start the update activity
               Intent i = new Intent(getApplicationContext(),UpdateFriendActivity.class);
               i.putExtra("FriendId", friend.getId());
               startActivity(i);
            }
        });

        //onclick listener for delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //alert box to ask the user to confirm the deletion
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),R.style.MyDialogTheme);

                builder.setTitle("Confirm").setMessage("Do you want to delete?").setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //If user confirm the delete then delete the friend detail
                                int deletedRows = databaseHelper.deleteData(String.valueOf(friend.getId()));
                                if(deletedRows>0)
                                {
                                    Toast.makeText(getApplicationContext(),"Friend Detail Deleted",Toast.LENGTH_LONG).show();
                                    finish();
                                    Intent intent =new Intent(FriendsDetailActivity.this,MainActivity.class);
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                                }
                                dialogInterface.cancel();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //if not do nothing
                        dialogInterface.cancel();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();


            }
        });

        //set view element transparent
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.findViewById(R.id.callButton).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.findViewById(R.id.editButton).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.findViewById(R.id.deleteButton).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    //naviagte to Add friend activty
    public void addNewFriendNavigate(View view) {
        Intent i = new Intent(this,NewFriendActivity.class);
        startActivity(i);
    }

    public void search(){
        btnsearch.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        //get match friend data using search key word
                        LoadList(searchTxt.getText().toString());
                    }
                }
        );
    }

    public void Refresh(View view) {
        searchTxt.setText("");
        LoadList("");
    }


    public void LoadList(String key)
    {
        Cursor res = databaseHelper.getSearch(key);
        //If no data found prompt the user
        if(res.getCount()==0)
        {
            MyDialogWindow.showMessage(FriendsDetailActivity.this,"Error","No friend Found");
            return ;
        }
        friendList.clear();
        //iterate through the friend data and add those friend to friend list
        while(res.moveToNext())
        {
            int id =res.getInt(0);
            String firstName = res.getString(1);
            String lastName = res.getString(2);
            String phone = res.getString(3);
            String location = res.getString(4);
            double latitude = res.getDouble(5);
            double longitude = res.getDouble(6);
            byte[] imageByte = res.getBlob(7);

            friendList.add(new Friend(id,firstName,lastName,phone,location,latitude,longitude,imageByte));

        }
        friendsAdapter.notifyDataSetChanged();
    }

    //hlper class to show custome dialog box
    public static class MyDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.listview, null));
                    // Add action button

            return builder.create();
        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
        double myDouble = savedInstanceState.getDouble("myDouble");
        int myInt = savedInstanceState.getInt("MyInt");
        String myString = savedInstanceState.getString("MyString");
    }
}
