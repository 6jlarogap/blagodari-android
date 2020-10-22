package blagodarie.rating.server;

public class BadAuthorizationTokenException
        extends BadRequestException {

    BadAuthorizationTokenException () {
        super(401, "Bad authorization token");
    }

}
