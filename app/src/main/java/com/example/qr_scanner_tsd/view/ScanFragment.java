package com.example.qr_scanner_tsd.view;

import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.qr_scanner_tsd.App;
import com.example.qr_scanner_tsd.controller.FileController;
import com.example.qr_scanner_tsd.controller.ScannerController;
import com.example.qr_scanner_tsd.controller.YandexDiskController;
import com.example.qr_scanner_tsd.databinding.FragmentScanBinding;
import com.example.qr_scanner_tsd.model.Barcode;
import com.example.qr_scanner_tsd.model.SettingsRepository;

public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;
    private ScannerController controller;
    private BarcodeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setToolbarTitle("Сканирование");
            mainActivity.setNavHeaderTitle("Сканирование");
        }

        controller = App.getInstance().getScannerController();
        var repository = App.getInstance().getBarcodeRepository();
        repository.setTrimLength(SettingsRepository.getTrimLength());

        adapter = new BarcodeAdapter();
        binding.rvBarcodes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBarcodes.setAdapter(adapter);

        adapter.setAll(repository.getAll());

        controller.setListener(this::onScan);

        binding.btnSave.setOnClickListener(v -> saveToFile());
        binding.btnUpload.setOnClickListener(v -> uploadToYandexDisk());
        binding.btnClear.setOnClickListener(v -> clearAll());

        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        controller.start(requireActivity());
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        controller.stop(requireActivity());
    }

    private void onScan(String barcode) {
        var repository = App.getInstance().getBarcodeRepository();
        boolean allowDuplicates = SettingsRepository.isAllowDuplicates();
        boolean added;

        if (allowDuplicates) {
            repository.addAllowDuplicate(barcode);
            added = true;
        } else {
            added = repository.add(barcode);
        }

        if (added) {
            adapter.add(repository.getLast());
        } else {
            vibrateDuplicate();
            Toast.makeText(requireContext(), "Уже есть в списке", Toast.LENGTH_SHORT).show();
        }
        updateUI();
    }

    private void updateUI() {
        var repository = App.getInstance().getBarcodeRepository();
        binding.tvScanCount.setText(String.valueOf(repository.getCount()));
    }

    private void vibrateDuplicate() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) requireContext().getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }
    }

    private void saveToFile() {
        var repository = App.getInstance().getBarcodeRepository();
        if (repository.isEmpty()) {
            Toast.makeText(requireContext(), "Нечего сохранять", Toast.LENGTH_SHORT).show();
            return;
        }

        FileController.FileType fileType = SettingsRepository.getFileType();
        FileController.saveToDocuments(requireContext(), repository.getAll(), fileType, new FileController.SaveListener() {
            @Override
            public void onSuccess(String filePath) {
                Toast.makeText(requireContext(), "Сохранено: " + filePath, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), "Ошибка: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadToYandexDisk() {
        var repository = App.getInstance().getBarcodeRepository();
        if (repository.isEmpty()) {
            Toast.makeText(requireContext(), "Нечего выгружать", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnUpload.setEnabled(false);
        binding.btnUpload.setText("Загрузка...");

        YandexDiskController.uploadFile(repository.getAll(), new YandexDiskController.UploadListener() {
            @Override
            public void onSuccess(String fileName, String remotePath) {
                if (binding == null) return;
                binding.btnUpload.setEnabled(true);
                binding.btnUpload.setText("Выгрузить на диск");
                Toast.makeText(requireContext(), "Загружено: " + fileName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int percent) {
                if (binding == null) return;
                binding.btnUpload.setText("Загрузка " + percent + "%");
            }

            @Override
            public void onError(String message) {
                if (binding == null) return;
                binding.btnUpload.setEnabled(true);
                binding.btnUpload.setText("Выгрузить на диск");
                Toast.makeText(requireContext(), "Ошибка: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearAll() {
        var repository = App.getInstance().getBarcodeRepository();
        if (repository.isEmpty()) {
            Toast.makeText(requireContext(), "Список пуст", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Очистка")
                .setMessage("Очистить все отсканированные коды?")
                .setPositiveButton("Да", (dialog, which) -> {
                    repository.clear();
                    adapter.clear();
                    updateUI();
                    Toast.makeText(requireContext(), "Очищено", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}