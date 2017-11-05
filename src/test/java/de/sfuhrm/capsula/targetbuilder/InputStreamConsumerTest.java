package de.sfuhrm.capsula.targetbuilder;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the {@link InputStreamConsumer}.
 *
 * @author Stephan Fuhrmann
 */
public class InputStreamConsumerTest {
    @Test
    public void testRun() {
        final List<String> lines = new ArrayList<>();
        Charset charset = Charset.forName("UTF-8");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                "Foo\nbar\nbaz\n".getBytes(charset));
        InputStreamConsumer inputStreamConsumer = new InputStreamConsumer(
                inputStream, s -> lines.add(s), charset);
        inputStreamConsumer.run();

        assertEquals(
                Arrays.asList("Foo", "bar", "baz"),
                lines);
    }
}
