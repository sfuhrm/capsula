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

import de.sfuhrm.capsula.yaml.command.MkdirCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
class MkdirDelegate extends AbstractDelegate {

    public MkdirDelegate(TargetBuilder targetBuilder) {
        super(targetBuilder);
    }

    public void mkdir(MkdirCommand command) {
        try {
            MDC.put("to", command.getTo());
            Objects.requireNonNull(command.getTo(), "to is null");
            Path toPath = getTargetBuilder().getTargetPath().resolve(command.getTo());
            
            log.debug("Mkdir path {}", toPath);
            
            if (Files.exists(toPath)) {
                throw new BuildException("Target does exist: " + toPath);
            }
            if (!toPath.startsWith(getTargetBuilder().getTargetPath())) {
                throw new BuildException("Target is not within target directory: " + toPath);
            }
            mkdirs(toPath, getTargetBuilder().getTargetPath(), command);
        } catch (IOException ex) {
            throw new BuildException("Problem in mkdir", ex);
        }
    }
    
    void mkdirs(Path p, Path targetPath, MkdirCommand command) throws IOException {
        log.debug("mkdirs {}", p);
        Files.createDirectories(p);
        
        for (int i = 0; i < p.getNameCount(); i++) {
            Path sub = p.subpath(0, i);
            if (!sub.equals(targetPath) && sub.startsWith(targetPath)) {
                applyTargetFileModifications(sub, command);
            }
        }
    }
}
