package org.blagodari.server.api;

public interface APIListener<OutputType> {

    void onStart ();

    void onSuccess (OutputType data);

    void onError (Throwable throwable);

}
