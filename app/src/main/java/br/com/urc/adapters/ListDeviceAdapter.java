package br.com.urc.adapters;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.urc.R;
import br.com.urc.activities.ListDevicesActivity;
import br.com.urc.holders.DeviceHolder;

public class ListDeviceAdapter extends RecyclerView.Adapter {

    private ListDevicesActivity context;
    private List<WifiP2pDevice> devices;

    public ListDeviceAdapter(ListDevicesActivity context, List<WifiP2pDevice> devices) {
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_holder, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder view, int position) {
        DeviceHolder holder = (DeviceHolder) view;
        final WifiP2pDevice device = devices.get(position);
        holder.getDeviceName().setText(device.deviceName);
        holder.getDeviceName().setOnClickListener(v -> context.connectToDevice(device));

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
