package com.hfad.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;



public class MainScreen extends AppCompatActivity{

    HomeScreen fragment1;
    FoodScreen fragment2;
    PlanScreen fragment3;
    TodoScreen fragment4;
    ReportScreen fragment5;

    private long backKeyPressedTime = 0;
    public String home_selected_day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        fragment1 = new HomeScreen();
        fragment2 = new FoodScreen();
        fragment3 = new PlanScreen();
        fragment4 = new TodoScreen();
        fragment5 = new ReportScreen();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.tab1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();
                        return true;
                    case R.id.tab2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment2).commit();
                        return true;
                    case R.id.tab3:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment3).commit();
                        return true;
                    case R.id.tab4:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment4, "TodoFragmentTag").commit();
                        return true;
                    case R.id.tab5:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment5).commit();
                        return true;
                }
                return false;
            }
        });
    }

    //뒤로가기 금지
    public void onBackPressed(){
//        super.onBackPressed()

        Toast toast=Toast.makeText(MainScreen.this, "Do you want to finish this app?", Toast.LENGTH_SHORT);
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            toast.show();
            return;
        }
        if(System.currentTimeMillis() <= backKeyPressedTime + 2000){
            finish();
            toast.cancel();
        }
    }


}