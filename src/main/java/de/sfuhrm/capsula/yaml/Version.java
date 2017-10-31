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
 * Version information.
 *
 * @author Stephan Fuhrmann
 */
public class Version {

    /**
     * This is the source tar version by the author.
     */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    private String version;
    /**
     * This is the packaging release by the maintainer.
     */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    private String release;
    /**
     * This is the the release number for this version. The first release gets
     * number 1.
     */
    @Getter
    @Setter
    private int releaseNumber;
}
