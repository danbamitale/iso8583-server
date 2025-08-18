package com.titp.server.utils;

import com.solab.iso8583.IsoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsoLogger {
    private static final Logger logger = LoggerFactory.getLogger(IsoLogger.class);

    /**
     * Logs detailed information about the response ISO 8583 message
     */
    public static void logResponseMessage(IsoMessage message) {
        try {
            // Log all present fields with better formatting
            StringBuilder fieldsInfo = new StringBuilder();
            fieldsInfo.append("\nMTI: ").append(Integer.toString(message.getType(), 16));
            fieldsInfo.append("\nFields:");

            boolean hasFields = false;
            for (int i = 2; i <= 128; i++) {
                if (message.hasField(i)) {
                    String fieldValue = String.valueOf(message.getField(i).getValue());
                    if (fieldValue != null) {
                        fieldsInfo.append(String.format("\nF%d=%s", i, fieldValue));
                        hasFields = true;
                    }
                }
            }

            if (hasFields) {
                // Remove trailing comma and space
                fieldsInfo.setLength(fieldsInfo.length() - 2);
                logger.info(fieldsInfo.toString());
            }

        } catch (Exception e) {
            logger.warn("Error message details: {}", e.getMessage());
        }
    }
}
