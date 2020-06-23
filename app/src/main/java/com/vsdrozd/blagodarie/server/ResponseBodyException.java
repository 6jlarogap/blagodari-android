package com.vsdrozd.blagodarie.server;

public final class ResponseBodyException extends Exception {

    public ResponseBodyException () {
        super("Response body is null");
    }
}
