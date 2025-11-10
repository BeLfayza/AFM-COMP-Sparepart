package xyz.tugaskelompok.e_commerce_afm_remake;

import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> filteredProducts;
    private EditText editTextSearch;
    private ImageView imageViewCart;
    private LinearLayout layoutBelanja, layoutPesanan, layoutProfil;
    private Chip chipCpu, chipGpu, chipCase, chipSsd, chipRam, chipPsu;
    private String selectedCategory = null;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        initializeViews();
        setupRecyclerView();
        loadProductsFromDatabase();
        setupCategoryChips();
        setupSearch();
        setupFooterNavigation();
        setupCart();
    }

    private void initializeViews() {
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        editTextSearch = findViewById(R.id.editTextSearch);
        imageViewCart = findViewById(R.id.imageViewCart);
        layoutBelanja = findViewById(R.id.layoutBelanja);
        layoutPesanan = findViewById(R.id.layoutPesanan);
        layoutProfil = findViewById(R.id.layoutProfil);
        chipCpu = findViewById(R.id.chipCpu);
        chipGpu = findViewById(R.id.chipGpu);
        chipCase = findViewById(R.id.chipCase);
        chipSsd = findViewById(R.id.chipSsd);
        chipRam = findViewById(R.id.chipRam);
        chipPsu = findViewById(R.id.chipPsu);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewProducts.setLayoutManager(layoutManager);
        
        filteredProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(filteredProducts);
        productAdapter.setOnProductClickListener(product -> {
            // TODO: Handle product click - navigate to product detail
            // Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            // intent.putExtra("product", product);
            // startActivity(intent);
        });
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void loadProductsFromDatabase() {
        filterProducts();
    }

    private void setupCategoryChips() {
        CompoundButton.OnCheckedChangeListener chipListener = (buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck other chips
                if (buttonView != chipCpu) chipCpu.setChecked(false);
                if (buttonView != chipGpu) chipGpu.setChecked(false);
                if (buttonView != chipCase) chipCase.setChecked(false);
                if (buttonView != chipSsd) chipSsd.setChecked(false);
                if (buttonView != chipRam) chipRam.setChecked(false);
                if (buttonView != chipPsu) chipPsu.setChecked(false);
                
                // Set selected category
                if (buttonView == chipCpu) selectedCategory = "CPU";
                else if (buttonView == chipGpu) selectedCategory = "GPU";
                else if (buttonView == chipCase) selectedCategory = "Case";
                else if (buttonView == chipSsd) selectedCategory = "SSD";
                else if (buttonView == chipRam) selectedCategory = "RAM";
                else if (buttonView == chipPsu) selectedCategory = "PSU";
                
                filterProducts();
            } else {
                selectedCategory = null;
                filterProducts();
            }
        };

        chipCpu.setOnCheckedChangeListener(chipListener);
        chipGpu.setOnCheckedChangeListener(chipListener);
        chipCase.setOnCheckedChangeListener(chipListener);
        chipSsd.setOnCheckedChangeListener(chipListener);
        chipRam.setOnCheckedChangeListener(chipListener);
        chipPsu.setOnCheckedChangeListener(chipListener);
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts();
            }

                @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts() {
        String searchQuery = editTextSearch.getText().toString().trim();
        
        List<Product> products;
        
        if (searchQuery.isEmpty() && selectedCategory == null) {
            // Load all products
            products = databaseHelper.getAllProducts();
        } else if (searchQuery.isEmpty()) {
            // Filter by category only
            products = databaseHelper.getProductsByCategory(selectedCategory);
        } else if (selectedCategory == null) {
            // Filter by search only
            products = databaseHelper.searchProducts(searchQuery);
        } else {
            // Filter by both category and search
            products = databaseHelper.getProductsByCategoryAndSearch(selectedCategory, searchQuery);
        }
        
        filteredProducts.clear();
        filteredProducts.addAll(products);
        productAdapter.notifyDataSetChanged();
    }

    private void setupFooterNavigation() {
        // Belanja (already on this page, so highlight it)
        updateFooterSelection(layoutBelanja, true);
        
        layoutBelanja.setOnClickListener(v -> {
            // Already on belanja page
            updateFooterSelection(layoutBelanja, true);
        });

        layoutPesanan.setOnClickListener(v -> {
            // TODO: Navigate to orders page
            // Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
            // startActivity(intent);
            updateFooterSelection(layoutPesanan, true);
        });

        layoutProfil.setOnClickListener(v -> {
            // TODO: Navigate to profile page
            // Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            // startActivity(intent);
            updateFooterSelection(layoutProfil, true);
        });
    }

    private void updateFooterSelection(LinearLayout selectedLayout, boolean isSelected) {
        // Reset all
        updateFooterItem(layoutBelanja, false);
        updateFooterItem(layoutPesanan, false);
        updateFooterItem(layoutProfil, false);
        
        // Highlight selected
        if (isSelected) {
            updateFooterItem(selectedLayout, true);
        }
    }

    private void updateFooterItem(LinearLayout layout, boolean isSelected) {
        ImageView icon = (ImageView) layout.getChildAt(0);
        TextView text = (TextView) layout.getChildAt(1);
        
        int color = isSelected ? 0xFF2196F3 : 0xFF757575;
        icon.setColorFilter(color);
        text.setTextColor(color);
    }

    private void setupCart() {
        imageViewCart.setOnClickListener(v -> {
            // TODO: Navigate to cart page
            // Intent intent = new Intent(MainActivity.this, CartActivity.class);
            // startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
