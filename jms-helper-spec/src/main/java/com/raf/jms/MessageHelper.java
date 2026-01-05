package com.raf.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

public interface MessageHelper {

    <T> T getMessage(Message message, Class<T> clazz) throws JMSException;

    String createTextMessage(Object object);

}
