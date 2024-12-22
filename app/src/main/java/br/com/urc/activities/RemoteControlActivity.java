package br.com.urc.activities;

import static br.com.urc.common.Contants.DEVICE_ID_EXTRA_NAME;
import static br.com.urc.common.Contants.DEVICE_IP_EXTRA_NAME;
import static br.com.urc.common.Contants.LOG_TAG;
import static br.com.urc.common.Contants.MANUFACTURER_ID_EXTRA_NAME;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;

import br.com.urc.R;
import br.com.urc.client.handler.TokenHandler;
import br.com.urc.common.enums.Manufacturer;
import br.com.urc.common.enums.TvCommand;
import br.com.urc.control.RemoteControl;
import br.com.urc.control.RemoteControlHandler;
import br.com.urc.control.SamsungRemoteControl;

public class RemoteControlActivity extends RemoteControlHandler {

    private String token;
    private String ip;
    private String id;
    private Manufacturer manufacturer;
    private TokenHandler tokenHandler;
    private RemoteControl remoteControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_remote_control);
        getSupportActionBar().hide();
        initializeObjects();
        initializeUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }

    private void initializeObjects() {
        ip = getIntent().getStringExtra(DEVICE_IP_EXTRA_NAME);
        id = getIntent().getStringExtra(DEVICE_ID_EXTRA_NAME);
        manufacturer = Manufacturer.valueOf(getIntent().getStringExtra(MANUFACTURER_ID_EXTRA_NAME));
        remoteControl = getRemoteControlInstance();
        tokenHandler = new TokenHandler(this);
        token = tokenHandler.get(getDeviceUuid());
    }

    private RemoteControl getRemoteControlInstance() {
        switch (manufacturer) {
            case SAMSUNG: {
                return new SamsungRemoteControl(ip, token, this);
            }
        }
        return null;
    }

    private void initializeUi() {
        findViewById(R.id.onOff).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.POWER));
        findViewById(R.id.home).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.HOME));
        findViewById(R.id.source).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.SOURCE));
        findViewById(R.id.arrowUp).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.ARROW_UP));
        findViewById(R.id.arrowDown).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.ARROW_DOWN));
        findViewById(R.id.arrowLeft).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.ARROW_LEFT));
        findViewById(R.id.arrowRight).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.ARROW_RIGHT));
        findViewById(R.id.enter).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.ENTER));
        findViewById(R.id.volumeUp).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.VOLUME_UP));
        findViewById(R.id.volumeDown).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.VOLUME_DOWN));
        findViewById(R.id.previousChannel).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.PREVIOUS_CHANNEL));
        findViewById(R.id.nextChannel).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.NEXT_CHANNEL));
        findViewById(R.id.info).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.INFO));
        findViewById(R.id.mute).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.VOLUME_MUTE));
        findViewById(R.id.back).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.BACK));
        findViewById(R.id.exit).setOnClickListener(view -> remoteControl.sendCommand(TvCommand.EXIT));
    }

    private void connect() {
        remoteControl.connect();
    }

    private void disconnect() {
        remoteControl.disconnect();
    }

    public String getDeviceUuid() {
        return manufacturer.name() + "-" + id;
    }

    public void saveToken(String token) {
        this.token = token;
        tokenHandler.save(getDeviceUuid(), token);
        Log.i(LOG_TAG, "Token saved: " + token);
    }

}