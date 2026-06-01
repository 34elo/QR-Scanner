package com.example.qr_scanner_tsd.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.qr_scanner_tsd.BuildConfig;
import com.example.qr_scanner_tsd.controller.FileController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsRepository {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_FILE_TYPE = "file_type";
    private static final String KEY_TRIM_LENGTH = "trim_length";
    private static final String KEY_ALLOW_DUPLICATES = "allow_duplicates";

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

    public static int getTrimLength() {
        return prefs.getInt(KEY_TRIM_LENGTH, 0);
    }

    public static void setTrimLength(int length) {
        prefs.edit().putInt(KEY_TRIM_LENGTH, length).apply();
    }

    public static boolean isAllowDuplicates() {
        return prefs.getBoolean(KEY_ALLOW_DUPLICATES, false);
    }

    public static void setAllowDuplicates(boolean allow) {
        prefs.edit().putBoolean(KEY_ALLOW_DUPLICATES, allow).apply();
    }

    public static String getYandexToken() {
        return BuildConfig.YANDEX_TOKEN;
    }

    public static String buildOutputFileName() {
        String prefix = getFileName();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "scan";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy_HH.mm.ss", Locale.getDefault());
        return prefix + "_" + sdf.format(new Date());
    }
}