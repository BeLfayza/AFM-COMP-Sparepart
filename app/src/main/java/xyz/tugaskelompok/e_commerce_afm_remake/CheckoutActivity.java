package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private EditText editTextAddress;
    private EditText editTextPhone;
    private TextView textViewUserName;
    private TextView textViewSubtotal;
    private TextView textViewShipping;
    private TextView textViewTotal;
    private Button buttonPlaceOrder;
    private DatabaseHelper databaseHelper;
    private SharedPreferences cartPrefs;
    private SharedPreferences userPrefs;
    private List<CartItem> cartItems;
    private double subtotal = 0;
    private double shipping = 15000; // Fixed shipping cost

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);

        databaseHelper = new DatabaseHelper(this);
        cartPrefs = getSharedPreferences("cart", MODE_PRIVATE);
        userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        initializeViews();
        loadUserInfo();
        loadCartItems();
        calculateTotals();
        setupPlaceOrder();
    }

    private void initializeViews() {
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhone = findViewById(R.id.editTextPhone);
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewShipping = findViewById(R.id.textViewShipping);
        textViewTotal = findViewById(R.id.textViewTotal);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
    }

    private void loadUserInfo() {
        String email = userPrefs.getString("user_email", "");
        String[] userInfo = databaseHelper.getUserInfoByEmail(email);
        
        if (userInfo != null) {
            textViewUserName.setText(userInfo[0]); // name
            if (userInfo[2] != null && !userInfo[2].isEmpty()) {
                editTextAddress.setText(userInfo[2]); // address
            }
            if (userInfo[3] != null && !userInfo[3].isEmpty()) {
                editTextPhone.setText(userInfo[3]); // phone
            }
        }
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
    }

    private void calculateTotals() {
        subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getProduct().getPrice() * item.getQuantity();
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        textViewSubtotal.setText("Subtotal: " + format.format(subtotal));
        textViewShipping.setText("Ongkir: " + format.format(shipping));
        textViewTotal.setText("Total: " + format.format(subtotal + shipping));
    }

    private void setupPlaceOrder() {
        buttonPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = editTextAddress.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();

                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(CheckoutActivity.this, "Alamat wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(CheckoutActivity.this, "Nomor HP wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cartItems.isEmpty()) {
                    Toast.makeText(CheckoutActivity.this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save address and phone to user profile
                String email = userPrefs.getString("user_email", "");
                databaseHelper.updateUserProfile(email, address, phone);

                // Create JSON for items
                JSONArray itemsArray = new JSONArray();
                try {
                    for (CartItem item : cartItems) {
                        JSONObject itemObj = new JSONObject();
                        itemObj.put("product_id", item.getProduct().getId());
                        itemObj.put("product_name", item.getProduct().getName());
                        itemObj.put("product_category", item.getProduct().getCategory());
                        itemObj.put("quantity", item.getQuantity());
                        itemObj.put("price", item.getProduct().getPrice());
                        itemsArray.put(itemObj);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String itemsJson = itemsArray.toString();
                String userName = textViewUserName.getText().toString();

                // Create order
                long orderId = databaseHelper.createOrder(
                    email,
                    userName,
                    address,
                    phone,
                    itemsJson,
                    subtotal,
                    shipping
                );

                if (orderId > 0) {
                    // Clear cart
                    SharedPreferences.Editor editor = cartPrefs.edit();
                    editor.clear();
                    editor.apply();

                    Toast.makeText(CheckoutActivity.this, "Pesanan berhasil dibuat", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to orders page
                    Intent intent = new Intent(CheckoutActivity.this, OrdersActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

