package com.example.qr_scanner_tsd.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.qr_scanner_tsd.App;
import com.example.qr_scanner_tsd.R;
import com.example.qr_scanner_tsd.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private TextView tvHeaderTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        View headerView = binding.navigationView.getHeaderView(0);
        tvHeaderTitle = headerView.findViewById(R.id.tvHeaderTitle);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.nav_open, R.string.nav_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navigationView.setNavigationItemSelectedListener(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (savedInstanceState == null) {
            setToolbarTitle("Сканирование");
            setNavHeaderTitle("Сканирование");
            loadFragment(new ScanFragment());
        }
    }

    private void updateHeaderTitle(String title) {
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(title);
        }
    }

    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void setNavHeaderTitle(String title) {
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(title);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            loadFragment(new ScanFragment());
            setToolbarTitle("Сканирование");
            setNavHeaderTitle("Сканирование");
        } else if (id == R.id.nav_settings) {
            loadFragment(new SettingsFragment());
            setToolbarTitle("Настройки");
            setNavHeaderTitle("Настройки");
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getInstance().getScannerController().stop(this);
    }
}