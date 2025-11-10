package xyz.tugaskelompok.e_commerce_afm_remake;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AddEditProductActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextCategory;
    private EditText editTextPrice;
    private EditText editTextStock;
    private EditText editTextDescription;
    private Button buttonSave;
    private Button buttonDelete;
    private TextView textViewTitle;

    private DatabaseHelper databaseHelper;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_product);

        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        loadProductIfNeeded();
        setupActions();
    }

    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        editTextName = findViewById(R.id.editTextProductName);
        editTextCategory = findViewById(R.id.editTextProductCategory);
        editTextPrice = findViewById(R.id.editTextProductPrice);
        editTextStock = findViewById(R.id.editTextProductStock);
        editTextDescription = findViewById(R.id.editTextProductDescription);
        buttonSave = findViewById(R.id.buttonSaveProduct);
        buttonDelete = findViewById(R.id.buttonDeleteProduct);
    }

    private void loadProductIfNeeded() {
        String productId = getIntent().getStringExtra("productId");
        if (!TextUtils.isEmpty(productId)) {
            currentProduct = databaseHelper.getProductById(Integer.parseInt(productId));
            if (currentProduct != null) {
                textViewTitle.setText("Edit Produk");
                editTextName.setText(currentProduct.getName());
                editTextCategory.setText(currentProduct.getCategory());
                editTextPrice.setText(String.valueOf(currentProduct.getPrice()));
                editTextStock.setText(String.valueOf(currentProduct.getStock()));
                editTextDescription.setText(currentProduct.getDescription());
                buttonDelete.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupActions() {
        buttonSave.setOnClickListener(v -> saveProduct());
        buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    private void saveProduct() {
        String name = editTextName.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String stockStr = editTextStock.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(stockStr)) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Harga dan stok harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;
        if (currentProduct == null) {
            success = databaseHelper.addProduct(name, category, price, "", description, stock);
        } else {
            success = databaseHelper.updateProduct(
                    Integer.parseInt(currentProduct.getId()),
                    name,
                    category,
                    price,
                    currentProduct.getImageUrl() != null ? currentProduct.getImageUrl() : "",
                    description,
                    stock
            );
        }

        if (success) {
            Toast.makeText(this, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Gagal menyimpan produk", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        if (currentProduct == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Hapus Produk")
                .setMessage("Yakin ingin menghapus produk ini?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    boolean success = databaseHelper.deleteProduct(Integer.parseInt(currentProduct.getId()));
                    if (success) {
                        Toast.makeText(this, "Produk dihapus", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Gagal menghapus produk", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}




