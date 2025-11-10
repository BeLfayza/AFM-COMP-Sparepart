package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView textViewEmail;
    private TextView textViewPassword;
    private Button buttonLogout;
    private LinearLayout layoutProduk, layoutPesanan, layoutProfil;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        initializeViews();
        loadProfileInfo();
        setupLogout();
        setupFooterNavigation();
    }

    private void initializeViews() {
        textViewEmail = findViewById(R.id.textViewAdminEmail);
        textViewPassword = findViewById(R.id.textViewAdminPassword);
        buttonLogout = findViewById(R.id.buttonAdminLogout);
        layoutProduk = findViewById(R.id.layoutProduk);
        layoutPesanan = findViewById(R.id.layoutPesanan);
        layoutProfil = findViewById(R.id.layoutProfil);
    }

    private void loadProfileInfo() {
        textViewEmail.setText(prefs.getString("user_email", ""));
        textViewPassword.setText(prefs.getString("user_password", ""));
    }

    private void setupLogout() {
        buttonLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            getSharedPreferences("cart", MODE_PRIVATE).edit().clear().apply();

            Intent intent = new Intent(AdminProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupFooterNavigation() {
        updateFooterSelection(layoutProfil, true);

        layoutProduk.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProfileActivity.this, AdminMainActivity.class);
            startActivity(intent);
            finish();
        });

        layoutPesanan.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProfileActivity.this, AdminOrdersActivity.class);
            startActivity(intent);
            finish();
        });

        layoutProfil.setOnClickListener(v -> updateFooterSelection(layoutProfil, true));
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
}




