package de.sfuhrm.capsula;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the {@link Params}.
 *
 * @author Stephan Fuhrmann
 */
public class ParamsTest {

    @Test
    public void testParseWithHelp() {
        Params p = Params.parse(new String[] { "-h"});
        assertNull(p);
    }
}
