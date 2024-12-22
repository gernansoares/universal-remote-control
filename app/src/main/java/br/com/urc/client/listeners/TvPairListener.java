package br.com.urc.client.listeners;

import static br.com.urc.common.Contants.LOG_TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import br.com.urc.control.SamsungRemoteControl;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class TvPairListener extends WebSocketListener {

    private SamsungRemoteControl controller;

    public TvPairListener(SamsungRemoteControl controller) {
        this.controller = controller;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.i(LOG_TAG, "Websocket connected!");
        controller.setWebSocket(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.i(LOG_TAG, "Message received: " + text);
        controller.receiveResponse(text);
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        Log.i(LOG_TAG, "Message received: " + bytes);
        controller.receiveResponse(bytes.base64());
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(LOG_TAG, t.getMessage());
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.i(LOG_TAG, "WebSocket closed: " + reason);
    }

}
