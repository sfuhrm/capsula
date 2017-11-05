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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

/**
 * The reference to a git repository.
 * @author Stephan Fuhrmann
 */
public class GitRepository {
    /**
     * Where to get the project source code.
     */
    @Getter
    @Setter
    @NotNull
    @URL
    private String gitUrl;

    /**
     * The branch to pull.
     */
    @Getter
    @Setter
    @NotNull
    private String branch = "master";

    /**
     * The optional commit to checkout.
     */
    @Getter
    @Setter
    @javax.validation.constraints.Pattern(regexp = "[0-9a-f]{40,}")
    private String commit;

    /**
     * Get the project directory name from the GIT URL.
     * @return the project name extracted from the URL.
     * @throws IllegalArgumentException if there was no project name
     * identifyable.
     */
    public final String getGitProject() {
        Pattern p = Pattern.compile(".*/([^/]*)\\.git");
        Matcher m = p.matcher(getGitUrl());
        if (m.matches()) {
            return m.group(1);
        } else {
            throw new IllegalArgumentException("Can not determine got "
                    + "project from url '" + getGitUrl() + "'");
        }
    }
}
