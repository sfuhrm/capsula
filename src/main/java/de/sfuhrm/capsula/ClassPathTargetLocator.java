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
package de.sfuhrm.capsula;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Locates possible build targets in the class path.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
final class ClassPathTargetLocator implements TargetLocator {

    /**
     * All class path resources.
     */
    private static Set<ClassPath.ResourceInfo> resourceInfos;

    /**
     * Get the class path resources containing targets.
     *
     * @return a set of resource infos for resources inside the
     * {@link TargetLocator#TARGETS_DIRECTORY} hierarchy.
     * @throws IOException if an IO problem occurs.
     */
    private static synchronized
    Set<ClassPath.ResourceInfo> getClassPathResources() throws IOException {
        if (resourceInfos == null) {
            ClassPath classPath = ClassPath.from(Main.class.getClassLoader());
            resourceInfos = classPath.getResources()
                    .stream()
                    .filter(ri -> ri.getResourceName()
                            .startsWith(TARGETS_DIRECTORY)
                            || ri.getResourceName()
                            .startsWith(INCLUDE_DIRECTORY))
                    .collect(Collectors.toSet());
        }
        return resourceInfos;
    }

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
    @Override
    public Path extractTargetToTmp(final Path tempParent,
                                   final String target) throws IOException {
        Path targetPath = tempParent.resolve(target
                + "-layout").toAbsolutePath();
        if (Files.exists(targetPath)) {
            FileUtils.deleteRecursive(targetPath);
        }
        targetPath = Files.createDirectory(targetPath);
        log.debug("Target {} will be extracted to {}", target, targetPath);
        Set<String> targets = getTargets();
        log.debug("Targets in classpath: {}", targets);
        if (targets.isEmpty()) {
            throw new IllegalStateException("Targets not found");
        }
        if (!targets.contains(target)) {
            throw new NoSuchElementException("Target not found: " + target);
        }

        Path finalTargetPath = targetPath;
        getClassPathResources().stream().filter(cp ->
                cp.getResourceName().startsWith(
                        TARGETS_DIRECTORY + "/" + target)
        || cp.getResourceName().startsWith(INCLUDE_DIRECTORY))
                .forEach(cp -> {
                    InputStream is = null;
                    Path toPath = null;
                    try {
                        is = cp.asByteSource().openStream();
                        String[] parts = cp.getResourceName().split("/");
                        toPath = finalTargetPath.resolve(
                                parts[parts.length - 1]);
                        Files.copy(is, toPath);
                    } catch (IOException e) {
                        throw new RuntimeException("Problem copying resource "
                                + cp.getResourceName() + " to " + toPath, e);
                    }
                });
        return targetPath;
    }

    /**
     * Get the list of possible targets from the classpath.
     *
     * @return a set of target names.
     * @throws IOException if an IO problem occurs.
     */
    @Override
    public Set<String> getTargets() throws IOException {
        return getClassPathResources()
                .stream()
                .filter(ri ->
                        ri.getResourceName()
                                .startsWith(TARGETS_DIRECTORY + "/"))
                .map(ri -> ri.getResourceName().split("/")[1])
                .collect(Collectors.toSet());
    }
}
