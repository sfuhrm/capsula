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
import lombok.Setter;

/**
 * Specific descriptor for Redhat distribution.
 * TBD this isn't Redhat anymore
 * @author Stephan Fuhrmann
 */
public class Redhat extends Distribution {

    /** The package group of the Fedora package.
     * TBD AFAIK this is deprecated in Fedora, check
     * */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    private String group;

    /** The RPM build architecture.
     * If the package is not architecture dependent, for example
     * written entirely in an interpreted programming language,
     * this should be {@code BuildArch: noarch} otherwise it
     * will automatically inherit the Architecture of the
     * machine it’s being built on.
     * */
    enum BuildArch {
        /** Interpreted programming language. */
        noarch,
        /** x86 64 bit architecture. */
        x86_64
    }

    /** The build architecture to set.
     * If the package is not architecture dependent, for example
     * written entirely in an interpreted programming language,
     * this should be {@code BuildArch: noarch} otherwise it
     * will automatically inherit the Architecture of the
     * machine it’s being built on.
     * */
    @Getter
    @Setter
    private BuildArch buildArch = BuildArch.noarch;
}
