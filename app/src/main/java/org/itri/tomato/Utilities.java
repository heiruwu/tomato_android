package org.itri.tomato;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by heiruwu on 7/17/15.
 */
public class Utilities {
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String USER_ID = "userId";
    public static final String USER_TOKEN = "userToken";
    public static final String USER_ACCOUNT = "userAccount";
    public static final String USER_PASSWORD = "userPassword";
    public static final String HAS_ACCOUNT = "hasAccount";
    public static final String SDK_VERSION = "sdkVersion";
    public static final int CHECK_BOX = 1;
    public static final int RADIO_BUTTON = 2;
    public static final int SEARCH_DIALOG = 3;
    public static final int CHECK_BOX_EDIT = 4;
    public static final int RADIO_BUTTON_EDIT = 5;
    public static final int MAP = 0;
    public static final int SCHEDULE = 7;

    /**
     * For Server API
     */
    public static final String ACTION = "&action=";
    public static final String PARAMS = "&params=";
    public static final String API_URL = "http://113.196.140.131/TomatoX2/index.php/API_Operations/tomatox_api";
    public static final String SENDER_ID = "948528150442";
    public static final String TYPE = "android";

    private static String responseCode;

    public static JSONObject API_CONNECT(String Action, String Params,final Context context, boolean hasInput) {
        if (!isOnline(context)){
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT).show();
                }
            });
            ((Activity)context).finish();
            responseCode = "noResponse";
            return null;
        }
        try {
            URL url = new URL(Utilities.API_URL);
            String out = Action + Params;
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(out.getBytes());
            outputStream.flush();
            outputStream.close();
            if (hasInput) {
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String temp;
                while ((temp = bReader.readLine()) != null) {
                    stringBuilder.append(temp + "\n");
                }
                inputStream.close();
                Log.w("Response", stringBuilder.toString());
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                JSONObject jsonResponse = new JSONObject(jsonObject.getJSONObject("response").toString());
                responseCode = jsonResponse.getString("status");
                if (responseCode.equals("true")) {
                    Log.i(Action, responseCode);
                    return jsonResponse;
                } else {
                    Log.w(Action, "failed, errorMsg: " + jsonResponse.getString("errormsg"));
                    return null;
                }
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getResponseCode() {
        return responseCode;
    }

    public static void getHashKey(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo("org.itri.tomato", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String KeyResult = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", KeyResult);
                Toast.makeText(context, "My FB Key is \n" + KeyResult, Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
