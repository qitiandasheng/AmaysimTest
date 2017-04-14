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

    private String responseData;

    private EditText nameEdit;
    private EditText pinEdit;
    private Button loginButton;
    private TextView forgetTxt;

    private String TAG="LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

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

    private void authenticate(String jsonData){
       // MainActivity.authenticated=true;
        try{
            JSONObject jsonObj = new JSONObject(jsonData);
                String msn = jsonObj.getJSONObject("attributes").getString("msn");
                if(msn.equals(nameEdit.getText().toString().trim()) && msn.equals(pinEdit.getText().toString().trim())){
                    MainActivity.authenticated=true;
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.loginButton) {
            authenticate(responseData);
            if (MainActivity.authenticated) {
//                Intent[] intent = {new Intent(LoginActivity.this, MainActivity.class)};
//                startActivities(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Entered incorrect username or password", Toast.LENGTH_SHORT).show();
            }
        }
        if(v.getId()==R.id.forgetText){
            Toast.makeText(LoginActivity.this, "MSN (0468874507) for both username and password", Toast.LENGTH_SHORT).show();
        }
    }
}
