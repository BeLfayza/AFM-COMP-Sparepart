package xyz.tugaskelompok.e_commerce_afm_remake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewOrderId;
        private TextView textViewOrderDate;
        private TextView textViewOrderTotal;
        private TextView textViewOrderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            textViewOrderTotal = itemView.findViewById(R.id.textViewOrderTotal);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOrderClick(orders.get(position));
                }
            });
        }

        public void bind(Order order) {
            textViewOrderId.setText("Pesanan #" + order.getId());
            
            try {
                long timestamp = Long.parseLong(order.getOrderDate());
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                textViewOrderDate.setText(sdf.format(new Date(timestamp)));
            } catch (Exception e) {
                textViewOrderDate.setText(order.getOrderDate());
            }
            
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            double grandTotal = order.getTotal() + order.getShipping();
            textViewOrderTotal.setText(format.format(grandTotal));
            
            textViewOrderStatus.setText(order.getStatus());
        }
    }
}


