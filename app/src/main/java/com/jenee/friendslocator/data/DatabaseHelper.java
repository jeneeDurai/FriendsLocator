package com.jenee.friendslocator.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by jne on 3/18/2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "FriendsLocator.db";
    public static final String TABLE_NAME = "FriendsTable";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "FirstName";
    public static final String COL_3 = "LastName";
    public static final String COL_4 = "Phone";
    public static final String COL_5 = "Location";
    public static final String COL_6 = "Latitude";
    public static final String COL_7 = "Longitude";
    public static final String COL_8 = "Image";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL("create table "+TABLE_NAME + "("+
                "ID INTEGER PRIMARY KEY AUTOINCREMENT,FirstName TEXT,LastName TEXT, Phone VARCHAR(12),Location Text,Latitude REAL, Longitude REAL,Image BLOB"
                +")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String firstname, String lastname, String phone, String location, double lat, double lon, byte[] image)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_2,firstname);
        cv.put(COL_3,lastname);
        cv.put(COL_4,phone);
        cv.put(COL_5,location);
        cv.put(COL_6,lat);
        cv.put(COL_7,lon);
        cv.put(COL_8,image);

        long result = db.insert(TABLE_NAME,null,cv);

        if(result == -1)
        {
            return false;
        }
        return true;
    }

    public Cursor getAllData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME,null);
        return cursor;
    }

    public Cursor getFriend(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME +" WHERE ID = "+id,null);
        return cursor;
    }

    public boolean updateData(String id, String firstname, String lastname, String phone, String location,double lat,double lon,byte[] image)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_1,id);
        cv.put(COL_2,firstname);
        cv.put(COL_3,lastname);
        cv.put(COL_4,phone);
        cv.put(COL_5,location);
        cv.put(COL_6,lat);
        cv.put(COL_7,lon);
        cv.put(COL_8,image);

        db.update(TABLE_NAME,cv,"ID = ?",new String[] {id});

        return true;
    }

    public Integer deleteData(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,"ID = ?",new String[] {id});

    }

    public Cursor getSearch(String key )
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME +" WHERE FirstName LIKE '%"+key+"%' or LastName LIKE '%"+key+"%' or Location LIKE '%"+key+"%' or Phone LIKE '%"+key+"%'",null);
        return cursor;
    }

    public Bitmap getImage(String phone)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT Image FROM "+TABLE_NAME+" WHERE Phone = "+"'"+phone+"'",null);
        cursor.moveToNext();

       byte[] byteArray = cursor.getBlob(0);

        Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0 ,byteArray.length);
        return bm;
    }

    public Cursor getFriendById(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE ID = "+id,null);
        return cursor;
    }
}
