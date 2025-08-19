package com.titp.server.processor;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.titp.server.utils.ISOResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MTI processors using the Template Pattern.
 * Defines the processing flow for all ISO 8583 message types.
 */
public abstract class MTIProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MTIProcessor.class);
    protected final MessageFactory<?> messageFactory;

    public MTIProcessor(MessageFactory<?> messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Template method that defines the processing flow.
     * This method cannot be overridden by subclasses.
     */
    public final ProcessingResult process(IsoMessage request) {
        try {
            logger.debug("Processing MTI: {}", Integer.toString(request.getType(), 16));

            // Step 1: Validate the request
            if (!validateRequest(request)) {
                logger.warn("Request validation failed for MTI: {}", Integer.toString(request.getType(), 16));
                return new ProcessingResult(false, ISOResponseCode.ERROR, "Request validation failed");
            }

            // Step 2: Process the business logic
            ProcessingResult result = processBusinessLogic(request);

            // Step 3: Log the processing result
            logProcessingResult(request, result);

            return result;

        } catch (Exception e) {
            logger.error("Error processing MTI: {}", Integer.toString(request.getType(), 16), e);
            return new ProcessingResult(false, ISOResponseCode.ERROR, "Processing error: " + e.getMessage());
        }
    }

    /**
     * Validate the incoming request. Subclasses can override this method.
     * @param request The ISO message to validate
     * @return true if validation passes, false otherwise
     */
    protected boolean validateRequest(IsoMessage request) {
        // Default validation - check if message has required fields
        return request != null && request.getType() > 0;
    }

    /**
     * Abstract method that subclasses must implement for specific business logic.
     * @param request The ISO message to process
     * @return ProcessingResult containing the result of business logic processing
     */
    protected abstract ProcessingResult processBusinessLogic(IsoMessage request);

    /**
     * Log the processing result. Subclasses can override this method.
     * @param request The original request
     * @param result The processing result
     */
    protected void logProcessingResult(IsoMessage request, ProcessingResult result) {
        if (result.isSuccess()) {
            logger.info("Successfully processed MTI: {} with response code: {}",
                    Integer.toString(request.getType(), 16), result.getResponseCode().getCode());
        } else {
            logger.warn("Failed to process MTI: {} with response code: {}",
                    Integer.toString(request.getType(), 16), result.getResponseCode().getCode());
        }
    }

    /**
     * Get the MTI type this processor handles.
     * @return The MTI type as an integer
     */
    public abstract int getMTI();

    /**
     * Result of message processing
     */
    public static class ProcessingResult {
        private final boolean success;
        private final ISOResponseCode responseCode;
        private final String message;
        private final IsoMessage response;

        public ProcessingResult(boolean success, ISOResponseCode responseCode, String message) {
            this.success = success;
            this.responseCode = responseCode;
            this.message = message;
            this.response = null;
        }

        public ProcessingResult(boolean success, ISOResponseCode responseCode, String message, IsoMessage response) {
            this.success = success;
            this.responseCode = responseCode;
            this.message = message;
            this.response = response;
        }

        public boolean isSuccess() { return success; }
        public ISOResponseCode getResponseCode() { return responseCode; }
        public String getMessage() { return message; }
        public IsoMessage getResponse() { return response; }

        /**
         * Check if a custom response message was provided
         */
        public boolean hasCustomResponse() { return response != null; }
    }
}
