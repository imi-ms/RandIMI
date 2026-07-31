package de.unimuenster.imi.randimi.model.study;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiteTest {

    private final Random random = new Random();
    private Site site;

    @BeforeEach
    public void beforeTests() {
        site = new Site();
    }

    @Test
    public void testGetAndSetCapacity() {
        int testCapacity = random.nextInt();
        site.setCapacity(testCapacity);
        assertEquals(testCapacity, site.getCapacity());
    }
}
