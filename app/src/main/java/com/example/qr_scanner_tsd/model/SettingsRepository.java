package com.example.qr_scanner_tsd.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.qr_scanner_tsd.controller.FileController;

public class SettingsRepository {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_FILE_TYPE = "file_type";

    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getFileName() {
        return prefs.getString(KEY_FILE_NAME, "scan");
    }

    public static void setFileName(String name) {
        prefs.edit().putString(KEY_FILE_NAME, name).apply();
    }

    public static FileController.FileType getFileType() {
        String type = prefs.getString(KEY_FILE_TYPE, "CSV");
        try {
            return FileController.FileType.valueOf(type);
        } catch (Exception e) {
            return FileController.FileType.CSV;
        }
    }

    public static void setFileType(FileController.FileType type) {
        prefs.edit().putString(KEY_FILE_TYPE, type.name()).apply();
    }
}