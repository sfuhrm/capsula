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
import de.sfuhrm.capsula.yaml.command.TargetCommand;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for delegates. It is holding a reference to the
 * {@link #targetBuilder TargetBuilder}.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
class AbstractDelegate {

    @Getter(AccessLevel.PROTECTED)
    private final TargetBuilder targetBuilder;

    public AbstractDelegate(TargetBuilder targetBuilder) {
        this.targetBuilder = Objects.requireNonNull(targetBuilder);
    }

    /**
     * Does owner/group/permission changes for a target path.
     *
     * @param command abstract target path description in the target command.
     */
    protected void applyTargetFileModifications(TargetCommand command) throws IOException {
        Path toPath = targetBuilder.getTargetPath().resolve(command.getTo());
        FileUtils.applyTargetFileModifications(toPath, command);
    }
}
