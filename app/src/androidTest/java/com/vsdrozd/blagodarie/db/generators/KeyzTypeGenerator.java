package com.vsdrozd.blagodarie.db.generators;

import com.vsdrozd.blagodarie.db.scheme.KeyzType;

public final class KeyzTypeGenerator
        extends AbstractGenerator<KeyzType> {

    private static final KeyzTypeGenerator INSTANCE = new KeyzTypeGenerator();
    private static long mNumber = 0;

    private KeyzTypeGenerator () {
    }

    public static KeyzTypeGenerator getInstance () {
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
    protected final KeyzType generate () {
        return new KeyzType(getNumber(), "title" + getNumber());
    }

}
