package com.titp.server;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TITP ISO 8583 Socket Server using j8583 library
 * Processes ISO 8583 messages over TCP socket connections
 */
public class TITPServer {
    private static final Logger logger = LoggerFactory.getLogger(TITPServer.class);

    private final int port;
    private final MessageFactory<IsoMessage> messageFactory;
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public TITPServer(int port) throws IOException {
        this.port = port;
        this.executorService = Executors.newCachedThreadPool();

        // Load ISO 8583 configuration
        this.messageFactory = initialiseMessageFactory();
        logger.info("ISO 8583 configuration loaded successfully");
    }

    private MessageFactory<IsoMessage> initialiseMessageFactory() throws IOException {
        var messageFactory = ConfigParser.createFromClasspathConfig("config_titp.xml");

        messageFactory.setBinaryHeader(true);
        messageFactory.setUseBinaryBitmap(true);
        messageFactory.setBinaryFields(true);
        messageFactory.setAssignDate(true);

        return messageFactory;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            logger.info("TITP Server started on port {}", port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected: {}", clientSocket.getInetAddress());

                    // Handle each client in a separate thread
                    executorService.submit(new ClientHandler(clientSocket, messageFactory));
                } catch (IOException e) {
                    if (running) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error starting server", e);
        }
    }

    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error closing server socket", e);
            }
        }
        executorService.shutdown();
        logger.info("TITP Server stopped");
    }

    public static void main(String[] args) {
        int port = 8080; // Default port

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.error("Invalid port number: {}", args[0]);
                System.exit(1);
            }
        }

        try {
            TITPServer server = new TITPServer(port);

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            server.start();
        } catch (IOException e) {
            logger.error("Failed to start TITP Server", e);
            System.exit(1);
        }
    }
}
