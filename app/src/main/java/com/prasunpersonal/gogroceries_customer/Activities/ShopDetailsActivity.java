package com.prasunpersonal.gogroceries_customer.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prasunpersonal.gogroceries_customer.Adapters.AdapterProduct;
import com.prasunpersonal.gogroceries_customer.Models.ModelCartItem;
import com.prasunpersonal.gogroceries_customer.Models.ModelProduct;
import com.prasunpersonal.gogroceries_customer.Models.ModelShop;
import com.prasunpersonal.gogroceries_customer.R;
import com.prasunpersonal.gogroceries_customer.databinding.ActivityShopDetailsBinding;
import com.prasunpersonal.gogroceries_customer.databinding.ItemSelectionDialogBinding;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ShopDetailsActivity extends AppCompatActivity {
    ActivityShopDetailsBinding binding;
    ModelShop shop;

    private ArrayList<ModelProduct> productList;
    public static ArrayList<ModelCartItem> SELECTED_ITEMS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        shop = Parcels.unwrap(getIntent().getParcelableExtra("SHOP"));

        binding.shopDetailsToolbar.setTitle(shop.getShopName());
        binding.shopDetailsToolbar.setSubtitle(String.format(Locale.getDefault(), "%s, %s, %s, %s", shop.getAddress(), shop.getCity(), shop.getState(), shop.getCountry()));
        setSupportActionBar(binding.shopDetailsToolbar);
        binding.shopDetailsToolbar.setNavigationOnClickListener(v -> finish());

        productList = new ArrayList<>();
        SELECTED_ITEMS = new ArrayList<>();

        binding.productsRv.setLayoutManager(new LinearLayoutManager(this));
        binding.productsRv.setAdapter(new AdapterProduct(this, productList, SELECTED_ITEMS, this::showSelectionDialog));

        binding.searchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((AdapterProduct) Objects.requireNonNull(binding.productsRv.getAdapter())).getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shop_details_menu, menu);

        menu.findItem(R.id.cart).getActionView().findViewById(R.id.cartCountContainer).setVisibility((SELECTED_ITEMS.size() > 0) ? View.VISIBLE : View.GONE);
        ((TextView) menu.findItem(R.id.cart).getActionView().findViewById(R.id.cartItemCount)).setText((SELECTED_ITEMS.size() > 99) ? "99+" : String.valueOf(SELECTED_ITEMS.size()));
        menu.findItem(R.id.cart).getActionView().findViewById(R.id.cartBtn).setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class).putExtra("SHOP", Parcels.wrap(shop))));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.map) {
            openMap();
        } else if (itemId == R.id.call) {
            dialPhone();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?daddr=" + shop.getLatitude() + "," + shop.getLongitude();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(address)));
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shop.getOwnerPhone()))));
    }

    private void loadProducts() {
        FirebaseFirestore.getInstance().collection("Shops").document(shop.getUid()).collection("Products").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null) {
                productList.clear();
                for (DocumentSnapshot dc : value) {
                    productList.add(dc.toObject(ModelProduct.class));
                }
                ((AdapterProduct) Objects.requireNonNull(binding.productsRv.getAdapter())).notifyChange();
            }
        });
    }

    private void showSelectionDialog(ModelProduct product, int position) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        ItemSelectionDialogBinding dialogBinding = ItemSelectionDialogBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setOnDismissListener(dialog1 -> ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0));

        dialogBinding.productName.setText(product.getProductName());
        Glide.with(this).load(product.getProductImg()).placeholder(R.drawable.ic_image).into(dialogBinding.productImg);
        if (product.getDiscountPercentage() > 0) {
            dialogBinding.mrpContainer.setVisibility(View.VISIBLE);
            dialogBinding.mrpPrice.setText(String.format(Locale.getDefault(), "%.02f", product.getOriginalPrice()));
            dialogBinding.discountPercentage.setText(String.format(Locale.getDefault(), "(%d%% off)", (int) product.getDiscountPercentage()));
        } else {
            dialogBinding.mrpContainer.setVisibility(View.GONE);
        }
        dialogBinding.pricePerItem.setText(String.format(Locale.getDefault(), "%.02f", (product.getOriginalPrice() - (product.getOriginalPrice() * product.getDiscountPercentage() / 100))));
        dialogBinding.itemUnit.setText(String.format(Locale.getDefault(), "/%s", new ArrayList<>(product.getUnitMap().keySet()).get(0)));
        if (product.getProductDescription().trim().isEmpty()) {
            dialogBinding.productDescription.setVisibility(View.GONE);
        } else {
            dialogBinding.productDescription.setText(product.getProductDescription());
            dialogBinding.productDescription.setVisibility(View.VISIBLE);
        }

        if (product.getProductType() == ModelProduct.SINGLE_QUANTITY_PRODUCT) {
            dialogBinding.quantity.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        } else {
            dialogBinding.quantity.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(product.getUnitMap().keySet()));
        dialogBinding.unit.setAdapter(unitAdapter);

        dialogBinding.quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    dialogBinding.priceTotal.setText(String.format(Locale.getDefault(), "%.2f", (product.getOriginalPrice() - (product.getOriginalPrice() * product.getDiscountPercentage() / 100)) * Double.parseDouble(s.toString().trim()) * product.getUnitMap().get(dialogBinding.unit.getSelectedItem().toString())));
                    dialogBinding.selectBtn.setEnabled(true);
                }catch (Exception e) {
                    if (!dialogBinding.quantity.getText().toString().trim().isEmpty()) {
                        dialogBinding.quantity.setError("Please enter a valid quantity!");
                    }
                    dialogBinding.priceTotal.setText(null);
                    dialogBinding.selectBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialogBinding.unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!dialogBinding.quantity.getText().toString().trim().isEmpty()) {
                    dialogBinding.priceTotal.setText(String.format(Locale.getDefault(), "%.2f", (product.getOriginalPrice() - (product.getOriginalPrice() * product.getDiscountPercentage() / 100)) * Double.parseDouble(dialogBinding.quantity.getText().toString().trim()) * product.getUnitMap().get(dialogBinding.unit.getSelectedItem().toString())));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialogBinding.selectBtn.setOnClickListener(v -> {
            SELECTED_ITEMS.remove(new ModelCartItem(product));
            if (Double.parseDouble(dialogBinding.quantity.getText().toString().trim()) > 0) {
                SELECTED_ITEMS.add(new ModelCartItem(product, Double.parseDouble(dialogBinding.quantity.getText().toString().trim()), dialogBinding.unit.getSelectedItem().toString()));
            }
            dialog.dismiss();
            invalidateOptionsMenu();
        });

        if (SELECTED_ITEMS.contains(new ModelCartItem(product))) {
            ModelCartItem item = SELECTED_ITEMS.get(SELECTED_ITEMS.indexOf(new ModelCartItem(product)));
            dialogBinding.quantity.setText((item.getProduct().getProductType() == ModelProduct.SINGLE_QUANTITY_PRODUCT) ? String.valueOf ((int) item.getQuantity()) : String.valueOf(item.getQuantity()));
            dialogBinding.unit.setSelection(unitAdapter.getPosition(item.getUnit()));
        }

        dialogBinding.quantity.requestFocus();
        dialogBinding.quantity.setSelection(dialogBinding.quantity.getText().length());
        dialog.show();
    }
}