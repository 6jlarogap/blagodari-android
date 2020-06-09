package org.blagodari;

import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;

import java.util.Random;

public class RandKeyz {
    private int mIndex = 0;
    private final Random mRandom = new Random();

    public RandKeyz (){}

    public Keyz getNext(){
        Long keyzTypeId = mRandom.nextInt(KeyzType.Types.values().length - 1) + 1L;
        return new Keyz("value " + (mIndex++), keyzTypeId);
    }
}
