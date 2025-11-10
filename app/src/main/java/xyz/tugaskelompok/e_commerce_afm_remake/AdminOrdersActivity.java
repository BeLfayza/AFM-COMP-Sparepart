package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private TextView textViewEmpty;
    private LinearLayout layoutProduk, layoutPesanan, layoutProfil;
    private DatabaseHelper databaseHelper;
    private AdminOrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_orders);
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
        recyclerViewOrders = findViewById(R.id.recyclerViewAdminOrders);
        textViewEmpty = findViewById(R.id.textViewEmptyOrders);
        layoutProduk = findViewById(R.id.layoutProduk);
        layoutPesanan = findViewById(R.id.layoutPesanan);
        layoutProfil = findViewById(R.id.layoutProfil);
    }

    private void setupRecyclerView() {
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new AdminOrderAdapter(null, new AdminOrderAdapter.AdminOrderListener() {
            @Override
            public void onViewDetail(Order order) {
                Intent intent = new Intent(AdminOrdersActivity.this, OrderDetailActivity.class);
                intent.putExtra("orderId", order.getId());
                startActivity(intent);
            }

            @Override
            public void onCancelOrder(Order order) {
                confirmCancelOrder(order);
            }
        });
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        List<Order> orders = databaseHelper.getAllOrders();
        orderAdapter.updateOrders(orders);
        if (orders.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
        }
    }

    private void confirmCancelOrder(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Batalkan Pesanan")
                .setMessage("Yakin ingin membatalkan pesanan #" + order.getId() + " ?")
                .setPositiveButton("Batalkan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseHelper.addOrderStatusUpdate(order.getId(), "Pesanan dibatalkan oleh admin");
                        Toast.makeText(AdminOrdersActivity.this, "Pesanan dibatalkan", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void setupFooterNavigation() {
        updateFooterSelection(layoutPesanan, true);

        layoutProduk.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOrdersActivity.this, AdminMainActivity.class);
            startActivity(intent);
            finish();
        });

        layoutPesanan.setOnClickListener(v -> updateFooterSelection(layoutPesanan, true));

        layoutProfil.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOrdersActivity.this, AdminProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateFooterSelection(LinearLayout selectedLayout, boolean isSelected) {
        updateFooterItem(layoutProduk, false);
        updateFooterItem(layoutPesanan, false);
        updateFooterItem(layoutProfil, false);

        if (isSelected) {
            updateFooterItem(selectedLayout, true);
        }
    }

    private void updateFooterItem(LinearLayout layout, boolean isSelected) {
        if (layout == null) return;
        android.widget.ImageView icon = (android.widget.ImageView) layout.getChildAt(0);
        TextView text = (TextView) layout.getChildAt(1);

        int color = ContextCompat.getColor(this, isSelected ? R.color.primary_blue : R.color.text_secondary);
        icon.setColorFilter(color);
        text.setTextColor(color);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
        updateFooterSelection(layoutPesanan, true);
    }
}

