package com.hfad.assignment;

import android.app.Application;
import android.content.ServiceConnection;

public class MyApp extends Application {
    private ServiceConnection savedServiceConnection;

    public ServiceConnection getSavedServiceConnection() {
        return savedServiceConnection;
    }

    public void setSavedServiceConnection(ServiceConnection connection) {
        savedServiceConnection = connection;
    }
}
