package org.blagodari.server;

public final class ResponseBodyException extends Exception {

    public ResponseBodyException () {
        super("Response body is null");
    }
}
