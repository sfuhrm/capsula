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
package de.sfuhrm.capsula.yaml.command;

/** A tuple of permissions and user / group.
 * All methods can return {@code null}.
 * */
public interface PermissionSet {
    /** Get the file mode in octal format.
     * @return a octal format String representing the {@link #getMode()}.
     * */
    String getOctal();

    /** The name of the owner to assign the created file / directory to.
     * @return the textual owner name, for example {@code "joe"}.
     * */
    String getOwner();

    /** The name of the group to assign the created file / directory to.
     * @return the textual group name, for example {@code "users"}.
     * */
    String getGroup();

    /** The UNIX permissions to assign the file / directory.
     * Has the format {@code rwxrwx---}.
     * @return the mode in UNIX bit notation.
     * */
    String getMode();
}
