package com.titp.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.titp.server.processor.MTIProcessor;
import com.titp.server.processor.ProcessorFactory;
import com.titp.server.utils.ISOResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles ISO 8583 message processing logic with improved separation of concerns
 */
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private final MessageFactory<?> messageFactory;

    public MessageProcessor(MessageFactory<?> messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Main entry point for processing raw message bytes
     *
     * @param messageBytes The raw message bytes
     * @param messageId Unique identifier for the message
     * @return Processed message result
     */
    public MessageResult processMessage(byte[] messageBytes, long messageId) {
        try {
            // Step 1: Parse the message
            IsoMessage request = parseMessage(messageBytes, messageId);
            
            // Step 2: Process using template pattern
            MTIProcessor.ProcessingResult processingResult = processRequestWithTemplate(request);
            
            // Step 3: Create appropriate response
            IsoMessage response = createAppropriateResponse(request, processingResult);
            
            return new MessageResult(true, request, response, null);
            
        } catch (Exception e) {
            logger.error("Error processing message #{}", messageId, e);
            return handleProcessingError(messageId, e);
        }
    }

    /**
     * Parse raw message bytes into ISO message
     */
    private IsoMessage parseMessage(byte[] messageBytes, long messageId) throws Exception {
        // Strip header if present
        byte[] isoMessageBytes = HeaderStripper.stripHeaderIfPresent(messageBytes);
        
        logger.debug("Message #{} - Raw bytes: {}, Stripped bytes: {}", 
            messageId, 
            ByteArrayUtil.toHexString(messageBytes), 
            ByteArrayUtil.toHexString(isoMessageBytes));
        
        IsoMessage request = messageFactory.parseMessage(isoMessageBytes, 0);
        
        // Log the received message
        MessageLogger.logReceivedMessage(
            request,
            messageId,
            messageBytes.length,
            isoMessageBytes.length);
        
        return request;
    }

    /**
     * Process request using the Template Pattern
     */
    private MTIProcessor.ProcessingResult processRequestWithTemplate(IsoMessage request) {
        int mti = request.getType();
        MTIProcessor processor = ProcessorFactory.getProcessor(mti);
        
        if (processor == null) {
            logger.warn("No processor found for MTI: {}, using default processing", String.format("%04X", mti));
            return new MTIProcessor.ProcessingResult(true, ISOResponseCode.SUCCESS, "Default processing");
        }
        
        logger.info("Using processor for MTI: {}", String.format("%04X", mti));
        return processor.process(request);
    }

    /**
     * Create appropriate response based on processing result
     */
    private IsoMessage createAppropriateResponse(IsoMessage request, MTIProcessor.ProcessingResult processingResult) {
        if (processingResult.hasCustomResponse()) {
            logger.debug("Using custom response from processor");
            return processingResult.getResponse();
        } else {
            logger.debug("Creating default response with code: {}", processingResult.getResponseCode().getCode());
            return createDefaultResponse(request, processingResult.getResponseCode());
        }
    }

    /**
     * Creates a default response message for the given request
     */
    @SuppressWarnings("unchecked")
    private IsoMessage createDefaultResponse(IsoMessage request, ISOResponseCode responseCode) {
        IsoMessage response = ((MessageFactory<IsoMessage>) messageFactory).createResponse(request);
        response.setField(39, new IsoValue<>(IsoType.ALPHA, responseCode.getCode(), 2));
        return response;
    }

    /**
     * Handle processing errors and create error response
     */
    private MessageResult handleProcessingError(long messageId, Exception error) {
        logger.error("Processing error for message #{}: {}", messageId, error.getMessage());
        
        try {
            IsoMessage errorResponse = createErrorResponse();
            return new MessageResult(false, null, errorResponse, error);
        } catch (Exception responseError) {
            logger.error("Failed to create error response for message #{}", messageId, responseError);
            return new MessageResult(false, null, null, error);
        }
    }

    /**
     * Create a generic error response when request parsing fails
     */
    @SuppressWarnings("unchecked")
    private IsoMessage createErrorResponse() {
        // Create a minimal error response
        IsoMessage errorResponse = ((MessageFactory<IsoMessage>) messageFactory).createResponse(null);
        errorResponse.setField(39, new IsoValue<>(IsoType.ALPHA, ISOResponseCode.ERROR.getCode(), 2));
        return errorResponse;
    }

    /**
     * Result of message processing
     */
    public static class MessageResult {
        private final boolean success;
        private final IsoMessage request;
        private final IsoMessage response;
        private final Exception error;

        public MessageResult(boolean success, IsoMessage request, IsoMessage response, Exception error) {
            this.success = success;
            this.request = request;
            this.response = response;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public IsoMessage getRequest() {
            return request;
        }

        public IsoMessage getResponse() {
            return response;
        }

        public Exception getError() {
            return error;
        }
    }
}
