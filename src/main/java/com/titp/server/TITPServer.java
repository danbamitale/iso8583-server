package com.titp.server;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.titp.server.config.MessageFactoryManager;
import com.titp.server.config.ServerConfig;
import com.titp.server.processor.ProcessorFactory;
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

    private final ServerConfig serverConfig;
    private final MessageFactory<IsoMessage> messageFactory;
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public TITPServer(ServerConfig serverConfig) throws IOException {
        this.serverConfig = serverConfig;
        this.executorService = Executors.newFixedThreadPool(serverConfig.getThreadPoolSize());
        this.messageFactory = initializeMessageFactory();
        initializeProcessorFactory();
    }

    /**
     * Initialize the ISO 8583 message factory with configuration
     */
    private MessageFactory<IsoMessage> initializeMessageFactory() throws IOException {
        MessageFactoryManager factoryManager = new MessageFactoryManager(serverConfig);
        MessageFactory<IsoMessage> messageFactory = factoryManager.createMessageFactory();
        factoryManager.validateConfiguration(messageFactory);
        return messageFactory;
    }

    /**
     * Initialize the processor factory with message factory
     */
    private void initializeProcessorFactory() {
        logger.info("Initializing processor factory...");
        ProcessorFactory.initialize(messageFactory);
        logger.info("Processor factory initialized successfully");
    }

    /**
     * Start the server and begin accepting client connections
     */
    public void start() {
        try {
            startServerSocket();
            runServerLoop();
        } catch (IOException e) {
            logger.error("Error starting server", e);
            throw new RuntimeException("Failed to start server", e);
        }
    }

    /**
     * Initialize and start the server socket
     */
    private void startServerSocket() throws IOException {
        serverSocket = new ServerSocket(serverConfig.getPort());
        running = true;
        logger.info("TITP Server started on port {}", serverConfig.getPort());
    }

    /**
     * Main server loop for accepting client connections
     */
    private void runServerLoop() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleNewClient(clientSocket);
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting client connection", e);
                }
            }
        }
    }

    /**
     * Handle a new client connection
     */
    private void handleNewClient(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        logger.info("New client connected: {}", clientAddress);
        
        try {
            // Handle each client in a separate thread
            executorService.submit(new ClientHandler(clientSocket, messageFactory));
        } catch (Exception e) {
            logger.error("Error creating client handler for {}", clientAddress, e);
            closeClientSocket(clientSocket);
        }
    }

    /**
     * Close client socket safely
     */
    private void closeClientSocket(Socket clientSocket) {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing client socket", e);
        }
    }

    /**
     * Stop the server and cleanup resources
     */
    public void stop() {
        logger.info("Stopping TITP Server...");
        
        running = false;
        closeServerSocket();
        shutdownExecutorService();
        
        logger.info("TITP Server stopped");
    }

    /**
     * Close the server socket
     */
    private void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error closing server socket", e);
            }
        }
    }

    /**
     * Shutdown the executor service gracefully
     */
    private void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            logger.info("Executor service shutdown initiated");
        }
    }

    /**
     * Check if the server is currently running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get the server port
     */
    public int getPort() {
        return serverConfig.getPort();
    }

    /**
     * Main entry point for the TITP Server
     */
    public static void main(String[] args) {
        ServerConfig config = ServerConfig.fromArgs(args);
        config.logConfiguration();
        
        try {
            TITPServer server = new TITPServer(config);
            setupShutdownHook(server);
            server.start();
        } catch (IOException e) {
            logger.error("Failed to start TITP Server", e);
            System.exit(1);
        }
    }

    /**
     * Setup shutdown hook for graceful server shutdown
     */
    private static void setupShutdownHook(TITPServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received, stopping server...");
            server.stop();
        }));
    }
}
