package com.hfad.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;

public class Food_search_Screen extends Fragment implements EdamamApiTask.OnApiResultListener{

    FoodScreen fd_screen_fragment;
    private SQLiteDatabase fd_db;
    private  TextView test;
    private Spinner fd_name_results, meal_time;
    private Button add_btn, cancel_btn;
    private ImageButton kcal_cal_btn;
    private EditText in_w_or_n, in_kcal;
    double calories_cal;
    String strValue, save_fd_name;
    int save_fd_w_or_n, save_fd_cal;

    public ArrayList<String> foodName = new ArrayList<>();
    public ArrayList<Double> foodCal = new ArrayList<>();

    EdamamApiTask task = new EdamamApiTask();




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.food_search_fragment,container,false);

        test = view.findViewById(R.id.textView8);
        fd_name_results = view.findViewById(R.id.food_name_results);
        meal_time = view.findViewById(R.id.meal_time_spin);

        in_w_or_n = view.findViewById(R.id.input_w_or_n);
        in_kcal = view.findViewById(R.id.input_kcals);

        kcal_cal_btn = view.findViewById(R.id.kcal_cal_btn);
        add_btn = view.findViewById(R.id.fd_search_add);
        cancel_btn = view.findViewById(R.id.fd_search_cancel);


        //이전 fragment에 있는 edittext의 값 가져오는 부분
        Bundle bundle = getArguments();
        String fd = bundle.getString("key");


        DBmanage dbHelper = new DBmanage(getContext());
        fd_db=dbHelper.getReadableDatabase();

        //API에 있는 음식 이름 가져와 spinner에 넣음
        task.setOnApiResultListener(this);
        task.execute(fd);

        //음식 무게 입력시


        //버튼 관련 세팅
        kcal_cal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = foodName.indexOf(fd_name_results.getSelectedItem().toString());
                int temp_w = Integer.parseInt(in_w_or_n.getText().toString());
                double temp_r_kcal = foodCal.get(index);

                calories_cal = ( ((double) temp_w)/(100) * temp_r_kcal );
                strValue = String.format("%.2f", calories_cal);

                in_kcal.setText(strValue);

            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String currentDate = format.format(calendar.getTime());

                switch (meal_time.getSelectedItem().toString()){
                    case "Morning":
                        save_fd_name = fd_name_results.getSelectedItem().toString();
                        save_fd_w_or_n = Integer.parseInt(in_w_or_n.getText().toString());
                        save_fd_cal = (int) ( Double.parseDouble(in_kcal.getText().toString()) );

                        fd_db.execSQL("insert into morning_food (date, mf_name, mf_w, mf_kcal) values('"+currentDate+"','"+save_fd_name+"',"+save_fd_w_or_n+","+save_fd_cal+");");
                        Toast.makeText(getContext(), "Saved your food record to Breakfast", Toast.LENGTH_LONG).show();
                        showFoodScreenFragment();
                        return;
                    case "Lunch":
                        save_fd_name = fd_name_results.getSelectedItem().toString();
                        save_fd_w_or_n = Integer.parseInt(in_w_or_n.getText().toString());
                        save_fd_cal = (int) ( Double.parseDouble(in_kcal.getText().toString()) );

                        fd_db.execSQL("insert into lunch_food (date, lf_name, lf_w, lf_kcal) values('"+currentDate+"','"+save_fd_name+"',"+save_fd_w_or_n+","+save_fd_cal+");");
                        Toast.makeText(getContext(), "Saved your food record to Lunch", Toast.LENGTH_LONG).show();
                        showFoodScreenFragment();
                        return;
                    case "Dinner":
                        save_fd_name = fd_name_results.getSelectedItem().toString();
                        save_fd_w_or_n = Integer.parseInt(in_w_or_n.getText().toString());
                        save_fd_cal = (int) ( Double.parseDouble(in_kcal.getText().toString()) );

                        fd_db.execSQL("insert into dinner_food (date, df_name, df_w, df_kcal) values('"+currentDate+"','"+save_fd_name+"',"+save_fd_w_or_n+","+save_fd_cal+");");
                        Toast.makeText(getContext(), "Saved your food record to Dinner", Toast.LENGTH_LONG).show();
                        showFoodScreenFragment();
                        return;
                }

            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showFoodScreenFragment();

            }
        });


        return view;
    }

    public void showFoodScreenFragment() {
        FoodScreen fd_screen_fragment = new FoodScreen();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fd_screen_fragment);
        fragmentTransaction.commit();
    }

    public void onApiResult(ArrayList<String> fd_Name, ArrayList<Double> fd_Kcal) {

        for(int i = 0; i < task.fd_Name.size(); i++){
            foodName.add(task.fd_Name.get(i));
            foodCal.add(task.fd_Kcal.get(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, task.fd_Name);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fd_name_results.setAdapter(adapter);
    }
}
