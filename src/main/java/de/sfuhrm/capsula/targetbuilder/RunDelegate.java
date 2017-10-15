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
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
class RunDelegate extends AbstractDelegate {
    
    public RunDelegate(TargetBuilder targetBuilder) throws IOException {
        super(targetBuilder);
    }

    public void run(String... cmd) {
        String cmdString = Arrays.toString(cmd);
        try {
            MDC.put("cmd", cmd);
            Objects.requireNonNull(cmd, "cmd is null");
            
            ProcessBuilder builder = new ProcessBuilder(cmd);
            
            log.debug("Starting cmd {}", cmdString);
            Process process = builder
                    .directory(getTargetBuilder().getTargetPath().toFile())
                    .inheritIO()
                    .start();
            log.debug("Waiting for cmd {}", cmdString);
            process.waitFor();
            log.debug("Finished waiting for cmd {}", cmdString);
            int exitValue = process.exitValue();
            log.debug("Exit value for cmd {} is {}", cmdString, exitValue);
            if (exitValue != 0) {
                throw new BuildException(cmdString+" returned exit value "+exitValue);
            }
        } catch (InterruptedException | IOException ex) {
            throw new BuildException(cmdString, ex);
        }
    }
}
