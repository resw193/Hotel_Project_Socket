package common.protocol.request;

import common.protocol.command.CommandType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class BaseRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String requestId;
    private CommandType commandType;
    private Object data;
    private LocalDateTime timestamp;

    public BaseRequest() {
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.commandType = CommandType.UNKNOWN;
    }

    public BaseRequest(CommandType commandType, Object data) {
        this.requestId = UUID.randomUUID().toString();
        this.commandType = commandType;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static BaseRequest of(CommandType commandType, Object data) {
        return new BaseRequest(commandType, data);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}