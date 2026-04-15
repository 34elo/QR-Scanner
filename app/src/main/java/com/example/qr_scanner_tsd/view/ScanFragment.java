package com.example.qr_scanner_tsd.view;

import android.os.Bundle;
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
import com.example.qr_scanner_tsd.model.BarcodeRepository;
import com.example.qr_scanner_tsd.model.SettingsRepository;

import java.io.File;

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

        adapter = new BarcodeAdapter();
        binding.rvBarcodes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBarcodes.setAdapter(adapter);

        for (Barcode barcode : BarcodeRepository.getAll()) {
            adapter.add(barcode);
        }

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
        int trimLength = SettingsRepository.getTrimLength();
        if (trimLength > 0 && barcode.length() > trimLength) {
            barcode = barcode.substring(0, trimLength);
        }

        boolean allowDuplicates = SettingsRepository.isAllowDuplicates();
        boolean added;

        if (allowDuplicates) {
            BarcodeRepository.addAllowDuplicate(barcode);
            added = true;
        } else {
            added = BarcodeRepository.add(barcode);
        }

        if (added) {
            adapter.add(BarcodeRepository.getLast());
        } else {
            Toast.makeText(requireContext(), "Уже есть в списке", Toast.LENGTH_SHORT).show();
        }
        updateUI();
    }

    private void updateUI() {
        binding.tvScanCount.setText(String.valueOf(BarcodeRepository.getCount()));
    }

    private void saveToFile() {
        if (BarcodeRepository.isEmpty()) {
            Toast.makeText(requireContext(), "Нечего сохранять", Toast.LENGTH_SHORT).show();
            return;
        }

        FileController.FileType fileType = SettingsRepository.getFileType();
        FileController.saveToDocuments(requireContext(), BarcodeRepository.getAll(), fileType, new FileController.SaveListener() {
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
        if (BarcodeRepository.isEmpty()) {
            Toast.makeText(requireContext(), "Нечего выгружать", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnUpload.setEnabled(false);
        binding.btnUpload.setText("Загрузка...");

        FileController.FileType fileType = SettingsRepository.getFileType();
        FileController.saveToDocuments(requireContext(), BarcodeRepository.getAll(), fileType, new FileController.SaveListener() {
            @Override
            public void onSuccess(String filePath) {
                uploadToYandexDiskAndDelete(filePath);
            }

            @Override
            public void onError(String message) {
                binding.btnUpload.setEnabled(true);
                binding.btnUpload.setText("Выгрузить на диск");
                Toast.makeText(requireContext(), "Ошибка сохранения: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadToYandexDiskAndDelete(String filePath) {
        YandexDiskController.uploadFile(BarcodeRepository.getAll(), new YandexDiskController.UploadListener() {
            @Override
            public void onSuccess(String fileName, String remotePath) {
                deleteLocalFile(filePath);
                binding.btnUpload.setEnabled(true);
                binding.btnUpload.setText("Выгрузить на диск");
                Toast.makeText(requireContext(), "Загружено: " + fileName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int percent) {
                binding.btnUpload.setText("Загрузка " + percent + "%");
            }

            @Override
            public void onError(String message) {
                binding.btnUpload.setEnabled(true);
                binding.btnUpload.setText("Выгрузить на диск");
                Toast.makeText(requireContext(), "Ошибка: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteLocalFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearAll() {
        if (BarcodeRepository.isEmpty()) {
            Toast.makeText(requireContext(), "Список пуст", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Очистка")
                .setMessage("Очистить все отсканированные коды?")
                .setPositiveButton("Да", (dialog, which) -> {
                    BarcodeRepository.clear();
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