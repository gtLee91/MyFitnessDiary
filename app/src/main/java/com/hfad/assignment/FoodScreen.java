package com.hfad.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.widget.TableLayout;
import android.widget.TableRow;



public class FoodScreen extends Fragment {
    private SQLiteDatabase fd_db;
    Food_search_Screen fd_search_fragment;

    String date, mf_name, lf_name, df_name;
    int mf_w, mf_n, lf_w, lf_n, df_w, df_n;

    int total_kcal;

    private ImageButton fd_search_btn;
    private EditText fd_name;
    private TextView today_date, total_eat_kcal;
    TimeZone nzTimeZone = TimeZone.getTimeZone("Pacific/Auckland");
    Calendar nzCalendar = Calendar.getInstance(nzTimeZone);
    long now = System.currentTimeMillis();

    private RecyclerView recyclerview;
    private TableLayout tableLayout;
    public String[][] food_kcal_array;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.food_fragment,container,false);
        today_date = view.findViewById(R.id.fd_today_date);
        total_eat_kcal = view.findViewById(R.id.total_kcal_value);
        fd_search_btn = view.findViewById(R.id.fd_search_Button);
        fd_name = view.findViewById(R.id.search_fd);

        recyclerview = view.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        //today date setting
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(nzCalendar.getTime());
//        System.out.println(now);

        DBmanage dbHelper = new DBmanage(getContext());
        fd_db=dbHelper.getReadableDatabase();

        today_date.setText(currentDate);
        //food searching
        fd_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Food_search_Screen fd_search_fragment = new Food_search_Screen();
                Bundle bundle = new Bundle();
                bundle.putString("key", fd_name.getText().toString());

                fd_search_fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, fd_search_fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
        //total kcal

        String query1 = "SELECT SUM(mf_kcal) FROM morning_food WHERE date ='"+currentDate+"'";
        Cursor cursor1 = fd_db.rawQuery(query1, null);
        if(cursor1.moveToFirst()){
            int mf_kcal = cursor1.getInt(0);
            total_kcal = mf_kcal;
        }
        cursor1.close();

        String query2 = "SELECT SUM(lf_kcal) FROM lunch_food WHERE date ='"+currentDate+"'";
        Cursor cursor2 = fd_db.rawQuery(query2, null);
        if(cursor2.moveToFirst()){
            int lf_kcal = cursor2.getInt(0);
            total_kcal = total_kcal + lf_kcal;
        }
        cursor2.close();

        String query3 = "SELECT SUM(df_kcal) FROM dinner_food WHERE date ='"+currentDate+"'";
        Cursor cursor3 = fd_db.rawQuery(query3, null);
        if(cursor3.moveToFirst()){
            int df_kcal = cursor3.getInt(0);
            total_kcal = total_kcal + df_kcal;
        }
        cursor3.close();
        total_eat_kcal.setText(String.valueOf(total_kcal));
        //morning kcal

        List<ExpandableListAdapter.Item> data = new ArrayList<>();
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Breakfast","","",""));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Food Name","Weight","Kcal","Breakfast"));
        recyclerview.setAdapter(new ExpandableListAdapter(data));


        String query4 = "SELECT * FROM morning_food where date='"+currentDate+"'";
        Cursor cursor4 = fd_db.rawQuery(query4, null);

        int ml_row_num = cursor4.getCount();

        if(ml_row_num != 0) {
            food_kcal_array = new String[ml_row_num][3];
        }

        if (cursor4.moveToFirst()) {
            int init_num = 0;
            do {
                String food_result_Name = cursor4.getString(2);
                int food_result_W = cursor4.getInt(3);
                int food_result_N = cursor4.getInt(4);
                int food_result_cal = cursor4.getInt(5);

                food_kcal_array[init_num][0] = food_result_Name;
                if(food_result_W == 0){
                    food_kcal_array[init_num][1] = String.valueOf(food_result_N);
                }else{
                    food_kcal_array[init_num][1] = String.valueOf(food_result_W);
                }
                food_kcal_array[init_num][2] = String.valueOf(food_result_cal);

                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, food_kcal_array[init_num][0],food_kcal_array[init_num][1],food_kcal_array[init_num][2],"Breakfast"));
                recyclerview.setAdapter(new ExpandableListAdapter(data));

                init_num++;
            } while (cursor4.moveToNext());
        }
        cursor4.close();



        //lunch kcal
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Lunch","","",""));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Food Name","Weight","Kcal","Lunch"));
        recyclerview.setAdapter(new ExpandableListAdapter(data));


        String query5 = "SELECT * FROM lunch_food where date='"+currentDate+"'";
        Cursor cursor5 = fd_db.rawQuery(query5, null);

        int lf_row_num = cursor5.getCount();

        if(lf_row_num != 0) {
            food_kcal_array = new String[lf_row_num][3];
        }

        if (cursor5.moveToFirst()) {
            int init_num = 0;
            do {
                String food_result_Name = cursor5.getString(2);
                int food_result_W = cursor5.getInt(3);
                int food_result_N = cursor5.getInt(4);
                int food_result_cal = cursor5.getInt(5);

                food_kcal_array[init_num][0] = food_result_Name;
                if(food_result_W == 0){
                    food_kcal_array[init_num][1] = String.valueOf(food_result_N);
                }else{
                    food_kcal_array[init_num][1] = String.valueOf(food_result_W);
                }
                food_kcal_array[init_num][2] = String.valueOf(food_result_cal);

                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, food_kcal_array[init_num][0],food_kcal_array[init_num][1],food_kcal_array[init_num][2],"Lunch"));
                recyclerview.setAdapter(new ExpandableListAdapter(data));

                init_num++;
            } while (cursor5.moveToNext());
        }
        cursor5.close();

        //dinner kcal
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Dinner","","",""));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Food Name","Weight","Kcal","Dinner"));
        recyclerview.setAdapter(new ExpandableListAdapter(data));


        String query6 = "SELECT * FROM dinner_food where date='"+currentDate+"'";
        Cursor cursor6 = fd_db.rawQuery(query6, null);

        int df_row_num = cursor6.getCount();

        if(df_row_num != 0) {
            food_kcal_array = new String[df_row_num][3];
        }

        if (cursor6.moveToFirst()) {
            int init_num = 0;
            do {
                String food_result_Name = cursor6.getString(2);
                int food_result_W = cursor6.getInt(3);
                int food_result_N = cursor6.getInt(4);
                int food_result_cal = cursor6.getInt(5);

                food_kcal_array[init_num][0] = food_result_Name;
                if(food_result_W == 0){
                    food_kcal_array[init_num][1] = String.valueOf(food_result_N);
                }else{
                    food_kcal_array[init_num][1] = String.valueOf(food_result_W);
                }
                food_kcal_array[init_num][2] = String.valueOf(food_result_cal);

                data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, food_kcal_array[init_num][0],food_kcal_array[init_num][1],food_kcal_array[init_num][2],"Dinner"));
                recyclerview.setAdapter(new ExpandableListAdapter(data));

                init_num++;
            } while (cursor6.moveToNext());
        }
        cursor6.close();

        return view;
    }



}
