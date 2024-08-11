package com.hfad.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import android.util.TypedValue;
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
import android.widget.Toast;


public class PlanScreen extends Fragment {
    private SQLiteDatabase pl_db;
    Plan_search_Screen pl_search_fragment;

    String date, mf_name, lf_name, df_name;
    int mf_w, mf_n, lf_w, lf_n, df_w, df_n;

    int total_kcal;

    private ImageButton pl_search_btn;
    private EditText pl_name;
    private TextView today_date;
    TimeZone nzTimeZone = TimeZone.getTimeZone("Pacific/Auckland");
    Calendar nzCalendar = Calendar.getInstance(nzTimeZone);
    long now = System.currentTimeMillis();


    private TableLayout tableLayout;
    public String[][] plan_array;
    public String home_select_day;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plan_fragment,container,false);
        today_date = view.findViewById(R.id.pl_today_date);

        pl_search_btn = view.findViewById(R.id.pl_search_Button);
        pl_name = view.findViewById(R.id.search_pl);


        //today date setting
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(nzCalendar.getTime());
//        System.out.println(now);

        Bundle bundle = getArguments();
        if (bundle != null) {
            home_select_day = bundle.getString("select_day");
            today_date.setText(home_select_day);
            // 인자를 사용하여 원하는 작업 수행
        }else{
            today_date.setText(currentDate);
        }
