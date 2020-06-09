package com.vsdrozd.blagodarie.ui.contacts;

enum ContactsOrder {

    NAME,
    UPDATE_TIMESTAMP,
    FAME,
    LIKES_COUNT,
    SUM_LIKES_COUNT;

    private static ContactsOrder DEFAULT_ORDER = FAME;

    static ContactsOrder getDefault () {
        return DEFAULT_ORDER;
    }
}
