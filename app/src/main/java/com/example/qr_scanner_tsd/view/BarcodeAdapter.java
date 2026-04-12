package com.example.qr_scanner_tsd.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qr_scanner_tsd.R;
import com.example.qr_scanner_tsd.model.Barcode;

import java.util.ArrayList;
import java.util.List;

public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.ViewHolder> {

    private final List<Barcode> barcodes = new ArrayList<>();

    public void add(Barcode barcode) {
        barcodes.add(barcode);
        notifyItemInserted(barcodes.size() - 1);
    }

    public void clear() {
        barcodes.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_barcode, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvValue.setText(barcodes.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return barcodes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvValue;

        ViewHolder(View itemView) {
            super(itemView);
            tvValue = itemView.findViewById(R.id.tvBarcodeValue);
        }
    }
}