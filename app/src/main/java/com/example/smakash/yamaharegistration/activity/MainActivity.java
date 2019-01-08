package com.example.smakash.yamaharegistration.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smakash.yamaharegistration.R;
import com.example.smakash.yamaharegistration.app.AppConfig;
import com.example.smakash.yamaharegistration.app.AppController;
import com.example.smakash.yamaharegistration.helper.SQLiteHandler;
import com.example.smakash.yamaharegistration.helper.SessionManager;

import static com.example.smakash.yamaharegistration.app.AppController.TAG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.pusher.pushnotifications.PushNotifications;

import com.google.firebase.iid.FirebaseInstanceId;


public class MainActivity extends AppCompatActivity {

    private TextView nameTV,mobileTV,engineTV,chasisTV,invoiceTV, notice,invoiceNoTV;

    private CheckBox moneyCheck,brtaCheck,passportCheck,nidCheck,utilityCheck;

    private int checkX = 0;

    private SQLiteHandler db;
    private SessionManager session;

    private ProgressDialog pDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        String token = FirebaseInstanceId.getInstance().getToken();
//        Log.d("Tokennnnnnn",token);

        PushNotifications.start(getApplicationContext(), "045181e6-9065-4603-8cad-74baa2c563cc");
        PushNotifications.subscribe("hello");

        nameTV = (TextView) findViewById(R.id.nameTV);
        mobileTV = (TextView) findViewById(R.id.mobileTV);
        engineTV = (TextView) findViewById(R.id.engineTV);
        chasisTV = (TextView) findViewById(R.id.chasisTV);
        invoiceTV = (TextView) findViewById(R.id.invoiceTV);
        invoiceNoTV = (TextView) findViewById(R.id.invoiceNoTV);
        notice = (TextView) findViewById(R.id.notice);

        moneyCheck = (CheckBox) findViewById(R.id.moneyCheck);
        brtaCheck = (CheckBox) findViewById(R.id.brtaCheck);
        passportCheck = (CheckBox) findViewById(R.id.passportCheck);
        nidCheck = (CheckBox) findViewById(R.id.nidCheck);
        utilityCheck = (CheckBox) findViewById(R.id.utilityCheck);

        isNetworkConnectionAvailable();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        final Bundle bundle = getIntent().getExtras();
        final String UserId = bundle.getString("UserId");

