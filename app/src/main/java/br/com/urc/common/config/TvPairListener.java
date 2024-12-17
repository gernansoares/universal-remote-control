package br.com.urc.common.config;

import static br.com.urc.common.Contants.LOG_TAG;

import android.util.Log;

import br.com.urc.activities.RemoteControlActivity;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class TvPairListener extends WebSocketListener {

    private RemoteControlActivity activity;

    public TvPairListener(RemoteControlActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.i(LOG_TAG, "Websocket connected!");
        activity.setWebSocket(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.i(LOG_TAG, "Message received: " + text);
        activity.receiveResponse(text);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(LOG_TAG, t.getMessage());
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.i(LOG_TAG, "WebSocket closed: " + reason);
    }

}
