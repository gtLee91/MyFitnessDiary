package com.hfad.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.EditText;
import android.view.View;
import android.content.Intent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView check;
    private Button Button_submit;
    private Spinner Gen_spin;
    private EditText user_name, Age_edittext, height_edittext, weight_edittext;
    public DBmanage dBmanage;
    public SQLiteDatabase db;

    private static final String DB_NAME = "fitness.db";
    private static final String DB_PATH = "/data/data/com.hfad.assignment/databases/";

    String name, gen;
    int user_age, user_height, user_weight;
    int data_exist;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        copyDatabase(this);
        check = findViewById(R.id.textView3);
        user_name = findViewById(R.id.name_editText);
        Gen_spin = findViewById(R.id.init_gender_spinner);
        Age_edittext = findViewById(R.id.age_editTextNumber);
        height_edittext = findViewById(R.id.height_editTextNumber);
        weight_edittext = findViewById(R.id.Weight_editTextNumber);

        dBmanage = new DBmanage(this);

        try {
            db = dBmanage.getReadableDatabase();
        }catch (SQLiteException e){
            db = dBmanage.getWritableDatabase();
        }


        String ck_exist_data_sql = "SELECT * from user_info";
        Cursor cs = db.rawQuery(ck_exist_data_sql, null);

        if(cs.getCount() == 1){
            cs.close();

            Intent main_screen = new Intent(this, MainScreen.class);//screen change
            startActivity(main_screen);
            finish();
        }else{
            Button_submit = findViewById(R.id.init_button);
            Button_submit.setOnClickListener(this);
        }

    }

    private void copyDatabase(Context context) {
        String databasePath = DB_PATH + DB_NAME;
        File databaseFile = new File(databasePath);

        if (databaseFile.exists()) {
            return;
        }

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(DB_NAME);

            String outFileName = DB_PATH + DB_NAME;
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            System.out.println("database copy complete!!!");
        } catch (IOException e) {
            System.out.println("database copy fail!!!");
            e.printStackTrace();
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.init_button:
                name = user_name.getText().toString();
                gen = Gen_spin.getSelectedItem().toString();
                user_age = Integer.parseInt(Age_edittext.getText().toString());
                user_height = Integer.parseInt(height_edittext.getText().toString());
                user_weight = Integer.parseInt(weight_edittext.getText().toString());

                db.execSQL("insert into user_info values(1,'"+name+"','"+gen+"',"+user_age+","+user_height+","+user_weight+");");

                user_name.setText("");
                Age_edittext.setText("");
                height_edittext.setText("");
                weight_edittext.setText("");

                Intent main_screen = new Intent(this, MainScreen.class);//screen change
                startActivity(main_screen);
                finish();
                break;
        }
    }
}