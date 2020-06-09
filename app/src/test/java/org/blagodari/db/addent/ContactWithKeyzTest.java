package org.blagodari.db.addent;

import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContactWithKeyzTest {

    @Test
    public void equalsTest(){
        final Contact contact1 = new Contact("contact 1");
        final Keyz keyz1 = new Keyz("phone 1", KeyzType.Types.PHONE_NUMBER.getId());
        final Keyz keyz2 = new Keyz("email 1", KeyzType.Types.EMAIL.getId());
        final Set<Keyz> keyzList1 = new HashSet<>();
        keyzList1.add(keyz1);
        keyzList1.add(keyz2);

        final Contact contact2 = new Contact("contact 1");
        final Keyz keyz3 = new Keyz("phone 1", KeyzType.Types.PHONE_NUMBER.getId());
        final Keyz keyz4 = new Keyz("email 1", KeyzType.Types.EMAIL.getId());
        final Set<Keyz> keyzList2 = new HashSet<>();
        keyzList2.add(keyz3);
        keyzList2.add(keyz4);

        final ContactWithKeyz contactWithKeyz1 = new ContactWithKeyz(contact1, keyzList1);
        final ContactWithKeyz contactWithKeyz2 = new ContactWithKeyz(contact2, keyzList2);

        assertEquals(contactWithKeyz1, contactWithKeyz2);
    }

    @Test
    public void notEqualsContactsTest(){
        final Contact contact1 = new Contact("contact 1");
        final Keyz keyz1 = new Keyz("phone 1", KeyzType.Types.PHONE_NUMBER.getId());
        final Keyz keyz2 = new Keyz("email 1", KeyzType.Types.EMAIL.getId());
        final Set<Keyz> keyzList1 = new HashSet<>();
        keyzList1.add(keyz1);
        keyzList1.add(keyz2);

        final Contact contact2 = new Contact("contact 2");
        final Keyz keyz3 = new Keyz("phone 1", KeyzType.Types.PHONE_NUMBER.getId());
        final Keyz keyz4 = new Keyz("email 1", KeyzType.Types.EMAIL.getId());
        final Set<Keyz> keyzList2 = new HashSet<>();
        keyzList2.add(keyz3);
        keyzList2.add(keyz4);

        final ContactWithKeyz contactWithKeyz1 = new ContactWithKeyz(contact1, keyzList1);
        final ContactWithKeyz contactWithKeyz2 = new ContactWithKeyz(contact2, keyzList2);

        assertNotEquals(contactWithKeyz1, contactWithKeyz2);
    }

    @Test
    public void notEqualsKeyzTest(){
        final Contact contact1 = new Contact("contact 1");
        final Keyz keyz1 = new Keyz("phone 1", KeyzType.Types.PHONE_NUMBER.getId());
        final Keyz keyz2 = new Keyz("email 1", KeyzType.Types.EMAIL.getId());
        final Set<Keyz> keyzList1 = new HashSet<>();
        keyzList1.add(keyz1);
        keyzList1.add(keyz2);

        final Contact contact2 = new Contact("contact 1");
        final Keyz keyz3 = new Keyz("phone 2", KeyzType.Types.PHONE_NUMBER.getId());
        final Keyz keyz4 = new Keyz("email 1", KeyzType.Types.EMAIL.getId());
        final Set<Keyz> keyzList2 = new HashSet<>();
        keyzList2.add(keyz3);
        keyzList2.add(keyz4);

        final ContactWithKeyz contactWithKeyz1 = new ContactWithKeyz(contact1, keyzList1);
        final ContactWithKeyz contactWithKeyz2 = new ContactWithKeyz(contact2, keyzList2);

        assertNotEquals(contactWithKeyz1, contactWithKeyz2);
    }

    @Test
    public void notEqualsKeyzCountTest(){
        final Contact contact1 = new Contact("contact 1");
        final Keyz keyz1 = new Keyz("phone 1", KeyzType.Types.PHONE_NUMBER.getId());
        final Keyz keyz2 = new Keyz("email 1", KeyzType.Types.EMAIL.getId());
        final Set<Keyz> keyzList1 = new HashSet<>();
        keyzList1.add(keyz1);
        keyzList1.add(keyz2);

        final Contact contact2 = new Contact("contact 1");
        final Keyz keyz3 = new Keyz("phone 2", KeyzType.Types.PHONE_NUMBER.getId());
        final Keyz keyz4 = new Keyz("email 1", KeyzType.Types.EMAIL.getId());
        final Keyz keyz5 = new Keyz("email 2", KeyzType.Types.EMAIL.getId());
        final Set<Keyz> keyzList2 = new HashSet<>();
        keyzList2.add(keyz3);
        keyzList2.add(keyz4);
        keyzList2.add(keyz5);

        final ContactWithKeyz contactWithKeyz1 = new ContactWithKeyz(contact1, keyzList1);
        final ContactWithKeyz contactWithKeyz2 = new ContactWithKeyz(contact2, keyzList2);

        assertNotEquals(contactWithKeyz1, contactWithKeyz2);
    }
}
