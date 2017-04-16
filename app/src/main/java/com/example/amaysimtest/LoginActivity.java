package com.example.amaysimtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    
    // response data for user login
    private String responseData;
    
    // the UI components
    private EditText nameEdit;
    private EditText pinEdit;
    private Button loginButton;
    private TextView forgetTxt;
    
    // for the log 
    private String TAG="LoginActivity";

    /**
     * display UI
     * 
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // init variables
        init();
    }

    /**
     * init variables
     */
    private void init(){
        responseData=null;

        nameEdit = (EditText)findViewById(R.id.nameEdit);
        forgetTxt = (TextView)findViewById(R.id.forgetText);
        pinEdit = (EditText)findViewById(R.id.pinEdit);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        forgetTxt.setOnClickListener(this);

        setupConnection();
    }

    /**
     * send request to get all services data which contains user's MSN, it used for login
     */
    private void setupConnection(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{

                    HttpUtil.sendOkHttpRequest(HttpUtil.SERVICES_URL , new Callback(){
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            responseData = response.body().string();
                            Log.d(TAG, "responseData from Luna server: "+responseData);
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {

                        }


                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * start to process authentication by MSN number,
     * 
     * @param jsonData
     */
    private void authenticate(String jsonData){
       // MainActivity.authenticated=true;
        try{
            Log.d(TAG, "authenticate: extracting msn data from response json data");
            //extracting msn data from response json data
            JSONObject jsonObj = new JSONObject(jsonData);
            String msn = jsonObj.getJSONObject("attributes").getString("msn");

            Log.d(TAG, "authenticate: if the user input are all the same as MSN number, then the status should be changed to logged in");
            // if the user input are all the same as MSN number, then the status should be changed to logged in
            if(msn.equals(nameEdit.getText().toString().trim()) && msn.equals(pinEdit.getText().toString().trim())){
                MainActivity.authenticated=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * process login button click
     * 
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.loginButton) {
            Log.d(TAG, "onClick: process authentication by the response data");
            // process authentication by the response data
            authenticate(responseData);
            if (MainActivity.authenticated) {
                Log.d(TAG, "onClick: if the user provided correct name and password, login UI should be terminated");
                // if the user provided correct name and password, login UI should be terminated
                finish();
            } else {
                Log.d(TAG, "onClick: alert to remind wrongly input");
                // alert to remind wrongly input
                Toast.makeText(LoginActivity.this, "Entered incorrect username or password", Toast.LENGTH_SHORT).show();
            }
        }
        if(v.getId()==R.id.forgetText){
            Log.d(TAG, "onClick: tell the user how to login");
            // tell the user how to login
            Toast.makeText(LoginActivity.this, "MSN (0468874507) for both username and password", Toast.LENGTH_SHORT).show();
        }
    }
}
