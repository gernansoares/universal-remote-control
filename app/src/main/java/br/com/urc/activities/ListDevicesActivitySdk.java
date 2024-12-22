package br.com.urc.activities;

import static br.com.urc.common.Contants.DEVICE_ID_EXTRA_NAME;
import static br.com.urc.common.Contants.DEVICE_IP_EXTRA_NAME;
import static br.com.urc.common.Contants.LOG_TAG;
import static br.com.urc.common.Contants.MANUFACTURER_ID_EXTRA_NAME;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.urc.R;
import br.com.urc.detection.DeviceLister;
import br.com.urc.detection.DeviceSource;
import br.com.urc.detection.SamsungDeviceSource;
import br.com.urc.model.Device;
import br.com.urc.view.adapters.ListDeviceAdapterSdk;

public class ListDevicesActivitySdk extends DeviceLister {

    private final Long SEARCH_DURATION = (long) (1000 * 10);
    private final Boolean LOCK = true;
    private Boolean discovering;
    private List<Device> devices;
    private RecyclerView devicesListRV;
    private MenuItem refresh;
    private List<DeviceSource> sources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_devices);
        initializeObjects();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        refresh = menu.findItem(R.id.refresh);
        View actionView = refresh.getActionView();
        actionView.setOnClickListener(v -> onOptionsItemSelected(refresh));
        actionView.performClick();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public synchronized void addDevice(Device device) {
        synchronized (LOCK) {
            if (!devices.contains(device)) {
                devices.add(device);
                loadDevices();
            }
        }
    }

    @Override
    public void removeDevice(Device device) {
        synchronized (LOCK) {
            devices.remove(device);
            loadDevices();
        }
    }

    private void rotateMenuItemIcon(MenuItem item) {
        View actionView = item.getActionView();
        ImageView icon = actionView.findViewById(R.id.icon);

        RotateAnimation rotate = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(800);
        rotate.setRepeatCount(Animation.INFINITE);

        icon.startAnimation(rotate);

        actionView.postDelayed(icon::clearAnimation, 20000);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item != null) {
            int id = item.getItemId();
            if (id == R.id.refresh) {
                startDiscovery();
                rotateMenuItemIcon(item);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onOptionsItemSelected(refresh);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAffinity();
    }

    private void startDiscovery() {
        Log.d(LOG_TAG, "Trying to start discover");
        synchronized (LOCK) {
            if (!discovering) {
                discovering = true;
                sources.forEach(DeviceSource::startDiscovery);
                refresh.getActionView().setEnabled(false);
                new Handler(Looper.getMainLooper()).postDelayed(this::stopDiscovery, SEARCH_DURATION);
                Log.d(LOG_TAG, "Discover started");
            }
        }
    }

    private void stopDiscovery() {
        Log.d(LOG_TAG, "Trying to stop discover");
        synchronized (LOCK) {
            if (discovering) {
                sources.forEach(DeviceSource::stopDiscovery);
                discovering = false;
                refresh.getActionView().setEnabled(true);
                Log.d(LOG_TAG, "Discover stopped");
            }
        }
    }

    private void initializeObjects() {
        try {
            discovering = false;
            devices = Collections.synchronizedList(new ArrayList<>());
            sources = new ArrayList<>();
            sources.add(new SamsungDeviceSource(this));
            devicesListRV = findViewById(R.id.devicesListRV);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void loadDevices() {
        devicesListRV.setAdapter(new ListDeviceAdapterSdk(this, devices));
        RecyclerView.LayoutManager layout = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        devicesListRV.setLayoutManager(layout);
    }

    public void connectToDevice(Device device) {
        Intent intent = new Intent(this, RemoteControlActivity.class);
        intent.putExtra(DEVICE_ID_EXTRA_NAME, device.getId());
        intent.putExtra(DEVICE_IP_EXTRA_NAME, device.getIp());
        intent.putExtra(MANUFACTURER_ID_EXTRA_NAME, device.getManufacturer().name());
        startActivity(intent);
    }
}