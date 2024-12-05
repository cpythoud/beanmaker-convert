package org.beanmaker.converters.client;

public class ConversionException extends Exception {

    public ConversionException() {
        super();
    }

    // Constructor that accepts a custom message
    public ConversionException(String message) {
        super(message);
    }

    // Constructor that accepts a custom message and a cause
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public ConversionException(Throwable cause) {
        super(cause);
    }

}
