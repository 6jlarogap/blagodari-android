package org.blagodari.contacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import org.junit.Test;
import androidx.test.core.app.ApplicationProvider;

import org.blagodari.AccountGeneral;

import static android.content.Context.ACCOUNT_SERVICE;
import static org.junit.Assert.assertNotNull;

public class ContactRepositoryTest {

    @Test
    public void asdf () {
        Context context = ApplicationProvider.getApplicationContext();
        final Account account = new Account(AccountGeneral.ACCOUNT_NAME, AccountGeneral.ACCOUNT_TYPE);
        final AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        ContactRepository contactRepository = new ContactRepository(ApplicationProvider.getApplicationContext().getContentResolver(),accountManager, account);
        //List<ContactWithKeyz>  contactWithKeyzList = contactRepository.getAll();
        //assertNotNull(contactWithKeyzList);
    }
}
