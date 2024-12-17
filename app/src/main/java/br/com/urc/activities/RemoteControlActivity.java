package br.com.urc.activities;

import static br.com.urc.common.Contants.DEVICE_EXTRA_NAME;
import static br.com.urc.common.Contants.LOG_TAG;
import static br.com.urc.common.Contants.TLS;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import br.com.urc.R;
import br.com.urc.common.config.GenericTrustManager;
import br.com.urc.common.config.TvPairListener;
import br.com.urc.enums.TvCommand;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class RemoteControlActivity extends AppCompatActivity {

    private final Charset PAYLOAD_CHARSET = StandardCharsets.UTF_8;
    private final String SAMSUNG_URL = "wss://%s:8002/api/v2/channels/samsung.remote.control?name=UniversalRemoteControl";
    private String token;
    private OkHttpClient client;

    @Setter
    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_remote_control);
        getSupportActionBar().hide();
        initializeObjects();
        initializeUi();
        pairWithTV(getIntent().getStringExtra(DEVICE_EXTRA_NAME));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSocket();
    }

    private void closeSocket() {
        if (webSocket != null) {
            boolean isClosed = webSocket.close(1000, "Closing connection");
            if (isClosed) {
                Log.i(LOG_TAG, "WebSocket closed successfully.");
            } else {
                Log.e(LOG_TAG, "Failed to close WebSocket.");
            }
        }
    }

    private void initializeObjects() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new GenericTrustManager()
            };
            SSLContext sslContext = SSLContext.getInstance(TLS);
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCertificates[0])
                    .hostnameVerifier((hostname, session) -> true)  // Trust all hostnames
                    .build();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void initializeUi() {
        findViewById(R.id.onOff).setOnClickListener(view -> sendCommand(TvCommand.POWER));
        findViewById(R.id.home).setOnClickListener(view -> sendCommand(TvCommand.HOME));
        findViewById(R.id.source).setOnClickListener(view -> sendCommand(TvCommand.SOURCE));
        findViewById(R.id.arrowUp).setOnClickListener(view -> sendCommand(TvCommand.ARROW_UP));
        findViewById(R.id.arrowDown).setOnClickListener(view -> sendCommand(TvCommand.ARROW_DOWN));
        findViewById(R.id.arrowLeft).setOnClickListener(view -> sendCommand(TvCommand.ARROW_LEFT));
        findViewById(R.id.arrowRight).setOnClickListener(view -> sendCommand(TvCommand.ARROW_RIGHT));
        findViewById(R.id.enter).setOnClickListener(view -> sendCommand(TvCommand.ENTER));
        findViewById(R.id.volumeUp).setOnClickListener(view -> sendCommand(TvCommand.VOLUME_UP));
        findViewById(R.id.volumeDown).setOnClickListener(view -> sendCommand(TvCommand.VOLUME_DOWN));
        findViewById(R.id.previousChannel).setOnClickListener(view -> sendCommand(TvCommand.PREVIOUS_CHANNEL));
        findViewById(R.id.nextChannel).setOnClickListener(view -> sendCommand(TvCommand.NEXT_CHANNEL));
        findViewById(R.id.info).setOnClickListener(view -> sendCommand(TvCommand.INFO));
        findViewById(R.id.mute).setOnClickListener(view -> sendCommand(TvCommand.VOLUME_MUTE));
        findViewById(R.id.back).setOnClickListener(view -> sendCommand(TvCommand.BACK));
        findViewById(R.id.exit).setOnClickListener(view -> sendCommand(TvCommand.EXIT));
    }

    private void pairWithTV(String ip) {
        String formattedUrl = String.format(SAMSUNG_URL, ip);
        Request request = new Request.Builder().url(formattedUrl).build();
        webSocket = client.newWebSocket(request, new TvPairListener(this));
    }

    public void sendCommand(TvCommand command) {
        try {
            String payload = "{"
                    + "\"method\": \"ms.remote.control\","
                    + "\"params\": {"
                    + "    \"Cmd\": \"Click\","
                    + "    \"DataOfCmd\": \"" + command.getSamsung() + "\","
                    + "    \"Option\": \"false\","
                    + "    \"Token\": \"" + token + "\","
                    + "    \"TypeOfRemote\": \"SendRemoteKey\""
                    + "}"
                    + "}";
            byte[] payloadBytes = payload.getBytes(PAYLOAD_CHARSET);
            String payloadCharset = new String(payloadBytes, PAYLOAD_CHARSET);
            Log.d(LOG_TAG, "Sending command: " + payloadCharset);
            webSocket.send(payloadCharset);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public void receiveResponse(String response) {
        try {
            Log.e(LOG_TAG, "Message from socket: " + response);
            saveToken(response);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void saveToken(String responseString) throws JSONException {
        JSONObject response = new JSONObject(responseString);
        if (response.has("data") && response.getJSONObject("data").has("token")) {
            String token = response.getJSONObject("data").getString("token");
            Log.i(LOG_TAG, "Token received: " + token);

            if (this.token == null) {
                this.token = token;
                Log.i(LOG_TAG, "Token saved: " + token);
            }
        }
    }

}