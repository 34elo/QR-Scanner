package com.example.qr_scanner_tsd.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qr_scanner_tsd.controller.FileController;
import com.example.qr_scanner_tsd.databinding.FragmentSettingsBinding;
import com.example.qr_scanner_tsd.model.SettingsRepository;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setToolbarTitle("Настройки");
            mainActivity.setNavHeaderTitle("Настройки");
        }

        binding.etFileName.setText(SettingsRepository.getFileName());

        FileController.FileType fileType = SettingsRepository.getFileType();
        if (fileType == FileController.FileType.XLSX) {
            binding.rbXlsx.setChecked(true);
        } else {
            binding.rbCsv.setChecked(true);
        }

        int trimLength = SettingsRepository.getTrimLength();
        binding.etTrimLength.setText(trimLength > 0 ? String.valueOf(trimLength) : "");

        if (SettingsRepository.isAllowDuplicates()) {
            binding.rbDuplicatesYes.setChecked(true);
        } else {
            binding.rbDuplicatesNo.setChecked(true);
        }

        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etFileName.getText().toString().trim();
            if (name.isEmpty()) {
                name = "scan";
            }
            SettingsRepository.setFileName(name);

            FileController.FileType fileType2 = binding.rbXlsx.isChecked() 
                ? FileController.FileType.XLSX 
                : FileController.FileType.CSV;
            SettingsRepository.setFileType(fileType2);

            String trimStr = binding.etTrimLength.getText().toString().trim();
            int trimLen = 0;
            if (!trimStr.isEmpty()) {
                try {
                    trimLen = Integer.parseInt(trimStr);
                    if (trimLen < 0) trimLen = 0;
                } catch (NumberFormatException e) {
                    trimLen = 0;
                }
            }
            SettingsRepository.setTrimLength(trimLen);

            boolean allowDuplicates = binding.rbDuplicatesYes.isChecked();
            SettingsRepository.setAllowDuplicates(allowDuplicates);

            Toast.makeText(requireContext(), "Сохранено", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}