package com.example.appsyncops;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TeamFragment extends Fragment {

    private TextView tvUserYesterdayTask;
    private static final String PREF_NAME = "SyncOpsPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);

        // Vincula corretamente o TextView do card no XML
        // Substitua R.id.tvUserYesterday pelo ID exato que está no seu fragment_team.xml
        tvUserYesterdayTask = view.findViewById(R.id.tvUserYesterday);

        carregarReportDiario();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarReportDiario();
    }

    private void carregarReportDiario() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Recupera a última tarefa concluída salva pelo TasksFragment
        String ultimaEntrega = prefs.getString("user_yesterday_task", "⏪ O que fiz ontem: Nenhuma demanda reportada ainda.");

        if (tvUserYesterdayTask != null) {
            tvUserYesterdayTask.setText(ultimaEntrega);
        }
    }
}