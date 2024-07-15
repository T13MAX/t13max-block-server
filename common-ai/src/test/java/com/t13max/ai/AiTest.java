package com.t13max.ai;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author: t13max
 * @since: 10:20 2024/7/15
 */
public class AiTest extends TestCase {

    public AiTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AiTest.class);
    }


    public void testApp() {
        assertTrue(true);
    }
}
