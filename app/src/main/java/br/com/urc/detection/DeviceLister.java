package br.com.urc.detection;

import androidx.appcompat.app.AppCompatActivity;

import br.com.urc.model.Device;

public abstract class DeviceLister extends AppCompatActivity {

    public abstract void addDevice(Device device);

    public abstract void removeDevice(Device device);

}
