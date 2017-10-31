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

import java.util.Objects;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Relation definition to other packages.
 *
 * @author Stephan Fuhrmann
 */
public class Relation {

    /**
     * The name of the related package in the distributions repository.
     */
    @Getter
    @Setter
    @NotEmpty
    private String pkg;
    /**
     * This is the the release number for this version. The first release gets
     * number 1.
     */
    @Getter
    @Setter
    @NotNull
    private RelationType type = RelationType.depends;
    @Getter
    @Setter
    private VersionOperator op;
    /**
     * The reference version.
     */
    @Getter
    @Setter
    private String version;

    /**
     * @see https://www.debian.org/doc/manuals/maint-guide/dreq.en.html
     * @see http://rpm.org/user_doc/dependencies.html
     */
    public enum RelationType {
        depends,
        recommends,
        suggests,
        conflicts,
        breaks,
        provides,
        replaces;
    }

    public enum VersionOperator {
        eq("="),
        gt(">"),
        ge(">="),
        lt("<"),
        le("<=");
        @Getter
        private final String operator;

        private VersionOperator(final String op) {
            this.operator = Objects.requireNonNull(op);
        }
    }
}
