package blagodarie.rating.server;

public class ServerInternalException
        extends HttpException {

    ServerInternalException (
            final int code,
            final String message
    ) {
        super(code, message);
    }

}
