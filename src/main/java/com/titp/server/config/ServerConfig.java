package com.titp.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized server configuration management
 */
public class ServerConfig {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

    // Default configuration values
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_CONFIG_FILE = "config_titp.xml";
    public static final int DEFAULT_THREAD_POOL_SIZE = 50;
    public static final int DEFAULT_SOCKET_TIMEOUT = 30000; // 30 seconds

    // Configuration properties
    private final int port;
    private final String configFile;
    private final int threadPoolSize;
    private final int socketTimeout;
    private final boolean binaryHeader;
    private final boolean useBinaryBitmap;
    private final boolean binaryFields;
    private final boolean assignDate;

    private ServerConfig(Builder builder) {
        this.port = builder.port;
        this.configFile = builder.configFile;
        this.threadPoolSize = builder.threadPoolSize;
        this.socketTimeout = builder.socketTimeout;
        this.binaryHeader = builder.binaryHeader;
        this.useBinaryBitmap = builder.useBinaryBitmap;
        this.binaryFields = builder.binaryFields;
        this.assignDate = builder.assignDate;
    }

    // Getters
    public int getPort() { return port; }
    public String getConfigFile() { return configFile; }
    public int getThreadPoolSize() { return threadPoolSize; }
    public int getSocketTimeout() { return socketTimeout; }
    public boolean isBinaryHeader() { return binaryHeader; }
    public boolean isUseBinaryBitmap() { return useBinaryBitmap; }
    public boolean isBinaryFields() { return binaryFields; }
    public boolean isAssignDate() { return assignDate; }

    /**
     * Log the current configuration
     */
    public void logConfiguration() {
        logger.info("Server Configuration:");
        logger.info("  Port: {}", port);
        logger.info("  Config File: {}", configFile);
        logger.info("  Thread Pool Size: {}", threadPoolSize);
        logger.info("  Socket Timeout: {}ms", socketTimeout);
        logger.info("  Binary Header: {}", binaryHeader);
        logger.info("  Binary Bitmap: {}", useBinaryBitmap);
        logger.info("  Binary Fields: {}", binaryFields);
        logger.info("  Assign Date: {}", assignDate);
    }

    /**
     * Builder pattern for ServerConfig
     */
    public static class Builder {
        private int port = DEFAULT_PORT;
        private String configFile = DEFAULT_CONFIG_FILE;
        private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
        private boolean binaryHeader = true;
        private boolean useBinaryBitmap = true;
        private boolean binaryFields = true;
        private boolean assignDate = true;

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder configFile(String configFile) {
            this.configFile = configFile;
            return this;
        }

        public Builder threadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            return this;
        }

        public Builder socketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder binaryHeader(boolean binaryHeader) {
            this.binaryHeader = binaryHeader;
            return this;
        }

        public Builder useBinaryBitmap(boolean useBinaryBitmap) {
            this.useBinaryBitmap = useBinaryBitmap;
            return this;
        }

        public Builder binaryFields(boolean binaryFields) {
            this.binaryFields = binaryFields;
            return this;
        }

        public Builder assignDate(boolean assignDate) {
            this.assignDate = assignDate;
            return this;
        }

        public ServerConfig build() {
            return new ServerConfig(this);
        }
    }

    /**
     * Create default configuration
     */
    public static ServerConfig getDefault() {
        return new Builder().build();
    }

    /**
     * Create configuration from command line arguments
     */
    public static ServerConfig fromArgs(String[] args) {
        Builder builder = new Builder();

        if (args.length > 0) {
            try {
                builder.port(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                logger.error("Invalid port number: {}, using default: {}", args[0], DEFAULT_PORT);
            }
        }

        return builder.build();
    }
}
