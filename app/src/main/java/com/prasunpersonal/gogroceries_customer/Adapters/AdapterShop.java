package com.prasunpersonal.gogroceries_customer.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prasunpersonal.gogroceries_customer.Models.ModelShop;
import com.prasunpersonal.gogroceries_customer.R;
import com.prasunpersonal.gogroceries_customer.databinding.RowShopBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> implements Filterable {
    private final Context context;
    private final ArrayList<ModelShop> shops;
    private final ArrayList<ModelShop> shopsList;
    private final setOnClickListener listener;
    private String filterText;

    public AdapterShop(Context context, ArrayList<ModelShop> shopsList, setOnClickListener listener) {
        this.context = context;
        this.shops = shopsList;
        this.shopsList = new ArrayList<>(shopsList);
        this.listener = listener;
        this.filterText = "";
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderShop(RowShopBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        Glide.with(context).load(shopsList.get(position).getDp()).placeholder(R.drawable.ic_store).into(holder.binding.shopIv);
        holder.binding.shopNameTv.setText(shopsList.get(position).getShopName());
        holder.binding.addressTv.setText(String.format(Locale.getDefault(), "%s, %s, %s, %s", shopsList.get(position).getAddress(), shopsList.get(position).getCity(), shopsList.get(position).getState(), shopsList.get(position).getCountry()));
        holder.itemView.setOnClickListener(v -> listener.OnClickListener(shopsList.get(position)));
    }

    @Override
    public int getItemCount() {
        return shopsList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                filterText = constraint.toString().trim();
                ArrayList<ModelShop> filteredPosts = new ArrayList<>();
                if (constraint.toString().trim().isEmpty()) {
                    filteredPosts.addAll(shops);
                } else {
                    filteredPosts.addAll(shops.stream().filter(shop -> shop.getShopName().toLowerCase(Locale.getDefault()).contains(filterText.toLowerCase(Locale.ROOT))).collect(Collectors.toList()));
                }
                FilterResults results = new FilterResults();
                results.values = filteredPosts;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                shopsList.clear();
                shopsList.addAll((Collection<? extends ModelShop>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    public final void notifyChange() {
        shopsList.clear();
        shopsList.addAll(shops.stream().filter(shop -> shop.getShopName().toLowerCase(Locale.getDefault()).contains(filterText.toLowerCase(Locale.ROOT))).collect(Collectors.toList()));
        notifyDataSetChanged();
    }

    public interface setOnClickListener{
        void OnClickListener(ModelShop shop);
    }

    static class HolderShop extends RecyclerView.ViewHolder{
        RowShopBinding binding;

        public HolderShop(@NonNull RowShopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
