package com.prasunpersonal.gogroceries_customer.Activities;

import static com.prasunpersonal.gogroceries_customer.App.ME;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.prasunpersonal.gogroceries_customer.Adapters.AdapterFragment;
import com.prasunpersonal.gogroceries_customer.Fragments.CustomerOrdersFragment;
import com.prasunpersonal.gogroceries_customer.Fragments.CustomerShopsFragment;
import com.prasunpersonal.gogroceries_customer.R;
import com.prasunpersonal.gogroceries_customer.databinding.ActivityMainCustomerBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainCustomerActivity extends AppCompatActivity {
    ActivityMainCustomerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle(ME.getName());
        setSupportActionBar(binding.toolbar);

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new CustomerShopsFragment());
        fragments.add(new CustomerOrdersFragment());

        binding.userMainViewpager.setAdapter(new AdapterFragment(getSupportFragmentManager(), getLifecycle(), fragments));
        binding.userMainViewpager.setOffscreenPageLimit(fragments.size());
        binding.userMainViewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.userMainTab.selectTab(binding.userMainTab.getTabAt(position));
            }
        });
        binding.userMainTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.userMainViewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.userMainViewpager.setCurrentItem(getIntent().getIntExtra("STARTING_TAB", 0));

        detectLocation();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_customer_menu, menu);
        Glide.with(this).load(ME.getDp()).placeholder(R.drawable.ic_person).into((ImageView) menu.findItem(R.id.profile).getActionView().findViewById(R.id.profileMenuBtnPhoto));
        menu.findItem(R.id.profile).getActionView().findViewById(R.id.profileMenuBtnPhoto).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, location -> {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    binding.toolbar.setSubtitle(addresses.get(0).getLocality());
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
