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
package de.sfuhrm.capsula.yaml;

import lombok.Getter;
import lombok.Setter;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the {@link PropertyInheritance}.
 * @author Stephan Fuhrmann
 */
public class PropertyInheritanceTest {
    static class A {
        @Getter @Setter
        String a;
        
        @Getter @Setter
        Integer b;
    }
    
    static class CopyOfA {
        @Getter @Setter
        String a;
        
        @Getter @Setter
        Integer b;
    }
    
    static class AwithoutB {
        @Getter @Setter
        String a;
    }
    
    @Test
    public void testInheritWithAllCopy() {
        A a = new A();
        CopyOfA coa = new CopyOfA();
        a.setA("foo");
        a.setB(42);
        PropertyInheritance.inherit(a, coa);
        
        assertEquals("foo", a.getA());
        assertEquals(Integer.valueOf(42), a.getB());
        assertEquals("foo", coa.getA());
        assertEquals(Integer.valueOf(42), coa.getB());
    }
    
    @Test
    public void testInheritWithNoCopy() {
        A a = new A();
        CopyOfA coa = new CopyOfA();
        a.setA("foo");
        a.setB(42);
        coa.setA("bar");
        coa.setB(43);
        PropertyInheritance.inherit(a, coa);
        
        assertEquals("foo", a.getA());
        assertEquals(Integer.valueOf(42), a.getB());
        assertEquals("bar", coa.getA());
        assertEquals(Integer.valueOf(43), coa.getB());
    }
    
    @Test
    public void testInheritWithPartialA() {
        A a = new A();
        AwithoutB aWithoutB = new AwithoutB();
        a.setA("foo");
        a.setB(42);
        PropertyInheritance.inherit(a, aWithoutB);
        
        assertEquals("foo", a.getA());
        assertEquals(Integer.valueOf(42), a.getB());
        assertEquals("foo", aWithoutB.getA());
    }
}
