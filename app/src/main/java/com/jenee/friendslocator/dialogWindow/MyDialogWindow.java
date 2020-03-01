package com.jenee.friendslocator.dialogWindow;

import android.content.Context;

import com.jenee.friendslocator.R;

/**
 * Created by jne on 3/26/2018.
 */

public class MyDialogWindow {


    public static void showMessage(Context context, String title, String message)
    {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", null);
        builder.show();
    }
}
