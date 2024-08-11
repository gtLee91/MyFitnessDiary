package com.hfad.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;

public class Plan_search_Screen extends Fragment{

    FoodScreen fd_screen_fragment;
    private SQLiteDatabase pl_db;
    private  TextView test;
    private Spinner pl_name_results;
    private Button add_btn, cancel_btn;
    private ImageButton kcal_cal_btn;
    private EditText in_count, in_sets, in_time;
    double calories_cal;
    String save_ex_name;
    int save_count, save_sets, save_time;
    int temp_r_w;
    int temp1,temp2;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plan_search_fragment,container,false);

        pl_name_results = view.findViewById(R.id.ex_name_results);


        in_count = view.findViewById(R.id.input_count);
        in_sets = view.findViewById(R.id.input_sets);
        in_time = view.findViewById(R.id.input_time);


        add_btn = view.findViewById(R.id.pl_search_add);
        cancel_btn = view.findViewById(R.id.pl_search_cancel);

        ArrayList<String> ex_Name = new ArrayList<>();

        //이전 fragment에 있는 edittext의 값 가져오는 부분
        Bundle bundle = getArguments();
        String pl = bundle.getString("key");
        String sd_date = bundle.getString("sd_date");

        //db에 있는 음식 이름 가져와 spinner에 넣음
        DBmanage dbHelper = new DBmanage(getContext());
        pl_db=dbHelper.getReadableDatabase();
        String query = "SELECT * FROM exercise_info WHERE exercise_name like '%"+pl+"%'";
        Cursor cursor = pl_db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String exercise_result_Name = cursor.getString(1);

                ex_Name.add(exercise_result_Name);

            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, ex_Name);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        pl_name_results.setAdapter(adapter);
        //음식 무게 입력시

        in_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String ct_tmp = in_count.getText().toString();
                if(ct_tmp.equals("")){
                    temp1 = 0;
                }else{
                    temp1 = Integer.parseInt(in_count.getText().toString());
                }

//                System.out.println(temp1);

                if(temp1 == 0 && temp2 == 0){
                    in_time.setEnabled(true);
                    in_time.setFocusable(true);
                    in_time.setFocusableInTouchMode(true);
                    in_time.requestFocus();
                }else{
                    in_time.setEnabled(false);
                    in_time.setFocusable(false);
                    in_time.setFocusableInTouchMode(true);
                    in_time.clearFocus();
                }
            }
        });

        in_sets.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String sets_tmp = in_sets.getText().toString();
                if(sets_tmp.equals("")){
                    temp2 = 0;
                }else{
                    temp2 = Integer.parseInt(in_sets.getText().toString());
                }
//                System.out.println(temp2);

                if(temp1 == 0 && temp2 == 0){
                    in_time.setEnabled(true);
                    in_time.setFocusable(true);
                    in_time.setFocusableInTouchMode(true);
                    in_time.requestFocus();
                }else{
                    in_time.setEnabled(false);
                    in_time.setFocusable(false);
                    in_time.setFocusableInTouchMode(true);
                    in_time.clearFocus();
                }
            }
        });

        //버튼 관련 세팅

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String currentDate = format.format(calendar.getTime());


                save_ex_name = pl_name_results.getSelectedItem().toString();
                save_count = Integer.parseInt(in_count.getText().toString());
                save_sets = Integer.parseInt(in_sets.getText().toString());
                save_time = Integer.parseInt(in_time.getText().toString());

                if(save_count != 0 && save_sets != 0){
                    save_time = 0;
                }

                if(sd_date != null){
                    pl_db.execSQL("insert into exercise_plan (date, p_ex, p_ex_ct, p_ex_sets, p_ex_time) values('"+sd_date+"','"+save_ex_name+"',"+save_count+","+save_sets+","+save_time+");");
                }else{
                    pl_db.execSQL("insert into exercise_plan (date, p_ex, p_ex_ct, p_ex_sets, p_ex_time) values('"+currentDate+"','"+save_ex_name+"',"+save_count+","+save_sets+","+save_time+");");
                }

                Toast.makeText(getContext(), "Saved your plan", Toast.LENGTH_LONG).show();

                if(sd_date != null){
                    askagainpopup(sd_date);
                }else{
                    showPlanScreenFragment();
                }


            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPlanScreenFragment();

            }
        });


        return view;
    }

    public void showPlanScreenFragment() {
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.tab3);
    }

    public void askagainpopup(String sd_date){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View continue_PopupView = inflater.inflate(R.layout.ask_again_add_popup, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(continue_PopupView);
        AlertDialog continue_PopupDialog = builder.create();


        Button in_yes_btn = continue_PopupView.findViewById(R.id.yes_add_again_button);
        Button in_no_btn =continue_PopupView.findViewById(R.id.no_add_again_button);

        in_yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                continue_PopupDialog.dismiss();
                changefragment(sd_date);

            }
        });

        in_no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continue_PopupDialog.dismiss();
                showPlanScreenFragment();
            }
        });

        continue_PopupDialog.show();
    }

    private void changefragment(String selectday){
        // 전환하고자 하는 프래그먼트를 생성합니다.
        PlanScreen planFragment = new PlanScreen();

        // 인자를 설정합니다.
        Bundle bundle = new Bundle();
        bundle.putString("select_day", selectday);
        planFragment.setArguments(bundle);

        // bottomNavigationView에서 해당 탭을 선택합니다.
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.tab3);

        // 현재 화면의 프래그먼트 매니저를 가져옵니다.
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, planFragment, "PlanFragmentTag");
        fragmentTransaction.commit();


    }
}
