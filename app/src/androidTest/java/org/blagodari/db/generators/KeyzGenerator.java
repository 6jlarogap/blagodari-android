package org.blagodari.db.generators;

import org.blagodari.db.scheme.Keyz;

public class KeyzGenerator
extends AbstractGenerator<Keyz>{

    private static final KeyzGenerator INSTANCE = new KeyzGenerator();
    private static long mNumber = 0;

    private KeyzGenerator () {
    }

    public static KeyzGenerator getInstance () {
        return INSTANCE;
    }

    @Override
    protected void incrementNumber () {
        ++mNumber;
    }

    @Override
    protected long getNumber () {
        return mNumber;
    }

    @Override
    protected final Keyz generate () {
        /*Keyz keyz = new Keyz("value" + getNumber(), )
        return new Keyz(getNumber(), "title" + getNumber());*/
        return null;
    }

}
