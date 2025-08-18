package com.titp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles stripping of message headers
 */
public class HeaderStripper {
    private static final Logger logger = LoggerFactory.getLogger(HeaderStripper.class);

    /**
     * Strips the 5-byte header from the message if present
     * @param messageBytes The raw message bytes
     * @return The message bytes without the header
     */
    public static byte[] stripHeaderIfPresent(byte[] messageBytes) {
        // Check if message starts with 5-byte header pattern
        if (messageBytes.length >= 5) {
            // Look for common header patterns like "02020"
            String header = new String(messageBytes, 0, 5);
            if (header.matches("\\d{5}")) {
                logger.debug("Stripping 5-byte header: {}", header);
                byte[] isoMessageBytes = new byte[messageBytes.length - 5];
                System.arraycopy(messageBytes, 5, isoMessageBytes, 0, isoMessageBytes.length);
                return isoMessageBytes;
            }
        }
        
        // No header found, return original message
        return messageBytes;
    }
}
