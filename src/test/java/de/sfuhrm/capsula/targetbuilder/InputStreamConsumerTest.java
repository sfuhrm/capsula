package de.sfuhrm.capsula.targetbuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

import de.sfuhrm.capsula.TargetLocator;
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
