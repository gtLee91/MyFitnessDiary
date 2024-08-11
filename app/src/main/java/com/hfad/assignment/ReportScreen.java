package com.hfad.assignment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import org.threeten.bp.temporal.ChronoUnit;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ReportScreen extends Fragment {


    private SQLiteDatabase report_db;

    public String[][] daily_array, weekly_array, morning_kcal_array, lunch_kcal_array, dinner_kcal_array, morning_array, lunch_array, dinner_array;
    public int daily_row_num, daily_init_num, daily_burn_kcal = 0;
    public int weekly_row_num, weekly_init_num, day_ck_num, weekly_burn_kcal = 0, total_burn_kcal = 0;
    public int food1_row_num, food2_row_num, food3_row_num;
    public int morning_date_row_num, lunch_date_row_num, dinner_date_row_num;
    TimeZone nzTimeZone = TimeZone.getTimeZone("Pacific/Auckland");
    Calendar nzCalendar = Calendar.getInstance(nzTimeZone);
    long now = System.currentTimeMillis();

    private PieChart pieChart;
    private BarChart barChart, food_barChart;
    private TableLayout tableLayout, weekly_tableLayout, food_tableLayout;
    private TextView daily_date, weekly_date, food_date, weekly_total_burn_kcal, food_kcal;
    private ImageButton daily_calendar, weekly_calendar, food_calendar;

    public MaterialCalendarView calendarView, weekly_calendarView, food_calendarView;
    public String type, rp_select_day, first_select_day, second_select_day, date_diff;
    public String daily_query, weekly_query, food_query1, food_query2, food_query3;
    public String rewrite_date;

    private int lastSelectedTabIdx = 0;
    public String[] tmpArray;
    public String[] tmpArray_diffver;
    public int itemCount;
    public String[] same_day_array;
    public int[] day_total_burn;

    public int morning_init_num, lunch_init_num, dinner_init_num;
    public int morning_d_init_num, lunch_d_init_num, dinner_d_init_num;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_fragment,container,false);
//textView date
        daily_date = view.findViewById(R.id.rt_daily_date);
        weekly_date = view.findViewById(R.id.rt_weekly_date);
        food_date = view.findViewById(R.id.rt_food_date);
        weekly_total_burn_kcal = view.findViewById(R.id.weekly_burn_value);
        food_kcal = view.findViewById(R.id.food_kcal_value);
//ImageButton
        daily_calendar = view.findViewById(R.id.rt_daily_calendar);
        weekly_calendar = view.findViewById(R.id.rt_weelky_calendar);
        food_calendar = view.findViewById(R.id.rt_food_calendar);
//chart
        pieChart = view.findViewById(R.id.chart1);
        barChart = view.findViewById(R.id.chart2);
        food_barChart = view.findViewById(R.id.chart3);
//table
        tableLayout = view.findViewById(R.id.report_daily_table);
        weekly_tableLayout = view.findViewById(R.id.report_weekly_table);
        food_tableLayout = view.findViewById(R.id.report_food_table);
//time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(nzCalendar.getTime());

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getString("type");
            if(type.equals("1")){
                rp_select_day = bundle.getString("select_day");
                daily_date.setText(rp_select_day);
                daily_query = "SELECT * FROM exercise_record WHERE date ='"+rp_select_day+"'";
                dailySqlRunning(daily_query);

            }else if(type.equals("2")){
                first_select_day = bundle.getString("first_select_day");
                second_select_day = bundle.getString("second_select_day");
                date_diff = bundle.getString("date_diff");
                itemCount = Integer.parseInt(date_diff) + 1;
                weekly_date.setText(first_select_day + " ~ "+second_select_day);
                weekly_query = "SELECT * FROM exercise_record WHERE date BETWEEN '"+first_select_day+"' AND '"+second_select_day+"'";
                rangeDateSqlRunning(weekly_query);
//                System.out.println(date_diff);
//                calculatedate(first_select_day , second_select_day);

            }else if(type.equals("3")){
                first_select_day = bundle.getString("first_select_day");
                second_select_day = bundle.getString("second_select_day");
                date_diff = bundle.getString("date_diff");
                itemCount = Integer.parseInt(date_diff) + 1;
                food_date.setText(first_select_day + " ~ "+second_select_day);
                food_query1 = "SELECT strftime('%Y-%m-%d', date) AS date, SUM(mf_kcal) AS total_kcal " +
                            "FROM morning_food " +
                            "WHERE date BETWEEN '" + first_select_day + "' AND '" + second_select_day + "' " +
                            "GROUP BY strftime('%Y-%m-%d', date)";

                food_query2 = "SELECT strftime('%Y-%m-%d', date) AS date, SUM(lf_kcal) AS total_kcal " +
                            "FROM lunch_food " +
                            "WHERE date BETWEEN '" + first_select_day + "' AND '" + second_select_day + "' " +
                            "GROUP BY strftime('%Y-%m-%d', date)";

                food_query3 = "SELECT strftime('%Y-%m-%d', date) AS date, SUM(df_kcal) AS total_kcal " +
                            "FROM dinner_food " +
                            "WHERE date BETWEEN '" + first_select_day + "' AND '" + second_select_day + "' " +
                            "GROUP BY strftime('%Y-%m-%d', date)";

                foodDateSqlRunning(food_query1,food_query2,food_query3);
            }


        }else{
            daily_date.setText(currentDate);
            daily_query = "SELECT * FROM exercise_record WHERE date ='"+currentDate+"'";
            dailySqlRunning(daily_query);
        }
//tab
        TabHost tabHost = view.findViewById(R.id.tabHost);
        tabHost.setup();

//daily_Imagebutton
        daily_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowCalendarPopup();
            }
        });
//weekly tab start
        weekly_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowrangeCalendarPopup();
            }
        });

        food_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowfoodCalendarPopup();
            }
        });

