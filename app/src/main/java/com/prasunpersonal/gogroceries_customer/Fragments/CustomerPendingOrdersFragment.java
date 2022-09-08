package com.prasunpersonal.gogroceries_customer.Fragments;

import static com.prasunpersonal.gogroceries_customer.App.ME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prasunpersonal.gogroceries_customer.Activities.CustomerOrderDetailsActivity;
import com.prasunpersonal.gogroceries_customer.Adapters.AdapterOrder;
import com.prasunpersonal.gogroceries_customer.Models.ModelOrder;
import com.prasunpersonal.gogroceries_customer.databinding.FragmentCustomerPendingOrdersBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class CustomerPendingOrdersFragment extends Fragment {
    FragmentCustomerPendingOrdersBinding binding;
    ArrayList<ModelOrder> orders;
    Context context;

    public CustomerPendingOrdersFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCustomerPendingOrdersBinding.inflate(getLayoutInflater());
        context = binding.getRoot().getContext();

        orders = new ArrayList<>();

        binding.customerPendingOrders.setLayoutManager(new LinearLayoutManager(context));
        binding.customerPendingOrders.setAdapter(new AdapterOrder(context, orders, (order, position) -> startActivity(new Intent(context, CustomerOrderDetailsActivity.class).putExtra("ORDER_ID", order.getOrderId()))));

        loadOrders();
        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadOrders() {
        FirebaseFirestore.getInstance().collection("Orders").whereEqualTo("customerId", ME.getUid()).orderBy("orderStatus").orderBy("orderTime", Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                orders.clear();
                for (QueryDocumentSnapshot doc : value) {
                    ModelOrder order = doc.toObject(ModelOrder.class);
                    if (order.getOrderStatus() == ModelOrder.DELIVERED) break;
                    orders.add(order);
                }
                Collections.reverse(orders);
                Objects.requireNonNull(binding.customerPendingOrders.getAdapter()).notifyDataSetChanged();
                Toast.makeText(context, "Order list updated just now", Toast.LENGTH_SHORT).show();
            }
        });
    }
}