package blagodarie.rating.server;

public class HttpException
        extends Exception {

    private final int mCode;

    public HttpException (
            final int code,
            final String message
    ) {
        super(message);
        mCode = code;
    }

    public int getCode () {
        return mCode;
    }
}
