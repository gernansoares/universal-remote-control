package br.com.urc.activities;

import static br.com.urc.common.Contants.DEVICE_EXTRA_NAME;
import static br.com.urc.common.Contants.LOG_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.samsung.multiscreen.Search;
import com.samsung.multiscreen.Service;

import br.com.urc.R;
import br.com.urc.adapters.ListDeviceAdapterSdk;
import br.com.urc.dialog.LoadingDialog;

public class ListDevicesActivitySdk extends AppCompatActivity {

    private Search search;
    private List<Service> devices;
    private RecyclerView devicesListRV;
    private LoadingDialog loadingDialog;

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            loadDevices();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openLoadingDialog();
        search.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeLoadingDialog();
        search.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAffinity();
    }

    private void initializeSearch() {
        search = Service.search(this);

        search.setOnServiceFoundListener(
                service -> {
                    Log.d(LOG_TAG, "Search.onFound() service: " + service.toString());
                    if (!devices.contains(service)) {
                        devices.add(service);
                        loadDevices();
                        closeLoadingDialog();
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

    public void openLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog.newInstance();
            loadingDialog.setCancelable(false);
            loadingDialog.show(getSupportFragmentManager(), "loadingDialog");
        }
    }

    public void closeLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isVisible()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private void initializeObjects() {
        try {
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
        intent.putExtra(DEVICE_EXTRA_NAME, service.getUri().getHost());
        startActivity(intent);
    }
}