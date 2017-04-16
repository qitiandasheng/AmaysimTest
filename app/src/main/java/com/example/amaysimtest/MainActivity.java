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

    // identify login status
    public static boolean authenticated = false;

    // used for an additional function which can make photo
    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;


    // the UI components
    private ImageView image;
    private Button balanceBtn;
    private Button priceBtn;
    private Button creditBtn;
    private Button infoBtn;
    private Button productBtn;

    // used for store query data
    private String balance;
    private String price;
    private String credit;
    private String info;
    private String product;
    private String responseData;

    // to extract json data
    private JSONObject jsonObj;

    // for displaying decimal number format
    private DecimalFormat df;

    // an inner class for thread's lock control
    private QueueBuffer queue;

    // for log
    private String TAG="MainActivity";

    /**
     * initialize user interface and varivables, It has login process
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: start to process login");
        // process login
        if(authenticated == false) {
            Log.d(TAG, "onCreate: the user hasn't been authenticated and start to process login");
            loginProcess();
        }
        Log.d(TAG, "onCreate: the user is logged");
        // init UI
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: start to initialize the variables");
        // init varibles and UI componnets
        image = (ImageView)findViewById(R.id.avatar_image);
        balanceBtn=(Button)findViewById(R.id.balanceBtn);
        priceBtn=(Button)findViewById(R.id.priceBtn);
        creditBtn=(Button)findViewById(R.id.creditBtn);
        infoBtn=(Button)findViewById(R.id.infoBtn);
        productBtn=(Button)findViewById(R.id.productBtn);
        queue = new QueueBuffer();

        Log.d(TAG, "onCreate: set up date info");
        // set a date information in the information displaying area initially
        infoBtn.setText(DateFormat.getDateInstance().format(new Date()));

        Log.d(TAG, "onCreate: set listenners");
        // set listenners
        image.setOnClickListener(this);
        balanceBtn.setOnClickListener(this);
        priceBtn.setOnClickListener(this);
        creditBtn.setOnClickListener(this);
        infoBtn.setOnClickListener(this);
        productBtn.setOnClickListener(this);

        Log.d(TAG, "onCreate: setup decimal format");
        // keep 2 decimal format
        df = new DecimalFormat("#.00");

        Log.d(TAG, "onCreate: set up user name on the title bar");
        // if the user logged in, show the user name on top
        setUsernameTitle();
    }


    /**
     * once the user logged, this method can query user's title, first and last names and display them on the title bar.
     * it uses a thread the manage query delay issues, so the the program won't put any value into the field until the
     * data is queried from the server.
     */
    private void setUsernameTitle(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    Log.d(TAG, "run: send ACCOUNT_RUL to the server: "+HttpUtil.ACCOUNTS_URL);
                    //send request to get the user's information
                    HttpUtil.sendOkHttpRequest(HttpUtil.ACCOUNTS_URL , new Callback(){
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d(TAG, "onResponse: got Response data"+responseData);
                            // got the response data
                            responseData = response.body().string();


                            try {
                                Log.d(TAG, "onResponse: extract response data: "+responseData);
                                // start to extracting response data as it's in JSON format
                                jsonObj = new JSONObject(responseData);

                                Log.d(TAG, "onResponse: get information of title, first and last name");
                                // get the information and put them into veriables
                                String title = jsonObj.getJSONObject("attributes").getString("title");
                                String firstName = jsonObj.getJSONObject("attributes").getString("first-name");
                                String lastName = jsonObj.getJSONObject("attributes").getString("last-name");

                                Log.d(TAG, "onResponse: start to wait until all data are ready");
                                // start to wait until all data are ready
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
        Log.d(TAG, "setUsernameTitle: put data into the title bar as it has been processed already");
        // put data into the title bar as it has been processed already
        setTitle(queue.get());
    }

    /**
     * process all click actions, it has 6 actions totally, click image, balance botton, credit botton,
     * price button, product name button and infomation button, and the buttons of balance, credit, price
     * and product name are all used thread to manage query delay issue, so that the program can keep
     * waiting until the data responsed from the server side.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: process the image clicking, this is an additional function which is not required");
        // process the image clicking, this is an additional function which is not required
        if(v.getId()==R.id.avatar_image) {

            Log.d(TAG, "onClick: set a temp file to create a directory for storing image file.");
            // set a temp file to create a directory for storing image file.
            File outputImage = new File(getExternalCacheDir(), "output_image.jpg");

            Log.d(TAG, "onClick: delete the temp file, as the directory has been created");
            // delete the temp file, as the directory has been created
            try{
                if(outputImage.exists()){
                    outputImage.delete();
                }
                outputImage.createNewFile();
            }catch(IOException e){
                e.printStackTrace();
            }

            Log.d(TAG, "onClick: some old version SDK has different access compare with the new SDK, we set 2 user cases");
            // some old version SDK has different access compare with the new SDK, we set 2 user cases
            if(Build.VERSION.SDK_INT >=24){
                imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.amaysimtest.fileprovider", outputImage);
            }else {
                imageUri = Uri.fromFile(outputImage);
            }

            Log.d(TAG, "onClick: process picture making");
            // process picture making
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent,TAKE_PHOTO);

        }

        Log.d(TAG, "onClick: process balance button clicking with a thread");
        // process balance button clicking with a thread
        if(v.getId()==R.id.balanceBtn){
            if(balance==null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(TAG, "run: send request to get the data balance information");
                            //send request to get the data balance information
                            HttpUtil.sendOkHttpRequest(HttpUtil.SUBSCRIPTIONS_URL, new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    Log.d(TAG, "onResponse: got the response data");
                                    // got the response data
                                    responseData = response.body().string();

                                    try {
                                        Log.d(TAG, "onResponse: start to extracting response data as it's in JSON format");
                                        // start to extracting response data as it's in JSON format
                                        jsonObj = new JSONObject(responseData);

                                        Log.d(TAG, "onResponse: get the balance data from json object");
                                        // get the balance data from json object
                                        balance = jsonObj.getJSONObject("attributes").getString("included-data-balance");

                                        Log.d(TAG, "onResponse: start to process format");
                                        // start to process format
                                        if (balance != null) {
                                            balance = df.format(Float.parseFloat(balance) / 1024) + " GB";
                                        } else {
                                            balance = "0 GB";
                                        }

                                        Log.d(TAG, "onResponse: start to wait until all data are ready");
                                        // start to wait until all data are ready
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

                Log.d(TAG, "onClick: put data into the title bar as it has been processed already");
                // put data into the title bar as it has been processed already
                infoBtn.setText(queue.get());

            }else{
                Log.d(TAG, "onClick: if the balance already has a value, directly put into the information field.");
                // if the balance already has a value, directly put into the information field.
                infoBtn.setText(balance);
            }

        }

        Log.d(TAG, "onClick: process credit button clicking with a thread, it has the same process as balance button,");
        // process credit button clicking with a thread, it has the same process as balance button,
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

        Log.d(TAG, "onClick: process price button clicking with a thread, it has the same process as balance button,");
        // process price button clicking with a thread, it has the same process as balance button,
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

        Log.d(TAG, "onClick: process product button clicking with a thread, it has the same process as balance button,");
        // process product button clicking with a thread, it has the same process as balance button,
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

        Log.d(TAG, "onClick: if the user presses information button, it will show an alert to show the data's catagory");
        // if the user presses information button, it will show an alert to show the data's catagory
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

    /**
     * this is used for the additional picture making function, it processes the picture making and
     * image setting.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    /**
     * open login UI to process login
     */
    private void loginProcess(){
        Intent[] intent = {new Intent(MainActivity.this, LoginActivity.class)};
        startActivities(intent);
    }

    /**
     * create an option menu which contains logout function
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    /**
     * process item clicking action which is the logout function in the option menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout:
                logoutProcess();
                break;
        }
        return true;
    }

    /**
     * process the logout function
     */
    private void logoutProcess(){
        authenticated = false;
        loginProcess();
        finish();
    }

    /**
     * this is a class used for thread running, it contains get and set methods which can manage the lock
     */
    class QueueBuffer {
        // the value for processing
        String value;
        // show the status wether the value has been set
        boolean valueSet = false;

        /**
         * get the value
         *
         * @return
         */
        synchronized String get() {
            if (!valueSet)
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.d(TAG, "get: InterruptedException caught");

                }

            Log.d(TAG, "get: "+value);
            valueSet = false;
            notify();
            return value;
        }

        /**
         * set the value by waiting till the value be set.
         * @param value
         */
        synchronized void put(String value) {
            if (valueSet)
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.d(TAG, "put: InterruptedException caught");

                }
            this.value = value;
            valueSet = true;
            Log.d(TAG, "put: "+value);
            notify();
        }
    }
}
