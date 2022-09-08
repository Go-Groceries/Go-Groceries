package com.prasunpersonal.gogroceries_customer.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prasunpersonal.gogroceries_customer.Models.ModelCartItem;
import com.prasunpersonal.gogroceries_customer.Models.ModelProduct;
import com.prasunpersonal.gogroceries_customer.R;
import com.prasunpersonal.gogroceries_customer.databinding.RowItemBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.HolderProductCustomer> implements Filterable {

    private final Context context;
    private final setOnClickListener listener;
    private final ArrayList<ModelProduct> products;
    private final ArrayList<ModelProduct> productList;
    private final ArrayList<ModelCartItem> selected;
    private String filterText;

    public AdapterProduct(Context context, ArrayList<ModelProduct> productList, @Nullable ArrayList<ModelCartItem> selected, setOnClickListener listener) {
        this.context = context;
        this.products = productList;
        this.productList = new ArrayList<>(productList);
        this.selected = selected;
        this.listener = listener;
        this.filterText = "";
    }

    @NonNull
    @Override
    public HolderProductCustomer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderProductCustomer(RowItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductCustomer holder, int position) {
        Glide.with(context).load(productList.get(position).getProductImg()).placeholder(R.drawable.ic_image).into(holder.binding.productImg);
        holder.binding.productName.setText(productList.get(position).getProductName());
        if (productList.get(position).getDiscountPercentage() > 0) {
            holder.binding.mrpContainer.setVisibility(View.VISIBLE);
            holder.binding.mrpPrice.setText(String.format(Locale.getDefault(), "%.02f", productList.get(position).getOriginalPrice()));
            holder.binding.discountPercentage.setText(String.format(Locale.getDefault(), "(%d%% off)", (int) productList.get(position).getDiscountPercentage()));
        } else {
            holder.binding.mrpContainer.setVisibility(View.GONE);
        }
        holder.binding.pricePerItem.setText(String.format(Locale.getDefault(), "%.02f", (productList.get(position).getOriginalPrice() - (productList.get(position).getOriginalPrice() * productList.get(position).getDiscountPercentage() / 100))));
        holder.binding.itemUnit.setText(String.format(Locale.getDefault(), "/%s", new ArrayList<>(productList.get(position).getUnitMap().keySet()).get(0)));
        if (productList.get(position).getProductDescription().trim().isEmpty()) {
            holder.binding.productDescription.setVisibility(View.GONE);
        } else {
            holder.binding.productDescription.setText(productList.get(position).getProductDescription());
            holder.binding.productDescription.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> listener.OnClickListener(productList.get(position), position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public final void notifyChange() {
        productList.clear();
        productList.addAll(products.stream().filter(product -> product.getProductName().toLowerCase(Locale.getDefault()).contains(filterText.toLowerCase(Locale.ROOT))).collect(Collectors.toList()));
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                filterText = constraint.toString().trim();
                ArrayList<ModelProduct> filteredPosts = new ArrayList<>();
                if (constraint.toString().trim().isEmpty()) {
                    filteredPosts.addAll(products);
                } else {
                    filteredPosts.addAll(products.stream().filter(product -> product.getProductName().toLowerCase(Locale.getDefault()).contains(filterText.toLowerCase(Locale.ROOT))).collect(Collectors.toList()));
                }
                FilterResults results = new FilterResults();
                results.values = filteredPosts;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                productList.clear();
                productList.addAll((Collection<? extends ModelProduct>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public interface setOnClickListener{
        void OnClickListener(ModelProduct product, int position);
    }

    static class HolderProductCustomer extends RecyclerView.ViewHolder{
        RowItemBinding binding;

        public HolderProductCustomer(@NonNull RowItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}



