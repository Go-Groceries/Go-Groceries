package com.prasunpersonal.gogroceries_customer.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prasunpersonal.gogroceries_customer.Activities.ShopDetailsActivity;
import com.prasunpersonal.gogroceries_customer.Adapters.AdapterShop;
import com.prasunpersonal.gogroceries_customer.Models.ModelShop;
import com.prasunpersonal.gogroceries_customer.databinding.FragmentCustomerShopsBinding;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CustomerShopsFragment extends Fragment {
    private static String CURRENT_CITY;
    FragmentCustomerShopsBinding binding;
    Context context;

    ArrayList<ModelShop> localShops;
    ArrayList<ModelShop> otherShops;

    public CustomerShopsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCustomerShopsBinding.inflate(inflater, container, false);
        context = binding.getRoot().getContext();

        detectLocation();

        localShops = new ArrayList<>();
        binding.cityShops.setLayoutManager(new LinearLayoutManager(context));
        binding.cityShops.setAdapter(new AdapterShop(context, localShops, shop -> startActivity(new Intent(context, ShopDetailsActivity.class).putExtra("SHOP", Parcels.wrap(shop)))));

        otherShops = new ArrayList<>();
        binding.otherShops.setLayoutManager(new LinearLayoutManager(context));
        binding.otherShops.setAdapter(new AdapterShop(context, otherShops, shop -> startActivity(new Intent(context, ShopDetailsActivity.class).putExtra("SHOP", Parcels.wrap(shop)))));


        binding.searchShops.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((AdapterShop) Objects.requireNonNull(binding.cityShops.getAdapter())).getFilter().filter(s);
                ((AdapterShop) Objects.requireNonNull(binding.otherShops.getAdapter())).getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location -> {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(context, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (CURRENT_CITY == null || !CURRENT_CITY.equals(addresses.get(0).getLocality())) {
                        CURRENT_CITY = addresses.get(0).getLocality();
                        loadShops();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadShops() {
        // Load shops in city
        FirebaseFirestore.getInstance().collection("Shops").whereEqualTo("city", CURRENT_CITY).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                localShops.clear();
                for (DocumentSnapshot doc : value) {
                    localShops.add(doc.toObject(ModelShop.class));
                }
                ((AdapterShop) Objects.requireNonNull(binding.cityShops.getAdapter())).notifyChange();
            }
        });

        // Load other shops
        FirebaseFirestore.getInstance().collection("Shops").whereNotEqualTo("city", CURRENT_CITY).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                otherShops.clear();
                for (DocumentSnapshot doc : value) {
                    otherShops.add(doc.toObject(ModelShop.class));
                }
                ((AdapterShop) Objects.requireNonNull(binding.otherShops.getAdapter())).notifyChange();
            }
        });

    }
}