package com.example.qr_scanner_tsd.view;

import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qr_scanner_tsd.App;
import com.example.qr_scanner_tsd.controller.ScannerController;
import com.example.qr_scanner_tsd.databinding.FragmentCheckBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class CheckFragment extends Fragment {

    private FragmentCheckBinding binding;
    private ScannerController controller;
    private Set<String> loadedCodes = new HashSet<>();

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::onFileSelected
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setToolbarTitle("Проверка");
            mainActivity.setNavHeaderTitle("Проверка");
        }

        controller = App.getInstance().getScannerController();
        controller.setListener(this::onScan);

        binding.btnSelectFile.setOnClickListener(v -> selectFile());
        binding.btnClearCodes.setOnClickListener(v -> clearCodes());

        view.post(() -> {
            binding.dummyFocus.requestFocus();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        controller.start(requireActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        controller.stop(requireActivity());
    }

    private void selectFile() {
        filePickerLauncher.launch("text/*");
    }

    private void onFileSelected(Uri uri) {
        if (uri == null) {
            return;
        }

        loadedCodes.clear();

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(requireContext().getContentResolver().openInputStream(uri), StandardCharsets.UTF_8)
            );

            String fileName = getFileName(uri);
            binding.tvFileName.setText(fileName != null ? fileName : "файл");

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String code = line;
                    int commaIndex = line.indexOf(',');
                    if (commaIndex > 0) {
                        code = line.substring(0, commaIndex).trim();
                    }
                    if (!code.isEmpty()) {
                        loadedCodes.add(code);
                    }
                }
            }
            reader.close();

            updateLoadedCodesCount();
            Toast.makeText(requireContext(), "Загружено " + loadedCodes.size() + " кодов", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Ошибка чтения файла: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String normalizeCode(String code) {
        return code.trim();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private void updateLoadedCodesCount() {
        binding.tvCodesCount.setText("Загружено кодов: " + loadedCodes.size());
    }

    private void clearCodes() {
        loadedCodes.clear();
        binding.tvFileName.setText("Файл не выбран");
        binding.tvCodesCount.setText("Загружено кодов: 0");
        binding.tvLastScanned.setText("Код: -");
        binding.tvCheckResult.setText("");
        binding.tvCheckResult.setTextColor(requireContext().getColor(android.R.color.black));

        Toast.makeText(requireContext(), "Коды очищены", Toast.LENGTH_SHORT).show();
    }

    private void onScan(String barcode) {
        String normalized = normalizeCode(barcode);

        binding.tvLastScanned.setText("Код: " + normalized);

        if (loadedCodes.isEmpty()) {
            binding.tvCheckResult.setText("Сначала загрузите файл");
            binding.tvCheckResult.setTextColor(requireContext().getColor(android.R.color.darker_gray));
            return;
        }

        boolean found = false;
        for (String loadedCode : loadedCodes) {
            if (loadedCode.equals(normalized)) {
                found = true;
                break;
            }
        }

        if (found) {
            binding.tvCheckResult.setText("НАЙДЕН");
            binding.tvCheckResult.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvCheckResult.setText("НЕ НАЙДЕН");
            binding.tvCheckResult.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}