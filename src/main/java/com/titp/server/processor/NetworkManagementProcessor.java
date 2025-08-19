package com.titp.server.processor;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.titp.server.utils.ISOResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor for Network Management messages (MTI 0800)
 */
public class NetworkManagementProcessor extends MTIProcessor {
    private static final Logger logger = LoggerFactory.getLogger(NetworkManagementProcessor.class);

    public NetworkManagementProcessor(MessageFactory<?> messageFactory) {
        super(messageFactory);
    }

    @Override
    protected boolean validateRequest(IsoMessage request) {
        boolean hasRequiredFields = request.hasField(3);

        if (!hasRequiredFields) {
            logger.warn("Network management message missing required field 3 Processing code");
            return false;
        }

        return true;
    }

    @Override
    protected ProcessingResult processBusinessLogic(IsoMessage request) {
        try {
            logger.info("Processing network management message");

            // Process different network management functions
            boolean isProcessed = processNetworkManagement("", "001");

            if (isProcessed) {
                logger.info("Network management message processed successfully");
                return new ProcessingResult(true, ISOResponseCode.SUCCESS, "Network management processed successfully");
            } else {
                logger.info("Network management message failed");
                return new ProcessingResult(false, ISOResponseCode.ERROR, "Network management processing failed");
            }

        } catch (Exception e) {
            logger.error("Error processing network management message", e);
            return new ProcessingResult(false, ISOResponseCode.ERROR, "Network management processing error");
        }
    }

    @Override
    public int getMTI() {
        return 0x800; // 0800 in hex
    }

    /**
     * Process different network management functions
     */
    private boolean processNetworkManagement(String transmissionDateTime, String networkCode) {
        switch (networkCode) {
            case "001": // Sign-on
                logger.info("Processing sign-on request");
                return processSignOn(transmissionDateTime);

            case "002": // Sign-off
                logger.info("Processing sign-off request");
                return processSignOff(transmissionDateTime);

            case "301": // Echo test
                logger.info("Processing echo test");
                return processEchoTest(transmissionDateTime);

            case "302": // Cutover
                logger.info("Processing cutover request");
                return processCutover(transmissionDateTime);

            default:
                logger.warn("Unknown network management code: {}", networkCode);
                return true;
        }
    }

    /**
     * Process sign-on request
     */
    private boolean processSignOn(String transmissionDateTime) {
        logger.info("Sign-on request received at: {}", transmissionDateTime);
        // Simulate successful sign-on
        return true;
    }

    /**
     * Process sign-off request
     */
    private boolean processSignOff(String transmissionDateTime) {
        logger.info("Sign-off request received at: {}", transmissionDateTime);
        // Simulate successful sign-off
        return true;
    }

    /**
     * Process echo test
     */
    private boolean processEchoTest(String transmissionDateTime) {
        logger.info("Echo test received at: {}", transmissionDateTime);
        // Echo tests should always succeed
        return true;
    }

    /**
     * Process cutover request
     */
    private boolean processCutover(String transmissionDateTime) {
        logger.info("Cutover request received at: {}", transmissionDateTime);
        // Simulate successful cutover
        return true;
    }
}
