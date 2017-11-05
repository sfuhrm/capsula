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

import de.sfuhrm.capsula.FileUtils;
import de.sfuhrm.capsula.yaml.command.CopyCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import de.sfuhrm.capsula.yaml.command.TargetCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 * Delegate for copying one file or directory.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
class CopyDelegate extends AbstractDelegate {

    /**
     * Creates a new instance.
     * @param targetBuilder the target builder this class is a delegate for.
     */
    CopyDelegate(final TargetBuilder targetBuilder) {
        super(targetBuilder);
    }

    /** Executes the given command.
     * @param command the command object to execute the action for.
     * */
    void copy(final CopyCommand command) {
        MDC.put("from", command.getFrom());
        MDC.put("to", command.getTo());
        try {
            Objects.requireNonNull(command.getFrom(), "from is null");
            Objects.requireNonNull(command.getTo(), "to is null");
            Path fromPath = getTargetBuilder().getLayoutDirectory().resolve(
                    command.getFrom());
            Path toPath = getTargetBuilder().getTargetPath()
                    .resolve(command.getTo());
            log.info("Copying {} to {}", command.getFrom(), command.getTo());
            log.debug("Copying path {} to {}", fromPath, toPath);
            if (!Files.exists(fromPath)) {
                throw new BuildException("Source does not exist: " + fromPath);
            }
            if (!fromPath.startsWith(getTargetBuilder().getLayoutDirectory())) {
                throw new BuildException("Source is not "
                        + "within layout directory: "
                        + fromPath);
            }
            if (Files.exists(toPath)) {
                throw new BuildException("Target does exist: " + toPath);
            }
            if (!toPath.startsWith(getTargetBuilder().getTargetPath())) {
                throw new BuildException("Target is not "
                        + "within target directory: "
                        + toPath);
            }
            if (Files.isDirectory(fromPath) || Files.isRegularFile(fromPath)) {
                copyRecursive(fromPath, toPath, command);
            } else {
                throw new BuildException("Unknown file type: " + fromPath);
            }
        } finally {
            MDC.remove("from");
            MDC.remove("to");
        }
    }

    /** Makes the directories plus sub directories.
     * @param p the path to generate.
     * @param command the file attributes for the created directories.
     * @throws IOException if there is a problem making the directory or
     * applying the directory permissions.
     * */
    private void mkdirs(final Path p,
                        final TargetCommand command) throws IOException {
        log.debug("mkdirs {}", p);
        Files.createDirectories(p);
        applyTargetFileModifications(command);
        // TODO this just changes the deepest path
        // TODO there is a MkdirDelegate that does it better
    }

    /** Recursively copies files and directories.
     * @param from the source to copy from.
     * @param to the target path to copy to.
     * @param command the file permissions of the target files and directories.
     * */
    private void copyRecursive(final Path from,
                               final Path to, final TargetCommand command) {
        try {
            if (Files.isRegularFile(from)) {
                if (Files.isDirectory(from.getParent())) {
                    mkdirs(to.getParent(), command);
                }
                Files.copy(from, to);
                FileUtils.applyTargetFileModifications(to, command);
                return;
            }
            if (Files.isDirectory(from)) {
                Path name = from.getFileName();
                Path target = to.resolve(name);
                mkdirs(target, command);
                Files.list(from).forEach(p -> {
                    copyRecursive(p, target.resolve(p.getFileName()), command);
                });
            }
        } catch (IOException exception) {
            throw new BuildException("Exception while copying from "
                    + from + " to " + to, exception);
        }
    }
}
