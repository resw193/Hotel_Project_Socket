package client.network.socket;

import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SocketRequestExecutor implements AutoCloseable {

    private final HotelClient hotelClient;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public SocketRequestExecutor(HotelClient hotelClient) {
        this.hotelClient = hotelClient;
    }

    public synchronized void open() throws IOException {
        if (!hotelClient.isConnected()) {
            hotelClient.connect();
        }

        if (out == null || in == null) {
            out = new ObjectOutputStream(hotelClient.getSocket().getOutputStream());
            out.flush();

            in = new ObjectInputStream(hotelClient.getSocket().getInputStream());
        }
    }

    public synchronized BaseResponse execute(BaseRequest request) {
        try {
            open();

            out.writeObject(request);
            out.flush();
            out.reset();

            Object responseObj = in.readObject();
            if (responseObj instanceof BaseResponse response) {
                return response;
            }
            return BaseResponse.error("Phản hồi từ server không hợp lệ.");
        } catch (Exception e) {
            return BaseResponse.error("Lỗi gửi request tới server: " + e.getMessage());
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
        }
        if (out != null) {
            out.close();
            out = null;
        }
        hotelClient.close();
    }
}