//        System.out.println(home_select_day);


        DBmanage dbHelper = new DBmanage(getContext());
        pl_db=dbHelper.getReadableDatabase();


        //exercise searching
        pl_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Plan_search_Screen pl_search_fragment = new Plan_search_Screen();
                Bundle bundle = new Bundle();
                bundle.putString("key", pl_name.getText().toString());
                bundle.putString("sd_date", home_select_day);
                pl_search_fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, pl_search_fragment);
                fragmentTransaction.addToBackStack(null); // 뒤로 가기 버튼 클릭 시 이전 Fragment로 이동
                fragmentTransaction.commit();

            }
        });
        //list


        tableLayout = view.findViewById(R.id.plan_tableLayout);

        if(home_select_day != null){
            String sd_query = "SELECT * FROM exercise_plan where date='"+home_select_day+"'";
            Cursor sd_cursor = pl_db.rawQuery(sd_query, null);

            int sd_plan_row_num = sd_cursor.getCount();

            if(sd_plan_row_num != 0) {
                plan_array = new String[sd_plan_row_num][4];
            }

            if (sd_cursor.moveToFirst()) {
                int init_num = 0;
                do {
                    String ex_Name = sd_cursor.getString(2);
                    int ex_count = sd_cursor.getInt(3);
                    int ex_sets = sd_cursor.getInt(4);
                    int ex_time = sd_cursor.getInt(5);

                    plan_array[init_num][0] = ex_Name;
                    plan_array[init_num][1] = String.valueOf(ex_count);
                    plan_array[init_num][2] = String.valueOf(ex_sets);
                    plan_array[init_num][3] = String.valueOf(ex_time);

                    TableRow row = new TableRow(tableLayout.getContext());

                    TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.WRAP_CONTENT,
                            TableLayout.LayoutParams.WRAP_CONTENT
                    );
                    int margin = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            10,
                            tableLayout.getResources().getDisplayMetrics()
                    );

                    layoutParams.setMargins(0,0,0,margin);

                    row.setLayoutParams(layoutParams);

//                row.setBackground(borderDrawable);
                    TextView textView = new TextView(row.getContext());
                    textView.setText(plan_array[init_num][0]);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(20);

                    TextView textView2 = new TextView(row.getContext());
                    textView2.setText(plan_array[init_num][1]);
                    textView2.setGravity(Gravity.CENTER);
                    textView2.setTextSize(20);

                    TextView textView3 = new TextView(row.getContext());
                    textView3.setText(plan_array[init_num][2]);
                    textView3.setGravity(Gravity.CENTER);
                    textView3.setTextSize(20);

                    TextView textView4 = new TextView(row.getContext());
                    textView4.setText(plan_array[init_num][3]);
                    textView4.setGravity(Gravity.CENTER);
                    textView4.setTextSize(20);


                    ImageButton del_btn = new ImageButton(row.getContext());
                    del_btn.setImageResource(R.drawable.delete);
                    del_btn.setBackgroundColor(0xffffffff);
                    del_btn.setLayoutParams(new TableRow.LayoutParams(48, 60));
                    del_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // 클릭된 버튼의 부모 TableRow 찾기
                            TableRow row = (TableRow) view.getParent();

                            // TableRow의 첫 번째 셀(TextView) 찾기
                            TextView firstCell = (TextView) row.getChildAt(0);

                            // 첫 번째 셀의 텍스트 값 가져오기
                            String text = firstCell.getText().toString();

                            DBmanage dbHelper = new DBmanage(view.getContext());
                            pl_db=dbHelper.getWritableDatabase();

                            pl_db.execSQL("DELETE FROM exercise_plan WHERE date = '" +home_select_day+ "' AND p_ex = '"+ text +"'");
                            Toast.makeText(view.getContext(), "The Exercise plan Deleted", Toast.LENGTH_LONG).show();


                            pl_db.close();

                            TableLayout tableLayout = (TableLayout) row.getParent();

                            tableLayout.removeView(row);


                        }
                    });

                    row.addView(textView);
                    row.addView(textView2);
                    row.addView(textView3);
                    row.addView(textView4);
                    row.addView(del_btn);
                    tableLayout.addView(row);


                    init_num++;
                } while (sd_cursor.moveToNext());
            }
            sd_cursor.close();

        }else{
            String query = "SELECT * FROM exercise_plan where date='"+currentDate+"'";
            Cursor cursor = pl_db.rawQuery(query, null);

            int plan_row_num = cursor.getCount();

            if(plan_row_num != 0) {
                plan_array = new String[plan_row_num][4];
            }

            if (cursor.moveToFirst()) {
                int init_num = 0;
                do {
                    String ex_Name = cursor.getString(2);
                    int ex_count = cursor.getInt(3);
                    int ex_sets = cursor.getInt(4);
                    int ex_time = cursor.getInt(5);

                    plan_array[init_num][0] = ex_Name;
                    plan_array[init_num][1] = String.valueOf(ex_count);
                    plan_array[init_num][2] = String.valueOf(ex_sets);
                    plan_array[init_num][3] = String.valueOf(ex_time);

                    TableRow row = new TableRow(tableLayout.getContext());

                    TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.WRAP_CONTENT,
                            TableLayout.LayoutParams.WRAP_CONTENT
                    );
                    int margin = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            10,
                            tableLayout.getResources().getDisplayMetrics()
                    );

                    layoutParams.setMargins(0,0,0,margin);

                    row.setLayoutParams(layoutParams);

//                row.setBackground(borderDrawable);
                    TextView textView = new TextView(row.getContext());
                    textView.setText(plan_array[init_num][0]);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(20);

                    TextView textView2 = new TextView(row.getContext());
                    textView2.setText(plan_array[init_num][1]);
                    textView2.setGravity(Gravity.CENTER);
                    textView2.setTextSize(20);

                    TextView textView3 = new TextView(row.getContext());
                    textView3.setText(plan_array[init_num][2]);
                    textView3.setGravity(Gravity.CENTER);
                    textView3.setTextSize(20);

                    TextView textView4 = new TextView(row.getContext());
                    textView4.setText(plan_array[init_num][3]);
                    textView4.setGravity(Gravity.CENTER);
                    textView4.setTextSize(20);


                    ImageButton del_btn = new ImageButton(row.getContext());
                    del_btn.setImageResource(R.drawable.delete);
                    del_btn.setBackgroundColor(0xffffffff);
                    del_btn.setLayoutParams(new TableRow.LayoutParams(48, 60));
                    del_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // 클릭된 버튼의 부모 TableRow 찾기
                            TableRow row = (TableRow) view.getParent();

                            // TableRow의 첫 번째 셀(TextView) 찾기
                            TextView firstCell = (TextView) row.getChildAt(0);

                            // 첫 번째 셀의 텍스트 값 가져오기
                            String text = firstCell.getText().toString();

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String currentDate = sdf.format(nzCalendar.getTime());

                            System.out.println(currentDate);
                            System.out.println("check start");
                            DBmanage dbHelper = new DBmanage(view.getContext());
                            pl_db=dbHelper.getWritableDatabase();

                            pl_db.execSQL("DELETE FROM exercise_plan WHERE date = '" + currentDate + "' AND p_ex = '"+ text +"'");
                            Toast.makeText(view.getContext(), "The Exercise plan Deleted", Toast.LENGTH_LONG).show();


                            pl_db.close();

                            TableLayout tableLayout = (TableLayout) row.getParent();

                            tableLayout.removeView(row);


                        }
                    });

                    row.addView(textView);
                    row.addView(textView2);
                    row.addView(textView3);
                    row.addView(textView4);
                    row.addView(del_btn);
                    tableLayout.addView(row);


                    init_num++;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }


        return view;
    }

}
