package com.cheah.david.cloudfunctionlogin;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.HttpUrl;

public class RegistrationLoginActivity extends BaseAction {
    private static final String TAG = "RegLoginActivity";

    private String FIREBASE_CLOUD_FUNCTION_REG_URL = baseUrl + "/register";
    private String FIREBASE_CLOUD_FUNCTION_LOGIN_URL = baseUrl + "/login";

    private String loginEmail;
    private ViewGroup regLayout;
    private ViewGroup loginLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //clearSharedPreference();

        setContentView(R.layout.registration_login_layout);

        regLayout = findViewById(R.id.registration_layout);
        loginLayout = findViewById(R.id.login_layout);

        // Set initial layout to login
        regLayout.setVisibility(View.GONE);

        setListeners();

    }

    private void setListeners() {
        //registration
        Button regButton = findViewById(R.id.register_btn);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration();
            }
        });

        //Go to login screen from reg screen
        Button regToLogin = findViewById(R.id.reg_to_login_btn);
        regToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
            }
        });

        //login
        Button loginButton = findViewById(R.id.login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        //Go to reg screen from login screen
        Button loginToReg = findViewById(R.id.login_to_reg_btn);
        loginToReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regLayout.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.GONE);
            }
        });

    }

    private void registration() {
        EditText nameEt = findViewById(R.id.reg_name_et);
        EditText emailEt = findViewById(R.id.reg_email_et);
        EditText passwordEt = findViewById(R.id.reg_password_et);

        String name = nameEt.getText().toString();
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();

        HttpUrl.Builder httpBuilder = prepareRegRequestBuilder(name, email, password);
        sendMessageToCloudFunction(httpBuilder);
    }

    private void login() {
        EditText emailEt = findViewById(R.id.login_email_et);
        EditText passwordEt = findViewById(R.id.login_password_et);

        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();

        HttpUrl.Builder httpBuilder = prepareLoginRequestBuilder(email, password);
        sendMessageToCloudFunction(httpBuilder);
    }

    private HttpUrl.Builder prepareRegRequestBuilder(String name, String email, String password){
        HttpUrl.Builder httpBuider =
                HttpUrl.parse(FIREBASE_CLOUD_FUNCTION_REG_URL).newBuilder();
        httpBuider.addQueryParameter("name", name);
        httpBuider.addQueryParameter("email", email);
        httpBuider.addQueryParameter("password", password);
        return httpBuider;
    }

    private HttpUrl.Builder prepareLoginRequestBuilder(String email, String password) {
        HttpUrl.Builder httpBuider =
                HttpUrl.parse(FIREBASE_CLOUD_FUNCTION_LOGIN_URL).newBuilder();
        httpBuider.addQueryParameter("email", email);
        httpBuider.addQueryParameter("password", password);
        return httpBuider;
    }

    public Runnable responseRunnable(final String responseStr) {
        Runnable resRunnable = new Runnable() {
            public void run() {
                Log.d(TAG, responseStr);
                //login success
                if(responseStr.contains("token")){
                    //retrieve token from response and save it in shared preference
                    //so that token can be sent in the request to services

                    String tokenStr[] = responseStr.split(":");
                    Log.d(TAG, tokenStr[1]);
                    SharedPreferences sharedPref = PreferenceManager.
                            getDefaultSharedPreferences(
                                    RegistrationLoginActivity.this.getApplication());

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.auth_email), loginEmail);
                    editor.putString(getString(R.string.auth_token), tokenStr[1]);
                    editor.commit();

                    Toast.makeText(RegistrationLoginActivity.this,
                            "Login Successful.",
                            Toast.LENGTH_SHORT).show();

                    clearUI();
                    getAccountInfo();
                }else if(responseStr.contains("account created")){
                    Toast.makeText(RegistrationLoginActivity.this,
                            "Account created, login now.",
                            Toast.LENGTH_SHORT).show();

                    clearUI();
                    showLogin();
                }else {
                    Toast.makeText(RegistrationLoginActivity.this,
                            responseStr,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        return resRunnable;
    }

    private void clearUI(){
        ((EditText)findViewById(R.id.reg_name_et)).setText("");
        ((EditText)findViewById(R.id.reg_email_et)).setText("");
        ((EditText)findViewById(R.id.reg_password_et)).setText("");

        ((EditText)findViewById(R.id.login_email_et)).setText("");
        ((EditText)findViewById(R.id.login_password_et)).setText("");
    }
    private void showLogin(){
        regLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
    }
}
