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
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
/**
 * Validates an object tree using Bean validation.
 * @author Stephan Fuhrmann
 */
@Slf4j
public class ValidationDelegate {
    /** Bean validator to use. */
    private final Validator validator;
    public ValidationDelegate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    /**
     * Validates the given object. Prints all constraint violations.
     *
     * @param <T> the class of the object to check and the constraint violations.
     * @param o the object to validate.
     * @return the set of constraint violations detected.
     */
    public final <T> Set<ConstraintViolation<T>> validate(final T o) {
        final Set<ConstraintViolation<T>> violations = validator.validate(o);
        if (violations.size() > 0) {
            System.err.println("YAML config contains errors:");
            violations.forEach(u -> {
                log.error("Validation error for {} {}. ",
                        u.getPropertyPath().toString(), u.getMessage());
                System.err.printf("  \"%s\"" + " %s (value: %s)%n",
                        u.getPropertyPath().toString(),
                        u.getMessage(),
                        u.getInvalidValue()
                        );
            });
            System.err.printf("Got %d validation errors%n", violations.size());
        } else {
            log.debug("Object validated");
        }
        return violations;
    }
}
