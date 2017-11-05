package de.sfuhrm.capsula.targetbuilder;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RunDelegateTest {
    @Test
    public void testParseWithEmptyString() {
        List<String> actual = RunDelegate.parse("");
        assertEquals(Collections.emptyList(), actual);
    }

    @Test
    public void testParseWithTwoParts() {
        List<String> actual = RunDelegate.parse("echo hello");
        assertEquals(Arrays.asList("echo", "hello"), actual);
    }

    @Test
    public void testParseWithThreeParts() {
        List<String> actual = RunDelegate.parse("echo hello world");
        assertEquals(Arrays.asList("echo", "hello", "world"), actual);
    }

    @Test
    public void testParseWithQuotationMarks() {
        List<String> actual = RunDelegate.parse("echo \"hello world\"");
        assertEquals(Arrays.asList("echo", "hello world"), actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithIllegalQuotes() {
        List<String> actual = RunDelegate.parse("echo \"hello world");
    }
}
