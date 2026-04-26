package server.network.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Mở ServerSocket và lắng nghe client kết nối đến server
public class HotelServer {

    private final int port;
    private final ExecutorService clientPool; // ThreadPool tạo luồng xử lý cho mỗi request của Client
    private boolean running;

    public HotelServer(int port) {
        this.port = port;
        this.clientPool = Executors.newFixedThreadPool(20);
        this.running = false;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            running = true;
            System.out.println("[SERVER] HotelServer đang chạy tại cổng " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Client connected: " + clientSocket.getRemoteSocketAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket); // Tạo luồng xử lý cho request của Client
                clientPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Lỗi khởi động server: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        clientPool.shutdown();
        System.out.println("[SERVER] HotelServer đã dừng.");
    }

    public static void main(String[] args) {
        new HotelServer(1111).start();
    }
}