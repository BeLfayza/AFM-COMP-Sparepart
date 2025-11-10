package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private LinearLayout layoutBelanja, layoutPesanan, layoutProfil;
    private DatabaseHelper databaseHelper;
    private TextView textViewEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupRecyclerView();
        loadOrders();
        setupFooterNavigation();
    }

    private void initializeViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        layoutBelanja = findViewById(R.id.layoutBelanja);
        layoutPesanan = findViewById(R.id.layoutPesanan);
        layoutProfil = findViewById(R.id.layoutProfil);
    }

    private void setupRecyclerView() {
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(null);
        orderAdapter.setOnOrderClickListener(new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                Intent intent = new Intent(OrdersActivity.this, OrderDetailActivity.class);
                intent.putExtra("orderId", order.getId());
                startActivity(intent);
            }
        });
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        
        List<Order> orders = databaseHelper.getOrdersByUserEmail(email);
        
        if (orders.isEmpty()) {
            textViewEmpty.setVisibility(TextView.VISIBLE);
            recyclerViewOrders.setVisibility(RecyclerView.GONE);
        } else {
            textViewEmpty.setVisibility(TextView.GONE);
            recyclerViewOrders.setVisibility(RecyclerView.VISIBLE);
            orderAdapter = new OrderAdapter(orders);
            orderAdapter.setOnOrderClickListener(new OrderAdapter.OnOrderClickListener() {
                @Override
                public void onOrderClick(Order order) {
                    Intent intent = new Intent(OrdersActivity.this, OrderDetailActivity.class);
                    intent.putExtra("orderId", order.getId());
                    startActivity(intent);
                }
            });
            recyclerViewOrders.setAdapter(orderAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void setupFooterNavigation() {
        updateFooterSelection(layoutPesanan, true);
        
        layoutBelanja.setOnClickListener(v -> {
            Intent intent = new Intent(OrdersActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        layoutPesanan.setOnClickListener(v -> {
            // Already on orders page
            updateFooterSelection(layoutPesanan, true);
        });

        layoutProfil.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(OrdersActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateFooterSelection(LinearLayout selectedLayout, boolean isSelected) {
        updateFooterItem(layoutBelanja, false);
        updateFooterItem(layoutPesanan, false);
        updateFooterItem(layoutProfil, false);
        
        if (isSelected) {
            updateFooterItem(selectedLayout, true);
        }
    }

    private void updateFooterItem(LinearLayout layout, boolean isSelected) {
        android.widget.ImageView icon = (android.widget.ImageView) layout.getChildAt(0);
        TextView text = (TextView) layout.getChildAt(1);
        
        int color = isSelected 
            ? ContextCompat.getColor(this, R.color.primary_blue)
            : ContextCompat.getColor(this, R.color.text_secondary);
        icon.setColorFilter(color);
        text.setTextColor(color);
    }
}

