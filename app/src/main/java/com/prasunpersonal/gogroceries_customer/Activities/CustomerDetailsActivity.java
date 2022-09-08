package com.prasunpersonal.gogroceries_customer.Activities;

import static com.prasunpersonal.gogroceries_customer.Activities.ShopDetailsActivity.SELECTED_ITEMS;
import static com.prasunpersonal.gogroceries_customer.App.ME;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.prasunpersonal.gogroceries_customer.Models.ModelOrder;
import com.prasunpersonal.gogroceries_customer.Models.ModelShop;
import com.prasunpersonal.gogroceries_customer.databinding.ActivityCustomerDetailsBinding;

import org.parceler.Parcels;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CustomerDetailsActivity extends AppCompatActivity {
    ActivityCustomerDetailsBinding binding;
    ModelShop shop;

    private double latitude = 0.0, longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.customerDetailsToolbar);

        binding.customerDetailsToolbar.setNavigationOnClickListener(v -> finish());

        shop = Parcels.unwrap(getIntent().getParcelableExtra("SHOP"));

        binding.cName.setText(ME.getName());
        binding.cPhone.setText(ME.getPhone());
        binding.cEmail.setText(ME.getEmail());

        binding.cCurrentLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Detecting location. Please wait.", Toast.LENGTH_SHORT).show();
                ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location -> {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        binding.cPincode.setText(addresses.get(0).getPostalCode());
                        binding.cCountry.setText(addresses.get(0).getCountryName());
                        binding.cState.setText(addresses.get(0).getAdminArea());
                        binding.cCity.setText(addresses.get(0).getLocality());
                        binding.cAddress.setText(addresses.get(0).getAddressLine(0));
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.placeOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        if (binding.cName.getText().toString().trim().isEmpty()) {
            binding.cName.setError("Name is required!");
            binding.cName.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.cEmail.getText().toString().trim()).matches()) {
            binding.cEmail.setError("Enter a valid Email address!");
            binding.cEmail.requestFocus();
            return;
        }
        if (!Patterns.PHONE.matcher(binding.cPhone.getText().toString().trim()).matches()) {
            binding.cPhone.setError("Enter a valid phone number!");
            binding.cPhone.requestFocus();
            return;
        }
        if (binding.cAddress.getText().toString().trim().isEmpty()) {
            binding.cAddress.setError("Shop address is required!");
            binding.cAddress.requestFocus();
            return;
        }
        if (binding.cCity.getText().toString().trim().isEmpty()) {
            binding.cCity.setError("Town/City is required!");
            binding.cCity.requestFocus();
            return;
        }
        if (binding.cState.getText().toString().trim().isEmpty()) {
            binding.cState.setError("State is required!");
            binding.cState.requestFocus();
            return;
        }
        if (binding.cCountry.getText().toString().trim().isEmpty()) {
            binding.cCountry.setError("Country is required!");
            binding.cCountry.requestFocus();
            return;
        }
        if (binding.cPincode.getText().toString().trim().isEmpty()) {
            binding.cPincode.setError("Pincode is required!");
            binding.cPincode.requestFocus();
            return;
        }

        binding.placeOrder.setEnabled(false);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocationName(String.format(Locale.getDefault(), "%s, %s, %s, %s, %s", binding.cAddress.getText().toString().trim(), binding.cCity.getText().toString().trim(), binding.cState.getText().toString().trim(), binding.cCountry.getText().toString().trim(), binding.cPincode.getText().toString().trim()), 1);
            latitude = addresses.get(0).getLatitude();
            longitude = addresses.get(0).getLongitude();

            binding.cPincode.setText(addresses.get(0).getPostalCode());
            binding.cCountry.setText(addresses.get(0).getCountryName());
            binding.cState.setText(addresses.get(0).getAdminArea());
            binding.cCity.setText(addresses.get(0).getLocality());
            binding.cAddress.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.placeOrder.setEnabled(true);
        }

        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Address can't be found!", Toast.LENGTH_SHORT).show();
            binding.placeOrder.setEnabled(true);
            return;
        }

        String orderId = String.format(Locale.getDefault(), "%s%s%d", ME.getUid(), shop.getUid(), System.currentTimeMillis());

        ModelOrder order = new ModelOrder(orderId, ME.getUid(), shop.getUid(), shop.getDeliveryCharge(), latitude, longitude, binding.cAddress.getText().toString().trim(), SELECTED_ITEMS);
        FirebaseFirestore.getInstance().collection("Orders").document(order.getOrderId()).set(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                SELECTED_ITEMS.clear();
                finish();
            } else {
                Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
            binding.placeOrder.setEnabled(true);
        });
    }
}