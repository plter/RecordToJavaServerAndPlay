package com.example.demo;

import com.example.demo.ws.WSCommands;
import com.example.demo.ws.WSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.setBinaryMessageSizeLimit(2 * 1024 * 1024); // limit data size 2M
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        stopRecordSession(session);
    }

    private void stopRecordSession(WebSocketSession session) throws IOException {
        if (session.getAttributes().containsKey("currentFileChannel")) {
            FileChannel channel = (FileChannel) session.getAttributes().get("currentFileChannel");
            channel.close();
            session.getAttributes().remove("currentFileChannel");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);

        WSMessage m = new WSMessage(message.getPayload());
        if (WSCommands.REQUEST_FILE_NAME.equals(m.getCommand())) {
            String filename = "" + new Date().getTime();
            session.getAttributes().put("currentFilename", filename);
            File file = new File(filename);
            if (!file.exists()) {
                boolean suc = file.createNewFile();
            }
            FileChannel channel = FileChannel.open(Path.of(file.getPath()), StandardOpenOption.WRITE);
            session.getAttributes().put("currentFileChannel", channel);
            session.sendMessage(new TextMessage(new WSMessage(WSCommands.FILE_NAME_RESPONSE, filename).toJSONString()));
        } else if (WSCommands.STOP_RECORD.equals(m.getCommand())) {
            stopRecordSession(session);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);

        FileChannel channel = (FileChannel) session.getAttributes().get("currentFileChannel");
        channel.write(message.getPayload());
    }
}
