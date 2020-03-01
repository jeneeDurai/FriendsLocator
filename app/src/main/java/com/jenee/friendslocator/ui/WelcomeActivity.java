package com.jenee.friendslocator.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jenee.friendslocator.R;
import com.jenee.friendslocator.dialogWindow.MyDialogWindow;

public class WelcomeActivity extends AppCompatActivity {

    private TextView tvName;
    private ImageView ivLogoUser;
    private ImageView ivLogoRound;

    public static boolean WelcomeStart = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        if(WelcomeStart == false)
        {
            //Log.i("Summa","Welcome Create "+WelcomeStart);
            Intent i = new Intent(WelcomeActivity.this,MainActivity.class);
            startActivity(i);
        }
        else {
            WelcomeStart = true;

            //set view elements
            tvName = (TextView) findViewById(R.id.logoName);
            ivLogoUser = (ImageView) findViewById(R.id.logoUser);
            ivLogoRound = (ImageView) findViewById(R.id.logoRound);

            //load the animation
            final Animation anim = AnimationUtils.loadAnimation(this, R.anim.basic_animation);
            tvName.startAnimation(anim);
            ivLogoUser.startAnimation(anim);
            ivLogoRound.startAnimation(anim);

            Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.logo_rotate);
            ivLogoRound.startAnimation(anim2);

            final Intent i = new Intent(this, MainActivity.class);

            //check whether the user already turn on the internet orf else prompt the user to turn on the internet
            if (!isConnected(getApplicationContext())) {
                MyDialogWindow.showMessage(this, "Error", "Turn on Internet!!");
            }


            Thread thread = new Thread() {
                public void run() {
                    try {
                        //sleep the activty untill the animations finish
                        sleep(6500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    //wait untill user turn on the internet
                                    while (!isConnected(getApplicationContext())) {
                                        Thread.sleep(1000);
                                    }
                                } catch (Exception e) {
                                } finally {
                                    startActivity(i);
                                }
                            }
                        };
                        t.start();
                    }
                }
            };
            thread.start();
        }
    }

    //method to check internet
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Welcome Log","Welcome onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("welcome Log","Welcome onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //WelcomeStart = true;
        Log.i("Welcome Log","Welcome onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
       // WelcomeStart = true;
        Log.i("welcome Log","Welcome onStop");
    }
}
