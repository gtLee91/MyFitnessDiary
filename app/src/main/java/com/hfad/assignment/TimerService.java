package com.hfad.assignment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TimerService extends Service {

    private Handler handler;
    private Runnable runnable;
    private long startTime;
    private long elapsedTime;
    private long pausedTime = 0; // 일시 정지된 시간을 저장할 변수
    private boolean isPaused = false; // 일시 정지 여부를 나타내는 변수
    private static long currentTime = 0;
    private TimerCallback timerCallback;

    private static final String CHANNEL_ID = "timer_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        startTime = SystemClock.elapsedRealtime();

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification()); // Foreground 서비스로 설정
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runnable = new Runnable() {
            @Override
            public void run() {
                if(!isPaused){
                    elapsedTime = SystemClock.elapsedRealtime() - startTime;
                    currentTime = elapsedTime;
                    updateNotification(elapsedTime);

                    if (timerCallback != null) {
                        timerCallback.onTimeChanged(elapsedTime);
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(runnable, 1000);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // 작업 중지
        stopForeground(true); // Foreground 서비스 중지
        stopSelf(); // 서비스 종료
        isPaused = false;
        elapsedTime = 0;
        currentTime = 0;
        startTime = 0;
        System.out.println("서비스 종료");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TimerServiceBinder();
    }

    public void setTimerCallback(TimerCallback callback) {
        timerCallback = callback;
    }

    public void pauseTimer() {
        if (!isPaused) { // 이미 일시 정지된 상태가 아닌 경우에만 일시 정지 처리
            pausedTime = SystemClock.elapsedRealtime() - startTime - elapsedTime;
            isPaused = true;
        }
    }

    public void resumeTimer() {
        if (isPaused) { // 일시 정지된 상태에서만 다시 시작 처리
            startTime = SystemClock.elapsedRealtime() - elapsedTime - pausedTime;
            pausedTime = 0;
            isPaused = false;
        }
    }

    public static long getElapsedTimeFromService() {
        long elapsedTime = currentTime; // TimerService의 getElapsedTime() 메서드 호출
        return elapsedTime;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.stop)
                .setContentTitle("Timer Service")
                .setContentText("Timer is running...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        return builder.build();
    }

    private void updateNotification(long elapsedTime) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.start)
                .setContentTitle("Timer Service")
//                .setContentText("Elapsed Time: " + (elapsedTime / 1000) + " s")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);
        long hour = (elapsedTime / 1000) / 3600;
        long min = (elapsedTime / 1000) / 60;
        long sec = elapsedTime / 1000;
        if( min >= 1 && hour < 1){
            builder.setContentText("Exercise Time: " + min + " m " +(sec - (min*60))+ " s");
        }else if( hour >= 1 ){
            builder.setContentText("Exercise Time: " + hour + " h " +min+ " m " +(sec - (min*60))+ " s");
        }else{
            builder.setContentText("Exercise Time: " +sec+ " s");
        }


        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        managerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    public class TimerServiceBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }
}


