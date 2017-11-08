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

import de.sfuhrm.capsula.yaml.command.PermissionSet;
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
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;

/**
 * Changes file attributes.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
public final class FileUtils {

    /**
     * No instance allowed.
     */
    private FileUtils() {
    }

    /**
     * Does owner/group/permission changes for a target path.
     * Throws a {@link BuildException} in case of a problem.
     *
     * @param toPath direct target path to modify.
     * @param permissions the permissions to take the
     *                    owner/group/permissions from.
     */
    public static void applyPermissionSetWithBuildException(final Path toPath,
                                          final PermissionSet permissions) {
        try {
            applyPermissionSet(toPath, permissions);
        } catch (IOException e) {
            throw new BuildException(toPath.toString(), e);
        }
    }

    /**
     * Does owner/group/permission changes for a target path.
     *
     * @param toPath direct target path to modify.
     * @param permissions the permissions to take the
     *                    owner/group/permissions from.
     * @throws IOException when one of the operations didn't succeed.
     */
    public static void applyPermissionSet(final Path toPath,
                                          final PermissionSet permissions)
            throws IOException {
        if (permissions.getOwner() != null) {
            changeOwner(toPath, permissions.getOwner());
        }
        if (permissions.getGroup() != null) {
            changeGroup(toPath, permissions.getGroup());
        }
        if (permissions.getMode() != null) {
            changeMode(toPath, permissions.getMode());
        }
    }

    /** Recursively copies files and directories.
     * @param from the source to copy from.
     * @param to the target path to copy to.
     * @param newPathConsumer receives every newly
     *                        created file or directory.
     * */
    public static void copyRecursive(final Path from,
                           final Path to,
                           final Consumer<Path> newPathConsumer) {
        try {
            if (Files.isRegularFile(from)) {
                if (Files.isDirectory(from.getParent())) {
                    FileUtils.mkdirs(to.getParent(), newPathConsumer);
                }
                Files.copy(from, to);
                newPathConsumer.accept(to);
                return;
            }
            if (Files.isDirectory(from)) {
                Path name = from.getFileName();
                Path target = to.resolve(name);
                FileUtils.mkdirs(target, newPathConsumer);
                Files.list(from).forEach(p -> {
                    copyRecursive(p,
                            target.resolve(p.getFileName()),
                            newPathConsumer);
                });
            }
        } catch (IOException exception) {
            throw new BuildException("Exception while copying from "
                    + from + " to " + to, exception);
        }
    }

    /**
     * Creates directories.
     *
     * @param p the directory path to create.
     * @param newDirectoryConsumer a consumer for newly created
     *                             directories. Gets each directory that is new.
     * @throws IOException if an error occurs.
     * @see Files#createDirectories(java.nio.file.Path,
     * java.nio.file.attribute.FileAttribute...)
     */
    public static void mkdirs(final Path p,
              final Consumer<Path> newDirectoryConsumer) throws IOException {
        log.debug("mkdirs {}", p);

        Path absolute = p.toAbsolutePath();
        int existsIndex = -1;
        Path current = absolute;
        for (int i = 0; i < absolute.getNameCount(); i++) {
            if (Files.isDirectory(current) && existsIndex == -1) {
                existsIndex = i;
            }
            current = current.getParent();
        }
        Files.createDirectories(absolute);

        current = absolute;
        for (int i = 0; i < existsIndex; i++) {
            newDirectoryConsumer.accept(current);
            current = current.getParent();
        }
    }

    /**
     * Changes the owner of the given path.
     *
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
     *
     * @param p the path to change the group of.
     * @param groupName the new group name for the path.
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
     *
     * @param p the path to change the access mode for.
     * @param mode the new access mode, for example {@code rwx---rwx}.
     * @throws IOException if an error occurs.
     */
    public static void changeMode(final Path p,
            final String mode) throws IOException {
        log.debug("chmod {} to {}", p, mode);
        Set<PosixFilePermission> permissions
                = PosixFilePermissions.fromString(mode);
        Files.getFileAttributeView(p,
                PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS)
                .setPermissions(permissions);
    }

    /**
     * Deletes a path and its children.
     *
     * @param p the path to delete.
     * @throws BuildException in case of an IO exception.
     */
    public static void deleteRecursive(final Path p) {
        try {
            if (Files.isRegularFile(p)) {
                log.debug("Deleting file {}", p);
                Files.delete(p);
            }
            if (Files.isSymbolicLink(p)) {
                log.debug("Deleting symbolic link {}", p);
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
