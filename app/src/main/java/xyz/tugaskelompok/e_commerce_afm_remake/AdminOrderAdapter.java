package xyz.tugaskelompok.e_commerce_afm_remake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {

    public interface AdminOrderListener {
        void onViewDetail(Order order);
        void onCancelOrder(Order order);
    }

    private List<Order> orders;
    private final AdminOrderListener listener;

    public AdminOrderAdapter(List<Order> orders, AdminOrderListener listener) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.listener = listener;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders != null ? newOrders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewOrderId;
        private final TextView textViewUser;
        private final TextView textViewDate;
        private final TextView textViewTotal;
        private final TextView textViewStatus;
        private final Button buttonDetail;
        private final Button buttonCancel;

        AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewAdminOrderId);
            textViewUser = itemView.findViewById(R.id.textViewAdminOrderUser);
            textViewDate = itemView.findViewById(R.id.textViewAdminOrderDate);
            textViewTotal = itemView.findViewById(R.id.textViewAdminOrderTotal);
            textViewStatus = itemView.findViewById(R.id.textViewAdminOrderStatus);
            buttonDetail = itemView.findViewById(R.id.buttonDetailOrder);
            buttonCancel = itemView.findViewById(R.id.buttonCancelOrder);
        }

        void bind(Order order) {
            textViewOrderId.setText("Pesanan #" + order.getId());
            textViewUser.setText("User: " + order.getUserName());
            try {
                long timestamp = Long.parseLong(order.getOrderDate());
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                textViewDate.setText(sdf.format(new Date(timestamp)));
            } catch (Exception e) {
                textViewDate.setText(order.getOrderDate());
            }
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            double grandTotal = order.getTotal() + order.getShipping();
            textViewTotal.setText(format.format(grandTotal));
            textViewStatus.setText(order.getStatus());

            buttonDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetail(order);
                }
            });

            buttonCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelOrder(order);
                }
            });
        }
    }
}


