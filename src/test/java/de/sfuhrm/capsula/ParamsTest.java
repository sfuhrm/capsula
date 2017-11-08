/*
 * Copyright (C) 2017 Stephan Fuhrmann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
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
