package com.example.appsyncops;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OverviewFragment extends Fragment {

    private TextView tvSprintBadge, tvSprintGoal, tvDaysRemaining, tvChecklistCount, tvStoryPointsMetric, tvCompletedTasksMetric, tvFocusTimeMetric;
    private TextView tvTimelineToggleArrow, tvChecklistToggleArrow;
    private LinearLayout layoutMasterConfig, layoutChecklistContainer, layoutTimelineContent, layoutChecklistContent;
    private LinearLayout headerTimelineToggle, headerChecklistToggle;

    // Contêiner para a Central de Validação dinâmica
    private LinearLayout layoutValidationContainer;

    private EditText etSprintTitleInput, etSprintGoalInput, etSprintDaysInput, etNewCheckItem;
    private Button btnSaveSprint, btnAddCheckItem;

    private static final String PREF_NAME = "SyncOpsPrefs";
    private ArrayList<String> checklistItems = new ArrayList<>(Arrays.asList("Atualizar Daily da Equipe", "Aaaaa"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        tvSprintBadge = view.findViewById(R.id.tvSprintBadge);
        tvSprintGoal = view.findViewById(R.id.tvSprintGoal);
        tvDaysRemaining = view.findViewById(R.id.tvDaysRemaining);
        tvChecklistCount = view.findViewById(R.id.tvChecklistCount);

        tvStoryPointsMetric = view.findViewById(R.id.tvStoryPointsMetric);
        tvCompletedTasksMetric = view.findViewById(R.id.tvCompletedTasksMetric);
        tvFocusTimeMetric = view.findViewById(R.id.tvFocusTimeMetric);

        layoutMasterConfig = view.findViewById(R.id.layoutMasterConfig);
        etSprintTitleInput = view.findViewById(R.id.etSprintTitleInput);
        etSprintGoalInput = view.findViewById(R.id.etSprintGoalInput);
        etSprintDaysInput = view.findViewById(R.id.etSprintDaysInput);
        btnSaveSprint = view.findViewById(R.id.btnSaveSprint);

        etNewCheckItem = view.findViewById(R.id.etNewCheckItem);
        btnAddCheckItem = view.findViewById(R.id.btnAddCheckItem);
        layoutChecklistContainer = view.findViewById(R.id.layoutChecklistContainer);

        // Elementos de Minimizar / Maximizar
        headerTimelineToggle = view.findViewById(R.id.headerTimelineToggle);
        layoutTimelineContent = view.findViewById(R.id.layoutTimelineContent);
        tvTimelineToggleArrow = view.findViewById(R.id.tvTimelineToggleArrow);

        headerChecklistToggle = view.findViewById(R.id.headerChecklistToggle);
        layoutChecklistContent = view.findViewById(R.id.layoutChecklistContent);
        tvChecklistToggleArrow = view.findViewById(R.id.tvChecklistToggleArrow);

        // Evento de clique para Linha do Tempo
        headerTimelineToggle.setOnClickListener(v -> {
            if (layoutTimelineContent.getVisibility() == View.VISIBLE) {
                layoutTimelineContent.setVisibility(View.GONE);
                tvTimelineToggleArrow.setText("▼");
            } else {
                layoutTimelineContent.setVisibility(View.VISIBLE);
                tvTimelineToggleArrow.setText("▲");
            }
        });

        // Evento de clique para o Checklist
        headerChecklistToggle.setOnClickListener(v -> {
            if (layoutChecklistContent.getVisibility() == View.VISIBLE) {
                layoutChecklistContent.setVisibility(View.GONE);
                tvChecklistToggleArrow.setText("▼");
            } else {
                layoutChecklistContent.setVisibility(View.VISIBLE);
                tvChecklistToggleArrow.setText("▲");
            }
        });

        carregarDadosSprint();
        carregarChecklist();
        verificarPerfilMaster();
        carregarValidacoesPendentes(view);
        atualizarMetricasDaSprint(); // Atualiza a velocidade e tarefas concluídas com base nas entregas reais

        btnSaveSprint.setOnClickListener(v -> {
            String novoTitulo = etSprintTitleInput.getText().toString().trim();
            String novoObjetivo = etSprintGoalInput.getText().toString().trim();
            String novosDias = etSprintDaysInput.getText().toString().trim();

            if (novoTitulo.isEmpty() || novoObjetivo.isEmpty() || novosDias.isEmpty()) {
                Toast.makeText(getContext(), "Preencha todos os campos da sprint!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString("sprint_title", "🚀 " + novoTitulo)
                    .putString("sprint_goal", "\"" + novoObjetivo + "\"")
                    .putString("sprint_days", novosDias)
                    .apply();

            carregarDadosSprint();
            Toast.makeText(getContext(), "Sprint atualizada com sucesso!", Toast.LENGTH_SHORT).show();
        });

        btnAddCheckItem.setOnClickListener(v -> {
            String novoItemText = etNewCheckItem.getText().toString().trim();
            if (!novoItemText.isEmpty()) {
                checklistItems.add(novoItemText);
                salvarChecklist();
                atualizarViewChecklist();
                etNewCheckItem.setText("");
                Toast.makeText(getContext(), "Meta adicionada ao checklist!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Digite um texto para a meta!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Atualiza as métricas sempre que retornar para esta aba
        atualizarMetricasDaSprint();
    }

    private void atualizarMetricasDaSprint() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String historico = prefs.getString("history_logs", "");

        if (!historico.isEmpty()) {
            String[] linhas = historico.split("\n");
            int totalConcluidas = 0;
            int totalPontos = 0;

            for (String linha : linhas) {
                if (linha.contains("✓ Concluída")) {
                    totalConcluidas++;
                    totalPontos += 10; // Cada tarefa concluída adiciona 10 pts na velocidade
                }
            }

            if (tvStoryPointsMetric != null) {
                tvStoryPointsMetric.setText(totalPontos + " pts");
            }
            if (tvCompletedTasksMetric != null) {
                tvCompletedTasksMetric.setText(String.valueOf(totalConcluidas));
            }
        }
    }

    private void carregarValidacoesPendentes(View view) {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String pendencias = prefs.getString("pending_validations", "");

        if (!pendencias.isEmpty()) {
            // Lógica para processar as pendências de validação na tela
        }
    }

    private void carregarDadosSprint() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        tvSprintBadge.setText(prefs.getString("sprint_title", "🚀 Sprint 03 - Motor Principal"));
        tvSprintGoal.setText(prefs.getString("sprint_goal", "\"Finalizar motor de cronômetro, autenticação e integração com repositórios Git.\""));
        tvDaysRemaining.setText(prefs.getString("sprint_days", "4"));
    }

    private void verificarPerfilMaster() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isMaster = prefs.getBoolean("is_scrum_master", false);

        if (layoutMasterConfig != null) {
            layoutMasterConfig.setVisibility(isMaster ? View.VISIBLE : View.GONE);
        }
    }

    private void carregarChecklist() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> savedSet = prefs.getStringSet("checklist_items", null);
        if (savedSet != null) {
            checklistItems = new ArrayList<>(savedSet);
        }
        atualizarViewChecklist();
    }

    private void salvarChecklist() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(checklistItems);
        prefs.edit().putStringSet("checklist_items", set).apply();
    }

    private void atualizarViewChecklist() {
        if (getContext() == null) return;
        layoutChecklistContainer.removeAllViews();

        for (int i = 0; i < checklistItems.size(); i++) {
            final int index = i;
            String itemText = checklistItems.get(i);

            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 8, 0, 8);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            checkBox.setText(itemText);
            checkBox.setTextColor(0xFF263238);

            Button btnDelete = new Button(getContext());
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(110, 110);
            deleteParams.setMargins(8, 0, 0, 0);
            btnDelete.setLayoutParams(deleteParams);
            btnDelete.setText("🗑️");
            btnDelete.setTextSize(14);
            btnDelete.setPadding(0, 0, 0, 0);
            btnDelete.setOnClickListener(v -> {
                checklistItems.remove(index);
                salvarChecklist();
                atualizarViewChecklist();
            });

            row.addView(checkBox);
            row.addView(btnDelete);
            layoutChecklistContainer.addView(row);
        }

        tvChecklistCount.setText("0/" + checklistItems.size());
    }
}