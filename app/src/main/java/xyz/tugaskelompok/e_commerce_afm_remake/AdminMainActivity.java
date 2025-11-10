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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProducts;
    private AdminProductAdapter productAdapter;
    private FloatingActionButton fabAddProduct;
    private LinearLayout layoutProduk, layoutPesanan, layoutProfil;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);

        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupRecyclerView();
        loadProducts();
        setupFab();
        setupFooterNavigation();
    }

    private void initializeViews() {
        recyclerViewProducts = findViewById(R.id.recyclerViewAdminProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        layoutProduk = findViewById(R.id.layoutProduk);
        layoutPesanan = findViewById(R.id.layoutPesanan);
        layoutProfil = findViewById(R.id.layoutProfil);
    }

    private void setupRecyclerView() {
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new AdminProductAdapter(null, new AdminProductAdapter.ProductActionListener() {
            @Override
            public void onEdit(Product product) {
                Intent intent = new Intent(AdminMainActivity.this, AddEditProductActivity.class);
                intent.putExtra("productId", product.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Product product) {
                confirmDeleteProduct(product);
            }
        });
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupFab() {
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AddEditProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        List<Product> products = databaseHelper.getAllProducts();
        productAdapter.updateProducts(products);
    }

    private void confirmDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Produk")
                .setMessage("Yakin ingin menghapus produk \"" + product.getName() + "\"?")
                .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success = databaseHelper.deleteProduct(Integer.parseInt(product.getId()));
                        if (success) {
                            loadProducts();
                            Toast.makeText(AdminMainActivity.this, "Produk dihapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminMainActivity.this, "Gagal menghapus produk", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void setupFooterNavigation() {
        updateFooterSelection(layoutProduk, true);

        layoutProduk.setOnClickListener(v -> updateFooterSelection(layoutProduk, true));

        layoutPesanan.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminOrdersActivity.class);
            startActivity(intent);
            finish();
        });

        layoutProfil.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminProfileActivity.class);
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
        loadProducts();
        updateFooterSelection(layoutProduk, true);
    }
}
