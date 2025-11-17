package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private Product product;
    private TextView textViewProductName;
    private TextView textViewProductPrice;
    private TextView textViewProductCategory;
    private TextView textViewProductDescription;
    private ImageView imageViewProduct;
    private Button buttonAddToCart;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        databaseHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        String productId = intent.getStringExtra("productId");

        if (productId != null) {
            product = databaseHelper.getProductById(Integer.parseInt(productId));
        } else {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (product == null) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        displayProduct();
        setupAddToCart();
    }

    private void initializeViews() {
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewProductCategory = findViewById(R.id.textViewProductCategory);
        textViewProductDescription = findViewById(R.id.textViewProductDescription);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        buttonAddToCart = findViewById(R.id.buttonAddToCart);
    }

    private void displayProduct() {
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                    imageViewProduct.setImageURI(Uri.parse(imageUrl));
                } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    // Load from network on background thread
                    imageViewProduct.setImageBitmap(null);
                    new Thread(() -> {
                        Bitmap bmp = downloadBitmap(imageUrl);
                        if (bmp != null) {
                            imageViewProduct.post(() -> imageViewProduct.setImageBitmap(bmp));
                        }
                    }).start();
                }
            } catch (Exception ignored) {
            }
        }
        textViewProductName.setText(product.getName());
        textViewProductCategory.setText(product.getCategory());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        textViewProductPrice.setText(format.format(product.getPrice()));

        // Get full product details from database
        Product fullProduct = databaseHelper.getProductById(Integer.parseInt(product.getId()));
        if (fullProduct != null) {
            // Description would be in the database, but Product class doesn't have it yet
            // For now, just show basic info
            textViewProductDescription.setText("Deskripsi Produk: \n" + fullProduct.getDescription());
        }
    }

    private void setupAddToCart() {
        buttonAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });
    }

    private void addToCart() {
        SharedPreferences prefs = getSharedPreferences("cart", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Get current quantity
        int currentQuantity = prefs.getInt("product_" + product.getId(), 0);
        editor.putInt("product_" + product.getId(), currentQuantity + 1);
        editor.apply();

        Toast.makeText(this, "Produk ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
    }
    private Bitmap downloadBitmap(String src) {
        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            return null;
        } finally {
            try { if (input != null) input.close(); } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
    }
}


