package com.prasunpersonal.gogroceries_customer.Activities;

import static com.prasunpersonal.gogroceries_customer.Models.ModelOrder.MONEY_RECEIVED;
import static com.prasunpersonal.gogroceries_customer.Models.ModelOrder.OUT_FOR_DELIVERY;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prasunpersonal.gogroceries_customer.Adapters.AdapterOrderDetails;
import com.prasunpersonal.gogroceries_customer.Models.ModelCartItem;
import com.prasunpersonal.gogroceries_customer.Models.ModelDeliveryGuy;
import com.prasunpersonal.gogroceries_customer.Models.ModelOrder;
import com.prasunpersonal.gogroceries_customer.Models.ModelShop;
import com.prasunpersonal.gogroceries_customer.R;
import com.prasunpersonal.gogroceries_customer.databinding.ActivityCustomerOrderDetailsBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CustomerOrderDetailsActivity extends AppCompatActivity {
    ActivityCustomerOrderDetailsBinding binding;
    String orderID;
    private ModelOrder order;
    private ModelDeliveryGuy deliveryGuy;
    private ModelShop shop;

    double subTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.orderDetailsToolbar);

        orderID = getIntent().getStringExtra("ORDER_ID");

        binding.orderDetailsToolbar.setNavigationOnClickListener(v -> finish());

        binding.cancelOrder.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Cancel Order?")
                    .setMessage("Are you sure about canceling this order?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog1, which) -> {
                        if (order.getOrderStatus() < OUT_FOR_DELIVERY) {
                            FirebaseFirestore.getInstance().collection("Orders").document(order.getOrderId()).delete();
                            finish();
                        } else {
                            Toast.makeText(this, "Sorry, this order is already out for delivery!", Toast.LENGTH_SHORT).show();
                        }
                        dialog1.dismiss();
                    }).setNegativeButton("No", (dialog1, which) -> dialog1.dismiss()).create();

            dialog.show();
        });

        binding.callDeliveryGuy.setOnClickListener(v -> dialPhone());

        FirebaseFirestore.getInstance().collection("Orders").document(orderID).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null && value.exists()) {
                order = value.toObject(ModelOrder.class);
                assert order != null;

                FirebaseFirestore.getInstance().collection("Shops").document(order.getShopId()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() &&  task.getResult().exists()) {
                        shop = task.getResult().toObject(ModelShop.class);
                        binding.orderTitle.setText(String.format(Locale.getDefault(), "%d product(s) ordered from %s", order.getCartItems().size(), shop.getShopName()));
                    } else {
                        Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


                MaterialCardView[] points = {binding.orderPlacedPoint, binding.orderPackedPoint, binding.outForDeliveryPoint, binding.moneyReceivedPoint, binding.deliveredPoint};
                View[] lines = {binding.connectionLine0, binding.connectionLine1, binding.connectionLine2, binding.connectionLine3};
                for (int i=0; i < points.length; i++){
                    if (i < order.getOrderStatus()){
                        points[i].setCardBackgroundColor(Color.GREEN);
                        if (i < lines.length) lines[i].setBackgroundColor(Color.GREEN);
                    } else if (i == order.getOrderStatus()) {
                        points[i].setCardBackgroundColor(Color.GREEN);
                        if (i < lines.length) lines[i].setBackgroundColor(Color.LTGRAY);
                    } else {
                        points[i].setCardBackgroundColor(Color.LTGRAY);
                        if (i < lines.length) lines[i].setBackgroundColor(Color.LTGRAY);
                    }
                }

                if (order.getOrderStatus() < OUT_FOR_DELIVERY) {
                    binding.cancelOrder.setVisibility(View.VISIBLE);
                } else {
                    binding.cancelOrder.setVisibility(View.GONE);
                }
                if (order.getDeliveryGuyId() == null) {
                    binding.deliveryGuyArea.setVisibility(View.GONE);
                } else {
                    binding.deliveryGuyArea.setVisibility(View.VISIBLE);
                    FirebaseFirestore.getInstance().collection("DeliveryGuys").document(order.getDeliveryGuyId()).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() &&  task.getResult().exists()) {
                            deliveryGuy = task.getResult().toObject(ModelDeliveryGuy.class);
                            assert deliveryGuy != null;
                            Glide.with(this).load(deliveryGuy.getDp()).placeholder(R.drawable.ic_person).into(binding.deliveryGuyDp);
                            binding.deliveryGuyName.setText(deliveryGuy.getName());
                            binding.deliveryGuyPhone.setText(deliveryGuy.getPhone());
                        } else {
                            Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (order.getOrderStatus() == MONEY_RECEIVED) {
                    binding.secretCodeArea.setVisibility(View.VISIBLE);
                    binding.secretCode.setText(order.getSecretCode());
                } else {
                    binding.secretCodeArea.setVisibility(View.GONE);
                }

                binding.orderTime.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date(order.getOrderTime())));
                binding.orderID.setText(order.getOrderId());
                binding.orderedItems.setLayoutManager(new LinearLayoutManager(this));
                binding.orderedItems.setAdapter(new AdapterOrderDetails(this, order.getCartItems()));
                for (ModelCartItem item : order.getCartItems()) {
                    subTotal += (item.getProduct().getOriginalPrice() - (item.getProduct().getOriginalPrice() * item.getProduct().getDiscountPercentage() / 100)) * item.getQuantity() * item.getProduct().getUnitMap().get(item.getUnit());
                }
                binding.orderDetailsSubTotal.setText(String.format(Locale.getDefault(), "%.02f", subTotal));
                binding.orderDetailsDeliveryCharge.setText(String.format(Locale.getDefault(), "%.02f", order.getDeliveryCharge()));
                binding.orderDetailsGrandTotal.setText(String.format(Locale.getDefault(), "%.02f", subTotal + order.getDeliveryCharge()));

                invalidateOptionsMenu();
            } else {
                Toast.makeText(this, "Sorry, the order may be canceled or not found!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invoice, menu);
        menu.findItem(R.id.invoice).setVisible(order != null && order.getInvoice() != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.invoice) {
            startActivity(new Intent(this, PDFViewActivity.class).putExtra("PDF_URL", order.getInvoice()).putExtra("PDF_NAME", String.format(Locale.getDefault(), "%d.pdf", order.getOrderTime())));
        }
        return super.onOptionsItemSelected(item);
    }

    private void dialPhone() {
        if (!binding.deliveryGuyPhone.getText().toString().trim().isEmpty()) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(binding.deliveryGuyPhone.getText().toString().trim()))));
        }
    }
}