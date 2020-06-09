package org.blagodari.repository.ppp;

import androidx.test.core.app.ApplicationProvider;

import org.blagodari.repository.java.DataRepository;
import org.blagodari.repository.java.Key;
import org.blagodari.repository.java.KeyType;
import org.junit.Test;

public class TestClass {
    @Test
    public void asdf(){
        DataRepository repository = DataRepository.getInstance(ApplicationProvider.getApplicationContext());
        Key key = repository.getKeyRepository().create("asdf", KeyType.Type.PHONE_NUMBER);
    }
}
