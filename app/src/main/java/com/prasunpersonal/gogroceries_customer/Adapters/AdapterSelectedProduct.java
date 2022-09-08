package com.prasunpersonal.gogroceries_customer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prasunpersonal.gogroceries_customer.Models.ModelCartItem;
import com.prasunpersonal.gogroceries_customer.Models.ModelProduct;
import com.prasunpersonal.gogroceries_customer.R;
import com.prasunpersonal.gogroceries_customer.databinding.RowCartItemBinding;

import java.util.ArrayList;
import java.util.Locale;

public class AdapterSelectedProduct extends RecyclerView.Adapter<AdapterSelectedProduct.HolderSelectedProduct> {

    private final Context context;
    private final setOnDeleteListener listener;
    private final ArrayList<ModelCartItem> selected;

    public AdapterSelectedProduct(Context context, ArrayList<ModelCartItem> selected, setOnDeleteListener listener) {
        this.context = context;
        this.selected = selected;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HolderSelectedProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderSelectedProduct(RowCartItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderSelectedProduct holder, int position) {
        Glide.with(context).load(selected.get(position).getProduct().getProductImg()).placeholder(R.drawable.ic_image).into(holder.binding.productImg);
        holder.binding.productName.setText(selected.get(position).getProduct().getProductName());
        if (selected.get(position).getProduct().getDiscountPercentage() > 0) {
            holder.binding.mrpContainer.setVisibility(View.VISIBLE);
            holder.binding.mrpPrice.setText(String.format(Locale.getDefault(), "%.02f", selected.get(position).getProduct().getOriginalPrice()));
            holder.binding.discountPercentage.setText(String.format(Locale.getDefault(), "(%d%% off)", (int) selected.get(position).getProduct().getDiscountPercentage()));
        } else {
            holder.binding.mrpContainer.setVisibility(View.GONE);
        }
        holder.binding.pricePerItem.setText(String.format(Locale.getDefault(), "%.02f", (selected.get(position).getProduct().getOriginalPrice() - (selected.get(position).getProduct().getOriginalPrice() * selected.get(position).getProduct().getDiscountPercentage() / 100))));
        holder.binding.itemUnit.setText(String.format(Locale.getDefault(), "/%s", new ArrayList<>(selected.get(position).getProduct().getUnitMap().keySet()).get(0)));

        holder.binding.selectionQuantity.setText(String.format(Locale.getDefault(), "%s %s", (selected.get(position).getProduct().getProductType() == ModelProduct.SINGLE_QUANTITY_PRODUCT) ? (int) selected.get(position).getQuantity() : selected.get(position).getQuantity(), selected.get(position).getUnit()));
        holder.binding.selectionQuantity.setText((selected.get(position).getProduct().getProductType() == ModelProduct.SINGLE_QUANTITY_PRODUCT) ? String.format(Locale.getDefault(), "%d %s", (int) selected.get(position).getQuantity(), selected.get(position).getUnit()) : String.format(Locale.getDefault(), "%f %s", selected.get(position).getQuantity(), selected.get(position).getUnit()));
        holder.binding.selectionTotal.setText(String.format(Locale.getDefault(), "%.02f", (selected.get(position).getProduct().getOriginalPrice() - (selected.get(position).getProduct().getOriginalPrice() * selected.get(position).getProduct().getDiscountPercentage() / 100)) * selected.get(position).getQuantity() * selected.get(position).getProduct().getUnitMap().get(selected.get(position).getUnit())));

        holder.binding.removeFromCart.setOnClickListener(v -> listener.OnDeleteListener(selected.get(position), position));
    }

    @Override
    public int getItemCount() {
        return selected.size();
    }

    public interface setOnDeleteListener {
        void OnDeleteListener(ModelCartItem item, int position);
    }

    static class HolderSelectedProduct extends RecyclerView.ViewHolder{
        RowCartItemBinding binding;

        public HolderSelectedProduct(@NonNull RowCartItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}



