package com.example.appsyncops;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import java.util.HashSet;
import java.util.Set;

public class ProfileFragment extends Fragment {

    private TextView tvProfileEmail, tvSkillsDisplay, tvCurrentActiveProfileTitle;
    private EditText etProfileName, etProfileRole, etProfileDepartment, etNewSkill, etMasterNotes;
    private EditText etSlackLink, etTeamsLink, etGithubLink, etGitlabLink;
    private Spinner spinnerStatus;
    private Button btnSaveProfile, btnAddSkill, btnSaveNotes, btnToggleTheme, btnSwitchToMemberProfile, btnSwitchToMasterProfile, btnLogout;
    private LinearLayout layoutMemberSection, layoutMasterSection;
    private static final String PREF_NAME = "SyncOpsPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvSkillsDisplay = view.findViewById(R.id.tvSkillsDisplay);
        tvCurrentActiveProfileTitle = view.findViewById(R.id.tvCurrentActiveProfileTitle);

        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileRole = view.findViewById(R.id.etProfileRole);
        etProfileDepartment = view.findViewById(R.id.etProfileDepartment);
        etNewSkill = view.findViewById(R.id.etNewSkill);
        etMasterNotes = view.findViewById(R.id.etMasterNotes);

        // Campos de Integrações
        etSlackLink = view.findViewById(R.id.etSlackLink);
        etTeamsLink = view.findViewById(R.id.etTeamsLink);
        etGithubLink = view.findViewById(R.id.etGithubLink);
        etGitlabLink = view.findViewById(R.id.etGitlabLink);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);

        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnAddSkill = view.findViewById(R.id.btnAddSkill);
        btnSaveNotes = view.findViewById(R.id.btnSaveNotes);
        btnToggleTheme = view.findViewById(R.id.btnToggleTheme);

        btnSwitchToMemberProfile = view.findViewById(R.id.btnSwitchToMemberProfile);
        btnSwitchToMasterProfile = view.findViewById(R.id.btnSwitchToMasterProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        layoutMemberSection = view.findViewById(R.id.layoutMemberSection);
        layoutMasterSection = view.findViewById(R.id.layoutMasterSection);

        // Configurar as opções do Spinner de Status
        ArrayAdapter<CharSequence> adapterStatus = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.status_disponibilidade_array,
                android.R.layout.simple_spinner_item
        );
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapterStatus);

        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        carregarDadosDoPerfilAtivo(prefs);

        // Salvar alterações do perfil atual (incluindo status e integrações)
        btnSaveProfile.setOnClickListener(v -> {
            boolean isMaster = prefs.getBoolean("is_scrum_master", false);
            String prefixo = isMaster ? "master_" : "member_";
            String statusSelecionado = spinnerStatus.getSelectedItem().toString();

            prefs.edit()
                    .putString(prefixo + "name", etProfileName.getText().toString().trim())
                    .putString(prefixo + "role", etProfileRole.getText().toString().trim())
                    .putString(prefixo + "department", etProfileDepartment.getText().toString().trim())
                    .putString(prefixo + "status", statusSelecionado)
                    .putString(prefixo + "slack", etSlackLink.getText().toString().trim())
                    .putString(prefixo + "teams", etTeamsLink.getText().toString().trim())
                    .putString(prefixo + "github", etGithubLink.getText().toString().trim())
                    .putString(prefixo + "gitlab", etGitlabLink.getText().toString().trim())
                    .apply();

            Toast.makeText(getContext(), "Perfil e integrações salvos com sucesso!", Toast.LENGTH_SHORT).show();
        });

        // Alternar para o Perfil de Membro
        btnSwitchToMemberProfile.setOnClickListener(v -> {
            prefs.edit().putBoolean("is_scrum_master", false).apply();
            Toast.makeText(getContext(), "Alternado para o Perfil: Membro 👤", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().recreate();
        });

        // Alternar para o Perfil de Scrum Master
        btnSwitchToMasterProfile.setOnClickListener(v -> {
            prefs.edit().putBoolean("is_scrum_master", true).apply();
            Toast.makeText(getContext(), "Alternado para o Perfil: Scrum Master 🚀", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().recreate();
        });

        // Adicionar Skill (Específico do Membro)
        btnAddSkill.setOnClickListener(v -> {
            String novaSkill = etNewSkill.getText().toString().trim();
            if (!novaSkill.isEmpty()) {
                Set<String> skillsSet = prefs.getStringSet("member_skills", new HashSet<>());
                Set<String> updatedSet = new HashSet<>(skillsSet);
                updatedSet.add("[" + novaSkill + "]");

                prefs.edit().putStringSet("member_skills", updatedSet).apply();
                carregarSkills(prefs);
                etNewSkill.setText("");
                Toast.makeText(getContext(), "Competência adicionada!", Toast.LENGTH_SHORT).show();
            }
        });

        // Salvar notas de 1-on-1 (Específico do Master)
        btnSaveNotes.setOnClickListener(v -> {
            String notas = etMasterNotes.getText().toString().trim();
            prefs.edit().putString("master_private_notes", notas).apply();
            Toast.makeText(getContext(), "Notas de facilitação salvas!", Toast.LENGTH_SHORT).show();
        });

        // Tema
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        btnToggleTheme.setText(isDarkMode ? "☀️ Mudar para Tema Claro" : "🌙 Mudar para Tema Escuro");

        btnToggleTheme.setOnClickListener(v -> {
            boolean currentMode = prefs.getBoolean("dark_mode", false);
            boolean newMode = !currentMode;
            prefs.edit().putBoolean("dark_mode", newMode).apply();

            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            btnToggleTheme.setText(newMode ? "☀️ Mudar para Tema Claro" : "🌙 Mudar para Tema Escuro");
        });

        btnLogout.setOnClickListener(v -> getActivity().finish());

        return view;
    }

    private void carregarDadosDoPerfilAtivo(SharedPreferences prefs) {
        boolean isMaster = prefs.getBoolean("is_scrum_master", false);
        String prefixo = isMaster ? "master_" : "member_";

        if (isMaster) {
            tvCurrentActiveProfileTitle.setText("🟢 Perfil Ativo: Scrum Master (Gestão)");
            tvProfileEmail.setText(prefs.getString("master_email", "carlos.lead@syncops.io"));
            etProfileName.setText(prefs.getString("master_name", "Carlos Lead"));
            etProfileRole.setText(prefs.getString("master_role", "Agile Coach & Scrum Master"));
            etProfileDepartment.setText(prefs.getString("master_department", "Governança Ágil"));

            if (layoutMemberSection != null) layoutMemberSection.setVisibility(View.GONE);
            if (layoutMasterSection != null) layoutMasterSection.setVisibility(View.VISIBLE);
            if (btnSwitchToMasterProfile != null) btnSwitchToMasterProfile.setVisibility(View.GONE);
            if (btnSwitchToMemberProfile != null) btnSwitchToMemberProfile.setVisibility(View.VISIBLE);

            etMasterNotes.setText(prefs.getString("master_private_notes", ""));
        } else {
            tvCurrentActiveProfileTitle.setText("🟢 Perfil Ativo: Membro (Colaborador)");
            tvProfileEmail.setText(prefs.getString("member_email", "ana.silva@syncops.io"));
            etProfileName.setText(prefs.getString("member_name", "Ana Silva"));
            etProfileRole.setText(prefs.getString("member_role", "Desenvolvedora Mobile Front-end"));
            etProfileDepartment.setText(prefs.getString("member_department", "Engenharia de Software"));

            if (layoutMemberSection != null) layoutMemberSection.setVisibility(View.VISIBLE);
            if (layoutMasterSection != null) layoutMasterSection.setVisibility(View.GONE);
            if (btnSwitchToMemberProfile != null) btnSwitchToMemberProfile.setVisibility(View.GONE);
            if (btnSwitchToMasterProfile != null) btnSwitchToMasterProfile.setVisibility(View.VISIBLE);

            carregarSkills(prefs);
        }

        // Carregar integrações e status salvos
        etSlackLink.setText(prefs.getString(prefixo + "slack", "slack.com/app/ana"));
        etTeamsLink.setText(prefs.getString(prefixo + "teams", "teams.microsoft.com/ana"));
        etGithubLink.setText(prefs.getString(prefixo + "github", "github.com/ana-syncops"));
        etGitlabLink.setText(prefs.getString(prefixo + "gitlab", "gitlab.com/ana-syncops"));

        // Selecionar o status salvo no Spinner
        String statusSalvo = prefs.getString(prefixo + "status", "Foco / Em desenvolvimento");
        ArrayAdapter adapter = (ArrayAdapter) spinnerStatus.getAdapter();
        if (adapter != null) {
            int spinnerPosition = adapter.getPosition(statusSalvo);
            if (spinnerPosition >= 0) {
                spinnerStatus.setSelection(spinnerPosition);
            }
        }
    }

    private void carregarSkills(SharedPreferences prefs) {
        Set<String> skillsSet = prefs.getStringSet("member_skills", null);
        if (skillsSet == null || skillsSet.isEmpty()) {
            skillsSet = new HashSet<>();
            skillsSet.add("[Java]");
            skillsSet.add("[Android]");
            skillsSet.add("[Git]");
            skillsSet.add("[Scrum]");
        }
        tvSkillsDisplay.setText(String.join(" ", skillsSet));
    }
}