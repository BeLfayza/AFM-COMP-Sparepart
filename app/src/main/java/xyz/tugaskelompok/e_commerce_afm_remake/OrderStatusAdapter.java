package xyz.tugaskelompok.e_commerce_afm_remake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderStatusAdapter extends RecyclerView.Adapter<OrderStatusAdapter.StatusViewHolder> {

    public interface OnStatusActionListener {
        void onEditStatus(OrderStatusUpdate statusUpdate);
        void onDeleteStatus(OrderStatusUpdate statusUpdate);
    }

    private List<OrderStatusUpdate> statuses;
    private final boolean isAdmin;
    private final OnStatusActionListener listener;

    public OrderStatusAdapter(List<OrderStatusUpdate> statuses, boolean isAdmin, OnStatusActionListener listener) {
        this.statuses = statuses != null ? statuses : new ArrayList<>();
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public void updateStatuses(List<OrderStatusUpdate> newStatuses) {
        this.statuses = newStatuses != null ? newStatuses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        OrderStatusUpdate statusUpdate = statuses.get(position);
        holder.bind(statusUpdate);
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    class StatusViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewStatusText;
        private final TextView textViewStatusTimestamp;
        private final LinearLayout layoutStatusActions;
        private final Button buttonEdit;
        private final Button buttonDelete;

        StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStatusText = itemView.findViewById(R.id.textViewStatusText);
            textViewStatusTimestamp = itemView.findViewById(R.id.textViewStatusTimestamp);
            layoutStatusActions = itemView.findViewById(R.id.layoutStatusActions);
            buttonEdit = itemView.findViewById(R.id.buttonEditStatus);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteStatus);
        }

        void bind(OrderStatusUpdate statusUpdate) {
            textViewStatusText.setText(statusUpdate.getStatusText());
            try {
                long timestamp = Long.parseLong(statusUpdate.getTimestamp());
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                textViewStatusTimestamp.setText(sdf.format(new Date(timestamp)));
            } catch (Exception e) {
                textViewStatusTimestamp.setText(statusUpdate.getTimestamp());
            }

            if (isAdmin) {
                layoutStatusActions.setVisibility(View.VISIBLE);
                buttonEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditStatus(statusUpdate);
                    }
                });
                buttonDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteStatus(statusUpdate);
                    }
                });
            } else {
                layoutStatusActions.setVisibility(View.GONE);
            }
        }
    }
}


