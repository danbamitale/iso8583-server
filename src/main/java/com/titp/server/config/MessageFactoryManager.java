package com.titp.server.config;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Manages ISO 8583 message factory initialization and configuration
 */
public class MessageFactoryManager {
    private static final Logger logger = LoggerFactory.getLogger(MessageFactoryManager.class);

    private final ServerConfig serverConfig;

    public MessageFactoryManager(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * Initialize and configure the message factory
     */
    public MessageFactory<IsoMessage> createMessageFactory() throws IOException {
        logger.info("Initializing message factory with config: {}", serverConfig.getConfigFile());
        
        MessageFactory<IsoMessage> messageFactory = loadConfiguration();
        configureMessageFactory(messageFactory);
        
        logger.info("Message factory initialized successfully");
        return messageFactory;
    }

    /**
     * Load message factory configuration from classpath
     */
    private MessageFactory<IsoMessage> loadConfiguration() throws IOException {
        try {
            return ConfigParser.createFromClasspathConfig(serverConfig.getConfigFile());
        } catch (IOException e) {
            logger.error("Failed to load configuration file: {}", serverConfig.getConfigFile(), e);
            throw new IOException("Failed to load ISO 8583 configuration", e);
        }
    }

    /**
     * Configure message factory settings
     */
    private void configureMessageFactory(MessageFactory<IsoMessage> messageFactory) {
        logger.debug("Configuring message factory settings...");
        
        // Configure binary message handling
        messageFactory.setBinaryHeader(serverConfig.isBinaryHeader());
        messageFactory.setUseBinaryBitmap(serverConfig.isUseBinaryBitmap());
        messageFactory.setBinaryFields(serverConfig.isBinaryFields());
        messageFactory.setAssignDate(serverConfig.isAssignDate());
        
        logger.debug("Message factory configuration applied:");
        logger.debug("  Binary Header: {}", serverConfig.isBinaryHeader());
        logger.debug("  Binary Bitmap: {}", serverConfig.isUseBinaryBitmap());
        logger.debug("  Binary Fields: {}", serverConfig.isBinaryFields());
        logger.debug("  Assign Date: {}", serverConfig.isAssignDate());
    }

    /**
     * Validate message factory configuration
     */
    public void validateConfiguration(MessageFactory<IsoMessage> messageFactory) {
        logger.info("Validating message factory configuration...");
        
        try {
            // Basic validation - check if message factory is properly configured
            if (messageFactory != null) {
                logger.info("Message factory validation successful");
            } else {
                logger.warn("Message factory validation warning: message factory is null");
            }
        } catch (Exception e) {
            logger.error("Message factory validation failed", e);
            throw new RuntimeException("Message factory validation failed", e);
        }
    }
}
