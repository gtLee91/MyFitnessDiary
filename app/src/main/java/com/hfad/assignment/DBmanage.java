package com.hfad.assignment;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DBmanage extends SQLiteOpenHelper{

    public final String Name = "name";
    public static final String DATABASE_NAME = "fitness.db";
    public final String Table_name = "user_info";
    public final String Gender="gen";
    public final String Age = "age";
    public final String Height = "height";
    public final String Weight = "weight";
    public static final int DATABASE_Version = 1;

    private final String createQuery = "CREATE TABLE IF NOT EXISTS "+Table_name+"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            +Name+" TEXT, "+Gender+" TEXT, "+Age+" NUMBER, "+Height+" NUMBER, "+Weight+" NUMBER)";

    public DBmanage(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_Version);
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL(createQuery);
    }

    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1){

    }

}
