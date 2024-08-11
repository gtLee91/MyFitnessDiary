package com.hfad.assignment;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EdamamApiTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "EdamamApiTask";
    private static final String APP_ID = "45670595";
    private static final String APP_KEY = "0943e0d5113f68f8ace676c629b90e0a";

    public ArrayList<String> fd_Name = new ArrayList<>();
    public ArrayList<Double> fd_Kcal = new ArrayList<>();

    private OnApiResultListener onApiResultListener;

    @Override
    protected String doInBackground(String... params) {
        String foodName = params[0];

        OkHttpClient client = new OkHttpClient();

        String url = "https://api.edamam.com/api/food-database/v2/parser";
        String requestUrl = url + "?ingr=" + foodName + "&app_id=" + APP_ID + "&app_key=" + APP_KEY;

        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, "Error executing HTTP request: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String jsonData) {
        if (jsonData != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONArray hintsArray = jsonObject.getJSONArray("hints");

                if (hintsArray.length() > 0) {

                    for(int i = 0; i < hintsArray.length(); i++) {
                        JSONObject foodObject = hintsArray.getJSONObject(i).getJSONObject("food");

                        if(foodObject.has("brand")){
                            String brand_name = foodObject.getString("brand");
                            String fdname = foodObject.getString("label");
                            if(fdname.contains(",")){
                                String[] parts = fdname.split(",");
                                fdname = parts[0].trim();
                            }
                            fd_Name.add(fdname + ", " + brand_name);
                        }else{
                            String fdname = foodObject.getString("label");
                            if(fdname.contains(",")){
                                String[] parts = fdname.split(",");
                                fdname = parts[0].trim();
                            }
                            fd_Name.add(fdname);
                        }

                        double calories = foodObject.getJSONObject("nutrients").getDouble("ENERC_KCAL");


                        fd_Kcal.add(calories);

//                        Log.d(TAG, "food name: " + fd_Name.get(i));
//                        Log.d(TAG, "Calories: " + fd_Kcal.get(i));
                    }

                    // 추출한 칼로리 정보 사용
//                    Log.d(TAG, "Calories: " + calories);
                } else {
                    Log.d(TAG, "No food data found.");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON data: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Empty response from API.");
        }

        if (onApiResultListener != null) {
            onApiResultListener.onApiResult(fd_Name, fd_Kcal);
        }
    }

    public interface OnApiResultListener {
        void onApiResult(ArrayList<String> fd_Name, ArrayList<Double> fd_Kcal);
    }

    public void setOnApiResultListener(OnApiResultListener listener) {
        this.onApiResultListener = listener;
    }
}

