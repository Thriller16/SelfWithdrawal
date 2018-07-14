package com.example.lawrene.selfwithdrawal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.vistrav.ask.Ask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    //    Makes the network request
    private static final int PERMISSION_REQUEST = 100;
    String finalResult;
    HttpParse httpParse = new HttpParse();
    HashMap<String, String> hashmap = new HashMap<>();
    String HttpURL1 = "http://www.caurix.net/insertNew.php";
    String HttpURL2 = "http://www.caurix.net/change_server_balance.php";

    String smsBody;
    String smsSender;
    String amount;
    String pin;

    String ussdCode = "blank";
    TextView textView;
    List<Response> responseList;
    DatabaseAccess databaseAccess;
    List<Transaction> transactionList;
    TransactionAdapter transactionAdapter;
    ListView transactionListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkpermission();

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        transactionListView = findViewById(R.id.transaction_list);
        responseList = databaseAccess.getResponses();
        transactionList = databaseAccess.getTransactions();

        textView = findViewById(R.id.textview);

        if(!transactionList.isEmpty()){
            textView.setVisibility(View.GONE);
        }

        transactionAdapter = new TransactionAdapter(this, transactionList);
        transactionListView.setAdapter(transactionAdapter);

        smsBody = getIntent().getStringExtra("body");
        smsSender = getIntent().getStringExtra("sender");

//        smsSender = "+221781463122";
//        smsBody = "retrait*300";

        if (smsSender != null) {
            smsSender = smsSender.substring(4);
        }

//        -------------------------------------THis handles a case where there is internet connection----------------------
        if (isConnectingToInternet(MainActivity.this)) {
////            Check for the local ones to upload them
//            for (int i = 0; i < responseList.size(); i++) {
//                getRequest(responseList.get(i).getPhoneNumber(), responseList.get(i).getAmount(), responseList.get(i).getConfirmation_id());
//            }
            if (smsBody != null) {

                if (smsBody.contains("reussi")) {
                    if (parseSMS(smsBody).length() > 20) {
                        getRequest(databaseAccess.getPhoneNumber(), databaseAccess.getAmount(), parseSMS(smsBody));
                    }
                }
                else if (smsBody.contains("FAIL")) {
                    Log.i("TAG", "SMS FAILED");
                }
                else if (smsBody.contains("retrait")) {

                    amount = databaseAccess.getAmount();
                    pin = databaseAccess.getPin();
                    databaseAccess.storePhoneNumber(smsSender);

                    ussdCode = Uri.encode("#") + "143" + Uri.encode("#") + "*1*" + smsSender + "*" + amount + "*" + pin + Uri.encode("#");
                    Toast.makeText(this, "" + ussdCode, Toast.LENGTH_LONG).show();

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdCode)));

                } else {
                    Toast.makeText(this, "This message is useless", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }
//
//
////        ----------------------------------This handles a case where there is no internet-----------------------------
//        else {
//
//            if (smsBody != null) {
//
//                if (smsBody.contains("reussi")) {
//                    databaseAccess.addOfflineRecord(smsSender, amount, parseSMS(smsBody));
//                }
//
//                else if (smsBody.contains("FAIL")) {
//                    Log.i("TAG", "SMS FAILED");
//                }
//
//                else if (smsBody.contains("retrait*")) {
//                    amount = "50";
//
//                    ussdCode = Uri.encode("#") + "143#*1*" + smsSender + "*" + amount + "*" + "1217" + Uri.encode("#");
//                    Toast.makeText(this, "" + ussdCode, Toast.LENGTH_LONG).show();
//
//                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdCode)));
//                }
//            }
//
//        }

//    }

    public void getRequest(final String phonenumber, final String amount, final String confirmationid) {
        String url = "http://www.caurix.net/selfpayment.php";
        final AsyncHttpClient client = new AsyncHttpClient();


        client.get(this, url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONArray arr = new JSONArray(responseString);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject eachObject = arr.getJSONObject(i);

                        String phone_number = eachObject.getString("phone_number");
                        String bank_id = eachObject.getString("bank_id");
                        String bank_name = eachObject.getString("bank_name");
                        String balance = eachObject.getString("balance");

                        if (phonenumber.equals(phone_number)) {
                            if (Integer.parseInt(balance) > Integer.parseInt(amount)) {
                                Toast.makeText(MainActivity.this, "Records have been Updated", Toast.LENGTH_SHORT).show();
                                updateRecords(phone_number, bank_id, bank_name, amount, confirmationid);

                                String newBalance = String.valueOf(Integer.parseInt(balance) - Integer.parseInt(amount));
                                changeBalance(phone_number, newBalance);


                                databaseAccess.addTransaction(phone_number, amount,confirmationid, newBalance);

                                transactionList = databaseAccess.getTransactions();
                                transactionAdapter = new TransactionAdapter(MainActivity.this, transactionList);
                                transactionListView.setAdapter(transactionAdapter);
                                transactionAdapter.notifyDataSetChanged();

                            }
                        }
//                        Toast.makeText(MainActivity.this, "" + phone_number, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateRecords(String number, String bank_id, String bank_name, String balance, String confirmation_id) {
        class UpdateRecords extends AsyncTask<String, Void, String> {

            @Override
            protected void onCancelled() {
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                Toast.makeText(MainActivity.this, "" + s.toString(), Toast.LENGTH_SHORT).show();
                Log.i("TAG", "" + s.toString());
            }

            @Override
            protected String doInBackground(String... params) {
                hashmap.put("phone_number", params[0]);
                hashmap.put("bank_id", params[1]);
                hashmap.put("bank_name", params[2]);
                hashmap.put("balance", params[3]);
                hashmap.put("confirmation_id", params[4]);

                finalResult = httpParse.postRequest(hashmap, HttpURL1);
                return finalResult;
            }
        }

        UpdateRecords updateRecords = new UpdateRecords();
        updateRecords.execute(number, bank_id, bank_name, balance, confirmation_id);
    }

    private void checkpermission() {
        Ask.on(this)
                .id(PERMISSION_REQUEST) // in case you are invoking multiple time Ask from same activity or fragment
                .forPermissions(Manifest.permission.RECEIVE_SMS
                        , Manifest.permission.CALL_PHONE)
                .withRationales("In other for the app to work perfectly you have to give it permission") //optional
                .go();
    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public void changeBalance(String phone_number, String balance){
        class ChangeBalance extends AsyncTask<String, Void, String> {

            @Override
            protected void onCancelled() {
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                Toast.makeText(MainActivity.this, "" + s.toString(), Toast.LENGTH_SHORT).show();
                Log.i("TAG", "" + s.toString());
            }

            @Override
            protected String doInBackground(String... params) {
                hashmap.put("phone_number", params[0]);
                hashmap.put("balance", params[1]);

                finalResult = httpParse.postRequest(hashmap, HttpURL2);
                return finalResult;
            }
        }

        ChangeBalance changeBalance = new ChangeBalance();
        changeBalance.execute(phone_number, balance);
    }

    //
    public String parseSMS(String original) {
        String test = "R" + original.substring(original.lastIndexOf("Ref") + 1);
        String test2 = test.substring(test.lastIndexOf(".") - 7);
        String finalNumber = test.replace(test2, "");

        return finalNumber;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(MainActivity.this, ConfigureActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
