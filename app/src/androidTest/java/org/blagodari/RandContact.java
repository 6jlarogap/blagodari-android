package org.blagodari;

import org.blagodari.db.scheme.Contact;

import java.util.Random;

public class RandContact {

    private int mIndex = 0;
    private final Random mRandom = new Random();

    public RandContact (){}

    public Contact getNext(){
        final Contact contact = new Contact("title " + (++mIndex));
        if(mRandom.nextBoolean()){
            contact.setPhotoUri("photoUri " + mIndex);
        }
        return contact;
    }
}
