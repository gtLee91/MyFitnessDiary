package com.hfad.assignment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;

import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;
import java.util.TimeZone;

public class HomeScreen extends Fragment {


    private SQLiteDatabase db;
    private  TextView val, home_tot_kcal, home_burn_kcal;
    String gen;
    int user_age, user_height, user_weight;
    int total_kcal;
    double cal_val;
    private TableLayout tableLayout;
    public String[][] home_plan_array;
    TimeZone nzTimeZone = TimeZone.getTimeZone("Pacific/Auckland");
    Calendar nzCalendar = Calendar.getInstance(nzTimeZone);
    long now = System.currentTimeMillis();

//    private CalendarView calendarView;
    private MaterialCalendarView calendarView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment,container,false);
        //calendar
        calendarView = view.findViewById(R.id.calendarView);

        home_burn_kcal = view.findViewById(R.id.home_burn_kcal);
        //currentDate
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(nzCalendar.getTime());
        //DB
        DBmanage dbHelper = new DBmanage(getContext());
        db=dbHelper.getReadableDatabase();

        String ck_exist_data_sql = "SELECT * from user_info";
        Cursor cursor = db.rawQuery(ck_exist_data_sql, null);

        cursor.moveToFirst();
        String name = cursor.getString(1);
        gen = cursor.getString(2);
        user_age = cursor.getInt(3);
        user_height = cursor.getInt(4);
        user_weight = cursor.getInt(5);

        if(gen.equals("Male")) {
            cal_val = (10 * user_weight) + (6.25 * user_height) - (5 * user_age) + 5;
        }else{
            cal_val = (10 * user_weight) + (6.25 * user_height) - (5 * user_age) - 161;
        }


        val = view.findViewById(R.id.BMR_value);
        val.setText(Double.toString(cal_val));

        cursor.close();
//        db.close();

        home_tot_kcal = view.findViewById(R.id.home_today_kcal);

        String query1 = "SELECT SUM(mf_kcal) FROM morning_food WHERE date ='"+currentDate+"'";
        Cursor cursor1 = db.rawQuery(query1, null);
        if(cursor1.moveToFirst()){
            int mf_kcal = cursor1.getInt(0);
            total_kcal = mf_kcal;
        }
        cursor1.close();

        String query2 = "SELECT SUM(lf_kcal) FROM lunch_food WHERE date ='"+currentDate+"'";
        Cursor cursor2 = db.rawQuery(query2, null);
        if(cursor2.moveToFirst()){
            int lf_kcal = cursor2.getInt(0);
            total_kcal = total_kcal + lf_kcal;
        }
        cursor2.close();

        String query3 = "SELECT SUM(df_kcal) FROM dinner_food WHERE date ='"+currentDate+"'";
        Cursor cursor3 = db.rawQuery(query3, null);
        if(cursor3.moveToFirst()){
            int df_kcal = cursor3.getInt(0);
            total_kcal = total_kcal + df_kcal;
        }
        cursor3.close();
        home_tot_kcal.setText(String.valueOf(total_kcal));


        tableLayout = view.findViewById(R.id.home_ex_plan);

        String query = "SELECT * FROM exercise_plan where date='"+currentDate+"'";
        Cursor cursor4 = db.rawQuery(query, null);

        int plan_row_num = cursor4.getCount();

        if(plan_row_num != 0) {
            home_plan_array = new String[plan_row_num][4];
        }

        if (cursor4.moveToFirst()) {
            int init_num = 0;
            do {
                String ex_Name = cursor4.getString(2);
                int ex_count = cursor4.getInt(3);
                int ex_sets = cursor4.getInt(4);
                int ex_time = cursor4.getInt(5);

                home_plan_array[init_num][0] = ex_Name;
                home_plan_array[init_num][1] = String.valueOf(ex_count);
                home_plan_array[init_num][2] = String.valueOf(ex_sets);
                home_plan_array[init_num][3] = String.valueOf(ex_time);

                TableRow row = new TableRow(tableLayout.getContext());
//                row.setBackgroundResource(R.drawable.table_outsideline);
                TextView textView = new TextView(row.getContext());
                textView.setText(home_plan_array[init_num][0]);
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundResource(R.drawable.table_outsideline);
                row.addView(textView);
                if("0".equals(home_plan_array[init_num][1])){
                    TextView textView2 = new TextView(row.getContext());
                    textView2.setText(home_plan_array[init_num][3] + " min");
                    textView2.setTextSize(20);
                    textView2.setGravity(Gravity.CENTER);
                    textView2.setBackgroundResource(R.drawable.table_outsideline);
                    row.addView(textView2);
                }else{
                    int temp_aim = ex_count * ex_sets;

                    TextView textView2 = new TextView(row.getContext());
                    textView2.setText(String.valueOf(temp_aim));
                    textView2.setTextSize(20);
                    textView2.setGravity(Gravity.CENTER);
                    textView2.setBackgroundResource(R.drawable.table_outsideline);
                    row.addView(textView2);
                }
                tableLayout.addView(row);
                init_num++;
            } while (cursor4.moveToNext());

        }
        cursor4.close();

        String query5 = "SELECT * FROM exercise_record where date='"+currentDate+"'";
        Cursor cursor5 = db.rawQuery(query5, null);
        int today_burn_kcal = 0;
        if (cursor5.moveToFirst()) {
            int init_num = 0;
            do {
                int burn_kcal = cursor5.getInt(5);
                today_burn_kcal = today_burn_kcal + burn_kcal;
                init_num++;
            } while (cursor5.moveToNext());

        }
        home_burn_kcal.setText(String.valueOf(today_burn_kcal)); today_burn_kcal = 0;
        cursor5.close();


        String query6 = "SELECT DISTINCT date FROM exercise_plan";
        Cursor cursor6 = db.rawQuery(query6, null);

        if(cursor6.moveToFirst()){
            int init_num = 0;
            do{
                String sd_day = cursor6.getString(0);
                System.out.println(sd_day);
                decorateDateWithDot(sd_day);

                init_num++;
            } while (cursor6.moveToNext());
        }
        cursor6.close();
        db.close();

        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader);
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                // 모든 날짜에 스타일을 적용하기 위해 true를 반환
                return true;
            }

            @Override
            public void decorate(DayViewFacade view) {
                // 날짜 숫자에 대한 스타일 설정
                view.addSpan(new AbsoluteSizeSpan(18, true));
            }
        });

        changeSunColor();

        changeSatColor();

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

