package org.blagodari.server;

public final class ResponseException extends Exception {

    public ResponseException () {
        super("Unrecognized response");
    }

}
