package client.network.socket;

import client.presentation.login.main.Application;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

public final class SocketSessionManager {

    private static HotelClient client;
    private static SocketRequestExecutor executor;

    private SocketSessionManager() {

    }

    public static synchronized void init(String host, int port) throws Exception {
        if (client != null && client.isConnected() && executor != null) {
            return;
        }

        client = new HotelClient(host, port);
        executor = new SocketRequestExecutor(client);
        executor.open();
    }

    public static synchronized BaseResponse send(BaseRequest request) {
        try {
            if (executor == null || client == null || !client.isConnected()) {
                init(Application.getSocketHost(), Application.getSocketPort());
            }

            return executor.execute(request);
        } catch (Exception e) {
            return BaseResponse.error("Không thể kết nối server: " + e.getMessage());
        }
    }

    public static synchronized boolean isReady() {
        return client != null && client.isConnected() && executor != null;
    }

    public static synchronized void shutdown() {
        try {
            if (executor != null) executor.close();
        } catch (Exception ignored) {
        } finally {
            executor = null;
            client = null;
        }
    }
}