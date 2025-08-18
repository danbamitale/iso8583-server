package com.titp.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.solab.iso8583.IsoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Handles sending messages to clients
 */
public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private final DataOutputStream outputStream;

    public MessageSender(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Sends an ISO 8583 message to the client
     */
    public void sendMessage(IsoMessage message) throws IOException {
        byte[] messageBytes = message.writeData();

        ByteBuffer byteBuffer = ByteBuffer.allocate(messageBytes.length + 2);
        byteBuffer.putShort((short) messageBytes.length);
        byteBuffer.put(messageBytes);

        byte[] output = byteBuffer.array();
        outputStream.write(output);
        outputStream.flush();

        logger.debug("Response sent: MTI={}, Length={}, output = {}", Integer.toString(message.getType(), 16), output.length, ByteArrayUtil.toHexString(output));
        byteBuffer.reset();
    }

    /**
     * Sends an error response
     */
    public void sendErrorResponse(IsoMessage response) throws IOException {
        try {
            if (response != null) {
                sendMessage(response);
                logger.warn("Sending error response due to processing failure");
            }
        } catch (Exception e) {
            logger.error("Error sending error response", e);
        } finally {
            // Close the output stream to indicate error
            outputStream.close();
        }
    }
}
