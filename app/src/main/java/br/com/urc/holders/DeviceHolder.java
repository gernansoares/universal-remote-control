package br.com.urc.holders;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import br.com.urc.R;
import lombok.Data;

@Data
public class DeviceHolder extends RecyclerView.ViewHolder {

    final Button deviceName;

    public DeviceHolder(@NonNull View view) {
        super(view);
        deviceName = (Button) view.findViewById(R.id.deviceName);
    }

}
