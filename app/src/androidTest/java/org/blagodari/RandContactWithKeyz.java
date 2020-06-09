package org.blagodari;

import androidx.annotation.NonNull;

import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.Keyz;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RandContactWithKeyz {

    private final Random mRandom = new Random();
    private RandKeyz mRandKeyz = new RandKeyz();
    private RandContact mRandContact = new RandContact();

    public RandContactWithKeyz () {
    }

    public final ContactWithKeyz getNext (final int maxKeyzCount) {
        final Contact contact = mRandContact.getNext();
        int keyzCount = mRandom.nextInt(maxKeyzCount + 1);
        final List<Keyz> keyzList = new ArrayList<>();
        for (int i = 0; i < keyzCount; i++) {
            keyzList.add(mRandKeyz.getNext());
        }
        return new ContactWithKeyz(contact, keyzList);
    }

    public final List<ContactWithKeyz> getList (final int count, final int maxKeyzCount) {
        final List<ContactWithKeyz> contactWithKeyzList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            contactWithKeyzList.add(getNext(maxKeyzCount));
        }
        return contactWithKeyzList;
    }

    public final ContactWithKeyz getRandFromList (@NonNull final List<ContactWithKeyz> contactWithKeyzList) {
        final int randIndex = mRandom.nextInt(contactWithKeyzList.size());
        return contactWithKeyzList.get(randIndex);
    }

    public final List<ContactWithKeyz> getRandSublistFromList (
            @NonNull final List<ContactWithKeyz> contactWithKeyzList,
            final int sublistSize
    ) {
        final List<ContactWithKeyz> randSublist = new ArrayList<>(sublistSize);
        while(randSublist.size() < sublistSize) {
                final ContactWithKeyz randContactsWithKeyzFromList = getRandFromList(contactWithKeyzList);
                if (!randSublist.contains(randContactsWithKeyzFromList)) {
                    randSublist.add(randContactsWithKeyzFromList);
                }
        }
        return randSublist;
    }
}
