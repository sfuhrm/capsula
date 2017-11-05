package de.sfuhrm.capsula;

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

    @Test
    public void testParseWithRequiredMissing() {
        Params p = Params.parse(new String[] { });
        assertNull(p);
    }

    @Test
    public void testParseWithResult() {
        Params p = Params.parse(new String[] {
                "-out", "/tmp",
                "-descriptor", "capsula.yaml"
        });
        assertEquals("/tmp", p.getOut().toString());
        assertEquals("capsula.yaml", p.getDescriptor().toString());
    }
}
