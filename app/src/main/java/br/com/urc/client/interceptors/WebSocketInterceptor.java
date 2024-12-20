package br.com.urc.client.interceptors;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class WebSocketInterceptor implements Interceptor {

    private static final String SEC_WEBSOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";
    private static final String CLIENT_MAX_WINDOW_BITS = "client_max_window_bits";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();
        requestBuilder.addHeader(SEC_WEBSOCKET_EXTENSIONS, CLIENT_MAX_WINDOW_BITS);
        return chain.proceed(requestBuilder.build());
    }

}
