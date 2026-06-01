package com.example.qr_scanner_tsd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BarcodeRepository {

    private final List<String> barcodes = new ArrayList<>();
    private final Set<String> barcodeSet = new HashSet<>();

    private int trimLength = 0;

    public void setTrimLength(int length) {
        if (length >= 0) {
            this.trimLength = length;
        }
    }

    private String process(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimLength > 0 && trimmed.length() > trimLength) {
            return trimmed.substring(0, trimLength);
        }
        return trimmed;
    }

    public boolean add(String value) {
        String processed = process(value);
        if (processed == null || processed.isEmpty()) {
            return false;
        }
        if (barcodeSet.contains(processed)) {
            return false;
        }
        barcodes.add(processed);
        barcodeSet.add(processed);
        return true;
    }

    public void addAllowDuplicate(String value) {
        String processed = process(value);
        if (processed == null || processed.isEmpty()) {
            return;
        }
        barcodes.add(processed);
        barcodeSet.add(processed);
    }

    public boolean contains(String value) {
        String processed = process(value);
        if (processed == null || processed.isEmpty()) return false;
        return barcodeSet.contains(processed);
    }

    public List<Barcode> getAll() {
        List<Barcode> result = new ArrayList<>();
        for (int i = barcodes.size() - 1; i >= 0; i--) {
            result.add(new Barcode(barcodes.get(i)));
        }
        return result;
    }

    public void clear() {
        barcodes.clear();
        barcodeSet.clear();
    }

    public int getCount() {
        return barcodes.size();
    }

    public boolean isEmpty() {
        return barcodes.isEmpty();
    }

    public Barcode getLast() {
        if (barcodes.isEmpty()) return null;
        return new Barcode(barcodes.get(barcodes.size() - 1));
    }
}