// 첫 번째 탭 추가
        TabHost.TabSpec tab1 = tabHost.newTabSpec("Tab 1");
        tab1.setIndicator("Daily");
        tab1.setContent(R.id.rt_tab_daily); // 탭 내용을 표시할 레이아웃 ID 설정
        tabHost.addTab(tab1);

// 두 번째 탭 추가
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Tab 2");
        tab2.setIndicator("Weekly");
        tab2.setContent(R.id.rt_tab_weekly); // 탭 내용을 표시할 레이아웃 ID 설정
        tabHost.addTab(tab2);

// 네 번째 탭 추가
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Tab 4");
        tab3.setIndicator("Food report");
        tab3.setContent(R.id.rt_food_report); // 탭 내용을 표시할 레이아웃 ID 설정
        tabHost.addTab(tab3);

        int lastTabIdx = getLastSelectedTab();
        tabHost.setCurrentTab(lastTabIdx);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                lastSelectedTabIdx = tabHost.getCurrentTab();
                saveLastSelectedTab(lastSelectedTabIdx);
            }
        });

        return view;
    }

    private void dailySqlRunning(String query){
        DBmanage dbHelper = new DBmanage(getContext());
        report_db=dbHelper.getReadableDatabase();

        Cursor daily_cursor = report_db.rawQuery(query, null);

        daily_row_num = daily_cursor.getCount();

        if(daily_row_num != 0){
            daily_array = new String[daily_row_num][4];
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        if(daily_cursor.moveToFirst()) {
            daily_init_num = 0;
            do {
                String recd_ex_result_Name = daily_cursor.getString(2);
                String recd_ex_process = daily_cursor.getString(3);
                int recd_ex_aim = daily_cursor.getInt(4);
                int recd_ex_burn_kcal = daily_cursor.getInt(5);

                daily_array[daily_init_num][0] = recd_ex_result_Name;
                daily_array[daily_init_num][1] = recd_ex_process;
                daily_array[daily_init_num][2] = String.valueOf(recd_ex_aim);
                daily_array[daily_init_num][3] = String.valueOf(recd_ex_burn_kcal);

                entries.add(new PieEntry(recd_ex_burn_kcal, recd_ex_result_Name));

                daily_burn_kcal = daily_burn_kcal + recd_ex_burn_kcal;

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

                TextView textView = new TextView(row.getContext());
                textView.setText(recd_ex_result_Name);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(22);

                TextView textView2 = new TextView(row.getContext());
                textView2.setText(recd_ex_process);
                textView2.setGravity(Gravity.CENTER);
                textView2.setTextSize(22);

                TextView textView3 = new TextView(row.getContext());
                textView3.setText(String.valueOf(recd_ex_burn_kcal));
                textView3.setGravity(Gravity.CENTER);
                textView3.setTextSize(22);


                row.addView(textView);
                row.addView(textView2);
                row.addView(textView3);

                tableLayout.addView(row);

                daily_init_num++;
            } while (daily_cursor.moveToNext());
        }

        daily_cursor.close();
        report_db.close();

//daily_pie chart
        int itemCount = 9;

        int[] colors = ColorTemplate.MATERIAL_COLORS;
        int[] colors2 = ColorTemplate.LIBERTY_COLORS;

        int[] itemColors = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            if(i < 4){
                itemColors[i] = colors[i];
            }else{
                itemColors[i] = colors2[i % colors.length];
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(itemColors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(22f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.setCenterText("Today Burn Kcal\n" + daily_burn_kcal); daily_burn_kcal = 0;
        pieChart.setCenterTextSize(23f);
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.animateY(1000);
    }

    private void rangeDateSqlRunning(String query){
        DBmanage dbHelper = new DBmanage(getContext());
        report_db=dbHelper.getReadableDatabase();

        Cursor weekly_cursor = report_db.rawQuery(query, null);

        weekly_row_num = weekly_cursor.getCount();

        if(weekly_row_num != 0){
            weekly_array = new String[weekly_row_num][4];
            same_day_array = new String[itemCount];
            day_total_burn = new int[itemCount];
        }

        if(weekly_cursor.moveToFirst()) {
            weekly_init_num = 0;
            day_ck_num = 0;
            total_burn_kcal = 0;
            do {
                String recd_date = weekly_cursor.getString(1);
                String recd_ex_result_Name = weekly_cursor.getString(2);
                String recd_ex_process = weekly_cursor.getString(3);
                int recd_ex_burn_kcal = weekly_cursor.getInt(5);

                weekly_array[weekly_init_num][0] = recd_date;
                weekly_array[weekly_init_num][1] = recd_ex_result_Name;
                weekly_array[weekly_init_num][2] = recd_ex_process;
                weekly_array[weekly_init_num][3] = String.valueOf(recd_ex_burn_kcal);

                total_burn_kcal = total_burn_kcal + recd_ex_burn_kcal;

                if(weekly_init_num > 0) {
                    if (weekly_array[weekly_init_num - 1][0].equals(recd_date)) {
                        weekly_burn_kcal = weekly_burn_kcal + recd_ex_burn_kcal;
                        if(weekly_init_num+1 == weekly_row_num){

                            same_day_array[day_ck_num] = weekly_array[weekly_init_num][0];
                            day_total_burn[day_ck_num] = weekly_burn_kcal;
                            day_ck_num++;
                        }
                    } else {
                        same_day_array[day_ck_num] = weekly_array[weekly_init_num - 1][0];
                        day_total_burn[day_ck_num] = weekly_burn_kcal;
                        day_ck_num++;
                        weekly_burn_kcal = 0;
                        weekly_burn_kcal = weekly_burn_kcal + recd_ex_burn_kcal;

                        TableRow row = new TableRow(weekly_tableLayout.getContext());
                        row.setBackgroundResource(R.drawable.table_outsideline);

                        TextView textView = new TextView(row.getContext());
                        textView.setText(recd_date);
                        textView.setTextColor(Color.BLACK);
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextSize(27);
                        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
                        layoutParams.span = 3; // layout_span 속성 설정
                        row.addView(textView, layoutParams);
                        weekly_tableLayout.addView(row);
                    }
                }else if(weekly_init_num == 0){
                    weekly_burn_kcal = recd_ex_burn_kcal;
                    TableRow row = new TableRow(weekly_tableLayout.getContext());
                    row.setBackgroundResource(R.drawable.table_outsideline);

                    TextView textView = new TextView(row.getContext());
                    textView.setText(recd_date);
                    textView.setTextColor(Color.BLACK);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(27);
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
                    layoutParams.span = 3; // layout_span 속성 설정
                    row.addView(textView, layoutParams);
                    weekly_tableLayout.addView(row);
                }

                TableRow row = new TableRow(weekly_tableLayout.getContext());

                TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                );
                int margin = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10,
                        weekly_tableLayout.getResources().getDisplayMetrics()
                );

                layoutParams.setMargins(0,0,0,margin);

                row.setLayoutParams(layoutParams);

                TextView textView = new TextView(row.getContext());
                textView.setText(recd_ex_result_Name);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(22);

                TextView textView2 = new TextView(row.getContext());
                textView2.setText(recd_ex_process);
                textView2.setGravity(Gravity.CENTER);
                textView2.setTextSize(22);

                TextView textView3 = new TextView(row.getContext());
                textView3.setText(String.valueOf(recd_ex_burn_kcal));
                textView3.setGravity(Gravity.CENTER);
                textView3.setTextSize(22);


                row.addView(textView);
                row.addView(textView2);
                row.addView(textView3);

                weekly_tableLayout.addView(row);

                weekly_init_num++;
            } while (weekly_cursor.moveToNext());
        }

        weekly_total_burn_kcal.setText(String.valueOf(total_burn_kcal));
        weekly_cursor.close();
        report_db.close();

//weekly bar chart

        calculatedate(first_select_day , second_select_day);
        calculatedateDiffver(first_select_day , second_select_day);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);

        ArrayList<BarEntry> bar_entries = new ArrayList<>();
        for(int i = 0; i < itemCount; i++){
            for(int j = 0; j < day_ck_num; j++){
                System.out.println(same_day_array[j]);
                if(tmpArray[i].equals(same_day_array[j])){
                    bar_entries.add(new BarEntry(i, day_total_burn[j]));
                    break;
                }else{
                    if(j == day_ck_num-1){
                        bar_entries.add(new BarEntry(i, 0));
                    }
                }
            }
        }
        BarDataSet barDataSet = new BarDataSet(bar_entries, first_select_day + " ~ "+second_select_day);
        barDataSet.setColors(Color.BLUE);
        barDataSet.setValueTextSize(12f);
        barDataSet.setValueTextColor(Color.BLACK);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(false);         xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getXAxisValues()));

        YAxis leftYAxis = barChart.getAxisLeft();         leftYAxis.setAxisMinimum(0f);
        YAxis rightYAxis = barChart.getAxisRight();         rightYAxis.setEnabled(false);
        rightYAxis.setDrawLabels(false);

        BarData barData = new BarData(dataSets);
        barChart.setTouchEnabled(false);
        barChart.setData(barData);
        barChart.animateXY(1000, 1000);
        barChart.invalidate();

    }

    private void foodDateSqlRunning(String query, String query2, String query3){
        int morning_kcal = 0;
        int lunch_kcal = 0;
        int dinner_kcal =0;

        DBmanage dbHelper = new DBmanage(getContext());
        report_db=dbHelper.getReadableDatabase();

        Cursor food1_cursor = report_db.rawQuery(query, null);

        food1_row_num = food1_cursor.getCount();

        if(food1_row_num != 0){
            morning_kcal_array = new String[food1_row_num][2];
            if(food1_row_num != itemCount){
                morning_kcal_array = new String[itemCount][2];
            }
        }else{
            morning_kcal_array = new String[itemCount][2];
            for(int i = 0; i < itemCount; i ++){
                morning_kcal_array[i][0] = "0";
            }
        }

        if(food1_cursor.moveToFirst()) {
            morning_init_num = 0;
            do {
                String tmp_morning_kcal_date = food1_cursor.getString(0);
                int tmp_morning_kcal = food1_cursor.getInt(1);

                morning_kcal = morning_kcal + tmp_morning_kcal;

                morning_kcal_array[morning_init_num][0] = tmp_morning_kcal_date;
                morning_kcal_array[morning_init_num][1] = String.valueOf(tmp_morning_kcal);
                morning_init_num++;
            } while (food1_cursor.moveToNext());
        }
        food1_cursor.close();

        Cursor food2_cursor = report_db.rawQuery(query2, null);

        food2_row_num = food2_cursor.getCount();

        if(food2_row_num != 0){
            lunch_kcal_array = new String[food2_row_num][2];
            if(food2_row_num != itemCount){
                lunch_kcal_array = new String[itemCount][2];
            }
        }else{
            lunch_kcal_array = new String[itemCount][2];
            for(int i = 0; i < itemCount; i ++){
                lunch_kcal_array[i][0] = "0";
            }
        }

        if(food2_cursor.moveToFirst()) {
            lunch_init_num = 0;
            do {
                String tmp_lunch_kcal_date = food2_cursor.getString(0);
                int tmp_lunch_kcal = food2_cursor.getInt(1);

                lunch_kcal = lunch_kcal + tmp_lunch_kcal;

                lunch_kcal_array[lunch_init_num][0] = tmp_lunch_kcal_date;
                lunch_kcal_array[lunch_init_num][1] = String.valueOf(tmp_lunch_kcal);
                lunch_init_num++;
            } while (food2_cursor.moveToNext());
        }
        food2_cursor.close();

        Cursor food3_cursor = report_db.rawQuery(query3, null);

        food3_row_num = food3_cursor.getCount();
        if(food3_row_num != 0){
            dinner_kcal_array = new String[food3_row_num][2];
            if(food3_row_num != itemCount){
                dinner_kcal_array = new String[itemCount][2];
            }
        }else{
            dinner_kcal_array = new String[itemCount][2];
            for(int i = 0; i < itemCount; i ++){
                dinner_kcal_array[i][0] = "0";
            }
        }

        if(food3_cursor.moveToFirst()) {
            dinner_init_num = 0;
            do {
                String tmp_dinner_kcal_date = food3_cursor.getString(0);
                int tmp_dinner_kcal = food3_cursor.getInt(1);

                dinner_kcal = dinner_kcal + tmp_dinner_kcal;

                dinner_kcal_array[dinner_init_num][0] = tmp_dinner_kcal_date;
                dinner_kcal_array[dinner_init_num][1] = String.valueOf(tmp_dinner_kcal);
                dinner_init_num++;
            } while (food3_cursor.moveToNext());
        }
        food3_cursor.close();

        int total_kcal = morning_kcal + lunch_kcal + dinner_kcal;

        food_kcal.setText(String.valueOf(total_kcal));

        String morning_date_query = "SELECT * FROM morning_food where date BETWEEN '"+first_select_day+"' and '"+second_select_day+"'";
        Cursor morning_date_cursor = report_db.rawQuery(morning_date_query, null);
        morning_date_row_num = morning_date_cursor.getCount();
        if(morning_date_row_num != 0){
            morning_array = new String[morning_date_row_num][4];
        }else{
            morning_array = new String[itemCount][4];
            for(int i = 0; i < itemCount; i++){
                morning_array[i][0] = "0";
            }
        }
        if(morning_date_cursor.moveToFirst()){
            morning_d_init_num = 0;
            do{
                String tmp_morning_date = morning_date_cursor.getString(1);
                String tmp_morning_name = morning_date_cursor.getString(2);
                int tmp_morning_weight = morning_date_cursor.getInt(3);
                int tmp_morning_kcal = morning_date_cursor.getInt(5);

                morning_array[morning_d_init_num][0] = tmp_morning_date;
                morning_array[morning_d_init_num][1] = tmp_morning_name;
                morning_array[morning_d_init_num][2] = String.valueOf(tmp_morning_weight);
                morning_array[morning_d_init_num][3] = String.valueOf(tmp_morning_kcal);

                morning_d_init_num++;
            }while(morning_date_cursor.moveToNext());
        }
        morning_date_cursor.close();

        String lunch_date_query = "SELECT * FROM lunch_food where date BETWEEN '"+first_select_day+"' and '"+second_select_day+"'";
        Cursor lunch_date_cursor = report_db.rawQuery(lunch_date_query, null);
        lunch_date_row_num = lunch_date_cursor.getCount();
        if(lunch_date_row_num != 0){
            lunch_array = new String[lunch_date_row_num][4];
        }else{
            lunch_array = new String[itemCount][4];
            for(int i = 0; i < itemCount; i++){
                lunch_array[i][0] = "0";
            }
        }
        if(lunch_date_cursor.moveToFirst()){
            lunch_d_init_num = 0;
            do{
                String tmp_lunch_date = lunch_date_cursor.getString(1);
                String tmp_lunch_name = lunch_date_cursor.getString(2);
                int tmp_lunch_weight = lunch_date_cursor.getInt(3);
                int tmp_lunch_kcal = lunch_date_cursor.getInt(5);

                lunch_array[lunch_d_init_num][0] = tmp_lunch_date;
                lunch_array[lunch_d_init_num][1] = tmp_lunch_name;
                lunch_array[lunch_d_init_num][2] = String.valueOf(tmp_lunch_weight);
                lunch_array[lunch_d_init_num][3] = String.valueOf(tmp_lunch_kcal);

                lunch_d_init_num++;
            }while(lunch_date_cursor.moveToNext());
        }
        lunch_date_cursor.close();

        String dinner_date_query = "SELECT * FROM dinner_food where date BETWEEN '"+first_select_day+"' and '"+second_select_day+"'";
        Cursor dinner_date_cursor = report_db.rawQuery(dinner_date_query, null);
        dinner_date_row_num = dinner_date_cursor.getCount();
        if(dinner_date_row_num != 0){
            dinner_array = new String[dinner_date_row_num][4];
        }else{
            dinner_array = new String[itemCount][4];
            for(int i = 0; i < itemCount; i++){
                dinner_array[i][0] = "0";
            }
        }
        if(dinner_date_cursor.moveToFirst()){
            dinner_d_init_num = 0;
            do{
                String tmp_dinner_date = dinner_date_cursor.getString(1);
                String tmp_dinner_name = dinner_date_cursor.getString(2);
                int tmp_dinner_weight = dinner_date_cursor.getInt(3);
                int tmp_dinner_kcal = dinner_date_cursor.getInt(5);

                dinner_array[dinner_d_init_num][0] = tmp_dinner_date;
                dinner_array[dinner_d_init_num][1] = tmp_dinner_name;
                dinner_array[dinner_d_init_num][2] = String.valueOf(tmp_dinner_weight);
                dinner_array[dinner_d_init_num][3] = String.valueOf(tmp_dinner_kcal);

                dinner_d_init_num++;
            }while(dinner_date_cursor.moveToNext());
        }
        dinner_date_cursor.close();

        report_db.close();

        // 데이터 엔트리 생성
        calculatedate(first_select_day , second_select_day);
        calculatedateDiffver(first_select_day , second_select_day);

        List<BarEntry> food_entries = new ArrayList<>();

        int m_count = 0, l_count = 0, d_count = 0;
        String state;

        for(int i = 0; i < itemCount; i++){
            System.out.println("roop:"+tmpArray[i]);
            if(tmpArray[i].equals(morning_kcal_array[m_count][0])){
                m_count++;
                if(tmpArray[i].equals(lunch_kcal_array[l_count][0])){
                    l_count++;
                    if(tmpArray[i].equals(dinner_kcal_array[d_count][0])){
                        d_count++;
                        state = "1-1-1";
                    }else{
                        state = "1-1-0";
                    }
                }else{
                    if(tmpArray[i].equals(dinner_kcal_array[d_count][0])){
                        d_count++;
                        state = "1-0-1";
                    }else{
                        state = "1-0-0";
                    }
                }
            }else{
                if(tmpArray[i].equals(lunch_kcal_array[l_count][0])){
                    l_count++;
                    if(tmpArray[i].equals(dinner_kcal_array[d_count][0])){
                        d_count++;
                        state = "0-1-1";
                    }else{
                        state = "0-1-0";
                    }
                }else{
                    if(tmpArray[i].equals(dinner_kcal_array[d_count][0])){
                        d_count++;
                        state = "0-0-1";
                    }else{
                        state = "0-0-0";
                    }
                }
            }

            switch (state){
                case "0-0-0":
                    food_entries.add(new BarEntry(i, new float[]{0, 0, 0}));
                    break;
                case "0-0-1":
                    food_entries.add(new BarEntry(i, new float[]{0, 0, Float.parseFloat(dinner_kcal_array[d_count-1][1])}));
                    break;
                case "0-1-1":
                    food_entries.add(new BarEntry(i, new float[]{0, Float.parseFloat(lunch_kcal_array[l_count-1][1]), Float.parseFloat(dinner_kcal_array[d_count-1][1])}));
                    break;
                case "0-1-0":
                    food_entries.add(new BarEntry(i, new float[]{0, Float.parseFloat(lunch_kcal_array[l_count-1][1]), 0}));
                    break;
                case "1-0-0":
                    food_entries.add(new BarEntry(i, new float[]{Float.parseFloat(morning_kcal_array[m_count-1][1]), 0, 0}));
                    break;
                case "1-0-1":
                    food_entries.add(new BarEntry(i, new float[]{Float.parseFloat(morning_kcal_array[m_count-1][1]), 0, Float.parseFloat(dinner_kcal_array[d_count-1][1])}));
                    break;
                case "1-1-0":
                    food_entries.add(new BarEntry(i, new float[]{Float.parseFloat(morning_kcal_array[m_count-1][1]), Float.parseFloat(lunch_kcal_array[l_count-1][1]), 0}));
                    break;
                case "1-1-1":
                    food_entries.add(new BarEntry(i, new float[]{Float.parseFloat(morning_kcal_array[m_count-1][1]), Float.parseFloat(lunch_kcal_array[l_count-1][1]), Float.parseFloat(dinner_kcal_array[d_count-1][1])}));
                    break;
            }

            if(!state.equals("0-0-0")){
                TableRow row = new TableRow(food_tableLayout.getContext());
                row.setBackgroundResource(R.drawable.table_outsideline);

                TextView textView = new TextView(row.getContext());
                textView.setText(tmpArray[i]);
                textView.setTextColor(Color.BLACK);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(27);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
                layoutParams.span = 3; // layout_span 속성 설정
                row.addView(textView, layoutParams);
                food_tableLayout.addView(row);

                TableRow row2 = new TableRow(food_tableLayout.getContext());
                row2.setBackgroundResource(R.drawable.table_outsideline);

                TextView morning_title = new TextView(row2.getContext());
                morning_title.setText("Breakfast");
                morning_title.setTextColor(Color.BLACK);
                morning_title.setGravity(Gravity.CENTER);
                morning_title.setTextSize(22);
                TableRow.LayoutParams layoutParams2 = new TableRow.LayoutParams();
                layoutParams2.span = 3; // layout_span 속성 설정
                row2.addView(morning_title, layoutParams2);
                food_tableLayout.addView(row2);
            }

            if(morning_d_init_num != 0){
                for(int m = 0; m<morning_d_init_num; m++){
                    if(tmpArray[i].equals(morning_array[m][0])){
                        TableRow m_row = new TableRow(food_tableLayout.getContext());

                        TextView m_textView = new TextView(m_row.getContext());
                        m_textView.setText(morning_array[m][1]);
                        m_textView.setGravity(Gravity.CENTER);
                        m_textView.setTextSize(22);
                        m_textView.setEllipsize(TextUtils.TruncateAt.END);
                        m_textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 2)); // 너비를 고정값으로 설정

                        TextView m_textView2 = new TextView(m_row.getContext());
                        m_textView2.setText(morning_array[m][2]);
                        m_textView2.setGravity(Gravity.CENTER);
                        m_textView2.setTextSize(22);
                        m_textView2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1)); // 너비를 고정값으로 설정

                        TextView m_textView3 = new TextView(m_row.getContext());
                        m_textView3.setText(morning_array[m][3]);
                        m_textView3.setGravity(Gravity.CENTER);
                        m_textView3.setTextSize(22);
                        m_textView3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1)); // 너비를 고정값으로 설정

                        m_row.addView(m_textView);
                        m_row.addView(m_textView2);
                        m_row.addView(m_textView3);

                        food_tableLayout.addView(m_row);
                    }
                }
            }
            if(!state.equals("0-0-0")) {
                TableRow row3 = new TableRow(food_tableLayout.getContext());
                row3.setBackgroundResource(R.drawable.table_outsideline);

                TextView lunch_title = new TextView(row3.getContext());
                lunch_title.setText("Lunch");
                lunch_title.setTextColor(Color.BLACK);
                lunch_title.setGravity(Gravity.CENTER);
                lunch_title.setTextSize(22);
                TableRow.LayoutParams layoutParams3 = new TableRow.LayoutParams();
                layoutParams3.span = 3; // layout_span 속성 설정
                row3.addView(lunch_title, layoutParams3);
                food_tableLayout.addView(row3);
            }
            if(lunch_d_init_num != 0){
                for(int l = 0; l<lunch_d_init_num; l++){
                    if(tmpArray[i].equals(lunch_array[l][0])){
                        TableRow l_row = new TableRow(food_tableLayout.getContext());

                        TextView l_textView = new TextView(l_row.getContext());
                        l_textView.setText(lunch_array[l][1]);
                        l_textView.setGravity(Gravity.CENTER);
                        l_textView.setTextSize(22);
                        l_textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 2)); // 너비를 고정값으로 설정

                        TextView l_textView2 = new TextView(l_row.getContext());
                        l_textView2.setText(lunch_array[l][2]);
                        l_textView2.setGravity(Gravity.CENTER);
                        l_textView2.setTextSize(22);
                        l_textView2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1)); // 너비를 고정값으로 설정

                        TextView l_textView3 = new TextView(l_row.getContext());
                        l_textView3.setText(lunch_array[l][3]);
                        l_textView3.setGravity(Gravity.CENTER);
                        l_textView3.setTextSize(22);
                        l_textView3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1)); // 너비를 고정값으로 설정

                        l_row.addView(l_textView);
                        l_row.addView(l_textView2);
                        l_row.addView(l_textView3);

                        food_tableLayout.addView(l_row);
                    }
                }
            }
            if(!state.equals("0-0-0")) {
                TableRow row4 = new TableRow(food_tableLayout.getContext());
                row4.setBackgroundResource(R.drawable.table_outsideline);

                TextView dinner_title = new TextView(row4.getContext());
                dinner_title.setText("Dinner");
                dinner_title.setTextColor(Color.BLACK);
                dinner_title.setGravity(Gravity.CENTER);
                dinner_title.setTextSize(22);
                TableRow.LayoutParams layoutParams4 = new TableRow.LayoutParams();
                layoutParams4.span = 3; // layout_span 속성 설정
                row4.addView(dinner_title, layoutParams4);
                food_tableLayout.addView(row4);
            }
            if(dinner_d_init_num != 0){
                for(int d = 0; d<dinner_d_init_num; d++){
                    if(tmpArray[i].equals(dinner_array[d][0])){
                        TableRow d_row = new TableRow(food_tableLayout.getContext());

                        TextView d_textView = new TextView(d_row.getContext());
                        d_textView.setText(dinner_array[d][1]);
                        d_textView.setGravity(Gravity.CENTER);
                        d_textView.setTextSize(22);
                        d_textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 2)); // 너비를 고정값으로 설정

                        TextView d_textView2 = new TextView(d_row.getContext());
                        d_textView2.setText(dinner_array[d][2]);
                        d_textView2.setGravity(Gravity.CENTER);
                        d_textView2.setTextSize(22);
                        d_textView2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1)); // 너비를 고정값으로 설정

                        TextView d_textView3 = new TextView(d_row.getContext());
                        d_textView3.setText(dinner_array[d][3]);
                        d_textView3.setGravity(Gravity.CENTER);
                        d_textView3.setTextSize(22);
                        d_textView3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1)); // 너비를 고정값으로 설정

                        d_row.addView(d_textView);
                        d_row.addView(d_textView2);
                        d_row.addView(d_textView3);

                        food_tableLayout.addView(d_row);
                    }
                }
            }

            state = null;
        }


