package top.pigest.queuemanagerdemo.liveroom;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.brotli.dec.BrotliInputStream;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.liveroom.event.EventRegistry;
import top.pigest.queuemanagerdemo.liveroom.ui.DanmakuServicePage;
import top.pigest.queuemanagerdemo.util.RequestUtils;
import top.pigest.queuemanagerdemo.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.zip.InflaterInputStream;

public class LiveMessageService implements WebSocket.Listener {
    private static LiveMessageService INSTANCE;

    private final long uid;
    private final long roomId;
    private final String key;
    private final String buvid;
    private HttpClient client;
    private WebSocket webSocket;
    private int sequence = 1;

    public LiveMessageService(long uid, long roomId, String key, String buvid) {
        this.uid = uid;
        this.roomId = roomId;
        this.key = key;
        this.buvid = buvid;
    }

    public CompletionStage<?> onBinary(WebSocket socket, ByteBuffer buffer, boolean last) {
        List<Pair<Integer, Optional<JsonObject>>> list = new ArrayList<>();
        try {
            receivePacket(buffer, list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Pair<Integer, Optional<JsonObject>> pair : list) {
            if (pair.getKey() != -1) {
                if (pair.getKey() == 8 && pair.getValue().isPresent()) {
                    int s = pair.getValue().get().get("code").getAsInt();
                    if (s != 0) {
                        Platform.runLater(() -> Utils.showDialogMessage("认证失败", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                    }
                }
                if (pair.getKey() == 5 && pair.getValue().isPresent()) {
                    JsonObject payload = pair.getValue().get();
                    EventRegistry.getRegistries().forEach(event -> event.onReceive(payload));
                }
            }
        }
        return WebSocket.Listener.super.onBinary(socket, buffer, last);
    }

    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        Platform.runLater(() -> {
            if (QueueManager.INSTANCE.getMainScene().getBorderPane().getCenter() instanceof DanmakuServicePage container && container.getInnerContainer().getId().equals("c0")) {
                container.disconnectedButton(((QMButton) ((BorderPane) container.getInnerContainer().getChildren().getFirst()).getRight()));
            }
        });
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        sendVerify(webSocket);
        Platform.runLater(() -> {
            Utils.showDialogMessage("连接直播弹幕服务成功", false, QueueManager.INSTANCE.getMainScene().getRootDrawer());
            if (QueueManager.INSTANCE.getMainScene().getBorderPane().getCenter() instanceof DanmakuServicePage container && container.getInnerContainer().getId().equals("c0")) {
                container.connectedButton(((QMButton) (((BorderPane) ((VBox) container.getInnerContainer().getChildren().getFirst()).getChildren().getFirst())).getRight()));
            }
        });
        webSocket.request(10);
    }

    private void sendVerify(WebSocket webSocket) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uid", uid);
        jsonObject.addProperty("roomid", roomId);
        jsonObject.addProperty("buvid", buvid);
        jsonObject.addProperty("protover", 3);
        jsonObject.addProperty("platform", "web");
        jsonObject.addProperty("type", 2);
        jsonObject.addProperty("key", key);
        String s = jsonObject.toString();
        byte[] bytes = s.getBytes();
        int size = bytes.length + 16;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(size);
        buffer.putShort((short) 16);
        buffer.putShort((short) 1);
        buffer.putInt(7);
        buffer.putInt(sequence);
        buffer.put(bytes);
        try {
            sendPacket(webSocket, buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendHeartbeat(WebSocket socket) throws IOException {
        int size = 16;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(size);
        buffer.putShort((short) 16);
        buffer.putShort((short) 1);
        buffer.putInt(2);
        buffer.putInt(sequence);
        sequence++;
        sendPacket(socket, buffer);
    }

    public void sendPacket(WebSocket webSocket, ByteBuffer buffer) {
        webSocket.sendBinary(ByteBuffer.wrap(buffer.array()), true);
    }

    public void receivePacket(ByteBuffer buffer, List<Pair<Integer, Optional<JsonObject>>> list) throws IOException {
        while (buffer.remaining() > 0) {
            int size = buffer.getInt();
            short headSize = buffer.getShort();
            short protocolVersion = buffer.getShort();
            int opcode = buffer.getInt();
            buffer.getInt();
            byte[] body = new byte[size - headSize];
            buffer.get(body);
            switch (protocolVersion) {
                case 2 -> {
                    body = decompress(body);
                    receivePacket(ByteBuffer.wrap(body), list);
                    return;
                }
                case 3 -> {
                    body = decompressBrotli(body);
                    receivePacket(ByteBuffer.wrap(body), list);
                    return;
                }
            }
            String x = new String(body);
            if (opcode == 3) {
                list.add(new Pair<>(opcode, Optional.empty()));
                return;
            } else if (opcode == 8) {
                JsonObject jsonObject = JsonParser.parseString(x).getAsJsonObject();
                if (jsonObject.has("code") && jsonObject.get("code").getAsInt() == 0) {
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                if (webSocket.isOutputClosed()) {
                                    this.cancel();
                                }
                                sendHeartbeat(webSocket);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }, 0, 30000);
                    list.add(new Pair<>(opcode, Optional.empty()));
                    return;
                } else {
                    list.add(new Pair<>(opcode, Optional.of(jsonObject)));
                    return;
                }
            }
            list.add(new Pair<>(opcode, Optional.of(JsonParser.parseString(x).getAsJsonObject())));
        }
    }

    public boolean isSessionAvailable() {
        return webSocket != null && !webSocket.isInputClosed() && !webSocket.isOutputClosed();
    }

    public void close() throws IOException {
        this.webSocket.sendClose(0, "closed");
        this.client.shutdownNow();
    }

    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        InflaterInputStream fis = new InflaterInputStream(bais);
        return outputDecompress(fis);
    }

    public static byte[] decompressBrotli(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BrotliInputStream bis = new BrotliInputStream(bais);
        return outputDecompress(bis);
    }

    private static byte[] outputDecompress(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        is.close();
        return bos.toByteArray();
    }

    public static void connect() {
        try {
            LiveMessageService.connect(QueueManager.getSelfUid(), QueueManager.INSTANCE.ROOM_ID);
        } catch (Exception e) {
            throw new ConnectFailedException(e.getMessage());
        }
    }

    public static void connect(long uid, long roomId) {
        MessageStreamVerification verification = getMessageStreamVerification(roomId);
        if (verification.hosts == null || verification.hosts.isEmpty()) {
            throw new RuntimeException(verification.token());
        } else {
            String hostname = verification.hosts.getFirst().hostname;
            Thread thread = new Thread(() -> {
                try {
                    URI uri = new URI("wss://%s:%s/sub".formatted(hostname, verification.hosts.getFirst().wssPort));
                    HttpClient client = HttpClient.newHttpClient();
                    INSTANCE = new LiveMessageService(uid, roomId, verification.token, RequestUtils.getCookie("buvid3"));
                    client.newWebSocketBuilder()
                            .header("User-Agent", Settings.USER_AGENT)
                            .buildAsync(uri, INSTANCE).join();
                    INSTANCE.client = client;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
        }
    }

    public static LiveMessageService getInstance() {
        return INSTANCE;
    }

    private static MessageStreamVerification getMessageStreamVerification(long roomId) {
        try {
            Utils.fillCookies(RequestUtils.getCookieStore());
            JsonObject object = RequestUtils.requestToJson(RequestUtils.httpGet("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo")
                    .appendUrlParameter("id", roomId)
                    .appendUrlParameter("type", 0)
                    .appendUrlParameter("web_location", 444.8).buildWithWbiSign());
            if (object.get("code").getAsInt() == 0) {
                List<Host> hosts = new ArrayList<>();
                JsonObject data = object.getAsJsonObject("data");
                data.getAsJsonArray("host_list").forEach(host -> {
                    JsonObject hostObj = host.getAsJsonObject();
                    hosts.add(new Host(hostObj.get("host").getAsString(), hostObj.get("port").getAsInt(), hostObj.get("wss_port").getAsInt(), hostObj.get("ws_port").getAsInt()));
                });
                return new MessageStreamVerification(data.get("token").getAsString(), hosts);
            } else {
                return new MessageStreamVerification("%s(%s)".formatted(object.get("message").getAsString(), object.get("code").getAsInt()), null);
            }
        } catch (Exception e) {
            return new MessageStreamVerification("获取验证密钥失败", null);
        }
    }

    public record MessageStreamVerification(String token, List<Host> hosts) {
    }

    public record Host(String hostname, int port, int wssPort, int wsPort) {
    }

    public static class ConnectFailedException extends RuntimeException {
        public ConnectFailedException(String message) {
            super(message);
        }
    }

    public static class VerifyFailedException extends RuntimeException {
        public VerifyFailedException(String message) {
            super(message);
        }
    }
}
