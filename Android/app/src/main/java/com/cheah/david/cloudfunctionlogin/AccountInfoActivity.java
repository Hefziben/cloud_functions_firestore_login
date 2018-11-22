package com.cheah.david.cloudfunctionlogin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import okhttp3.HttpUrl;

public class AccountInfoActivity extends BaseAction {

    private static final String TAG = "AccountBalance";

    private String FIREBASE_CLOUD_FUNCTION_ACCOUNT_BAL_URL = baseUrl + "/getAccountInfo";

    private String FIREBASE_CLOUD_FUNCTION_DESTROY_SESSION_URL = baseUrl + "/destroySession";
    TextView accountBalanceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.account_info_layout);

        setListeners();

        accountBalanceText = findViewById(R.id.acc_bal_tv);
        getAccountBalance();
    }

    private void setListeners(){
        //registration
        Button logoutButton = findViewById(R.id.logout_btn);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    private void logout(){
        destroySession();
        clearSharedPreference();
        loginRegister();
    }

    private void destroySession() {
        String token = getLoginToken();
        if (token == null || token.isEmpty()) {
            loginRegister();
        }
        Log.d(TAG, "token " + token);
        HttpUrl.Builder httpBuider =
                HttpUrl.parse(FIREBASE_CLOUD_FUNCTION_DESTROY_SESSION_URL).newBuilder();
        httpBuider.addQueryParameter("token", token);
        sendMessageToCloudFunction(httpBuider);
    }


    private void getAccountBalance() {
        String token = getLoginToken();
        //send user to login screen if no token
        if (token == null || token.isEmpty()) {
            loginRegister();
        }
        Log.d(TAG, "token " + token);
        HttpUrl.Builder httpBuider =
                HttpUrl.parse(FIREBASE_CLOUD_FUNCTION_ACCOUNT_BAL_URL).newBuilder();
        httpBuider.addQueryParameter("token", token);
        sendMessageToCloudFunction(httpBuider);
    }

    @Override
    public Runnable responseRunnable(final String responseStr) {
        Runnable resRunnable = new Runnable() {
            @Override
            public void run() {
                if (responseStr.contentEquals("Invalid token")){
                    logout();
                }
                accountBalanceText.setText("Account Balance: "+ responseStr);
            }
        };
        return resRunnable;
    }
}