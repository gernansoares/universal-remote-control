package br.com.urc.control;

import static br.com.urc.common.Contants.LOG_TAG;
import static br.com.urc.common.Contants.TLS;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import br.com.urc.client.interceptors.WebSocketInterceptor;
import br.com.urc.client.listeners.TvPairListener;
import br.com.urc.common.enums.TvCommand;
import br.com.urc.config.ssl.GenericTrustManager;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class SamsungRemoteControl implements RemoteControl {

    private final Integer TIMEOUT = 30;
    private final Charset PAYLOAD_CHARSET = StandardCharsets.ISO_8859_1;
    private final String SAMSUNG_URL = "wss://%s:8002/api/v2/channels/samsung.remote.control?name=UniversalRemoteControl";
    private final String SAMSUNG_URL_WITH_TOKEN = SAMSUNG_URL + "&token=%s";
    private final String ip;
    private final RemoteControlHandler controlHandler;
    private String token;
    private OkHttpClient client;

    @Setter
    private WebSocket webSocket;

    public SamsungRemoteControl(String ip, String token, RemoteControlHandler controlHandler) {
        this.ip = ip;
        this.token = token;
        this.controlHandler = controlHandler;
        initializeClient();
    }

    private void initializeClient() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new GenericTrustManager()
            };
            SSLContext sslContext = SSLContext.getInstance(TLS);
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

            client = new OkHttpClient.Builder()
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .callTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(new WebSocketInterceptor())
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCertificates[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void connect() {
        String formattedUrl = encode(Optional.ofNullable(token)
                .map(t -> String.format(SAMSUNG_URL_WITH_TOKEN, ip, token))
                .orElseGet(() -> String.format(SAMSUNG_URL, ip)));
        Request request = new Request.Builder().url(formattedUrl).build();
        webSocket = client.newWebSocket(request, new TvPairListener(this));
    }

    @Override
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
            Log.d(LOG_TAG, "Sending command: " + payload);
            webSocket.send(encode(payload));
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        if (webSocket != null) {
            boolean isClosed = webSocket.close(1000, "Closing connection");
            if (isClosed) {
                Log.i(LOG_TAG, "WebSocket closed successfully.");
            } else {
                Log.e(LOG_TAG, "Failed to close WebSocket.");
            }
        }
    }

    private String encode(String value) {
        byte[] payloadBytes = value.getBytes(PAYLOAD_CHARSET);
        return new String(payloadBytes, PAYLOAD_CHARSET);
    }

    public void receiveResponse(String response) {
        try {
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
                controlHandler.saveToken(token);
                disconnect();
                connect();
            }
        }
    }

}
