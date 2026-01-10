package com.raf.jms;

import javax.jms.JMSException;
import javax.jms.Message;

public interface MessageHelper {

    <T> T getMessage(Message message, Class<T> clazz) throws JMSException;

    String createTextMessage(Object object);
}
