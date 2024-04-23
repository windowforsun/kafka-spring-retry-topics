package com.windowforsun.kafka.retrytopic.exception;

public class RetryableMessagingException extends RuntimeException{
    public RetryableMessagingException(String message) {
        super(message);
    }
}