        if(isNetworkConnectionAvailable()) {
            getInvoiceData(UserId);
        }

    }

    public void checkNetworkConnection(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();
                isNetworkConnectionAvailable();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isNetworkConnectionAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if(isConnected) {
            Log.d("Network", "Connected");
            return true;
        }
        else{
            checkNetworkConnection();
            Log.d("Network","Not Connected");
            return false;
        }
    }

    private void getInvoiceData(final String UserId){
        String tag_string_req = "req_assign";

        pDialog.setMessage("Please Wait ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.INVOICE_DATA + "?userid=" + String.valueOf(UserId), new Response.Listener<String>() {
            int x = 1;
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Getting Pending Service: " + response.toString());
//                hideDialog();
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    //boolean error = jObj.getBoolean("error");
                    String status = jObj.getString("StatusCode");
                    Log.d("Pending--->", status);
                    // Check for error node in json
                    if (status.equals("200")) {



                        String StatusMessage = jObj.getString("StatusMessage");

                        JSONArray statusMessageArray = new JSONArray(StatusMessage);

                        String ShowDeliverablesPanel = jObj.getString("ShowDeliverablesPanel");

                        String RegistrationType = jObj.getString("RegistrationType");

                        String CustomerInfo = jObj.getString("CustomerInfo");
                        JSONArray customerInfoArray = new JSONArray(CustomerInfo);

                        String customerName = customerInfoArray.getJSONObject(0).getString("CustomerName");
                        String mobile = customerInfoArray.getJSONObject(0).getString("Mobile");
                        String engineNo = customerInfoArray.getJSONObject(0).getString("EngineNo");
                        String chasisNo = customerInfoArray.getJSONObject(0).getString("ChasisNo");
                        String invoiceDate = customerInfoArray.getJSONObject(0).getString("InvoiceDate");
                        String invoiceNo = customerInfoArray.getJSONObject(0).getString("InvoiceNo");

                        nameTV.setText(customerName);
                        mobileTV.setText(mobile);
                        engineTV.setText(engineNo);
                        chasisTV.setText(chasisNo);
                        invoiceTV.setText(invoiceDate);
                        invoiceNoTV.setText(invoiceNo);

                        nameTV.setTextAppearance(getApplicationContext(),R.style.largeText);

                        if(RegistrationType.equals("ACI")) {

                            Log.d("StatusMessage", "Length->" + String.valueOf(statusMessageArray.length()));

                            //                        String Status = statusMessageArray.getJSONObject(0).getString("Status");
                            //                        String InvoiceNo = statusMessageArray.getJSONObject(0).getString("InvoiceNo");
                            String UserName = statusMessageArray.getJSONObject(0).getString("UserId__UserName");


                            String moneyCheckS = statusMessageArray.getJSONObject(0).getString("Status");
                            String brtaCheckS = statusMessageArray.getJSONObject(1).getString("Status");
                            String passportCheckS = statusMessageArray.getJSONObject(2).getString("Status");
                            String photocopyCheckS = statusMessageArray.getJSONObject(3).getString("Status");
                            String utilityCheckS = statusMessageArray.getJSONObject(4).getString("Status");

                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");

                            Date bDdate = new Date();
                            Date brtaDDate = new Date();
                            Date passportDDate = new Date();
                            Date photocopyDDate = new Date();
                            Date utilityDDate = new Date();

                            Date bankDDate = new Date();
                            Date submitBRTADDate = new Date();
                            Date taxTokenDDate = new Date();

                            String bankDepositDate = statusMessageArray.getJSONObject(0).getString("EntryDate");
                            String brtaDate = statusMessageArray.getJSONObject(1).getString("EntryDate");
                            String passportDate = statusMessageArray.getJSONObject(2).getString("EntryDate");
                            String photocopyDate = statusMessageArray.getJSONObject(3).getString("EntryDate");
                            String utilityDate = statusMessageArray.getJSONObject(4).getString("EntryDate");

                            String bankDate = statusMessageArray.getJSONObject(5).getString("EntryDate");
                            String submitBRTADate= statusMessageArray.getJSONObject(6).getString("EntryDate");
                            String taxTokenDate = statusMessageArray.getJSONObject(7).getString("EntryDate");

                            bDdate = df.parse(bankDepositDate);
                            String bDdateS = df1.format(bDdate);

                            brtaDDate = df.parse(brtaDate);
                            String brtaDDateS = df1.format(brtaDDate);

                            passportDDate = df.parse(passportDate);
                            String passportDDateS = df1.format(passportDDate);

                            photocopyDDate = df.parse(photocopyDate);
                            String photocopyDDateS = df1.format(photocopyDDate);

                            utilityDDate = df.parse(utilityDate);
                            String utilityDDateS = df1.format(utilityDDate);
                            String bankDDateS = null,submitBRTADDateS =null,taxTokenDDateS=null;

                            if(!bankDate.equals("null")){
                                bankDDate = df.parse(bankDate);
                                bankDDateS = df1.format(bankDDate);
                            }
                            else{
                                bankDDateS = "Yet To Publish";
                            }

                            if(!submitBRTADate.equals("null")){
                                submitBRTADDate = df.parse(submitBRTADate);
                                submitBRTADDateS = df1.format(submitBRTADDate);
                            }

                            else{
                                submitBRTADDateS = "Yet To Publish";
                            }

                            if(!taxTokenDate.equals("null")){
                                taxTokenDDate = df.parse(taxTokenDate);
                                taxTokenDDateS = df1.format(taxTokenDDate);
                            }

                            else{
                                taxTokenDDateS = "Yet To Publish";
                            }

                            Log.d("Check", moneyCheckS + brtaCheckS + passportCheckS + photocopyCheckS + utilityCheckS);



                            if (ShowDeliverablesPanel.equals("1")) {





                                checkX++;
                                moneyCheck.setVisibility(View.INVISIBLE);
                                brtaCheck.setVisibility(View.GONE);
                                passportCheck.setVisibility(View.GONE);
                                nidCheck.setVisibility(View.GONE);
                                utilityCheck.setVisibility(View.GONE);

                                LinearLayout llMoney = (LinearLayout) findViewById(R.id.LLMoney);
                                LinearLayout llBRTA = (LinearLayout) findViewById(R.id.LLBRTA);
                                LinearLayout llPassport = (LinearLayout) findViewById(R.id.LLPassport);
                                LinearLayout llNID = (LinearLayout) findViewById(R.id.LLNID);
                                LinearLayout llUtility = (LinearLayout) findViewById(R.id.LLUtility);


                                TextView tvLM = new TextView(getApplicationContext());
                                TextView tvBRTA = new TextView(getApplicationContext());
                                TextView tvPassport = new TextView(getApplicationContext());
                                TextView tvNID = new TextView(getApplicationContext());
                                TextView tvUtility = new TextView(getApplicationContext());

                                TextView str1 = (TextView) findViewById(R.id.str1);
                                TextView str2 = (TextView) findViewById(R.id.str2);
                                TextView str3 = (TextView) findViewById(R.id.str3);
                                TextView str4 = (TextView) findViewById(R.id.str4);
                                TextView str5 = (TextView) findViewById(R.id.str5);

                                if (checkX < 2) {

                                    nameTV.setText(UserName);

                                    str1.setText("Bank Deposit");
                                    str2.setText("Submit to BRTA");
                                    str3.setText("Tax Token and Number");

                                    str4.setVisibility(View.GONE);
                                    str5.setVisibility(View.GONE);

                                    llNID.setVisibility(View.GONE);
                                    llUtility.setVisibility(View.GONE);

                                    tvLM.setText(bankDDateS);
                                    tvLM.setTextColor(Color.parseColor("#000000"));
                                    tvLM.setTypeface(null, Typeface.BOLD_ITALIC);
                                    llMoney.addView(tvLM);

                                    tvBRTA.setText(submitBRTADDateS);
                                    tvBRTA.setTextColor(Color.parseColor("#000000"));
                                    tvBRTA.setTypeface(null, Typeface.BOLD_ITALIC);
                                    llBRTA.addView(tvBRTA);

                                    tvPassport.setText(taxTokenDDateS);
                                    tvPassport.setTextColor(Color.parseColor("#000000"));
                                    tvPassport.setTypeface(null, Typeface.BOLD_ITALIC);
                                    llPassport.addView(tvPassport);


                                    notice.setText(R.string.CollectDocument);

                                }


                                Log.d("money!", bankDepositDate);

                            } else {

                                if (moneyCheckS.equals("Y")) {
                                    moneyCheck.setChecked(true);
                                } else {
                                    moneyCheck.setChecked(false);
                                }

                                if (brtaCheckS.equals("Y")) {
                                    brtaCheck.setChecked(true);
                                } else {
                                    brtaCheck.setChecked(false);
                                }

                                if (passportCheckS.equals("Y")) {
                                    passportCheck.setChecked(true);
                                } else {
                                    passportCheck.setChecked(false);
                                }

                                if (photocopyCheckS.equals("Y")) {
                                    nidCheck.setChecked(true);
                                } else {
                                    nidCheck.setChecked(false);
                                }

                                if (utilityCheckS.equals("Y")) {
                                    utilityCheck.setChecked(true);
                                } else {
                                    utilityCheck.setChecked(false);
                                }

                                nameTV.setText(UserName);
                                notice.setText(R.string.documentText2);
                            }
                        }

                        if(RegistrationType.equals("Self")) {
                            //nameTV.setText(UserName);

                            moneyCheck.setVisibility(View.INVISIBLE);
                            brtaCheck.setVisibility(View.GONE);
                            passportCheck.setVisibility(View.GONE);
                            nidCheck.setVisibility(View.GONE);
                            utilityCheck.setVisibility(View.GONE);

                            LinearLayout llMoney = (LinearLayout) findViewById(R.id.LLMoney);
                            LinearLayout llBRTA = (LinearLayout) findViewById(R.id.LLBRTA);
                            LinearLayout llPassport = (LinearLayout) findViewById(R.id.LLPassport);
                            LinearLayout llNID = (LinearLayout) findViewById(R.id.LLNID);
                            LinearLayout llUtility = (LinearLayout) findViewById(R.id.LLUtility);

                            llBRTA.setVisibility(View.GONE);
                            llPassport.setVisibility(View.GONE);
                            llNID.setVisibility(View.GONE);
                            llUtility.setVisibility(View.GONE);

                            TextView str1 = (TextView) findViewById(R.id.str1);
                            TextView str2 = (TextView) findViewById(R.id.str2);
                            TextView str3 = (TextView) findViewById(R.id.str3);
                            TextView str4 = (TextView) findViewById(R.id.str4);
                            TextView str5 = (TextView) findViewById(R.id.str5);


                            str2.setVisibility(View.GONE);
                            str3.setVisibility(View.GONE);
                            str4.setVisibility(View.GONE);
                            str5.setVisibility(View.GONE);

                            str1.setText("Document Status");

                            //String stsMsg = statusMessageArray.getJSONObject(0).toString();

                            x++;
                            StatusMessage = StatusMessage.replace("\"", "");;
                            StatusMessage = StatusMessage.replace("[","");
                            StatusMessage = StatusMessage.replace("]","");

                            if(!StatusMessage.equals("Pending")){
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");

                                Date selfDate = new Date();
                                selfDate = df.parse(StatusMessage);
                                StatusMessage = df1.format(selfDate);

                                notice.setText("Please Collect your document from your retailer showroom");
                            }

                            if(x>2) {
                                TextView tvStatus = new TextView(getApplicationContext());
                                tvStatus.setText(StatusMessage);

                                tvStatus.setTextColor(Color.parseColor("#000000"));
                                tvStatus.setGravity(Gravity.CENTER);
                                tvStatus.setTypeface(null, Typeface.BOLD_ITALIC);
                                llMoney.addView(tvStatus);
                                notice.setText("");
                            }

                        }

                    } else {
                        // Error in login. Get the error message
                        String statusCode = jObj.getString("StatusCode");
                        String errorMsg = jObj.getString("StatusMessage");
                        Toast.makeText(getApplicationContext(), "ERROR IN LOGIN", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
//                hideDialog();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(strReq);

        // Adding request to request queue

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        //requestQueue.getCache().clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.guideline_label){
            String userId = session.getUserId();
            Bundle bundle = new Bundle();
            bundle.putString("UserId",userId);
            Intent intent = new Intent(MainActivity.this,GuidelineActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }

        if (id == R.id.action_label) {
            logoutUser();
            Log.d("logout","logout has happend");
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
