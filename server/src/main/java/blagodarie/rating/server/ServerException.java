package blagodarie.rating.server;

public class ServerException
        extends Exception {

    private final int mCode;

    ServerException (
            final String message,
            final int code
    ) {
        super(message);
        mCode = code;
    }

    public int getCode () {
        return mCode;
    }
}
