package com.cpen321.modernwaiter.payment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cpen321.modernwaiter.HARDCODED;
import com.cpen321.modernwaiter.MainActivity;
import com.cpen321.modernwaiter.R;
import com.cpen321.modernwaiter.TableSession;
import com.cpen321.modernwaiter.ui.MenuItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public class StripePayment extends AppCompatActivity {
    /**
     * This example collects card payments, implementing the guide here: https://stripe.com/docs/payments/accept-a-payment-synchronously#android
     */
    // 10.0.2.2 is the Android emulator's alias to localhost
    private static final String BACKEND_URL = "http://10.0.2.2:3000/";
    private double totalAmount = 0;
    private Stripe stripe;
    private Activity context = this;
    private int num_users = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_payment);
        loadPage();

        TextView payment_option = findViewById(R.id.payment_option);
        TextView amount_to_pay = findViewById(R.id.amount_to_pay);
        String option_text;
        String amount_text;

        getAmountToPay();
        if(MainPayment.option.equals("payForAll")) option_text = "Total amount to be paid is:";
        else if(MainPayment.option.equals("paySplitEvenly")) option_text = "Total amount to be paid by you after splitting evenly is:";
        else if(MainPayment.option.equals("payPerItem")) option_text = "Toatl amount to be paid by you for the items you selected is:";
        else option_text = "Oops! Looks like something went wrong with your billing";

        payment_option.setText(option_text);

        amount_text = "$ " + String.valueOf(totalAmount) + " CAD";
        amount_to_pay.setText(amount_text);

    }

    private void loadPage() {
        // Clear the card widget
        CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
        cardInputWidget.clear();

        requestKey();
    }

    private void requestKey() {
        RequestQueue queue = Volley.newRequestQueue(this);
        // For added security, our sample app gets the publishable key from the server

        StringRequest request = new StringRequest(
                Request.Method.GET,
                BACKEND_URL + "stripe/key",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Map<String, String> responseMap = parseResponseToMap(response);

                        final String stripePublishableKey = responseMap.get("publishableKey");
                        if (stripePublishableKey != null) {
                            System.out.println(stripePublishableKey);
                            onRetrievedKey(stripePublishableKey);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                });

        queue.add(request);
    }

    private void pay() {
        CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();

        if (params == null) {
            return;
        }
        stripe.createPaymentMethod(params, new ApiResultCallback<PaymentMethod>() {
            @Override
            public void onSuccess(@NonNull PaymentMethod result) {
                // Create and confirm the PaymentIntent by calling the sample server's /pay endpoint.
                sendPaymentMethod(result.id, null);
            }

            @Override
            public void onError(@NonNull Exception e) {

            }
        });
    }

    /**
     * Function to get the amount to be paid by a user
     * @return totalAmount
     */
    private double getAmountToPay(){

        if(MainPayment.option.equals("payperItem")){
            totalAmount = 0;
            String url = HARDCODED.URL + "order/user/" + HARDCODED.USER_ID + "?isActive=1";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            JSONArray jsonArray = null;
                            try {
                                jsonArray = response.getJSONArray("data");
                                //only need this user's bill
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                int id = jsonObject.getInt("id");
                                int tableId = jsonObject.getInt("tables_id");
                                double amount = jsonObject.getDouble("users_id");
                                int has_paid = jsonObject.getInt("has_paid");
                                int is_active_session = jsonObject.getInt("is_active_session");
                                //TODO: check logic for these values, I ignore description and quantity when getting data from backend
                                totalAmount = amount;

                            } catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error

                        }
                    });
            MainActivity.requestQueue.add(jsonObjectRequest);
            //Make GET Request to get the bill for the entire table
            return totalAmount;
        }
        else{
            totalAmount = 0;
            String url = HARDCODED.URL + "order/table/"+ HARDCODED.TABLE_ID + "?isActive=1";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            JSONArray jsonArray = null;
                            try {
                                jsonArray = response.getJSONArray("data");
                                for( int i = 0; i<jsonArray.length (); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    int id = jsonObject.getInt("id");
                                    int tableId = jsonObject.getInt("tables_id");
                                    double amount = jsonObject.getDouble("users_id");
                                    int has_paid = jsonObject.getInt("has_paid");
                                    int is_active_session = jsonObject.getInt("is_active_session");
                                    totalAmount = totalAmount + amount;
                                    num_users++;
                                }
                            } catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error

                        }
                    });
            MainActivity.requestQueue.add(jsonObjectRequest);
            //get the bill for the entire table
            if(MainPayment.option.equals("payForAll")){
                return totalAmount;
            }
            //get bill after splitting evenly
            else if(MainPayment.option.equals("paySplitEvenly")){
                return totalAmount/num_users;
            }
            //it should never get here really !!
            else {
                totalAmount = 0;
                return totalAmount;
            }
        }
    }

    private void sendPaymentMethod(@Nullable String paymentMethodId, @Nullable String paymentIntentId) {

        final Map<String, String> bodyFields = new HashMap<>();

        // TODO: GENERATE THIS AUTOMATICALLY
        if (paymentMethodId != null) {
            bodyFields.put("useStripeSdk", "true");
            bodyFields.put("paymentMethodId", paymentMethodId);
            bodyFields.put("currency", "cad");
            // TODO:
            bodyFields.put("amounts", String .valueOf(totalAmount));
            bodyFields.put("items", "fried_rice");
        } else {
            bodyFields.put("paymentIntentId", paymentIntentId);
        }

        final String bodyJSON = new Gson().toJson(bodyFields);
        RequestQueue queue = Volley.newRequestQueue(this);

        Intent startPostPayment = new Intent(this, PostPayment.class);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                BACKEND_URL + "stripe/pay",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);

                        Map<String, String> responseMap = parseResponseToMap(response);

                        String error = responseMap.get("error");
                        String paymentIntentClientSecret = responseMap.get("clientSecret");
                        String requiresAction = responseMap.get("requiresAction");

                        if (error != null) {
                            displayAlert("Error", error, false);
                        } else if (paymentIntentClientSecret != null) {
                            if ("true".equals(requiresAction)) {
                                stripe.handleNextActionForPayment(context, paymentIntentClientSecret);
                            } else {
                                putPaid();
                                startActivity(startPostPayment);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return bodyJSON.getBytes();
            }
        };

        queue.add(stringRequest);
    }

    private Map<String, String> parseResponseToMap(String response) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        final Map<String, String> responseMap;
        if (response != null) {
            responseMap = gson.fromJson(response, type);
        } else {
            responseMap = new HashMap<>();
        }

        return responseMap;
    }

    private void displayAlert(@NonNull String title, @NonNull String message, boolean restartDemo) {
        runOnUiThread(() -> {
            final AlertDialog.Builder builder =
                    new AlertDialog.Builder(this)
                            .setTitle(title)
                            .setMessage(message);
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            if (restartDemo) {
                builder.setPositiveButton("Restart demo",
                        (DialogInterface dialog, int index) -> loadPage());
            } else {
                builder.setPositiveButton("Ok", null);
            }
            builder
                    .create()
                    .show();
        });
    }

    private void onRetrievedKey(@NonNull String stripePublishableKey) {
        // Configure the SDK with your Stripe publishable key so that it can make requests to the Stripe API
        final Context applicationContext = getApplicationContext();
        PaymentConfiguration.init(applicationContext, stripePublishableKey);
        stripe = new Stripe(applicationContext, stripePublishableKey);

        // Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener((View view) -> pay());
    }

    /**
     * PUT request to notify backend that the amount has been paid
     * RIGHT NOW FOR PAY_FOR_ALL ONLY
     */
    private void putPaid(){
        //PUT request to confirm that the order has been paid
        String url = HARDCODED.URL + "ordered-items/paid/" + "?isActive=1";
        //TODO: pass order_id, needs billfragment
        HashMap<MenuItem, Integer> orderedItems = MainActivity.tableSession.getBill();
        for( Map.Entry<MenuItem, Integer> item : orderedItems.entrySet() ) {
            Map<String,String> params = new HashMap<>();
            params.put("orderId", String.valueOf(MainActivity.tableSession.getOrderId()));
            params.put("itemId", String.valueOf(item.getKey().id));
            params.put("hasPaid", "1");
            JSONObject parameters = new JSONObject(params);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.PUT, url, parameters, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            //on success
                            //TODO: print some message or not
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            // TODO: Handle error

                        }
                    });
            MainActivity.requestQueue.add(jsonObjectRequest);
        }
    }
}
