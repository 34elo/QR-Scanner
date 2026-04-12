package com.example.qr_scanner_tsd;

import android.app.Application;

import com.example.qr_scanner_tsd.controller.ScannerController;
import com.example.qr_scanner_tsd.model.SettingsRepository;

public class App extends Application {

    private static App instance;
    private ScannerController scannerController;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        scannerController = new ScannerController(this);
        SettingsRepository.init(this);
    }

    public static App getInstance() {
        return instance;
    }

    public ScannerController getScannerController() {
        return scannerController;
    }
}