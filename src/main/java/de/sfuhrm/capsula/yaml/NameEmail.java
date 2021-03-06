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

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;

/**
 * Author/maintainer name and email address.
 *
 * @author Stephan Fuhrmann
 */
public class NameEmail {

    /** The name of a human being, for example {@code "Peter Smith"}. */
    @Getter
    @NotNull
    @Pattern(regexp = ".* .*")
    private String name;

    /** The E-Mail address of a human being, for
     * example {@code "peter.smith@gmail.com"}. */
    @Getter
    @NotNull
    @Email
    private String email;
}
