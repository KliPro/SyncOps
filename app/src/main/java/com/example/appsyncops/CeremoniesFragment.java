package com.example.appsyncops;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CeremoniesFragment extends Fragment {

    private EditText etMeetingType, etMeetingLink;
    private Button btnScheduleMeeting;
    private TextView tvMeetingMinutesList;

    private static final String PREF_NAME = "SyncOpsPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ceremonies, container, false);

        etMeetingType = view.findViewById(R.id.etMeetingType);
        etMeetingLink = view.findViewById(R.id.etMeetingLink);
        btnScheduleMeeting = view.findViewById(R.id.btnScheduleMeeting);
        tvMeetingMinutesList = view.findViewById(R.id.tvMeetingMinutesList);

        carregarAtasSalvas();

        btnScheduleMeeting.setOnClickListener(v -> {
            String tipo = etMeetingType.getText().toString().trim();
            String link = etMeetingLink.getText().toString().trim();

            if (tipo.isEmpty()) {
                Toast.makeText(getContext(), "Informe o tipo da cerimônia!", Toast.LENGTH_SHORT).show();
                return;
            }

            String registro = "📅 " + tipo + (link.isEmpty() ? "" : " • Link: " + link);
            salvarAtaLocal(registro);

            etMeetingType.setText("");
            etMeetingLink.setText("");
            Toast.makeText(getContext(), "Cerimônia registrada com sucesso!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void salvarAtaLocal(String novoRegistro) {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String anterior = prefs.getString("meetings_log", "");
        String atualizado = novoRegistro + "\n\n" + anterior;

        prefs.edit().putString("meetings_log", atualizado).apply();
        tvMeetingMinutesList.setText(atualizado);
    }

    private void carregarAtasSalvas() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String salvo = prefs.getString("meetings_log", "Nenhuma ata ou agendamento registrado nesta sessão.");
        tvMeetingMinutesList.setText(salvo);
    }
}