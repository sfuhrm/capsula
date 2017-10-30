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

import com.google.common.io.ByteSource;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
public class TargetLocator {
    /** The prefix of targets in the classpath. */
    public final static String TARGETS = "targets";
    
    /** All class path resources. */
    private static Set<ClassPath.ResourceInfo> resourceInfos;
    
    /** Get the class path resources containing targets. */
    private static Set<ClassPath.ResourceInfo> getClassPathResources() throws IOException {
        if (resourceInfos == null) {
            ClassPath classPath = ClassPath.from(Main.class.getClassLoader());
            resourceInfos = classPath.getResources()
                    .stream()
                    .filter(ri -> ri.getResourceName().startsWith(TARGETS))
                    .collect(Collectors.toSet());
        }
        return resourceInfos;
    }
    
    /** Extracts the target folder from the JAR archive to a temporary
     * file on disk.
     * @param target the target name to extract.
     * @return the name of the temporary directory where the target was extracted to.
     */
    public Path extractTargetToTmp(String target) throws IOException {
        Path targetPath = Files.createTempDirectory(target).toAbsolutePath();
        log.debug("Target {} will be extracted to {}", target, targetPath);
        
        Set<String> targets = getTargets();
        log.debug("Targets in classpath: {}", targets);
        if (targets.isEmpty()) {
            throw new IllegalStateException("Targets not found");
        }
        
        if (!targets.contains(target)) {
            throw new NoSuchElementException("Target not found: "+target);
        }
        
        Set<String> files = getTargetResources(target);
        log.debug("Target {} files: {}", target, files);
        if (files.isEmpty()) {
            throw new IllegalStateException("Target "+target+" contains no files");
        }
        
        for (String file : files) {
            try (InputStream is = getResourceAsStream(target, file)) {
                Path toPath = targetPath.resolve(file);
                Files.copy(is, toPath);
            }
        }
        
        return targetPath;
    }
        
    /** Opens a stream for the given resource. */
    private InputStream getResourceAsStream(String target, String resource) throws IOException {
        Optional<ByteSource> byteSource = getClassPathResources()
                .stream()
                .filter(ri -> ri.getResourceName().equals(TARGETS+"/"+target+"/"+resource))
                .map(ri -> ri.asByteSource())
                .findFirst();
        return byteSource.orElseThrow(() -> new IOException("Can't find "+target+"/"+resource)).openStream();
    }
    
    /** Get the list of possible target resources from the classpath. */
    private Set<String> getTargetResources(String target) throws IOException {
        return getClassPathResources()
                .stream()
                .filter(ri -> ri.getResourceName().startsWith(TARGETS+"/"+target))
                .map(ri -> ri.getResourceName().split("/")[2])
                .collect(Collectors.toSet());
    }
    
    /** Get the list of possible targets from the classpath. */
    public Set<String> getTargets() throws IOException {
        return getClassPathResources()
                .stream()
                .filter(ri -> ri.getResourceName().startsWith(TARGETS+"/"))
                .map(ri -> ri.getResourceName().split("/")[1])
                .collect(Collectors.toSet());
    }
}