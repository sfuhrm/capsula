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

import de.sfuhrm.capsula.BuildException;
import de.sfuhrm.capsula.yaml.command.RunCommand;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 * Delegate for running a command.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
class RunDelegate extends AbstractDelegate {

    /**
     * Creates a new instance.
     * @param targetBuilder the target builder this class is a delegate for.
     */
    RunDelegate(final TargetBuilder targetBuilder) {
        super(targetBuilder);
    }

    /** Parses the given command and splits it up into parts
     * needed by {@link ProcessBuilder#ProcessBuilder(String...)}.
     * @param command the command String which may contain quotes.
     *                Example: {@code echo "hello world"}.
     * @return the parsed command. The example will return the
     * list {@code ["echo", "hello world"]}.
     * @throws IllegalArgumentException if the quoting is not valid.
     * */
    static List<String> parse(final String command) {
        StringTokenizer stringTokenizer = new StringTokenizer(
                command, " \"", true);
        List<String> result = new ArrayList<>();

        StringBuilder quoted = null;
        while (stringTokenizer.hasMoreElements()) {
            String token = stringTokenizer.nextToken();
            switch (token) {
                case " ":
                    if (quoted != null) {
                        quoted.append(token);
                    }
                    break;
                case "\"":
                    if (quoted != null) {
                        result.add(quoted.toString());
                        quoted = null;
                    } else {
                        quoted = new StringBuilder();
                    }
                    break;
                default:
                    if (quoted != null) {
                        quoted.append(token);
                    } else {
                        result.add(token);
                    }
                    break;
            }
        }

        if (quoted != null) {
            throw new IllegalArgumentException("Command '"
                    + command + "' has illegal quoting");
        }

        return result;
    }

    /**
     * Runs a command redirecting the output to the logging facility.
     * @param command the command object containing the command to run.
     * @throws IOException if an I/O problem occurs running the command.
     */
    public void run(final RunCommand command) throws IOException {
        try {
            Objects.requireNonNull(command.getCommand(), "command is null");
            List<String> cmdArray = parse(command.getCommand());
            MDC.put("cmd", cmdArray.get(0));
            String cmdString = cmdArray.toString();
            ProcessBuilder builder = new ProcessBuilder(cmdArray);
            log.info("Starting command {}", command.getCommand());

            Process process = builder
                    .directory(getTargetBuilder().getTargetPath().toFile())
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .start();

            InputStreamConsumer stdin = new InputStreamConsumer(
                    process.getInputStream(),
                    line -> {
                        log.info(line);
                        if (getTargetBuilder().isVerbose()) {
                            System.out.println(line);
                        }
                    },
                    Charset.forName("UTF-8"));

            InputStreamConsumer stderr = new InputStreamConsumer(
                    process.getErrorStream(),
                    line -> {
                        log.warn(line);
                        if (getTargetBuilder().isVerbose()) {
                            System.err.println(line);
                        }
                    },
                    Charset.forName("UTF-8"));

            Thread stdinThread = new Thread(stdin,
                    getTargetBuilder().getTargetName() + "-stdin");
            stdinThread.start();
            Thread stderrThread = new Thread(stderr,
                    getTargetBuilder().getTargetName() + "-stderr");
            stderrThread.start();

            log.debug("Waiting for cmd {}", cmdString);
            process.waitFor();
            log.debug("Finished waiting for cmd {}", cmdString);
            int exitValue = process.exitValue();
            log.debug("Exit value for cmd {} is {}", cmdString, exitValue);
            if (exitValue != 0) {
                throw new BuildException("Command '"
                        + command.getCommand()
                        + "' returned exit value " + exitValue);
            }
        } catch (InterruptedException ex) {
            throw new BuildException(command.getCommand(), ex);
        } finally {
            MDC.remove("cmd");
        }
    }

}
