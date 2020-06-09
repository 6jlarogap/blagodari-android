package org.blagodari.db.scheme;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyzTypeEntityTest {

    @Test
    public void testFullConstructor () {
        Long id = 1L;
        String title = "testTitle";

        KeyzType keyzType = new KeyzType(id, title);

        assertEquals(keyzType.getId(), id);
        assertEquals(keyzType.getTitle(), title);

        System.out.println("toString" + keyzType.toString());
    }

}
