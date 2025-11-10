package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private EditText editTextAddress;
    private EditText editTextPhone;
    private TextView textViewPasswordLabel;
    private TextView textViewPassword;
    private Button buttonSave;
    private Button buttonLogout;
    private LinearLayout layoutBelanja, layoutPesanan, layoutProfil;
    private DatabaseHelper databaseHelper;
    private SharedPreferences userPrefs;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String role = userPrefs.getString("user_role", "user");
        isAdmin = "admin".equalsIgnoreCase(role);

        initializeViews();
        loadUserInfo();
        if (!isAdmin) {
            setupSave();
        }
        setupLogout();
        setupFooterNavigation();
    }

    private void initializeViews() {
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewPasswordLabel = findViewById(R.id.textViewPasswordLabel);
        textViewPassword = findViewById(R.id.textViewPassword);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSave = findViewById(R.id.buttonSave);
        buttonLogout = findViewById(R.id.buttonLogout);
        layoutBelanja = findViewById(R.id.layoutBelanja);
        layoutPesanan = findViewById(R.id.layoutPesanan);
        layoutProfil = findViewById(R.id.layoutProfil);
    }

    private void loadUserInfo() {
        String email = userPrefs.getString("user_email", "");

        if (isAdmin) {
            String[] userInfo = databaseHelper.getUserInfoByEmail(email);
            if (userInfo != null) {
                textViewUserName.setText(userInfo[0]);
                textViewUserEmail.setText(userInfo[1]);
            } else {
                textViewUserEmail.setText(email);
            }
            textViewPasswordLabel.setVisibility(View.VISIBLE);
            textViewPassword.setVisibility(View.VISIBLE);
            textViewPassword.setText(userPrefs.getString("user_password", ""));

            editTextAddress.setVisibility(View.GONE);
            editTextPhone.setVisibility(View.GONE);
            buttonSave.setVisibility(View.GONE);
        } else {
            textViewPasswordLabel.setVisibility(View.GONE);
            textViewPassword.setVisibility(View.GONE);

            String[] userInfo = databaseHelper.getUserInfoByEmail(email);
            if (userInfo != null) {
                textViewUserName.setText(userInfo[0]); // name
                textViewUserEmail.setText(userInfo[1]); // email
                if (userInfo[2] != null && !userInfo[2].isEmpty()) {
                    editTextAddress.setText(userInfo[2]); // address
                }
                if (userInfo[3] != null && !userInfo[3].isEmpty()) {
                    editTextPhone.setText(userInfo[3]); // phone
                }
            }
        }
    }

    private void setupSave() {
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = editTextAddress.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();

                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(ProfileActivity.this, "Alamat wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(ProfileActivity.this, "Nomor HP wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String email = prefs.getString("user_email", "");

                boolean success = databaseHelper.updateUserProfile(email, address, phone);
                if (success) {
                    Toast.makeText(ProfileActivity.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupLogout() {
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear user session
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                
                // Clear cart
                SharedPreferences cartPrefs = getSharedPreferences("cart", MODE_PRIVATE);
                cartPrefs.edit().clear().apply();
                
                // Navigate to login
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupFooterNavigation() {
        updateFooterSelection(layoutProfil, true);
        
        layoutBelanja.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        layoutPesanan.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrdersActivity.class);
            startActivity(intent);
            finish();
        });

        layoutProfil.setOnClickListener(v -> {
            // Already on profile page
            updateFooterSelection(layoutProfil, true);
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

