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
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

/**
 * Descriptor file for a application to generate a package for.
 *
 * @author Stephan Fuhrmann
 */
public class Capsula {

    /**
     * The name of the package to gernerate.
     */
    @Getter
    @NotNull
    @NotBlank
    private String packageName;

    /** The command to use for building the package.
     * @see #cleanCommand
     * */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    private String buildCommand;

    /** The command to use for cleaning the package.
     * @see #buildCommand
     * */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    private String cleanCommand;

    /**
     * The author of the software in the package.
     */
    @Getter
    @NotNull
    @Valid
    private NameEmail author;
    /**
     * The maintainer of the package.
     */
    @Getter
    @NotNull
    @Valid
    private NameEmail maintainer;
    /**
     * The homepage of the project.
     */
    @Getter
    @NotNull
    @NotBlank
    @URL
    private String homepage;
    /**
     * Where to get the project source code.
     */
    @Getter
    @NotNull
    @Valid
    private GitRepository git;

    /**
     * Short summary what this project is.
     */
    @Getter
    @NotNull
    @NotBlank
    private String shortSummary;
    /**
     * Long multi-line description of the project.
     */
    @Getter
    @NotNull
    @Size(min = 1)
    private List<String> longDescription;

    /**
     * The license for the project.
     */
    @Getter
    @NotNull
    @Valid
    private License license;
    /**
     * Debian specific information.
     */
    @Getter
    @Valid
    private Debian debian;

    /**
     * Redhat specific information.
     */
    @Getter
    @Valid
    private Redhat redhat;

    /**
     * Archlinux specific information.
     */
    @Getter
    @Valid
    private Archlinux archlinux;

    /**
     * Which targets to create packages for.
     */
    @Getter
    @Valid
    @Size(min = 1)
    private Set<String> targets;
    /**
     * A changelog with newest versions coming first. The first version is the
     * version to generate.
     */
    @Getter
    @Valid
    @Size(min = 1)
    private List<VersionWithChanges> versions;
    /**
     * Installation commmands.
     */
    @Getter
    @Valid
    @NotNull
    private List<Command> install;

    /**
     * Calculates {@link Version#releaseNumber} fields.
     */
    public final void calculateReleaseNumbers() {
        for (VersionWithChanges versionWithChanges : versions) {
            List<VersionWithChanges> sameVersion = versions.stream()
                    .filter(v -> v.getVersion()
                            .equals(versionWithChanges.getVersion()))
                    .collect(Collectors.toList());
            int total = sameVersion.size();
            int indexInverse = sameVersion.indexOf(versionWithChanges);
            int index = total - indexInverse - 1;
            versionWithChanges.setReleaseNumber(index);
        }
    }
}
