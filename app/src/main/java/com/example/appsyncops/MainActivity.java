package com.example.appsyncops;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin, btnMicrosoftLogin;
    private TextView tvForgotPassword, tvRegister;
    private static final String PREF_NAME = "SyncOpsPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Garante o tema claro na tela de login
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnMicrosoftLogin = findViewById(R.id.btnMicrosoftLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        // Ação do Botão Principal "Entrar no SyncOps"
        btnLogin.setOnClickListener(v -> realizarLogin());

        // Atalho para login social do Google
        btnGoogleLogin.setOnClickListener(v -> {
            etEmail.setText("ana.silva@googleworkspace.com");
            realizarLogin();
        });

        // Atalho para login social da Microsoft
        btnMicrosoftLogin.setOnClickListener(v -> {
            etEmail.setText("carlos.lead@microsoft365.com");
            realizarLogin();
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Instruções de recuperação enviadas para o e-mail.", Toast.LENGTH_SHORT).show()
        );

        tvRegister.setOnClickListener(v ->
                Toast.makeText(this, "Redirecionando para o cadastro do SyncOps...", Toast.LENGTH_SHORT).show()
        );
    }

    private void realizarLogin() {
        String email = etEmail != null ? etEmail.getText().toString().trim() : "membro@syncops.io";
        if (email.isEmpty()) {
            email = "membro@syncops.io";
        }

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Define automaticamente se é Scrum Master ou Membro com base no e-mail digitado
        boolean isMasterInitial = email.toLowerCase().contains("admin") ||
                email.toLowerCase().contains("lead") ||
                email.toLowerCase().contains("gestor") ||
                email.toLowerCase().contains("carlos") ||
                email.toLowerCase().contains("ana");

        prefs.edit()
                .putString("logged_user_email", email)
                .putBoolean("is_scrum_master", isMasterInitial)
                .apply();

        // Entra no Workspace
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
    }
}