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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Specific descriptor for Debian distribution.
 *
 * @see <a href="https://www.debian.org/doc/debian-policy/">
 *     Debian Policy Manual</a>
 * @author Stephan Fuhrmann
 */
public class Debian extends Distribution {

    /** Priority of a package.
     * @see <a href="https://www.debian.org/doc/debian-policy/">
     *     Debian Policy Manual</a>
     * */
    enum Priority {
        /** This is the default priority for the majority of the archive. */
        optional,
        /** Packages which are necessary for the proper functioning of
         * the system. */
        required,
        /** Important programs, including those which one would expect to
         * find on any Unix-like system. */
        important,
        /** These packages provide a reasonably small but
         * not too limited character-mode system. */
        standard
    }

    /** Debian-specific Priority of this package. */
    @Getter
    @NotNull
    private Priority priority = Priority.optional;

    /** The Debian section this package belongs to. */
    @Getter
    @NotNull
    @NotBlank
    private String section;

    /** The Debian specific architecture of this package. */
    enum Architecture {
        /** Any matches all Debian machine architectures and
         * is the most frequently used. */
        any,
        /** Indicates an architecture-independent package. */
        all,
        /** Indicates a source package. */
        source
    }

    /** The Debian specific architecture of this package. */
    @Getter
    @NotNull
    private Architecture architecture;

    /**
     * The name of the distribution this package release is for.
     */
    @Getter
    @NotNull
    private String distribution = "unstable";

    /** The Debian specific urgency. */
    enum Urgency {
        /** High urgency. */
        high,
        /** Medium urgency. */
        medium,
        /** Low urgency. */
        low
    }

    /**
     * The urgency of this release.
     * @see https://www.debian.org/doc/manuals/developers-reference/ch05.en.html
     */
    @Getter
    @NotNull
    private Urgency urgency = Urgency.medium;
}
