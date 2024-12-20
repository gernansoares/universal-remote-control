package br.com.urc.adapters;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.multiscreen.Service;

import java.util.List;

import br.com.urc.R;
import br.com.urc.activities.ListDevicesActivity;
import br.com.urc.activities.ListDevicesActivitySdk;
import br.com.urc.holders.DeviceHolder;

public class ListDeviceAdapterSdk extends RecyclerView.Adapter {

    private ListDevicesActivitySdk context;
    private List<Service> devices;

    public ListDeviceAdapterSdk(ListDevicesActivitySdk context, List<Service> devices) {
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
        final Service device = devices.get(position);
        holder.getDeviceName().setText(device.getName());
        holder.getDeviceName().setOnClickListener(v -> context.connectToDevice(device));

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
}
