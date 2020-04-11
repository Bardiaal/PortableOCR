package com.bardia.pocr.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bardia.pocr.R;
import com.bardia.pocr.model.TextObjectDecoded;

import java.util.ArrayList;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>{

    ArrayList<TextObjectDecoded> objectDecodedArrayList;
    HistoryRecyclerViewAdapter.OnItemClickListener itemClickListener, showDetail;
    public interface OnItemClickListener {
        void onItemClick(TextObjectDecoded objectDecoded, int adapterPosition);
    }

    public HistoryRecyclerViewAdapter(ArrayList<TextObjectDecoded> objectDecodedArrayList,
                                      HistoryRecyclerViewAdapter.OnItemClickListener listener,
                                      HistoryRecyclerViewAdapter.OnItemClickListener showDetail) {
        this.objectDecodedArrayList = objectDecodedArrayList;
        this.itemClickListener = listener;
        this.showDetail = showDetail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.history_item_layout, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if(objectDecodedArrayList.get(position).getText().length() < 50) {
            holder.tvText.setText(objectDecodedArrayList.get(position).getText());
        } else {
            holder.tvText.setText(objectDecodedArrayList.get(position).getText().substring(0, 50).concat("[...]"));
        }
        holder.tvDate.setText(objectDecodedArrayList.get(position).getDate().substring(0, objectDecodedArrayList.get(position).getDate().length() - 3));
        holder.imageView.setImageBitmap(objectDecodedArrayList.get(position).getImage());
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onItemClick(objectDecodedArrayList.get(position), position);
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDetail.onItemClick(objectDecodedArrayList.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return objectDecodedArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout layout;
        TextView tvText, tvDate, tvEmpty;
        ImageView imageView, emptyImage;
        ImageButton button;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.historyItemLayout);
            tvText = itemView.findViewById(R.id.historyText);
            tvDate = itemView.findViewById(R.id.historyDate);
            imageView = itemView.findViewById(R.id.historyImage);
            button = itemView.findViewById(R.id.deleteFromHistory);
        }
    }
}
