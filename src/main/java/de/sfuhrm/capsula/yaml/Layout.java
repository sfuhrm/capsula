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

import de.sfuhrm.capsula.yaml.command.Command;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Getter;

/**
 * Layout file for one Linux distribution target.
 *
 * @author Stephan Fuhrmann
 */
public class Layout {

    /** The machine version for this layout. */
    @NotNull
    @Getter
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9_]{2,}")
    private String id;

    /** The human readable name for this layout. */
    @NotNull
    @Getter
    @NotBlank
    private String name;

    /** The commands for the {@link de.sfuhrm.capsula.Stage#PREPARE prepare}
     * stage. */
    @Getter
    @Valid
    private List<Command> prepare;

    /** The commands for the {@link de.sfuhrm.capsula.Stage#BUILD build}
     * stage. */
    @Getter
    @Valid
    private List<Command> build;

    /** The file names of the generated package files after the build. */
    @Getter
    private List<String> packages;
}
