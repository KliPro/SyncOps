package com.example.appsyncops;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;

public class ReportsFragment extends Fragment {

    private MaterialCardView cardRbacLocked;
    private LinearLayout layoutReportsContent;
    private TextView tvAuditLogDisplay;
    private static final String PREF_NAME = "SyncOpsPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        cardRbacLocked = view.findViewById(R.id.cardRbacLocked);
        layoutReportsContent = view.findViewById(R.id.layoutReportsContent);

        // Se você adicionar um TextView no fragment_reports.xml para exibir a auditoria, vincule-o aqui:
        // tvAuditLogDisplay = view.findViewById(R.id.tvAuditLogDisplay);

        verificarPermissaoRbac();
        carregarLogAuditoria();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarLogAuditoria();
    }

    private void carregarLogAuditoria() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Recupera o histórico consolidado de tarefas concluídas e ações do sistema
        String historicoTarefas = prefs.getString("history_logs", "Nenhuma atividade registrada no sistema.");
        String ritoTipo = prefs.getString("rito_tipo", "Nenhum");
        String ritoData = prefs.getString("rito_data_hora", "N/A");

        StringBuilder auditoriaCompleta = new StringBuilder();
        auditoriaCompleta.append("=== AUDITORIA DE ATIVIDADES DO SYNCCOPS ===\n\n");
        auditoriaCompleta.append("📌 Último Rito Publicado: ").append(ritoTipo).append(" (").append(ritoData).append(")\n\n");
        auditoriaCompleta.append("📋 Histórico de Execuções:\n").append(historicoTarefas);

        if (tvAuditLogDisplay != null) {
            tvAuditLogDisplay.setText(auditoriaCompleta.toString());
        }
    }

    private void verificarPermissaoRbac() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString("logged_user_email", "").toLowerCase();

        boolean isGestor = email.contains("admin") || email.contains("lead") || email.contains("gestor") || email.contains("carlos");

        if (isGestor) {
            cardRbacLocked.setVisibility(View.GONE);
            layoutReportsContent.setVisibility(View.VISIBLE);
        } else {
            cardRbacLocked.setVisibility(View.VISIBLE);
            layoutReportsContent.setVisibility(View.GONE);
        }
    }
}