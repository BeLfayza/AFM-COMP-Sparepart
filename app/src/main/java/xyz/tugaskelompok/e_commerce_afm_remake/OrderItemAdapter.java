package xyz.tugaskelompok.e_commerce_afm_remake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private List<CartItem> items;

    public OrderItemAdapter(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewProductName;
        private TextView textViewQuantity;
        private TextView textViewPrice;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }

        public void bind(CartItem item) {
            textViewProductName.setText(item.getProduct().getName());
            textViewQuantity.setText("x" + item.getQuantity());
            
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            double totalPrice = item.getProduct().getPrice() * item.getQuantity();
            textViewPrice.setText(format.format(totalPrice));
        }
    }
}




