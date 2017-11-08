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
import de.sfuhrm.capsula.FileUtils;
import de.sfuhrm.capsula.yaml.command.MkdirCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 * Delegate for making a directory.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
class MkdirDelegate extends AbstractDelegate {

    /**
     * Creates a new instance.
     * @param targetBuilder the target builder this class is a delegate for.
     */
    MkdirDelegate(final TargetBuilder targetBuilder) {
        super(targetBuilder);
    }

    /** Makes the directories.
     * @param command the make directory command to execute.
     * */
    public void mkdir(final MkdirCommand command) {
        try {
            MDC.put("to", command.getTo());
            Objects.requireNonNull(command.getTo(), "to is null");
            Path toPath = getTargetBuilder().getTargetPath().resolve(
                    command.getTo());
            log.debug("Mkdir path {}", toPath);
            if (Files.exists(toPath)) {
                throw new BuildException("Target does exist: " + toPath);
            }
            if (!toPath.startsWith(getTargetBuilder().getTargetPath())) {
                throw new BuildException("Target is not within "
                        + "target directory: " + toPath);
            }
            FileUtils.mkdirs(toPath,
                    dir -> FileUtils.applyPermissionSetWithBuildException(
                            dir, command));
        } catch (IOException ex) {
            throw new BuildException("Problem in mkdir", ex);
        } finally {
            MDC.remove("to");
        }
    }
}
