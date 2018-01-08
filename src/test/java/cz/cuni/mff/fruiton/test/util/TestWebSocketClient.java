package cz.cuni.mff.fruiton.test.util;

import com.google.protobuf.InvalidProtocolBufferException;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestWebSocketClient extends WebSocketClient {

    private static final int CONNECTION_TIMEOUT = 10_000;

    private static final String TOKEN_HEADER = "x-auth-token";

    private static final Logger logger = Logger.getLogger(TestWebSocketClient.class.getName());

    private BlockingQueue<CommonProtos.WrapperMessage> messageQueue = new LinkedBlockingQueue<>();

    public TestWebSocketClient(final String token, final int port) throws URISyntaxException {
        super(new URI("ws://localhost:" + port + "/socket"), new Draft_6455(), Map.of(TOKEN_HEADER, token), CONNECTION_TIMEOUT);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.log(Level.FINE, "Opened connection {0}", serverHandshake);
    }

    @Override
    public void onMessage(String message) {
        logger.log(Level.FINE, "Received message {0}", message);
        try {
            messageQueue.add(CommonProtos.WrapperMessage.parseFrom(message.getBytes()));
        } catch (InvalidProtocolBufferException e) {
            logger.log(Level.SEVERE, "Cannot parse message {0}", message);
        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        logger.log(Level.FINE, "Received message {0}", bytes);
        try {
            messageQueue.add(CommonProtos.WrapperMessage.parseFrom(bytes.array()));
        } catch (InvalidProtocolBufferException e) {
            logger.log(Level.SEVERE, "Cannot parse message {0}", bytes);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.log(Level.FINE, "Closed connection");
    }

    @Override
    public void onError(Exception e) {
        logger.log(Level.WARNING, "WebSocket error", e);
    }

    public CommonProtos.WrapperMessage blockingPoll() throws InterruptedException {
        return messageQueue.poll(5, TimeUnit.SECONDS);
    }

    public CommonProtos.WrapperMessage peek() {
        return messageQueue.peek();
    }

    public boolean hasInQueue(CommonProtos.WrapperMessage.MessageCase messageCase) {
        for (CommonProtos.WrapperMessage msg : messageQueue) {
            if (msg.getMessageCase() == messageCase) {
                return true;
            }
        }
        return false;
    }

}
