package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AddEditProductActivity extends AppCompatActivity {

    private EditText editTextName, editTextCategory, editTextPrice, editTextStock, editTextDescription;
    private Button buttonSave, buttonDelete, buttonPickImage;
    private TextView textViewTitle, textViewSelectedImage;
    private ImageView imageViewProductPreview;

    private String selectedImageUri = "";
    private static final int REQ_PICK_IMAGE = 1001;

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
        buttonPickImage = findViewById(R.id.buttonPickImage);

        textViewSelectedImage = findViewById(R.id.textViewSelectedImage);

        imageViewProductPreview = findViewById(R.id.imageViewProductPreview); // TAMBAHKAN DI XML
    }

    private void loadProductIfNeeded() {
        String productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            currentProduct = databaseHelper.getProductById(Integer.parseInt(productId));

            if (currentProduct != null) {
                textViewTitle.setText("Edit Produk");

                editTextName.setText(currentProduct.getName());
                editTextCategory.setText(currentProduct.getCategory());
                editTextPrice.setText(String.valueOf(currentProduct.getPrice()));
                editTextStock.setText(String.valueOf(currentProduct.getStock()));
                editTextDescription.setText(currentProduct.getDescription());

                // load image jika ada
                if (currentProduct.getImageUrl() != null && !currentProduct.getImageUrl().isEmpty()) {
                    selectedImageUri = currentProduct.getImageUrl();
                    textViewSelectedImage.setText(selectedImageUri);
                    imageViewProductPreview.setImageURI(Uri.parse(selectedImageUri));
                }

                buttonDelete.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupActions() {
        buttonPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQ_PICK_IMAGE);
        });

        buttonSave.setOnClickListener(v -> saveProduct());
        buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    private void saveProduct() {
        String name = editTextName.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String stockStr = editTextStock.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int stock;

        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (Exception e) {
            Toast.makeText(this, "Harga & stok harus angka!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;

        if (currentProduct == null) {
            // tambah produk
            success = databaseHelper.addProduct(
                    name, category, price,
                    selectedImageUri != null ? selectedImageUri : "",
                    description, stock
            );
        } else {
            // update produk
            success = databaseHelper.updateProduct(
                    Integer.parseInt(currentProduct.getId()),
                    name, category, price,
                    selectedImageUri != null ? selectedImageUri : currentProduct.getImageUrl(),
                    description, stock
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            selectedImageUri = uri.toString();
            textViewSelectedImage.setText(selectedImageUri);

            // tampilkan di preview
            imageViewProductPreview.setImageURI(uri);
        }
    }
}