// 데이터셋 생성
        String[] stackLabels = new String[]{"Breakfast", "Lunch", "Dinner"};
        BarDataSet barDataSet = new BarDataSet(food_entries, " ");
        barDataSet.setColors(Color.BLUE, Color.GREEN, Color.RED); // 각 데이터셋에 대한 색상 설정
        barDataSet.setStackLabels(stackLabels); // 세그먼트의 이름 설정
        barDataSet.setValueTextSize(13f);
// 데이터셋 리스트 생성 및 데이터셋 추가
        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
// BarData 생성 및 데이터셋 리스트 설정
        BarData barData = new BarData(dataSets);
// BarChart 설정
        food_barChart.setData(barData);
        food_barChart.setDrawValueAboveBar(true); // 막대 위에 값 표시 설정
        food_barChart.getDescription().setEnabled(false); // 차트 설명 비활성화
        food_barChart.getAxisRight().setEnabled(false); // 오른쪽 Y축 비활성화
        food_barChart.getXAxis().setGranularity(1f); // X축 라벨 간격 설정
        food_barChart.animateXY(1000, 1000);
// X축 설정
        XAxis xAxis = food_barChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 라벨 위치 설정
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getXAxisValues())); // X축 라벨 값 설정
// Y축
        YAxis leftYAxis = food_barChart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f); // 왼쪽 y축의 최소값을 0으로 설정
