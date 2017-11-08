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
package de.sfuhrm.capsula.targetlocator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Locates possible build targets.
 *
 * @author Stephan Fuhrmann
 */
public interface TargetLocator {

    /**
     * The directory of targets in the classpath.
     */
    String TARGETS_DIRECTORY = "targets";

    /**
     * The directory of includes in the classpath.
     */
    String INCLUDE_DIRECTORY = "include";

    /**
     * Extracts the target folder from the JAR archive to a temporary file on
     * disk.
     *
     * @param tempParent the directory to create the temporary files in.
     * @param target the target name to extract.
     * @return the name of the temporary directory where the target was
     * extracted to.
     * @throws IOException if an IO problem occurs.
     */
    Path extractTargetToTmp(Path tempParent,
                            String target) throws IOException;

    /**
     * Get the list of possible targets from the classpath.
     *
     * @return a set of target names.
     * @throws IOException if an IO problem occurs.
     */
    Set<String> getTargets() throws IOException;
}
