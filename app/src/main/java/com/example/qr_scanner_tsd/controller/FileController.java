package com.example.qr_scanner_tsd.controller;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.example.qr_scanner_tsd.model.Barcode;
import com.example.qr_scanner_tsd.model.SettingsRepository;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileController {

    public enum FileType {
        CSV, XLSX
    }

    public interface SaveListener {
        void onSuccess(String filePath);
        void onError(String message);
    }

    public static void saveToDocuments(Context context, List<Barcode> barcodes, FileType fileType, SaveListener listener) {
        if (barcodes == null || barcodes.isEmpty()) {
            listener.onError("Список пуст");
            return;
        }

        String fileName = generateFileName();

        if (fileType == FileType.CSV) {
            saveCsv(context, barcodes, fileName + ".csv", listener);
        } else if (fileType == FileType.XLSX) {
            saveXlsx(context, barcodes, fileName + ".xlsx", listener);
        }
    }

    public static void share(Context context, List<Barcode> barcodes, FileType fileType) {
        if (barcodes == null || barcodes.isEmpty()) {
            return;
        }

        String fileName = generateFileName();

        if (fileType == FileType.CSV) {
            shareCsv(context, barcodes, fileName + ".csv");
        } else if (fileType == FileType.XLSX) {
            shareXlsx(context, barcodes, fileName + ".xlsx");
        }
    }

    private static String generateFileName() {
        String prefix = SettingsRepository.getFileName();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "scan";
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy_HH.mm", Locale.getDefault());
            prefix = prefix + "_" + sdf.format(new Date());
        }
        return prefix;
    }

    private static void saveCsv(Context context, List<Barcode> barcodes, String fileName, SaveListener listener) {
        StringBuilder sb = new StringBuilder();
        for (Barcode code : barcodes) {
            sb.append(code.getValue()).append("\n");
        }
        byte[] data = sb.toString().getBytes();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, fileName, "text/csv", data, listener);
        } else {
            saveViaFile(context, fileName, data, listener);
        }
    }

    private static void shareCsv(Context context, List<Barcode> barcodes, String fileName) {
        StringBuilder sb = new StringBuilder();
        for (Barcode code : barcodes) {
            sb.append(code.getValue()).append("\n");
        }

        try {
            File cacheDir = new File(context.getCacheDir(), "shared");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File file = new File(cacheDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(sb.toString().getBytes());
            }

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Отправить файл"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveViaMediaStore(Context context, String fileName, String mimeType, byte[] data, SaveListener listener) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/QRScanner");

        Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
        if (uri == null) {
            listener.onError("Не удалось создать файл");
            return;
        }

        try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
            os.write(data);
            os.flush();
            listener.onSuccess(fileName);
        } catch (Exception e) {
            context.getContentResolver().delete(uri, null, null);
            listener.onError(e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    private static void saveViaFile(Context context, String fileName, byte[] data, SaveListener listener) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "QRScanner");
        if (!dir.exists() && !dir.mkdirs()) {
            listener.onError("Не удалось создать папку");
            return;
        }

        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.flush();
            listener.onSuccess(file.getAbsolutePath());
        } catch (Exception e) {
            listener.onError(e.getMessage());
        }
    }

    private static void saveXlsx(Context context, List<Barcode> barcodes, String fileName, SaveListener listener) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Barcodes");
            int rowNum = 0;
            for (Barcode code : barcodes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(code.getValue());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            byte[] data = baos.toByteArray();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveViaMediaStore(context, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", data, listener);
            } else {
                saveViaFile(context, fileName, data, listener);
            }
        } catch (Exception e) {
            listener.onError(e.getMessage());
        }
    }

    private static void shareXlsx(Context context, List<Barcode> barcodes, String fileName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Barcodes");
            int rowNum = 0;
            for (Barcode code : barcodes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(code.getValue());
            }

            File cacheDir = new File(context.getCacheDir(), "shared");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File file = new File(cacheDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Отправить файл"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}