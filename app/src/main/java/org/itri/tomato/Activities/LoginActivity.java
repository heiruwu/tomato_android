package org.itri.tomato.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.itri.tomato.R;
import org.itri.tomato.Utilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private static String UserAccount;
    private static String UserPassword;
    JSONObject jsonObject;
    EditText editAccount;
    EditText editPass;
    SharedPreferences sharedPreferences;
    Thread loginThread;
    ProgressDialog progressDialog;
    boolean isCancel = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editAccount = (EditText) findViewById(R.id.editAccount);
        editPass = (EditText) findViewById(R.id.editPass);
        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserAccount = editAccount.getText().toString();
                UserPassword = editPass.getText().toString();
                progressDialog = ProgressDialog.show(LoginActivity.this, "Logging in", "please wait......", true);
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if(isCancel) {
                            Toast.makeText(LoginActivity.this, "Account invalid", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                loginThread = new Thread(loginRunnable);
                loginThread.start();
            }
        });
        if(sharedPreferences.getBoolean(Utilities.HAS_ACCOUNT, false)) {
            UserAccount = sharedPreferences.getString(Utilities.USER_ACCOUNT,null);
            UserPassword = sharedPreferences.getString(Utilities.USER_PASSWORD, null);
            progressDialog = ProgressDialog.show(LoginActivity.this, "Logging in", "please wait......", true);
            loginThread = new Thread(loginRunnable);
            loginThread.start();
        }
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    private static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    Runnable loginRunnable = new Runnable() {
        @Override
        public void run() {
            if(isEmailValid(UserAccount)) {
                String Action = Utilities.ACTION + "Login";
                String Params = Utilities.PARAMS + "{\"email\":\"" + UserAccount + "\",\"pass\":\"" + UserPassword + "\"}";
                jsonObject = Utilities.API_CONNECT(Action, Params, true);
                if (Utilities.getResponseCode() == 200) {
                    if (jsonObject != null) {
                        try {
                            if (jsonObject.getString("success").equals("true")) {
                                if (!sharedPreferences.getBoolean(Utilities.HAS_ACCOUNT, false)) {
                                    sharedPreferences.edit().putString(Utilities.USER_ACCOUNT, UserAccount).apply();
                                    sharedPreferences.edit().putString(Utilities.USER_PASSWORD,UserPassword).apply();
                                    sharedPreferences.edit().putBoolean(Utilities.HAS_ACCOUNT, true).apply();
                                }
                                JSONObject jsonObjectTmp = new JSONObject(jsonObject.getString("response"));
                                Log.i("uid", jsonObjectTmp.get("uid").toString());
                                Log.i("token", jsonObjectTmp.get("token").toString());
                                sharedPreferences.edit().putString(Utilities.USER_ID, jsonObjectTmp.get("uid").toString()).apply();
                                sharedPreferences.edit().putString(Utilities.USER_TOKEN, jsonObjectTmp.get("token").toString()).apply();
                                Intent intent = new Intent();
                                intent.setClass(LoginActivity.this, MarketActivity.class);
                                startActivity(intent);
                                isCancel = false;
                                progressDialog.dismiss();
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        isCancel = true;
                        progressDialog.cancel();
                    }
                }
            } else {
                isCancel = true;
                progressDialog.cancel();
            }
        }
    };
}