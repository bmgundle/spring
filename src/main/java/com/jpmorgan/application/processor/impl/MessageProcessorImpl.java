package com.jpmorgan.application.processor.impl;

import com.jpmorgan.application.helper.ReportGenerator;
import com.jpmorgan.application.model.ApplicationTracker;
import com.jpmorgan.application.model.Message;
import com.jpmorgan.application.model.MessageType;
import com.jpmorgan.application.processor.MessageProcessor;
import com.jpmorgan.application.repository.impl.MessageRepositoryImpl;
import com.jpmorgan.application.util.DataStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MessageProcessorImpl implements MessageProcessor {

    public static Logger logger = LogManager.getLogger(MessageProcessorImpl.class);

    @Autowired
    private MessageRepositoryImpl messageRepositoryImpl;

    @Autowired
    private ReportGenerator reportGenerator;

    @Autowired
    DataStore dataStore;

    @Autowired
    ApplicationTracker applicationTracker;

    /**
     * This method will start processing the received message and does following things
     *
     * 1 validate message
     * 2 record message
     * 3 if message type is adjustment it will update data store for existing stock
     * 4 if message type is single or multi sale type then process and store in data store
     * 5 call the report generator method which will print data on console after 10th message
     */
    @Override
    public boolean processMessage(Message message) {
        logger.info("received message type {} for processing : {}", message.getMessageType());

        //first validate the received message
        if (validateMessage(message)){

            //record the message

            if (MessageType.ADJUSTMENT_EVENT.equals(message.getMessageType())) {
                return updateStockPriceInDataStoreAsPerAdjustmentOperation(message);
            }
            applicationTracker.recordMessage();
            //proceed to store the message into data store
            messageRepositoryImpl.processMessage(message);

            //send for report printing method
            generateMessageReport();
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }

    //TODO : Validation needs to be implemented based on MessageType
    private boolean validateMessage(Message message) {
        return Boolean.TRUE;
    }

    /**
     * This method will update data store for existing stock
     */
    private Boolean updateStockPriceInDataStoreAsPerAdjustmentOperation(Message currentMessage)
    {
        for(Message message: dataStore.getMessages()) {

            if(message.getProductName().equals(currentMessage.getProductName())){

                message.setPriceBeforeAdjustment(message.getPrice());
                message.setAdjustmentPrice(currentMessage.getAdjustmentPrice());

                switch (currentMessage.getOperationType()) {
                    case ADD:
                        message.setPrice(message.getPrice().add(currentMessage.getAdjustmentPrice())
                                .multiply(BigDecimal.valueOf(message.getNoOfQuantity())));
                        break;
                    case SUBTRACT:
                        message.setPrice(message.getPrice().subtract(currentMessage.getAdjustmentPrice())
                                .multiply(BigDecimal.valueOf(message.getNoOfQuantity())));
                        break;
                    case MULTIPLY:
                        message.setPrice(message.getPrice().multiply(currentMessage.getAdjustmentPrice())
                                .multiply(BigDecimal.valueOf(message.getNoOfQuantity())));
                        break;
                    default:
                        break;
                }

            }

        }
        return Boolean.TRUE;
    }

    /**
     * This method responsible checking if message count is 10
     * then print sale report and if message count
     * is 20 then print report with adjustments made into data store
     */
    private void generateMessageReport() {
        if (applicationTracker.getRecordedMessageCount() % 10 == 0) {
            reportGenerator.printSingleAndMultiSaleReport();
        }
        if (applicationTracker.getRecordedMessageCount() == 20) {
            reportGenerator.printAdjustmentsReport();
            applicationNowPaused();
        }
    }

    /**
     * This method displays application is paused Now on console
     */

    private void applicationNowPaused() {
        logger.info("===================================================");
        logger.info("==         The Application Is Now Paused         ==");
        logger.info("===================================================");
    }
}