//                System.out.println(select_date);

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

                DBmanage dbHelper = new DBmanage(getContext());
                db=dbHelper.getReadableDatabase();

                String plan_ck_query = "SELECT * FROM exercise_plan where date='"+select_date+"'";
                Cursor plan_ck_cursor = db.rawQuery(plan_ck_query, null);

                int plan_row_num = plan_ck_cursor.getCount();
                String popup_ment;
                if(plan_row_num == 0){
                    popup_ment = "Do you want to add "+select_date+" plan?";
                }else{
                    popup_ment = "Do you want to check "+select_date+" plan?";
                }

                AskPopupWindow(select_date, popup_ment);

            }
        });


        return view;
    }

    private void AskPopupWindow(String selectday, String ment){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View AskPopupView = inflater.inflate(R.layout.home_select_plan, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(AskPopupView);
        AlertDialog AskPopupDialog = builder.create();

        TextView text = AskPopupView.findViewById(R.id.select_text);
        text.setText(ment);
        text.setTextSize(24);

        Button in_save_btn = AskPopupView.findViewById(R.id.home_pl_go_button);
        Button in_cancel_btn = AskPopupView.findViewById(R.id.home_pl_cancel_button);

        in_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AskPopupDialog.dismiss();
                changefragment(selectday);
            }
        });

        in_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AskPopupDialog.dismiss();
            }
        });

        AskPopupDialog.show();
    }

    private void changefragment(String selectday ){
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

    private void decorateDateWithDot(String targetDate) {
        calendarView.addDecorator(new DayViewDecorator() {

            String dateStr = targetDate;

            String[] parts = dateStr.split("-");
            String yearStr = parts[0];
            String monthStr = parts[1];
            String dayStr = parts[2];

            int sd_day = Integer.parseInt(dayStr);
            int sd_month = Integer.parseInt(monthStr);
            int sd_year = Integer.parseInt(yearStr);

            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return day.getYear() == sd_year
                        && day.getMonth() == sd_month
                        && day.getDay() == sd_day;
            }

            @Override
            public void decorate(DayViewFacade view) {
                // 오늘 날짜에 대한 스타일 설정
                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circle_dotted));
            }
        });
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

    private void changeStructuredate(String changedate){
        String inputDate = changedate;
        String outputPattern = "dd-MM-yyyy";

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        try {
            Date date = inputFormat.parse(inputDate);
            String outputDate = outputFormat.format(date);

            System.out.println("변환된 날짜: " + outputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
