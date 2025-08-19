package com.titp.server.processor;

import com.solab.iso8583.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating and managing MTI processors.
 * Uses the Factory pattern to provide the appropriate processor for each MTI.
 */
public class ProcessorFactory {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorFactory.class);
    
    private static final Map<Integer, MTIProcessor> processors = new HashMap<>();
    private static MessageFactory<?> messageFactory;
    
    /**
     * Initialize the processor factory with a message factory
     * @param factory The message factory to use for creating responses
     */
    public static void initialize(MessageFactory<?> factory) {
        messageFactory = factory;
        
        // Initialize all processors
        processors.put(0x100, new AuthorizationProcessor(messageFactory));      // 0100
        processors.put(0x200, new FinancialProcessor(messageFactory));          // 0200
        processors.put(0x800, new NetworkManagementProcessor(messageFactory));  // 0800
        
        logger.info("Initialized {} MTI processors", processors.size());
    }
    
    /**
     * Get the appropriate processor for the given MTI.
     * @param mti The Message Type Indicator
     * @return The processor for the MTI, or null if not supported
     */
    public static MTIProcessor getProcessor(int mti) {
        MTIProcessor processor = processors.get(mti);
        if (processor == null) {
            logger.warn("No processor found for MTI: {}", String.format("%04X", mti));
        }
        return processor;
    }
    
    /**
     * Check if a processor exists for the given MTI.
     * @param mti The Message Type Indicator
     * @return true if a processor exists, false otherwise
     */
    public static boolean hasProcessor(int mti) {
        return processors.containsKey(mti);
    }
    
    /**
     * Get all supported MTIs.
     * @return Array of supported MTI values
     */
    public static int[] getSupportedMTIs() {
        return processors.keySet().stream().mapToInt(Integer::intValue).toArray();
    }
    
    /**
     * Get the number of supported processors.
     * @return The number of processors
     */
    public static int getProcessorCount() {
        return processors.size();
    }
}
