package org.blagodarie.repository.ppp;

import androidx.test.core.app.ApplicationProvider;

import org.blagodarie.repository.java.DataRepository;
import org.blagodarie.repository.java.Key;
import org.blagodarie.repository.java.KeyType;
import org.junit.Test;

public class TestClass {
    @Test
    public void asdf(){
        DataRepository repository = DataRepository.getInstance(ApplicationProvider.getApplicationContext());
        Key key = repository.getKeyRepository().create("asdf", KeyType.Type.PHONE_NUMBER);
    }
}
