package com.example.qr_scanner_tsd.model;

import java.util.ArrayList;
import java.util.List;

public class BarcodeRepository {

    private static final List<String> barcodes = new ArrayList<>();

    public static boolean add(String value) {
        if (value != null && !value.isEmpty()) {
            String trimmed = value.trim();
            for (String s : barcodes) {
                if (s.equals(trimmed)) {
                    return false;
                }
            }
            barcodes.add(trimmed);
            return true;
        }
        return false;
    }

    public static boolean contains(String value) {
        if (value == null || value.isEmpty()) return false;
        String trimmed = value.trim();
        for (String s : barcodes) {
            if (s.equals(trimmed) == true) {
                return true;
            }
        }
        return false;
    }

    public static List<Barcode> getAll() {
        List<Barcode> result = new ArrayList<>();
        for (String s : barcodes) {
            result.add(new Barcode(s));
        }
        return result;
    }

    public static void clear() {
        barcodes.clear();
    }

    public static int getCount() {
        return barcodes.size();
    }

    public static boolean isEmpty() {
        return barcodes.isEmpty();
    }

    public static Barcode getLast() {
        if (barcodes.isEmpty()) return null;
        return new Barcode(barcodes.get(barcodes.size() - 1));
    }
}