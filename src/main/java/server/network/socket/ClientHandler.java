package server.network.socket;

import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// Xử lý Request của client gửi đến server --> trả về response
// Đọc request -> Mỗi client sẽ tạo 1 thread xử lý riêng -> Gọi dispatcher
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestDispatcher dispatcher;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.dispatcher = new RequestDispatcher();
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (!socket.isClosed()) {
                Object obj = in.readObject();

                if (!(obj instanceof BaseRequest request)) {
                    BaseResponse invalidResponse = BaseResponse.error("Request không hợp lệ.");
                    out.writeObject(invalidResponse);
                    out.flush();
                    continue;
                }

                BaseResponse response = dispatcher.dispatch(request);

                out.writeObject(response); // gửi response tới client
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[SERVER] Client disconnected: " + socket.getRemoteSocketAddress() + " | Reason: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {

            }
        }
    }
}