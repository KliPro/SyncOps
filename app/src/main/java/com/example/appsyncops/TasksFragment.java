package com.example.appsyncops;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private TextView tvTimerDisplay, tvTimerStatus, tvCompletedHistory;
    private Button btnToggleTimer, btnFinishTask, btnAddDetailedTask;
    private EditText etTaskTitle, etTaskEstimate, etTaskPoints, etTaskGit;
    private Spinner spinnerSelectTask;

    private ArrayList<String> backlogList;
    private ArrayAdapter<String> spinnerAdapter;

    private int totalSeconds = 0;
    private int estimateMinutes = 0;
    private boolean isRunning = false;
    private boolean isOverdue = false;
    private String currentTaskName = "";

    private Handler handler;
    private Runnable timerRunnable;
    private ObjectAnimator pulseAnimator;

    private static final String PREF_NAME = "SyncOpsPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        tvTimerDisplay = view.findViewById(R.id.tvTimerDisplay);
        tvTimerStatus = view.findViewById(R.id.tvTimerStatus);
        tvCompletedHistory = view.findViewById(R.id.tvCompletedHistory);

        btnToggleTimer = view.findViewById(R.id.btnToggleTimer);
        btnFinishTask = view.findViewById(R.id.btnFinishTask);

        etTaskTitle = view.findViewById(R.id.etTaskTitle);
        etTaskEstimate = view.findViewById(R.id.etTaskEstimate);
        etTaskPoints = view.findViewById(R.id.etTaskPoints);
        etTaskGit = view.findViewById(R.id.etTaskGit);
        btnAddDetailedTask = view.findViewById(R.id.btnAddDetailedTask);

        spinnerSelectTask = view.findViewById(R.id.spinnerSelectTask);

        handler = new Handler(Looper.getMainLooper());

        // Inicializa a lista do Backlog para o Spinner
        backlogList = new ArrayList<>();
        backlogList.add("Selecione uma demanda do Backlog...");

        spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, backlogList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectTask.setAdapter(spinnerAdapter);

        spinnerSelectTask.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selected = backlogList.get(position);
                    if (!isRunning) {
                        currentTaskName = selected;
                        tvTimerStatus.setText("Foco em: " + currentTaskName);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        carregarDadosSalvos();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    totalSeconds++;
                    updateTimerDisplay();

                    if (estimateMinutes > 0 && totalSeconds > (estimateMinutes * 60) && !isOverdue) {
                        isOverdue = true;
                        dispararAlertaEstouroPrazo();
                    }

                    handler.postDelayed(this, 1000);
                }
            }
        };

        btnAddDetailedTask.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            String estStr = etTaskEstimate.getText().toString().trim();
            String pointsStr = etTaskPoints.getText().toString().trim();
            String gitStr = etTaskGit.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Informe o título da demanda!", Toast.LENGTH_SHORT).show();
                tocarSistemaFeedback("warn");
                return;
            }

            String fullTaskDescription = title;
            if (!pointsStr.isEmpty()) fullTaskDescription += " (" + pointsStr + " pts)";
            if (!gitStr.isEmpty()) fullTaskDescription += " [" + gitStr + "]";

            backlogList.add(fullTaskDescription);
            spinnerAdapter.notifyDataSetChanged();
            spinnerSelectTask.setSelection(backlogList.size() - 1);

            currentTaskName = fullTaskDescription;
            estimateMinutes = estStr.isEmpty() ? 30 : Integer.parseInt(estStr);
            totalSeconds = 0;
            isOverdue = false;

            tvTimerStatus.setText("Em Foco • " + currentTaskName);

            etTaskTitle.setText("");
            etTaskEstimate.setText("");
            etTaskPoints.setText("");
            etTaskGit.setText("");

            tocarSistemaFeedback("success");
            Toast.makeText(getContext(), "Demanda adicionada ao backlog!", Toast.LENGTH_SHORT).show();
        });

        btnToggleTimer.setOnClickListener(v -> {
            if (currentTaskName.isEmpty() || spinnerSelectTask.getSelectedItemPosition() == 0) {
                Toast.makeText(getContext(), "Cadastre ou selecione uma demanda primeiro!", Toast.LENGTH_SHORT).show();
                tocarSistemaFeedback("warn");
                return;
            }

            if (!isRunning) {
                isRunning = true;
                btnToggleTimer.setText("Pausar Foco");
                handler.post(timerRunnable);
                tocarSistemaFeedback("success");
            } else {
                isRunning = false;
                btnToggleTimer.setText("Retomar Foco");
                handler.removeCallbacks(timerRunnable);
                tocarSistemaFeedback("warn");
            }
        });

        btnFinishTask.setOnClickListener(v -> {
            if (currentTaskName.isEmpty()) return;

            isRunning = false;
            handler.removeCallbacks(timerRunnable);
            pararAlertaVisual();

            String logResultado = "✓ Concluída: " + currentTaskName + " (" + formatTime(totalSeconds) + ")";
            salvarHistoricoLocal(logResultado);

            if (getActivity() != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                // Atualiza a Daily
                prefs.edit().putString("user_yesterday_task", "⏪ O que fiz ontem: " + currentTaskName).apply();

                // Envia para Validação Pendente
                String pendenciasAntigas = prefs.getString("pending_validations", "");
                String pendenciasAtualizadas = currentTaskName + " (" + formatTime(totalSeconds) + ")\n" + pendenciasAntigas;
                prefs.edit().putString("pending_validations", pendenciasAtualizadas).apply();
            }

            int currentPos = spinnerSelectTask.getSelectedItemPosition();
            if (currentPos > 0) {
                backlogList.remove(currentPos);
                spinnerAdapter.notifyDataSetChanged();
            }

            currentTaskName = "";
            totalSeconds = 0;
            estimateMinutes = 0;
            isOverdue = false;
            spinnerSelectTask.setSelection(0);
            tvTimerStatus.setText("Selecione ou cadastre uma demanda");
            tvTimerDisplay.setText("00:00:00");
            btnToggleTimer.setText("Iniciar Foco");

            tocarSistemaFeedback("success");
            vibrarDispositivo(new long[]{0, 60, 40, 60});
            Toast.makeText(getContext(), "Demanda entregue e reportada!", Toast.LENGTH_LONG).show();
        });

        return view;
    }

    private void updateTimerDisplay() {
        tvTimerDisplay.setText(formatTime(totalSeconds));
    }

    private String formatTime(int seconds) {
        int hrs = seconds / 3600;
        int mins = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs);
    }

    private void dispararAlertaEstouroPrazo() {
        tvTimerStatus.setText("⚠️ ATENÇÃO: Tempo estourado em " + currentTaskName);
        tvTimerStatus.setTextColor(Color.RED);
        tocarSistemaFeedback("warn");
        vibrarDispositivo(new long[]{0, 100, 100, 100});

        pulseAnimator = ObjectAnimator.ofInt(tvTimerDisplay, "textColor", Color.BLACK, Color.RED);
        pulseAnimator.setDuration(500);
        pulseAnimator.setEvaluator(new ArgbEvaluator());
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.start();
    }

    private void pararAlertaVisual() {
        if (pulseAnimator != null && pulseAnimator.isRunning()) {
            pulseAnimator.cancel();
        }
        tvTimerDisplay.setTextColor(Color.BLACK);
        tvTimerStatus.setTextColor(Color.DKGRAY);
    }

    private void tocarSistemaFeedback(String type) {
        try {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_SYSTEM, 80);
            if (type.equals("success")) {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
            } else {
                toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200);
            }
        } catch (Exception e) {}
    }

    private void vibrarDispositivo(long[] pattern) {
        try {
            if (getActivity() != null) {
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    v.vibrate(pattern, -1);
                }
            }
        } catch (Exception e) {}
    }

    private void salvarHistoricoLocal(String novoLog) {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historicoAntigo = prefs.getString("history_logs", "");
        String historicoAtualizado = novoLog + "\n\n" + historicoAntigo;

        prefs.edit().putString("history_logs", historicoAtualizado).apply();
        tvCompletedHistory.setText(historicoAtualizado);
    }

    private void carregarDadosSalvos() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historicoSalvo = prefs.getString("history_logs", "Nenhuma demanda finalizada nesta sessão.");
        tvCompletedHistory.setText(historicoSalvo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(timerRunnable);
        pararAlertaVisual();
    }
}