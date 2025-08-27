package com.titp.server.processor;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.titp.server.utils.ISOResponseCode;
import com.titp.server.utils.IsoMessageUtils;
import com.titp.server.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Processor for Network Management messages (MTI 0800)
 */
public class NetworkManagementProcessor extends MTIProcessor {
    private static final Logger logger = LoggerFactory.getLogger(NetworkManagementProcessor.class);
    private final IsoMessageUtils isoMessageUtils;


    public NetworkManagementProcessor(MessageFactory<?> messageFactory) {
        super(messageFactory);
        this.isoMessageUtils = new IsoMessageUtils((MessageFactory<IsoMessage>) messageFactory);
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
            var response = processNetworkManagement(request);
            var isSuccess =  response.getField(39).getValue().toString().equals("00");

            if (isSuccess) {
                logger.info("Network management message processed successfully");
                return new ProcessingResult(true, ISOResponseCode.SUCCESS, "Network management processed successfully", response);
            } else {
                logger.info("Network management message failed");
                return new ProcessingResult(false, ISOResponseCode.ERROR, "Network management processing failed", response);
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
    private IsoMessage processNetworkManagement(IsoMessage request) {
        var processingCode = request.getField(3).getValue().toString();
        var transmissionDate = request.getField(11).getValue().toString();
        var transactionType = Integer.parseInt(processingCode.substring(0, 2));

        logger.info("Processing code: {}, transactionType: {}, transmissionDate: {}", processingCode, transactionType, transmissionDate);


        switch (transactionType) {
            case 92:
                return processWorkingKeyDownload(request);
            default:
                logger.warn("Unknown network management code: {}", transactionType);
                return isoMessageUtils.createSuccessResponse(request, ISOResponseCode.SUCCESS);
        }
    }


    private IsoMessage processWorkingKeyDownload(IsoMessage request) {
        var response = isoMessageUtils.createSuccessResponse(request, ISOResponseCode.SUCCESS);

        var key = ByteArrayUtil.hexStringToByteArray("00000000000000000000000000000000");
        var buffer = ByteBuffer.allocate(2 + 16);
        buffer.putShort((short) key.length);
        buffer.put(key);

        response.setField(62, new IsoValue<>(IsoType.LLLBIN, buffer.array()));

        return response;
    }
}
