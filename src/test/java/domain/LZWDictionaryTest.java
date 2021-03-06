package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

public class LZWDictionaryTest {

    private Dictionary dict;

    private ByteSequence k1;
    private Integer v1;

    @BeforeEach
    public void setUp() {
        dict = new LZWDictionary();
    }

    @Test
    public void putPutsOneEntryInDictionaryAndGetReturnsItsValue() {
        k1 = new ByteSequence(new byte[]{(byte) 0, (byte) 5, (byte) 255, (byte) 43, (byte) 79, (byte) 104});
        v1 = 51;
        dict.put(k1, v1);
        assertEquals(v1, dict.get(k1));
    }

    @Test
    public void getReturnsNullWhenKeyIsNotInDictionary() {
        k1 = new ByteSequence(new byte[]{(byte) 3, (byte) 129, (byte) 78});
        assertNull(dict.get(k1));
    }

    @Test
    public void getReturnsCorrectValuesWhenManyEntriesArePutInDictionaryInAnOrderKindaPlausibleForLZW() {

        k1 = new ByteSequence(new byte[]{(byte) 5});
        ByteSequence k2 = new ByteSequence(new byte[]{(byte) 7});
        ByteSequence k3 = new ByteSequence(new byte[]{(byte) 5, (byte) 231});
        ByteSequence k4 = new ByteSequence(new byte[]{(byte) 5, (byte) 231, (byte) 101});
        ByteSequence k5 = new ByteSequence(new byte[]{(byte) 5, (byte) 231, (byte) 101, (byte) 89});

        v1 = 51;
        Integer v2 = 98;
        Integer v3 = 305;
        Integer v4 = 3009;
        Integer v5 = 2074;

        dict.put(k1, v1);
        dict.put(k2, v2);

        assertEquals(v1, dict.get(k1));
        assertNull(dict.get(k3));
        dict.put(k3, v3);

        assertEquals(v1, dict.get(k1));
        assertEquals(v3, dict.get(k3));
        assertNull(dict.get(k4));
        dict.put(k4, v4);

        assertEquals(v1, dict.get(k1));
        assertEquals(v3, dict.get(k3));
        assertEquals(v4, dict.get(k4));
        assertNull(dict.get(k5));
        dict.put(k5, v5);

        assertEquals(v1, dict.get(k1));
        assertEquals(v3, dict.get(k3));
        assertEquals(v4, dict.get(k4));
        assertEquals(v5, dict.get(k5));
    }
}
