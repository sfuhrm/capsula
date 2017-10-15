/*
 * Copyright 2017 Stephan Fuhrmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sfuhrm.capsula.targetbuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
class CopyDelegate extends AbstractDelegate {

    public CopyDelegate(TargetBuilder targetBuilder) {
        super(targetBuilder);
    }

    public void copy(String from, String to) {
        MDC.put("from", from);
        MDC.put("to", to);
        Objects.requireNonNull(from, "from is null");
        Objects.requireNonNull(to, "to is null");
        Path fromPath = getTargetBuilder().getLayoutDirectory().resolve(from);
        Path toPath = getTargetBuilder().getTargetPath().resolve(to);

        log.info("Copying {} to {}", from, to);
        log.debug("Copying path {} to {}", fromPath, toPath);

        if (!Files.exists(fromPath)) {
            throw new BuildException("Source does not exist: " + fromPath);
        }
        if (!fromPath.startsWith(getTargetBuilder().getLayoutDirectory())) {
            throw new BuildException("Source is not within layout directory: " + fromPath);
        }
        if (Files.exists(toPath)) {
            throw new BuildException("Target does exist: " + toPath);
        }
        if (!toPath.startsWith(getTargetBuilder().getTargetPath())) {
            throw new BuildException("Target is not within target directory: " + toPath);
        }

        if (Files.isDirectory(fromPath) || Files.isRegularFile(fromPath)) {
            copyRecursive(fromPath, toPath);

        } else {
            throw new BuildException("Unknown file type: " + fromPath);
        }
    }
    
    private void mkdirs(Path p) throws IOException {
        log.debug("mkdirs {}", p);
        Files.createDirectories(p);
    }

    private void copyRecursive(Path from, Path to) {
        try {
            if (Files.isRegularFile(from)) {
                if (Files.isDirectory(from.getParent())) {
                    mkdirs(to.getParent());
                }
                
                Files.copy(from, to);
                return;
            }

            if (Files.isDirectory(from)) {
                Path name = from.getFileName();
                Path target = to.resolve(name);
                mkdirs(target);

                Files.list(from).forEach(p -> {
                    copyRecursive(p, target.resolve(p.getFileName()));
                });
            }
        } catch (IOException exception) {
            throw new BuildException("Exception while copying from " + from + " to " + to, exception);
        }
    }
}
