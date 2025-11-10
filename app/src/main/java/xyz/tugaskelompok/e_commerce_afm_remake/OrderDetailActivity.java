package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView textViewOrderId;
    private TextView textViewOrderDate;
    private TextView textViewUserName;
    private TextView textViewAddress;
    private TextView textViewPhone;
    private TextView textViewSubtotal;
    private TextView textViewShipping;
    private TextView textViewTotal;
    private TextView textViewStatus;
    private RecyclerView recyclerViewItems;
    private RecyclerView recyclerViewStatus;
    private TextView textViewStatusEmpty;
    private View buttonAddStatus;
    private OrderStatusAdapter statusAdapter;
    private boolean isAdmin = false;
    private DatabaseHelper databaseHelper;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_detail);

        databaseHelper = new DatabaseHelper(this);

        int orderId = getIntent().getIntExtra("orderId", -1);
        if (orderId == -1) {
            finish();
            return;
        }

        order = databaseHelper.getOrderById(orderId);
        if (order == null) {
            finish();
            return;
        }

        initializeViews();
        setupStatusList();
        displayOrderDetails();
    }

    private void initializeViews() {
        textViewOrderId = findViewById(R.id.textViewOrderId);
        textViewOrderDate = findViewById(R.id.textViewOrderDate);
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewShipping = findViewById(R.id.textViewShipping);
        textViewTotal = findViewById(R.id.textViewTotal);
        textViewStatus = findViewById(R.id.textViewStatus);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewStatus = findViewById(R.id.recyclerViewStatus);
        textViewStatusEmpty = findViewById(R.id.textViewStatusEmpty);
        buttonAddStatus = findViewById(R.id.buttonAddStatus);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "user");
        isAdmin = "admin".equalsIgnoreCase(role);

        if (isAdmin) {
            buttonAddStatus.setVisibility(View.VISIBLE);
            buttonAddStatus.setOnClickListener(v -> showStatusDialog(null));
        }
    }

    private void displayOrderDetails() {
        textViewOrderId.setText("Pesanan #" + order.getId());
        
        try {
            long timestamp = Long.parseLong(order.getOrderDate());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
            textViewOrderDate.setText(sdf.format(new Date(timestamp)));
        } catch (Exception e) {
            textViewOrderDate.setText(order.getOrderDate());
        }
        
        textViewUserName.setText(order.getUserName());
        textViewAddress.setText(order.getAddress());
        textViewPhone.setText(order.getPhone());
        textViewStatus.setText(order.getStatus());
        
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        textViewSubtotal.setText("Subtotal: " + format.format(order.getTotal()));
        textViewShipping.setText("Ongkir: " + format.format(order.getShipping()));
        textViewTotal.setText("Total: " + format.format(order.getTotal() + order.getShipping()));
        
        // Parse and display items
        List<CartItem> items = parseItems(order.getItemsJson());
        OrderItemAdapter adapter = new OrderItemAdapter(items);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(adapter);

        loadStatusUpdates();
    }

    private void setupStatusList() {
        statusAdapter = new OrderStatusAdapter(null, isAdmin, new OrderStatusAdapter.OnStatusActionListener() {
            @Override
            public void onEditStatus(OrderStatusUpdate statusUpdate) {
                showStatusDialog(statusUpdate);
            }

            @Override
            public void onDeleteStatus(OrderStatusUpdate statusUpdate) {
                confirmDeleteStatus(statusUpdate);
            }
        });
        recyclerViewStatus.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStatus.setAdapter(statusAdapter);
    }

    private void loadStatusUpdates() {
        List<OrderStatusUpdate> updates = databaseHelper.getOrderStatusUpdates(order.getId());
        statusAdapter.updateStatuses(updates);

        if (updates.isEmpty()) {
            textViewStatusEmpty.setVisibility(View.VISIBLE);
        } else {
            textViewStatusEmpty.setVisibility(View.GONE);
            OrderStatusUpdate latest = updates.get(updates.size() - 1);
            textViewStatus.setText(latest.getStatusText());
            order.setStatus(latest.getStatusText());
        }
    }

    private void showStatusDialog(OrderStatusUpdate statusUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(statusUpdate == null ? "Tambah Status Pengiriman" : "Edit Status Pengiriman");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_status, null);
        EditText editTextStatus = dialogView.findViewById(R.id.editTextStatusDescription);
        if (statusUpdate != null) {
            editTextStatus.setText(statusUpdate.getStatusText());
        }
        builder.setView(dialogView);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String statusText = editTextStatus.getText().toString().trim();
            if (TextUtils.isEmpty(statusText)) {
                Toast.makeText(this, "Status tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success;
            if (statusUpdate == null) {
                success = databaseHelper.addOrderStatusUpdate(order.getId(), statusText) != -1;
            } else {
                success = databaseHelper.updateOrderStatusUpdate(statusUpdate.getId(), statusText);
            }

            if (success) {
                order = databaseHelper.getOrderById(order.getId());
                loadStatusUpdates();
                Toast.makeText(this, "Status tersimpan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gagal menyimpan status", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void confirmDeleteStatus(OrderStatusUpdate statusUpdate) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Status")
                .setMessage("Yakin ingin menghapus status ini?")
                .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success = databaseHelper.deleteOrderStatusUpdate(statusUpdate.getId());
                        if (success) {
                            order = databaseHelper.getOrderById(order.getId());
                            loadStatusUpdates();
                            Toast.makeText(OrderDetailActivity.this, "Status dihapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OrderDetailActivity.this, "Gagal menghapus status", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private List<CartItem> parseItems(String itemsJson) {
        List<CartItem> items = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(itemsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemObj = jsonArray.getJSONObject(i);
                String productId = itemObj.getString("product_id");
                int quantity = itemObj.getInt("quantity");
                String productName = itemObj.optString("product_name", "Produk");
                String productCategory = itemObj.optString("product_category", "");
                double price = itemObj.optDouble("price", 0);
                
                Product product = null;
                try {
                    product = databaseHelper.getProductById(Integer.parseInt(productId));
                } catch (NumberFormatException ignored) { }
                if (product == null) {
                    product = new Product();
                    product.setId(productId);
                    product.setName(productName);
                    product.setCategory(productCategory);
                    product.setPrice(price);
                }
                items.add(new CartItem(product, quantity));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }
}

