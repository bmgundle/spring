package com.jpmorgan.application.helper.impl;

import com.jpmorgan.application.helper.ReportGenerator;
import com.jpmorgan.application.model.Message;
import com.jpmorgan.application.processor.MessageProcessor;
import com.jpmorgan.application.util.DataStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ReportGeneratorImpl implements ReportGenerator {

    @Autowired
    DataStore dataStore;

    @Autowired
    MessageProcessor messageProcessor;

    public static Logger logger = LogManager.getLogger(ReportGeneratorImpl.class);


    /**
     * This method to print single and multi sale messages report
     */
    @Override
    public void printSingleAndMultiSaleReport() {
        logger.info("----------------Sales Report Start----------------");
        BigDecimal totalSale = BigDecimal.ZERO;
        for (Message message : dataStore.getMessages()) {
            if (!message.isProcessed()) {
                logger.info(printSingleAndMultiSaleData(message));
                totalSale = totalSale.add(message.getPrice().multiply(BigDecimal.valueOf(message.getNoOfQuantity())));
                message.setProcessed(Boolean.TRUE);
            }
        }
        logger.info("--------------------------------------------------");
        logger.info("----------TOTAL SALE AMOUNT--------------{}", totalSale);
        logger.info("--------------------------------------------------");
    }

    /**
     * This method to print adjustment report
     */
    @Override
    public void printAdjustmentsReport() {
        logger.info("==========Adjustment Report Start==============");
        BigDecimal totalSale = BigDecimal.ZERO;
        for (Message message : dataStore.getMessages()) {

                logger.info(printAdjustmentData(message));
                totalSale = totalSale.add(message.getPrice().multiply(BigDecimal.valueOf(message.getNoOfQuantity())));

        }
        logger.info("--------------------------------------------------");
        logger.info("----------TOTAL SALE AMOUNT--------------{}", totalSale);
        logger.info("--------------------------------------------------");
    }

    private String printSingleAndMultiSaleData(Message message) {
        return "{" +
                "productName='" + message.getProductName() + '\'' +
                ", price=" + message.getPrice() +
                ", noOfQuantity='" + message.getNoOfQuantity() + '\'' +
                '}';
    }

    private String printAdjustmentData(Message message) {
        return "{" +
                "productName='" + message.getProductName() + '\'' +
                ", price=" + message.getPrice() +
                ", noOfQuantity='" + message.getNoOfQuantity() + '\'' +
                ", priceBefoeAdjustment=" + message.getPriceBeforeAdjustment() +
                ", messageType=" + message.getMessageType() +
                ", adjustmentPrice=" + message.getAdjustmentPrice() +
                '}';
    }

}
