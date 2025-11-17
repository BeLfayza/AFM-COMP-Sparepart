package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputName;
    private TextView linkLogin;
    private EditText inputEmail;
    private EditText inputPassword;
    private Button buttonRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        linkLogin = findViewById(R.id.linkLogin);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String role = "user"; // restrict registration to regular users

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean ok = databaseHelper.registerUser(name, email, password, role);
                if (ok) {
                    Toast.makeText(RegisterActivity.this, "Pendaftaran berhasil. Silakan login.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Email sudah digunakan", Toast.LENGTH_SHORT).show();
                }
            }
        });
        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }
}


