package com.prasunpersonal.gogroceries_customer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prasunpersonal.gogroceries_customer.Models.ModelCartItem;
import com.prasunpersonal.gogroceries_customer.Models.ModelProduct;
import com.prasunpersonal.gogroceries_customer.databinding.RowOrderDetailsBinding;

import java.util.ArrayList;
import java.util.Locale;

public class AdapterOrderDetails extends RecyclerView.Adapter<AdapterOrderDetails.HolderOrderDetails> {

    private final Context context;
    private final ArrayList<ModelCartItem> items;

    public AdapterOrderDetails(Context context, ArrayList<ModelCartItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public HolderOrderDetails onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderOrderDetails(RowOrderDetailsBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderDetails holder, int position) {
        ModelCartItem item = items.get(position);
        holder.binding.itemName.setText(item.getProduct().getProductName());
        holder.binding.itemQuantity.setText(String.format(Locale.getDefault(), "%s %s", (item.getProduct().getProductType() == ModelProduct.SINGLE_QUANTITY_PRODUCT) ? (int) item.getQuantity() : item.getQuantity(), item.getUnit()));
        holder.binding.itemTotalPrice.setText(String.format(Locale.getDefault(), "%.02f", (item.getProduct().getOriginalPrice() - (item.getProduct().getOriginalPrice() * item.getProduct().getDiscountPercentage() / 100)) * item.getQuantity() * item.getProduct().getUnitMap().get(items.get(position).getUnit())));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HolderOrderDetails extends RecyclerView.ViewHolder{
        RowOrderDetailsBinding binding;

        public HolderOrderDetails(@NonNull RowOrderDetailsBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}



