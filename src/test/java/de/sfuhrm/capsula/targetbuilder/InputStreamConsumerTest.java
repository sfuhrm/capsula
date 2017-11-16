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
                inputStream, lines::add, charset);
        inputStreamConsumer.run();

        assertEquals(
                Arrays.asList("Foo", "bar", "baz"),
                lines);
    }
}
