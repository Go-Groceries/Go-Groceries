package com.prasunpersonal.gogroceries_customer.Activities;

import static com.prasunpersonal.gogroceries_customer.Activities.ShopDetailsActivity.SELECTED_ITEMS;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prasunpersonal.gogroceries_customer.Adapters.AdapterSelectedProduct;
import com.prasunpersonal.gogroceries_customer.Models.ModelCartItem;
import com.prasunpersonal.gogroceries_customer.Models.ModelShop;
import com.prasunpersonal.gogroceries_customer.R;
import com.prasunpersonal.gogroceries_customer.databinding.ActivityCartBinding;

import org.parceler.Parcels;

import java.util.Locale;

public class CartActivity extends AppCompatActivity {
    ActivityCartBinding binding;
    ModelShop shop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.cartToolbar);

        binding.cartToolbar.setNavigationOnClickListener(v -> finish());

        shop = Parcels.unwrap(getIntent().getParcelableExtra("SHOP"));

        binding.cartItemsRv.setLayoutManager(new LinearLayoutManager(this));
        binding.cartItemsRv.setAdapter(new AdapterSelectedProduct(this, SELECTED_ITEMS, (item, position) -> {
            SELECTED_ITEMS.remove(position);
            if (binding.cartItemsRv.getAdapter() != null) binding.cartItemsRv.getAdapter().notifyItemRemoved(position);
            calculatePricing();
            invalidateOptionsMenu();
        }));

        calculatePricing();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        menu.findItem(R.id.done).setVisible(!SELECTED_ITEMS.isEmpty());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.done) {
            startActivity(new Intent(this, CustomerDetailsActivity.class).putExtra("SHOP", Parcels.wrap(shop)));
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculatePricing() {
        if (!SELECTED_ITEMS.isEmpty()) {
            double subTotal = 0;
            for (ModelCartItem item : SELECTED_ITEMS) {
                subTotal += (item.getProduct().getOriginalPrice() - (item.getProduct().getOriginalPrice() * item.getProduct().getDiscountPercentage() / 100)) * item.getQuantity() * item.getProduct().getUnitMap().get(item.getUnit());
            }
            binding.subTotal.setText(String.format(Locale.getDefault(), "%.02f", subTotal));
            binding.deliveryCharge.setText(String.format(Locale.getDefault(), "%.02f", shop.getDeliveryCharge()));
            binding.grandTotal.setText(String.format(Locale.getDefault(), "%.02f", (subTotal + shop.getDeliveryCharge())));
        }
    }
}