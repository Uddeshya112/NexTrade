package com.nextrade.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class StompWebSocketClient extends WebSocketClient implements AutoCloseable {
    private static final Logger log = Logger.getLogger(StompWebSocketClient.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Consumer<String>> subscriptions = new ConcurrentHashMap<>();
    private final String accessToken;
    private boolean connected = false;

    public StompWebSocketClient(String serverUrl, String accessToken) {
        super(toWebSocketUri(serverUrl));
        this.accessToken = accessToken;
    }


    private static URI toWebSocketUri(String serverUrl) {
        String normalized = serverUrl == null ? "" : serverUrl.trim().replaceAll("/+$", "");
        if (normalized.startsWith("https://")) normalized = "wss://" + normalized.substring("https://".length());
        else if (normalized.startsWith("http://")) normalized = "ws://" + normalized.substring("http://".length());
        if (!normalized.startsWith("ws://") && !normalized.startsWith("wss://")) {
            throw new IllegalArgumentException("WebSocket URL must use ws:// or wss:// (or provide an http(s) base URL)");
        }
        return URI.create(normalized + "/ws");
    }

    @Override public void onOpen(ServerHandshake handshakedata) {
        sendStompConnect();
    }

    @Override public void onMessage(String message) {
        handleStompFrame(message);
    }

    @Override public void onClose(int code, String reason, boolean remote) {
        connected = false;
    }

    @Override public void onError(Exception ex) { ex.printStackTrace(); }

    private void sendStompConnect() {
        String connectFrame = "CONNECT\n" +
            "accept-version:1.1,1.0\n" +
            "heart-beat:10000,10000\n" +
            "Authorization:Bearer " + accessToken + "\n" +
            "\n\u0000";
        send(connectFrame);
    }

    public void subscribe(String destination, Consumer<String> handler) {
        String subId = "sub-" + System.currentTimeMillis();
        subscriptions.put(subId, handler);
        String subscribeFrame = "SUBSCRIBE\n" +
            "id:" + subId + "\n" +
            "destination:" + destination + "\n" +
            "\n\u0000";
        send(subscribeFrame);
    }

    public void send(String destination, Object payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            String sendFrame = "SEND\n" +
                "destination:" + destination + "\n" +
                "content-type:application/json\n" +
                "content-length:" + body.length() + "\n" +
                "\n" + body + "\u0000";
            send(sendFrame);
        } catch (Exception e) { log.log(Level.SEVERE, "Failed to send STOMP message", e); }
    }

    private void handleStompFrame(String frame) {
        String[] lines = frame.split("\n");
        if (lines.length == 0) return;
        String command = lines[0];
        Map<String, String> headers = new ConcurrentHashMap<>();
        StringBuilder body = new StringBuilder();
        boolean inBody = false;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty() || line.equals("\u0000")) { inBody = true; continue; }
            if (inBody) body.append(line);
            else { int colon = line.indexOf(':'); if (colon > 0) headers.put(line.substring(0, colon), line.substring(colon + 1)); }
        }
        switch (command) {
            case "CONNECTED" -> { connected = true; }
            case "MESSAGE" -> {
                String subId = headers.get("subscription");
                Consumer<String> handler = subscriptions.get(subId);
                if (handler != null) handler.accept(body.toString());
            }
            case "ERROR" -> System.err.println("STOMP error: " + body);
        }
    }

    public boolean isConnected() { return connected; }

    @Override
    public void close() {
        connected = false;
        subscriptions.clear();
        super.close();
    }
}
