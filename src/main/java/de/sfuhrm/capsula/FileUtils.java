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

import de.sfuhrm.capsula.targetbuilder.BuildException;
import de.sfuhrm.capsula.yaml.command.TargetCommand;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Changes file attributes.
 * @author Stephan Fuhrmann
 */
@Slf4j
public final class FileUtils {

    /** No instance allowed. */
    private FileUtils() {
    }

    /** Does owner/group/permission changes for a target path.
     * @param toPath direct target path to modify.
     * @param command  the command to take the owner/group/permissions from.
     * @throws IOException when one of the operations didn't succeed.
     */
    public static void applyTargetFileModifications(final Path toPath,
            final TargetCommand command) throws IOException {
        if (command.getOwner() != null) {
            changeOwner(toPath, command.getOwner());
        }

        if (command.getGroup() != null) {
            changeGroup(toPath, command.getGroup());
        }

        if (command.getMode() != null) {
            changeMode(toPath, command.getMode());
        }
    }

    /**
     * Creates directories.
     * @param p the directory path to create.
     * @throws IOException if an error occurs.
     * @see Files#createDirectories(java.nio.file.Path,
     * java.nio.file.attribute.FileAttribute...)
     */
    public static void mkdirs(final Path p) throws IOException {
        log.debug("mkdirs {}", p);
        Files.createDirectories(p);
    }

    /**
     * Changes the owner of the given path.
     * @param p the path to change the owner of.
     * @param ownerName the new owner name for the path.
     * @throws IOException if an error occurs.
     */
    public static void changeOwner(final Path p,
            final String ownerName) throws IOException {
        log.debug("chown {} to {}", p, ownerName);
        FileSystem fileSystem = p.getFileSystem();
        UserPrincipalLookupService lookupService
                = fileSystem.getUserPrincipalLookupService();
        UserPrincipal owner = lookupService.lookupPrincipalByName(ownerName);
        Files.setOwner(p, owner);
    }

    /**
     * Changes the group of the given path.
     * @param p the path to change the group of.
     * @param groupName  the new group name for the path.
     * @throws IOException if an error occurs.
     */
    public static void changeGroup(final Path p,
            final String groupName) throws IOException {
        log.debug("chgrp {} to {}", p, groupName);
        FileSystem fileSystem = p.getFileSystem();
        UserPrincipalLookupService lookupService
                = fileSystem.getUserPrincipalLookupService();
        GroupPrincipal group = lookupService
                .lookupPrincipalByGroupName(groupName);
        Files.getFileAttributeView(p,
                PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS)
                .setGroup(group);
    }

    /**
     * Change the access mode for the given path.
     * @param p the path to change the access mode for.
     * @param mode the new access mode, for example {@code rwx---rwx}.
     * @throws IOException if an error occurs.
     */
    public static void changeMode(final Path p,
            final String mode) throws IOException {
        log.debug("chmod {} to {}", p, mode);
        Set<PosixFilePermission> permissions =
                PosixFilePermissions.fromString(mode);
        Files.getFileAttributeView(p,
                PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS)
                .setPermissions(permissions);
    }

    /** Deletes a path and its children.
     * @param p the path to delete.
     * @throws BuildException in case of an IO exception.
     */
    public static void deleteRecursive(final Path p) {
        try {
            if (Files.isRegularFile(p)) {
                log.debug("Deleting file {}", p);
                Files.delete(p);
            }
            if (Files.isDirectory(p)) {
                log.debug("Deleting directory contents {}", p);
                Files.list(p).forEach(t -> deleteRecursive(t));
                log.debug("Deleting directory {}", p);
                Files.delete(p);
            }
        } catch (IOException exception) {
            throw new BuildException("Error deleting recursively: " + p,
                    exception);
        }
    }
}
