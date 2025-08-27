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
 * Handles individual client connections and processes ISO 8583 messages
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final AtomicLong messageCounter = new AtomicLong(0);

    private final Socket clientSocket;
    private final MessageProcessor messageProcessor;
    private final MessageSender messageSender;
    private final DataInputStream inputStream;
    private final String clientAddress;

    public ClientHandler(Socket clientSocket, MessageFactory<?> messageFactory) throws IOException {
        this.clientSocket = clientSocket;
        this.clientAddress = clientSocket.getInetAddress().getHostAddress();
        this.messageProcessor = new MessageProcessor(messageFactory);
        this.messageSender = new MessageSender(new DataOutputStream(clientSocket.getOutputStream()));
        this.inputStream = new DataInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            logger.info("Client handler started for {}", clientAddress);
            processClientMessages();
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                logger.error("Error handling client connection from {}", clientAddress, e);
            }
        } finally {
            closeConnection();
        }
    }

    /**
     * Main message processing loop
     */
    private void processClientMessages() throws IOException {
        while (!clientSocket.isClosed()) {
            MessageData messageData = readMessage();
            if (messageData == null) {
                logger.info("Client {} disconnected", clientAddress);
                break;
            }
            
            processMessage(messageData);
        }
    }

    /**
     * Read a complete message from the client
     */
    private MessageData readMessage() throws IOException {
        try {
            // Read message length (2 bytes)
            int messageLength = inputStream.readShort();
            if (messageLength <= 0) {
                logger.warn("Invalid message length from {}: {}", clientAddress, messageLength);
                return null;
            }

            // Read the ISO message
            byte[] messageBytes = new byte[messageLength];
            int bytesRead = inputStream.read(messageBytes);
            if (bytesRead != messageLength) {
                logger.warn("Expected {} bytes but read {} from {}", messageLength, bytesRead, clientAddress);
                return null;
            }

            return new MessageData(messageBytes, messageLength);
            
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                logger.error("Error reading message from {}", clientAddress, e);
            }
            throw e;
        }
    }

    /**
     * Process a single message
     */
    private void processMessage(MessageData messageData) throws IOException {
        long messageId = messageCounter.incrementAndGet();
        logger.debug("Processing message #{} from {} ({} bytes)", messageId, clientAddress, messageData.length);

        // Process the message
        MessageProcessor.MessageResult result = messageProcessor.processMessage(messageData.bytes, messageId);

        if (result.isSuccess()) {
            handleSuccessfulProcessing(result, messageId);
        } else {
            handleFailedProcessing(result, messageId);
        }
    }

    /**
     * Handle successful message processing
     */
    private void handleSuccessfulProcessing(MessageProcessor.MessageResult result, long messageId) throws IOException {
        // Log the response message
        logger.info("Response Iso Message:");
        IsoLogger.logResponseMessage(result.getResponse());

        // Send response
        messageSender.sendMessage(result.getResponse());
        logger.info("Message #{} processed successfully for {}", messageId, clientAddress);
    }

    /**
     * Handle failed message processing
     */
    private void handleFailedProcessing(MessageProcessor.MessageResult result, long messageId) throws IOException {
        logger.error("Error processing message #{} from {}: {}", 
            messageId, clientAddress, result.getError().getMessage());
        
        if (result.getResponse() != null) {
            messageSender.sendErrorResponse(result.getResponse());
        }
        
        // Close connection on processing error
        closeConnection();
    }

    /**
     * Close the client connection and cleanup resources
     */
    private void closeConnection() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            logger.info("Client connection closed: {}", clientAddress);
        } catch (IOException e) {
            logger.error("Error closing client connection for {}", clientAddress, e);
        }
    }

    /**
     * Data class to hold message information
     */
    private static class MessageData {
        final byte[] bytes;
        final int length;

        MessageData(byte[] bytes, int length) {
            this.bytes = bytes;
            this.length = length;
        }
    }
}
