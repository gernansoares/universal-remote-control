package br.com.urc.handler;

import static br.com.urc.common.Contants.LOG_TAG;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import br.com.urc.activities.ListDevicesActivity;

public class WifiP2pHandler {

    private final Long DELAY = (long) (1000 * 1);

    private ListDevicesActivity activity;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IntentFilter wifip2pFilter;
    private BroadcastReceiver broadcastReceiver;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private Queue<WifiP2pOperation> operations;
    private List<WifiP2pDevice> devices;
    private WifiP2pDevice device;
    private WifiP2pOperation executing;
    private final Integer RETRY_NUMBER = 3;
    private int retryAttemps = 0;

    public enum WifiP2pOperation {
        CONNECT,
        DISCONNECT,
        REGISTER,
        UNREGISTER,
        DISCOVER,
        STOP_DISCOVER;
    }

    public WifiP2pHandler(ListDevicesActivity activity, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.activity = activity;
        this.manager = manager;
        this.channel = channel;
        this.operations = new LinkedList<>();
        this.devices = new LinkedList<>();
    }

    public void resume() {
        cancel();
        addOperation(WifiP2pHandler.WifiP2pOperation.DISCONNECT);
        addOperation(WifiP2pHandler.WifiP2pOperation.REGISTER);
        addOperation(WifiP2pHandler.WifiP2pOperation.DISCOVER);
        executeNextOperation();
    }

    public void pause() {
        cancel();
        addOperation(WifiP2pHandler.WifiP2pOperation.STOP_DISCOVER);
        addOperation(WifiP2pHandler.WifiP2pOperation.UNREGISTER);
        executeNextOperation();
    }

    private void registerReceiver() {
        activity.registerReceiver(broadcastReceiver, wifip2pFilter);
    }

    private void unregisterReceiver() {
        try {
            activity.unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
        }
    }

    public void cancel() {
        operations.clear();
    }

    private void stopPeerDiscovery() {
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Peer discovery stopped.");
                executeNextOperation();
            }

            @Override
            public void onFailure(int reason) {
                Log.e(LOG_TAG, "Failed to stop peer discovery. Reason: " + reason);
                executeNextOperation();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void disconnect() {
        manager.requestGroupInfo(channel, group -> {
            if (group != null) {
                try {
                    manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(LOG_TAG, "Disconnected successfully");
                            executeNextOperation();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.e(LOG_TAG, "Failed to disconnect: " + reason);
                            executeNextOperation();
                        }
                    });
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to set group idle: " + e.getMessage());
                    executeNextOperation();
                }
            } else {
                Log.d(LOG_TAG, "No group to disconnect");
                executeNextOperation();
            }
        });
    }

    public void initializeWifiDirect() {
        mPeerListListener = (peers) -> {
            Optional.of(peers)
                    .map(WifiP2pDeviceList::getDeviceList)
                    .filter(p -> !p.isEmpty() && WifiP2pOperation.DISCOVER.equals(executing))
                    .ifPresentOrElse(p -> {
                        List<WifiP2pDevice> devices = new ArrayList<>();
                        for (WifiP2pDevice device : p) {
                            Log.d(LOG_TAG, device.deviceName + " " + device.deviceAddress);
                            devices.add(device);
                        }
                        this.devices.clear();
                        this.devices.addAll(devices);
                        activity.loadDevices(devices);
                        executeNextOperation();
                    }, this::executeNextOperation);
        };

        broadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                Optional.ofNullable(intent.getAction())
                        .ifPresent(action -> {
                            switch (action) {
                                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                                    if (WifiP2pOperation.REGISTER.equals(executing)) {
                                        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                                        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                                            Log.d(LOG_TAG, "Wi-Fi P2P is enabled");
                                        } else {
                                            Log.e(LOG_TAG, "Wi-Fi P2P is disabled");
                                        }
                                        executeNextOperation();
                                    }
                                    break;
                                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                                    if (WifiP2pOperation.DISCOVER.equals(executing)) {
                                        manager.requestPeers(channel, mPeerListListener);
                                    }
                                    break;
                                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                                    if (WifiP2pOperation.CONNECT.equals(executing)) {
                                        manager.requestConnectionInfo(channel, (v) -> {
                                            synchronized (executing) {
                                                Log.d(LOG_TAG, v.toString());
                                                if (WifiP2pOperation.CONNECT.equals(executing)) {
                                                    if (v.groupOwnerAddress == null) {
                                                        Log.e(LOG_TAG, "Device group has no address.");
                                                    } else {
                                                        Log.i(LOG_TAG, "Launching remote.");
                                                        addOperation(WifiP2pOperation.DISCONNECT);
                                                        executeNextOperation();
                                                        activity.startRemoteControl(v.groupOwnerAddress.getHostAddress());
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    break;
                                default:
                                    break;

                            }
                        });
            }
        };

        wifip2pFilter = new IntentFilter();
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifip2pFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    public void discover() {
        cancel();
        addOperation(WifiP2pOperation.DISCOVER);
        executeNextOperation();
    }

    @SuppressLint("MissingPermission")
    private void connect() {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Connected to device.");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(LOG_TAG, "Error when connecting to device: " + reason);
                retryConnectingToDevice();
            }
        });
    }

    private void retryConnectingToDevice() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            retryAttemps++;
            if (retryAttemps <= RETRY_NUMBER) {
                addOperation(WifiP2pOperation.DISCONNECT);
                addOperation(WifiP2pOperation.STOP_DISCOVER);
                addOperation(WifiP2pOperation.DISCOVER);
                addOperation(WifiP2pOperation.CONNECT);
            }
            executeNextOperation();
        }, DELAY);
    }

    @SuppressLint("MissingPermission")
    private void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Peer discovery started successfully.");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(LOG_TAG, "Peer discovery failed: " + reason);
                executeNextOperation();
            }
        });
    }

    public void addOperation(WifiP2pOperation operation) {
        operations.add(operation);
    }

    public void connect(WifiP2pDevice device) {
        this.device = device;
        this.retryAttemps = 0;
        operations.add(WifiP2pOperation.DISCONNECT);
        operations.add(WifiP2pOperation.CONNECT);
        executeNextOperation();
    }

    private void executeNextOperation() {
        Log.d(LOG_TAG, "Was executing: " + executing);
        Optional.ofNullable(operations.poll())
                .ifPresentOrElse(nextOperation -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Log.d(LOG_TAG, "Executing: " + nextOperation);
                        executing = nextOperation;
                        switch (nextOperation) {
                            case DISCOVER:
                                discoverPeers();
                                break;
                            case STOP_DISCOVER:
                                stopPeerDiscovery();
                                break;
                            case REGISTER:
                                registerReceiver();
                                break;
                            case UNREGISTER:
                                unregisterReceiver();
                                break;
                            case CONNECT:
                                connect();
                                break;
                            case DISCONNECT:
                                disconnect();
                                break;
                            default:
                                break;
                        }
                    }, DELAY);
                }, () -> {
                    Log.i(LOG_TAG, "Finished execution");
                    executing = null;
                    activity.closeLoadingDialog();
                });
    }

}
