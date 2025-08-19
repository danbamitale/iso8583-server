package com.titp.server.processor;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.titp.server.utils.ISOResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor for Authorization Request messages (MTI 0100)
 */
public class AuthorizationProcessor extends MTIProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationProcessor.class);

    public AuthorizationProcessor(MessageFactory<?> messageFactory) {
        super(messageFactory);
    }

    @Override
    protected boolean validateRequest(IsoMessage request) {
        // Check for required fields for authorization
        boolean hasRequiredFields = request.hasField(2) && request.hasField(3) && request.hasField(4);

        if (!hasRequiredFields) {
            logger.warn("Authorization request missing required fields (2, 3, 4)");
            return false;
        }

        // Validate PAN (Field 2)
        String pan = ByteArrayUtil.toHexString((byte[]) request.getField(2).getValue());
        if (pan == null || pan.length() < 13 || pan.length() > 19) {
            logger.warn("Invalid PAN length: {}", pan != null ? pan.length() : "null");
            return false;
        }

        // Validate Processing Code (Field 3)
        String processingCode = String.valueOf(request.getField(3).getValue());
        if (processingCode == null || processingCode.length() != 6) {
            logger.warn("Invalid processing code length: {}", processingCode != null ? processingCode.length() : "null");
            return false;
        }

        return true;
    }

    @Override
    protected ProcessingResult processBusinessLogic(IsoMessage request) {
        try {
            logger.info("Processing authorization request");

            // Extract key fields
            String pan = ByteArrayUtil.toHexString((byte[]) request.getField(2).getValue());
            String processingCode = String.valueOf(request.getField(3).getValue());
            String amount = request.hasField(4) ? String.valueOf(request.getField(4).getValue()) : "0";

            logger.info("Authorization - PAN: {}, Processing Code: {}, Amount: {}",
                maskPAN(pan), processingCode, amount);

            // Simulate authorization logic
            // In a real implementation, this would check with the card issuer
            boolean isAuthorized = simulateAuthorization(pan, processingCode, amount);

            if (isAuthorized) {
                logger.info("Authorization approved");
                // Create custom response with additional fields
                IsoMessage customResponse = createCustomResponse(request, ISOResponseCode.SUCCESS);
                return new ProcessingResult(true, ISOResponseCode.SUCCESS, "Authorization approved", customResponse);
            } else {
                logger.info("Authorization declined");
                return new ProcessingResult(false, ISOResponseCode.ERROR, "Authorization declined");
            }

        } catch (Exception e) {
            logger.error("Error processing authorization", e);
            return new ProcessingResult(false, ISOResponseCode.ERROR, "Authorization processing error");
        }
    }

    @Override
    public int getMTI() {
        return 0x100; // 0100 in hex
    }

    /**
     * Simulate authorization logic
     */
    private boolean simulateAuthorization(String pan, String processingCode, String amount) {
        // Simple simulation - approve most requests, decline some based on amount
        try {
            long amountValue = Long.parseLong(amount);
            // Decline transactions over 10000 (simulating fraud detection)
            return amountValue <= 10000;
        } catch (NumberFormatException e) {
            logger.warn("Invalid amount format: {}", amount);
            return false;
        }
    }

    /**
     * Create a custom response with additional fields
     */
    @SuppressWarnings("unchecked")
    private IsoMessage createCustomResponse(IsoMessage request, ISOResponseCode responseCode) {
        IsoMessage response = ((MessageFactory<IsoMessage>) messageFactory).createResponse(request);

        // Set response code
        response.setField(39, new IsoValue<>(IsoType.ALPHA, responseCode.getCode(), 2));

        // Copy key fields from request to response
        if (request.hasField(2)) {
            response.setField(2, request.getField(2)); // PAN
        }
        if (request.hasField(3)) {
            response.setField(3, request.getField(3)); // Processing Code
        }
        if (request.hasField(4)) {
            response.setField(4, request.getField(4)); // Amount
        }
        if (request.hasField(11)) {
            response.setField(11, request.getField(11)); // STAN
        }
        if (request.hasField(41)) {
            response.setField(41, request.getField(41)); // Terminal ID
        }
        if (request.hasField(42)) {
            response.setField(42, request.getField(42)); // Merchant ID
        }

        // Add authorization-specific fields
        response.setField(38, new IsoValue<>(IsoType.ALPHA, "AUTH", 4)); // Authorization ID
        response.setField(44, new IsoValue<>(IsoType.ALPHA, "APPROVED", 8)); // Additional Response Data

        return response;
    }

    /**
     * Mask PAN for logging (show only first 6 and last 4 digits)
     */
    private String maskPAN(String pan) {
        if (pan == null || pan.length() < 10) {
            return "****";
        }
        return pan.substring(0, 6) + "****" + pan.substring(pan.length() - 4);
    }
}
