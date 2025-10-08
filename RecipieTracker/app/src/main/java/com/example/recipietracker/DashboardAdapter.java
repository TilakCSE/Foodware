package com.example.recipietracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    public interface OnDashboardClickListener {
        void onDashboardItemClick(String label);
    }

    private final List<DashboardItem> items;
    private final OnDashboardClickListener listener;

    public DashboardAdapter(List<DashboardItem> items, OnDashboardClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardItem item = items.get(position);
        holder.txtLabel.setText(item.getLabel());
        holder.imgIcon.setImageResource(item.getIconResId());
        holder.itemView.setOnClickListener(v -> listener.onDashboardItemClick(item.getLabel()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtLabel;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtLabel = itemView.findViewById(R.id.txtLabel);
        }
    }
}







