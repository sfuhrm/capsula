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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Inheritance for properties.
 * @author Stephan Fuhrmann
 */
@Slf4j
public class PropertyInheritance {
    private PropertyInheritance () {
    }
    
    public static class InheritanceException extends RuntimeException {

        public InheritanceException(Throwable inner) {
            super(inner);
        }
        
        public InheritanceException(String inner) {
            super(inner);
        }
    }
    
    private static Object readProperty(Method readMethod, Object o) {
        try {
            return readMethod.invoke(o);
        } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
            log.error("Can not read with method {}", readMethod.getName());
            throw new InheritanceException(ex);
        }
    }
    
    /** 
     * Inherits all properties from the parent object to the child object.
     * All null child properties will be looked up in the parent. If the
     * parent has a value for the property, it fills the child.
     * @param parent the object to read property values from.
     * @param child the object to write property values to.
     */
    public static void inherit(final Object parent, final Object child) {
        Objects.requireNonNull(parent, "parent is null");
        Objects.requireNonNull(child, "child is null");
        try {
            final BeanInfo parentDescriptor = Introspector.getBeanInfo(parent.getClass());
            final BeanInfo childDescriptor = Introspector.getBeanInfo(child.getClass());
            
            // map of parent properties
            Map<String, PropertyDescriptor> parentProperties = 
                    Arrays.asList(parentDescriptor.getPropertyDescriptors())
                    .stream()
                    .collect(Collectors.toMap(p -> p.getName(), p -> p));
            
            // try to fill all null child properties with the parent
            Arrays.asList(childDescriptor.getPropertyDescriptors())
                    .stream()
                    .filter(p -> parentProperties.containsKey(p.getName())) // parent has property
                    .filter(p -> readProperty(p.getReadMethod(), child) == null) // child has property null value
                    .forEach(p -> {
                try {
                    Method parentRead = parentProperties.get(p.getName()).getReadMethod();
                    Method childWrite = p.getWriteMethod();
                    if (childWrite == null) {
                        throw new InheritanceException("Child class "+child.getClass().getName()+" write method for "+p.getName()+" missing");
                    }
                    Object value = parentRead.invoke(parent);
                    childWrite.invoke(child, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    log.error("Can not read property {}", p.getName());
                    throw new InheritanceException(ex);
                }
                    });
        } catch (IntrospectionException ex) {
            throw new InheritanceException(ex);
        }
    }
}