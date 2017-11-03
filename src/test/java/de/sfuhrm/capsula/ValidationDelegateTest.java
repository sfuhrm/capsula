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

import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test for {@link ValidationDelegate}.
 * @author Stephan Fuhrmann
 */
public class ValidationDelegateTest {

    static class Foo {
        @NotNull
        public String bar;
    }

    @Test
    public void testNewInstance() {
        new ValidationDelegate();
    }

    @Test
    public void testValidateWithNoErrors() {
        ValidationDelegate delegate = new ValidationDelegate();
        Foo test = new Foo();
        test.bar = "baz";
        Set<ConstraintViolation<Foo>> constraintViolations = delegate.validate(test);
        assertEquals(Collections.emptySet(), constraintViolations);
    }

    @Test
    public void testValidateWithOneError() {
        ValidationDelegate delegate = new ValidationDelegate();
        Foo test = new Foo();
        Set<ConstraintViolation<Foo>> constraintViolations = delegate.validate(test);
        assertEquals(1, constraintViolations.size());
    }
}
