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
