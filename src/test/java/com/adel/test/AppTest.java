package com.adel.test;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppTest {

    @Test
    public void testAddition() {
        int a = 5 + 3;
        assertEquals(8, a);
    }

    @Test
    public void testString() {
        String name = "Адель";
        assertTrue(name.startsWith("А"));
    }

    @Test
    public void testNotNull() {
        Object obj = new Object();
        assertNotNull(obj);
    }
}
