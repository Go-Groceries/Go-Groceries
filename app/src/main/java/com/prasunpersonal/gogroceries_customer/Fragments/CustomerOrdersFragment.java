package com.prasunpersonal.gogroceries_customer.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.prasunpersonal.gogroceries_customer.Adapters.AdapterFragment;
import com.prasunpersonal.gogroceries_customer.databinding.FragmentCustomerOrdersBinding;

import java.util.ArrayList;

public class CustomerOrdersFragment extends Fragment {
    FragmentCustomerOrdersBinding binding;
    Context context;

    public CustomerOrdersFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCustomerOrdersBinding.inflate(inflater, container, false);
        context = binding.getRoot().getContext();

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new CustomerPendingOrdersFragment());
        fragments.add(new CustomerDeliveredOrdersFragment());

        binding.customerOrdersViewpager.setAdapter(new AdapterFragment(getChildFragmentManager(), getLifecycle(), fragments));
        binding.customerOrdersViewpager.setOffscreenPageLimit(fragments.size());
        binding.customerOrdersViewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.customerOrdersMainTab.selectTab(binding.customerOrdersMainTab.getTabAt(position));
            }
        });
        binding.customerOrdersMainTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.customerOrdersViewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return binding.getRoot();
    }
}