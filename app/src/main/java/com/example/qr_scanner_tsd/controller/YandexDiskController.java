package com.example.qr_scanner_tsd.controller;

import android.os.Handler;
import android.os.Looper;

import com.example.qr_scanner_tsd.model.Barcode;
import com.example.qr_scanner_tsd.model.SettingsRepository;
import com.google.gson.Gson;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class YandexDiskController {

    private static final String BASE_URL = "https://cloud-api.yandex.net/v1/disk/resources";
    private static final MediaType MEDIA_TYPE_OCTET = MediaType.parse("application/octet-stream");
    private static final MediaType MEDIA_TYPE_XLSX = MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private static final Gson GSON = new Gson();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setName("YandexDisk-" + t.getId());
        return t;
    });

    public interface UploadListener {
        void onSuccess(String fileName, String remotePath);
        void onProgress(int percent);
        void onError(String message);
    }

    public interface ConnectionTestListener {
        void onSuccess(long usedSpace, long totalSpace);
        void onError(String message);
    }

    public static void uploadFile(List<Barcode> barcodes, UploadListener listener) {
        FileController.FileType fileType = SettingsRepository.getFileType();
        uploadFile(barcodes, fileType, listener);
    }

    public static void uploadFile(List<Barcode> barcodes, FileController.FileType fileType, UploadListener listener) {
        if (listener == null) {
            return;
        }
        if (barcodes == null || barcodes.isEmpty()) {
            listener.onError("Список штрихкодов пуст");
            return;
        }

        String token = SettingsRepository.getYandexToken();
        if (token == null || token.isEmpty()) {
            listener.onError("Токен Яндекс.Диска не настроен");
            return;
        }

        byte[] data;
        String extension;
        MediaType mediaType;

        if (fileType == FileController.FileType.XLSX) {
            data = generateXlsxData(barcodes);
            extension = ".xlsx";
            mediaType = MEDIA_TYPE_XLSX;
        } else {
            StringBuilder sb = new StringBuilder();
            for (Barcode code : barcodes) {
                if (code != null && code.getValue() != null) {
                    sb.append(code.getValue()).append("\n");
                }
            }
            data = sb.toString().getBytes(StandardCharsets.UTF_8);
            extension = ".csv";
            mediaType = MEDIA_TYPE_OCTET;
        }

        String fileName = generateFileName();
        String remotePath = "/QRScanner/" + fileName + extension;

        new UploadOperation(token, remotePath, data, fileName, mediaType, listener).start();
    }

    private static byte[] generateXlsxData(List<Barcode> barcodes) {
        if (barcodes == null || barcodes.isEmpty()) {
            return new byte[0];
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Barcodes");

            int rowNum = 0;
            for (Barcode code : barcodes) {
                if (code != null && code.getValue() != null) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(code.getValue());
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateFileName() {
        String prefix = SettingsRepository.getFileName();
        if (prefix == null || prefix.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy_HH.mm", Locale.getDefault());
            return "scan_" + sdf.format(new Date());
        }
        return prefix;
    }

    public static void testConnection(String token, ConnectionTestListener listener) {
        if (token == null || token.isEmpty()) {
            listener.onError("Токен не указан");
            return;
        }
        new TestConnectionOperation(token, listener).start();
    }

    private static class UploadOperation {
        private final String token;
        private final String remotePath;
        private final byte[] data;
        private final String fileName;
        private final MediaType mediaType;
        private final UploadListener listener;

        UploadOperation(String token, String remotePath, byte[] data, String fileName, MediaType mediaType, UploadListener listener) {
            this.token = token;
            this.remotePath = remotePath;
            this.data = data;
            this.fileName = fileName;
            this.mediaType = mediaType;
            this.listener = listener;
        }

        void start() {
            postProgress(0);
            EXECUTOR.execute(() -> {
                try {
                    ensureFolderExists();
                    String uploadUrl = getUploadUrl();
                    if (uploadUrl == null) {
                        postError("Не удалось получить URL загрузки");
                        return;
                    }
                    postProgress(50);
                    uploadToUrl(uploadUrl);
                } catch (Exception e) {
                    postError(e.getMessage());
                }
            });
        }

        private void ensureFolderExists() throws IOException {
            String encodedPath = java.net.URLEncoder.encode("/QRScanner", "UTF-8");
            String url = BASE_URL + "?path=" + encodedPath;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "OAuth " + token)
                    .addHeader("Accept", "application/json")
                    .put(RequestBody.create(new byte[0], MEDIA_TYPE_OCTET))
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                // folder created or already exists
            }
        }

        private String getUploadUrl() throws IOException {
            String encodedPath = java.net.URLEncoder.encode(remotePath, "UTF-8");
            String url = BASE_URL + "/upload?path=" + encodedPath;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "OAuth " + token)
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return null;
                }
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> json = GSON.fromJson(body, Map.class);
                return (String) json.get("href");
            }
        }

        private void uploadToUrl(String uploadUrl) throws IOException {
            RequestBody body = RequestBody.create(data, mediaType);
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .put(body)
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                postProgress(100);
                if (response.isSuccessful()) {
                    postSuccess(fileName, remotePath);
                } else {
                    postError("Ошибка загрузки: " + response.code());
                }
            }
        }

        private void postProgress(int percent) {
            MAIN_HANDLER.post(() -> listener.onProgress(percent));
        }

        private void postSuccess(String fileName, String filePath) {
            MAIN_HANDLER.post(() -> listener.onSuccess(fileName, filePath));
        }

        private void postError(String message) {
            MAIN_HANDLER.post(() -> listener.onError(message));
        }
    }

    private static class TestConnectionOperation {
        private final String token;
        private final ConnectionTestListener listener;

        TestConnectionOperation(String token, ConnectionTestListener listener) {
            this.token = token;
            this.listener = listener;
        }

        void start() {
            EXECUTOR.execute(() -> {
                try {
                    Request request = new Request.Builder()
                            .url(BASE_URL)
                            .addHeader("Authorization", "OAuth " + token)
                            .addHeader("Accept", "application/json")
                            .get()
                            .build();

                    try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            postError("Ошибка подключения: " + response.code());
                            return;
                        }

                        String body = response.body() != null ? response.body().string() : "";
                        Map<String, Object> json = GSON.fromJson(body, Map.class);

                        Double used = (Double) json.get("used_space");
                        Double total = (Double) json.get("total_space");

                        postSuccess(
                                used != null ? used.longValue() : 0,
                                total != null ? total.longValue() : 0
                        );
                    }
                } catch (Exception e) {
                    postError(e.getMessage());
                }
            });
        }

        private void postSuccess(long usedSpace, long totalSpace) {
            MAIN_HANDLER.post(() -> listener.onSuccess(usedSpace, totalSpace));
        }

        private void postError(String message) {
            MAIN_HANDLER.post(() -> listener.onError(message));
        }
    }
}