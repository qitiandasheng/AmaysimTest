package com.example.amaysimtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    public static boolean authenticated = false;
    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;
    private ImageView image;
    private Button balanceBtn;
    private Button priceBtn;
    private Button creditBtn;
    private Button infoBtn;
    private Button productBtn;
    private String balance;
    private String price;
    private String credit;
    private String info;
    private String product;
    private String responseData;
    private JSONObject jsonObj;
    private DecimalFormat df;
    private String TAG="MainActivity";
    private QueueBuffer queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(authenticated == false) {
            loginProcess();
        }
        setContentView(R.layout.activity_main);
        image = (ImageView)findViewById(R.id.avatar_image);
        balanceBtn=(Button)findViewById(R.id.balanceBtn);
        priceBtn=(Button)findViewById(R.id.priceBtn);
        creditBtn=(Button)findViewById(R.id.creditBtn);
        infoBtn=(Button)findViewById(R.id.infoBtn);
        productBtn=(Button)findViewById(R.id.productBtn);
        infoBtn.setText(DateFormat.getDateInstance().format(new Date()));
        image.setOnClickListener(this);
        balanceBtn.setOnClickListener(this);
        priceBtn.setOnClickListener(this);
        creditBtn.setOnClickListener(this);
        infoBtn.setOnClickListener(this);
        productBtn.setOnClickListener(this);

        df = new DecimalFormat("#.00");

        queue = new QueueBuffer();
        setUsernameTitle();
    }

    private void setUsernameTitle(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{

                    HttpUtil.sendOkHttpRequest(HttpUtil.ACCOUNTS_URL , new Callback(){
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            responseData = response.body().string();

                            try {
                                jsonObj = new JSONObject(responseData);
                                String title = jsonObj.getJSONObject("attributes").getString("title");
                                String firstName = jsonObj.getJSONObject("attributes").getString("first-name");
                                String lastName = jsonObj.getJSONObject("attributes").getString("last-name");
                                queue.put("Welcome Back! "+title+" "+firstName+" "+lastName);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

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
        setTitle(queue.get());
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.avatar_image) {
            File outputImage = new File(getExternalCacheDir(), "output_image.jpg");

            try{
                if(outputImage.exists()){
                    outputImage.delete();
                }
                outputImage.createNewFile();
            }catch(IOException e){
                e.printStackTrace();
            }
            if(Build.VERSION.SDK_INT >=24){
                imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.amaysimtest.fileprovider", outputImage);
            }else {
                imageUri = Uri.fromFile(outputImage);
            }

            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent,TAKE_PHOTO);

        }
        if(v.getId()==R.id.balanceBtn){
            if(balance==null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            HttpUtil.sendOkHttpRequest(HttpUtil.SUBSCRIPTIONS_URL, new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    responseData = response.body().string();

                                    try {
                                        jsonObj = new JSONObject(responseData);

                                        balance = jsonObj.getJSONObject("attributes").getString("included-data-balance");
                                        if (balance != null) {
                                            balance = df.format(Float.parseFloat(balance) / 1024) + " GB";
                                        } else {
                                            balance = "0 GB";
                                        }
                                        queue.put(balance);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call call, IOException e) {

                                }


                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                infoBtn.setText(queue.get());

            }else{
                infoBtn.setText(balance);
            }

        }
        if(v.getId()==R.id.creditBtn){
            if(credit==null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            HttpUtil.sendOkHttpRequest(HttpUtil.SERVICES_URL, new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    responseData = response.body().string();
                                    Log.d(TAG, "responseData from Luna server: " + responseData);
                                    try {
                                        JSONObject jsonObj = new JSONObject(responseData);
                                        credit = jsonObj.getJSONObject("attributes").getString("credit")+" Credit(s)";
                                        queue.put(credit);
                                    } catch (Exception e) {
                                        e.printStackTrace();

                                    }
                                }

                                @Override
                                public void onFailure(Call call, IOException e) {

                                }


                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                infoBtn.setText(queue.get());

            }else{
                infoBtn.setText(credit);
            }

        }
        if(v.getId()==R.id.priceBtn){
            if(price==null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            HttpUtil.sendOkHttpRequest(HttpUtil.PRODUCTS_URL, new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    responseData = response.body().string();
                                    Log.d(TAG, "responseData from Luna server: " + responseData);
                                    try {
                                        jsonObj = new JSONObject(responseData);


                                        price = jsonObj.getJSONObject("attributes").getString("price");

                                        if (price != null) {
                                            price = "$" + Float.parseFloat(price) / 100;
                                        } else {
                                            price = "$0";
                                        }
                                        queue.put(price);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                infoBtn.setText(queue.get());

            }else{
                infoBtn.setText(price);
            }

        }

        if(v.getId()==R.id.productBtn){
            if(product==null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpUtil.sendOkHttpRequest(HttpUtil.PRODUCTS_URL, new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    responseData = response.body().string();
                                    Log.d(TAG, "responseData from Luna server: " + responseData);
                                    try {
                                        jsonObj = new JSONObject(responseData);
                                        product = jsonObj.getJSONObject("attributes").getString("name");
                                        queue.put(product);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                infoBtn.setText(queue.get());
                Log.d(TAG, "credit values: " + credit + " price: " + price + " product name: " + product + " balance: " + balance);
            }else{
                infoBtn.setText(product);
            }
        }

        if(v.getId()==R.id.infoBtn){
            if(infoBtn.getText().equals(DateFormat.getDateInstance().format(new Date()))){
                Toast.makeText(MainActivity.this, "This is today's date", Toast.LENGTH_SHORT).show();
            }
            if(infoBtn.getText().equals(product)){
                Toast.makeText(MainActivity.this, "Product Name", Toast.LENGTH_SHORT).show();
            }
            if(infoBtn.getText().equals(credit)){
                Toast.makeText(MainActivity.this, "User Credit", Toast.LENGTH_SHORT).show();
            }
            if(infoBtn.getText().equals(price)){
                Toast.makeText(MainActivity.this, "Product Price", Toast.LENGTH_SHORT).show();
            }
            if(infoBtn.getText().equals(balance)){
                Toast.makeText(MainActivity.this, "Included Data Balance", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));

                        image.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void loginProcess(){
        Intent[] intent = {new Intent(MainActivity.this, LoginActivity.class)};
        startActivities(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout:
                logoutProcess();
                break;
        }
        return true;
    }

    private void logoutProcess(){
        authenticated = false;
        loginProcess();
        finish();
    }

    class QueueBuffer {
        String value;
        boolean valueSet = false;

        synchronized String get() {
            if (!valueSet)
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException caught");
                }

            Log.d(TAG, "get: "+value);
            valueSet = false;
            notify();
            return value;
        }

        synchronized void put(String value) {
            if (valueSet)
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException caught");
                }
            this.value = value;
            valueSet = true;
            Log.d(TAG, "put: "+value);
            notify();
        }
    }
}
