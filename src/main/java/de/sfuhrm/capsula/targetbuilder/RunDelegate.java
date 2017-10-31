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
package de.sfuhrm.capsula.targetbuilder;
import de.sfuhrm.capsula.yaml.command.RunCommand;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;
/**
 * Delegate for running a command.
 * @author Stephan Fuhrmann
 */
@Slf4j
class RunDelegate extends AbstractDelegate {
    public RunDelegate(TargetBuilder targetBuilder) throws IOException {
        super(targetBuilder);
    }
    public void run(RunCommand command) throws IOException {
        try {
            MDC.put("cmd", command.getCommand());
            Objects.requireNonNull(command.getCommand(), "command is null");
            String[] cmdArray = command.getCommand().split(" ");
            String cmdString = Arrays.toString(cmdArray);
            ProcessBuilder builder = new ProcessBuilder(cmdArray);
            log.info("Starting command {}", command.getCommand());
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
        } catch (InterruptedException ex) {
            throw new BuildException(command.getCommand(), ex);
        }
    }
}
