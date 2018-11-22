package com.cheah.david.cloudfunctionlogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public abstract class BaseAction extends AppCompatActivity {
    private static final String TAG = "BaseAction";

    //TODO : Change URL
    public String baseUrl = "https://us-central1-testfire-6b175.cloudfunctions.net";


    // Go to AccountInfoActivity
    void getAccountInfo() {
        Intent intent = new Intent();
        intent.setClass(this, AccountInfoActivity.class);
        startActivity(intent);
    }

    public void loginRegister() {
        Intent i = new Intent();
        i.setClass(this, RegistrationLoginActivity.class);
        startActivity(i);
    }

    // Check if valid login token still exist
    boolean isLogin() {
        String token = getLoginToken();
        if (token == null || token.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    // Return login token from shared preference
    String getLoginToken() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        String token = sharedPref.getString(getString(R.string.auth_token), null);
        return token;
    }

    void clearSharedPreference(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        sharedPref.edit().clear().commit();
    }

    void sendMessageToCloudFunction(HttpUrl.Builder httpBuilder) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(httpBuilder.build()).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "error response firebase cloud functions");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BaseAction.this, "Action failed, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                String resp = "";
                if (!response.isSuccessful()){
                    Log.e(TAG, "action failed");
                    resp = "Failed perform the action, please try again";
                } else {
                    try {
                        resp = responseBody.string();
                        Log.e(TAG, "Response " + resp);
                    } catch (IOException e){
                        resp = "Problem in reading resposne";
                        Log.e(TAG, "Problem in reading response " + e);
                    }
                }

                runOnUiThread(responseRunnable(resp));
            }
        });
    }

    public abstract Runnable responseRunnable(final String responseStr);

}
