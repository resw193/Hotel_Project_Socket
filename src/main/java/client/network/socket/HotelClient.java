package client.network.socket;

import java.io.IOException;
import java.net.Socket;

public class HotelClient implements AutoCloseable {

    private final String host;
    private final int port;
    private Socket socket;

    public HotelClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void connect() throws IOException {
        if (isConnected()) return;
        socket = new Socket(host, port);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized Socket getSocket() {
        return socket;
    }

    @Override
    public synchronized void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}