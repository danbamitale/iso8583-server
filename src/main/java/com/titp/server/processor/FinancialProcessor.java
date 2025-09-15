package com.titp.server.processor;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.titp.server.utils.ISOResponseCode;
import com.titp.server.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor for Financial Transaction messages (MTI 0200)
 */
public class FinancialProcessor extends MTIProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FinancialProcessor.class);

    public FinancialProcessor(MessageFactory<?> messageFactory) {
        super(messageFactory);
    }

    @Override
    protected boolean validateRequest(IsoMessage request) {
        // Check for required fields for financial transactions
        boolean hasRequiredFields = request.hasField(2) && request.hasField(3) && request.hasField(4);

        if (!hasRequiredFields) {
            logger.warn("Financial transaction missing required fields (2, 3, 4)");
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
        if (processingCode == null) {
            logger.warn("Invalid processing code length: {}", processingCode != null ? processingCode.length() : "null");
            return false;
        }

        // Validate Amount (Field 4)
        String amount = String.valueOf(request.getField(4).getValue());
        if (amount == null || amount.length() == 0) {
            logger.warn("Invalid amount: {}", amount);
//            return false;
        }

        return true;
    }

    @Override
    protected ProcessingResult processBusinessLogic(IsoMessage request) {
        try {
            logger.info("Processing financial transaction");

            // Extract key fields
            String pan = ByteArrayUtil.toHexString((byte[]) request.getField(2).getValue());
            int processingCode = Integer.parseInt(String.valueOf(request.getField(3).getValue()));
            String amount = String.valueOf(request.getField(4).getValue());
            String merchantId = request.hasField(42) ? String.valueOf(request.getField(42).getValue()) : "UNKNOWN";

            logger.info("Financial Transaction - PAN: {}, Processing Code: {}, Amount: {}, Merchant: {}",
                maskPAN(pan), processingCode, amount, merchantId);

            // Simulate financial transaction processing
            boolean isProcessed = simulateFinancialTransaction(pan, processingCode, amount, merchantId);

            if (isProcessed) {
                logger.info("Financial transaction processed successfully");
                IsoMessage customResponse = createSuccessResponse(request);
                return new ProcessingResult(true, ISOResponseCode.SUCCESS, "Transaction approved", customResponse);
            } else {
                logger.info("Financial transaction failed");
                return new ProcessingResult(false, ISOResponseCode.ERROR, "Transaction processing failed");
            }

        } catch (Exception e) {
            logger.error("Error processing financial transaction", e);
            return new ProcessingResult(false, ISOResponseCode.ERROR, "Financial transaction processing error");
        }
    }

    @Override
    public int getMTI() {
        return 0x200; // 0200 in hex
    }

    /**
     * Simulate financial transaction processing
     */
    private boolean simulateFinancialTransaction(String pan, int processingCode, String amount, String merchantId) {
        try {
            long amountValue = Long.parseLong(amount);

            // Simulate different transaction types based on processing code
            switch (processingCode) {
                case 0: // Purchase
                    logger.info("Processing purchase transaction");
                    return amountValue <= 50000; // Limit purchase to 50000

                case 20: // Refund
                    logger.info("Processing refund transaction");
                    return amountValue <= 10000; // Limit refund to 10000

                case 31: // Cash withdrawal
                    logger.info("Processing cash withdrawal");
                    return amountValue <= 20000; // Limit withdrawal to 20000
                case 10: //reversal
                    logger.info("Processing reversal");
                    return true;
                default:
                    logger.warn("Unknown transaction type: {}", processingCode);
                    return false;
            }

        } catch (NumberFormatException e) {
            logger.warn("Invalid amount format: {}", amount);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private IsoMessage createSuccessResponse(IsoMessage request) {
        IsoMessage response = ((MessageFactory<IsoMessage>) messageFactory).createResponse(request);

        // Set response code
        response.setField(39, new IsoValue<>(IsoType.ALPHA, ISOResponseCode.SUCCESS.getCode(), 2));

        // Add retrieval reference number (Field 37)
        response.setField(37, new IsoValue<>(IsoType.ALPHA, RandomUtils.getRandomString(12), 12));

        // Add authorization ID (Field 38)
        response.setField(38, new IsoValue<>(IsoType.ALPHA, RandomUtils.getRandomString(6), 6));

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
