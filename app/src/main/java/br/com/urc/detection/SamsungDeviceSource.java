package br.com.urc.detection;

import static br.com.urc.common.Contants.LOG_TAG;

import android.util.Log;

import com.samsung.multiscreen.Search;
import com.samsung.multiscreen.Service;

import br.com.urc.model.Device;

public class SamsungDeviceSource implements DeviceSource {

    private DeviceLister deviceLister;

    private Search search;

    public SamsungDeviceSource(DeviceLister deviceLister) {
        this.deviceLister = deviceLister;
        initializeSearch();
    }

    private void initializeSearch() {
        search = Service.search(deviceLister);

        search.setOnServiceFoundListener(
                service -> {
                    Log.d(LOG_TAG, "Search.onFound() service: " + service.toString());
                    deviceLister.addDevice(buildDevice(service));
                }
        );

        search.setOnServiceLostListener(
                service -> {
                    Log.d(LOG_TAG, "Search.onLost() service: " + service.toString());
                    deviceLister.removeDevice(buildDevice(service));
                }
        );
    }

    private Device buildDevice(Service service) {
        return new Device(service.getId(), service.getUri().getHost(), service.getName());
    }

    @Override
    public void startDiscovery() {
        Log.d(LOG_TAG, "Discover for samsung devices started");
        search.start();
    }

    @Override
    public void stopDiscovery() {
        search.stop();
        Log.d(LOG_TAG, "Discover for samsung devices stopped");
    }
}