// 그래프 업데이트
        food_barChart.invalidate();

    }

    private void ShowCalendarPopup(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View ShowCalendarView = inflater.inflate(R.layout.report_calendar_popup, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(ShowCalendarView);
        AlertDialog ShowCalendarDialog = builder.create();

        calendarView = ShowCalendarView.findViewById(R.id.rp_daily_calendarView);

        changeSatColor();
        changeSunColor();

        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // 오늘 날짜인지 확인
                Calendar calendar = Calendar.getInstance();
                CalendarDay today = CalendarDay.today();
                return day.equals(today);
            }

            @Override
            public void decorate(DayViewFacade view) {
                // 오늘 날짜에 대한 스타일 설정
                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_circle));
            }
        });


        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                int year = date.getYear();
                int month = date.getMonth();
                int dayOfMonth = date.getDay();

                String select_date;
                if(month >= 10){
                    if(dayOfMonth < 10){
                        select_date = String.valueOf(year) + "-" +String.valueOf(month)+"-"+ "0"+String.valueOf(dayOfMonth);
                    }else{
                        select_date = String.valueOf(year) + "-" +String.valueOf(month)+"-"+ String.valueOf(dayOfMonth);
                    }
                }else{
                    if(dayOfMonth < 10){
                        select_date = String.valueOf(year) + "-0" +String.valueOf(month)+"-"+ "0"+String.valueOf(dayOfMonth);
                    }else{
                        select_date = String.valueOf(year) + "-0" +String.valueOf(month)+"-" + String.valueOf(dayOfMonth);
                    }
                }

                System.out.println(select_date);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayOfMonth); // month는 0부터 시작하므로 1을 빼줍니다.
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                changeSatColor();
                changeSunColor();
                // 선택한 날짜가 토요일인 경우에만 해당 날짜의 텍스트 색상을 검은색으로 변경합니다.
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    calendarView.addDecorator(new DayViewDecorator() {
                        @Override
                        public boolean shouldDecorate(CalendarDay day) {
                            return day.equals(date);
                        }

                        @Override
                        public void decorate(DayViewFacade view) {
                            view.addSpan(new ForegroundColorSpan(Color.WHITE));
                        }
                    });
                }else{
                    changeSatColor();
                    changeSunColor();
                }

                ShowCalendarDialog.dismiss();
                changefragment(select_date);

            }
        });

        ShowCalendarDialog.show();
    }

    private void ShowrangeCalendarPopup(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View ShowrangeCalendarView = inflater.inflate(R.layout.report_range_calendar_popup, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(ShowrangeCalendarView);
        AlertDialog ShowrangeCalendarDialog = builder.create();

        weekly_calendarView = ShowrangeCalendarView.findViewById(R.id.rp_weekly_calendarView);

        changerangeSatColor();
        changerangeSunColor();

        weekly_calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return true;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_selector));
            }

        });

        weekly_calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // 오늘 날짜인지 확인
                Calendar calendar = Calendar.getInstance();
                CalendarDay today = CalendarDay.today();
                return day.equals(today);
            }

            @Override
            public void decorate(DayViewFacade view) {
                // 오늘 날짜에 대한 스타일 설정
                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_circle));
            }
        });

        weekly_calendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            String f_select_date, s_select_date;
            String f_temp, s_temp;
            long daysBetween;

            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView calendar, @NonNull List<CalendarDay> dates) {

                if (dates.size() >= 2) {
                    CalendarDay firstSelectedDay = dates.get(0);
                    CalendarDay secondSelectedDay = dates.get(dates.size() - 1);


                    f_temp = firstSelectedDay.getDate().toString();
                    s_temp = secondSelectedDay.getDate().toString();


                    LocalDate firstDate = LocalDate.parse(f_temp);
                    LocalDate secondDate = LocalDate.parse(s_temp);
                    // 날짜 간의 차이 계산
                    daysBetween = ChronoUnit.DAYS.between(firstDate, secondDate);

                }

                weekly_calendarView.addDecorator(new DayViewDecorator() {
                    @Override
                    public boolean shouldDecorate(CalendarDay day) {
                        return true;
                    }

                    @Override
                    public void decorate(DayViewFacade view) {
                        view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_selector));
                    }

                });
                if (daysBetween >= 7) {
                    // 7일 이상인 경우 처리 로직
                    Toast.makeText(getContext(), "Exceed the range. Please choose again.", Toast.LENGTH_LONG).show();
                    ShowrangeCalendarDialog.dismiss();

                } else {
                    // 7일 미만인 경우 처리 로직
                    ShowrangeCalendarDialog.dismiss();
                    changeWeeklyfragment(f_temp, s_temp, String.valueOf(daysBetween));
                }


            }
        });


        ShowrangeCalendarDialog.show();
    }

    private void ShowfoodCalendarPopup(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View ShowfoodCalendarView = inflater.inflate(R.layout.report_range_calendar_popup, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(ShowfoodCalendarView);
        AlertDialog ShowfoodCalendarDialog = builder.create();

        food_calendarView = ShowfoodCalendarView.findViewById(R.id.rp_weekly_calendarView);

        changefoodSatColor();
        changefoodSunColor();

        food_calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return true;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_selector));
            }

        });

        food_calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // 오늘 날짜인지 확인
                Calendar calendar = Calendar.getInstance();
                CalendarDay today = CalendarDay.today();
                return day.equals(today);
            }

            @Override
            public void decorate(DayViewFacade view) {
                // 오늘 날짜에 대한 스타일 설정
                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_circle));
            }
        });

        food_calendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            String f_select_date, s_select_date;
            String f_temp, s_temp;
            long daysBetween;

            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView calendar, @NonNull List<CalendarDay> dates) {

                if (dates.size() >= 2) {
                    CalendarDay firstSelectedDay = dates.get(0);
                    CalendarDay secondSelectedDay = dates.get(dates.size() - 1);


                    f_temp = firstSelectedDay.getDate().toString();
                    s_temp = secondSelectedDay.getDate().toString();


                    LocalDate firstDate = LocalDate.parse(f_temp);
                    LocalDate secondDate = LocalDate.parse(s_temp);
                    // 날짜 간의 차이 계산
                    daysBetween = ChronoUnit.DAYS.between(firstDate, secondDate);

                }

                food_calendarView.addDecorator(new DayViewDecorator() {
                    @Override
                    public boolean shouldDecorate(CalendarDay day) {
                        return true;
                    }

                    @Override
                    public void decorate(DayViewFacade view) {
                        view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_selector));
                    }

                });
                if (daysBetween >= 7) {
                    // 7일 이상인 경우 처리 로직
                    Toast.makeText(getContext(), "Exceed the range. Please choose again.", Toast.LENGTH_LONG).show();
                    ShowfoodCalendarDialog.dismiss();

                } else {
                    // 7일 미만인 경우 처리 로직
                    ShowfoodCalendarDialog.dismiss();
                    changeFoodfragment(f_temp, s_temp, String.valueOf(daysBetween));
                }


            }
        });


        ShowfoodCalendarDialog.show();
    }

    private void changefragment(String selectday){
        // 전환하고자 하는 프래그먼트를 생성합니다.
        ReportScreen reportFragment = new ReportScreen();

        // 인자를 설정합니다.
        Bundle bundle = new Bundle();
        bundle.putString("type", "1");
        bundle.putString("select_day", selectday);
        reportFragment.setArguments(bundle);

        // 현재 화면의 프래그먼트 매니저를 가져옵니다.
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, reportFragment);
        fragmentTransaction.commit();

    }

    private void changeWeeklyfragment(String first_day, String second_day, String day_diff){
        // 전환하고자 하는 프래그먼트를 생성합니다.
        ReportScreen reportFragment = new ReportScreen();

        // 인자를 설정합니다.
        Bundle bundle = new Bundle();
        bundle.putString("type", "2");
        bundle.putString("first_select_day", first_day);
        bundle.putString("second_select_day", second_day);
        bundle.putString("date_diff", day_diff);
        reportFragment.setArguments(bundle);

        // 현재 화면의 프래그먼트 매니저를 가져옵니다.
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, reportFragment);
        fragmentTransaction.commit();

    }

    private void changeFoodfragment(String first_day, String second_day, String day_diff){
        // 전환하고자 하는 프래그먼트를 생성합니다.
        ReportScreen reportFragment = new ReportScreen();

        // 인자를 설정합니다.
        Bundle bundle = new Bundle();
        bundle.putString("type", "3");
        bundle.putString("first_select_day", first_day);
        bundle.putString("second_select_day", second_day);
        bundle.putString("date_diff", day_diff);
        reportFragment.setArguments(bundle);

        // 현재 화면의 프래그먼트 매니저를 가져옵니다.
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, reportFragment);
        fragmentTransaction.commit();

    }

    private void changeSatColor() {

        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // Check if the day is Saturday
                int year = day.getYear();
                int month = day.getMonth();
                int dayOfMonth = day.getDay();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                return dayOfWeek == Calendar.SATURDAY;
            }

            @Override
            public void decorate(DayViewFacade view) {
                // Set the text color of Saturdays to blue
                view.addSpan(new ForegroundColorSpan(Color.BLUE));
            }
        });
    }

    private void changeSunColor() {
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // Check if the day is Sunday
                int year = day.getYear();
                int month = day.getMonth();
                int dayOfMonth = day.getDay();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                return dayOfWeek == Calendar.SUNDAY;
            }

            @Override
            public void decorate(DayViewFacade view) {
                // Set the text color of Sundays to red
                view.addSpan(new ForegroundColorSpan(Color.RED));
            }
        });
    }

    private void changerangeSatColor() {

        weekly_calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // Check if the day is Saturday
                int year = day.getYear();
                int month = day.getMonth();
                int dayOfMonth = day.getDay();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                return dayOfWeek == Calendar.SATURDAY;
            }

            @Override
            public void decorate(DayViewFacade view) {
                // Set the text color of Saturdays to blue
                view.addSpan(new ForegroundColorSpan(Color.BLUE));
            }
        });
    }

    private void changerangeSunColor() {
        weekly_calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // Check if the day is Sunday
                int year = day.getYear();
                int month = day.getMonth();
                int dayOfMonth = day.getDay();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                return dayOfWeek == Calendar.SUNDAY;
            }

            @Override
            public void decorate(DayViewFacade view) {
                // Set the text color of Sundays to red
                view.addSpan(new ForegroundColorSpan(Color.RED));
            }
        });
    }

    private void changefoodSatColor() {

        food_calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // Check if the day is Saturday
                int year = day.getYear();
                int month = day.getMonth();
                int dayOfMonth = day.getDay();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                return dayOfWeek == Calendar.SATURDAY;
            }

            @Override
            public void decorate(DayViewFacade view) {
                // Set the text color of Saturdays to blue
                view.addSpan(new ForegroundColorSpan(Color.BLUE));
            }
        });
    }

    private void changefoodSunColor() {
        food_calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // Check if the day is Sunday
                int year = day.getYear();
                int month = day.getMonth();
                int dayOfMonth = day.getDay();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                return dayOfWeek == Calendar.SUNDAY;
            }

            @Override
            public void decorate(DayViewFacade view) {
                // Set the text color of Sundays to red
                view.addSpan(new ForegroundColorSpan(Color.RED));
            }
        });
    }

    private void rewritedate(String temp){
        String[] dateArray = temp.split("-");
        String year = dateArray[0];
        String month = dateArray[1];
        String day = dateArray[2];

        rewrite_date = day+"-"+month+"-"+year;
    }

    private void calculatedate(String tmp1, String tmp2){
        // SimpleDateFormat을 사용하여 String을 Date로 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = dateFormat.parse(tmp1);
            endDate = dateFormat.parse(tmp2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 시작 날짜부터 종료 날짜까지의 모든 날짜를 생성
        List<Date> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            dateList.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        }

        // 날짜를 다시 String으로 변환하여 배열에 추가
        tmpArray = new String[dateList.size()];
        for (int i = 0; i < dateList.size(); i++) {
            tmpArray[i] = dateFormat.format(dateList.get(i));
            System.out.println("class : "+tmpArray[i]);
        }

    }

    private void calculatedateDiffver(String tmp1, String tmp2){
        // SimpleDateFormat을 사용하여 String을 Date로 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = dateFormat.parse(tmp1);
            endDate = dateFormat.parse(tmp2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 시작 날짜부터 종료 날짜까지의 모든 날짜를 생성
        List<Date> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            dateList.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        }

        // 날짜를 다시 String으로 변환하여 배열에 추가
        tmpArray_diffver = new String[dateList.size()];
        for (int i = 0; i < dateList.size(); i++) {
            tmpArray_diffver[i] = dateFormat.format(dateList.get(i));
        }

    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxisValues = new ArrayList<>();
        for(int i =  0; i < itemCount ; i++){
            xAxisValues.add(tmpArray_diffver[i]);
        }
        return xAxisValues;
    }

    // 마지막으로 선택한 탭의 인덱스를 저장하는 메서드
    private void saveLastSelectedTab(int tabIdx) {
        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lastSelectedTab", tabIdx);
        editor.apply();
    }

    // 마지막으로 선택한 탭의 인덱스를 복원하는 메서드
    private int getLastSelectedTab() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getInt("lastSelectedTab", 0);
    }
}