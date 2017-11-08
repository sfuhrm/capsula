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

import de.sfuhrm.capsula.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Locator for targets based on a file system path.
 * */
@Slf4j
final class PathTargetLocator implements TargetLocator {

    /** Path to targets that are available. */
    private final Path targets;

    /** Creates a locator that extracts its targets from
     * the given path.
     * @param myTargets path to extract the targets from.
     * */
    PathTargetLocator(final Path myTargets) {
        this.targets = Objects.requireNonNull(myTargets);
    }

    @Override
    public Path extractTargetToTmp(final Path tempParent,
                                   final String target) throws IOException {

        log.debug("Extracting target {} to {}",
                target, tempParent);

        Path includeSource = targets.resolve(INCLUDE_DIRECTORY);
        Path tempTarget = tempParent.resolve(target);

        Files.list(includeSource).forEach(p ->
                FileUtils.copyRecursive(
                        p, tempTarget.resolve(p.getFileName()), q -> { }));

        Path targetSource = targets.resolve(TARGETS_DIRECTORY).resolve(target);

        Files.list(targetSource).forEach(p ->
                FileUtils.copyRecursive(
                        p, tempTarget.resolve(p.getFileName()), q -> { }));

        return tempTarget;
    }

    @Override
    public Set<String> getTargets() throws IOException {
        return Files.list(targets)
                .filter(p -> Files.isDirectory(p))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toSet());
    }
}
