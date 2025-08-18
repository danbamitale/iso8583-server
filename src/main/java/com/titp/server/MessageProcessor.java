package com.titp.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.titp.server.utils.ISOResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles ISO 8583 message processing logic
 */
public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private final MessageFactory<?> messageFactory;

    public MessageProcessor(MessageFactory<?> messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Processes a raw message byte array
     *
     * @param messageBytes The raw message bytes
     * @return Processed message result
     */
    public MessageResult processMessage(byte[] messageBytes, long messageId) {
        IsoMessage request = null;
        try {
            // Strip header if present
            byte[] isoMessageBytes = HeaderStripper.stripHeaderIfPresent(messageBytes);

            logger.info("messageBytes = {}\nstripMessageBytes = {}", ByteArrayUtil.toHexString(messageBytes), ByteArrayUtil.toHexString(isoMessageBytes));
            request = messageFactory.parseMessage(isoMessageBytes, 0);

            // Parse the ISO message
            // Log the received message
            MessageLogger.logReceivedMessage(
                    request,
                    messageId,
                    messageBytes.length,
                    HeaderStripper.stripHeaderIfPresent(messageBytes).length);


            //Todo Process request here. Use Template Pattern to handle

            // Create response
            IsoMessage response = createResponse(request, ISOResponseCode.SUCCESS);

            return new MessageResult(true, request, response, null);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error processing message", e);
            IsoMessage response = createResponse(request, ISOResponseCode.ERROR);
            return new MessageResult(false, request, response, e);
        }
    }

    /**
     * Creates a response message for the given request
     */
    @SuppressWarnings("unchecked")
    private IsoMessage createResponse(IsoMessage request, ISOResponseCode responseCode) {
        IsoMessage response = ((MessageFactory<IsoMessage>) messageFactory).createResponse(request);
        response.setField(39, new IsoValue<>(IsoType.ALPHA, responseCode.getCode(), 2));
        return response;
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
