package client.network.socket;

import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

// Chạy server trước (HotelServer) --> Sau đó chạy test ScoketClient
public class TestSocketClient {
    public static void main(String[] args) {
        try (
                HotelClient client = new HotelClient("localhost", 1111);
                SocketRequestExecutor executor = new SocketRequestExecutor(client)
        ) {
            BaseRequest request = BaseRequest.of(CommandType.PING, null);
            BaseResponse response = executor.execute(request);

            System.out.println("success = " + response.isSuccess());
            System.out.println("message = " + response.getMessage());
            System.out.println("data = " + response.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}