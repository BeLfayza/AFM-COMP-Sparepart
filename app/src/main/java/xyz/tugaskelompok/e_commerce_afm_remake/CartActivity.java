package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private TextView textViewTotal;
    private Button buttonCheckout;
    private DatabaseHelper databaseHelper;
    private SharedPreferences cartPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        databaseHelper = new DatabaseHelper(this);
        cartPrefs = getSharedPreferences("cart", MODE_PRIVATE);

        initializeViews();
        loadCartItems();
        setupRecyclerView();
        setupCheckout();
    }

    private void initializeViews() {
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        textViewTotal = findViewById(R.id.textViewTotal);
        buttonCheckout = findViewById(R.id.buttonCheckout);
    }

    private void loadCartItems() {
        cartItems = new ArrayList<>();
        Map<String, ?> allEntries = cartPrefs.getAll();
        
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("product_")) {
                String productId = entry.getKey().replace("product_", "");
                int quantity = (Integer) entry.getValue();
                
                if (quantity > 0) {
                    Product product = databaseHelper.getProductById(Integer.parseInt(productId));
                    if (product != null) {
                        cartItems.add(new CartItem(product, quantity));
                    }
                }
            }
        }
        
        updateTotal();
    }

    private void setupRecyclerView() {
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged() {
                updateTotal();
            }

            @Override
            public void onItemRemoved(int position) {
                cartItems.remove(position);
                cartAdapter.notifyItemRemoved(position);
                updateTotal();
            }
        });
        cartAdapter.setCartPrefs(cartPrefs);
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        textViewTotal.setText("Total: " + format.format(total));
    }

    private void setupCheckout() {
        buttonCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartItems.isEmpty()) {
                    Toast.makeText(CartActivity.this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
        if (cartAdapter != null) {
            cartAdapter.notifyDataSetChanged();
        }
    }
}

