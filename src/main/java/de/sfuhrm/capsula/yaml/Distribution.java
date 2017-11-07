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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * An abstract linux distribution.
 *
 * @author Stephan Fuhrmann
 */
public class Distribution {

    /** The name of the package for this distribution.
     * The name can be change for each distribution.
     * */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    private String packageName; // inherited

    /** The command to use for building the package.
     * */
    @Getter
    @Setter
    @NotNull
    @NotBlank
    private String buildCommand;  // inherited

    /** The dependencies and other relations for distribution provided
     * packages. */
    @Getter
    @Setter
    @NotNull
    @Valid
    private Map<Relation.RelationType, List<Relation>> relations;

    /**
     * Get relations of a certain type.
     *
     * @param type the type as can be found in {@link Relation.RelationType}.
     * @return the sublist of relations of the given type. Can be
     * an empty list, but never {@code null}.
     */
    public final List<Relation> relationsFor(final String type) {
        Relation.RelationType enumType = Relation.RelationType.valueOf(type);
        return relations.getOrDefault(enumType, Collections.emptyList());
    }
}
