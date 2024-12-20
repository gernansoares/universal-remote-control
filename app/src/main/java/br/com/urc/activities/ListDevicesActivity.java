package br.com.urc.activities;

import static br.com.urc.common.Contants.DEVICE_ID_EXTRA_NAME;
import static br.com.urc.common.Contants.LOG_TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import br.com.urc.R;
import br.com.urc.adapters.ListDeviceAdapter;
import br.com.urc.dialog.LoadingDialog;
import br.com.urc.handler.WifiP2pHandler;

public class ListDevicesActivity extends AppCompatActivity {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pHandler wifiP2pHandler;
    private RecyclerView devicesListRV;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_devices);
        initializeObjects();
        wifiP2pHandler.initializeWifiDirect();
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
            updateDevicesList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openLoadingDialog();
        wifiP2pHandler.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeLoadingDialog();
        wifiP2pHandler.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAffinity();
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
            manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
            channel = manager.initialize(this, getMainLooper(), null);
            wifiP2pHandler = new WifiP2pHandler(this, manager, channel);
            devicesListRV = findViewById(R.id.devicesListRV);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void updateDevicesList() {
        openLoadingDialog();
        wifiP2pHandler.discover();
    }

    public void loadDevices(List<WifiP2pDevice> devices) {
        devicesListRV.setAdapter(new ListDeviceAdapter(this, devices));
        RecyclerView.LayoutManager layout = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        devicesListRV.setLayoutManager(layout);
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(WifiP2pDevice device) {
        openLoadingDialog();
        wifiP2pHandler.connect(device);
    }

    public void startRemoteControl(String ip) {
        Intent intent = new Intent(this, RemoteControlActivity.class);
        intent.putExtra(DEVICE_ID_EXTRA_NAME, ip);
        startActivity(intent);
    }

}