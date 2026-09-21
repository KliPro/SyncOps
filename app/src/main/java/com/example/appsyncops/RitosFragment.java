package com.example.appsyncops;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import androidx.fragment.app.Fragment;

public class RitosFragment extends Fragment {

    private Spinner spinnerTipoRito;
    private EditText etDataRito, etHoraRito, etLinkVideo;
    private Button btnSalvarAgendamento, btnEntrarReuniao;
    private TextView tvProximoRitoInfo, tvTituloPainelRitos;
    private LinearLayout layoutMasterAgendamento;
    private static final String PREF_NAME = "SyncOpsPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agendar_rito, container, false);

        spinnerTipoRito = view.findViewById(R.id.spinnerTipoRito);
        etDataRito = view.findViewById(R.id.etDataRito);
        etHoraRito = view.findViewById(R.id.etHoraRito);
        etLinkVideo = view.findViewById(R.id.etLinkVideo);
        btnSalvarAgendamento = view.findViewById(R.id.btnSalvarAgendamento);

        tvProximoRitoInfo = view.findViewById(R.id.tvProximoRitoInfo);
        tvTituloPainelRitos = view.findViewById(R.id.tvTituloPainelRitos);
        btnEntrarReuniao = view.findViewById(R.id.btnEntrarReuniao);
        layoutMasterAgendamento = view.findViewById(R.id.layoutMasterAgendamento);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.ritos_agile_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoRito.setAdapter(adapter);

        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isMaster = prefs.getBoolean("is_scrum_master", false);

        if (isMaster) {
            if (layoutMasterAgendamento != null) layoutMasterAgendamento.setVisibility(View.VISIBLE);
            if (tvTituloPainelRitos != null) tvTituloPainelRitos.setText("📅 Painel do Master: Agendar e Gerenciar Ritos");
        } else {
            if (layoutMasterAgendamento != null) layoutMasterAgendamento.setVisibility(View.GONE);
            if (tvTituloPainelRitos != null) tvTituloPainelRitos.setText("📅 Próximos Ritos Agendados (Equipe)");
            carregarRitoParaMembro(prefs);
        }

        btnSalvarAgendamento.setOnClickListener(v -> {
            String tipoRito = spinnerTipoRito.getSelectedItem().toString();
            String data = etDataRito.getText().toString().trim();
            String hora = etHoraRito.getText().toString().trim();
            String link = etLinkVideo.getText().toString().trim();

            if (data.isEmpty() || hora.isEmpty() || link.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString("rito_tipo", tipoRito)
                    .putString("rito_data_hora", data + " às " + hora)
                    .putString("rito_link", link)
                    .apply();

            Toast.makeText(getContext(), "Rito agendado e publicado para o time!", Toast.LENGTH_SHORT).show();

            etDataRito.setText("");
            etHoraRito.setText("");
            etLinkVideo.setText("");
        });

        return view;
    }

    private void carregarRitoParaMembro(SharedPreferences prefs) {
        String tipo = prefs.getString("rito_tipo", "🚀 Sprint Planning (Planejamento)");
        String dataHora = prefs.getString("rito_data_hora", "05/09/2026 às 10:00");
        String link = prefs.getString("rito_link", "https://meet.google.com/abc-defg-hij");

        if (tvProximoRitoInfo != null) {
            tvProximoRitoInfo.setText(tipo + "\n🕒 Data: " + dataHora);
        }

        if (btnEntrarReuniao != null) {
            btnEntrarReuniao.setOnClickListener(v -> {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Link de videochamada inválido.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}