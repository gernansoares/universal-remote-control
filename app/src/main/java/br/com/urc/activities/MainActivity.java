package br.com.urc.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import br.com.urc.R;

public class MainActivity extends AppCompatActivity {

    private final String NEEDED_PERMISSION = Manifest.permission.NEARBY_WIFI_DEVICES;

    private LinearLayout permissionLayout;
    private LinearLayout iconLayout;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeObjects();
        registerPermissionLauncher();
        requirePermissions();
    }

    private void initializeObjects() {
        permissionLayout = findViewById(R.id.permissionLayout);
        iconLayout = findViewById(R.id.iconLayout);
        findViewById(R.id.permissionsButton).setOnClickListener(view -> {
            requirePermissions();
        });
    }

    private void registerPermissionLauncher() {
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        this.iconLayout.setVisibility(View.VISIBLE);
                        this.permissionLayout.setVisibility(View.GONE);
                        startListDevices();
                    } else {
                        this.permissionLayout.setVisibility(View.VISIBLE);
                        this.iconLayout.setVisibility(View.GONE);
                        showPermissionDeniedToast();
                    }
                });
    }

    private void requirePermissions() {
        if (ContextCompat.checkSelfPermission(this, NEEDED_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            startListDevices();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, NEEDED_PERMISSION)) {
                showRequestPermissionDialog();
            } else {
                requestPermissionLauncher.launch(NEEDED_PERMISSION);
            }
        }
    }

    private void showRequestPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.provide_permissions)
                .setMessage(R.string.msg_permissions)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    requestPermissionLauncher.launch(NEEDED_PERMISSION);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    showPermissionDeniedToast();
                })
                .show();
    }

    private void showPermissionDeniedToast() {
        Toast.makeText(this, R.string.msg_permissions_missing, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startListDevices() {
        Intent listDevices = new Intent(this, ListDevicesActivitySdk.class);
        startActivity(listDevices);
    }
}