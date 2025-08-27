package com.titp.server.utils;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;

public class IsoMessageUtils {
    private final MessageFactory<IsoMessage> messageFactory;

    public IsoMessageUtils(MessageFactory<IsoMessage> messageFactory) {
        this.messageFactory = messageFactory;
    }

    public IsoMessage createSuccessResponse(IsoMessage request, ISOResponseCode responseCode) {
        IsoMessage response = messageFactory.createResponse(request);

        // Set response code
        response.setField(39, new IsoValue<>(IsoType.ALPHA, responseCode.getCode(), 2));

        return response;
    }
}
