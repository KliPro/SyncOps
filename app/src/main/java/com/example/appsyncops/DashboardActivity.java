package com.example.appsyncops;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUserRole;
    private Button navOverview, navTasks, navDaily, navCeremonies, navProfile;
    private static final String PREF_NAME = "SyncOpsPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FORÇA O TEMA CLARO GLOBALMENTE NA DASHBOARD
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvUserRole = findViewById(R.id.tvUserRole);
        navOverview = findViewById(R.id.nav_overview);
        navTasks = findViewById(R.id.nav_tasks);
        navDaily = findViewById(R.id.nav_daily);
        navCeremonies = findViewById(R.id.nav_ceremonies);
        navProfile = findViewById(R.id.nav_profile);

        atualizarPerfilRBAC();

        // Carrega a aba padrão (Sprint / Overview) ao iniciar
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new OverviewFragment())
                    .commit();
        }

        // Configuração dos cliques da barra de navegação inferior atualizada
        navOverview.setOnClickListener(v -> trocarFragment(new OverviewFragment()));
        navTasks.setOnClickListener(v -> trocarFragment(new TasksFragment()));

        // Agora abre a aba Daily corretamente integrando o TeamFragment
        navDaily.setOnClickListener(v -> trocarFragment(new TeamFragment()));

        navCeremonies.setOnClickListener(v -> trocarFragment(new RitosFragment()));
        navProfile.setOnClickListener(v -> trocarFragment(new ProfileFragment()));
    }

    private void trocarFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void atualizarPerfilRBAC() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isMaster = prefs.getBoolean("is_scrum_master", false);

        if (isMaster) {
            tvUserRole.setText("🚀 SCRUM MASTER");
            tvUserRole.setBackgroundColor(0xFFEF6C00); // Laranja destaque
            tvUserRole.setTextColor(0xFFFFFFFF);
        } else {
            tvUserRole.setText("MEMBRO");
            tvUserRole.setBackgroundColor(0xFFE0E0E0); // Cinza discreto
            tvUserRole.setTextColor(0xFF424242);
        }
    }
}