package com.cpen321.modernwaiter.ui.order;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cpen321.modernwaiter.HARDCODED;
import com.cpen321.modernwaiter.MainActivity;
import com.cpen321.modernwaiter.R;
import com.cpen321.modernwaiter.ui.MenuItem;

import java.util.HashMap;

public class OrderFragment extends Fragment {

    public final Fragment thisFragment =this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        Button startBillButton = view.findViewById(R.id.startBillButton);
        startBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_navigation_order_to_navigation_bill);
            }
        });


        // Set the adapter
        Context context = view.getContext();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.order_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        MainActivity mainActivity = (MainActivity) getActivity();
        HashMap<MenuItem, Integer> billMap = mainActivity.tableSession.getCart();

        OrderRecyclerAdapter orderRecyclerAdapter = new OrderRecyclerAdapter(billMap);
        recyclerView.setAdapter(orderRecyclerAdapter);

        Button checkoutButton = view.findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                notifyCheckout();
                mainActivity.tableSession.checkout();
                while (orderRecyclerAdapter.itemArray.size() != 0) {
                    orderRecyclerAdapter.itemArray.remove(0);
                    orderRecyclerAdapter.notifyItemRemoved(0);
                    orderRecyclerAdapter.notifyItemRangeChanged(0, orderRecyclerAdapter.itemArray.size());
                }
            }
        });

        return view;
    }
    private void notifyCheckout(){
        Log.i("IN CHECKOUT2","AS");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue((MainActivity) getActivity());
        String url = HARDCODED.URL+"checkout";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i("MSG:",response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("ERR:",error.toString());
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }
}