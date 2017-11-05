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

    /** The operator for the version declaration. */
    @Getter
    @Setter
    private VersionOperator op;

    /**
     * The reference version that is considered together with the
     * {@link #op operator}.
     */
    @Getter
    @Setter
    private String version;

    /**
     * The type of relation.
     * @see <a
     * href="https://www.debian.org/doc/manuals/maint-guide/dreq.en.html">
     * Debian Maintainer Guide</a>
     * @see <a href="http://rpm.org/user_doc/dependencies.html">
     *     RPM guide about dependencies</a>
     */
    public enum RelationType {
        /** The package will not be installed unless the packages it depends
         * on are installed. */
        depends,
        /** Use this for packages that are not strictly necessary but
         * are typically used with your program.
         * */
        recommends,
        /** Use this for packages which will work nicely with your program
         * but are not at all necessary. */
        suggests,
        /** The package will not be installed until all the packages it
         * conflicts with have been removed. */
        conflicts,
        /** When installed the package will break all the listed packages. */
        breaks,
        /** For some types of packages where there are multiple alternatives,
         * virtual names have been defined. */
        provides,
        /** Use this when your program replaces files from another package,
         * or completely replaces another package (used in conjunction with
         * Conflicts). */
        replaces;
    }

    /** An operator that compares the actual package version with
     * a version in the package specification.
     * */
    public enum VersionOperator {
        /** The version exactly matches the given version. */
        eq("="),
        /** The version is greater than the given version. */
        gt(">"),
        /** The version is greater or equal the given version. */
        ge(">="),
        /** The version is less than the given version. */
        lt("<"),
        /** The version is less than or equal the given version. */
        le("<=");

        /** The String for the operator to use in templates. */
        @Getter
        private final String operator;

        /**
         * Creates a new operator.
         * @param myOperator the operator textual representation
         *                   as required in the templates.
         */
        VersionOperator(final String myOperator) {
            this.operator = Objects.requireNonNull(myOperator);
        }
    }
}
