package br.com.urc.activities;

import static br.com.urc.common.Contants.DEVICE_ID_EXTRA_NAME;
import static br.com.urc.common.Contants.DEVICE_IP_EXTRA_NAME;
import static br.com.urc.common.Contants.LOG_TAG;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.samsung.multiscreen.Search;
import com.samsung.multiscreen.Service;

import br.com.urc.R;
import br.com.urc.view.adapters.ListDeviceAdapterSdk;

public class ListDevicesActivitySdk extends AppCompatActivity {

    private final Long SEARCH_DURATION = (long) (1000 * 10);
    private Boolean discovering;
    private Search search;
    private List<Service> devices;
    private RecyclerView devicesListRV;
    private MenuItem refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_devices);
        initializeObjects();
        initializeSearch();
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
        synchronized (discovering) {
            if (!discovering) {
                Log.d(LOG_TAG, "Discover started");
                search.start();
                discovering = true;
                refresh.getActionView().setEnabled(false);
                new Handler(Looper.getMainLooper()).postDelayed(this::stopDiscovery, SEARCH_DURATION);
            }
        }
    }

    private void stopDiscovery() {
        Log.d(LOG_TAG, "Trying to stop discover");
        synchronized (discovering) {
            if (discovering) {
                Log.d(LOG_TAG, "Discover stopped");
                search.stop();
                discovering = false;
                refresh.getActionView().setEnabled(true);
            }
        }
    }

    private void initializeSearch() {
        search = Service.search(this);

        search.setOnServiceFoundListener(
                service -> {
                    Log.d(LOG_TAG, "Search.onFound() service: " + service.toString());
                    if (!devices.contains(service)) {
                        devices.add(service);
                        loadDevices();
                    }
                }
        );

        search.setOnServiceLostListener(
                service -> {
                    Log.d(LOG_TAG, "Search.onLost() service: " + service.toString());
                    devices.remove(service);
                }
        );
    }

    private void initializeObjects() {
        try {
            discovering = false;
            devices = new ArrayList<>();
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

    public void connectToDevice(Service service) {
        Intent intent = new Intent(this, RemoteControlActivity.class);
        intent.putExtra(DEVICE_ID_EXTRA_NAME, service.getId());
        intent.putExtra(DEVICE_IP_EXTRA_NAME, service.getUri().getHost());
        startActivity(intent);
    }
}