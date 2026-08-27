package com.company.salonbooking.infrastructure.messaging;

import org.springframework.amqp.core.MessageProperties;

/** Small helper to clone MessageProperties before mutating headers/expiration for a retry. */
final class MessagePropertiesBuilderCopy {

    private MessagePropertiesBuilderCopy() {
    }

    static MessageProperties copy(MessageProperties source) {
        MessageProperties copy = new MessageProperties();
        copy.setContentType(source.getContentType());
        source.getHeaders().forEach(copy::setHeader);
        return copy;
    }
}