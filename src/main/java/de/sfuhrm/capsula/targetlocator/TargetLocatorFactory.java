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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/** Builds a new target locator.
 * @author Stephan Fuhrmann
 * */
@Slf4j
public final class TargetLocatorFactory {

    /** The optional path to a directory structure where
     * target layout directories
     * are located.
     * */
    @Getter @Setter
    private Path targetLayouts;

    /** Creates a new instance. */
    public TargetLocatorFactory() {

    }

    /** Creates a new target locator.
     * @return a new TargetLocator that either fetches its target layouts
     * from the given {@link #targetLayouts} path or from the built-in
     * targets in the classpath.
     * */
    public TargetLocator newInstance() {
        TargetLocator result;
        if (getTargetLayouts() != null) {
            log.debug("Using path target locator in {}",
                    getTargetLayouts());
            result = new PathTargetLocator(
                    getTargetLayouts());
        } else {
            log.debug("Using class path target locator");
            result = new ClassPathTargetLocator();
        }
        return result;
    }
}
