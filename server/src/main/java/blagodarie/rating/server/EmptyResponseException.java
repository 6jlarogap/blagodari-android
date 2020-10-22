package blagodarie.rating.server;

public class EmptyResponseException
        extends IllegalStateException {

    public EmptyResponseException () {
        super("Empty response");
    }

}
