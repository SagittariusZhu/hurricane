package org.iipg.hurricane.jmx.client;

public class JMXException extends Exception {
	
    /**
     * Constructor with a simple message
     *
     * @param message exception description
     */
    public JMXException(String message) {
        super(message);
    }

    /**
     * Exception with a nested exception
     *
     * @param message description of this exception
     * @param cause exception causing this exception
     */
    public JMXException(String message, Throwable cause) {
        super(message, cause);
    }
}
