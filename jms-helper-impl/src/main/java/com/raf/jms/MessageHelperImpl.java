package com.raf.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageHelperImpl implements MessageHelper {

    private final Validator validator;
    private final ObjectMapper objectMapper;

    public MessageHelperImpl() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public <T> T getMessage(Message message, Class<T> clazz) throws JMSException {
        if (!(message instanceof TextMessage)) {
            throw new JMSException("Expected TextMessage");
        }

        try {
            String json = ((TextMessage) message).getText();
            T data = objectMapper.readValue(json, clazz);

            Set<ConstraintViolation<T>> violations = validator.validate(data);
            if (!violations.isEmpty()) {
                throw new RuntimeException(
                        violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "))
                );
            }

            return data;

        } catch (IOException e) {
            throw new RuntimeException("Message parsing failed", e);
        }
    }

    @Override
    public String createTextMessage(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Message serialization failed", e);
        }
    }
}
