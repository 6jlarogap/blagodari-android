package com.vsdrozd.blagodarie;

import android.util.ArraySet;

import com.vsdrozd.blagodarie.db.addent.ContactWithKeyz;
import com.vsdrozd.blagodarie.db.scheme.Contact;

import org.junit.Test;

import java.util.Comparator;
import java.util.TreeSet;

public class TempTests {

    @Test
    public void tempTest () {
        Comparator<ContactWithKeyz> contactComparator = new Comparator<ContactWithKeyz>() {
            @Override
            public int compare (ContactWithKeyz o1, ContactWithKeyz o2) {
                return o1.getContact().getTitle().compareTo(o2.getContact().getTitle());
            }
        };
        TreeSet<ContactWithKeyz> contactWithKeyz = new TreeSet<>(contactComparator);
        contactWithKeyz.add(new ContactWithKeyz(new Contact("thrhw"), new ArraySet<>()));
        contactWithKeyz.add(new ContactWithKeyz(new Contact("ewrggrtj"), new ArraySet<>()));
        contactWithKeyz.add(new ContactWithKeyz(new Contact("dfbhtyj"), new ArraySet<>()));
        contactWithKeyz.add(new ContactWithKeyz(new Contact("dsfgrt"), new ArraySet<>()));
        contactWithKeyz.add(new ContactWithKeyz(new Contact("564987"), new ArraySet<>()));
        contactWithKeyz.add(new ContactWithKeyz(new Contact("rgrewgwr"), new ArraySet<>()));
        contactWithKeyz.add(new ContactWithKeyz(new Contact("6hehhe"), new ArraySet<>()));
        System.out.println(contactWithKeyz);
    }

}
