package com.hfad.assignment;
import static androidx.core.app.ServiceCompat.stopForeground;
import static androidx.core.content.ContextCompat.startForegroundService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.app.Service;

import android.os.IBinder;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class TodoScreen extends Fragment{


    private SQLiteDatabase Todo_db;
    private TextView today_date;
    private TextView total_burn_kcal_value;
    TimeZone nzTimeZone = TimeZone.getTimeZone("Pacific/Auckland");
    Calendar nzCalendar = Calendar.getInstance(nzTimeZone);
    long now = System.currentTimeMillis();

    public String[][] Todo_array;
    public String[][] Todo_recd_array;
    private TableLayout tableLayout;

    public Chronometer chronometer;
    private ImageButton startButton;
    private ImageButton stopButton;
    public int init_num;
    public int record_init_num;
    public int today_burn_kcal = 0;
    public int init_row_num;
    public int user_w;
    public double MET;
    public int elapsedMinutes;
    ArrayAdapter<String> spinnerAdapter;
    public String timer_value;
    public String currentDate;
    public String selected_spinner;

    private boolean isChronometerRunning = false;
    private boolean isTimerPaused = false;
    private long stoppedTime = 0;

    boolean isServiceRunning;
    private TimerService timerService;
    private boolean isServiceBound = false; // TimerService와의 바인딩 상태를 나타내는 변수

    private Context context;

    private ServiceConnection savedServiceConnection;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.todo_fragment,container,false);

        today_date = view.findViewById(R.id.todo_today_date);
        tableLayout = view.findViewById(R.id.Todo_tableLayout);
        startButton = view.findViewById(R.id.timer_start_button);
        stopButton = view.findViewById(R.id.timer_stop_button);
        chronometer = view.findViewById(R.id.myChronometer);
        total_burn_kcal_value = view.findViewById(R.id.total_burn_kcal_value);

        isServiceRunning = isTimerServiceRunning();

        if (timerService != null) {
            // timerService가 연결되어 있는 경우
            System.out.println("timerService가 연결되어 있습니다." + timerService);
            isServiceBound = true;
        } else {
            // timerService가 연결되어 있지 않은 경우
            System.out.println("timerService가 연결되어 있지 않습니다.");
            isServiceBound = false;
        }

        System.out.println("---------------------------------------");
        System.out.println("isServicerunning_0 : "+ isServiceRunning);
        System.out.println("isServiceBound_0 : "+ isServiceBound);
        System.out.println("istimerpaused_0 :" + isTimerPaused);
        System.out.println("isChronometerRunning_0 :" + isChronometerRunning);

        if (isServiceRunning && !isTimerPaused) {
            // 동작 중인 타이머의 값을 가져와서 Chronometer에 설정
            long elapsedTime = TimerService.getElapsedTimeFromService();
            setChronometerElapsedTime(elapsedTime);
            isChronometerRunning = true;
            if(!isServiceBound){// 강제 종료 됬을 시 돌아가는 코드
                bindTimerService();


                pauseTimerService();
                stopChronometer();

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startTimerService();
//                }
                startChronometer();
                resumeTimerService();
//                restoreServiceConnection();
            }
            startButton.setBackgroundResource(R.drawable.pause);
        }else if(isServiceRunning && isTimerPaused){
            long elapsedTime = TimerService.getElapsedTimeFromService();
            setPauseChronometerElapsedTime(elapsedTime);
        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = sdf.format(nzCalendar.getTime());

        today_date.setText(currentDate);

        DBmanage dbHelper = new DBmanage(getContext());
        Todo_db=dbHelper.getReadableDatabase();

        String user_w_query = "select weight from user_info";
        Cursor user_w_cursor = Todo_db.rawQuery(user_w_query, null);


        if(user_w_cursor.moveToFirst()) {
            do {
                user_w = user_w_cursor.getInt(0);
            }while (user_w_cursor.moveToNext());
        }
        user_w_cursor.close();

        String init_query = "SELECT * FROM exercise_record WHERE date ='"+currentDate+"'";
        Cursor init_cursor = Todo_db.rawQuery(init_query, null);

        init_row_num = init_cursor.getCount();

        if(init_row_num != 0){
            Todo_recd_array = new String[init_row_num][4];
        }

        if(init_cursor.moveToFirst()) {
            record_init_num = 0;
            do {
                String recd_ex_result_Name = init_cursor.getString(2);
                String recd_ex_process = init_cursor.getString(3);
                int recd_ex_aim = init_cursor.getInt(4);
                int recd_ex_burn_kcal = init_cursor.getInt(5);

                Todo_recd_array[record_init_num][0] = recd_ex_result_Name;
                Todo_recd_array[record_init_num][1] = recd_ex_process;
                Todo_recd_array[record_init_num][2] = String.valueOf(recd_ex_aim);
                Todo_recd_array[record_init_num][3] = String.valueOf(recd_ex_burn_kcal);

                today_burn_kcal = today_burn_kcal + recd_ex_burn_kcal;

                record_init_num++;
            } while (init_cursor.moveToNext());
        }
        total_burn_kcal_value.setText(String.valueOf(today_burn_kcal));
        today_burn_kcal = 0;
        init_cursor.close();
        Todo_db.close();



        if(init_row_num == 0){
            Todo_db=dbHelper.getReadableDatabase();

            String query1 = "SELECT * FROM exercise_plan WHERE date ='"+currentDate+"'";
            Cursor cursor1 = Todo_db.rawQuery(query1, null);

            int row_num = cursor1.getCount();

            if(row_num != 0) {
                Todo_array = new String[row_num][3];
            }

            if(cursor1.moveToFirst()) {
                init_num = 0;
                do {
                    String ex_result_Name = cursor1.getString(2);
                    int ex_p_ct = cursor1.getInt(3);
                    int ex_p_sets = cursor1.getInt(4);
                    int ex_p_time = cursor1.getInt(5);

                    int target = ex_p_ct * ex_p_sets;

                    Todo_array[init_num][0] = ex_result_Name;
                    Todo_array[init_num][1] = String.valueOf(target);
                    Todo_array[init_num][2] = String.valueOf(ex_p_time);

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
                    textView.setText(Todo_array[init_num][0]);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(22);

                    TextView textView2 = new TextView(row.getContext());
                    if(ex_p_ct == 0 && ex_p_sets == 0){
                        String t = "00:00";
                        textView2.setText(t);
                    }else{
                        textView2.setText("0");
                    }
                    textView2.setGravity(Gravity.CENTER);
                    textView2.setTextSize(22);

                    TextView textView3 = new TextView(row.getContext());
                    if(ex_p_ct == 0 && ex_p_sets == 0){
                        textView3.setText(Todo_array[init_num][2]);
                    }else{
                        textView3.setText(Todo_array[init_num][1]);
                    }
                    textView3.setGravity(Gravity.CENTER);
                    textView3.setTextSize(22);

                    TextView textView4 = new TextView(row.getContext());
                    textView4.setText("0");
                    textView4.setGravity(Gravity.CENTER);
                    textView4.setTextSize(22);

                    row.addView(textView);
                    row.addView(textView2);
                    row.addView(textView3);
                    row.addView(textView4);

                    tableLayout.addView(row);

                    init_num++;
                } while (cursor1.moveToNext());
            }
            cursor1.close();
            Todo_db.close();


            //exercise_plan data add in exercise_record table
            Todo_db=dbHelper.getWritableDatabase();
            for(int i = 0; i < init_num; i++){
                if(Todo_array[i][1].equals("0")){
                    String zero_progress = "00:00";
                    Todo_db.execSQL("insert into exercise_record (date, recd_ex_name, recd_ex_progress, recd_ex_aim, recd_burn_kcal) values('"+currentDate+"','"+Todo_array[i][0]+"','"+zero_progress+"',"+Todo_array[i][2]+","+0+");");
                }else{
                    Todo_db.execSQL("insert into exercise_record (date, recd_ex_name, recd_ex_progress, recd_ex_aim, recd_burn_kcal) values('"+currentDate+"','"+Todo_array[i][0]+"','"+0+"',"+Todo_array[i][1]+","+0+");");
                }

            }
            Todo_db.close();
        }else{
            Todo_db=dbHelper.getReadableDatabase();

            String query2 = "SELECT * FROM exercise_plan WHERE date ='"+currentDate+"'";
            Cursor cursor2 = Todo_db.rawQuery(query2, null);

            int row_num = cursor2.getCount();

            if(row_num != 0) {
                Todo_array = new String[row_num][3];
            }

            if(cursor2.moveToFirst()) {
                init_num = 0;
                do {
                    String ex_result_Name = cursor2.getString(2);
                    int ex_p_ct = cursor2.getInt(3);
                    int ex_p_sets = cursor2.getInt(4);
                    int ex_p_time = cursor2.getInt(5);

                    int target = ex_p_ct * ex_p_sets;

                    Todo_array[init_num][0] = ex_result_Name;
                    Todo_array[init_num][1] = String.valueOf(target);
                    Todo_array[init_num][2] = String.valueOf(ex_p_time);

                    init_num++;
                } while (cursor2.moveToNext());
            }
            cursor2.close();
            Todo_db.close();

            if(init_num != init_row_num){

                if(init_num > init_row_num){
                    for(int i = 0; i < init_num; i++){
                        Todo_db=dbHelper.getReadableDatabase();
                        String ck_query = "SELECT * FROM exercise_record WHERE date ='"+currentDate+"' and recd_ex_name = '"+Todo_array[i][0]+"'";
                        Cursor ck_cursor = Todo_db.rawQuery(ck_query, null);
                        int ck_row = ck_cursor.getCount();
                        if(ck_row == 0){
                            Todo_db=dbHelper.getWritableDatabase();
                            if(Todo_array[i][1].equals("0")){
                                String zero_progress = "00:00";
                                Todo_db.execSQL("insert into exercise_record (date, recd_ex_name, recd_ex_progress, recd_ex_aim, recd_burn_kcal) values('"+currentDate+"','"+Todo_array[i][0]+"','"+zero_progress+"',"+Todo_array[i][2]+","+0+");");

                            }else{
                                Todo_db.execSQL("insert into exercise_record (date, recd_ex_name, recd_ex_progress, recd_ex_aim, recd_burn_kcal) values('"+currentDate+"','"+Todo_array[i][0]+"','"+0+"',"+Todo_array[i][1]+","+0+");");

                            }
                        }
                        ck_cursor.close();
                        Todo_db.close();
                    }
                }
                else if(init_num < init_row_num){
                    for(int i = 0; i < init_row_num; i++){
                        Todo_db=dbHelper.getReadableDatabase();
                        String del_ck_query = "SELECT * FROM exercise_plan WHERE date ='"+currentDate+"' and p_ex = '"+Todo_recd_array[i][0]+"'";
                        Cursor del_ck_cursor = Todo_db.rawQuery(del_ck_query, null);
                        int del_ck_row = del_ck_cursor.getCount();
                        if(del_ck_row == 0){
                            Todo_db=dbHelper.getWritableDatabase();
                            Todo_db.execSQL("DELETE FROM exercise_record WHERE date = '" + currentDate + "' AND recd_ex_name = '"+ Todo_recd_array[i][0] +"'");
                        }
                        del_ck_cursor.close();
                        Todo_db.close();
                    }

                }

            }

            Todo_db=dbHelper.getReadableDatabase();

            Cursor last_cursor = Todo_db.rawQuery(init_query, null);

            init_row_num = last_cursor.getCount();

            if(init_row_num != 0){
                Todo_recd_array = new String[init_row_num][4];
            }

            if(last_cursor.moveToFirst()) {
                record_init_num = 0;
                do {
                    String recd_ex_result_Name = last_cursor.getString(2);
                    String recd_ex_process = last_cursor.getString(3);
                    int recd_ex_aim = last_cursor.getInt(4);
                    int recd_ex_burn_kcal = last_cursor.getInt(5);

                    Todo_recd_array[record_init_num][0] = recd_ex_result_Name;
                    Todo_recd_array[record_init_num][1] = recd_ex_process;
                    Todo_recd_array[record_init_num][2] = String.valueOf(recd_ex_aim);
                    Todo_recd_array[record_init_num][3] = String.valueOf(recd_ex_burn_kcal);

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
                    textView.setText(Todo_recd_array[record_init_num][0]);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(22);

                    TextView textView2 = new TextView(row.getContext());
                    textView2.setText(Todo_recd_array[record_init_num][1]);
                    textView2.setGravity(Gravity.CENTER);
                    textView2.setTextSize(22);
                    if(!Todo_array[record_init_num][1].equals("0")){
                        textView2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openInputPopupWindow(recd_ex_result_Name);
                            }
                        });
                    }

                    TextView textView3 = new TextView(row.getContext());
                    textView3.setText(Todo_recd_array[record_init_num][2]);
                    textView3.setGravity(Gravity.CENTER);
                    textView3.setTextSize(22);

                    TextView textView4 = new TextView(row.getContext());
                    textView4.setText(Todo_recd_array[record_init_num][3]);
                    textView4.setGravity(Gravity.CENTER);
                    textView4.setTextSize(22);

                    row.addView(textView);
                    row.addView(textView2);
                    row.addView(textView3);
                    row.addView(textView4);

                    tableLayout.addView(row);

                    record_init_num++;
                } while (last_cursor.moveToNext());
            }
            last_cursor.close();
            Todo_db.close();

        }


        startButton.setOnClickListener(new View.OnClickListener() {

//            boolean isRunning = false;
            @Override
            public void onClick(View view) {

                if (!isChronometerRunning) {
                    System.out.println("---------------------------------------");
                    System.out.println("시작 버튼 눌렀을 때");
                    System.out.println("isServicerunning : "+ isServiceRunning);
                    System.out.println("isServiceBound : "+ isServiceBound);
                    System.out.println("istimerpaused :" + isTimerPaused);
                    System.out.println("isChronometerRunning :" + isChronometerRunning);
                    System.out.println("시작 버튼 바인딩 service "+timerService);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startTimerService();
                    }
                    startChronometer();
                    bindTimerService();
                    resumeTimerService();
                }else {
                    System.out.println("---------------------------------------");
                    System.out.println("일시정지 버튼 눌렀을 때");
                    System.out.println("isServicerunning : "+ isServiceRunning);
                    System.out.println("isServiceBound : "+ isServiceBound);
                    System.out.println("istimerpaused :" + isTimerPaused);
                    System.out.println("isChronometerRunning :" + isChronometerRunning);
                    System.out.println("일시정지 버튼 바인딩 service "+timerService);
                    pauseTimerService();
                    stopChronometer();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> ex_name_List = new ArrayList<>();
                for(int i = 0; i < init_num; i++){
                    if (!Todo_array[i][2].equals("0")) {
                        ex_name_List.add(Todo_array[i][0]);
                    }
                }

                spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, ex_name_List);
                timer_value = chronometer.getText().toString();

                pauseTimerService();
                stopChronometer();

                openPopupWindow(timer_value);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.context = context;
    }

    private boolean isTimerServiceRunning() {
        ActivityManager manager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo service : runningServices) {
                if (TimerService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerServiceBinder binder = (TimerService.TimerServiceBinder) service;
            timerService = binder.getService();
            isServiceBound = true;
            System.out.println("service bind connect 완료!!" + timerService);
            System.out.println("current connection " + serviceConnection);
            System.out.println("saved connection " + savedServiceConnection);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            timerService = null;
            isServiceBound = false;
            System.out.println("service bind disconnect 됨");
        }
    };

    private void bindTimerService() {
        if (!isServiceBound) { // TimerService에 아직 바인딩되지 않은 경우에만 바인딩
            System.out.println("현재 바인딩 되어 있는 service는 " + timerService);
            Intent intent = new Intent(getContext(), TimerService.class);
            // 서비스 바인딩 전에 ServiceConnection 객체를 저장
            System.out.println("savedService valuse "+ savedServiceConnection);
            savedServiceConnection = serviceConnection;
            getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            System.out.println("bind timer service completed");
        }
    }

    private void unbindTimerService() {
        if (isServiceBound) { // TimerService에 바인딩된 경우에만 언바인딩
            System.out.println("현재 바인딩 되어 있는 service는 " + timerService);
            getContext().unbindService(serviceConnection);
            isServiceBound = false;
            timerService = null;
            System.out.println("unbind timer service completed");
        }
    }

    private void setChronometerElapsedTime(long elapsedTime) {
        long baseTime = SystemClock.elapsedRealtime() - elapsedTime;
        chronometer.setBase(baseTime);
        chronometer.start();
    }

    private void setPauseChronometerElapsedTime(long elapsedTime) {
        long baseTime = SystemClock.elapsedRealtime() - elapsedTime;
        chronometer.setBase(baseTime);
    }

    private void startChronometer() {
        if (!isChronometerRunning) {
            System.out.println("---------------------------------------");
            System.out.println("before startch_isServicerunning : "+ isServiceRunning);
            System.out.println("before startch_isServiceBound : "+ isServiceBound);
            System.out.println("before startch_istimerpaused :" + isTimerPaused);
            System.out.println("before startch_isChronometerRunning :" + isChronometerRunning);
            if (stoppedTime != 0) {
                long elapsedTime = TimerService.getElapsedTimeFromService();
                setPauseChronometerElapsedTime(elapsedTime);
            } else {
                chronometer.setBase(SystemClock.elapsedRealtime());
            }
            chronometer.start();
            isChronometerRunning = true;
            startButton.setBackgroundResource(R.drawable.pause);

            System.out.println("---------------------------------------");
            System.out.println("after startch_isServicerunning : "+ isServiceRunning);
            System.out.println("after startch_isServiceBound : "+ isServiceBound);
            System.out.println("after startch_istimerpaused :" + isTimerPaused);
            System.out.println("after startch_isChronometerRunning :" + isChronometerRunning);
        }
    }

    private void stopChronometer() {
        if (isChronometerRunning) {
            System.out.println("---------------------------------------");
            System.out.println("before stopch_isServicerunning : "+ isServiceRunning);
            System.out.println("before stopch_isServiceBound : "+ isServiceBound);
            System.out.println("before stopch_istimerpaused :" + isTimerPaused);
            System.out.println("before stopch_isChronometerRunning :" + isChronometerRunning);
            chronometer.stop();
            stoppedTime = SystemClock.elapsedRealtime();
            isChronometerRunning = false;
            startButton.setBackgroundResource(R.drawable.start);
            System.out.println("---------------------------------------");
            System.out.println("after stopch_isServicerunning : "+ isServiceRunning);
            System.out.println("after stopch_isServiceBound : "+ isServiceBound);
            System.out.println("after stopch_istimerpaused :" + isTimerPaused);
            System.out.println("after stopch_isChronometerRunning :" + isChronometerRunning);
        }
    }


    private void pauseTimerService() {
        if (isServiceBound && !isTimerPaused && isChronometerRunning) { // TimerService에 바인딩되었고 타이머가 일시 정지되지 않은 경우에만 일시 정지 처리
            timerService.pauseTimer();
            isTimerPaused = true;
            System.out.println("pauseTimerService 호출됨");
        }
    }

    private void resumeTimerService() {
        if (isServiceBound && isTimerPaused && isChronometerRunning) { // TimerService에 바인딩되었고 타이머가 일시 정지된 경우에만 일시 정지 해제 처리
            timerService.resumeTimer();
            isTimerPaused = false;
            System.out.println("resume timer service 호출됨!");
            System.out.println("isServicerunning : "+ isServiceRunning);
            System.out.println("isServiceBound method : "+ isServiceBound);
            System.out.println("istimerpaused :" + isTimerPaused);
            System.out.println("isChronometerRunning :" + isChronometerRunning);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startTimerService() {
        Intent intent = new Intent(getContext(), TimerService.class);
        ContextCompat.startForegroundService(getContext(), intent);
    }

    private void stopTimerService() {
        System.out.println("stoptimerservice1 - 현재 바인딩 되어 있는 service 는" + timerService);
        unbindTimerService();
//        unbindService(serviceConnection);
        System.out.println("stoptimerservice2 - 현재 바인딩 되어 있는 service 는" + timerService);
        Intent intent = new Intent(getContext(), TimerService.class);
        getContext().stopService(intent);
        System.out.println("stoptimerserivce 호출중");
        System.out.println("stoptimerservice3 - 현재 바인딩 되어 있는 service 는" + timerService);
    }

    private void openInputPopupWindow(String exercise_name){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View inputpopupView = inflater.inflate(R.layout.todo_input_progress, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(inputpopupView);
        AlertDialog inputpopupDialog = builder.create();

        EditText in_pg_count = inputpopupView.findViewById(R.id.todo_progress_input);

        Button in_save_btn = inputpopupView.findViewById(R.id.pg_save_button);
        Button in_cancel_btn = inputpopupView.findViewById(R.id.pg_cancel_button);

        in_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBmanage dbHelper = new DBmanage(getContext());
                Todo_db=dbHelper.getWritableDatabase();

                String ex_met_query = "select MET from exercise_info where exercise_name = '"+exercise_name+"'";
                Cursor ex_met_cursor = Todo_db.rawQuery(ex_met_query, null);

                if(ex_met_cursor.moveToFirst()) {
                    do {
                        MET = ex_met_cursor.getDouble(0);
                    }while (ex_met_cursor.moveToNext());
                }
                ex_met_cursor.close();
                Todo_db.close();

                String ex_count = in_pg_count.getText().toString();

                int ex_time = Integer.parseInt(ex_count) * 3;


                double kcal_burn = (MET * user_w * ex_time) / 3600;

                System.out.println(kcal_burn);
                System.out.println(MET);
                System.out.println(user_w);
                System.out.println(ex_time);

                Todo_db=dbHelper.getWritableDatabase();
                Todo_db.execSQL("UPDATE exercise_record SET recd_ex_progress = '" + ex_count + "', recd_burn_kcal = " + kcal_burn + " WHERE recd_ex_name = '" + exercise_name + "'");
                Todo_db.close();

                System.out.println("update complete");

                inputpopupDialog.dismiss();
                showTodoScreenFragment();
            }
        });

        in_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputpopupDialog.dismiss();
            }
        });

        inputpopupDialog.show();
    }
    private void openPopupWindow(String timer) {
        // 팝업 창 레이아웃 파일을 인플레이트하여 뷰 객체로 변환합니다.
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View popupView = inflater.inflate(R.layout.todo_time_record, null);

        // 팝업 창 다이얼로그 객체를 생성합니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(popupView);
        AlertDialog popupDialog = builder.create();

        // 스피너에 어댑터를 설정합니다.
        Spinner spinner = popupView.findViewById(R.id.popup_spinner);
        spinner.setAdapter(spinnerAdapter);

        Button popup_confirm_btn = popupView.findViewById(R.id.popup_ck_button);
        Button popup_cancel_btn = popupView.findViewById(R.id.popup_cancel_button);

        popup_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBmanage dbHelper = new DBmanage(getContext());
                Todo_db=dbHelper.getWritableDatabase();

                selected_spinner = spinner.getSelectedItem().toString();

                String ex_met_query = "select MET from exercise_info where exercise_name = '"+selected_spinner+"'";
                Cursor ex_met_cursor = Todo_db.rawQuery(ex_met_query, null);

                if(ex_met_cursor.moveToFirst()) {
                    do {
                        MET = ex_met_cursor.getDouble(0);
                    }while (ex_met_cursor.moveToNext());
                }
                ex_met_cursor.close();
                Todo_db.close();



                String[] timeParts = timer.split(":");
                if(timeParts.length == 2){
                    int minutes = Integer.parseInt(timeParts[0]);
                    int seconds = Integer.parseInt(timeParts[1]);
                    elapsedMinutes = (minutes * 60) + seconds;
                }else if(timeParts.length == 3){
                    int hour = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);
                    elapsedMinutes = (hour * 3600) + (minutes * 60) + seconds;
                }


                double kcal_burn = (MET * user_w * elapsedMinutes) / 3600;

                System.out.println(kcal_burn);
                System.out.println(MET);
                System.out.println(user_w);
                System.out.println(elapsedMinutes);
                System.out.println(selected_spinner);

                Todo_db=dbHelper.getWritableDatabase();
                Todo_db.execSQL("UPDATE exercise_record SET recd_ex_progress = '" + timer + "', recd_burn_kcal = " + kcal_burn + " WHERE recd_ex_name = '" + selected_spinner + "'");
                Todo_db.close();

                System.out.println("update complete");

                unbindTimerService();
                System.out.println("stoptimerserivce 호출전");
                stopTimerService();
                System.out.println("stoptimerserivce 호출후");
                System.out.println("stoptimerservice4 - 현재 바인딩 되어 있는 service 는" + timerService);
                isTimerPaused = false;
                isServiceBound = false;
                timerService= null;
                stoppedTime = 0;

                System.out.println("---------------------------------------");
                System.out.println("save after isServicerunning : "+ isServiceRunning);
                System.out.println("save after isServiceBound : "+ isServiceBound);
                System.out.println("save after istimerpaused :" + isTimerPaused);
                System.out.println("save after isChronometerRunning :" + isChronometerRunning);

                popupDialog.dismiss();
                showTodoScreenFragment();

            }
        });

        popup_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                popupDialog.dismiss();
            }
        });

        // 팝업 창을 화면에 표시합니다.
        popupDialog.show();
    }

    public void showTodoScreenFragment() {
        TodoScreen todo_fragment = new TodoScreen();

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, todo_fragment);
        fragmentTransaction.commit();
    }

    public void onPause() {
        super.onPause();
        unbindTimerService();
    }

}
