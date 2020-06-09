package com.vsdrozd.blagodarie.db.generators;

import com.vsdrozd.blagodarie.db.scheme.User;

public final class UserGenerator
        extends AbstractGenerator<User> {

    private static final UserGenerator INSTANCE = new UserGenerator();
    private static long mNumber = 0;

    private UserGenerator () {
    }

    public static UserGenerator getInstance () {
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
    protected final User generate () {
        return new User();
    }

}
