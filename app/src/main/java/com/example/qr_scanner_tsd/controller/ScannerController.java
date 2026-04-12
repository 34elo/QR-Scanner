package com.example.qr_scanner_tsd.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScannerController {

    public static final String SCAN_ACTION = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST";
    public static final String EXTRA_BARCODE_DATA = "EXTRA_BARCODE_DECODING_DATA";
    public static final String EXTRA_SYMBOLOGY = "EXTRA_BARCODE_DECODING_SYMBOLE";

    public interface Listener {
        void onScan(String barcode);
    }

    private final Context context;
    private Listener listener;
    private BarcodeReceiver barcodeReceiver;

    public ScannerController(Context context) {
        this.context = context;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void start(Activity activity) {
        if (barcodeReceiver == null) {
            barcodeReceiver = new BarcodeReceiver();
            IntentFilter filter = new IntentFilter(SCAN_ACTION);
            activity.registerReceiver(barcodeReceiver, filter, Context.RECEIVER_EXPORTED);
        }
    }

    public void stop(Activity activity) {
        if (barcodeReceiver != null) {
            try {
                activity.unregisterReceiver(barcodeReceiver);
            } catch (Exception ignored) {
            }
            barcodeReceiver = null;
        }
    }

    public void simulateScan() {
        if (listener != null) {
            listener.onScan("TEST_" + System.currentTimeMillis());
        }
    }

    private class BarcodeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SCAN_ACTION.equals(intent.getAction())) {
                String barcode = intent.getStringExtra(EXTRA_BARCODE_DATA);
                if (barcode != null && listener != null) {
                    listener.onScan(barcode);
                }
            }
        }
    }
}