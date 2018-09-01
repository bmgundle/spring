package com.jpmorgan.application.processor;

import com.jpmorgan.application.model.Message;


public interface MessageProcessor {

    /**
     * To check if application is paused or not
     */
//    boolean isPaused();

    /**
     * This method will start processing the received message
     */
    boolean processMessage(Message message);
}
