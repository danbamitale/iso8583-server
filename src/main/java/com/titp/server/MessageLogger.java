package com.titp.server;

import com.solab.iso8583.IsoMessage;
import com.titp.server.utils.IsoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles logging of ISO 8583 messages
 */
public class MessageLogger {
    private static final Logger logger = LoggerFactory.getLogger(MessageLogger.class);

    /**
     * Logs detailed information about a received ISO 8583 message
     */
    public static void logReceivedMessage(IsoMessage message, long messageId, int messageLength,
                                          int isoMessageLength) {
        try {
            logger.info("=== Message #{} Received ===", messageId);
            logger.info("Raw message length: {} bytes", messageLength);
            if (messageLength != isoMessageLength) {
                logger.info("ISO message length (after header): {} bytes", isoMessageLength);
            }
            IsoLogger.logResponseMessage(message);
            logger.info("=== End Message #{} ===", messageId);
        } catch (Exception e) {
            logger.warn("Error logging message details: {}", e.getMessage());
            // Fallback to basic logging
            logger.info("Message #{} received: MTI={}, Length={} bytes",
                    messageId, message.getType(), messageLength);
        }
    }

}
