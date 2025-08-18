package com.titp.server;

import com.solab.iso8583.MessageFactory;
import com.titp.server.utils.IsoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles individual client connections and processes ISO 8583 messages using j8583
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final AtomicLong messageCounter = new AtomicLong(0);

    private final Socket clientSocket;
    private final MessageProcessor messageProcessor;
    private final MessageSender messageSender;
    private final DataInputStream inputStream;

    public ClientHandler(Socket clientSocket, MessageFactory<?> messageFactory) throws IOException {
        this.clientSocket = clientSocket;
        this.messageProcessor = new MessageProcessor(messageFactory);
        this.messageSender = new MessageSender(new DataOutputStream(clientSocket.getOutputStream()));
        this.inputStream = new DataInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            logger.info("Client handler started for {}", clientSocket.getInetAddress());

            while (!clientSocket.isClosed()) {
                // Read message length (2 bytes)
                int messageLength = inputStream.readShort();
                if (messageLength <= 0) {
                    logger.warn("Invalid message length: {}", messageLength);
                    break;
                }

                // Read the ISO message
                byte[] messageBytes = new byte[messageLength];
                int bytesRead = inputStream.read(messageBytes);
                if (bytesRead != messageLength) {
                    logger.warn("Expected {} bytes but read {}", messageLength, bytesRead);
                    break;
                }

                // Process the message
                processMessage(messageBytes);
            }
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                logger.error("Error handling client connection", e);
            }
        } finally {
            closeConnection();
        }
    }

    private void processMessage(byte[] messageBytes) throws IOException {
        long messageId = messageCounter.incrementAndGet();

        // Process the message
        MessageProcessor.MessageResult result = messageProcessor.processMessage(messageBytes, messageId);

        if (result.isSuccess()) {
            // Log the response message
            logger.info("Response Iso Message:");
            IsoLogger.logResponseMessage(result.getResponse());

            // Send response
            messageSender.sendMessage(result.getResponse());
            logger.info("Message #{} processed successfully", messageId);

        } else {
            logger.error("Error processing message #{}: {}", messageId, result.getError().getMessage());
            messageSender.sendErrorResponse(result.getResponse());
        }
    }

    private void closeConnection() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            logger.info("Client connection closed: {}", clientSocket.getInetAddress());
        } catch (IOException e) {
            logger.error("Error closing client connection", e);
        }
    }
}
