package blagodarie.rating.server;

public final class UnauthorizedException
        extends ServerException {

    UnauthorizedException () {
        super("Unauthorized request", 401);
    }

}