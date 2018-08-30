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
    String smsSenderParsed;
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
        if (!transactionList.isEmpty()) {
            textView.setVisibility(View.GONE);
        }

        transactionAdapter = new TransactionAdapter(this, transactionList);
        transactionListView.setAdapter(transactionAdapter);
//
        smsBody = getIntent().getStringExtra("body");
        smsSender = getIntent().getStringExtra("sender");

//        smsSender = "+221781463122";
//        smsBody = "Paiement du 781463122 vers 778751544 reussi.Montant 65.00FCFA.Commission:1.62FCFA.Nouveau\" +\n" +
//                "\"solde:2526.45FCFA.Ref:MP180707.0253.B76597. Merci.OFMS";

//        smsBody = "retrait*700";


        if (smsSender != null) {
            smsSenderParsed = smsSender.substring(4);
        }

//        -------------------------------------THis handles a case where there is internet connection----------------------
        if (isConnectingToInternet(MainActivity.this)) {
////            Check for the local ones to upload them
//            for (int i = 0; i < responseList.size(); i++) {
//                getRequest(responseList.get(i).getPhoneNumber(), responseList.get(i).getAmount(), responseList.get(i).getConfirmation_id());
//            }
            if (smsBody != null) {

                if (smsBody.toLowerCase().contains("reussi")) {
                    if (parseSMS(smsBody).length() > 20) {
                        databaseAccess.storeConfirmationId(parseSMS(smsBody));
                        changeBalance(databaseAccess.getPhoneNumber(), databaseAccess.getNewBalance());
                    }
                }
                else if (smsBody.contains("FAIL")) {
                    Log.i("TAG", "SMS FAILED");
                }

                else if (smsBody.toLowerCase().contains("retrait")) {


                    databaseAccess.storePhoneNumber(smsSender);


                    StringTokenizer stringTokenizer = new StringTokenizer(smsBody, "*");
                    stringTokenizer.nextToken();
                    amount = stringTokenizer.nextToken();

                    databaseAccess.storeAmount(amount);

                    checkBalanceAndDail(smsSender, databaseAccess.getAmount());

                } else {
                    Toast.makeText(this, "This message is useless", Toast.LENGTH_SHORT).show();
                }

            }
        } else {
            Toast.makeText(this, "No internet connection. Please put on mobile data or wifi to use the app", Toast.LENGTH_SHORT).show();
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

    public void checkBalanceAndDail(final String phonenumber, final String amount) {
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

                        Log.i("TAG", phone_number + "    " + phonenumber);
                        if (phonenumber.equals(phone_number)) {
                            if (Integer.parseInt(balance) > Integer.parseInt(amount)) {
                                String newBalance = String.valueOf(Integer.parseInt(balance) - Integer.parseInt(amount));
////
                                smsSenderParsed = databaseAccess.getPhoneNumber().substring(4);

                                Toast.makeText(MainActivity.this, "Previous balance " + balance
                                        + " the current banace is " + newBalance, Toast.LENGTH_SHORT).show();

                                ussdCode = Uri.encode("#") + "145" + Uri.encode("#") + "*1*" + smsSenderParsed + "*" + amount + "*" + databaseAccess.getPin() + Uri.encode("#");
                                Toast.makeText(MainActivity.this, "" + ussdCode, Toast.LENGTH_LONG).show();

                                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
                                databaseAccess.storeAmount(amount);
                                databaseAccess.storePhoneNumber(smsSender);
                                databaseAccess.storeNewBalance(newBalance);
                                databaseAccess.storeBankId(bank_id);
                                databaseAccess.storeBankName(bank_name);

                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Customer does not have enough money", Toast.LENGTH_SHORT).show();
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void postTransaction(String number, String bank_id, String bank_name, String balance, String confirmation_id) {
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

    public void changeBalance(String phone_number, String balance) {
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
                Log.i("CHANGE", "" + s.toString());
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

        postTransaction(phone_number, databaseAccess.getBankId(), databaseAccess.getBankName(), databaseAccess.getAmount(), databaseAccess.getConfirmationId());
        databaseAccess.addTransaction(phone_number, databaseAccess.getAmount(), databaseAccess.getConfirmationId(), databaseAccess.getNewBalance());
//
        transactionList = new ArrayList<>();
        transactionList = databaseAccess.getTransactions();
        transactionAdapter = new TransactionAdapter(MainActivity.this, transactionList);
        transactionListView.setAdapter(transactionAdapter);
        transactionAdapter.notifyDataSetChanged();
    }

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
