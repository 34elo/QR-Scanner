package com.example.qr_scanner_tsd.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BarcodeRepository {

    private static final List<String> barcodes = new ArrayList<>();
    private static final Set<String> barcodeSet = new HashSet<>();

    public static boolean add(String value) {
        if (value != null && !value.isEmpty()) {
            String trimmed = value.trim();
            if (barcodeSet.contains(trimmed)) {
                return false;
            }
            barcodes.add(trimmed);
            barcodeSet.add(trimmed);
            return true;
        }
        return false;
    }

    public static void addAllowDuplicate(String value) {
        if (value != null && !value.isEmpty()) {
            barcodes.add(value.trim());
        }
    }

    public static boolean contains(String value) {
        if (value == null || value.isEmpty()) return false;
        return barcodeSet.contains(value.trim());
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
        barcodeSet.clear();
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