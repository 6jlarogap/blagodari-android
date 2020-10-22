package blagodarie.rating.server;

public class BadRequestException
        extends HttpException {

    BadRequestException (
            final int code,
            final String message
    ) {
        super(code, message);
    }
}
