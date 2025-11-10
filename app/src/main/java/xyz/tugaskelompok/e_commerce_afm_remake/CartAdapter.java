package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private OnCartItemChangeListener listener;
    private SharedPreferences cartPrefs;

    public interface OnCartItemChangeListener {
        void onQuantityChanged();
        void onItemRemoved(int position);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void setCartPrefs(SharedPreferences prefs) {
        this.cartPrefs = prefs;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewProductName;
        private TextView textViewProductPrice;
        private TextView textViewQuantity;
        private Button buttonDecrease;
        private Button buttonIncrease;
        private Button buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }

        public void bind(CartItem cartItem) {
            Product product = cartItem.getProduct();
            textViewProductName.setText(product.getName());
            
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            double totalPrice = product.getPrice() * cartItem.getQuantity();
            textViewProductPrice.setText(format.format(totalPrice));
            
            textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));

            buttonDecrease.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CartItem item = cartItems.get(position);
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        updateCartPrefs(item);
                        notifyItemChanged(position);
                        if (listener != null) listener.onQuantityChanged();
                    } else {
                        // Remove item
                        cartItems.remove(position);
                        removeFromCartPrefs(item);
                        notifyItemRemoved(position);
                        if (listener != null) listener.onItemRemoved(position);
                    }
                }
            });

            buttonIncrease.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CartItem item = cartItems.get(position);
                    item.setQuantity(item.getQuantity() + 1);
                    updateCartPrefs(item);
                    notifyItemChanged(position);
                    if (listener != null) listener.onQuantityChanged();
                }
            });

            buttonRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CartItem item = cartItems.get(position);
                    cartItems.remove(position);
                    removeFromCartPrefs(item);
                    notifyItemRemoved(position);
                    if (listener != null) listener.onItemRemoved(position);
                }
            });
        }

        private void updateCartPrefs(CartItem item) {
            if (cartPrefs != null) {
                SharedPreferences.Editor editor = cartPrefs.edit();
                editor.putInt("product_" + item.getProduct().getId(), item.getQuantity());
                editor.apply();
            }
        }

        private void removeFromCartPrefs(CartItem item) {
            if (cartPrefs != null) {
                SharedPreferences.Editor editor = cartPrefs.edit();
                editor.remove("product_" + item.getProduct().getId());
                editor.apply();
            }
        }
    }
}




